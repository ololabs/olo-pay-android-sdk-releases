// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;

import com.olo.olopay.api.ApiResultCallback;
import com.olo.olopay.api.IOloPayAPI;
import com.olo.olopay.api.IOloPayApiInitializer;
import com.olo.olopay.api.InitCompleteCallback;
import com.olo.olopay.api.OloPayAPI;
import com.olo.olopay.api.OloPayApiInitializer;
import com.olo.olopay.bootstrap.ApplicationProvider;
import com.olo.olopay.data.ICvvTokenParams;
import com.olo.olopay.data.ICvvUpdateToken;
import com.olo.olopay.data.IPaymentMethod;
import com.olo.olopay.data.IPaymentMethodParams;
import com.olo.olopay.data.OloPayEnvironment;
import com.olo.olopay.data.SetupParameters;
import com.olo.olopay.googlepay.Config;
import com.olo.olopay.googlepay.Environment;
import com.olo.olopay.googlepay.IGooglePayContext;
import com.olo.olopay.googlepay.Result;
import com.olo.olopaytestharness.BuildConfig;
import com.olo.olopaytestharness.R;
import com.olo.olopaytestharness.models.ILogger;
import com.olo.olopaytestharness.models.IOloApiSettings;
import com.olo.olopaytestharness.models.IUserSettings;
import com.olo.olopaytestharness.models.IWorkerStatus;
import com.olo.olopaytestharness.oloapi.entities.Basket;
import com.olo.olopaytestharness.oloapi.OloApiClient;
import com.olo.olopaytestharness.oloapi.OloApiClientExtensionsKt;
import com.olo.olopaytestharness.oloapi.entities.Order;
import com.olo.olopaytestharness.oloapi.entities.PaymentType;

import java.util.concurrent.Executor;

import kotlinx.serialization.ExperimentalSerializationApi;

public class JavaOloPayImplementation implements ISDKImplementation {
    private final ILogger _logger;
    private final IWorkerStatus _workerStatus;

    private Basket _googlePayBasket = null;

    public JavaOloPayImplementation(ILogger logger, IWorkerStatus workerStatus) {
        _logger = logger;
        _workerStatus = workerStatus;
    }

    public static void initializeSdk(InitCompleteCallback callback) {
        Application application = ApplicationProvider.getCurrentApplication();
        if (application == null) {
            return;
        }

        OloPayEnvironment oloPayEnv = BuildConfig.DEBUG ? OloPayEnvironment.Test : OloPayEnvironment.Production;

        Environment googlePayEnv =
                application.getResources().getBoolean(R.bool.google_pay_production_env) ? Environment.Production : Environment.Test;

        boolean existingPaymentMethodsRequired = application.getResources().getBoolean(R.bool.google_pay_existing_payment_methods_required);
        Config googlePayConfig = new Config(googlePayEnv, "Olo Pay SDK - Java", "US", existingPaymentMethodsRequired);

        //In a production app this could be mocked for testing purposes
        IOloPayApiInitializer initializer = new OloPayApiInitializer();
        initializer.setup(application, new SetupParameters(oloPayEnv, googlePayConfig), callback);
    }

    @Override
    @OptIn(markerClass = ExperimentalSerializationApi.class)
    public void submitCvv(@NonNull Context context, @Nullable ICvvTokenParams params, @NonNull IOloApiSettings apiSettings, @NonNull IUserSettings userSettings) {
        _workerStatus.isBusy().setValue(true);

        if(params == null) {
            _logger.logText("CVV Params not valid");
            _workerStatus.isBusy().setValue(false);
            return;
        }

        IOloPayAPI api = new OloPayAPI(); //This could be mocked for testing purposes
        api.createCvvUpdateToken(context, params, new ApiResultCallback<ICvvUpdateToken>() {
            @Override
            public void onSuccess(ICvvUpdateToken cvvUpdateToken) {
                new MainThreadExecutor().execute(() -> {
                    // In a production application you would use this token with the Olo Ordering API
                    // to revalidate a saved card
                    _logger.logCvvToken(cvvUpdateToken);

                    if (!apiSettings.getCompletePayment().getValue()) {
                        _workerStatus.isBusy().setValue(false);
                        return;
                    }

                    // Send the payment method to Olo's Ordering API
                    OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(apiSettings);
                    OloApiClientExtensionsKt.createBasketWithProductFromSettings(client, apiSettings, new ApiResultCallback<Basket>() {
                        @Override
                        public void onSuccess(Basket basket) {
                            new MainThreadExecutor().execute(() -> _logger.logBasket(basket));

                            PaymentType paymentType = new PaymentType(cvvUpdateToken);

                            //Basket created... time to submit it and get an order
                            submitBasket(client, basket, paymentType, apiSettings, userSettings);
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            new MainThreadExecutor().execute(() -> {
                                // In a production application you would likely want to catch each exception type individually
                                // and take appropriate action
                                _logger.logException(e);
                                _workerStatus.isBusy().setValue(false);
                            });
                        }
                    });
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                new MainThreadExecutor().execute(() -> {
                    // In a production application you would likely want to catch each exception type individually
                    // and take appropriate action
                    _logger.logException(e);
                    _workerStatus.isBusy().setValue(false);
                });
            }
        });
    }

    @NonNull
    @Override
    public ILogger getLogger() {
        return _logger;
    }

    @NonNull
    @Override
    public IWorkerStatus getWorkerStatus() {
        return _workerStatus;
    }

    @Override
    @OptIn(markerClass = ExperimentalSerializationApi.class)
    public void submitPayment(@NonNull Context context, @Nullable IPaymentMethodParams params, @NonNull IOloApiSettings oloApiSettings, @NonNull IUserSettings userSettings) {
        // This disables buttons... make sure to set this to false at the end of every flow so that
        // the buttons can be enabled again
        _workerStatus.isBusy().setValue(true);

        _logger.logText("Card Is Valid: " + (params != null));

        IOloPayAPI api = new OloPayAPI(); //This could be mocked for testing purposes
        api.createPaymentMethod(context, params, new ApiResultCallback<IPaymentMethod>() {
            @Override
            public void onSuccess(IPaymentMethod paymentMethod) {
                new MainThreadExecutor().execute(() -> {
                    _logger.logPaymentMethod(paymentMethod);

                    if (!oloApiSettings.getCompletePayment().getValue()) {
                        _workerStatus.isBusy().setValue(false);
                        return;
                    }

                    // Send the payment method to Olo's Ordering API
                    OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(oloApiSettings);
                    OloApiClientExtensionsKt.createBasketWithProductFromSettings(client, oloApiSettings, new ApiResultCallback<Basket>() {
                        @Override
                        public void onSuccess(Basket basket) {
                            new MainThreadExecutor().execute(() -> _logger.logBasket(basket));

                            PaymentType paymentType = new PaymentType(paymentMethod);

                            //Basket created... time to submit it and get an order
                            submitBasket(client, basket, paymentType, oloApiSettings, userSettings);
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            new MainThreadExecutor().execute(() -> {
                                // In a real application you would most likely want to check for more specific
                                // exception types and take appropriate action
                                _logger.logException(e);
                                _workerStatus.isBusy().setValue(false);
                            });
                        }
                    });
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                new MainThreadExecutor().execute(() -> {
                    // In a real application you would most likely want to check for more specific
                    // exception types and take appropriate action
                    _logger.logException(e);
                    _workerStatus.isBusy().setValue(false);
                });
            }
        });
    }

    @Override
    @OptIn(markerClass = ExperimentalSerializationApi.class)
    public void submitGooglePay(@NonNull IGooglePayContext context, @NonNull IOloApiSettings apiSettings, @NonNull IUserSettings userSettings) {
        _workerStatus.isBusy().setValue(true);

        _googlePayBasket = null;
        _logger.logText(KotlinOloPayImplementation.GooglePaySubmitHeader);

        if (!apiSettings.getCompletePayment().getValue()) {
            context.present();
            _workerStatus.isBusy().setValue(false);
            return;
        }

        OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(apiSettings);
        OloApiClientExtensionsKt.createBasketWithProductFromSettings(client, apiSettings, new ApiResultCallback<Basket>() {
            @Override
            public void onSuccess(Basket basket) {
                _googlePayBasket = basket;
                new MainThreadExecutor().execute(() -> {
                    _logger.logBasket(basket);
                    context.present("USD", (int)(_googlePayBasket.getTotal() * 100));
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                new MainThreadExecutor().execute(() -> {
                    _logger.logException(e);
                    _workerStatus.isBusy().setValue(false);
                });
            }
        });
    }

    @Override
    @OptIn(markerClass = ExperimentalSerializationApi.class)
    public void onGooglePayResult(@NonNull Result result, @NonNull IOloApiSettings apiSettings, @NonNull IUserSettings userSettings) {
        _logger.logGooglePayResult(result);

        if (!apiSettings.getCompletePayment().getValue() || !(result instanceof Result.Completed)) {
            _workerStatus.isBusy().setValue(false);
            return;
        }

        if (_googlePayBasket == null) {
            _logger.logText("Google pay basket not created");
            _workerStatus.isBusy().setValue(false);
            return;
        }

        IPaymentMethod paymentMethod = ((Result.Completed)result).getPaymentMethod();
        PaymentType paymentType = new PaymentType(paymentMethod);

        OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(apiSettings);
        submitBasket(client, _googlePayBasket, paymentType, apiSettings, userSettings);
    }

    @OptIn(markerClass = ExperimentalSerializationApi.class)
    private void submitBasket(OloApiClient client, Basket basket, PaymentType paymentType, IOloApiSettings apiSettings, IUserSettings userSettings) {
        OloApiClientExtensionsKt.submitBasketFromSettings(client, apiSettings, userSettings, paymentType, basket.getId(), new ApiResultCallback<Order>() {
            @Override
            public void onSuccess(Order order) {
                new MainThreadExecutor().execute(() -> {
                    _logger.logOrder(order);
                    _workerStatus.isBusy().setValue(false);
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                new MainThreadExecutor().execute(() -> {
                    // In a real application you would most likely want to check for more specific
                    // exception types and take appropriate action
                    _logger.logException(e);
                    _workerStatus.isBusy().setValue(false);
                });
            }
        });
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public void execute(Runnable runnable) {
            handler.post(runnable);
        }
    }
}

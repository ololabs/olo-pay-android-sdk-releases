// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.olo.olopay.api.IOloPayAPI;
import com.olo.olopay.api.OloPayAPI;
import com.olo.olopay.bootstrap.ApplicationProvider;
import com.olo.olopay.api.IOloPayApiInitializer;
import com.olo.olopay.api.OloPayApiInitializer;
import com.olo.olopay.controls.PaymentCardDetailsForm;
import com.olo.olopay.controls.PaymentCardDetailsMultiLineView;
import com.olo.olopay.controls.PaymentCardDetailsSingleLineView;
import com.olo.olopay.api.ApiResultCallback;
import com.olo.olopay.data.IPaymentMethod;
import com.olo.olopay.api.InitCompleteCallback;
import com.olo.olopay.data.IPaymentMethodParams;
import com.olo.olopay.data.OloPayEnvironment;
import com.olo.olopay.data.SetupParameters;
import com.olo.olopay.googlepay.Config;
import com.olo.olopay.googlepay.Environment;
import com.olo.olopay.googlepay.IGooglePayContext;
import com.olo.olopay.googlepay.Result;
import com.olo.olopaytestharness.BuildConfig;
import com.olo.olopaytestharness.R;
import com.olo.olopaytestharness.oloapi.Basket;
import com.olo.olopaytestharness.oloapi.OloApiClient;
import com.olo.olopaytestharness.oloapi.OloApiClientExtensionsKt;
import com.olo.olopaytestharness.oloapi.Order;
import com.olo.olopaytestharness.viewmodels.ActivityViewModel;
import com.olo.olopaytestharness.viewmodels.SettingsViewModel;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.Executor;

public class JavaSDKImplementation implements SDKImplementation {
    private final ActivityViewModel _viewModel;
    private final SettingsViewModel _settings;
    private Basket _googlePayBasket = null;

    public JavaSDKImplementation(ActivityViewModel viewModel, SettingsViewModel settings) {
        _viewModel = viewModel;
        _settings = settings;
    }

    // NOTE: This initialization should normally happen on app startup. For purposes of the test
    // harness app, where we want to test SDK initialization in both Java and Kotlin, we are
    // delaying it until launching the language-specific activity. See the SDK documentation about
    // initializing the SDK for more information about initializing on app startup
    public static void initializeSDK(InitCompleteCallback callback) {
        Application application = ApplicationProvider.getCurrentApplication();

        OloPayEnvironment oloPayEnv = BuildConfig.DEBUG ? OloPayEnvironment.Test : OloPayEnvironment.Production;
        boolean freshInstall = application.getResources().getBoolean(R.bool.fresh_install);

        Environment googlePayEnv =
                application.getResources().getBoolean(R.bool.google_pay_production_env) ? Environment.Production : Environment.Test;

        boolean existingPaymentMethodsRequired = application.getResources().getBoolean(R.bool.google_pay_existing_payment_methods_required);
        Config googlePayConfig = new Config(googlePayEnv, "Olo Pay SDK - Java", "US", existingPaymentMethodsRequired);

        IOloPayApiInitializer initializer = new OloPayApiInitializer();
        initializer.setup(application, new SetupParameters(oloPayEnv, freshInstall, googlePayConfig), callback);
    }

    @NonNull
    @Override
    public ActivityViewModel getViewModel() { return _viewModel; }

    @NonNull
    @Override
    public SettingsViewModel getSettings() { return _settings; }

    @Override
    public boolean getCompletePayment() {
        Boolean complete = _settings.getCompleteOloPayPayment().getValue();
        return complete != null ? complete : false;
    }

    @Override
    public void submitPayment(@NonNull PaymentCardDetailsSingleLineView cardDetails) {
        _viewModel.logText(_viewModel.getSingleLineCardSubmitHeader());
        submitPayment(cardDetails.getContext(), cardDetails.getPaymentMethodParams());
    }

    @Override
    public void submitPayment(@NonNull PaymentCardDetailsMultiLineView cardDetails) {
        _viewModel.logText(_viewModel.getMultiLineCardSubmitHeader());
        submitPayment(cardDetails.getContext(), cardDetails.getPaymentMethodParams());
    }

    @Override
    public void submitPayment(@NonNull PaymentCardDetailsForm cardDetails) {
        _viewModel.logText(_viewModel.getCardFormSubmitHeader());
        submitPayment(cardDetails.getContext(), cardDetails.getPaymentMethodParams());
    }

    private void submitPayment(Context context, IPaymentMethodParams params) {
        // This disables buttons... make sure to set this to false at the end of every flow so that
        // the buttons can be enabled again
        _viewModel.getSubmissionInProgress().postValue(true);

        _viewModel.logText("Card Is Valid: " + (params != null));

        IOloPayAPI api = new OloPayAPI(); //This could be mocked for testing purposes
        api.createPaymentMethod(context, params, new ApiResultCallback<IPaymentMethod>() {
            @Override
            public void onSuccess(IPaymentMethod paymentMethod) {
                new MainThreadExecutor().execute(() -> {
                    _viewModel.logPaymentMethod(paymentMethod);

                    if (!getCompletePayment()) {
                        _viewModel.getSubmissionInProgress().setValue(false);
                        return;
                    }

                    // Send the payment method to Olo's Ordering API
                    OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(_settings);
                    OloApiClientExtensionsKt.createBasketWithProductFromSettings(client, _settings, new ApiResultCallback<Basket>() {
                        @Override
                        public void onSuccess(Basket basket) {
                            new MainThreadExecutor().execute(() -> _viewModel.logBasket(basket));

                            //Basket created... time to submit it and get an order
                            submitBasket(client, basket, paymentMethod);
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            new MainThreadExecutor().execute(() -> {
                                // In a real application you would most likely want to check for more specific
                                // exception types and take appropriate action
                                _viewModel.logException(e);
                                _viewModel.getSubmissionInProgress().setValue(false);
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
                    _viewModel.logException(e);
                    _viewModel.getSubmissionInProgress().setValue(false);
                });
            }
        });
    }

    @Override
    public void submitGooglePay(@NotNull IGooglePayContext context) {
        _viewModel.getSubmissionInProgress().setValue(true);

        _googlePayBasket = null;
        _viewModel.logText(_viewModel.getGooglePaySubmitHeader());

        if (!getCompletePayment()) {
            context.present();
            _viewModel.getSubmissionInProgress().setValue(false);
            return;
        }

        OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(_settings);
        OloApiClientExtensionsKt.createBasketWithProductFromSettings(client, _settings, new ApiResultCallback<Basket>() {
            @Override
            public void onSuccess(Basket basket) {
                _googlePayBasket = basket;
                new MainThreadExecutor().execute(() -> {
                    _viewModel.logBasket(basket);
                    context.present("USD", (int)(_googlePayBasket.getTotal() * 100));
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                new MainThreadExecutor().execute(() -> {
                    _viewModel.logException(e);
                    _viewModel.getSubmissionInProgress().setValue(false);
                });
            }
        });
    }

    @Override
    public void onGooglePayResult(@NonNull Result result) {
        _viewModel.logGooglePayResult(result);

        if (!getCompletePayment() || !(result instanceof Result.Completed)) {
            _viewModel.getSubmissionInProgress().setValue(false);
            return;
        }

        if (_googlePayBasket == null) {
            _viewModel.logText("Google pay basket not created");
            _viewModel.getSubmissionInProgress().setValue(false);
            return;
        }

        IPaymentMethod paymentMethod = ((Result.Completed)result).getPaymentMethod();

        OloApiClient client = OloApiClientExtensionsKt.createApiClientFromSettings(_settings);
        submitBasket(client, _googlePayBasket, paymentMethod);
    }

    private void submitBasket(OloApiClient client, Basket basket, IPaymentMethod paymentMethod) {
        OloApiClientExtensionsKt.submitBasketFromSettings(client, _settings, paymentMethod, basket.getId(), new ApiResultCallback<Order>() {
            @Override
            public void onSuccess(Order order) {
                new MainThreadExecutor().execute(() -> {
                    _viewModel.logOrder(order);
                    _viewModel.getSubmissionInProgress().setValue(false);
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                new MainThreadExecutor().execute(() -> {
                    // In a real application you would most likely want to check for more specific
                    // exception types and take appropriate action
                    _viewModel.logException(e);
                    _viewModel.getSubmissionInProgress().setValue(false);
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

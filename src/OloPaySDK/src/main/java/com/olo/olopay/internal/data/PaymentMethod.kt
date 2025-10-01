// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data
import android.os.Parcelable
import com.olo.olopay.data.Address
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.internal.googlepay.GooglePaymentData
import com.stripe.android.model.PaymentMethod as StripePaymentMethod
import com.stripe.android.model.wallets.Wallet
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class PaymentMethod internal constructor(
    internal val _paymentMethod: StripePaymentMethod,
    internal val _googlePayData: GooglePaymentData?,
    internal val _googlePayConfig: GooglePayConfig?,
    internal val _address: Address,
) : Parcelable, IPaymentMethod {

    internal constructor(
        paymentMethod: StripePaymentMethod,
        googlePayData: GooglePaymentData? = null,
        googlePayConfig: GooglePayConfig? = null,
    ) : this(
        paymentMethod,
        googlePayData,
        googlePayConfig,
        Address.merge(
            paymentMethod.billingDetails?.address,
            paymentMethod.card?.country,
            googlePayConfig
        ),
    )

    override val id: String
        get() = _paymentMethod.id ?: ""

    override val last4: String
        get() = _paymentMethod.card?.last4 ?: ""

    override val cardType: CardBrand
        get() {
            val brand = _paymentMethod.card?.brand
            return if (brand != null) CardBrand.convertFrom(brand) else CardBrand.Unknown
        }

    override val expirationMonth: Int?
        get() = _paymentMethod.card?.expiryMonth

    override val expirationYear: Int?
        get() = _paymentMethod.card?.expiryYear

    override val postalCode: String
        get() = billingAddress.postalCode

    override val countryCode: String
        get() = billingAddress.countryCode

    override val email: String
        get() {
            _googlePayConfig?.emailRequired?.let {
                if (it) {
                    return _paymentMethod.billingDetails?.email ?: ""
                }
            }

            return ""
        }

    override val googlePayCardDescription: String
        get() = _googlePayData?.description ?: ""

    override val billingAddress: Address
        get() = _address

    override val fullName: String
        get() {
            _googlePayConfig?.fullNameRequired?.let {
                if (it) {
                    return _paymentMethod.billingDetails?.name ?: ""
                }
            }

            return ""
        }

    override val phoneNumber: String
        get() {
            _googlePayConfig?.phoneNumberRequired?.let {
                if (it) {
                    return _paymentMethod.billingDetails?.phone ?: ""
                }
            }

            return ""
        }

    override val isGooglePay: Boolean
        get() = _paymentMethod.card?.wallet is Wallet.GooglePayWallet

    override val environment: OloPayEnvironment
        get() = if (_paymentMethod.liveMode) OloPayEnvironment.Production else OloPayEnvironment.Test

    override fun toString(): String {
        val properties = listOf(
            "\n${PaymentMethod::id.name}=${id}",
            "${PaymentMethod::last4.name}=${last4}",
            "${PaymentMethod::cardType.name}=${cardType}",
            "${PaymentMethod::expirationMonth.name}=${expirationMonth}",
            "${PaymentMethod::expirationYear.name}=${expirationYear}",
            "${PaymentMethod::googlePayCardDescription.name}=${googlePayCardDescription}",
            "${PaymentMethod::postalCode.name}=${postalCode}",
            "${PaymentMethod::countryCode.name}=${countryCode}",
            "${PaymentMethod::fullName.name}=${fullName}",
            "${PaymentMethod::email.name}=${email}",
            "${PaymentMethod::phoneNumber.name}=${phoneNumber}",
            "${PaymentMethod::isGooglePay.name}=${isGooglePay}",
            "${PaymentMethod::environment.name}=${environment}",
            "${PaymentMethod::billingAddress.name}=${billingAddress.toString(4)}"
        )

        return "${this.javaClass.name}(${properties.joinToString(",\n")})"
    }
}
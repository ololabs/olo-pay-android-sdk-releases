// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data
import android.os.Parcelable
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.IPaymentMethod
import com.stripe.android.model.PaymentMethod as StripePaymentMethod
import com.stripe.android.model.wallets.Wallet
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class PaymentMethod constructor(internal val _paymentMethod: StripePaymentMethod) :
    Parcelable, IPaymentMethod {

    override val id: String?
        get() = _paymentMethod.id

    override val last4: String?
        get() = _paymentMethod.card?.last4

    override val cardType: CardBrand?
        get() {
            val brand = _paymentMethod.card?.brand
            return if (brand != null) CardBrand.convertFrom(brand) else null
        }

    override val expirationMonth: Int?
        get() = _paymentMethod.card?.expiryMonth

    override val expirationYear: Int?
        get() = _paymentMethod.card?.expiryYear

    override val postalCode: String?
        get() = _paymentMethod.billingDetails?.address?.postalCode

    override val country: String?
        get() = _paymentMethod.card?.country

    override val isGooglePay: Boolean
        get() = _paymentMethod.card?.wallet is Wallet.GooglePayWallet

    override fun toString(): String {
        val properties = listOf<String>(
            "${PaymentMethod::id.name}=${id}",
            "${PaymentMethod::last4.name}=${last4}",
            "${PaymentMethod::cardType.name}=${cardType}",
            "${PaymentMethod::expirationMonth.name}=${expirationMonth}",
            "${PaymentMethod::expirationYear.name}=${expirationYear}",
            "${PaymentMethod::postalCode.name}=${postalCode}",
            "${PaymentMethod::country.name}=${country}",
            "${PaymentMethod::isGooglePay.name}=${isGooglePay}"
        )

        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}
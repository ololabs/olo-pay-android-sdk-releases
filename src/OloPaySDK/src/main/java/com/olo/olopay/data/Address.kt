// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import android.os.Parcelable
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.internal.extensions.getOrDefault
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import com.stripe.android.model.Address as StripeAddress

@Parcelize
/**
 * Class to represent an address.
 *
 * **_Note:_** The fields on this class map to Google's
 * [Address](https://developers.google.com/pay/api/android/reference/response-objects#Address)
 * response object.
 */
data class Address(
    /** The postal or zip code */
    val postalCode: String,

    /** The ISO 3166-1 alpha-2 country code */
    val countryCode: String,

    /** The first line of the address */
    val address1: String = "",

    /** The second line of the address */
    val address2: String = "",

    /** The third line of the address */
    val address3: String = "",

    /** The city, town, neighborhood, or suburb */
    val locality: String = "",

    /** A country subdivision, such as a state or province */
    val administrativeArea: String = "",

    /** The sorting code */
    val sortingCode: String = ""
) : Parcelable {

    fun toString(indentSpaces: Int = 0): String {
        val indent = " ".repeat(indentSpaces)

        val properties = listOf(
            "\n${indent}${Address::address1.name}=${address1}",
            "${indent}${Address::address2.name}=${address2}",
            "${indent}${Address::address3.name}=${address3}",
            "${indent}${Address::locality.name}=${locality}",
            "${indent}${Address::administrativeArea.name}=${administrativeArea}",
            "${indent}${Address::postalCode.name}=${postalCode}",
            "${indent}${Address::sortingCode.name}=${sortingCode}",
            "${indent}${Address::countryCode.name}=${countryCode}",
        )

        return "${this.javaClass.name}(${properties.joinToString(",\n")})"
    }

    internal companion object {
        fun merge(
            address: StripeAddress?,
            defaultCountryCode: String?,
            config: GooglePayConfig?
        ): Address {
            val countryCodeOverride =
                if (!defaultCountryCode.isNullOrEmpty()) defaultCountryCode else ""

            if (address == null) {
                return Address(
                    postalCode = "",
                    countryCode = countryCodeOverride
                )
            }

            // Stripe's implementation with Google Pay appears to not be respecting the billing address
            // configuration settings, so we are manually filtering out data
            var billingAddressRequired = true
            config?.fullBillingAddressRequired?.let {
                billingAddressRequired = it
            }

            return Address(
                postalCode = address.postalCode ?: "",
                countryCode = if (address.country.isNullOrEmpty()) countryCodeOverride else address.country ?: "",
                address1 = if (billingAddressRequired) address.line1 ?: "" else "",
                address2 = if (billingAddressRequired) address.line2 ?: "" else "",
                locality = if (billingAddressRequired) address.city ?: "" else "",
                administrativeArea = if (billingAddressRequired) address.state ?: "" else ""
            )
        }

        fun from(json: JSONObject?): Address {
            if (json == null) {
                return Address(
                    postalCode = "",
                    countryCode = ""
                )
            }

            return Address(
                postalCode = json.getOrDefault(POSTAL_CODE_KEY, ""),
                countryCode = json.getOrDefault(COUNTRY_CODE_KEY, ""),
                address1 = json.getOrDefault(ADDRESS1_KEY, ""),
                address2 = json.getOrDefault(ADDRESS2_KEY, ""),
                address3 = json.getOrDefault(ADDRESS3_KEY, ""),
                locality = json.getOrDefault(LOCALITY_KEY, ""),
                administrativeArea = json.getOrDefault(ADMINISTRATIVE_AREA_KEY, ""),
                sortingCode = json.getOrDefault(SORTING_CODE_KEY, "")
            )
        }

        private const val POSTAL_CODE_KEY = "postalCode"
        private const val COUNTRY_CODE_KEY = "countryCode"
        private const val ADDRESS1_KEY = "address1"
        private const val ADDRESS2_KEY = "address2"
        private const val ADDRESS3_KEY = "address3"
        private const val LOCALITY_KEY = "locality"
        private const val ADMINISTRATIVE_AREA_KEY = "administrativeArea"
        private const val SORTING_CODE_KEY = "sortingCode"
    }
}
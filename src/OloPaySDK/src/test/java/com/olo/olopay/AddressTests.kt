// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.data.Address
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.googlepay.GooglePayEnvironment
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import com.stripe.android.model.Address as StripeAddress

class AddressTests {
    @Test
    fun from_withNullJsonObject_returnsEmptyAddress() {
        val json: JSONObject? = null
        assertEmptyAddress(Address.from(json))
    }

    @Test
    fun from_withInvalidJsonKeys_returnsEmptyAddress() {
        assertEmptyAddress(Address.from(JSONObject()))
    }

    @Test
    fun from_withMinAddressFieldsJson_fieldsMapCorrectly() {
        // NOTE: Google Pay Min address format includes name, but we don't put it in our
        // address object
        val address = Address.from(JSONObject()
            .put("name", "Ron Idaho")
            .put("postalCode", "12345")
            .put("countryCode", "US")
        )

        assertEquals("12345", address.postalCode)
        assertEquals("US", address.countryCode)
        assertEquals("", address.address1)
        assertEquals("", address.address2)
        assertEquals("", address.address3)
        assertEquals("", address.locality)
        assertEquals("", address.administrativeArea)
        assertEquals("", address.sortingCode)
    }

    @Test
    fun from_withFullAddressFieldsJson_fieldsMapCorrectly() {
        // NOTE: Google Pay Full address format includes name and phone, but we don't put them in our
        // address object
        val address = Address.from(JSONObject()
            .put("name", "Foo Bar")
            .put("postalCode", "54321")
            .put("countryCode", "CA")
            .put("phoneNumber", "555-555-5555")
            .put("address1", "c/o Ron")
            .put("address2", "123 Business Way")
            .put("address3", "Suite ABC")
            .put("locality", "New York")
            .put("administrativeArea", "NY")
            .put("sortingCode", "SortMe")
        )

        assertEquals("54321", address.postalCode)
        assertEquals("CA", address.countryCode)
        assertEquals("c/o Ron", address.address1)
        assertEquals("123 Business Way", address.address2)
        assertEquals("Suite ABC", address.address3)
        assertEquals("New York", address.locality)
        assertEquals("NY", address.administrativeArea)
        assertEquals("SortMe", address.sortingCode)
    }

    @Test
    fun merge_allNullParameters_returnsEmptyAddress() {
        assertEmptyAddress(Address.merge(null, null, null))
    }

    @Test
    fun merge_nullAddress_withDefaultCountryCode_returnsAddressWithCountryCode() {
        val address = Address.merge(null, "US", null)
        assertEquals("", address.postalCode)
        assertEquals("US", address.countryCode)
        assertEquals("", address.address1)
        assertEquals("", address.address2)
        assertEquals("", address.address3)
        assertEquals("", address.locality)
        assertEquals("", address.administrativeArea)
        assertEquals("", address.sortingCode)
    }

    @Test
    fun merge_withStripeAddress_returnsOloPayAddress_withAllFieldsMapped() {
        val stripeAddress = StripeAddress(
            city = "New York",
            country = "US",
            line1 = "c/o Ron",
            line2 = "123 Business Ave",
            postalCode = "12345",
            state = "NY"
        )

        val address = Address.merge(stripeAddress, null, null)
        assertEquals("12345", address.postalCode)
        assertEquals("US", address.countryCode)
        assertEquals("c/o Ron", address.address1)
        assertEquals("123 Business Ave", address.address2)
        assertEquals("", address.address3)
        assertEquals("New York", address.locality)
        assertEquals("NY", address.administrativeArea)
        assertEquals("", address.sortingCode)
    }

    @Test
    fun merge_stripeAddressWithCountryCode_defaultCountryCodeIgnored() {
        val stripeAddress = StripeAddress(
            city = "New York",
            country = "US",
            line1 = "c/o Ron",
            line2 = "123 Business Ave",
            postalCode = "12345",
            state = "NY"
        )

        val address = Address.merge(stripeAddress, "CA", null)
        assertEquals("US", address.countryCode)
    }

    @Test
    fun merge_stripeAddressWithEmptyCountryCode_setsDefaultCountryCode() {
        val stripeAddress = StripeAddress(
            city = "New York",
            country = "",
            line1 = "c/o Ron",
            line2 = "123 Business Ave",
            postalCode = "12345",
            state = "NY"
        )

        val address = Address.merge(stripeAddress, "CA", null)
        assertEquals("CA", address.countryCode)
    }

    @Test
    fun merge_stripeAddressWithNullCountryCode_setsDefaultCountryCode() {
        val stripeAddress = StripeAddress(
            city = "New York",
            country = null,
            line1 = "c/o Ron",
            line2 = "123 Business Ave",
            postalCode = "12345",
            state = "NY"
        )

        val address = Address.merge(stripeAddress, "CA", null)
        assertEquals("CA", address.countryCode)
    }

    @Test
    fun merge_billingAddressRequired_allFieldsMapped() {
        val stripeAddress = StripeAddress(
            city = "New York",
            country = "US",
            line1 = "c/o Ron",
            line2 = "123 Business Ave",
            postalCode = "12345",
            state = "NY"
        )

        val config = GooglePayConfig(
            environment = GooglePayEnvironment.Test,
            companyName = "ABC Corp",
            fullBillingAddressRequired = true
        )

        val address = Address.merge(stripeAddress, "CA", config)
        assertEquals("12345", address.postalCode)
        assertEquals("US", address.countryCode)
        assertEquals("c/o Ron", address.address1)
        assertEquals("123 Business Ave", address.address2)
        assertEquals("", address.address3)
        assertEquals("New York", address.locality)
        assertEquals("NY", address.administrativeArea)
        assertEquals("", address.sortingCode)
    }

    @Test
    fun merge_billingAddressNotRequired_OnlyPostalCodeAndCountryCodeMapped() {
        val stripeAddress = StripeAddress(
            city = "New York",
            country = "US",
            line1 = "c/o Ron",
            line2 = "123 Business Ave",
            postalCode = "12345",
            state = "NY"
        )

        val config = GooglePayConfig(
            environment = GooglePayEnvironment.Test,
            companyName = "ABC Corp",
            fullBillingAddressRequired = false
        )

        val address = Address.merge(stripeAddress, "CA", config)
        assertEquals("12345", address.postalCode)
        assertEquals("US", address.countryCode)
        assertEquals("", address.address1)
        assertEquals("", address.address2)
        assertEquals("", address.address3)
        assertEquals("", address.locality)
        assertEquals("", address.administrativeArea)
        assertEquals("", address.sortingCode)
    }

    private fun assertEmptyAddress(address: Address) {
        assertEquals("", address.postalCode)
        assertEquals("", address.countryCode)
        assertEquals("", address.address1)
        assertEquals("", address.address2)
        assertEquals("", address.address3)
        assertEquals("", address.locality)
        assertEquals("", address.administrativeArea)
        assertEquals("", address.sortingCode)
    }
}
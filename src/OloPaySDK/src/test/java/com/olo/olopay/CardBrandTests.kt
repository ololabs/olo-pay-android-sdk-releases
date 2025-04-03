// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.data.CardBrand
import org.junit.Assert.*
import org.junit.Test

import com.stripe.android.model.CardBrand as StripeCardBrand

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CardBrandTests {
    @Test
    fun convertFrom_stripeBrand_returnsOloPayBrand_visa() {
        assertEquals(CardBrand.Visa, CardBrand.convertFrom(StripeCardBrand.Visa))
    }

    @Test
    fun convertFrom_stripeBrand_returnsOloPayBrand_amex() {
        assertEquals(CardBrand.AmericanExpress, CardBrand.convertFrom(StripeCardBrand.AmericanExpress))
    }

    @Test
    fun convertFrom_stripeBrand_returnsOloPayBrand_discover() {
        assertEquals(CardBrand.Discover, CardBrand.convertFrom(StripeCardBrand.Discover))
    }

    @Test
    fun convertFrom_stripeBrand_returnsOloPayBrand_mastercard() {
        assertEquals(CardBrand.MasterCard, CardBrand.convertFrom(StripeCardBrand.MasterCard))
    }

    @Test
    fun convertFrom_jcbStripeBrand_returnsOloPayBrand_unsupported() {
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom(StripeCardBrand.JCB))
    }

    @Test
    fun convertFrom_dinersClubStripeBrand_returnsOloPayBrand_unsupported() {
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom(StripeCardBrand.DinersClub))
    }

    @Test
    fun convertFrom_unionPayStripeBrand_returnsOloPayBrand_unsupported() {
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom(StripeCardBrand.UnionPay))
    }

    @Test
    fun convertFrom_unknownStripeBrand_returnsOloPayBrand_unknown() {
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom(StripeCardBrand.Unknown))
    }

    @Test
    fun convertFrom_visaString_returnsOloPayBrand_visa() {
        assertEquals(CardBrand.Visa, CardBrand.convertFrom("VISA"))
        assertEquals(CardBrand.Visa, CardBrand.convertFrom("visa"))
        assertEquals(CardBrand.Visa, CardBrand.convertFrom("vIsA"))
    }

    @Test
    fun convertFrom_amexString_returnsOloPayBrand_amex() {
        assertEquals(CardBrand.AmericanExpress, CardBrand.convertFrom("AMEX"))
        assertEquals(CardBrand.AmericanExpress, CardBrand.convertFrom("amex"))
        assertEquals(CardBrand.AmericanExpress, CardBrand.convertFrom("aMEx"))
    }

    @Test
    fun convertFrom_discoverString_returnsOloPayBrand_discover() {
        assertEquals(CardBrand.Discover, CardBrand.convertFrom("DISCOVER"))
        assertEquals(CardBrand.Discover, CardBrand.convertFrom("discover"))
        assertEquals(CardBrand.Discover, CardBrand.convertFrom("dIsCoVEr"))
    }

    @Test
    fun convertFrom_mastercardString_returnsOloPayBrand_mastercard() {
        assertEquals(CardBrand.MasterCard, CardBrand.convertFrom("MASTERCARD"))
        assertEquals(CardBrand.MasterCard, CardBrand.convertFrom("mastercard"))
        assertEquals(CardBrand.MasterCard, CardBrand.convertFrom("mASterCaRD"))
    }

    @Test
    fun convertFrom_interacString_returnsOloPayBrand_unsupported() {
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom("INTERAC"))
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom("interac"))
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom("iNTerAc"))
    }

    @Test
    fun convertFrom_jcbString_returnsOloPayBrand_unsupported() {
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom("JCB"))
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom("jcb"))
        assertEquals(CardBrand.Unsupported, CardBrand.convertFrom("JcB"))
    }

    @Test
    fun convertFrom_invalidString_returnsOloPayBrand_unknown() {
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom("foobar"))
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom("visa2"))
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom("SoMeRandoMString"))
    }
}
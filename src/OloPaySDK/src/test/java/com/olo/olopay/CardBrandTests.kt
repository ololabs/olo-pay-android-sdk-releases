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
    fun convertFrom_StripeBrand_To_OloPayBrand_Visa() {
        assertEquals(CardBrand.Visa, CardBrand.convertFrom(StripeCardBrand.Visa))
    }

    @Test
    fun convertFrom_StripeBrand_To_OloPayBrand_Amex() {
        assertEquals(CardBrand.AmericanExpress, CardBrand.convertFrom(StripeCardBrand.AmericanExpress))
    }

    @Test
    fun convertFrom_StripeBrand_To_OloPayBrand_Discover() {
        assertEquals(CardBrand.Discover, CardBrand.convertFrom(StripeCardBrand.Discover))
    }

    @Test
    fun convertFrom_StripeBrand_To_OloPayBrand_MasterCard() {
        assertEquals(CardBrand.MasterCard, CardBrand.convertFrom(StripeCardBrand.MasterCard))
    }

    @Test
    fun convertFrom_UnsupportedStripeBrand_To_Unknown() {
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom(StripeCardBrand.JCB))
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom(StripeCardBrand.DinersClub))
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom(StripeCardBrand.UnionPay))
        assertEquals(CardBrand.Unknown, CardBrand.convertFrom(StripeCardBrand.Unknown))
    }
}
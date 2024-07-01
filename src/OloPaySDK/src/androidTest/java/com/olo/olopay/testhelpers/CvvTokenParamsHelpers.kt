// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.testhelpers

import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.internal.data.CvvTokenParams

class CvvTokenParamsHelpers {
    companion object {
        fun createValid(): ICvvTokenParams {
            return CvvTokenParams(validCvvValue)
        }

        fun createIncorrectParamsType(): ICvvTokenParams {
            return IncorrectCvvParamsType()
        }

        fun createInvalidWithTooFewDigits(): ICvvTokenParams {
            return CvvTokenParams(invalidCvvValueWithTooFewDigits)
        }

        fun createInvalidWithTooManyDigits(): ICvvTokenParams {
            return CvvTokenParams(invalidCvvValueWithTooManyDigits)
        }

        fun createInvalidWithCharacters(): ICvvTokenParams {
            return CvvTokenParams(invalidCvvValueWithCharacters)
        }

        fun createInvalidWithEmptyCvv(): ICvvTokenParams {
            return CvvTokenParams(emptyCvvValue)
        }

        private class IncorrectCvvParamsType : ICvvTokenParams {
            internal val cvvValue: String
                get() = ""
        }
        private const val validCvvValue = "123"
        private const val invalidCvvValueWithTooFewDigits = "12"
        private const val invalidCvvValueWithTooManyDigits = "12345"
        private const val invalidCvvValueWithCharacters = "1b3"
        private const val emptyCvvValue = ""
    }
}
// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.annotation.IntRange
import androidx.core.content.withStyledAttributes
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.R
import com.olo.olopay.controls.callbacks.CardInputListener
import com.olo.olopay.data.*
import com.olo.olopay.internal.data.CardState
import com.olo.olopay.internal.data.OloTextWatcher
import com.olo.olopay.internal.data.PaymentMethodParams
import com.stripe.android.view.CardInputWidget
import com.stripe.android.view.CardValidCallback as StripeCardValidCallback
import com.stripe.android.view.CardValidCallback.Fields as StripeCardField
import com.stripe.android.view.CardInputListener.FocusField as StripeFocusField
import com.stripe.android.view.CardInputListener as StripeCardInputListener

/**
 * Convenience view for collecting card details from a user. All card fields are combined into a
 * single-line view
 * <hr class="spacer">
 *
 * #### Important:
 *
 * _User-entered card details are intentionally restricted for PCI compliance
 * <hr class="divider">
 *
 * ## Styling/Branding
 *
 * This class can be styled in the following ways:
 * 1. Override general-purpose resource values
 * 2. Override resource values specific to [PaymentCardDetailsSingleLineView]
 * 3. Override styles specific to [PaymentCardDetailsSingleLineView]
 * <hr class="spacer">
 *
 * ### Override general-purpose resource values
 *
 * Overriding general-purpose resource values is the easiest way to control the look/feel of the views
 * in the Olo Pay SDK. Changing these values (listed below) will apply to not just [PaymentCardDetailsSingleLineView],
 * but also [PaymentCardDetailsMultiLineView], and in a limited way, [PaymentCardDetailsForm]. To
 * override the resources, simply define resources in your app with the same names listed below
 * <hr class="spacer">
 *
 * **Dimen Resources**
 * ```
 * olopay_textsize
 * olopay_errortext_textsize
 * ```
 *
 * **Color Resources**
 * ```
 * olopay_hintcolor
 * olopay_edittext_textcolor
 * olopay_errortext_textcolor
 * ```
 * <hr class="spacer">
 *
 * ### Override resource values specific to [PaymentCardDetailsSingleLineView]
 *
 * Overriding these resource values (listed below) provides for more flexibility, but they apply ONLY to
 * [PaymentCardDetailsSingleLineView]. The default values for these resources are defined in terms of
 * the general-purpose resources listed in the previous section. Overriding these values take precedence
 * over values defined by the general-purpose resources.
 * <hr class="spacer">
 *
 * **Dimen Resources**
 * ```
 * olopay_paymentcarddetailssinglelineview_edittext_textsize
 * olopay_paymentcarddetailssinglelineview_errortext_textsize
 * ```
 *
 * **Color Resources**
 * ```
 * olopay_paymentcarddetailssinglelineview_edittext_textcolor
 * olopay_paymentcarddetailssinglelineview_edittext_errorcolor
 * olopay_paymentcarddetailssinglelineview_edittext_hintcolor
 * olopay_paymentcarddetailssinglelineview_cardicon_tint
 * olopay_paymentcarddetailssinglelineview_errortext_textcolor
 * ```
 * <hr class="spacer">
 *
 * ### Override styles specific to [PaymentCardDetailsSingleLineView]
 *
 * This is the most difficult option for customization but also allows for the most flexibility. One
 * potential use case for overriding styles would be to change the font (though there are other
 * means of doing that via application-wide themes, etc)
 * <hr class="spacer">
 *
 * When overriding styles, note the following:
 * - The style names **MUST** match what is listed below. You can choose a different parent but it should be a similar type of parent
 * - Some resource values are not defined by the styles that can be overridden so when taking this approach you may need to override resource values as well
 * - The styles listed below use some of the properties from above (see code below for which ones)
 * - Overridden styles take precedence... if you override a style that is using one of the resource values from above, you will need to define it in your style
 * - Because these are custom controls with custom behavior, not all standard Android style attributes will have an effect on the control
 * <hr class="spacer">
 *
 * The styles that can be defined that will be applied to this view are listed here:
 * ```
 * <style name="OloPay.PaymentCardDetailsSingleLineView.TextAppearance" parent="TextAppearance.AppCompat">
 *     <item name="android:textColor">@color/olopay_paymentcarddetailssinglelineview_edittext_textcolor</item>
 *     <item name="android:textSize">@dimen/olopay_paymentcarddetailssinglelineview_edittext_textsize</item>
 *     <!-- Provide other customizations here -->
 * </style>
 *
 * <style name="OloPay.PaymentCardDetailsSingleLineView.ErrorText">
 *     <item name="android:textAppearance">@style/OloPay.PaymentCardDetailsSingleLineView.ErrorText.TextAppearance</item>
 *     <!-- Provide other customizations here -->
 * </style>
 *
 * <style name="OloPay.PaymentCardDetailsSingleLineView.ErrorText.TextAppearance" parent="TextAppearance.AppCompat">
 *     <item name="android:textColor">@color/olopay_paymentcarddetailssinglelineview_errortext_textcolor</item>
 *     <item name="android:textSize">@dimen/olopay_paymentcarddetailssinglelineview_errortext_textsize</item>
 *     <!-- Provide other customizations here -->
 * </style>
 * ```
 * @constructor Creates a new instance of [PaymentCardDetailsSingleLineView]
 */
class PaymentCardDetailsSingleLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr), StripeCardValidCallback, StripeCardInputListener {
    private lateinit var _inputWidget: CardInputWidget
    private lateinit var _errorText: TextView
    private var _displayErrors = defaultDisplayErrors
    private var cardState = CardState()

    private val _layoutRunnable = Runnable {
        measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
        layout(left, top, right, bottom)
    }

    init {
        inflate(context, R.layout.paymentcarddetailssinglelineview, this)

        getInputWidget()

        context.withStyledAttributes(attrs, R.styleable.PaymentCardDetailsSingleLineView) {
            //NOTE: These temp variables are required. For some reason the values don't get set correctly if we set the class properties directly in one statement
            val postalCodeRequiredAttrValue = getBoolean(R.styleable.PaymentCardDetailsSingleLineView_postalCodeRequired, defaultPostalCodeRequired)
            postalCodeRequired = postalCodeRequiredAttrValue

            val postalCodeEnabledAttrValue = getBoolean(R.styleable.PaymentCardDetailsSingleLineView_postalCodeEnabled, defaultPostalCodeEnabled)
            postalCodeEnabled = postalCodeEnabledAttrValue

            val usZipCodeRequiredAttrValue = getBoolean(R.styleable.PaymentCardDetailsSingleLineView_usZipCodeRequired, defaultUSZipCodeRequired)
            usZipCodeRequired = usZipCodeRequiredAttrValue

            val displayErrorsAttrValue = getBoolean(R.styleable.PaymentCardDetailsSingleLineView_displayErrors, defaultDisplayErrors)
            displayErrors = displayErrorsAttrValue
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        getErrorText()
        getInputWidget()
    }

    // IMPORTANT: This is needed to fix a bug when this view is used in a React Native app
    override fun requestLayout() {
        super.requestLayout()

        if (this::_inputWidget.isInitialized)
            _inputWidget.requestLayout()

        post(_layoutRunnable)
    }


    // This is a workaround for when the control is not instantiated via an XML layout file (like with ReactNative)...
    // In that case onFinishInflate() doesn't get called so _inputWidget wasn't getting initialized. Accessing the widget
    // through this method ensures it gets initialized.
    private fun getErrorText() : TextView? {
        if (this::_errorText.isInitialized) {
            return _errorText
        }

        return try {
            _errorText = findViewById(R.id.error_text)
            _errorText
        } catch (e: Exception) {
            null
        }
    }

    // This is a workaround for when the control is not instantiated via an XML layout file (like with ReactNative)...
    // In that case onFinishInflate() doesn't get called so _inputWidget wasn't getting initialized. Accessing the widget
    // through this method ensures it gets initialized.
    private fun getInputWidget() : CardInputWidget? {
        if (this::_inputWidget.isInitialized) {
            return _inputWidget
        }

        try {
            _inputWidget = findViewById(R.id.input_widget)
        } catch (e: Exception) {
            return null
        }

        _inputWidget.setCardInputListener(this)
        _inputWidget.setCardValidCallback(this)

        _inputWidget.setCardNumberTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                cardState.onFieldTextChanged(CardField.CardNumber, editable?.toString() ?: "")
            }
        })

        _inputWidget.setExpiryDateTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                cardState.onFieldTextChanged(CardField.Expiration, editable?.toString() ?: "")
            }
        })

        _inputWidget.setCvcNumberTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                cardState.onFieldTextChanged(CardField.Cvc, editable?.toString() ?: "")
            }
        })

        _inputWidget.setPostalCodeTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                cardState.onFieldTextChanged(CardField.PostalCode, editable?.toString() ?: "")
            }
        })

        return _inputWidget
    }

    /**
     * Enables or disables the control
     * @param enabled Whether or not the control should be enabled/disabled
     */
    override fun setEnabled(enabled: Boolean) {
        getInputWidget()?.isEnabled = enabled
    }

    /**
     * True if the form is enabled and can accept user input, false otherwise
     */
    override fun isEnabled() = getInputWidget()?.isEnabled ?: false

    /**
     * Provides a snapshot of the current state of each card field
     */
    val fieldStates: Map<CardField, ICardFieldState>
        get() = cardState.fieldStates

    /**
     * `true` if all fields are complete and valid, otherwise `false`
     */
    val isValid
        get() = cardState.isValid

    /**
     * Whether or not the postal code is enabled. It is enabled by default.
     * Disabling the postal code field is discouraged because it could
     * impact authorization success rates
     *
     * **Important:** This can be set in xml with `app:postalCodeEnabled`
     * */
    var postalCodeEnabled
        get() = getInputWidget()?.postalCodeEnabled ?: true
        set(newValue) {
            if (newValue == postalCodeEnabled)
                return

            getInputWidget()?.postalCodeEnabled = newValue
            cardState.postalCodeEnabled = newValue
        }

    /**
     * If both [postalCodeEnabled] and [postalCodeRequired] are `true`, then postal
     * code is a required field. If [postalCodeEnabled] is `false`, this value is ignored
     *
     * **Important:** This can be set in xml with `app:postalCodeRequired`
     */
    var postalCodeRequired
        get() = getInputWidget()?.postalCodeRequired ?: false
        set(newValue) {
            if (newValue == postalCodeRequired)
                return

            getInputWidget()?.postalCodeRequired = newValue
            cardState.postalCodeRequired = newValue
        }

    /**
     * If both [postalCodeEnabled] and [usZipCodeRequired] are `true`, then postal code
     * is a required field and must be a 5-digit US zip code. If [postalCodeEnabled] is `false`,
     * this value is ignored
     *
     * **Important:** This can be set in xml with `app:usZipCodeRequired`
     */
    var usZipCodeRequired
        get() = getInputWidget()?.usZipCodeRequired ?: false
        set(newValue) {
            if (newValue != usZipCodeRequired)
                getInputWidget()?.usZipCodeRequired = newValue
        }

    /**
     * Set this to receive callbacks about card input events for this control
     */
    var cardInputListener: CardInputListener? = null

    /**
     * Whether or not to display built-in error messages
     *
     * **Important:** This can be set in xml with `app:displayErrors`
     */
    var displayErrors
        get() = _displayErrors
        set(newValue) {
            _displayErrors = newValue
            updateError()

            if (!displayErrors)
                getErrorText()?.visibility = View.GONE
        }

    /**
     * Check if there is an error message to be displayed.
     *
     * @param ignoreUneditedFieldErrors If true (default value), only fields that have been edited
     *          by the user will be considered. "Edited" means the user has entered text and focus has
     *          changed to another field (note: empty fields are considered edited if text was entered
     *          and then later deleted)
     * @return `true` if there is an error message that can be displayed to the user
     *
     * <hr class="spacer">
     *
     * To provide custom error message behavior, set [displayErrors] to false and use [fieldStates] to implement
     * custom error logic
     */
    fun hasErrorMessage(ignoreUneditedFieldErrors: Boolean = true) : Boolean {
        return cardState.hasErrorMessage(ignoreUneditedFieldErrors)
    }

    /**
     * Get the error message that would be displayed if [displayErrors] is `true` and [isValid] is false.
     * Note that [isValid] having a value of `false` does not necessarily mean there will be an error
     * message (see [ignoreUneditedFieldErrors] param)
     *
     * @param ignoreUneditedFieldErrors If true (default value), only fields that have been edited
     *          by the user will be considered. "Edited" means the user has entered text and focus has
     *          changed to another field (note: empty fields are considered edited if text was entered
     *          and then later deleted)
     * @return An error message that can be displayed to the user (e.g. in a custom dialog)
     *
     * <hr class="spacer">
     *
     * Error message text can be customized by defining the following string resources in your app:
     * ```
     * olopay_invalid_card_number_error
     * olopay_empty_card_number_error
     * olopay_invalid_expiration_error
     * olopay_empty_expiration_error
     * olopay_invalid_cvc_error
     * olopay_empty_cvc_error
     * olopay_empty_postal_code_error
     * olopay_invalid_postal_code_error
     * olopay_invalid_card_details_error
     * ```
     */
    fun getErrorMessage(ignoreUneditedFieldErrors: Boolean): String {
        return cardState.getErrorMessage(context, ignoreUneditedFieldErrors)
    }

    /**
     * Sets the card number. Does not change field focus
     * @param number Card number to be set
     */
    fun setCardNumber(number: String?) = getInputWidget()?.setCardNumber(number)

    /**
     * Sets the hint for the card number
     * @param hint The hint to be displayed when no input is entered
     */
    fun setCardHint(hint: String) = getInputWidget()?.setCardHint(hint)

    /**
     * Set the expiration date. This invokes the completion listener and changes focus to the CVC field
     * if a valid date is entered
     * <hr class="spacer">
     *
     * #### Important:
     *
     * _While a four-digit and two-digit year will both work, information beyond the tens digit of
     * a year will be truncated. Logic elsewhere in the SDK makes assumptions about what century is implied
     * by various two-digit years, and will override any information provided here._
     *
     * @param month A month of the year, represented as a number between 1 and 12
     * @param year A year number, either in two-digit or four-digit form
     */
    fun setExpirationDate(@IntRange(from = 1, to = 12) month: Int, @IntRange(from = 0, to = 9999) year: Int) {
        getInputWidget()?.setExpiryDate(month, year)
    }

    /**
     * Set the CVC value for the card. The maximum length is assumed to be 3, unless the
     * brand of the card has already been set (by setting the card number)
     *
     * @param cvc The CVC value to be set
     */
    fun setCvc(cvc: String?) = getInputWidget()?.setCvcCode(cvc)

    /**
     * Set the CVC field label
     * @param label The label to be used for the field. If `null` default values will be used
     */
    fun setCvcLabel(label: String?) = getInputWidget()?.setCvcLabel(label)

    /** Clears all text fields in the control */
    fun clearFields() {
        getInputWidget()?.clear()
        cardState.reset()
    }

    /**
     * Get an [IPaymentMethodParams] instance that can be used to create an [IPaymentMethod] instance.
     * If the form is not valid this will return null.
     * <hr class="spacer">
     *
     * #### Important:
     *
     * _Accessing this property has side-effects if not valid. It will cause form fields to validate themselves
     * and the cursor will move to the first invalid field._
     *
     * @see OloPayAPI.createPaymentMethod Use [OloPayAPI.createPaymentMethod] to create an instance of [IPaymentMethod]
     */
    val paymentMethodParams: IPaymentMethodParams?
        get() {
            val params = getInputWidget()?.paymentMethodCreateParams
            updateError(false)

            return if (params != null) PaymentMethodParams(params) else null
        }

    /** @suppress */
    override fun onInputChanged(isValid: Boolean, invalidFields: Set<StripeCardField>) {
        val fields = invalidFields.map { CardField.from(it) }.toSet()
        cardState.onInputChanged(isValid, fields)

        updateError()
        cardInputListener?.onInputChanged(isValid, fieldStates)
    }

    private fun updateError(ignoreUneditedFieldErrors: Boolean = true) {
        if (!displayErrors)
            return

        getErrorText()?.text = getErrorMessage(ignoreUneditedFieldErrors)
        getErrorText()?.visibility = if (hasErrorMessage(ignoreUneditedFieldErrors)) View.VISIBLE else View.GONE
    }

    /** @suppress */
    override fun onCardComplete() {
        cardInputListener?.onFieldComplete(CardField.CardNumber)
    }

    /** @suppress */
    override fun onCvcComplete() {
        cardInputListener?.onFieldComplete(CardField.Cvc)
    }

    /** @suppress */
    override fun onExpirationComplete() {
        cardInputListener?.onFieldComplete(CardField.Expiration)
    }

    /** @suppress */
    override fun onPostalCodeComplete() {
        cardInputListener?.onFieldComplete(CardField.PostalCode)
    }

    /** @suppress */
    override fun onFocusChange(focusField: StripeFocusField) { val field = CardField.from(focusField)
        cardState.onFocusChanged(field)
        updateError()
        cardInputListener?.onFocusChange(field)
    }

    companion object {
        const val defaultPostalCodeEnabled = true
        const val defaultPostalCodeRequired = true
        const val defaultUSZipCodeRequired = false
        const val defaultDisplayErrors = true
    }
}
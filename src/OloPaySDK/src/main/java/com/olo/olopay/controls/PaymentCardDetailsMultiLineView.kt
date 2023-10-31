// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.R
import com.olo.olopay.controls.callbacks.CardInputListener
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.CardField
import com.olo.olopay.data.ICardFieldState
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.internal.callbacks.PostalCodeValidListener
import com.olo.olopay.internal.callbacks.ValidStateChangedListener
import com.olo.olopay.internal.data.CardState
import com.olo.olopay.internal.data.GlobalConstants
import com.olo.olopay.internal.data.OloTextWatcher
import com.olo.olopay.internal.data.PaymentMethodParams
import com.olo.olopay.internal.data.PaymentMethodSource
import com.olo.olopay.internal.extensions.dismissKeyboard
import com.olo.olopay.internal.extensions.isDescendantOf
import com.olo.olopay.internal.extensions.parseColorOrNull
import com.olo.olopay.internal.extensions.requestFocus
import com.olo.olopay.internal.extensions.setBackgroundStyle
import com.stripe.android.databinding.StripeCardMultilineWidgetBinding
import com.stripe.android.view.CardMultilineWidget
import com.stripe.android.view.StripeEditText
import com.stripe.android.view.CardValidCallback as StripeCardValidCallback
import com.stripe.android.view.CardValidCallback.Fields as StripeCardField
import com.stripe.android.view.CardInputListener.FocusField as StripeFocusField
import com.stripe.android.view.CardInputListener as StripeCardInputListener

/**
 * Convenience view for collecting card details from a user. Card fields are separated into multiple
 * input fields
 *
 * #### Important:
 * User-entered ard details are intentionally restricted for PCI compliance
 *
 * <hr class="divider">
 *
 * ## Styling/Branding
 *
 * This class can be styled in the following ways:
 * 1. Override general-purpose resource values
 * 2. Override resource values specific to [PaymentCardDetailsMultiLineView]
 * 3. Override styles specific to [PaymentCardDetailsMultiLineView]
 * 4. Set styles programmatically
 *
 * ### Override general-purpose resource values
 *
 * Overriding general-purpose resource values is the easiest way to control the look/feel of the views
 * in the Olo Pay SDK. Changing these values (listed below) will apply to not just [PaymentCardDetailsMultiLineView],
 * but also [PaymentCardDetailsSingleLineView], and in a limited way, [PaymentCardDetailsForm] and [PaymentCardCvvView]. To
 * override the resources, simply define resources in your app with the same names listed below
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
 *
 * ### Override resource values specific to [PaymentCardDetailsMultiLineView]
 *
 * Overriding the resource values listed below provides for more flexibility, but they apply ONLY to
 * [PaymentCardDetailsMultiLineView]. The default values for these resources are defined in terms of
 * the general-purpose resources listed in the previous section. Overriding these values take precedence
 * over values defined by the general-purpose resources.
 *
 * **Dimen Resources**
 * ```
 * olopay_paymentcarddetailsmultilineview_edittext_floatinglabeltextsize
 * olopay_paymentcarddetailsmultilineview_errortext_textsize
 * ```
 *
 * **Color Resources**
 * ```
 * olopay_paymentcarddetailsmultilineview_errortext_textcolor
 * olopay_paymentcarddetailsmultilineview_edittext_errorcolor
 * olopay_paymentcarddetailsmultilineview_edittext_hintcolor
 * olopay_paymentcarddetailsmultilineview_edittext_floatinghintcolor
 *
 * ```
 *
 * ### Override styles specific to [PaymentCardDetailsMultiLineView]
 *
 * This is the most difficult option for customization but also allows for the most flexibility. One
 * potential use case for overriding styles would be to change the font (though there are other
 * means of doing that via application-wide themes, etc)
 *
 * When overriding styles, note the following:
 * - The style names **MUST** match what is listed below. You can choose a different parent but it should be a similar type of parent
 * - Some resource values are not defined by the styles that can be overridden so when taking this approach you may need to override resource values as well
 * - The styles listed below use some of the properties from above (see code below for which ones)
 * - Overridden styles take precedence... if you override a style that is using one of the resource values from above, you will need to define it in your style
 * - Because these are custom controls with custom behavior, not all standard Android style attributes will have an effect on the control
 *
 * The styles that can be defined that will be applied to this view are listed here:
 * ```
 * <style name="OloPay.PaymentCardDetailsMultiLineView.ErrorText.TextAppearance" parent="TextAppearance.AppCompat">
 *     <item name="android:textColor">@color/olopay_paymentcarddetailsmultilineview_edittext_errorcolor</item>
 *     <item name="android:textSize">@dimen/olopay_paymentcarddetailsmultilineview_errortext_textsize</item>
 *     <!-- Provide other customizations here -->
 * </style>
 *
 * <style name="OloPay.PaymentCardDetailsMultiLineView.TextInputLayout.HintTextAppearance" parent="TextAppearance.AppCompat">
 *     <item name="android:textColor">@color/olopay_paymentcarddetailsmultilineview_edittext_floatinghintcolor</item>
 *     <item name="android:textSize">@dimen/olopay_paymentcarddetailsmultilineview_edittext_floatinglabeltextsize</item>
 *     <!-- Provide other customizations here -->
 * </style>
 * ```
 * ### Set styles programmatically
 * A number of methods exist that allow you to style [PaymentCardDetailsMultiLineView]. Methods exist
 * for changing the background style (color, border, radius, etc), the text colors, error text colors,
 * hint text colors, font, and font size.
 *
 * @constructor Creates a new instance of [PaymentCardDetailsMultiLineView]
 */
class PaymentCardDetailsMultiLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr), StripeCardValidCallback, StripeCardInputListener {
    private lateinit var _inputWidget: CardMultilineWidget
    private lateinit var _inputWidgetBinding: StripeCardMultilineWidgetBinding
    private lateinit var _allInputFields: Set<StripeEditText>
    private lateinit var _allInputLayouts: Set<TextInputLayout>
    private lateinit var _focusedHintColor: ColorStateList
    private lateinit var _hintColor: ColorStateList
    private lateinit var _errorColor: ColorStateList
    private val _errorText: TextView
    private var _displayErrors = defaultDisplayErrors
    private var cardState = CardState()
    private var _clearFieldsInProgress = false

    init {
        inflate(context, R.layout.paymentcarddetailsmultilineview, this)

        initializeInputWidget()
        initializeInputWidgetBinding()
        _errorText = findViewById(R.id.error_text)
        setHintText(CardField.Cvv, context.getString(R.string.olopay_cvv_text_hint))

        viewTreeObserver.addOnGlobalFocusChangeListener { oldFocus, newFocus ->
            val oldFocusInput = oldFocus as? StripeEditText
            val newFocusInput = newFocus as? StripeEditText
            if (newFocusInput == null && oldFocusInput != null && oldFocusInput.isDescendantOf(this)) {
                onFocusChange(null)
            }
        }

        cardState.postalCodeValidListener = PostalCodeValidListener {
            cardInputListener?.onFieldComplete(CardField.PostalCode)
        }

        cardState.validStateChangedListener = ValidStateChangedListener { isValid ->
            cardInputListener?.onValidStateChanged(isValid, fieldStates)
        }

        val hintColor = resources.getColor(R.color.olopay_paymentcarddetailsmultilineview_edittext_hintcolor, context.theme)
        setHintTextColor(hintColor)

        setErrorTextColor(_inputWidgetBinding.etCardNumber.defaultErrorColorInt)

        loadXmlStyles(context, attrs)

        val cardField = getTextField(CardField.CardNumber)
        cardField.doAfterTextChanged { updateCardNumberDrawableHintColor() }
        cardField.setOnFocusChangeListener { _, _ -> updateCardNumberDrawableHintColor() }
    }

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
     * The detected card brand based on the currently entered card number
     */
    val cardBrand
        get() = CardBrand.convertFrom(_inputWidget.brand)

    /**
     * Get an [IPaymentMethodParams] instance that can be used to create an [IPaymentMethod] instance.
     * If the form is not valid this will return null.
     * <hr class="spacer">
     *
     * @see OloPayAPI.createPaymentMethod Use [OloPayAPI.createPaymentMethod] to create an instance of [IPaymentMethod]
     */
    val paymentMethodParams: IPaymentMethodParams?
        get() {
            val isValid = cardState.isValid
            updateError(false)

            if (isValid) {
                val params = _inputWidget.paymentMethodCreateParams

                // params should NEVER be null... but we need to handle it just to be safe
                return if (params != null) PaymentMethodParams(params, PaymentMethodSource.MultiLineInput) else null
            }

            return null
        }

    /**
     * Whether or not the postal code is enabled. It is enabled by default.
     * Disabling the postal code field is discouraged because it could
     * impact authorization success rates.
     *
     * A postal code is needed in order to submit an order to Olo's Ordering API. If this
     * field is disabled, you will need to provide another mechanism in your app to collect
     * a postal code before attempting to submit an order to Olo's Ordering API.
     *
     * #### Important:
     * This can be set in xml with `app:postalCodeEnabled`
     * */
    var postalCodeEnabled
        get() = cardState.postalCodeEnabled
        set(newValue) {
            if (newValue == postalCodeEnabled)
                return

            _inputWidget.setShouldShowPostalCode(newValue)
            cardState.setPostalCodeEnabled(newValue)
        }

    /**
     * Set this to receive callbacks about card input events for this control
     */
    var cardInputListener: CardInputListener? = null

    /**
     * Whether or not to display built-in error messages
     *
     * #### Important:
     * This can be set in xml with `app:displayErrors`
     */
    var displayErrors
        get() = _displayErrors
        set(newValue) {
            _displayErrors = newValue
            updateError()

            if (!displayErrors)
                _errorText.visibility = View.GONE
        }

    /**
     * Enables or disables the control
     * @param enabled Whether or not the control should be enabled/disabled
     */
    override fun setEnabled(enabled: Boolean) { _inputWidget.isEnabled = enabled }

    /**
     * True if the form is enabled and can accept user input, false otherwise
     */
    override fun isEnabled() = _inputWidget.isEnabled

    /** @suppress */
    override fun onInputChanged(isValid: Boolean, invalidFields: Set<StripeCardField>) {
        if (_clearFieldsInProgress) {
            return
        }

        val fields = invalidFields.map { CardField.from(it) }.toSet()
        cardState.onInputChanged(cardBrand, fields)
    }

    /** @suppress */
    override fun onCardComplete() {
        cardInputListener?.onFieldComplete(CardField.CardNumber)
    }

    /** @suppress */
    override fun onCvcComplete() {
        cardInputListener?.onFieldComplete(CardField.Cvv)
    }

    /** @suppress */
    override fun onExpirationComplete() {
        cardInputListener?.onFieldComplete(CardField.Expiration)
    }

    /** @suppress */
    override fun onPostalCodeComplete() {
        // Do nothing here... we provide our own version of
        // this since we have our own postal code validation logic
    }

    /** @suppress */
    override fun onFocusChange(focusField: StripeFocusField) {
        onFocusChange(CardField.from(focusField))
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
     * olopay_invalid_cvv_error
     * olopay_empty_cvv_error
     * olopay_empty_postal_code_error
     * olopay_invalid_postal_code_error
     * olopay_invalid_card_details_error
     * olopay_unsupported_card_type_error
     * ```
     */
    fun getErrorMessage(ignoreUneditedFieldErrors: Boolean): String {
        return cardState.getErrorMessage(context, ignoreUneditedFieldErrors)
    }

    /**
     * Sets the card number. Does not change field focus
     * @param number Card number to be set
     */
    fun setCardNumber(number: String?) = _inputWidget.setCardNumber(number)

    /**
     * Deprecated - Use [setHintText] instead.
     */
    @Deprecated("setCardHint() is deprecated", ReplaceWith("setHintText"), DeprecationLevel.WARNING)
    fun setCardHint(hint: String) = setHintText(CardField.CardNumber, hint)

    /**
     * Set the expiration date. This invokes the completion listener and changes focus to the CVV field
     * if a valid date is entered
     * <hr class="spacer">
     *
     * #### Important:
     * While a four-digit and two-digit year will both work, information beyond the tens digit of
     * a year will be truncated. Logic elsewhere in the SDK makes assumptions about what century is implied
     * by various two-digit years, and will override any information provided here.
     *
     * @param month A month of the year, represented as a number between 1 and 12
     * @param year A year number, either in two-digit or four-digit form
     */
    fun setExpirationDate(@IntRange(from = 1, to = 12) month: Int, @IntRange(from = 0, to = 9999) year: Int) {
        _inputWidget.setExpiryDate(month, year)
    }

    /**
     * Deprecated - Use [setCvv] instead.
     */
    @Deprecated("setCvc() is deprecated", ReplaceWith("setCvv()"), DeprecationLevel.WARNING)
    fun setCvc(cvc: String?) = setCvv(cvc)

    /**
     * Set the CVV value for the card. The maximum length is assumed to be 3, unless the
     * brand of the card has already been set (by setting the card number)
     *
     * @param cvv The CVV value to be set
     */
    fun setCvv(cvv: String?) = _inputWidget.setCvcCode(cvv)

    /**
     * Deprecated - Use [setHintText] instead.
     */
    @Deprecated("setCvcLabel() is deprecated", ReplaceWith("setHintText"), DeprecationLevel.WARNING)
    fun setCvcLabel(label: String?) = setHintText(CardField.Cvv, label ?: "")

    /**
     * Sets the hint text for the specified field
     *
     * @param field The field to set hint text for
     * @param hint  The hint text to be set
     *
     * #### Important:
     * This can also be set in xml with the following attributes
     * - `app:cardNumberHint`
     * - `app:expirationHint`
     * - `app:cvvHint`
     * - `app:postalCodeHint`
     */
    fun setHintText(field: CardField, hint: String) {
        when(field) {
            CardField.CardNumber -> _inputWidget.setCardHint(hint)
            CardField.Cvv -> _inputWidget.setCvcLabel(hint)
            else -> getTextField(field).hint = hint
        }

        getTextInputLayout(field).hint = hint
    }

    /** Clears all text fields in the control */
    fun clearFields() {
        _clearFieldsInProgress = true

        val focusedField = cardState.focusedField

        //Call this prior to clearing text fields
        cardState.reset()

        // Clear the fields directly instead of calling Stripe's clear() because it's very presumptuous...
        // It sets focus on the card number field and shows the keyboard. This way is more precise
        // in what it does and gives developers more control.
        for(inputField in _allInputFields) {
            inputField.setText("")
        }

        _clearFieldsInProgress = false

        if (focusedField != CardField.CardNumber) {
            requestFocus(CardField.CardNumber, false)
        } else {
            onFocusChange(CardField.CardNumber)
        }

        cardInputListener?.onInputChanged(isValid, fieldStates)
    }

    /**
     * Moves focus to the specified input field
     * @param field: The field to move focus to
     * @param showKeyboard: Whether or not to show the keyboard when the focus changes
     */
    fun requestFocus(field: CardField, showKeyboard: Boolean) {
        getTextField(field).requestFocus(showKeyboard)
    }

    /**
     * Dismisses the keyboard, if visible, and removes focus from input fields in this control
     */
    fun dismissKeyboard() {
        _inputWidget.clearFocus()
        _inputWidgetBinding.root.requestFocus(false)
        _inputWidgetBinding.root.dismissKeyboard()
    }

    /**
     * Sets background styles for this view.
     * #### Important:
     * This method requires API level 27 or higher
     *
     * @param backgroundColorHex The background color (in hex format) for the view, or null which defaults to `transparent`
     * @param borderColorHex The color for the background border (in hex format) of this view, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border in pixels, or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null which defaults to `0` and removes the border radius
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setCardBackgroundStyle(backgroundColorHex: String? = null, borderColorHex: String? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        setCardBackgroundStyle(
            parseColorOrNull(backgroundColorHex),
            parseColorOrNull(borderColorHex),
            borderWidthPx,
            borderRadiusPx)
    }

    /**
     * Sets background styles for this view.
     *
     * @param backgroundColor The background color for the view, or null which defaults to `transparent`
     * @param borderColor The color for the background border of this view, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border in pixels, or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null which defaults to `0` and removes the border radius
     */
    fun setCardBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        _inputWidget.setBackgroundStyle(backgroundColor, borderColor,borderWidthPx, borderRadiusPx)
    }

    /**
     * Sets the padding for the area immediately surrounding the card input fields.
     *
     * @param startPx  Padding (in pixels) for the left of the card input area
     * @param topPx Padding (in pixels) for the top of the card input area
     * @param endPx Padding (in pixels) for the right of the card input area
     * @param bottomPx Padding (in pixels) for the bottom of the card input area
     *                 (the area between the bottom of the input fields and the error message)
     *
     * **_NOTE:_** If you want to set the padding around the entire control (including around the error
     *             message) use View.setPadding()
     */
    fun setCardPadding(startPx: Int?, topPx: Int?, endPx: Int?, bottomPx: Int?) {
        val start = startPx ?: _inputWidget.paddingLeft
        val top = topPx ?: _inputWidget.paddingTop
        val end = endPx ?: _inputWidget.paddingRight
        val bottom = bottomPx ?: _inputWidget.paddingBottom

        _inputWidget.setPadding(start, top, end, bottom)
    }

    /**
     * Sets the padding for the error message displayed below the card input fields.
     *
     * @param startPx  Padding (in pixels) for the left of the error message
     * @param topPx Padding (in pixels) for the top of the error message
     * @param endPx Padding (in pixels) for the right of the error message
     * @param bottomPx Padding (in pixels) for the bottom of the error message
     *
     * **_NOTE:_** If you want to set the padding around the entire control (including around the error
     *             message) use View.setPadding()
     */
    fun setErrorPadding(startPx: Int?, topPx: Int?, endPx: Int?, bottomPx: Int?) {
        val start = startPx ?: _errorText.paddingLeft
        val top = topPx ?: _errorText.paddingTop
        val end = endPx ?: _errorText.paddingRight
        val bottom = bottomPx ?: _errorText.paddingBottom

        _errorText.setPadding(start, top, end, bottom)
    }

    /**
     * Sets the text color for all input fields
     * #### Important:
     * This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setTextColor(color)
        }
    }

    /**
     * Sets the text color for all input fields
     * @param color The color to be set
     */
    fun setTextColor(color: Int) {
        for (inputField in _allInputFields) {
            inputField.setTextColor(color)
        }
    }

    /**
     * Sets the error text color for all input fields and error messages
     * #### Important:
     * This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setErrorTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setErrorTextColor(color)
        }
    }

    /**
     * Sets the error text color for all input fields and error messages
     * @param color The color to be set
     */
    fun setErrorTextColor(color: Int) {
        for (inputField in _allInputFields) {
            inputField.setErrorColor(color)
        }

        _errorColor = ColorStateList.valueOf(color)
        _errorText.setTextColor(color)
        updateCardNumberDrawableHintColor()
    }

    /**
     * Sets the color for the cursor, selection handles, and text selection highlight
     * #### Important:
     * This method requires API Level 29 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiQuinceTart)
    fun setCursorColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setCursorColor(color)
        }
    }

    /**
     * Sets the color for the cursor, selection handles, and text selection highlight
     * #### Important:
     * This method requires API Level 29 or higher
     * @param color The color to be set
     */
    @RequiresApi(GlobalConstants.ApiQuinceTart)
    fun setCursorColor(color: Int) {
        for (inputField in _allInputFields) {
            inputField.textCursorDrawable?.setTint(color)
            inputField.textSelectHandle?.setTint(color)
            inputField.textSelectHandleLeft?.setTint(color)
            inputField.textSelectHandleRight?.setTint(color)
            inputField.highlightColor = color
        }
    }

    /**
     * Sets the hint text color for all input fields
     * #### Important:
     * This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
    */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setHintTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setHintTextColor(color)
        }
    }

    /**
     * Sets the hint text color for all input fields
     * @param color The color to be set
     */
    fun setHintTextColor(color: Int) {
        _hintColor = ColorStateList.valueOf(color)
        _focusedHintColor = _hintColor

        for (inputLayout in _allInputLayouts) {
            inputLayout.defaultHintTextColor = _hintColor
            inputLayout.placeholderTextColor = _hintColor
        }

        updateCardNumberDrawableHintColor()
    }

    /**
     * Sets the hint text color for when a field has focus. Note that [setHintTextColor] overrides
     * this value, so this method must be called after calling [setHintTextColor].
     * #### Important:
     * This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setFocusedHintTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setFocusedHintTextColor(color)
        }
    }

    /**
     * Sets the hint text color for when a field has focus. Note that [setHintTextColor] overrides
     * this value, so this method must be called after calling [setHintTextColor].
     * @param color The color to be set
     */
    fun setFocusedHintTextColor(color: Int) {
        _focusedHintColor = ColorStateList.valueOf(color)
        for (inputLayout in _allInputLayouts) {
            inputLayout.hintTextColor = _focusedHintColor
        }

        updateCardNumberDrawableHintColor()
    }

    /**
     * Sets the text size for all input fields
     * @param size The size to be set
     */
    fun setTextSize(size: Float) {
        for (inputField in _allInputFields) {
            inputField.textSize = size
        }
    }

    /**
     * Sets the text size for error messages
     * @param size The size to be set
     */
    fun setErrorTextSize(size: Float) {
        _errorText.textSize = size
    }

    /**
     * Sets the font for all input fields
     * @param font The font to be set
     */
    fun setFont(font: Typeface) {
        for (inputLayout in _allInputLayouts) {
            inputLayout.editText?.typeface = font
            inputLayout.typeface = font
        }
    }

    /**
     * Sets the error font for error messages
     * @param font The font to be set
     */
    fun setErrorFont(font: Typeface) {
        _errorText.typeface = font
    }

    /**
     * By default, the edit text fields will use the styling defined by the app theme.
     * However, the colors for the edit text field's underline can be customized
     * programmatically by calling this method.
     * #### Important:
     * This method requires API level 27 or higher
     * @param defaultColorHex The color (in Hex format) for the underline when fields do not have focus
     * @param focusColorHex The color (in Hex format) for the underline when fields have focus
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setFieldUnderlineColors(defaultColorHex: String, focusColorHex: String) {
        parseColorOrNull(defaultColorHex)?.let { defaultColor ->
            parseColorOrNull(focusColorHex)?.let { focusColor ->
                setFieldUnderlineColors(defaultColor, focusColor)
            }
        }
    }

    /**
     * By default, the edit text fields will use the styling defined by the app theme.
     * However, the colors for the edit text field's underline can be customized
     * programmatically by calling this method.
     *
     * @param defaultColor The color for the underline when fields do not have focus
     * @param focusColor The color for the underline when fields have focus
     */
    fun setFieldUnderlineColors(defaultColor: Int, focusColor: Int) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(),
        )

        val colors = intArrayOf(
            focusColor,
            defaultColor
        )

        val underlineColor = ColorStateList(states, colors)

        // This whole setup is due to a bug in the Android source code for TextInputEditText instances
        // inside a TextInputLayout. This solution is adapted from this workaround:
        // https://github.com/material-components/material-components-android/issues/201#issuecomment-425418909
        for (inputField in _allInputFields) {
            // Set the default background color without any states so the edit text shows up with
            // the correct background color when it is initially displayed
            inputField.backgroundTintList = ColorStateList.valueOf(defaultColor)

            // Set the color state list when the view is given focus so that the background color will
            // change based on the states. Technically this only needs to be set once, but since there
            // isn't a way to remove this listener, adding code to only set the background once is just
            // extra effort without any noticeable ROI
            inputField.setOnFocusChangeListener { view, _ -> view.backgroundTintList = underlineColor }
        }
    }

    private fun onFocusChange(field: CardField?) {
        cardState.onFocusChanged(field)
        updateError()
        cardInputListener?.onFocusChange(field, fieldStates)
    }

    private fun afterTextChanged(field: CardField, newText: String) {
        if (_clearFieldsInProgress) {
            return
        }

        cardState.onFieldTextChanged(field, newText)
        updateError()

        // We have to call this here rather than in onInputChanged because of custom logic in CardState
        // and because onInputChanged gets called before the individual fields' afterTextChanged
        // handlers
        cardInputListener?.onInputChanged(isValid, fieldStates)
    }

    private fun updateError(ignoreUneditedFieldErrors: Boolean = true) {
        if (!displayErrors)
            return

        _errorText.text = getErrorMessage(ignoreUneditedFieldErrors)
        _errorText.visibility = if (hasErrorMessage(ignoreUneditedFieldErrors)) View.VISIBLE else View.GONE
    }

    private fun initializeInputWidget() {
        _inputWidget = findViewById(R.id.olopaysdk_multi_line_widget)

        // We have our own postal code logic...
        // Setting these values to false turns off Stripe's validation logic
        _inputWidget.postalCodeRequired = false
        _inputWidget.usZipCodeRequired = false

        _inputWidget.setCardInputListener(this)
        _inputWidget.setCardValidCallback(this)

        _inputWidget.setCardNumberTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged(CardField.CardNumber, editable?.toString() ?: "")
            }
        })

        _inputWidget.setExpiryDateTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged(CardField.Expiration, editable?.toString() ?: "")
            }
        })

        _inputWidget.setCvcNumberTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged(CardField.Cvv, editable?.toString() ?: "")
            }
        })

        _inputWidget.setPostalCodeTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged(CardField.PostalCode, editable?.toString() ?: "")
            }
        })

        // This removes the error handlers from the inputWidget, so we can provide custom error message handling
        _inputWidget.setCardNumberErrorListener{}
        _inputWidget.setExpirationDateErrorListener{}
        _inputWidget.setCvcErrorListener{}
        _inputWidget.setPostalCodeErrorListener{}
    }

    private fun initializeInputWidgetBinding() {
        _inputWidgetBinding = StripeCardMultilineWidgetBinding.bind(_inputWidget)

        _inputWidgetBinding.root.isFocusable = true
        _inputWidgetBinding.root.isFocusableInTouchMode = true
        _inputWidgetBinding.root.requestFocus(false)

        _allInputFields = setOf(
            _inputWidgetBinding.etCardNumber,
            _inputWidgetBinding.etExpiry,
            _inputWidgetBinding.etCvc,
            _inputWidgetBinding.etPostalCode
        )

        _allInputLayouts = setOf(
            _inputWidgetBinding.tlCardNumber,
            _inputWidgetBinding.tlExpiry,
            _inputWidgetBinding.tlCvc,
            _inputWidgetBinding.tlPostalCode
        )
    }

    private fun getTextField(field: CardField): StripeEditText {
        return when(field) {
            CardField.CardNumber -> _inputWidgetBinding.etCardNumber
            CardField.Expiration -> _inputWidgetBinding.etExpiry
            CardField.Cvv -> _inputWidgetBinding.etCvc
            CardField.PostalCode -> _inputWidgetBinding.etPostalCode
        }
    }

    private fun getTextInputLayout(field: CardField): TextInputLayout {
        return when(field) {
            CardField.CardNumber -> _inputWidgetBinding.tlCardNumber
            CardField.Expiration -> _inputWidgetBinding.tlExpiry
            CardField.Cvv -> _inputWidgetBinding.tlCvc
            CardField.PostalCode -> _inputWidgetBinding.tlPostalCode
        }
    }

    private fun updateCardNumberDrawableHintColor() {
        val field = _inputWidgetBinding.etCardNumber
        val fieldText = field.text

        val newColorStateList: ColorStateList? =
            if (field.shouldShowError && cardBrand == CardBrand.Unknown) {
                // Handle Error State
                // NOTE: When the brand image is displayed the tint makes the brand image become a solid color
                _errorColor
            } else if (field.hasFocus()) {
                // Handle Focused State
                if (fieldText == null || fieldText.isEmpty()) {
                    _focusedHintColor
                } else if (cardBrand == CardBrand.Unknown) {
                    field.textColors
                } else {
                    null
                }
            } else {
                // Handle Unfocused State
                if (fieldText == null || fieldText.isEmpty()) {
                    _hintColor
                } else if (cardBrand == CardBrand.Unknown) {
                    field.textColors
                } else {
                    null
                }
            }

        TextViewCompat.setCompoundDrawableTintList(field, newColorStateList)
    }

    private fun loadXmlStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardDetailsMultiLineView) {
        //NOTE: These temp variables are required. For some reason the values don't get set
        //      correctly if we set the class properties directly in one statement
        val postalCodeEnabledAttrValue = getBoolean(
            R.styleable.PaymentCardDetailsMultiLineView_postalCodeEnabled,
            defaultPostalCodeEnabled
        )
        postalCodeEnabled = postalCodeEnabledAttrValue

        val displayErrorsAttrValue = getBoolean(
            R.styleable.PaymentCardDetailsMultiLineView_displayErrors,
            defaultDisplayErrors
        )
        displayErrors = displayErrorsAttrValue

        getString(R.styleable.PaymentCardDetailsMultiLineView_cardNumberHint)?.let { hint ->
            setHintText(CardField.CardNumber, hint)
        }

        getString(R.styleable.PaymentCardDetailsMultiLineView_expirationHint)?.let { hint ->
            setHintText(CardField.Expiration, hint)
        }

        getString(R.styleable.PaymentCardDetailsMultiLineView_cvvHint)?.let { hint ->
            setHintText(CardField.Cvv, hint)
        }

        getString(R.styleable.PaymentCardDetailsMultiLineView_postalCodeHint)?.let { hint ->
            setHintText(CardField.PostalCode, hint)
        }
    }

    /**
     * @suppress
     */
    companion object {
        /**
         * @suppress
         */
        const val defaultPostalCodeEnabled = true
        /**
         * @suppress
         */
        const val defaultDisplayErrors = true
    }
}
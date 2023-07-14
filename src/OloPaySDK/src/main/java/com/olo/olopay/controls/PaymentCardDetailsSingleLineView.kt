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
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.R
import com.olo.olopay.controls.callbacks.CardInputListener
import com.olo.olopay.data.*
import com.olo.olopay.internal.callbacks.PostalCodeValidListener
import com.olo.olopay.internal.data.CardState
import com.olo.olopay.internal.data.GlobalConstants
import com.olo.olopay.internal.data.OloTextWatcher
import com.olo.olopay.internal.data.PaymentMethodParams
import com.olo.olopay.internal.data.PaymentMethodSource
import com.olo.olopay.internal.extensions.dismissKeyboard
import com.olo.olopay.internal.extensions.parseColorOrNull
import com.olo.olopay.internal.extensions.requestFocus
import com.stripe.android.databinding.StripeCardInputWidgetBinding
import com.stripe.android.view.CardInputWidget
import com.stripe.android.view.StripeEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * 4. Set styles programmatically
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
 * Overriding the resource values listed below provides for more flexibility, but they apply ONLY to
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
 * ### Set styles programmatically
 * A number of methods exist that allow you to style [PaymentCardDetailsSingleLineView]. Methods exist
 * for changing the background style (color, border, radius, etc), the text colors, error text colors,
 * hint text colors, font, and font size.
 *
 * @constructor Creates a new instance of [PaymentCardDetailsSingleLineView]
 */
class PaymentCardDetailsSingleLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr), StripeCardValidCallback, StripeCardInputListener {
    private lateinit var _inputWidget: CardInputWidget
    private lateinit var _inputWidgetBinding: StripeCardInputWidgetBinding
    private lateinit var _allInputFields: Set<StripeEditText>
    private val _errorText: TextView
    private var _displayErrors = defaultDisplayErrors
    private var cardState = CardState()

    init {
        inflate(context, R.layout.paymentcarddetailssinglelineview, this)

        initializeInputWidget()
        initializeInputWidgetBinding()
        _errorText = findViewById(R.id.error_text)

        loadXmlStyles(context, attrs)

        cardState.postalCodeValidListener = PostalCodeValidListener {
            cardInputListener?.onFieldComplete(CardField.PostalCode)
        }
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
                return if (params != null) PaymentMethodParams(params, PaymentMethodSource.SingleLineInput) else null
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
     * **Important:** This can be set in xml with `app:postalCodeEnabled`
     */
    var postalCodeEnabled
        get() = _inputWidget.postalCodeEnabled ?: true
        set(newValue) {
            if (newValue == postalCodeEnabled)
                return

            _inputWidget.postalCodeEnabled = newValue
            cardState.postalCodeEnabled = newValue
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
    override fun isEnabled() = _inputWidget.isEnabled ?: false

    /** @suppress */
    override fun onInputChanged(isValid: Boolean, invalidFields: Set<StripeCardField>) {
        val fields = invalidFields.map { CardField.from(it) }.toSet()
        cardState.onInputChanged(isValid, cardBrand, fields)
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
        // Do nothing here... we provide our own version of
        // this since we have our own postal code validation logic
    }

    /** @suppress */
    override fun onFocusChange(focusField: StripeFocusField) {
        val field = CardField.from(focusField)
        cardState.onFocusChanged(field)
        updateError()
        cardInputListener?.onFocusChange(field)
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
     * Sets the hint for the card number
     * @param hint The hint to be displayed when no input is entered
     */
    @Deprecated("setCardHint() is deprecated", ReplaceWith("setHintText"), DeprecationLevel.WARNING)
    fun setCardHint(hint: String) = _inputWidget.setCardHint(hint)

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
        _inputWidget.setExpiryDate(month, year)
    }

    /**
     * Set the CVC value for the card. The maximum length is assumed to be 3, unless the
     * brand of the card has already been set (by setting the card number)
     *
     * @param cvc The CVC value to be set
     */
    fun setCvc(cvc: String?) = _inputWidget.setCvcCode(cvc)

    /**
     * Set the CVC field label
     * @param label The label to be used for the field. If `null` default values will be used
     */
    @Deprecated("setCvcLabel() is deprecated", ReplaceWith("setHintText"), DeprecationLevel.WARNING)
    fun setCvcLabel(label: String?) = _inputWidget.setCvcLabel(label)

    /**
     * Sets the hint text for the specified field
     *
     * @param field The field to set hint text for
     * @param hint  The hint text to be set
     *
     * **Important:** This can also be set in xml with the following attributes
     * - `app:cardNumberHint`
     * - `app:expirationHint`
     * - `app:cvcHint`
     * - `app:postalCodeHint`
     */
    fun setHintText(field: CardField, hint: String) {
        when(field) {
            CardField.CardNumber -> _inputWidget.setCardHint(hint)
            CardField.Cvc -> _inputWidget.setCvcLabel(hint)
            else -> getTextField(field).hint = hint
        }
    }

    /** Clears all text fields in the control */
    fun clearFields() {
        // Clear the fields directly instead of calling Stripe's clear() because it's very presumptuous...
        // It sets focus on the card number field and shows the keyboard. This way is more precise
        // in what it does and gives developers more control.
        for(inputField in _allInputFields) {
            inputField.setText("")
        }
        cardState.reset()
        updateError(true)
    }

    /**
     * Moves focus to the specified input field
     * @param field: The field to move focus to
     * @param showKeyboard: Whether or not to show the keyboard when the focus changes
     */
    fun requestFocus(field: CardField, showKeyboard: Boolean) {
        val inputField = getTextField(field)
        inputField.requestFocus(showKeyboard)

        // Only fields that are currently visible can receive focus. When the card number field
        // is fully expanded the CVC and Postal Code fields are not visible, so Android puts focus
        // on the expiration date field, which collapses the card number field, and makes all card
        // fields visible. If the requested field was not visible and didn't receive focus, this
        // code provides time for all the fields to become visible and attempts the request again.
        if (!inputField.hasFocus()) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                inputField.requestFocus(showKeyboard)
            }
        }
    }

    /**
     * Dismisses the keyboard, if visible, and removes focus from input fields in this control
     */
    fun dismissKeyboard() {
        _inputWidgetBinding.container.requestFocus(false)
        _inputWidgetBinding.container.dismissKeyboard()
    }

    /**
     * Sets background styles for this view. If all parameters are null, this method has no effect.
     * IMPORTANT: This method requires API level 27 or higher
     *
     * @param backgroundColorHex The background color (in hex format) for the view, or null
     * @param borderColorHex The color for the background border (in hex format) of this view, or null
     * @param borderWidthPx The width of the background border in pixels, or null
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null
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
     * Sets background styles for this view. If all parameters are null, this method has no effect.
     * @param backgroundColor The background color for the view, or null
     * @param borderColor The color for the background border of this view, or null
     * @param borderWidthPx The width of the background border in pixels, or null
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null
     */
    fun setCardBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        if (backgroundColor == null && borderColor == null && borderWidthPx == null && borderRadiusPx == null) {
            return
        }

        val borderRadius = borderRadiusPx ?: noBorderRadius
        val borderWidth = borderWidthPx ?: unspecifiedBorderWidth

        _inputWidget.background = MaterialShapeDrawable(
            ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, borderRadius)
                .build()
        ).also { shape ->
            if (borderWidth >= minValidBorderWidth) {
                shape.strokeWidth = borderWidth
            }

            borderColor?.let { color ->
                shape.strokeColor = ColorStateList.valueOf(color)
            }

            backgroundColor?.let { color ->
                shape.fillColor = ColorStateList.valueOf(color)
            }
        }
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
     * IMPORTANT: This method requires API level 27 or higher
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
     * IMPORTANT: This method requires API level 27 or higher
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
     */
    fun setErrorTextColor(color: Int) {
        for (inputField in _allInputFields) {
            inputField.setErrorColor(color)
        }

        _errorText.setTextColor(color)
    }

    /**
     * Sets the color for the cursor, selection handles, and text selection highlight
     * IMPORTANT: This method requires API Level 29 or higher
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
     * IMPORTANT: This method requires API Level 29 or higher
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
     * IMPORTANT: This method requires API level 27 or higher
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
        for (inputField in _allInputFields) {
            inputField.setHintTextColor(color)
        }

        try {
            // Reflection isn't an ideal way to do this, but is the only way possible right now and
            // is the approach Stripe's own React Native library used to accomplish the same thing,
            // so it's probably a safe bet this isn't going to change:
            // https://github.com/stripe/stripe-react-native/blob/master/android/src/main/java/com/reactnativestripesdk/CardFieldView.kt#L167-L178
            _inputWidgetBinding.cardBrandView::class.java.getDeclaredField("tintColorInt").let { internalTint ->
                internalTint.isAccessible = true
                internalTint.set(_inputWidgetBinding.cardBrandView, color)
            }
        } catch (e: Exception) {
            // Nothing to really do here... this means Stripe changed
            // their SDK implementation and the icon will just not get tinted
            // until we update our implementation to match
        }
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
        for (inputField in _allInputFields) {
            inputField.typeface = font
        }
    }

    /**
     * Sets the error font for error messages
     * @param font The font to be set
     */
    fun setErrorFont(font: Typeface) {
        _errorText.typeface = font
    }

    private fun afterTextChanged(field: CardField, newText: String) {
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
        _inputWidget = findViewById(R.id.olopaysdk_single_line_widget)

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
                afterTextChanged(CardField.Cvc, editable?.toString() ?: "")
            }
        })

        _inputWidget.setPostalCodeTextWatcher(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged(CardField.PostalCode, editable?.toString() ?: "")
            }
        })
    }

    private fun initializeInputWidgetBinding() {
        _inputWidgetBinding = StripeCardInputWidgetBinding.bind(_inputWidget)

        // By making the container view focusable, we can clear focus of all input fields later
        // by requesting focus on the container
        _inputWidgetBinding.container.isFocusable = true
        _inputWidgetBinding.container.isFocusableInTouchMode = true
        _inputWidgetBinding.container.requestFocus(false)

        _allInputFields = setOf(
            _inputWidgetBinding.cardNumberEditText,
            _inputWidgetBinding.expiryDateEditText,
            _inputWidgetBinding.cvcEditText,
            _inputWidgetBinding.postalCodeEditText
        )
    }

    private fun getTextField(field: CardField): StripeEditText {
        return when(field) {
            CardField.CardNumber -> _inputWidgetBinding.cardNumberEditText
            CardField.Expiration -> _inputWidgetBinding.expiryDateEditText
            CardField.Cvc -> _inputWidgetBinding.cvcEditText
            CardField.PostalCode -> _inputWidgetBinding.postalCodeEditText
        }
    }

    private fun loadXmlStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardDetailsSingleLineView) {
        //NOTE: These temp variables are required. For some reason the values don't get set correctly if we set the class properties directly in one statement
        val postalCodeEnabledAttrValue = getBoolean(R.styleable.PaymentCardDetailsSingleLineView_postalCodeEnabled, defaultPostalCodeEnabled)
        postalCodeEnabled = postalCodeEnabledAttrValue

        val displayErrorsAttrValue = getBoolean(R.styleable.PaymentCardDetailsSingleLineView_displayErrors, defaultDisplayErrors)
        displayErrors = displayErrorsAttrValue

        getString(R.styleable.PaymentCardDetailsSingleLineView_cardNumberHint)?.let { hint ->
            setHintText(CardField.CardNumber, hint)
        }

        getString(R.styleable.PaymentCardDetailsSingleLineView_expirationHint)?.let { hint ->
            setHintText(CardField.Expiration, hint)
        }

        getString(R.styleable.PaymentCardDetailsSingleLineView_cvcHint)?.let { hint ->
            setHintText(CardField.Cvc, hint)
        }

        getString(R.styleable.PaymentCardDetailsSingleLineView_postalCodeHint)?.let { hint ->
            setHintText(CardField.PostalCode, hint)
        }
    }

    companion object {
        const val defaultPostalCodeEnabled = true
        const val defaultDisplayErrors = true
        const val unspecifiedBorderWidth = -1.0F
        const val minValidBorderWidth = 0.0F
        const val noBorderRadius = 0.0F
    }
}
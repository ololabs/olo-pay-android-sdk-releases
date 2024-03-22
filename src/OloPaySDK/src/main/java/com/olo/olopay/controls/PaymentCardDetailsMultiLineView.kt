// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.R
import com.olo.olopay.controls.callbacks.CardInputListener
import com.olo.olopay.controls.callbacks.ConfigurationChangeListener
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
import com.olo.olopay.internal.extensions.getColorAttributeFromResource
import com.olo.olopay.internal.extensions.getColorOrNull
import com.olo.olopay.internal.extensions.getDimensionOrNull
import com.olo.olopay.internal.extensions.getResourceOrNull
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
 * <br>
 *
 * #### Important:
 * _User-entered card details are intentionally restricted to reduce PCI compliance scope_
 *
 * <hr>
 *
 * ## Styling/Branding
 *
 * This view provides full support for styling and customization, which can be accomplished in the
 * following ways:
 * - **Individual Style Attributes:** Style specific aspects of the view individually
 * - **Text Appearance Style Attributes:** Style text using Android's [TextAppearance](https://developer.android.com/develop/ui/views/theming/themes#textappearance) styles
 * - **Background Resource Attributes:** Style the background using drawable resources
 *
 * <br>
 *
 * XML attributes can be set directly on the view or they can be defined in a style that gets applied to the view. The following
 * examples each produce the same result:
 *
 * #### Customizations applied directly to the view:
 * ```
 * // NOTE: This example assumes a namespace definition of `app`. Replace
 * // this with your own namespace qualifier if you use something different
 * <com.olo.olopay.controls.PaymentCardDetailsMultiLineView
 *   app:cardBorderColor="@color/android:black"
 *   app:cardBorderWidth="2dp" />
 * />
 * ```
 *
 * #### Customizations applied in a style:
 * ```
 * // Style definition
 * <style name="MyCardStyle">
 *   <item name="cardBorderColor">@color/android:black</item>
 *   <item name="cardBorderWidth">2dp</item>
 * </style>
 *
 * // Layout file
 * <com.olo.olopay.controls.PaymentCardDetailsMultiLineView
 *   style="@style/MyCardStyle" />
 * ```
 *
 * <hr>
 *
 * ### Individual Style Attributes
 *
 * These attributes are responsible for controlling a single aspect of the view. In the case of conflicting
 * styles between these attributes and _Text Appearance Style Attributes_ or _Background Resource Attributes_
 * these attributes take precedence.
 * <br>
 *
 * This view supports the following attributes for styling/branding purposes:
 *
 * - cardBackgroundColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBackgroundStyle])_
 * - cardBorderColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBackgroundStyle])_
 * - cardBorderWidth &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBackgroundStyle])_
 * - cardBorderRadius &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBackgroundStyle])_
 * - fieldUnderlineColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFieldUnderlineColors])_
 * - focusedFieldUnderlineColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFieldUnderlineColors])_
 * - cardStartPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - cardTopPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - cardEndPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - cardBottomPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - errorBackgroundColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBorderColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBorderWidth &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBorderRadius &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorStartPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - errorTopPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - errorEndPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - errorBottomPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - textColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setTextColor])_
 * - errorTextColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorTextColor])_
 * - hintTextColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintTextColor])_
 * - focusedHintTextColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintTextColor])_
 * - cursorColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCursorColor])_
 * - textSize &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setTextSize])_
 * - errorTextSize &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorTextSize])_
 * - cardNumberHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - expirationHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - cvvHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - postalCodeHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - focusedCardNumberHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - focusedExpirationHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - focusedCvvHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - focusedPostalCodeHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - postalCodeEnabled &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [postalCodeEnabled])_
 * - displayErrors &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [displayErrors])_
 *
 * #### Important:
 * _Some customizations are only possible when using Text Appearance Style Attributes or Background Resource Attributes.
 * Refer to those sections for more information._
 *
 * <hr>
 *
 * ### Text Appearance Attributes
 *
 * Unlike the text attributes specified in _Individual Style Attributes_ above, where each
 * attribute specifies one aspect of the text's appearance, these attributes define the entire
 * appearance through one attribute using a [text appearance](https://developer.android.com/develop/ui/views/theming/themes#textappearance)
 * style. These styles can define text color, text size, hint text color, or even have
 * different appearances defined based on the state of the view (e.g. focused or unfocused, enabled or disabled).
 * For a full list of available attributes, see [TextAppearance](https://developer.android.com/reference/android/R.styleable#TextAppearance).
 *
 * This view supports the following text appearance resource attributes for styling/branding purposes:
 *
 * - textAppearance &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setTextAppearance])_
 * - errorTextAppearance &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorTextAppearance])_
 *
 * #### Important
 * _Text attributes defined directly on this view (from the Individual Style Attributes section above)
 * will override attributes defined by the `textAppearance` and `errorTextAppearance` attributes_
 *
 * <hr>
 *
 * ### Background Attributes
 *
 * Unlike the background attributes specified in _Individual Style Attributes_ above, where each
 * attribute specifies one aspect of the background, these attributes define the entire background through one resource id.
 * For example, a single [drawable resource](https://developer.android.com/guide/topics/resources/drawable-resource)
 * could be used that specifies padding, border color, border radius, and a gradient background, or
 * even have different backgrounds defined based on the state of the view (e.g. focused or unfocused, enabled or disabled).
 *
 * This view supports the following background resource attributes for styling/branding purposes:
 *
 * - cardBackground &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBackgroundResource])_
 * - errorBackground &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundResource])_
 *
 * #### Important
 * _Background attributes from the Individual Style Attributes section above take precedence over these background attributes._
 *
 * <hr>
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

        loadStyles(context, attrs)
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
     * **XML Attribute:** _postalCodeEnabled_
     *
     * <br>
     * Whether or not the postal code is enabled. It is enabled by default.
     * Disabling the postal code field is discouraged because it could
     * impact authorization success rates.
     *
     * A postal code is needed in order to submit an order to Olo's Ordering API. If this
     * field is disabled, you will need to provide another mechanism in your app to collect
     * a postal code before attempting to submit an order to Olo's Ordering API.
     */
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
     * **XML Attribute:** _displayErrors_
     *
     * <br>
     * Whether or not to display built-in error messages
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
     * Set this to receive notifications when configuration changes occur
     */
    var configurationChangeListener: ConfigurationChangeListener? = null

    /**
     * **XML Attribute:** _android:enabled_
     *
     * <br>
     * Enables or disables the control
     *
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

    /** @suppress */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configurationChangeListener?.onConfigurationChanged(newConfig)
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
     * **XML Attributes:** _cardNumberHint, expirationHint, cvvHint, postalCodeHint_
     *
     * <br>
     * Sets the hint text for the specified field
     *
     * @param field The field to set hint text for
     * @param hint  The hint text to be set
     */
    fun setHintText(field: CardField, hint: String) {
        when(field) {
            CardField.Cvv -> _inputWidget.setCvcLabel(hint)
            else -> getTextInputLayout(field).hint = hint
        }
    }

    /**
     * **XML Attributes:** _focusedCardNumberHint, focusedExpirationHint, focusedCvvHint, focusedPostalCodeHint_
     *
     * <br>
     * Sets the hint text for the specified field that gets displayed when the field has focus.
     *
     * @param field The field to set hint text for
     * @param hint  The hint text to be set
     */
    @SuppressLint("RestrictedApi")
    fun setFocusedHintText(field: CardField, hint: String) {
        when(field) {
            CardField.Cvv -> _inputWidget.setCvcPlaceholderText(hint)
            else -> getTextInputLayout(field).placeholderText = hint
        }
    }

    /** Clears all text fields in the control */
    fun clearFields() {
        _clearFieldsInProgress = true

        val focusedField = cardState.focusedField

        //Call this prior to clearing text fields
        cardState.reset()

        _inputWidget.clear()

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
     * **XML Attributes:** _cardBackgroundColor, cardBorderColor, cardBorderWidth, cardBorderRadius_
     *
     * <br>
     * Sets background styles for the card input component of this view.
     *
     * @param backgroundColorHex The background color (in hex format) for the card input component, or null which defaults to `transparent`
     * @param borderColorHex The color for the background border (in hex format) of the card input component, or null which defaults to `transparent`
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
     * **XML Attributes:** _cardBackgroundColor, cardBorderColor, cardBorderWidth, cardBorderRadius_
     *
     * <br>
     * Sets background styles for the card input component of this view.
     *
     * @param backgroundColor The background color for the card input component, or null which defaults to `transparent`
     * @param borderColor The color for the background border of the card input component, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border in pixels, or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null which defaults to `0` and removes the border radius
     */
    fun setCardBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        _inputWidget.setBackgroundStyle(backgroundColor, borderColor,borderWidthPx, borderRadiusPx)
    }

    /**
     * **XML Attribute:** _cardBackground_
     *
     * <br>
     * Sets the background resource for the card input component of this view
     *
     * #### Important
     * _XML attributes associated with [setCardBackgroundStyle] take precedence over `cardBackground`_
     *
     * @param resourceId The id of the resource to be set as the background
     */
    fun setCardBackgroundResource(resourceId: Int) {
        _inputWidget.setBackgroundResource(resourceId)
    }

    /**
     * **XML Attributes:** _errorBackgroundColor, errorBorderColor, errorBorderWidth, errorBorderRadius_
     *
     * <br>
     * Sets background styles for the error component of this view.
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param backgroundColorHex The background color (in hex format) for the error component, or null which defaults to `transparent`
     * @param borderColorHex The color for the background border (in hex format) of the error component, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border in pixels, or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null which defaults to `0` and removes the border radius
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setErrorBackgroundStyle(backgroundColorHex: String? = null, borderColorHex: String? = null,
                                borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        setErrorBackgroundStyle(
            parseColorOrNull(backgroundColorHex),
            parseColorOrNull(borderColorHex),
            borderWidthPx,
            borderRadiusPx)
    }

    /**
     * **XML Attributes:** _errorBackgroundColor, errorBorderColor, errorBorderWidth, errorBorderRadius_
     *
     * <br>
     * Sets background styles for the error component of this view.
     *
     * @param backgroundColor The background color for the error component, or null which defaults to `transparent`
     * @param borderColor The color for the background border of the error component, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border in pixels, or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border in pixels, or null which defaults to `0` and removes the border radius
     */
    fun setErrorBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                                borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        _errorText.setBackgroundStyle(backgroundColor, borderColor,borderWidthPx, borderRadiusPx)
    }

    /**
     * **XML Attribute:** _errorBackground_
     *
     * <br>
     * Sets the background resource for the error component of this view
     *
     * #### Important
     * _XML attributes associated with [setErrorBackgroundStyle] take precedence over `errorBackground`_
     *
     * @param resourceId The id of the resource to be set as the background
     */
    fun setErrorBackgroundResource(resourceId: Int) {
        _errorText.setBackgroundResource(resourceId)
    }

    /**
     * **XML Attributes:** _cardStartPadding, cardTopPadding, cardEndPadding, cardBottomPadding_
     *
     * <br>
     * Sets the padding for the area immediately surrounding the card input fields.
     *
     * @param startPx  Padding (in pixels) for the left of the card input area
     * @param topPx Padding (in pixels) for the top of the card input area
     * @param endPx Padding (in pixels) for the right of the card input area
     * @param bottomPx Padding (in pixels) for the bottom of the card input area
     *                 (the area between the bottom of the input fields and the error message)
     */
    fun setCardPadding(startPx: Int?, topPx: Int?, endPx: Int?, bottomPx: Int?) {
        val start = startPx ?: _inputWidget.paddingLeft
        val top = topPx ?: _inputWidget.paddingTop
        val end = endPx ?: _inputWidget.paddingRight
        val bottom = bottomPx ?: _inputWidget.paddingBottom

        _inputWidget.setPadding(start, top, end, bottom)
    }

    /**
     * **XML Attributes:** _errorStartPadding, errorTopPadding, errorEndPadding, errorBottomPadding_
     *
     * <br>
     * Sets the padding for the error message displayed below the card input fields.
     *
     * @param startPx  Padding (in pixels) for the left of the error message
     * @param topPx Padding (in pixels) for the top of the error message
     * @param endPx Padding (in pixels) for the right of the error message
     * @param bottomPx Padding (in pixels) for the bottom of the error message
     */
    fun setErrorPadding(startPx: Int?, topPx: Int?, endPx: Int?, bottomPx: Int?) {
        val start = startPx ?: _errorText.paddingLeft
        val top = topPx ?: _errorText.paddingTop
        val end = endPx ?: _errorText.paddingRight
        val bottom = bottomPx ?: _errorText.paddingBottom

        _errorText.setPadding(start, top, end, bottom)
    }

    /**
     * **XML Attribute: _verticalSpacing_
     *
     * <br>
     * Sets the vertical space between the card input component and the error message component.
     * This is especially useful if both components have a background defined.
     *
     * @param spacingPx The spacing (in pixels) between the card input component and the error message component
     */
    fun setVerticalSpacing(spacingPx: Int) {
        _errorText.updateLayoutParams<MarginLayoutParams> {
            this.topMargin = spacingPx
        }
    }

    /**
     * **XML Attribute:** _textColor_
     *
     * <br>
     * Sets the text color for all input fields
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setTextColor(color)
        }
    }

    /**
     * **XML Attribute:** _textColor_
     *
     * <br>
     * Sets the text color for all input fields
     *
     * @param color The color to be set
     */
    fun setTextColor(color: Int) {
        for (inputField in _allInputFields) {
            inputField.setTextColor(color)
        }
    }

    /**
     * **XML Attribute:** _textAppearance_
     *
     * <br>
     * Sets the text appearance for all input fields
     *
     * #### Important
     * _XML attributes associated with [setTextColor], [setHintTextColor], and [setTextSize] take precedence over similar attributes that can be defined in `textAppearance`_
     *
     * @param resourceId The id of the resource to be set as the text appearance
     */
    fun setTextAppearance(resourceId: Int) {
        _allInputFields.forEach {
            it.setTextAppearance(resourceId)
        }

        // Need to specifically set hint text color or else the card icon hint color won't get updated
        getColorAttributeFromResource(context, resourceId, android.R.attr.textColorHint)?.let {
            setHintTextColor(it)
        }
    }

    /**
     * **XML Attribute:** _errorTextColor_
     *
     * <br>
     * Sets the error text color for all input fields and error messages
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setErrorTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setErrorTextColor(color)
        }
    }

    /**
     * **XML Attribute:** _errorTextColor_
     *
     * <br>
     * Sets the error text color for all input fields and error messages
     *
     * @param color The color to be set
     */
    fun setErrorTextColor(color: Int) {
        for (fieldState in fieldStates) {
            val inputField = getTextField(fieldState.key)
            inputField.setErrorColor(color)

            // Need to set this to false because Stripe short-circuits updating the error text color
            // if the field is already in an error state
            inputField.shouldShowError = false

            // Stripe error logic differs from CardState error logic so we only need to check `wasEdited` instead
            // of using our CardState class
            inputField.shouldShowError = fieldState.value.wasEdited && !fieldState.value.isValid
        }

        _errorColor = ColorStateList.valueOf(color)
        _errorText.setTextColor(color)
    }

    /**
     * **XML Attribute:** _errorTextAppearance_
     *
     * <br>
     * Sets the text appearance for the error component of this view as well as the error state text color for each input field
     *
     * #### Important
     * _XML attributes associated with [setErrorTextColor] and [setErrorTextSize] take precedence over similar attributes that can be defined in `errorTextAppearance`_
     *
     * @param resourceId The id of the resource to be set as the text appearance
     */
    fun setErrorTextAppearance(resourceId: Int) {
        _errorText.setTextAppearance(resourceId)

        // Load the text color of the errorTextAppearance property and set that
        // on all input fields to keep error text in sync
        getColorAttributeFromResource(context, resourceId, android.R.attr.textColor)?.let {
            setErrorTextColor(it)
        }
    }

    /**
     * **XML Attribute:** _cursorColor_
     *
     * <br>
     * Sets the color for the cursor, selection handles, and text selection highlight
     *
     * #### Important:
     * _This method requires API Level 29 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiQuinceTart)
    fun setCursorColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setCursorColor(color)
        }
    }

    /**
     * **XML Attribute:** _cursorColor_
     *
     * <br>
     * Sets the color for the cursor, selection handles, and text selection highlight
     *
     * #### Important:
     * _This method requires API Level 29 or higher_
     *
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
     * **XML Attribute:** _hintTextColor_
     *
     * <br>
     * Sets the hint text color for all input fields
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setHintTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setHintTextColor(color)
        }
    }

    /**
     * **XML Attribute:** _hintTextColor_
     *
     * <br>
     * Sets the hint text color for all input fields
     *
     * @param color The color to be set
     */
    fun setHintTextColor(color: Int) {
        _hintColor = ColorStateList.valueOf(color)
        _focusedHintColor = _hintColor

        for (inputLayout in _allInputLayouts) {
            inputLayout.defaultHintTextColor = _hintColor
            inputLayout.placeholderTextColor = _hintColor
        }
    }

    /**
     * **XML Attributes: _focusedHintTextColor_
     *
     * <br>
     * Sets the hint text color for when a field has focus. Note that [setHintTextColor] overrides
     * this value, so this method must be called after calling [setHintTextColor].
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setFocusedHintTextColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setFocusedHintTextColor(color)
        }
    }

    /**
     * **XML Attributes: _focusedHintTextColor_
     *
     * <br>
     * Sets the hint text color for when a field has focus. Note that [setHintTextColor] overrides
     * this value, so this method must be called after calling [setHintTextColor].
     *
     * @param color The color to be set
     */
    fun setFocusedHintTextColor(color: Int) {
        _focusedHintColor = ColorStateList.valueOf(color)
        for (inputLayout in _allInputLayouts) {
            inputLayout.hintTextColor = _focusedHintColor
            inputLayout.placeholderTextColor = _focusedHintColor
        }
    }

    /**
     * **XML Attribute:** _textSize_
     *
     * <br>
     * Sets the text size for all input fields
     *
     * @param size The size to be set
     */
    fun setTextSize(size: Float) {
        for (inputField in _allInputFields) {
            inputField.textSize = size
        }
    }

    /**
     * **XML Attribute:** _errorTextSize_
     *
     * <br>
     * Sets the text size for error messages
     *
     * @param size The size to be set
     */
    fun setErrorTextSize(size: Float) {
        _errorText.textSize = size
    }

    /**
     * Gets the font of the input fields
     */
    fun getFont(): Typeface {
        return _allInputFields.first().typeface
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
     * Gets the font of the error message
     */
    fun getErrorFont(): Typeface {
        return _errorText.typeface
    }

    /**
     * Sets the error font for error messages
     * @param font The font to be set
     */
    fun setErrorFont(font: Typeface) {
        _errorText.typeface = font
    }

    /**
     * **XML Attributes:** _fieldUnderlineColor, focusedFieldUnderlineColor_
     *
     * <br>
     * By default, the edit text fields will use the styling defined by the app theme.
     * However, the colors for the edit text field's underline can be customized
     * programmatically by calling this method.
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
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
     * **XML Attributes:** _fieldUnderlineColor, focusedFieldUnderlineColor_
     *
     * When specifying XML attributes, it is recommended to provide both. If only one is provided
     * there may be some scenarios where the view is unable to set the underline color.
     *
     * <br>
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

        delayedCardBrandErrorUpdate()
    }

    private fun delayedCardBrandErrorUpdate() {
        // Having this first call to `cardBrandErrorUpdate` here in addition to the call within the `post` prevents a
        // flicker on each change of the card number value
        cardBrandErrorUpdate()
        post {
            cardBrandErrorUpdate()
        }
    }

    private fun cardBrandErrorUpdate() {
        val cardField = getTextField(CardField.CardNumber)
        if(cardBrand == CardBrand.Unsupported) {
            cardField.shouldShowError = true
        }
    }

    @SuppressLint("RestrictedApi")
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

    private fun loadStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardDetailsMultiLineView) {
        overrideDefaultStyles()
        loadFunctionalityStyles()
        loadBackgroundStyles()
        loadPaddingStyles()
        loadTextStyles()
    }

    private fun TypedArray.loadFunctionalityStyles() {
        getBoolean(R.styleable.PaymentCardDetailsMultiLineView_android_enabled, true).let {
            isEnabled = it
        }

        getBoolean(R.styleable.PaymentCardDetailsMultiLineView_postalCodeEnabled, defaultPostalCodeEnabled).let {
            postalCodeEnabled = it
        }

        getBoolean(R.styleable.PaymentCardDetailsMultiLineView_displayErrors, defaultDisplayErrors).let {
            displayErrors = it
        }

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

        getString(R.styleable.PaymentCardDetailsMultiLineView_focusedCardNumberHint)?.let { hint ->
            setFocusedHintText(CardField.CardNumber, hint)
        }

        getString(R.styleable.PaymentCardDetailsMultiLineView_focusedExpirationHint)?.let { hint ->
            setFocusedHintText(CardField.Expiration, hint)
        }

        getString(R.styleable.PaymentCardDetailsMultiLineView_focusedCvvHint)?.let { hint ->
            setFocusedHintText(CardField.Cvv, hint)
        }

        getString(R.styleable.PaymentCardDetailsMultiLineView_focusedPostalCodeHint)?.let { hint ->
            setFocusedHintText(CardField.PostalCode, hint)
        }
    }

    private fun TypedArray.loadFieldUnderlineColors() {
        var underlineColor = getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_fieldUnderlineColor)
        var focusedUnderlineColor = getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_focusedFieldUnderlineColor)

        if (underlineColor == null && focusedUnderlineColor == null) {
            //No colors were specified... do nothing
            return
        }

        val colors = getTextField(CardField.CardNumber).compoundDrawableTintList
        if (underlineColor == null) {
            underlineColor = colors.defaultColor
        }

        if (focusedUnderlineColor == null) {
            //Attempt to get the default color
            val defaultFocusedUnderlineColor = colors?.getColorForState(intArrayOf(android.R.attr.state_focused), Int.MIN_VALUE)
            if (defaultFocusedUnderlineColor != Int.MIN_VALUE) {
                focusedUnderlineColor = defaultFocusedUnderlineColor
            }
        }

        if (focusedUnderlineColor == null) {
            // No focused color was specified and we were unable to get
            // the focused color from one of the text fields... do nothing
            return
        }

        setFieldUnderlineColors(underlineColor, focusedUnderlineColor)
    }

    private fun TypedArray.loadBackgroundStyles() {
        loadFieldUnderlineColors()

        val cardBackgroundColor = getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardBackgroundColor)
        val cardBackgroundResource = getResourceOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardBackground)
        val cardBorderColor = getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardBorderColor)
        val cardBorderWidth = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardBorderWidth)
        val cardBorderRadius = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardBorderRadius)

        if (cardBackgroundColor != null || cardBorderColor != null || cardBorderWidth != null || cardBorderRadius != null) {
            setCardBackgroundStyle(cardBackgroundColor, cardBorderColor, cardBorderWidth, cardBorderRadius)
        } else if(cardBackgroundResource != null) {
            setCardBackgroundResource(cardBackgroundResource)
        }

        val errorBackgroundColor = getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorBackgroundColor)
        val errorBackgroundResource = getResourceOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorBackground)
        val errorBorderColor = getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorBorderColor)
        val errorBorderWidth = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorBorderWidth)
        val errorBorderRadius = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorBorderRadius)

        if (errorBackgroundColor != null || errorBorderColor != null || errorBorderWidth != null || errorBorderRadius != null) {
            setErrorBackgroundStyle(errorBackgroundColor, errorBorderColor, errorBorderWidth, errorBorderRadius)
        } else if(errorBackgroundResource != null) {
            setErrorBackgroundResource(errorBackgroundResource)
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_verticalSpacing)?.toInt()?.let {
            setVerticalSpacing(it)
        }
    }

    private fun TypedArray.loadPaddingStyles() {
        val cardStartPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardStartPadding)?.toInt()
        val cardEndPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardEndPadding)?.toInt()
        val cardTopPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardTopPadding)?.toInt()
        val cardBottomPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_cardBottomPadding)?.toInt()

        if (cardStartPadding != null || cardEndPadding != null || cardTopPadding != null || cardBottomPadding != null) {
            setCardPadding(cardStartPadding, cardTopPadding, cardEndPadding, cardBottomPadding)
        }

        val errorStartPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorStartPadding)?.toInt()
        val errorEndPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorEndPadding)?.toInt()
        val errorTopPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorTopPadding)?.toInt()
        val errorBottomPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorBottomPadding)?.toInt()

        if (errorStartPadding != null || errorEndPadding != null || errorTopPadding != null || errorBottomPadding != null) {
            setErrorPadding(errorStartPadding, errorTopPadding, errorEndPadding, errorBottomPadding)
        }
    }

    private fun TypedArray.loadTextStyles() {
        // Set required defaults in case of no customization
        val defaultHintColor =
            resources.getColor(R.color.olopay_paymentcarddetailsmultilineview_edittext_hintcolor, context.theme)
        setHintTextColor(defaultHintColor)

        val defaultErrorColor = _inputWidgetBinding.etCardNumber.defaultErrorColorInt
        setErrorTextColor(getColor(R.styleable.PaymentCardDetailsMultiLineView_errorTextColor, defaultErrorColor))

        getResourceOrNull(R.styleable.PaymentCardDetailsMultiLineView_textAppearance)?.let {
            setTextAppearance(it)
        }

        getResourceOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorTextAppearance)?.let {
            setErrorTextAppearance(it)
        }

        getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_hintTextColor)?.let {
            setHintTextColor(it)
        }

        getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_focusedHintTextColor)?.let {
            setFocusedHintTextColor(it)
        }

        getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_textColor)?.let {
            setTextColor(it)
        }

        if (Build.VERSION.SDK_INT >= GlobalConstants.ApiQuinceTart) {
            getColorOrNull(R.styleable.PaymentCardDetailsMultiLineView_cursorTextColor)?.let {
                setCursorColor(it)
            }
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_textSize)?.let {
            setTextSize(it)
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsMultiLineView_errorTextSize)?.let {
            setErrorTextSize(it)
        }
    }

    private fun overrideDefaultStyles() {
        setHintText(CardField.Expiration, resources.getString(R.string.olopay_expiration_hint))
        setHintText(CardField.Cvv, resources.getString(R.string.olopay_cvv_text_hint))
        setHintText(CardField.PostalCode, resources.getString(R.string.olopay_postal_code_hint))

        setFocusedHintText(CardField.CardNumber, "")
        setFocusedHintText(CardField.Expiration, "")
        setFocusedHintText(CardField.Cvv, "")
        setFocusedHintText(CardField.PostalCode, "")
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
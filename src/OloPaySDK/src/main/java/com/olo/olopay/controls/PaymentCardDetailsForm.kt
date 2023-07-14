// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.olo.olopay.R
import com.olo.olopay.controls.callbacks.FormValidCallback
import com.olo.olopay.data.CardField
import com.olo.olopay.internal.data.PaymentMethodParams
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.view.CardFormView
import com.stripe.android.view.CardValidCallback
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.internal.data.GlobalConstants
import com.olo.olopay.internal.data.PaymentMethodSource
import com.olo.olopay.internal.extensions.dismissKeyboard
import com.olo.olopay.internal.extensions.parseColorOrNull
import com.stripe.android.databinding.StripeCardMultilineWidgetBinding
import com.stripe.android.view.StripeEditText
import com.stripe.android.databinding.StripeCardFormViewBinding
import com.olo.olopay.internal.extensions.requestFocus
import com.stripe.android.core.model.CountryCode

/**
 * Convenience multi-field form for collecting card details from a user. Card fields are separated
 * into multiple input fields, and the control has a "card" style background.
 * <hr class="spacer">
 *
 * #### Important:
 *
 * _User-entered card details are intentionally restricted for PCI compliance_
 * <hr class="divider">
 *
 * ## Styling/Branding
 *
 * This class can be styled in the following ways:
 * 1. Override color resource values
 * 1. Set styles programmatically
 *
 * ### Override color resource values
 *
 * This class can be styled by overriding the following color resource values:
 * ```
 * olopay_paymentcarddetailsform_edittext_textcolor
 * olopay_paymentcarddetailsform_edittext_disabledtextcolor
 * olopay_paymentcarddetailsform_background
 * olopay_paymentcarddetailsform_disabledbackground
 * olopay_paymentcarddetailsform_errorcolor
 * olopay_paymentcarddetailsform_bordercolor
 * ```
 * <hr class="spacer">
 * In addition, since this form uses an instance of [PaymentCardDetailsMultiLineView] under
 * the hood, many of the fields can be styled by following style documentation for
 * [PaymentCardDetailsMultiLineView].
 *
 * Note that values specifically called out here will take precedence over similar values for customizing
 * the same attributes on [PaymentCardDetailsMultiLineView]. For example,
 * `olopay_paymentcarddetailsmultilineview_edittext_textcolor` won't have an effect on this form because
 * of `olopay_paymentcarddetailsform_edittext_textcolor`
 *
 * ### Set styles programmatically
 *
 * A number of methods exist that allow you to style [PaymentCardDetailsForm]. Methods exist
 * for changing the background style (color, border, radius, etc), the text colors, error text colors,
 * hint text colors, font, and font size.
 *
 * @constructor Creates a new instance of [PaymentCardDetailsForm]
 */
class PaymentCardDetailsForm @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr), CardValidCallback {
    private val _inputWidget: CardFormView
    private lateinit var _inputWidgetBinding: StripeCardFormViewBinding
    private lateinit var _multiLineWidgetBinding: StripeCardMultilineWidgetBinding
    private lateinit var _allInputFields: Set<StripeEditText>
    private lateinit var _allInputLayouts: Set<TextInputLayout>
    private lateinit var _horizontalDividerViews: Set<View>
    private lateinit var _verticalDividerView: View
    private lateinit var _focusedHintColor: ColorStateList
    private lateinit var _hintColor: ColorStateList
    private var _errorColor: ColorStateList? = null
    private var _isValid = false

    init {
        inflate(context, R.layout.paymentcarddetailsform, this)
        _inputWidget = findViewById(R.id.form_view)
        _inputWidget.setCardValidCallback(this)

        initializeBindings()
        loadXmlStyles(context, attrs)

        val cardField = getTextField(CardField.CardNumber)
        cardField.doAfterTextChanged { updateCardNumberDrawableHintColor() }
        cardField.setOnFocusChangeListener { _, _ -> updateCardNumberDrawableHintColor() }

        val cvcField = getTextField(CardField.Cvc)
        cvcField.doAfterTextChanged { updateCvcDrawableHintColor() }
        cvcField.setOnFocusChangeListener { _, _ -> updateCvcDrawableHintColor() }

        _errorColor = _inputWidgetBinding.errors.textColors

        // Fixes an issue where the postal code field doesn't have the same styling as the rest of the fields
        // in the form. We apply the change to all fields to keep things consistent in case Stripe updates styles to different colors
        val hintColor = resources.getColor(R.color.stripe_card_multiline_textinput_hint_color, context.theme)
        setHintTextColor(hintColor)
    }

    /**
     * `true` if all fields are complete and valid, otherwise `false`
     */
    val isValid: Boolean
        get() = _isValid

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
            if (paymentMethodCreateParams != null)
                return PaymentMethodParams(paymentMethodCreateParams as PaymentMethodCreateParams, PaymentMethodSource.FormInput)

            return null
        }

    /**
     * The detected card brand based on the currently entered card number
     */
    val cardBrand: CardBrand
        get() = CardBrand.convertFrom(_inputWidgetBinding.cardMultilineWidget.brand)

    /**
     * Callback to be notified when the card transitions to valid and invalid states
     */
    var formValidCallback: FormValidCallback? = null

    private val paymentMethodCreateParams: PaymentMethodCreateParams?
        get() = _inputWidget.cardParams?.let { PaymentMethodCreateParams.createCard(it) }

    /**
     * True if the form is enabled and can accept user input, false otherwise
     */
    override fun isEnabled(): Boolean = _inputWidget.isEnabled!!

    /**
     * Enables or disables the control
     * @param enabled Whether or not the control should be enabled/disabled
     */
    override fun setEnabled(enabled: Boolean) { _inputWidget.isEnabled = enabled }

    /** @suppress */
    override fun onInputChanged(isValid: Boolean, invalidFields: Set<CardValidCallback.Fields>) {
        _isValid = isValid

        if (formValidCallback == null)
            return

        val fields = invalidFields.map { CardField.from(it) }
        formValidCallback?.onInputChanged(isValid, fields.toSet())
    }

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
        getTextInputLayout(field).hint = hint
    }

    /** Clears all text fields in the control */
    fun clearFields() {
        for(inputField in _allInputFields) {
            inputField.setText("")
        }

        _inputWidgetBinding.errors.text = ""
        _inputWidgetBinding.errors.isVisible = false
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
        _inputWidgetBinding.cardMultilineWidgetContainer.requestFocus(false)
        _inputWidgetBinding.cardMultilineWidgetContainer.dismissKeyboard()
    }

    /**
     * Sets the border color for this view
     * IMPORTANT: This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setCardBorderColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setCardBorderColor(color)
        }
    }

    /**
     * Sets the border color for this view
     * @param color The color to be set
     */
    fun setCardBorderColor(color: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.setStrokeColor(ColorStateList.valueOf(color))
    }

    /**
     * Sets the color for the field dividers
     * IMPORTANT: This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setFieldDividerColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setFieldDividerColor(color)
        }
    }

    /**
     * Sets the color for the field dividers
     * @param color The color to be set
     */
    fun setFieldDividerColor(color: Int) {
        for (divider in _horizontalDividerViews) {
            divider.setBackgroundColor(color)
        }

        _verticalDividerView.setBackgroundColor(color)
    }

    /**
     * Sets the border width for this view.
     * @param widthPx The width (in pixels) for the outer border
     */
    fun setCardBorderWidth(widthPx: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.strokeWidth = widthPx
    }

    /**
     * Sets the width of the field dividers
     */
    fun setFieldDividerWidth(widthPx: Int) {
        for (divider in _horizontalDividerViews) {
            val params = divider.layoutParams
            params.height = widthPx
            divider.layoutParams = params
        }

        val params = _verticalDividerView.layoutParams
        params.width = widthPx
        _verticalDividerView.layoutParams = params
    }

    /**
     * Sets the border color for this view
     * IMPORTANT: This method requires API level 27 or higher
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setCardBackgroundColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setBackgroundColor(color)
        }
    }

    /**
     * Sets the border color for this view
     * @param color The color to be set
     */
    fun setCardBackgroundColor(color: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.setCardBackgroundColor(color)
    }

    /**
     * Sets the card corner radius for this view
     * @param radius The radius to be set
     */
    fun setCardBorderRadius(radius: Float) {
        _inputWidgetBinding.cardMultilineWidgetContainer.radius = radius
    }

    /**
     * Sets the elevation of the card background
     * @param elevationPx The elevation to be set (in pixels)
     */
    fun setCardElevation(elevationPx: Float) {
        _inputWidgetBinding.cardMultilineWidgetContainer.elevation = elevationPx
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
        val card = _inputWidgetBinding.cardMultilineWidgetContainer
        val start = startPx ?: card.contentPaddingLeft
        val top = topPx ?: card.contentPaddingTop
        val end = endPx ?: card.contentPaddingRight
        val bottom = bottomPx ?: card.contentPaddingBottom

        card.setContentPadding(start, top, end, bottom)
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
        val error = _inputWidgetBinding.errors
        val start = startPx ?: error.paddingLeft
        val top = topPx ?: error.paddingStart
        val end = endPx ?: error.paddingEnd
        val bottom = bottomPx ?: error.paddingBottom

        error.setPadding(start, top, end, bottom)
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
        for (inputLayout in _allInputLayouts) {
            inputLayout.editText?.setTextColor(color)
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

        _errorColor = ColorStateList.valueOf(color)
        _inputWidgetBinding.errors.setTextColor(color)
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
        _hintColor = ColorStateList.valueOf(color)
        _focusedHintColor = _hintColor
        for (inputLayout in _allInputLayouts) {
            inputLayout.defaultHintTextColor = _hintColor
        }

        updateCardNumberDrawableHintColor()
        updateCvcDrawableHintColor()
    }

    /**
     * Sets the hint text color for when a field has focus. Note that [setHintTextColor] overrides
     * this value, so this method must be called after calling [setHintTextColor].
     * IMPORTANT: This method requires API level 27 or higher
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
        updateCvcDrawableHintColor()
    }

    /**
     * Sets the text size for all input fields
     * @param size The size to be set
     */
    fun setTextSize(size: Float) {
        for (inputLayout in _allInputLayouts) {
            inputLayout.editText?.textSize = size
        }
    }

    /**
     * Sets the text size for error messages
     * @param size The size to be set
     */
    fun setErrorTextSize(size: Float) {
        _inputWidgetBinding.errors.textSize = size
    }

    /**
     * Sets the font for all input fields
     * @param font The font to be set
     */
    fun setFont(font: Typeface) {
        for (inputLayout in _allInputLayouts) {
            inputLayout.typeface = font
            inputLayout.editText?.typeface = font
        }
    }

    /**
     * Sets the error font for error messages
     * @param font The font to be set
     */
    fun setErrorFont(font: Typeface) {
        _inputWidgetBinding.errors.typeface = font
    }

    /**
     * Sets the two letter country code for this form
     */
    fun setCountry(countryCode: String) {
        val code = CountryCode.create(countryCode)
        _inputWidgetBinding.countryLayout.setSelectedCountryCode(code)
        _inputWidgetBinding.countryLayout.updateUiForCountryEntered(code)
    }

    private fun initializeBindings() {
        _inputWidgetBinding = StripeCardFormViewBinding.bind(_inputWidget)
        _multiLineWidgetBinding = StripeCardMultilineWidgetBinding.bind(_inputWidgetBinding.cardMultilineWidget)

        _inputWidgetBinding.cardMultilineWidgetContainer.isFocusable = true
        _inputWidgetBinding.cardMultilineWidgetContainer.isFocusableInTouchMode = true
        _inputWidgetBinding.cardMultilineWidgetContainer.requestFocus(false)

        _allInputFields = setOf(
            _multiLineWidgetBinding.etCardNumber,
            _multiLineWidgetBinding.etExpiry,
            _multiLineWidgetBinding.etCvc,
            _inputWidgetBinding.postalCode
        )

        _allInputLayouts = setOf(
            _multiLineWidgetBinding.tlCardNumber,
            _multiLineWidgetBinding.tlExpiry,
            _multiLineWidgetBinding.tlCvc,
            _inputWidgetBinding.postalCodeContainer,
            _inputWidgetBinding.countryLayout
        )

        // IMPORTANT: THESE ARE DIRECTLY TIED TO STRIPE'S LAYOUT IMPLEMENTATION.
        // IF THAT CHANGES, THIS WILL NEED TO CHANGE AS WELL
        // https://github.com/stripe/stripe-android/blob/master/payments-core/src/main/java/com/stripe/android/view/CardFormView.kt#L370-L398
        _verticalDividerView = _inputWidgetBinding.cardMultilineWidget.secondRowLayout.getChildAt(1)
        _horizontalDividerViews = setOf(
            _inputWidgetBinding.cardMultilineWidget.getChildAt(1),
            _inputWidgetBinding.cardMultilineWidget.getChildAt(_inputWidgetBinding.cardMultilineWidget.childCount - 1),
            _inputWidgetBinding.countryPostalDivider
        )
    }

    private fun getTextField(field: CardField): StripeEditText {
        return when(field) {
            CardField.CardNumber -> _multiLineWidgetBinding.etCardNumber
            CardField.Expiration -> _multiLineWidgetBinding.etExpiry
            CardField.Cvc -> _multiLineWidgetBinding.etCvc
            CardField.PostalCode -> _inputWidgetBinding.postalCode
        }
    }

    private fun getTextInputLayout(field: CardField): TextInputLayout {
        return when(field) {
            CardField.CardNumber -> _multiLineWidgetBinding.tlCardNumber
            CardField.Expiration -> _multiLineWidgetBinding.tlExpiry
            CardField.Cvc -> _multiLineWidgetBinding.tlCvc
            CardField.PostalCode -> _inputWidgetBinding.postalCodeContainer
        }
    }

    private fun updateCardNumberDrawableHintColor() {
        val field = _multiLineWidgetBinding.etCardNumber
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

    private fun updateCvcDrawableHintColor() {
        val field = _multiLineWidgetBinding.etCvc
        val fieldText = field.text

        val newColorStateList: ColorStateList? =
            if (field.shouldShowError) {
                // Handle error state
                _errorColor
            } else if (field.hasFocus()) {
                // Handle focused state
                if (fieldText == null || fieldText.isEmpty()) {
                    _focusedHintColor
                } else {
                    field.textColors
                }
            } else {
                // Handle unfocused state
                if (fieldText == null || fieldText.isEmpty()) {
                    _hintColor
                } else {
                    field.textColors
                }
            }

        TextViewCompat.setCompoundDrawableTintList(field, newColorStateList)
    }

    private fun loadXmlStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardDetailsForm) {
        getString(R.styleable.PaymentCardDetailsForm_cardNumberHint)?.let { hint ->
            setHintText(CardField.CardNumber, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_expirationHint)?.let { hint ->
            setHintText(CardField.Expiration, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_cvcHint)?.let { hint ->
            setHintText(CardField.Cvc, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_postalCodeHint)?.let { hint ->
            setHintText(CardField.PostalCode, hint)
        }
    }
}
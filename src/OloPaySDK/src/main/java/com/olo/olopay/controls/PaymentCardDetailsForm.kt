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
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
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
import com.olo.olopay.controls.callbacks.ConfigurationChangeListener
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.internal.data.GlobalConstants
import com.olo.olopay.internal.data.PaymentMethodSource
import com.olo.olopay.internal.extensions.dismissKeyboard
import com.olo.olopay.internal.extensions.getColorAttributeFromResource
import com.olo.olopay.internal.extensions.getColorOrNull
import com.olo.olopay.internal.extensions.getDimensionOrNull
import com.olo.olopay.internal.extensions.getIntOrNull
import com.olo.olopay.internal.extensions.getResourceOrNull
import com.olo.olopay.internal.extensions.parseColorOrNull
import com.olo.olopay.internal.extensions.requestFocus
import com.olo.olopay.internal.extensions.setBackgroundStyle
import com.stripe.android.databinding.StripeCardMultilineWidgetBinding
import com.stripe.android.view.StripeEditText
import com.stripe.android.databinding.StripeCardFormViewBinding
import com.stripe.android.core.model.CountryCode

/**
 * Convenience multi-field form for collecting card details from a user. Card fields are separated
 * into multiple input fields, and the control has a "card" style background.
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
 * <com.olo.olopay.controls.PaymentCardDetailsForm
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
 * <com.olo.olopay.controls.PaymentCardDetailsForm
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
 * - cardBackgroundColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBackgroundColor])_
 * - cardBorderColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBorderColor])_
 * - cardBorderWidth &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBorderWidth])_
 * - cardBorderRadius &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardBorderRadius])_
 * - fieldDividerColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFieldDividerColor])_
 * - fieldDividerWidth &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFieldDividerWidth])_
 * - cardStartPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - cardTopPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - cardEndPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - cardBottomPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCardPadding])_
 * - verticalSpacing &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setVerticalSpacing])_
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
 * - errorGravity &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorGravity])_
 * - cardNumberHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - expirationHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - cvvHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - postalCodeHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - focusedCardNumberHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - focusedExpirationHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - focusedCvvHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
 * - focusedPostalCodeHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setFocusedHintText])_
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
    private var _clearFieldsInProgress = false

    init {
        inflate(context, R.layout.paymentcarddetailsform, this)
        _inputWidget = findViewById(R.id.form_view)
        _inputWidget.setCardValidCallback(this)

        initializeBindings()

        val cardField = getTextField(CardField.CardNumber)
        cardField.doAfterTextChanged { updateCardNumberDrawableHintColor() }
        cardField.setOnFocusChangeListener { _, _ -> updateCardNumberDrawableHintColor() }

        val cvvField = getTextField(CardField.Cvv)
        cvvField.doAfterTextChanged { updateCvvDrawableHintColor() }
        cvvField.setOnFocusChangeListener { _, _ -> updateCvvDrawableHintColor() }

        _errorColor = _inputWidgetBinding.errors.textColors
        setErrorGravity(Gravity.START)

        loadStyles(context, attrs)
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
     * Accessing this property has side-effects if not valid. It will cause form fields to validate themselves
     * and the cursor will move to the first invalid field.
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

    /**
     * Set this to receive notifications when configuration changes occur
     */
    var configurationChangeListener: ConfigurationChangeListener? = null

    private val paymentMethodCreateParams: PaymentMethodCreateParams?
        get() = _inputWidget.cardParams?.let { PaymentMethodCreateParams.createCard(it) }

    /**
     * True if the form is enabled and can accept user input, false otherwise
     */
    override fun isEnabled(): Boolean = _inputWidget.isEnabled

    /**
     * **XML Attribute:** _android:enabled_
     *
     * <br>
     * Enables or disables the control
     * @param enabled Whether or not the control should be enabled/disabled
     */
    override fun setEnabled(enabled: Boolean) { _inputWidget.isEnabled = enabled }

    /** @suppress */
    override fun onInputChanged(isValid: Boolean, invalidFields: Set<CardValidCallback.Fields>) {
        val previousValidState = _isValid
        _isValid = isValid

        // If we are clearing fields we don't want this callback to call notifyValidStateChanged()
        // because it would not have the correct set of invalid fields in certain scenarios.
        // clearFields() will call notifyValidStateChanged when all fields have been cleared.
        if (_clearFieldsInProgress) {
            return
        }

        val fields = invalidFields.map { CardField.from(it) }.toSet()
        formValidCallback?.onInputChanged(isValid, fields)

        notifyValidStateChanged(previousValidState, fields)
    }

    /** @suppress */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configurationChangeListener?.onConfigurationChanged(newConfig)
    }

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
            CardField.Cvv -> _inputWidgetBinding.cardMultilineWidget.setCvcLabel(hint)
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
            CardField.Cvv -> _inputWidgetBinding.cardMultilineWidget.setCvcPlaceholderText(hint)
            else -> getTextInputLayout(field).placeholderText = hint
        }
    }

    /** Clears all text fields in the control */
    fun clearFields() {
        _clearFieldsInProgress = true
        val previousValidState = _isValid

        for(inputField in _allInputFields) {
            inputField.setText("")
        }

        _inputWidgetBinding.errors.text = ""
        _inputWidgetBinding.errors.isVisible = false

        val invalidFields = setOf(
            CardField.CardNumber,
            CardField.Expiration,
            CardField.Cvv,
            CardField.PostalCode
        )

        notifyValidStateChanged(previousValidState, invalidFields)
        requestFocus(CardField.CardNumber,false)

        _clearFieldsInProgress = false

        formValidCallback?.onInputChanged(false, invalidFields)
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
     * **XML Attribute:** _cardBorderColor_
     *
     * <br>
     * Sets the border color for this view
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setCardBorderColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setCardBorderColor(color)
        }
    }

    /**
     * **XML Attribute:** _cardBorderColor_
     *
     * <br>
     * Sets the border color for this view
     *
     * @param color The color to be set
     */
    fun setCardBorderColor(color: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.setStrokeColor(ColorStateList.valueOf(color))
    }

    /**
     * **XML Attribute:** _fieldDividerColor_
     *
     * <br>
     * Sets the color for the field dividers
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setFieldDividerColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setFieldDividerColor(color)
        }
    }

    /**
     * **XML Attribute:** _fieldDividerColor_
     *
     * <br>
     * Sets the color for the field dividers
     *
     * @param color The color to be set
     */
    fun setFieldDividerColor(color: Int) {
        for (divider in _horizontalDividerViews) {
            divider.setBackgroundColor(color)
        }

        _verticalDividerView.setBackgroundColor(color)
    }

    /**
     * **XML Attribute:** _cardBorderWidth_
     *
     * <br>
     * Sets the border width for this view.
     *
     * @param widthPx The width (in pixels) for the outer border
     */
    fun setCardBorderWidth(widthPx: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.strokeWidth = widthPx
    }

    /**
     * **XML Attribute:** _fieldDividerWidth_
     *
     * <br>
     * Sets the width of the field dividers
     *
     * @param widthPx The width (in pixels) for the field dividers
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
     * **XML Attribute:** _cardBackgroundColor_
     *
     * <br>
     * Sets the border color for this view
     *
     * #### Important:
     * _This method requires API level 27 or higher_
     *
     * @param colorHex The color to be set (in Hex format)
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setCardBackgroundColor(colorHex: String) {
        parseColorOrNull(colorHex)?.let { color ->
            setCardBackgroundColor(color)
        }
    }

    /**
     * **XML Attribute:** _cardBackgroundColor_
     *
     * <br>
     * Sets the border color for this view
     *
     * @param color The color to be set
     */
    fun setCardBackgroundColor(color: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.setCardBackgroundColor(color)
    }

    /**
     * **XML Attribute:** cardBorderRadius_
     *
     * <br>
     * Sets the card corner radius for this view
     *
     * @param radius The radius to be set
     */
    fun setCardBorderRadius(radius: Float) {
        _inputWidgetBinding.cardMultilineWidgetContainer.radius = radius
    }

    /**
     * **XML Attribute:** _cardElevation_
     *
     * <br>
     * Sets the elevation of the card background
     *
     * @param elevationPx The elevation to be set (in pixels)
     */
    fun setCardElevation(elevationPx: Float) {
        _inputWidgetBinding.cardMultilineWidgetContainer.elevation = elevationPx
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
        val card = _inputWidgetBinding.cardMultilineWidgetContainer
        val start = startPx ?: card.contentPaddingLeft
        val top = topPx ?: card.contentPaddingTop
        val end = endPx ?: card.contentPaddingRight
        val bottom = bottomPx ?: card.contentPaddingBottom

        card.setContentPadding(start, top, end, bottom)
    }

    /**
     * **XML Attribute:** _cardBackground_
     *
     * <br>
     * Sets the background resource for the card input component of this view
     *
     * #### Important
     * _XML attributes associated with [setCardBackgroundColor], [setCardBorderColor], [setCardBorderWidth], or [setCardBorderRadius] take precedence over `cardBackground`_
     *
     * @param resourceId The id of the resource to be set as the background
     */
    fun setCardBackgroundResource(resourceId: Int) {
        _inputWidgetBinding.cardMultilineWidgetContainer.setBackgroundResource(resourceId)
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
        val error = _inputWidgetBinding.errors
        val start = startPx ?: error.paddingLeft
        val top = topPx ?: error.paddingStart
        val end = endPx ?: error.paddingEnd
        val bottom = bottomPx ?: error.paddingBottom

        error.setPadding(start, top, end, bottom)
    }

    /**
     * **XML Attribute:** _verticalSpacing_
     *
     * <br>
     * Sets the vertical space between the card input component and the error message component.
     * This is especially useful if both components have a background defined.
     *
     * @param spacingPx The spacing (in pixels) between the card input component and the error message component
     */
    fun setVerticalSpacing(spacingPx: Int) {
        _inputWidgetBinding.errors.updateLayoutParams<MarginLayoutParams> {
            this.topMargin = spacingPx
        }
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
        _inputWidgetBinding.cardMultilineWidgetContainer.setBackgroundStyle(backgroundColor, borderColor, borderWidthPx, borderRadiusPx)
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
        _inputWidgetBinding.errors.setBackgroundStyle(backgroundColor, borderColor,borderWidthPx, borderRadiusPx)
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
        _inputWidgetBinding.errors.setBackgroundResource(resourceId)
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
        for (inputLayout in _allInputLayouts) {
            inputLayout.editText?.setTextColor(color)
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
        for (inputField in _allInputFields) {
            inputField.setErrorColor(color)

            if (inputField.shouldShowError) {
                // We need to toggle this because Stripe short-circuits updating the error text color
                // if the field is already in an error state
                inputField.shouldShowError = false
                inputField.shouldShowError = true
            }
        }

        _errorColor = ColorStateList.valueOf(color)
        _inputWidgetBinding.errors.setTextColor(color)
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
        _inputWidgetBinding.errors.setTextAppearance(resourceId)

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

        updateCardNumberDrawableHintColor()
        updateCvvDrawableHintColor()
    }

    /**
     * **XML Attribute:** _focusedHintTextColor_
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
     * **XML Attribute:** _focusedHintTextColor_
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

        updateCardNumberDrawableHintColor()
        updateCvvDrawableHintColor()
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
        for (inputLayout in _allInputLayouts) {
            inputLayout.editText?.textSize = size
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
        _inputWidgetBinding.errors.textSize = size
    }

    /**
     * **XML Attribute:** _errorGravity_
     *
     * <br>
     * Sets the gravity for the built in error message
     *
     * @param gravityPosition The position for the text within the view, default is `start`
     */
    fun setErrorGravity(gravityPosition: Int) {
        _inputWidgetBinding.errors.gravity = gravityPosition
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
            inputLayout.typeface = font
            inputLayout.editText?.typeface = font
        }
    }

    /**
     * Gets the font of the error message
     */
    fun getErrorFont(): Typeface {
        return _inputWidgetBinding.errors.typeface
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
    @SuppressLint("RestrictedApi")
    fun setCountry(countryCode: String) {
        val code = CountryCode.create(countryCode)
        _inputWidgetBinding.countryLayout.setSelectedCountryCode(code)
        _inputWidgetBinding.countryLayout.updateUiForCountryEntered(code)
    }

    @SuppressLint("RestrictedApi")
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

        // Important: THESE ARE DIRECTLY TIED TO STRIPE'S LAYOUT IMPLEMENTATION.
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
            CardField.Cvv -> _multiLineWidgetBinding.etCvc
            CardField.PostalCode -> _inputWidgetBinding.postalCode
        }
    }

    private fun getTextInputLayout(field: CardField): TextInputLayout {
        return when(field) {
            CardField.CardNumber -> _multiLineWidgetBinding.tlCardNumber
            CardField.Expiration -> _multiLineWidgetBinding.tlExpiry
            CardField.Cvv -> _multiLineWidgetBinding.tlCvc
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

    private fun updateCvvDrawableHintColor() {
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

    private fun loadStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardDetailsForm) {
        overrideDefaultStyles()
        loadFunctionalityStyles()
        loadBackgroundStyles()
        loadPaddingStyles()
        loadTextStyles()
    }

    private fun TypedArray.loadFunctionalityStyles() {
        getBoolean(R.styleable.PaymentCardDetailsForm_android_enabled, true).let {
            isEnabled = it
        }

        getString(R.styleable.PaymentCardDetailsForm_cardNumberHint)?.let { hint ->
            setHintText(CardField.CardNumber, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_expirationHint)?.let { hint ->
            setHintText(CardField.Expiration, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_cvvHint)?.let { hint ->
            setHintText(CardField.Cvv, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_postalCodeHint)?.let { hint ->
            setHintText(CardField.PostalCode, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_focusedCardNumberHint)?.let { hint ->
            setFocusedHintText(CardField.CardNumber, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_focusedExpirationHint)?.let { hint ->
            setFocusedHintText(CardField.Expiration, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_focusedCvvHint)?.let { hint ->
            setFocusedHintText(CardField.Cvv, hint)
        }

        getString(R.styleable.PaymentCardDetailsForm_focusedPostalCodeHint)?.let { hint ->
            setFocusedHintText(CardField.PostalCode, hint)
        }
    }

    private fun TypedArray.loadBackgroundStyles() {
        getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardElevation)?.let {
            setCardElevation(it)
        }

        getColorOrNull(R.styleable.PaymentCardDetailsForm_fieldDividerColor)?.let {
            setFieldDividerColor(it)
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsForm_fieldDividerWidth)?.toInt()?.let {
            setFieldDividerWidth(it)
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsForm_verticalSpacing)?.toInt()?.let {
            setVerticalSpacing(it)
        }

        val cardBackgroundResource = getResourceOrNull(R.styleable.PaymentCardDetailsForm_cardBackground)
        val cardBackgroundColor = getColorOrNull(R.styleable.PaymentCardDetailsForm_cardBackgroundColor)
        val cardBorderColor = getColorOrNull(R.styleable.PaymentCardDetailsForm_cardBorderColor)
        val cardBorderWidth = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardBorderWidth)
        val cardBorderRadius = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardBorderRadius)

        if (cardBackgroundColor != null || cardBorderColor != null || cardBorderWidth != null || cardBorderRadius != null) {
            setCardBackgroundStyle(cardBackgroundColor, cardBorderColor, cardBorderWidth, cardBorderRadius)
        } else if(cardBackgroundResource != null) {
            setCardBackgroundResource(cardBackgroundResource)
        }

        val errorBackgroundColor = getColorOrNull(R.styleable.PaymentCardDetailsForm_errorBackgroundColor)
        val errorBackgroundResource = getResourceOrNull(R.styleable.PaymentCardDetailsForm_errorBackground)
        val errorBorderColor = getColorOrNull(R.styleable.PaymentCardDetailsForm_errorBorderColor)
        val errorBorderWidth = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorBorderWidth)
        val errorBorderRadius = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorBorderRadius)

        if (errorBackgroundColor != null || errorBorderColor != null || errorBorderWidth != null || errorBorderRadius != null) {
            setErrorBackgroundStyle(errorBackgroundColor, errorBorderColor, errorBorderWidth, errorBorderRadius)
        } else if(errorBackgroundResource != null) {
            setErrorBackgroundResource(errorBackgroundResource)
        }
    }

    private fun TypedArray.loadPaddingStyles() {
        val cardStartPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardStartPadding)?.toInt()
        val cardEndPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardEndPadding)?.toInt()
        val cardTopPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardTopPadding)?.toInt()
        val cardBottomPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_cardBottomPadding)?.toInt()

        if (cardStartPadding != null || cardEndPadding != null || cardTopPadding != null || cardBottomPadding != null) {
            setCardPadding(cardStartPadding, cardTopPadding, cardEndPadding, cardBottomPadding)
        }

        val errorStartPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorStartPadding)?.toInt()
        val errorEndPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorEndPadding)?.toInt()
        val errorTopPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorTopPadding)?.toInt()
        val errorBottomPadding = getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorBottomPadding)?.toInt()

        if (errorStartPadding != null || errorEndPadding != null || errorTopPadding != null || errorBottomPadding != null) {
            setErrorPadding(errorStartPadding, errorTopPadding, errorEndPadding, errorBottomPadding)
        }
    }

    private fun TypedArray.loadTextStyles() {
        val defaultHintColor =
            resources.getColor(R.color.olopay_paymentcarddetailsmultilineview_edittext_hintcolor, context.theme)

        setHintTextColor(getColor(R.styleable.PaymentCardDetailsForm_hintTextColor, defaultHintColor))
        setFocusedHintTextColor(getColor(R.styleable.PaymentCardDetailsForm_focusedHintTextColor, defaultHintColor))

        val defaultErrorColor = resources.getColor(R.color.olopay_paymentcarddetailsform_errorcolor, context.theme)

        setErrorTextColor(getColor(R.styleable.PaymentCardDetailsForm_errorTextColor, defaultErrorColor))

        getResourceOrNull(R.styleable.PaymentCardDetailsForm_textAppearance)?.let {
            setTextAppearance(it)
        }

        getResourceOrNull(R.styleable.PaymentCardDetailsForm_errorTextAppearance)?.let {
            setErrorTextAppearance(it)
        }

        getColorOrNull(R.styleable.PaymentCardDetailsForm_textColor)?.let {
            setTextColor(it)
        }

        getColorOrNull(R.styleable.PaymentCardDetailsForm_errorTextColor)?.let {
            setErrorTextColor(it)
        }

        if (Build.VERSION.SDK_INT >= GlobalConstants.ApiQuinceTart) {
            getColorOrNull(R.styleable.PaymentCardDetailsForm_cursorTextColor)?.let {
                setCursorColor(it)
            }
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsForm_textSize)?.let {
            setTextSize(it)
        }

        getDimensionOrNull(R.styleable.PaymentCardDetailsForm_errorTextSize)?.let {
            setErrorTextSize(it)
        }

        getIntOrNull(R.styleable.PaymentCardDetailsForm_errorGravity)?.let {
            setErrorGravity(it)
        }
    }

    private fun overrideDefaultStyles() {
        setHintText(CardField.Cvv, resources.getString(R.string.olopay_cvv_text_hint))
        getTextInputLayout(CardField.PostalCode).hint = resources.getString(R.string.olopay_postal_code_hint)
    }

    private fun notifyValidStateChanged(previousValidState: Boolean, invalidFields: Set<CardField>) {
        if (previousValidState != _isValid) {
            formValidCallback?.onValidStateChanged(_isValid, invalidFields)
        }
    }
}
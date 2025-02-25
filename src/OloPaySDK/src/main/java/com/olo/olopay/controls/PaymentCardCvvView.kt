// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.updateLayoutParams
import com.olo.olopay.internal.controls.PaymentCardCvvEditText
import com.olo.olopay.R
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.controls.callbacks.ConfigurationChangeListener
import com.olo.olopay.controls.callbacks.CvvInputListener
import com.olo.olopay.data.ICardFieldState
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.data.ICvvUpdateToken
import com.olo.olopay.internal.callbacks.ValidStateChangedListener
import com.olo.olopay.internal.data.CvvState
import com.olo.olopay.internal.data.CvvTokenParams
import com.olo.olopay.internal.data.GlobalConstants
import com.olo.olopay.internal.data.OloTextWatcher
import com.olo.olopay.internal.extensions.dismissKeyboard
import com.olo.olopay.internal.extensions.getColorAttributeFromResource
import com.olo.olopay.internal.extensions.getColorOrNull
import com.olo.olopay.internal.extensions.getDimensionOrNull
import com.olo.olopay.internal.extensions.getIntOrNull
import com.olo.olopay.internal.extensions.getResourceOrNull
import com.olo.olopay.internal.extensions.parseColorOrNull
import com.olo.olopay.internal.extensions.requestFocus
import com.olo.olopay.internal.extensions.setBackgroundStyle

/**
 * Convenience view for collecting CVV details from a user
 *
 * <br>
 *
 * #### Important:
 * _User-entered CVV details are intentionally restricted to reduce PCI compliance scope_
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
 * <com.olo.olopay.controls.PaymentCardCvvView
 *   app:cvvBorderColor="@color/android:black"
 *   app:cvvBorderWidth="2dp" />
 * />
 * ```
 *
 * #### Customizations applied in a style:
 * ```
 * // Style definition
 * <style name="MyCvvStyle">
 *   <item name="cvvBorderColor">@color/android:black</item>
 *   <item name="cvvBorderWidth">2dp</item>
 * </style>
 *
 * // Layout file
 * <com.olo.olopay.controls.PaymentCardCvvView
 *   style="@style/MyCvvStyle" />
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
 * - cursorColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCursorColor])_
 * - cvvBackgroundColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvBackgroundStyle])_
 * - cvvBorderColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvBackgroundStyle])_
 * - cvvBorderRadius &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvBackgroundStyle])_
 * - cvvBorderWidth &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvBackgroundStyle])_
 * - cvvBottomPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvPadding])_
 * - cvvEndPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvPadding])_
 * - cvvHint &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintText])_
 * - cvvStartPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvPadding])_
 * - cvvTopPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvPadding])_
 * - displayErrors &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [displayErrors])_
 * - errorBackgroundColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBorderColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBorderRadius &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBorderWidth &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundStyle])_
 * - errorBottomPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - errorEndPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - errorGravity &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorGravity])_
 * - errorStartPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - errorTextColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorTextColor])_
 * - errorTextSize &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorTextSize])_
 * - errorTopPadding &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorPadding])_
 * - hintTextColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setHintTextColor])_
 * - textColor &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setTextColor])_
 * - textSize &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setTextSize])_
 * - verticalSpacing &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setVerticalSpacing])_
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
 * - cvvBackground &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setCvvBackgroundResource])_
 * - errorBackground &nbsp;&nbsp;&nbsp; / &nbsp;&nbsp;&nbsp; _(See [setErrorBackgroundResource])_
 *
 * #### Important
 * _Background attributes from the Individual Style Attributes section above take precedence over these background attributes._
 *
 * <hr>
 *
 * @constructor Creates a new instance of [PaymentCardCvvView]
 */
class PaymentCardCvvView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): ConstraintLayout(context, attrs) {
    private val _inputWidget: PaymentCardCvvEditText
    private val _errorText: TextView
    private val cvvState = CvvState()
    private var _displayErrors = defaultDisplayErrors
    private var _errorStateTextColor = context.getColor(R.color.olopay_errortext_textcolor)
    private var _cvvTextColor = context.getColor(R.color.olopay_edittext_textcolor)
    private var _cvvHintTextColor = context.getColor(R.color.olopay_hintcolor)

    init {
        inflate(context, R.layout.paymentcardcvvview, this)
        _inputWidget = findViewById(R.id.cvv_edit_text)
        _errorText = findViewById(R.id.error_text)

        _inputWidget.addTextChangedListener(object: OloTextWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                cvvState.onInputChanged(editable?.toString() ?: "")
                onInputChanged()
                updateError()
            }
        })

        _inputWidget.setOnFocusChangeListener { _, isFocused ->
            cvvState.onFocusChanged(isFocused)
            updateError()
            onFocusChanged()
        }

        cvvState.validStateChangedListener = ValidStateChangedListener {
            onValidStateChanged()
        }

        _inputWidget.isFocusable = true
        _inputWidget.isFocusableInTouchMode = true

        // By making this view focusable, we can clear focus from the input field later
        // by requesting focus on this view
        this.isFocusable = true
        this.isFocusableInTouchMode = true

        // Convert DP units to PX units
        val initialBorderWidthPx = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
        val initialBorderRadiusPx = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)
        val initialPaddingPx = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()

        // Default styling for component
        _inputWidget.setTextColor(_cvvTextColor)
        _inputWidget.setHintTextColor(_cvvHintTextColor)
        _inputWidget.setBackgroundStyle(Color.WHITE, Color.GRAY, initialBorderWidthPx, initialBorderRadiusPx )
        setCvvPadding(initialPaddingPx, null, initialPaddingPx, null)
        _errorText.setTextColor(_errorStateTextColor)

        loadStyles(context, attrs)
    }

    /**
     * Provides a snapshot of the current state of the field
     */
    val fieldState: ICardFieldState
        get() = cvvState.fieldState

    /**
     * Whether or not the input field contains a valid CVV value
     */
    val isValid: Boolean
        get() = cvvState.isValid

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
     * Get an [ICvvTokenParams] instance that can be used to create an [ICvvUpdateToken] instance.
     * If the field is not valid this will return null.
     * <hr class="spacer">
     *
     * @see OloPayAPI.createCvvUpdateToken Use [OloPayAPI.createCvvUpdateToken] to create an instance of [ICvvUpdateToken]
     */
    val cvvTokenParams: ICvvTokenParams?
        get() {
            cvvState.editingCompleted()
            updateError(false)

            val params = _inputWidget.cvvValue

            if (cvvState.isValidCvvCode(params)) {
                // params should NEVER be null... but we need to handle it just to be safe
                return if (params != null) CvvTokenParams(params) else null
            }

            return null
        }

    /**
     * Set this to receive callbacks about card input events for this control
     */
    var cvvInputListener: CvvInputListener? = null

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
     * Clears the text field in this control
     */
    fun clear() {
        val focusedState = cvvState.isFocused
        cvvState.reset()
        cvvState.onFocusChanged(focusedState)
        _inputWidget.setText("")
    }

    /**
     * Moves focus to the input field
     * @param showKeyboard: Whether or not to show the keyboard when the focus changes
     */
    fun requestFocus(showKeyboard: Boolean) {
        _inputWidget.requestFocus(showKeyboard)
    }

    /**
     * Dismisses the keyboard, if visible, and removes focus from the input field in this control
     */
    fun dismissKeyboard() {
        _inputWidget.dismissKeyboard()
        super.requestFocus()
    }

    /** @suppress */
    fun onFocusChanged() {
        cvvInputListener?.onFocusChange(fieldState)
    }

    /** @suppress */
    fun onValidStateChanged() {
        cvvInputListener?.onValidStateChanged(fieldState)
    }

    /** @suppress */
    fun onInputChanged() {
        cvvInputListener?.onInputChanged(fieldState)
    }

    /** @suppress */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configurationChangeListener?.onConfigurationChanged(newConfig)
    }

    /**
     * Get the error message that would be displayed if [isValid] is false.
     * Note that [isValid] having a value of `false` does not necessarily mean there will be an error
     * message (see [ignoreUneditedFieldErrors] param)
     *
     * @param ignoreUneditedFieldErrors If true (default value), the field has not been edited
     *          by the user and will not be considered for validation. The field is considered "edited"
     *          if focus has, at any time, left the field while it contains text
     * @return An error message that can be displayed to the user
     *
     */
    fun getErrorMessage(ignoreUneditedFieldErrors: Boolean): String {
        return cvvState.getErrorMessage(context, ignoreUneditedFieldErrors)
    }

    /**
     * Check if there is an error message to be displayed.
     *
     * @param ignoreUneditedFieldErrors If true (default value), the field has not been edited
     *          by the user and will not be considered for validation. The field is considered "edited"
     *          if focus has, at any time, left the field while it contains text
     * @return `true` if there is an error message that can be displayed to the user
     */
    fun hasErrorMessage(ignoreUneditedFieldErrors: Boolean = true) : Boolean {
        return cvvState.hasErrorMessage(ignoreUneditedFieldErrors)
    }

    private fun updateError(ignoreUneditedFieldErrors: Boolean = true) {
        _inputWidget.setTextColor(if (hasErrorMessage(ignoreUneditedFieldErrors)) _errorStateTextColor else _cvvTextColor)

        if (!displayErrors)
            return

        _errorText.text = getErrorMessage(ignoreUneditedFieldErrors)
        _errorText.visibility = if (hasErrorMessage(ignoreUneditedFieldErrors)) View.VISIBLE else View.GONE
    }

    /**
     * **XML Attribute:** _cvvHint_
     *
     * <br>
     * Sets the hint text for the CVV input
     *
     * @param hint The hint text to be set
     */
    fun setHintText(hint: String){
        _inputWidget.hint = hint
    }

    /**
     * **XML Attribute:** _hintTextColor_
     *
     * <br>
     * Sets the hint text color for the CVV input
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
     * Sets the hint text color for the CVV input
     *
     * @param color The color to be set
     */
    fun setHintTextColor(color: Int) {
        _inputWidget.setHintTextColor(color)
    }

    /**
     * **XML Attribute:** _textColor_
     *
     * <br>
     * Sets the text color for the CVV input
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
     * Sets the text color for the CVV input
     *
     * @param color The color to be set
     */
    fun setTextColor(color: Int) {
        _cvvTextColor = color
        updateError()
    }

    /**
     * **XML Attribute:** _textAppearance_
     *
     * <br>
     * Sets the text appearance for the CVV input
     *
     * #### Important
     * _XML attributes associated with [setTextColor], [setHintTextColor], and [setTextSize] take precedence over similar attributes that can be defined in `textAppearance`_
     *
     * @param resourceId The id of the resource to be set as the text appearance
     */
    fun setTextAppearance(resourceId: Int) {
        _inputWidget.setTextAppearance(resourceId)

        val textColor = getColorAttributeFromResource(context, resourceId, android.R.attr.textColor)
        setTextColor(textColor ?: _cvvTextColor)
    }

    /**
     * **XML Attribute:** _textSize_
     *
     * <br>
     * Sets the text size for the CVV input
     *
     * @param size The size to be set
     */
    fun setTextSize(size: Float) {
        _inputWidget.textSize = size
    }

    /**
     * **XML Attribute:** _errorTextSize_
     *
     * <br>
     * Sets the text size for the error message
     *
     * @param size The size to be set
     */
    fun setErrorTextSize(size: Float) {
        _errorText.textSize = size
    }

    /**
     * **XML Attribute:** _errorGravity_
     *
     * <br>
     * Sets the gravity for the built in error message
     *
     * @param gravityPosition The position for the text within the view, default is `center`
     */
    fun setErrorGravity(gravityPosition: Int) {
        _errorText.gravity = gravityPosition
    }

    /**
     * Gets the font of the CVV input
     */
    fun getFont(): Typeface {
        return _inputWidget.typeface
    }

    /**
     * Sets the font for the CVV input
     * @param font The font to be set
     */
    fun setFont(font: Typeface) {
        _inputWidget.typeface = font
    }

    /**
     * Gets the font of the error message
     */
    fun getErrorFont(): Typeface {
        return _errorText.typeface
    }

    /**
     * Sets the error font for the error message
     * @param font The font to be set
     */
    fun setErrorFont(font: Typeface) {
        _errorText.typeface = font
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
        _inputWidget.textCursorDrawable?.setTint(color)
        _inputWidget.textSelectHandle?.setTint(color)
        _inputWidget.textSelectHandleLeft?.setTint(color)
        _inputWidget.textSelectHandleRight?.setTint(color)
        _inputWidget.highlightColor = color
    }

    /**
     * **XML Attribute:** _errorTextColor_
     *
     * <br>
     * Sets the error text color for the CVV input and error message
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
     * Sets the error text color for the CVV input and error message
     *
     * @param color The color to be set
     */
    fun setErrorTextColor(color: Int) {
        _errorStateTextColor = color
        _errorText.setTextColor(color)
        updateError()
    }

    /**
     * **XML Attribute:** _errorTextAppearance_
     *
     * <br>
     * Sets the text appearance for the error component of this view
     *
     * #### Important
     * _XML attributes associated with [setErrorTextColor] and [setErrorTextSize] take precedence over similar attributes that can be defined in `errorTextAppearance`_
     *
     * @param resourceId The id of the resource to be set as the text appearance
     */
    fun setErrorTextAppearance(resourceId: Int) {
        _errorText.setTextAppearance(resourceId)

        val textColor = getColorAttributeFromResource(context, resourceId, android.R.attr.textColor)
        setErrorTextColor(textColor ?: _errorStateTextColor)
    }

    /**
     * **XML Attributes:** _errorStartPadding, errorTopPadding, errorEndPadding, errorBottomPadding_
     *
     * <br>
     * Sets the padding for the error message displayed below the CVV input
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
    * **XML Attributes:** _errorBackgroundColor, errorBorderColor, errorBorderWidth, errorBorderRadius_
    *
    * <br>
    * Sets background styles for the error component of this view
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
     * Sets background styles for the error component of this view
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
     * Sets the background resource of the error component of this view
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
     * **XML Attributes:** _cvvStartPadding, cvvTopPadding, cvvEndPadding, cvvBottomPadding_
     *
     * <br>
     * Sets the padding for the area immediately surrounding the CVV input
     *
     * @param startPx  Padding (in pixels) for the left of the CVV input area
     * @param topPx Padding (in pixels) for the top of the CVV input area
     * @param endPx Padding (in pixels) for the right of the CVV input area
     * @param bottomPx Padding (in pixels) for the bottom of the CVV input area
     *                 (the area between the bottom of the input field and the error message)
     *
     */
    fun setCvvPadding(startPx: Int?, topPx: Int?, endPx: Int?, bottomPx: Int?) {
        val start = startPx ?: _inputWidget.paddingLeft
        val top = topPx ?: _inputWidget.paddingTop
        val end = endPx ?: _inputWidget.paddingRight
        val bottom = bottomPx ?: _inputWidget.paddingBottom

        _inputWidget.setPadding(start, top, end, bottom)
    }

    /**
     * **XML Attributes:** _cvvBackgroundColor, cvvBorderColor, cvvBorderWidth, cvvBorderRadius_
     *
     * <br>
     * Sets background styles for the CVV input
     *
     * @param backgroundColorHex The background color (in hex format) for the CVV input component, or null which defaults to `transparent`
     * @param borderColorHex The color for the background border (in hex format) of the CVV input component, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border (in pixels), or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border (in pixels), or null which defaults to `0` and removes the border radius
     */
    @RequiresApi(GlobalConstants.ApiOreo)
    fun setCvvBackgroundStyle(backgroundColorHex: String? = null, borderColorHex: String? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
        setCvvBackgroundStyle(
            parseColorOrNull(backgroundColorHex),
            parseColorOrNull(borderColorHex),
            borderWidthPx,
            borderRadiusPx)
    }

    /**
     * **XML Attributes:** _cvvBackgroundColor, cvvBorderColor, cvvBorderWidth, cvvBorderRadius_
     *
     * <br>
     * Sets background styles for the CVV input
     *
     * @param backgroundColor The background color for the CVV input component, or null which defaults to `transparent`
     * @param borderColor The color for the background border of the CVV input component, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border (in pixels), or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border (in pixels), or null which defaults to `0` and removes the border radius
     */
    fun setCvvBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
       _inputWidget.setBackgroundStyle(backgroundColor, borderColor,borderWidthPx, borderRadiusPx)
    }

    /**
     * **XML Attribute:** _cvvBackground_
     *
     * <br>
     * Sets the background resource of the CVV input
     *
     * #### Important
     * _XML attributes associated with [setCvvBackgroundStyle] take precedence over `cvvBackground`_
     *
     * @param resourceId The id of the resource to be set as the background
     */
    fun setCvvBackgroundResource(resourceId: Int) {
        _inputWidget.setBackgroundResource(resourceId)
    }

    /**
     * **XML Attribute:** _android:gravity_
     *
     * <br>
     * Sets the gravity for the CVV input
     *
     * @param gravityPosition The position for the text within the view
     */
    fun setGravity(gravityPosition: Int) {
        _inputWidget.gravity = gravityPosition
    }

    /**
     * **XML Attribute:** _verticalSpacing_
     *
     * <br>
     * Sets the vertical space between the CVV input and the error message
     *
     * @param spacingPx The spacing between the CVV input and the error message
     */
    fun setVerticalSpacing(spacingPx: Int) {
        _errorText.updateLayoutParams<MarginLayoutParams> {
            this.topMargin = spacingPx
        }
    }

    private fun loadStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardCvvView) {
        loadFunctionalityStyles()
        loadBackgroundStyles()
        loadPaddingStyles()
        loadTextStyles()
    }

    private fun TypedArray.loadFunctionalityStyles() {
        getBoolean(R.styleable.PaymentCardCvvView_displayErrors, defaultDisplayErrors).let {
            displayErrors = it
        }

        getBoolean(R.styleable.PaymentCardCvvView_android_enabled, true).let {
            isEnabled = it
        }

        getString(R.styleable.PaymentCardCvvView_cvvHint)?.let { hint ->
            setHintText(hint)
        }
    }

    private fun TypedArray.loadBackgroundStyles() {
        val cvvBackgroundColor = getColorOrNull(R.styleable.PaymentCardCvvView_cvvBackgroundColor)
        val cvvBackgroundResource = getResourceOrNull(R.styleable.PaymentCardCvvView_cvvBackground)
        val cvvBorderColor = getColorOrNull(R.styleable.PaymentCardCvvView_cvvBorderColor)
        val cvvBorderWidth = getDimensionOrNull(R.styleable.PaymentCardCvvView_cvvBorderWidth)
        val cvvBorderRadius = getDimensionOrNull(R.styleable.PaymentCardCvvView_cvvBorderRadius)

        if (cvvBackgroundColor != null || cvvBorderColor != null || cvvBorderWidth != null || cvvBorderRadius != null) {
            setCvvBackgroundStyle(cvvBackgroundColor, cvvBorderColor, cvvBorderWidth, cvvBorderRadius)
        } else if (cvvBackgroundResource != null) {
            setCvvBackgroundResource(cvvBackgroundResource)
        }

        val errorBackgroundColor = getColorOrNull(R.styleable.PaymentCardCvvView_errorBackgroundColor)
        val errorBackgroundResource = getResourceOrNull(R.styleable.PaymentCardCvvView_errorBackground)
        val errorBorderColor = getColorOrNull(R.styleable.PaymentCardCvvView_errorBorderColor)
        val errorBorderWidth = getDimensionOrNull(R.styleable.PaymentCardCvvView_errorBorderWidth)
        val errorBorderRadius = getDimensionOrNull(R.styleable.PaymentCardCvvView_errorBorderRadius)

        if (errorBackgroundColor != null || errorBorderColor != null || errorBorderWidth != null || errorBorderRadius != null) {
            setErrorBackgroundStyle(errorBackgroundColor, errorBorderColor, errorBorderWidth, errorBorderRadius)
        } else if (errorBackgroundResource != null) {
            setErrorBackgroundResource(errorBackgroundResource)
        }

        getDimensionOrNull(R.styleable.PaymentCardCvvView_verticalSpacing)?.toInt()?.let {
            setVerticalSpacing(it)
        }
    }

    private fun TypedArray.loadPaddingStyles() {
        val cvvStartPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_cvvStartPadding)?.toInt()
        val cvvEndPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_cvvEndPadding)?.toInt()
        val cvvTopPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_cvvTopPadding)?.toInt()
        val cvvBottomPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_cvvBottomPadding)?.toInt()

        if (cvvStartPadding != null || cvvEndPadding != null || cvvTopPadding != null || cvvBottomPadding != null) {
            setCvvPadding(cvvStartPadding, cvvTopPadding, cvvEndPadding, cvvBottomPadding)
        }

        val errorStartPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_errorStartPadding)?.toInt()
        val errorEndPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_errorEndPadding)?.toInt()
        val errorTopPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_errorTopPadding)?.toInt()
        val errorBottomPadding = getDimensionOrNull(R.styleable.PaymentCardCvvView_errorBottomPadding)?.toInt()

        if (errorStartPadding != null || errorEndPadding != null || errorTopPadding != null || errorBottomPadding != null) {
            setErrorPadding(errorStartPadding, errorTopPadding, errorEndPadding, errorBottomPadding)
        }
    }

    private fun TypedArray.loadTextStyles() {
        getResourceOrNull(R.styleable.PaymentCardCvvView_textAppearance)?.let {
            setTextAppearance(it)
        }

        getResourceOrNull(R.styleable.PaymentCardCvvView_errorTextAppearance)?.let {
            setErrorTextAppearance(it)
        }

        getColorOrNull(R.styleable.PaymentCardCvvView_textColor)?.let {
            setTextColor(it)
        }

        getColorOrNull(R.styleable.PaymentCardCvvView_errorTextColor)?.let {
            setErrorTextColor(it)
        }

        if (Build.VERSION.SDK_INT >= GlobalConstants.ApiQuinceTart) {
            getColorOrNull(R.styleable.PaymentCardCvvView_cursorTextColor)?.let {
                setCursorColor(it)
            }
        }

        getColorOrNull(R.styleable.PaymentCardCvvView_hintTextColor)?.let {
            setHintTextColor(it)
        }

        getDimensionOrNull(R.styleable.PaymentCardCvvView_textSize)?.let {
            setTextSize(it)
        }

        getDimensionOrNull(R.styleable.PaymentCardCvvView_errorTextSize)?.let {
            setErrorTextSize(it)
        }

        getIntOrNull(R.styleable.PaymentCardCvvView_android_gravity)?.let {
            setGravity(it)
        }

        getIntOrNull(R.styleable.PaymentCardCvvView_errorGravity)?.let {
            setErrorGravity(it)
        }
    }

    /**
     * @suppress
     */
    companion object {
        /**
         * @suppress
         */
        const val defaultDisplayErrors = true
    }
}

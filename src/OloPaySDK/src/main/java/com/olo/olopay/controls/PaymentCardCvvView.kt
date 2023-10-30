// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.olo.olopay.internal.controls.PaymentCardCvvEditText
import com.olo.olopay.R
import com.olo.olopay.api.OloPayAPI
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
import com.olo.olopay.internal.extensions.parseColorOrNull
import com.olo.olopay.internal.extensions.requestFocus
import com.olo.olopay.internal.extensions.setBackgroundStyle

/**
 * Convenience view for collecting CVV details from a user
 *
 * #### Important:
 * User-entered CVV details are intentionally restricted for PCI compliance
 *
 * <hr class="spacer">
 *
 * ## Styling/Branding
 *
 * This class can be styled in the following ways:
 * 1. Override general-purpose resource values
 * 2. Set styles programmatically
 *
 * ### Override general-purpose resource values
 *
 * Overriding general-purpose resource values is the easiest way to control the look/feel of the views
 * in the Olo Pay SDK. Changing these values (listed below) will apply to not just [PaymentCardCvvView],
 * but also [PaymentCardDetailsSingleLineView] and [PaymentCardDetailsMultiLineView]. To override
 * the resources, simply define resources in your app with the same names listed below
 *
 * **Color Resources**
 * ```
 * olopay_edittext_textcolor
 * olopay_errortext_textcolor
 * ```
 *
 * ### Set styles programmatically
 * A number of methods exist that allow you to style [PaymentCardCvvView]. Methods exist
 * for changing the background style (color, border, radius, etc), the text colors, error text colors,
 * hint text colors, font, and font size.
 *
 * @constructor Creates a new instance of [PaymentCardCvvView]
 */
class PaymentCardCvvView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): ConstraintLayout(context,attrs) {
    private val _inputWidget: PaymentCardCvvEditText
    private val _errorText: TextView
    private val cvvState = CvvState()
    private var _displayErrors = defaultDisplayErrors
    private var _errorStateTextColor = context.getColor(R.color.olopay_errortext_textcolor)
    private var _cvvTextColor = context.getColor(R.color.olopay_edittext_textcolor)

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
        _inputWidget.setHintTextColor(Color.GRAY)
        _inputWidget.setBackgroundStyle(Color.WHITE, Color.GRAY, initialBorderWidthPx, initialBorderRadiusPx )
        setCvvPadding(initialPaddingPx, null, initialPaddingPx, null)
        _errorText.setTextColor(_errorStateTextColor)

        loadXmlStyles(context, attrs)
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
     * Enables or disables the control
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
     * Sets the hint text for the CVV input
     * @param hint The hint text to be set
     */
    fun setHintText(hint: String){
        _inputWidget.hint = hint
    }

    /**
     * Sets the hint text color for the CVV input
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
     * Sets the hint text color for the CVV input
     * @param color The color to be set
     */
    fun setHintTextColor(color: Int) {
        _inputWidget.setHintTextColor(color)
    }

    /**
     * Sets the text color for the CVV input
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
     * Sets the text color for the CVV input
     * @param color The color to be set
     */
    fun setTextColor(color: Int) {
        _cvvTextColor = color
        updateError()
    }

    /**
     * Sets the text size for the CVV input
     * @param size The size to be set
     */
    fun setTextSize(size: Float) {
        _inputWidget.textSize = size
    }

    /**
     * Sets the text size for the error message
     * @param size The size to be set
     */
    fun setErrorTextSize(size: Float) {
        _errorText.textSize = size
    }

    /**
     * Sets the font for the CVV input
     * @param font The font to be set
     */
    fun setFont(font: Typeface) {
        _inputWidget.typeface = font
    }

    /**
     * Sets the error font for the error message
     * @param font The font to be set
     */
    fun setErrorFont(font: Typeface) {
        _errorText.typeface = font
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
        _inputWidget.textCursorDrawable?.setTint(color)
        _inputWidget.textSelectHandle?.setTint(color)
        _inputWidget.textSelectHandleLeft?.setTint(color)
        _inputWidget.textSelectHandleRight?.setTint(color)
        _inputWidget.highlightColor = color
    }

    /**
     * Sets the error text color for the CVV input and error message
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
     * Sets the error text color for the CVV input and error message
     * @param color The color to be set
     */
    fun setErrorTextColor(color: Int) {
        _errorStateTextColor = color
        _errorText.setTextColor(color)
        updateError()
    }

    /**
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
     * Sets the padding for the area immediately surrounding the CVV input
     *
     * @param startPx  Padding (in pixels) for the left of the CVV input area
     * @param topPx Padding (in pixels) for the top of the CVV input area
     * @param endPx Padding (in pixels) for the right of the CVV input area
     * @param bottomPx Padding (in pixels) for the bottom of the CVV input area
     *                 (the area between the bottom of the input field and the error message)
     *
     * **_NOTE:_** If you want to set the padding around the entire control (including around the error
     *             message) use View.setPadding()
     */
    fun setCvvPadding(startPx: Int?, topPx: Int?, endPx: Int?, bottomPx: Int?) {
        val start = startPx ?: _inputWidget.paddingLeft
        val top = topPx ?: _inputWidget.paddingTop
        val end = endPx ?: _inputWidget.paddingRight
        val bottom = bottomPx ?: _inputWidget.paddingBottom

        _inputWidget.setPadding(start, top, end, bottom)
    }

    /**
     * Sets background styles for this view.
     * #### Important:
     * This method requires API level 27 or higher
     *
     * @param backgroundColorHex The background color (in hex format), or null which defaults to `transparent`
     * @param borderColorHex The color for the background border (in hex format), or null which defaults to `transparent`
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
     * Sets background styles for this view.
     *
     * @param backgroundColor The background color, or null which defaults to `transparent`
     * @param borderColor The color for the background border, or null which defaults to `transparent`
     * @param borderWidthPx The width of the background border (in pixels), or null which defaults to `0` and removes the border
     * @param borderRadiusPx The radius for the corners of the border (in pixels), or null which defaults to `0` and removes the border radius
     */
    fun setCvvBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                               borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
       _inputWidget.setBackgroundStyle(backgroundColor, borderColor,borderWidthPx, borderRadiusPx)
    }

    /**
     * Sets the gravity for this view
     *
     * @param gravityPosition The position for the text within the view
     */
    fun setGravity(gravityPosition: Int) {
        _inputWidget.gravity = gravityPosition
    }

    private fun loadXmlStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.PaymentCardCvvView) {
        //NOTE: This temp variable is required. For some reason the value does not get set correctly if we set the class properties directly in one statement
        val displayErrorsAttrValue = getBoolean(R.styleable.PaymentCardCvvView_displayErrors, defaultDisplayErrors)
        displayErrors = displayErrorsAttrValue

        getString(R.styleable.PaymentCardCvvView_cvvHint)?.let { hint ->
            setHintText(hint)
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

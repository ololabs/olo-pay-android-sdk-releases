// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import com.google.android.gms.wallet.button.ButtonConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import com.olo.olopay.R
import com.olo.olopay.internal.extensions.dpToPx
import com.olo.olopay.internal.extensions.getDimensionOrNull

/**
 * Convenience view that wraps Google's [PayButton]
 *
 * This view provides all styling and customization options provided by Google's [PayButton] but
 * also adds the following extra functionality:
 * - Styling via XML attributes
 * - Use of android:onClick attribute in XML files
 * - Ability to update button options after view creation
 *
 * #### Important:
 * It is possible to use Google's [PayButton] directly. However, when doing so it is important to
 * ensure that your app uses the same version of `com.google.android.gms:play-services-wallet` as
 * the Olo Pay SDK. Version mismatches could cause unexpected behavior.
 *
 * <hr>
 *
 * ## Styling
 *
 * This view provides all styling options provided by Google's [PayButton]. Styling can be
 * done via XML attributes, or by calling [updateButton].
 *
 * XML attributes can be set directly on the view or they can be defined in a style that gets
 * applied to the view. THe following examples each produce the same result:
 *
 * #### Customizations applied directly to the view:
 * ```
 * // NOTE: This example assumes a namespace definition of `app`. Replace
 * // this with your own namespace qualifier if using something different
 * <com.olo.olopay.googlepay.GooglePayButton
 *   app:googlePayButtonTheme="dark"
 *   app:googlePayButtonType="checkout"
 *   app:googlePayCornerRadius="4dp"
 * />
 * ```
 *
 * #### Customizations applied in a style:
 * ```
 * // Style definition
 * <style name="GooglePayButtonStyle">
 *   <item name="googlePayButtonTheme">dark</item>
 *   <item name="googlePayButtonType">checkout</item>
 *   <item name="googlePayCornerRadius">4dp</item>
 * </style>
 *
 * //Layout file
 * <com.olo.olopay.googlepay.GooglePayButton
 *   style="@style/GooglePayButtonStyle"
 * />
 * ```
 *
 * <hr>
 *
 * @constructor Creates a new instance of [GooglePayButton]. Default styling uses
 * [ButtonConstants.ButtonTheme.DARK], [ButtonConstants.ButtonType.CHECKOUT], and a corner radius
 * of 8dp
 */
class GooglePayButton @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr){
    private var _payButton: PayButton? = null
    private var _clickListener: OnClickListener? = null
    private var _enabled = true

    init {
        isClickable = false
        isFocusable = false
        loadStyles(context, attrs)
    }

    /**
     * @return The enabled state of the button
     */
    override fun isEnabled(): Boolean {
        return _enabled
    }

    /**
     * Set the enabled/clickable state of the button
     *
     * _**Note: **_ Even when the button is disabled, the click animation will still happen due to how Google's Pay Button functions
     *
     * @param enabled The desired enabled state of the button
     */
    override fun setEnabled(enabled: Boolean) {
        _enabled = enabled
    }

    /**
     * Set a callback to handle click events.
     *
     * It is also possible to handle click events in XML using Android's built-in `android:onClick`
     * XML attribute.
     *
     * #### Important:
     * If this click listener is set then `android:onClick` will be ignored
     *
     * @param listener The callback to be executed when the view is clicked
     */
    override fun setOnClickListener(listener: OnClickListener?) {
        _clickListener = listener
    }

    /**
     * Update the button with new options
     *
     * #### Important:
     * Because Google does not provide a way to update button options after initialization, this
     * method removes the current [PayButton] instance from this view and then creates and attaches
     * a new instance.
     *
     * @param theme The visual theme for the button
     * @param type The type of the button
     * @param cornerRadiusPx The corner radius, in pixels, for the rounded corners of the button
     */
    fun updateButton(
        theme: GooglePayButtonTheme,
        type: GooglePayButtonType,
        cornerRadiusPx: Int
    ) {
        _payButton?.let { removeView(it) }

        val newOptions = ButtonOptions.newBuilder()
            .setButtonTheme(theme.value)
            .setButtonType(type.value)
            .setCornerRadius(cornerRadiusPx)
            .setAllowedPaymentMethods(defaultAllowedPaymentMethods)
            .build()

        // Inflate new button and initialize it
        _payButton = (LayoutInflater.from(context).inflate(
            R.layout.olopay_googlepay_button,
            this,
            false
        ) as PayButton).also {
            it.initialize(newOptions)

            it.setOnClickListener { view ->
                if (!_enabled) return@setOnClickListener

                if (_clickListener != null) {
                    // Runtime click listener
                    _clickListener?.onClick(view)
                } else {
                    // Use android's built in onClick XML attribute
                    performClick()
                }
            }

            addView(it)

            // Center the payment button within the wrapper view
            val constraintSet = ConstraintSet()
            constraintSet.clone(this)
            constraintSet.centerHorizontally(it.id, ConstraintSet.PARENT_ID)
            constraintSet.centerVertically(it.id, ConstraintSet.PARENT_ID)
            constraintSet.applyTo(this)
        }
    }

    private fun loadStyles(context: Context, attrs: AttributeSet?) = context.withStyledAttributes(attrs, R.styleable.GooglePayButton) {
        val theme = getInt(R.styleable.GooglePayButton_googlePayButtonTheme, defaultButtonTheme)
        val type = getInt(R.styleable.GooglePayButton_googlePayButtonType, defaultButtonType)

        val radius =
            getDimensionOrNull(R.styleable.GooglePayButton_googlePayCornerRadius) ?:
            defaultCornerRadiusDp.dpToPx(context)

        updateButton(
            GooglePayButtonTheme.convertFrom(theme),
            GooglePayButtonType.convertFrom(type),
            radius.toInt()
        )
    }

    /**
     * @suppress
     */
    companion object {
        /**
         * @suppress
         */
        const val defaultButtonTheme = GooglePayButtonTheme.DARK_ATTR_VALUE

        /**
         * @suppress
         */
        const val defaultButtonType = GooglePayButtonType.CHECKOUT_ATTR_VALUE

        /**
         * @suppress
         */
        const val defaultCornerRadiusDp = 8.0f

        // NOTE: This is required to get the button to show up, but it is ignored by our implementation
        // because we set the allowed payment methods elsewhere in the Olo Pay SDK
        /**
         * @suppress
         */
        const val defaultAllowedPaymentMethods = "[{\"type\":\"CARD\",\"parameters\":{\"allowedAuthMethods\":[\"PAN_ONLY\",\"CRYPTOGRAM_3DS\"],\"allowedCardNetworks\":[\"AMEX\",\"DISCOVER\",\"MASTERCARD\",\"VISA\"]}}]"
    }
}
// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.olo.olopay.R
import com.olo.olopay.controls.callbacks.FormValidCallback
import com.olo.olopay.data.CardField
import com.olo.olopay.internal.data.PaymentMethodParams
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.view.CardFormView
import com.stripe.android.view.CardValidCallback
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.IPaymentMethodParams

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
 * @constructor Creates a new instance of [PaymentCardDetailsForm]
 */
class PaymentCardDetailsForm @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr), CardValidCallback {
    private lateinit var _inputWidget: CardFormView
    private var _isValid = false

    init {
        inflate(context, R.layout.paymentcarddetailsform, this)
    }

    /**
     * Callback to be notified when the card transitions to valid and invalid states
     */
    var formValidCallback: FormValidCallback? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        getInputWidget()
    }

    private fun getInputWidget() : CardFormView? {
        if (this::_inputWidget.isInitialized) {
            return _inputWidget
        }

        try {
            _inputWidget = findViewById(R.id.form_view)
        } catch (e: Exception) {
            return null
        }

        _inputWidget.setCardValidCallback(this)

        return _inputWidget
    }

    /**
     * True if the form is enabled and can accept user input, false otherwise
     */
    override fun isEnabled(): Boolean = getInputWidget()?.isEnabled!!

    /**
     * Enables or disables the control
     * @param enabled Whether or not the control should be enabled/disabled
     */
    override fun setEnabled(enabled: Boolean) { getInputWidget()?.isEnabled = enabled }

    /**
     * `true` if all fields are complete and valid, otherwise `false`
     */
    val isValid: Boolean
    get() {
        if (getInputWidget() == null)
            return false

        return _isValid
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
        if (paymentMethodCreateParams != null)
            return PaymentMethodParams(paymentMethodCreateParams as PaymentMethodCreateParams)

        return null
    }

    private val paymentMethodCreateParams: PaymentMethodCreateParams?
    get() = getInputWidget()?.cardParams?.let { PaymentMethodCreateParams.createCard(it) }

    /** @suppress */
    override fun onInputChanged(isValid: Boolean, invalidFields: Set<CardValidCallback.Fields>) {
        _isValid = isValid

        if (formValidCallback == null)
            return

        val fields = invalidFields.map { CardField.from(it) }
        formValidCallback?.onInputChanged(isValid, fields.toSet())
    }
}
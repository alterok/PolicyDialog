package com.alterok.policydialoglib

import android.content.Context.MODE_PRIVATE
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_policy.view.*

private const val TAG = "PolicyDialog"

class PolicyDialog private constructor(
    private val builder: Builder,
    activity: AppCompatActivity
) : LifecycleObserver {
    private companion object {
        const val PREF_POLICY_ACCEPTED = "com.alterok.policydialog.PREF_POLICY_ACCEPTED"
    }

    interface OnDialogButtonListener {
        fun onAccept(fromUser: Boolean)
        fun onCancel()
    }

    private var policyDialogButtonListeners: MutableList<OnDialogButtonListener> = mutableListOf()

    private val lifecycle = activity.lifecycle
    private val layoutInflater = activity.layoutInflater
    private val prefs =
        activity.applicationContext.getSharedPreferences("alterok.PolicyDialog", MODE_PRIVATE)

    var hasAccepted = prefs.getBoolean(PREF_POLICY_ACCEPTED, false)
        private set(value) {
            field = value
            prefs.edit().putBoolean(PREF_POLICY_ACCEPTED, value).apply()
        }

    var isDestroyed = false
        private set

    private val dialogBuilder = AlertDialog.Builder(activity)
    var dialog: AlertDialog? = null
        private set

    init {
        lifecycle.addObserver(this)
    }

    class Builder(private val activity: AppCompatActivity) {
        internal companion object {
            const val TAG_OPENING_PRIVACY = "{privacy}"
            const val TAG_CLOSING_PRIVACY = "{/privacy}"

            const val TAG_OPENING_TERMS = "{terms}"
            const val TAG_CLOSING_TERMS = "{/terms}"
        }

        val policyLines: MutableList<String> = mutableListOf()

        var allowLogging = true

        var backgroundColor = Color.WHITE

        var isOutlineButtonStyle = true

        var acceptButtonColor = Color.BLACK
        var acceptTextColor = Color.BLACK

        var cancelTextColor = Color.GRAY
        var dialogTitleTextColor = Color.BLACK
        var linkTextColor = Color.BLUE

        var termsSubTextColor = Color.DKGRAY
        var policyLineTextColor = Color.DKGRAY

        var privacyPolicyURL = "https://localhost"
        var termsOfServiceURL = "https://localhost"

        var policySubText: String = activity.getString(R.string.alterok_dialog_policy_tos_subtext)
        var dialogTitle: String = activity.getString(R.string.alterok_dialog_policy_tos)

        var acceptButtonText = activity.getString(R.string.alterok_dialog_policy_accept)
        var cancelButtonText = activity.getString(R.string.alterok_dialog_policy_exit_app)

        fun addPolicyLine(policy: String): Builder {
            policyLines.add(policy)
            return this
        }

        fun create(): PolicyDialog {
            return PolicyDialog(this, activity)
        }
    }

    fun addPolicyDialogListener(listener: OnDialogButtonListener) {
        if (!policyDialogButtonListeners.contains(listener)) {
            policyDialogButtonListeners.add(listener)
        }
    }

    fun removePolicyDialogListener(listener: OnDialogButtonListener) {
        policyDialogButtonListeners.remove(listener)
    }

    private fun createDialogView(): View {
        val dialogView = layoutInflater.inflate(R.layout.dialog_policy, null, false)

        //dialogBackgroundColor
        dialogView.alterok_dialog_policy_container.setBackgroundColor(builder.backgroundColor)
        dialogView.alterok_dialog_policy_recyclerview.backgroundTintList = ColorStateList.valueOf(
            if (ColorUtils.calculateLuminance(builder.backgroundColor) <= 0.5f)
                Color.parseColor("#14F5F5F5")
            else
                Color.parseColor("#14080808")
        )

        //AcceptButton Styling
        dialogView.alterok_dialog_policy_accept_button.setTextColor(builder.acceptTextColor)
        dialogView.alterok_dialog_policy_title.text = builder.dialogTitle
        dialogView.alterok_dialog_policy_title.setTextColor(builder.dialogTitleTextColor)

        dialogView.alterok_dialog_policy_tos_subtext.text = formatToHtml(builder.policySubText)
        dialogView.alterok_dialog_policy_tos_subtext.movementMethod =
            LinkMovementMethod.getInstance()
        dialogView.alterok_dialog_policy_tos_subtext.setLinkTextColor(builder.linkTextColor)
        dialogView.alterok_dialog_policy_tos_subtext.setTextColor(builder.termsSubTextColor)

        if (builder.isOutlineButtonStyle) {
            dialogView.alterok_dialog_policy_accept_button.rippleColor =
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(builder.acceptButtonColor, 64))
        } else {
            dialogView.alterok_dialog_policy_accept_button.backgroundTintList =
                ColorStateList.valueOf(builder.acceptButtonColor)
            dialogView.alterok_dialog_policy_accept_button.rippleColor =
                ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(builder.acceptTextColor, 64)
                )
        }

        dialogView.alterok_dialog_policy_accept_button.strokeColor =
            ColorStateList.valueOf(builder.acceptButtonColor)
        dialogView.alterok_dialog_policy_accept_button.text = builder.acceptButtonText
        dialogView.alterok_dialog_policy_accept_button.setTextColor(builder.acceptTextColor)

        dialogView.alterok_dialog_policy_cancel_button.text = builder.cancelButtonText
        dialogView.alterok_dialog_policy_cancel_button.setTextColor(builder.cancelTextColor)
        dialogView.alterok_dialog_policy_cancel_button.rippleColor =
            ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(builder.cancelTextColor, 64)
            )

        dialogView.alterok_dialog_policy_recyclerview.layoutManager =
            LinearLayoutManager(dialogView.context)


        dialogView.alterok_dialog_policy_accept_button.setOnClickListener {
            hasAccepted = true

            policyDialogButtonListeners.forEach {
                it.onAccept(true)
            }

            destroy()
        }

        dialogView.alterok_dialog_policy_cancel_button.setOnClickListener {
            policyDialogButtonListeners.forEach {
                it.onCancel()
            }
            destroy()
        }

        PrivacyPolicyAdapter(builder.policyLineTextColor).apply {
            dialogView.alterok_dialog_policy_recyclerview.adapter = this
            setPolicies(builder.policyLines)
        }

        dialogView.alterok_dialog_policy_recyclerview.isVisible =
            builder.policyLines.isNotEmpty()

        return dialogView
    }

    private fun formatToHtml(text: String): Spanned {
        val htmlBody = text
            .replace(Builder.TAG_OPENING_PRIVACY, "<a href=\"${builder.privacyPolicyURL}\">")
            .replace(Builder.TAG_CLOSING_PRIVACY, "</a>")
            .replace(Builder.TAG_OPENING_TERMS, "<a href=\"${builder.termsOfServiceURL}\">")
            .replace(Builder.TAG_CLOSING_TERMS, "</a>")

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlBody, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlBody)
        }
    }

    fun show() {
        if (builder.allowLogging)
            Log.d(TAG, "show() called")

        if (isDestroyed) {
            Log.e(
                TAG, "show: ",
                IllegalStateException("Cannot show a destroyed dialog! Please create() a new PolicyDialog from the Builder.")
            )
            return
        }

        if (hasAccepted) {
            policyDialogButtonListeners.forEach {
                it.onAccept(false)
            }
            destroy()
        } else {
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
            }
            dialog = dialogBuilder.create()
            dialog?.setView(createDialogView())
            dialog?.setCancelable(false)
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.show()
        }
    }

    fun dismiss() {
        if (builder.allowLogging)
            Log.d(TAG, "dismiss() called")
        dialog?.dismiss()
        dialog = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        isDestroyed = true

        dismiss()

        if (builder.allowLogging)
            Log.d(TAG, "destroy() called")

        lifecycle.removeObserver(this)
        policyDialogButtonListeners.clear()
    }

    fun reset() {
        if (builder.allowLogging)
            Log.d(TAG, "reset() called")
        hasAccepted = false
    }
}
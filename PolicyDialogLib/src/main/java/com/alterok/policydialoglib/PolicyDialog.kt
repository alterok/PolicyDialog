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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

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
        fun onOptionClicked(optionIndex: Int, isChecked: Boolean)
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

    private val isInEurope = EUHelper.isEu(activity)

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
        val dialogOptions: MutableList<DialogOption> = mutableListOf()

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

        var optionTextColor = Color.DKGRAY
        var optionCheckMarkColor = Color.DKGRAY

        var privacyPolicyURL = "https://localhost"
        var termsOfServiceURL = "https://localhost"

        var policySubText: String = activity.getString(R.string.alterok_dialog_policy_tos_subtext)
        var dialogTitle: String = activity.getString(R.string.alterok_dialog_policy_tos)

        var acceptButtonText = activity.getString(R.string.alterok_dialog_policy_accept)
        var cancelButtonText = activity.getString(R.string.alterok_dialog_policy_exit_app)

        var showInEUOnly = false

        fun addPolicyLine(policy: String): Builder {
            policyLines.add(policy)
            return this
        }

        fun addOption(dialogOption: DialogOption): Builder {
            dialogOptions.add(dialogOption)
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
        dialogView.findViewById<View>(R.id.alterok_dialog_policy_container)
            .setBackgroundColor(builder.backgroundColor)

        //AcceptButton
        dialogView.findViewById<MaterialButton>(R.id.alterok_dialog_policy_accept_button)
            .apply {
                setTextColor(builder.acceptTextColor)

                //AcceptButton Styling
                if (builder.isOutlineButtonStyle) {
                    rippleColor =
                        ColorStateList.valueOf(
                            ColorUtils.setAlphaComponent(
                                builder.acceptButtonColor,
                                64
                            )
                        )
                } else {
                    backgroundTintList =
                        ColorStateList.valueOf(builder.acceptButtonColor)
                    rippleColor =
                        ColorStateList.valueOf(
                            ColorUtils.setAlphaComponent(builder.acceptTextColor, 64)
                        )
                }

                strokeColor =
                    ColorStateList.valueOf(builder.acceptButtonColor)
                text = builder.acceptButtonText
                setTextColor(builder.acceptTextColor)

                setOnClickListener {
                    hasAccepted = true

                    policyDialogButtonListeners.forEach {
                        it.onAccept(true)
                    }

                    destroy()
                }
            }

        //CancelButton
        dialogView.findViewById<MaterialButton>(R.id.alterok_dialog_policy_cancel_button).apply {
            text = builder.cancelButtonText
            setTextColor(builder.cancelTextColor)
            rippleColor =
                ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(builder.cancelTextColor, 64)
                )

            setOnClickListener {
                policyDialogButtonListeners.forEach {
                    it.onCancel()
                }
                destroy()
            }
        }

        //Policy Title
        dialogView.findViewById<TextView>(R.id.alterok_dialog_policy_title).apply {
            text = builder.dialogTitle
            setTextColor(builder.dialogTitleTextColor)
        }

        //TOS Subtext
        dialogView.findViewById<TextView>(R.id.alterok_dialog_policy_tos_subtext).apply {
            text = formatToHtml(builder.policySubText)
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(builder.linkTextColor)
            setTextColor(builder.termsSubTextColor)
        }

        //Policies List
        dialogView.findViewById<RecyclerView>(R.id.alterok_dialog_policy_recyclerview)
            .apply {
                backgroundTintList = ColorStateList.valueOf(
                    if (ColorUtils.calculateLuminance(builder.backgroundColor) <= 0.5f)
                        Color.parseColor("#14F5F5F5")
                    else
                        Color.parseColor("#14080808")
                )

                layoutManager = LinearLayoutManager(dialogView.context)

                PrivacyPolicyAdapter(builder.policyLineTextColor).apply {
                    adapter = this
                    setPolicies(builder.policyLines)
                }

                isVisible = builder.policyLines.isNotEmpty()
            }

        //Dialog Options
        dialogView.findViewById<RecyclerView>(R.id.alterok_dialog_policy_option_recyclerview)
            .apply {
                layoutManager = LinearLayoutManager(dialogView.context)

                DialogOptionAdapter(builder.optionTextColor, builder.optionCheckMarkColor,
                    object : OnDialogOptionClickListener {
                        override fun onDialogOptionClick(
                            isChecked: Boolean,
                            dialogOption: DialogOption
                        ) {
                            policyDialogButtonListeners.forEach {
                                it.onOptionClicked(
                                    builder.dialogOptions.indexOf(dialogOption),
                                    isChecked
                                )
                            }
                        }
                    }).apply {
                    adapter = this
                    setOptions(builder.dialogOptions.asSequence().filter {
                        !(!isInEurope && it.forEUOnly)
                    }.toList())
                }

                isVisible = builder.dialogOptions.isNotEmpty()
            }

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

            if (builder.showInEUOnly && !isInEurope) {
                Log.w(
                    TAG,
                    "Not in EU, Policy and ToS accepted by default. To show PolicyDialog in EU, please set showInEUOnly= true on the PolicyDialog builder."
                )

                hasAccepted = true

                policyDialogButtonListeners.forEach {
                    it.onAccept(false)
                }
            } else {
                dialog = dialogBuilder.create()
                dialog?.setView(createDialogView())
                dialog?.setCancelable(false)
                dialog?.setCanceledOnTouchOutside(false)
                dialog?.show()
            }
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

class DialogOption(val title: String, val defaultValue: Boolean, val forEUOnly: Boolean)
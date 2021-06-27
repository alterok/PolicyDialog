package com.alterok.policydialoglib

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.recyclerview.widget.RecyclerView

internal class DialogOptionAdapter(
    private val optionTextColor: Int,
    private val optionCheckMarkColor: Int,
    val listener: OnDialogOptionClickListener
) :
    RecyclerView.Adapter<OptionViewHolder>() {
    private val options: MutableList<DialogOption> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_dialog_option, parent, false).run {
                OptionViewHolder(this, optionTextColor, optionCheckMarkColor, listener)
            }
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int {
        return options.size
    }

    fun setOptions(options: List<DialogOption>) {
        this.options.clear()
        this.options.addAll(options)

        notifyDataSetChanged()
    }
}

internal class OptionViewHolder(
    itemView: View,
    policyLineTextColor: Int,
    optionCheckMarkColor: Int,
    listener: OnDialogOptionClickListener
) :
    RecyclerView.ViewHolder(itemView) {
    private val optionCheckedTextView = (itemView as? AppCompatCheckedTextView)?.apply {
        setTextColor(policyLineTextColor)
        checkMarkTintList = ColorStateList.valueOf(optionCheckMarkColor)
    }

    private lateinit var dialogOption: DialogOption

    init {
        optionCheckedTextView?.setOnClickListener {
            optionCheckedTextView.isChecked = optionCheckedTextView.isChecked.not()
            listener.onDialogOptionClick(optionCheckedTextView.isChecked, dialogOption)
        }
    }

    fun bind(option: DialogOption) {
        dialogOption = option

        optionCheckedTextView?.text = option.title
        optionCheckedTextView?.isChecked = option.defaultValue
    }
}

internal interface OnDialogOptionClickListener {
    fun onDialogOptionClick(isChecked: Boolean, dialogOption: DialogOption)
}
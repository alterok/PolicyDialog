package com.alterok.policydialoglib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrivacyPolicyAdapter(private val policyLineTextColor: Int) :
    RecyclerView.Adapter<PrivacyPolicyViewHolder>() {
    private val policies: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivacyPolicyViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_policy, parent, false).run {
                PrivacyPolicyViewHolder(this, policyLineTextColor)
            }
    }

    override fun onBindViewHolder(holder: PrivacyPolicyViewHolder, position: Int) {
        holder.bind((position + 1).toString().plus(". ").plus(policies[position]))
    }

    override fun getItemCount(): Int {
        return policies.size
    }

    fun setPolicies(policies: List<String>) {
        this.policies.clear()
        this.policies.addAll(policies)

        notifyDataSetChanged()
    }
}

class PrivacyPolicyViewHolder(itemView: View, policyLineTextColor: Int) :
    RecyclerView.ViewHolder(itemView) {
    private val policyTextView = (itemView as? TextView)?.apply {
        setTextColor(policyLineTextColor)
    }

    fun bind(policy: String) {
        policyTextView?.text = policy
    }
}
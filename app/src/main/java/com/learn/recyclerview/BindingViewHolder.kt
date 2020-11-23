package com.learn.recyclerview

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BindingViewHolder<T>(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: T, position: Int)

}

class SimpleBindingViewHolder<T>(
    binding: ViewDataBinding,
    private val variableId: Int
) : BindingViewHolder<T>(binding) {

    override fun bind(item: T, position: Int) {
        binding.apply { setVariable(variableId, item) }.executePendingBindings()
    }

}

class InvokeBindingViewHolder<T>(
    binding: ViewDataBinding,
    private val doBind: ((binding: ViewDataBinding, item: T, position: Int) -> Unit)?
) : BindingViewHolder<T>(binding) {

    override fun bind(item: T, position: Int) {
        doBind?.invoke(binding, item, position)
    }

}


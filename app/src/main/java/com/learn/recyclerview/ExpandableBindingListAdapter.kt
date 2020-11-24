package com.learn.recyclerview

import androidx.recyclerview.widget.DiffUtil

class ExpandableBindingListAdapter<VH : BindingViewHolder<ExpandableBindingListAdapter.ExpandableWrapper>>
private constructor(
    private val expandController: ExpandController,
    diffItemCallback: DiffUtil.ItemCallback<Any>? = null,
    initList: List<Any>? = null
) : MutableBindingListAdapter<ExpandableBindingListAdapter.ExpandableWrapper, VH>(
    diffItemCallback = DiffItemCallback(diffItemCallback)
) {

    var state: ExpandState = ExpandState.NONE
        private set

    interface ExpandController {

        fun isExpandable(item: Any): Boolean
        fun getChildItems(item: Any): List<Any>?
        fun onExpandedChange(item: Any, expanded: Boolean)

    }

    fun submitItems(items: List<Any>?) {
        submitList(addItems(mutableListOf(), items, false))
    }

    fun expand(position: Int, recursive: Boolean = false) {
        state = ExpandState.EXPAND
        try {
            items[position].run {
                if (isExpandable && !isExpanded) {
                    addAll(position + 1, addItems(mutableListOf(), childItems, recursive))
                    isExpanded = true
                }
            }
        } finally {
            state = ExpandState.NONE
        }
    }

    fun collapse(position: Int) {
        state = ExpandState.COLLAPSE
        try {
            removeRange(position + 1, getExpandedRowCount(position))
            items[position].isExpanded = false
        } finally {
            state = ExpandState.NONE
        }
    }

    private fun addItems(
        list: MutableList<ExpandableWrapper>,
        items: List<Any>?,
        recursive: Boolean
    ): List<ExpandableWrapper> {
        // TODO coroutines and submit partial
        items?.forEach {
            with(ExpandableWrapper(it, expandController)) {
                list.add(this)
                if (recursive && isExpandable) {
                    addItems(list, childItems, recursive)
                    isExpanded = true
                }
            }
        }
        return list
    }

    private fun getExpandedRowCount(position: Int): Int {
        var start = position
        items[position].run {
            if (isExpandable && isExpanded) {
                var stop = childItems?.size ?: 0 + start
                while (start < stop) {
                    val count = getExpandedRowCount(++start)
                    start += count
                    stop += count
                }
            }
        }
        return start - position
    }

    enum class ExpandState { NONE, EXPAND, COLLAPSE }

    data class ExpandableWrapper(
        private val data: Any,
        private val controller: ExpandController
    ) {

        var isExpanded: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    controller.onExpandedChange(data, value)
                }
            }
        val isExpandable: Boolean
            get() = controller.isExpandable(data)
        val childItems: List<Any>?
            get() = controller.getChildItems(data)

        override fun equals(other: Any?): Boolean {
            if (other is ExpandableWrapper) {
                return data == other.data
            }
            return super.equals(other)
        }

        override fun hashCode() = data.hashCode()

    }

    private class DiffItemCallback(private val dataDiffCallback: DiffUtil.ItemCallback<Any>?) :
        DiffUtil.ItemCallback<ExpandableWrapper>() {
        override fun areItemsTheSame(
            oldItem: ExpandableWrapper,
            newItem: ExpandableWrapper
        ) = dataDiffCallback?.areItemsTheSame(oldItem, newItem) ?: false

        override fun areContentsTheSame(
            oldItem: ExpandableWrapper,
            newItem: ExpandableWrapper
        ) = dataDiffCallback?.areContentsTheSame(oldItem, newItem) ?: false

    }

}
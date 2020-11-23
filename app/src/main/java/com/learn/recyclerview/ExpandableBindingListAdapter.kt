package com.learn.recyclerview

import androidx.recyclerview.widget.DiffUtil

interface ExpandableItem {
    val isExpanded: Boolean
    val expandable: Boolean
    val subElements: Collection<ExpandableItem>?
}

interface ExpandableList<T> {
    fun expand(item: T)
    fun collapse(item: T)
    fun expandAll()
    fun collapseAll()
}

class ExpandableBindingListAdapter<T, VH : BindingViewHolder<T>>(diffCallback: DiffUtil.ItemCallback<T>) :
    MutableListAdapter<T>, ExpandableList<T> {

    private val adapter = MutableBindingListAdapter<T, VH>()
}
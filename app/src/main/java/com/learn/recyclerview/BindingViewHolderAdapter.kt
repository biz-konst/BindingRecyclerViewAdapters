package com.learn.recyclerview

import android.view.ViewGroup

interface BindingViewHolderAdapter<T, VH: BindingViewHolder<T>> {

    interface ItemTypeLookup<U> {
        fun getItemType(item: U, position: Int): Int
    }

    val viewHolderFactory: BindingViewHolderFactory<VH>
    val itemTypeLookup: ItemTypeLookup<T>?

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    fun onBindViewHolder(holder: VH, position: Int)
    fun getItemViewType(position: Int): Int

    companion object {
        const val UNKNOWN_ITEM_TYPE = Int.MIN_VALUE
    }

}

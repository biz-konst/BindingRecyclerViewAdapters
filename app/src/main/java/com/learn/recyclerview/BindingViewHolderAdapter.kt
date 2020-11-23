package com.learn.recyclerview

interface BindingViewHolderAdapter<T, VH: BindingViewHolder<*>> {

    interface ItemTypeResolver<U> {
        fun getItemType(item: U, position: Int): Int
    }

    val viewHolderFactory: BindingViewHolderFactory<VH>
    val itemTypeResolver: ItemTypeResolver<T>?

    companion object {
        const val UNKNOWN_ITEM_TYPE = Int.MIN_VALUE
    }

}

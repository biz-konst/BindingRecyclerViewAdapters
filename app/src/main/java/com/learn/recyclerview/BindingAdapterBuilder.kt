package com.learn.recyclerview

import androidx.databinding.ViewDataBinding

@Suppress("unused")
abstract class BindingAdapterBuilder<T, VH : BindingViewHolder<T>> {

    var itemTypeLookup: BindingViewHolderAdapter.ItemTypeLookup<T>? = null
    var viewHolderFactory: BindingViewHolderFactory<VH>? = null
    var defFactory = BindingViewHolderMapFactory<VH>()

    fun setItemTypeResolver(lookup: BindingViewHolderAdapter.ItemTypeLookup<T>) =
        apply { itemTypeLookup = lookup }

    fun setViewHolderFactory(factory: BindingViewHolderFactory<VH>) =
        apply { viewHolderFactory = factory }

    fun addViewHolder(holder: VH, viewType: Int = BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE) =
        apply { defFactory.add(holder, viewType) }

    @Suppress("UNCHECKED_CAST")
    fun addViewHolder(
        binding: ViewDataBinding,
        doBind: ((binding: ViewDataBinding, item: T, position: Int) -> Unit)? = null,
        viewType: Int = BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE
    ) = apply { defFactory.add(InvokeBindingViewHolder(binding, doBind) as VH, viewType) }

    abstract fun build(): BindingViewHolderAdapter<T, VH>

}
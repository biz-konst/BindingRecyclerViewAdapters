package com.learn.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

interface BindingViewHolderFactory<out VH : BindingViewHolder<*>> {

    fun getViewHolderByType(parent: ViewGroup, viewType: Int): VH

}

class ViewHolderFactoryException(message: String) : Throwable(message)

class SimpleBindingViewHolderFactory<T, out VH: BindingViewHolder<*>>(
    private val layoutId: Int,
    private val variableId: Int
) :  BindingViewHolderFactory<VH> {

    @Suppress("UNCHECKED_CAST")
    override fun getViewHolderByType(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, layoutId, parent, false)

        return SimpleBindingViewHolder<T>(binding, variableId) as VH
    }

}

class BindingViewHolderMapFactory<VH : BindingViewHolder<*>> :
    BindingViewHolderFactory<VH> {

    private val holderList = SparseArrayCompat<VH>()

    override fun getViewHolderByType(parent: ViewGroup, viewType: Int) =
        holderList[viewType] ?: throw ViewHolderFactoryException("Unknown view holder type")

    fun add(holder: VH, viewType: Int) {
        holderList.put(viewType, holder)
    }

}

package com.learn.recyclerview

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class BindingListAdapter<T, VH : BindingViewHolder<T>> private constructor(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback), BindingViewHolderAdapter<T, VH> {

    override lateinit var viewHolderFactory: BindingViewHolderFactory<VH>
        private set
    override var itemTypeLookup: BindingViewHolderAdapter.ItemTypeLookup<T>? = null
        private set

    var onListChangedListener: OnListChangedListener<T>? = null

    interface OnListChangedListener<U> {
        fun onListChanged(previousList: List<U>, currentList: List<U>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        viewHolderFactory.getViewHolderByType(parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun getItemViewType(position: Int) =
        itemTypeLookup?.getItemType(getItem(position), position)
            ?: BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE

    override fun onCurrentListChanged(previousList: MutableList<T>, currentList: MutableList<T>) {
        onListChangedListener?.onListChanged(previousList, currentList)
    }

    fun setOnListChangedListener(listener: (previousList: List<T>, currentList: List<T>) -> Unit) {
        onListChangedListener = object : OnListChangedListener<T> {
            override fun onListChanged(previousList: List<T>, currentList: List<T>) {
                listener.invoke(previousList, currentList)
            }
        }
    }

    constructor(
        viewHolderFactory: BindingViewHolderFactory<VH>,
        itemTypeLookup: BindingViewHolderAdapter.ItemTypeLookup<T>? = null,
        diffCallback: DiffUtil.ItemCallback<T> = DiffItemCallback()
    ) : this(diffCallback) {
        this.viewHolderFactory = viewHolderFactory
        this.itemTypeLookup = itemTypeLookup
    }

    constructor(
        holder: VH,
        diffCallback: DiffUtil.ItemCallback<T> = DiffItemCallback()
    ) : this(
        viewHolderFactory = BindingViewHolderMapFactory<VH>()
            .apply { add(holder, BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE) },
        diffCallback = diffCallback
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        binding: ViewDataBinding,
        doBind: ((binding: ViewDataBinding, item: T, position: Int) -> Unit)? = null,
        diffCallback: DiffUtil.ItemCallback<T> = DiffItemCallback()
    ) : this(
        viewHolderFactory = BindingViewHolderMapFactory<VH>()
            .apply {
                add(
                    InvokeBindingViewHolder<T>(binding, doBind) as VH,
                    BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE
                )
            },
        diffCallback = diffCallback
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        binding: ViewDataBinding,
        variableId: Int,
        diffCallback: DiffUtil.ItemCallback<T> = DiffItemCallback()
    ) : this(
        holder = SimpleBindingViewHolder<T>(binding, variableId) as VH,
        diffCallback = diffCallback
    )

    constructor(
        layoutId: Int,
        variableId: Int,
        diffCallback: DiffUtil.ItemCallback<T> = DiffItemCallback()
    ) : this(
        viewHolderFactory = SimpleBindingViewHolderFactory<T, VH>(layoutId, variableId),
        diffCallback = diffCallback
    )

    inner class Builder(private val diffCallback: DiffUtil.ItemCallback<T>) :
        BindingAdapterBuilder<T, VH>() {

        private var onListChangedListener: OnListChangedListener<T>? = null

        fun setOnListChangedListener(listener: OnListChangedListener<T>) =
            apply { onListChangedListener = listener }

        fun setOnListChangedListener(listener: (previousList: List<T>, currentList: List<T>) -> Unit) =
            apply {
                onListChangedListener = object : OnListChangedListener<T> {
                    override fun onListChanged(previousList: List<T>, currentList: List<T>) {
                        listener.invoke(previousList, currentList)
                    }
                }
            }

        override fun build() =
            BindingListAdapter(viewHolderFactory ?: defFactory, itemTypeLookup, diffCallback)
                .also { it.onListChangedListener = onListChangedListener }

    }

    private class DiffItemCallback<T> : DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

        override fun areContentsTheSame(oldItem: T, newItem: T) = true

    }

}

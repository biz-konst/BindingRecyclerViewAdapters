package com.learn.recyclerview

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.*

open class MutableBindingListAdapter<T, VH : BindingViewHolder<T>> private constructor(
    private val diffItemCallback: DiffUtil.ItemCallback<T>? = null
) :
    RecyclerView.Adapter<VH>(), BindingViewHolderAdapter<T, VH>, MutableListAdapter<T> {

    private var items: MutableList<T> = Collections.emptyList()

    private val updateCallback by lazy { AdapterListUpdateCallback(this) }

    final override lateinit var viewHolderFactory: BindingViewHolderFactory<VH>
        private set
    final override var itemTypeResolver: BindingViewHolderAdapter.ItemTypeResolver<T>? = null
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        viewHolderFactory.get(parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun getItemViewType(position: Int) =
        itemTypeResolver?.getItemType(getItem(position), position)
            ?: BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE

    override fun getItemCount() = items.size

    private fun getItem(position: Int) = items[position]

    override fun add(item: T) =
        if (items.add(item)) {
            notifyItemInserted(indexOf(item))
            true
        } else false

    override fun add(position: Int, item: T) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    override fun addAll(position: Int, elements: Collection<T>) =
        if (items.addAll(position, elements)) {
            notifyItemRangeInserted(position, elements.size)
            true
        } else false


    override fun remove(item: T): Boolean {
        val position = indexOf(item)
        return if (items.remove(item)) {
            notifyItemRemoved(position)
            true
        } else false
    }

    override fun remove(position: Int) =
        items.removeAt(position).also { notifyItemRemoved(position) }

    override fun submitList(list: List<T>?) {
        if (list == null || list.isEmpty()) {
            if (items.isNotEmpty()) {
                items = Collections.emptyList()
                notifyDataSetChanged()
            }
        } else if (items.isEmpty()) {
            items = list.toMutableList()
            notifyDataSetChanged()
        } else {
            // TODO do it in coroutines
            val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(list))
            items = list.toMutableList()
            diffResult.dispatchUpdatesTo(updateCallback)
        }
    }

    override fun indexOf(item: T) = items.indexOf(item)

    override fun move(oldPosition: Int, newPosition: Int) {
        add(if (oldPosition < newPosition) newPosition - 1 else newPosition, remove(oldPosition))
        notifyItemMoved(oldPosition, newPosition)
    }

    constructor(
        viewHolderFactory: BindingViewHolderFactory<VH>,
        itemTypeResolver: BindingViewHolderAdapter.ItemTypeResolver<T>? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(diffItemCallback) {
        this.viewHolderFactory = viewHolderFactory
        this.itemTypeResolver = itemTypeResolver
    }

    constructor(
        holder: VH,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(
        viewHolderFactory = BindingViewHolderMapFactory<VH>()
            .apply { add(holder, BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE) },
        diffItemCallback = diffItemCallback
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        binding: ViewDataBinding,
        doBind: ((binding: ViewDataBinding, item: T, position: Int) -> Unit)? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(
        viewHolderFactory = BindingViewHolderMapFactory<VH>()
            .apply {
                add(
                    InvokeBindingViewHolder(binding, doBind) as VH,
                    BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE
                )
            },
        diffItemCallback = diffItemCallback
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        binding: ViewDataBinding,
        variableId: Int,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null,
    ) : this(
        holder = SimpleBindingViewHolder<T>(binding, variableId) as VH,
        diffItemCallback = diffItemCallback
    )

    constructor(
        layoutId: Int,
        variableId: Int,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null,
    ) : this(
        viewHolderFactory = SimpleBindingViewHolderFactory<T, VH>(layoutId, variableId),
        diffItemCallback = diffItemCallback
    )

    inner class Builder : BindingAdapterBuilder<T, VH>() {

        override fun build() =
            MutableBindingListAdapter(viewHolderFactory ?: defFactory, itemTypeResolver)

    }

    private inner class DiffUtilCallback(private val list: List<T>) : DiffUtil.Callback() {

        override fun getOldListSize() = items.size

        override fun getNewListSize() = list.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            diffItemCallback?.areItemsTheSame(items[oldItemPosition], list[newItemPosition])
                    ?: items[oldItemPosition] == list[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            diffItemCallback?.areContentsTheSame(items[oldItemPosition], list[newItemPosition])
                ?: true

    }

}
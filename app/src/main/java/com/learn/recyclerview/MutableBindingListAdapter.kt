package com.learn.recyclerview

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class MutableBindingListAdapter<T, VH : BindingViewHolder<T>> protected constructor(
    initList: List<T>? = null,
    private val diffItemCallback: DiffUtil.ItemCallback<T>? = null
) : RecyclerView.Adapter<VH>(), BindingViewHolderAdapter<T, VH>, MutableListAdapter<T> {

    protected var items = initList?.toMutableList() ?: mutableListOf()

    private val updateCallback by lazy { AdapterListUpdateCallback(this) }

    final override lateinit var viewHolderFactory: BindingViewHolderFactory<VH>
        private set
    final override var itemTypeLookup: BindingViewHolderAdapter.ItemTypeLookup<T>? = null
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        viewHolderFactory.getViewHolderByType(parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun getItemViewType(position: Int) =
        itemTypeLookup?.getItemType(getItem(position), position)
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

    override fun removeRange(position: Int, count: Int): Boolean {
        val trueCount = (position + count).coerceAtLeast(items.size) - position
        if (trueCount > 0) {
            for (pos in position until (position + trueCount)) items.removeAt(pos)
            notifyItemRangeRemoved(position, trueCount)
            return true
        }
        return false
    }

    override fun submitList(list: List<T>?) {
        if (list == null || list.isEmpty()) {
            if (items.isNotEmpty()) {
                items = mutableListOf()
                notifyDataSetChanged()
            }
        } else if (items.isEmpty()) {
            items = list.toMutableList()
            notifyDataSetChanged()
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                val diffResult =
                    withContext(Dispatchers.Default) {
                        DiffUtil.calculateDiff(DiffUtilCallback(list))
                    }
                items = list.toMutableList()
                diffResult.dispatchUpdatesTo(updateCallback)
            }
        }
    }

    override fun indexOf(item: T) = items.indexOf(item)

    override fun move(oldPosition: Int, newPosition: Int) {
        add(if (oldPosition < newPosition) newPosition - 1 else newPosition, remove(oldPosition))
        notifyItemMoved(oldPosition, newPosition)
    }

    constructor(
        viewHolderFactory: BindingViewHolderFactory<VH>,
        itemTypeLookup: BindingViewHolderAdapter.ItemTypeLookup<T>? = null,
        initList: List<T>? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(initList, diffItemCallback) {
        this.viewHolderFactory = viewHolderFactory
        this.itemTypeLookup = itemTypeLookup
    }

    constructor(
        holder: VH,
        initList: List<T>? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(
        viewHolderFactory = BindingViewHolderMapFactory<VH>()
            .apply { add(holder, BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE) },
        initList = initList,
        diffItemCallback = diffItemCallback
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        binding: ViewDataBinding,
        doBind: ((binding: ViewDataBinding, item: T, position: Int) -> Unit)? = null,
        initList: List<T>? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(
        viewHolderFactory = BindingViewHolderMapFactory<VH>()
            .apply {
                add(
                    InvokeBindingViewHolder(binding, doBind) as VH,
                    BindingViewHolderAdapter.UNKNOWN_ITEM_TYPE
                )
            },
        initList = initList,
        diffItemCallback = diffItemCallback
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        binding: ViewDataBinding,
        variableId: Int,
        initList: List<T>? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(
        holder = SimpleBindingViewHolder<T>(binding, variableId) as VH,
        initList = initList,
        diffItemCallback = diffItemCallback
    )

    constructor(
        layoutId: Int,
        variableId: Int,
        initList: List<T>? = null,
        diffItemCallback: DiffUtil.ItemCallback<T>? = null
    ) : this(
        viewHolderFactory = SimpleBindingViewHolderFactory<T, VH>(layoutId, variableId),
        initList = initList,
        diffItemCallback = diffItemCallback
    )

    inner class Builder : BindingAdapterBuilder<T, VH>() {

        override fun build() =
            MutableBindingListAdapter(viewHolderFactory ?: defFactory, itemTypeLookup)

    }

    private inner class DiffUtilCallback(private val list: List<T>) : DiffUtil.Callback() {

        override fun getOldListSize() = items.size

        override fun getNewListSize() = list.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            if (items[oldItemPosition] == null) list[newItemPosition] == null else
                diffItemCallback?.areItemsTheSame(items[oldItemPosition]!!, list[newItemPosition]!!)
                        ?: items[oldItemPosition] == list[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            if (items[oldItemPosition] == null) list[newItemPosition] == null else
                diffItemCallback?.areContentsTheSame(
                    items[oldItemPosition]!!,
                    list[newItemPosition]!!
                )
                    ?: true

    }

}
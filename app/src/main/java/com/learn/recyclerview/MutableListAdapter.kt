package com.learn.recyclerview

interface MutableListAdapter<T> {

    fun add(item: T): Boolean
    fun add(position: Int, item: T)
    fun addAll(position: Int, elements: Collection<T>): Boolean
    fun remove(item: T): Boolean
    fun remove(position: Int): T
    fun indexOf(item: T): Int
    fun move(oldPosition: Int, newPosition: Int)
    fun submitList(list: List<T>?)

}

fun <T> MutableListAdapter<T>.moveAfter(item: T, after: T) {
    move(indexOf(item), indexOf(after) + 1)
}

fun <T> MutableListAdapter<T>.moveBefore(item: T, before: T) {
    move(indexOf(item), indexOf(before))
}


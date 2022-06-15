package com.appknot.sample.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appknot.core_rx.adapter.BindingListAdapter
import com.appknot.core_rx.extensions.binding
import com.appknot.sample.R
import com.appknot.sample.databinding.ItemPassengerBinding
import com.appknot.sample.model.Pokemon

class PagingAdapter : BindingListAdapter<Pokemon, PagingAdapter.PagingViewHolder>(
    object : DiffUtil.ItemCallback<Pokemon>() {
        override fun areContentsTheSame(
            oldItem: Pokemon,
            newItem: Pokemon
        ): Boolean =
            oldItem == newItem

        override fun areItemsTheSame(
            oldItem: Pokemon,
            newItem: Pokemon
        ): Boolean =
            oldItem.name == newItem.name
    }
) {

    class PagingViewHolder(val binding: ItemPassengerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagingViewHolder {
        val binding = parent.binding<ItemPassengerBinding>(R.layout.item_passenger)

        return PagingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PagingViewHolder, position: Int) {
        holder.binding.apply {
            pokemon = getItem(position)
            executePendingBindings()
        }
    }
}
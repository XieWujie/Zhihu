package com.example.administrator.zhihu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.administrator.zhihu.R
import com.example.administrator.zhihu.data.NetworkState
import com.example.administrator.zhihu.data.Story
import com.example.administrator.zhihu.databinding.StoryItemBinding
import java.lang.IllegalArgumentException

class StoryAdapter(private val retry:()->Unit):PagedListAdapter<Story,BaseHodler>(diff){

    private var networkState:NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHodler {
      return when(viewType){
          R.layout.story_item->StoryHolder.create(parent)
          R.layout.network_state_item->NetworkStateItemViewHolder.create(parent,retry)
          else->throw IllegalArgumentException("unknown view type $viewType")
      }
    }

    override fun onBindViewHolder(holder: BaseHodler, position: Int) {
        when(getItemViewType(position)){
            R.layout.story_item-> holder.bind(getItem(position))
            R.layout.network_state_item->holder.bind(networkState)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1){
            R.layout.network_state_item
        }else{
            R.layout.story_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount()+ if (hasExtraRow())1 else 0
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    fun setNetworkState(networkState: NetworkState){
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = networkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow){
            if (hadExtraRow){
                notifyItemRemoved(super.getItemCount())
            }else{
                notifyItemInserted(super.getItemCount())
            }
        }else if (hasExtraRow && previousState != networkState){
            notifyItemChanged(itemCount-1)
        }
    }

    companion object {

        private val diff = Diff();

       private class Diff:DiffUtil.ItemCallback<Story>(){

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
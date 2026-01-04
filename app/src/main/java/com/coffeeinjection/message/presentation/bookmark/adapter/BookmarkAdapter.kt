package com.coffeeinjection.message.presentation.bookmark.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.ItemBookmakBinding
import com.coffeeinjection.message.presentation.bookmark.model.BookmarkModel

class BookmarkAdapter(
    private val onClickItem: (BookmarkModel) -> Unit,
    private val onClickBookmark: (BookmarkModel) -> Unit
) : ListAdapter<BookmarkModel, BookmarkAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBookmakBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClickItem, onClickBookmark)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemBookmakBinding,
        private val onClickItem: (BookmarkModel) -> Unit,
        private val onClickBookmark: (BookmarkModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookmarkModel) = with(binding) {
            // 루트 클릭
            root.setOnClickListener { onClickItem(item) }

            // 텍스트 바인딩 (변경 적은 값만)
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            tvPreview.text = item.preview
            tvTime.text = item.timeText

            ivBookmark.setOnClickListener { onClickBookmark(item) }

            // 아이콘(고정이면 굳이 매번 세팅 안 해도 되지만 명시)
            ivIcon.setImageResource(R.drawable.ic_message)

            // "읽지 않음 점" 표시가 필요하면 여기서 제어
            // viewDot.visibility = if (item.isUnread) View.VISIBLE else View.GONE
        }
    }

    private object Diff : DiffUtil.ItemCallback<BookmarkModel>() {
        override fun areItemsTheSame(oldItem: BookmarkModel, newItem: BookmarkModel): Boolean =
            oldItem.letterId == newItem.letterId

        override fun areContentsTheSame(oldItem: BookmarkModel, newItem: BookmarkModel): Boolean =
            oldItem == newItem
    }
}

package com.coffeeinjection.message.presentation.bookmark.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffeeinjection.message.R
import com.coffeeinjection.message.databinding.ItemBookmakBinding
import com.coffeeinjection.message.presentation.bookmark.model.BookmarkModel
import com.coffeeinjection.message.util.setProfileImageByIndex
import com.coffeeinjection.message.util.toKoreanRelativeOrMdH

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
            tvTime.text = item.timeText.toKoreanRelativeOrMdH()

            // 점 표시 및 메세지 색상 랜덤 적용 필요하면 여기서 제어
            val color = ContextCompat.getColor(root.context, item.accentColorRes)
            val d = viewDot.background?.mutate()
            if (d != null) {
                DrawableCompat.setTint(d, color)   // 원형 유지 + 색만 변경
            }
            cardIcon.setCardBackgroundColor(color)
            layoutParent.setBackgroundColor(color)
        }
    }

    private object Diff : DiffUtil.ItemCallback<BookmarkModel>() {
        override fun areItemsTheSame(oldItem: BookmarkModel, newItem: BookmarkModel): Boolean =
            oldItem.letterId == newItem.letterId

        override fun areContentsTheSame(oldItem: BookmarkModel, newItem: BookmarkModel): Boolean =
            oldItem == newItem
    }

}

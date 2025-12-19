package com.pin.kursovoi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(
    private val context: Context,
    private val reviewList: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewerNameTextView: TextView = itemView.findViewById(R.id.tv_reviewer_name)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rb_review_item_rating)
        val commentTextView: TextView = itemView.findViewById(R.id.tv_review_comment)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_review_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.reviewerNameTextView.text = review.username
        holder.ratingBar.rating = review.rating.toFloat()
        holder.commentTextView.text = review.comment

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(review.reviewDate)
    }

    override fun getItemCount(): Int = reviewList.size
}
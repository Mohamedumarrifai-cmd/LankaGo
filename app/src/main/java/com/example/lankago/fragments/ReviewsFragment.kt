package com.example.lankago.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ReviewsFragment : Fragment() {

    private lateinit var reviewsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.reviews, container, false)

        reviewsContainer = root.findViewById(R.id.layout_reviews_container)

        // Back button
        root.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Write review triggers
        val fabAddReview = root.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_review)
        val btnWriteTop = root.findViewById<TextView>(R.id.btn_write_top)

        fabAddReview?.setOnClickListener { showAddReviewDialog() }
        btnWriteTop?.setOnClickListener { showAddReviewDialog() }

        return root
    }

    private fun showAddReviewDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_review)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val ratingBar = dialog.findViewById<RatingBar>(R.id.dialog_rating_bar)
        val etReviewText = dialog.findViewById<EditText>(R.id.et_dialog_review_text)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_dialog_cancel)
        val btnSubmit = dialog.findViewById<Button>(R.id.btn_dialog_submit)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val text = etReviewText.text.toString().trim()
            val rating = ratingBar.rating

            if (text.isEmpty()) {
                etReviewText.error = "Please write a few words about your experience"
                return@setOnClickListener
            }

            addNewReviewCard("You (Traveler)", "Just now · ★ $rating", text)
            Toast.makeText(requireContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addNewReviewCard(userName: String, meta: String, reviewText: String) {
        val card = MaterialCardView(requireContext()).apply {
            radius = 32f
            cardElevation = 4f
            setCardBackgroundColor(Color.WHITE)
            strokeWidth = 1
            strokeColor = Color.parseColor("#E5E7EB")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
        }

        val cardLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val headerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val avatar = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80)
            setImageResource(android.R.drawable.ic_menu_myplaces)
            setColorFilter(Color.parseColor("#5D5FEF"))
        }

        val nameCol = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 0, 0)
        }

        val tvName = TextView(requireContext()).apply {
            text = userName
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A2B4C"))
        }

        val tvMeta = TextView(requireContext()).apply {
            text = meta
            textSize = 12f
            setTextColor(Color.parseColor("#FFA500"))
        }

        nameCol.addView(tvName)
        nameCol.addView(tvMeta)
        headerLayout.addView(avatar)
        headerLayout.addView(nameCol)

        val tvBody = TextView(requireContext()).apply {
            text = reviewText
            textSize = 14f
            setTextColor(Color.parseColor("#8A8F98"))
            setPadding(0, 16, 0, 0)
        }

        cardLayout.addView(headerLayout)
        cardLayout.addView(tvBody)
        card.addView(cardLayout)

        // Insert at index 1 (right below rating summary card)
        reviewsContainer.addView(card, 1)
    }
}

package com.example.lankago.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CloudReview(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 5.0f,
    val date: String = "",
    val timestamp: Long = 0L,
    val comment: String = ""
)

class ReviewsFragment : Fragment() {

    private lateinit var layoutDynamicReviews: LinearLayout
    private val reviewsList = mutableListOf<CloudReview>()
    private var reviewsListener: ValueEventListener? = null
    private val gson = Gson()

    private val rtdbRef by lazy {
        FirebaseDatabase.getInstance().reference.child("reviews")
    }       //.references-Database root eka access karanawa.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.reviews, container, false)

        layoutDynamicReviews = root.findViewById(R.id.layout_dynamic_reviews)

        // Back button
        root.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Write review triggers
        val fabAddReview = root.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_review)
        val btnWriteTop = root.findViewById<TextView>(R.id.btn_write_top)
        val btnWriteReview = root.findViewById<Button>(R.id.btn_write_review)

        fabAddReview?.setOnClickListener { showAddReviewDialog() }
        btnWriteTop?.setOnClickListener { showAddReviewDialog() }
        btnWriteReview?.setOnClickListener { showAddReviewDialog() }

        loadCachedReviews()
        listenToFirebaseReviews()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reviewsListener?.let { rtdbRef.removeEventListener(it) }
    }

    private fun loadCachedReviews() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("lankago_reviews_pref", Context.MODE_PRIVATE)
        val json = prefs.getString("cached_reviews", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<CloudReview>>() {}.type

                //Gson convert to Json
                val cached: List<CloudReview> = gson.fromJson(json, type)
                if (cached.isNotEmpty()) {
                    reviewsList.clear()
                    reviewsList.addAll(cached)
                    renderReviews()
                }
            } catch (e: Exception) {
                Log.w("ReviewsFragment", "Error loading cached reviews: ${e.message}")
            }
        }
    }

    private fun saveCachedReviews(list: List<CloudReview>) {
        val context = context ?: return
        try {
            val prefs = context.getSharedPreferences("lankago_reviews_pref", Context.MODE_PRIVATE)
            //save the reviews
            prefs.edit().putString("cached_reviews", gson.toJson(list)).apply()
        } catch (_: Exception) {}
    }


    //read the reviews from firebase
    private fun listenToFirebaseReviews() {
        reviewsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetched = mutableListOf<CloudReview>()
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                        val userId = child.child("userId").getValue(String::class.java) ?: ""
                        val userName = child.child("userName").getValue(String::class.java) ?: "Traveler"
                        val ratingVal = child.child("rating").getValue(Double::class.java)?.toFloat()
                            ?: child.child("rating").getValue(Long::class.java)?.toFloat()
                            ?: 5.0f
                        val date = child.child("date").getValue(String::class.java) ?: "Recent"
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val comment = child.child("comment").getValue(String::class.java) ?: ""

                        if (comment.isNotBlank()) {
                            fetched.add(
                                CloudReview(
                                    id = id,
                                    userId = userId,
                                    userName = userName,
                                    rating = ratingVal,
                                    date = date,
                                    timestamp = timestamp,
                                    comment = comment
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("ReviewsFragment", "Error parsing review: ${e.message}")
                    }
                }

                // Sort newest first
                fetched.sortByDescending { it.timestamp }

                reviewsList.clear()
                reviewsList.addAll(fetched)
                saveCachedReviews(fetched)
                renderReviews()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ReviewsFragment", "Firebase reviews cancelled: ${error.message}")
            }
        }

        rtdbRef.addValueEventListener(reviewsListener!!)
    }

    private fun renderReviews() {
        val context = context ?: return
        layoutDynamicReviews.removeAllViews()

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        for (review in reviewsList) {
            val card = MaterialCardView(context).apply {
                radius = 32f
                cardElevation = 0f
                setCardBackgroundColor(Color.WHITE)
                strokeWidth = 2
                strokeColor = Color.parseColor("#E5E7EB")
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
            }

            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val headerLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Avatar initial circle
            val tvAvatar = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(90, 90).apply {
                    setMargins(0, 0, 20, 0)
                }
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.bg_white_circle)
                setBackgroundColor(Color.parseColor("#EEF2FF"))
                text = review.userName.firstOrNull()?.uppercase() ?: "U"
                setTextColor(Color.parseColor("#4F46E5"))
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
            }

            val nameCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val isCurrentUser = currentUid != null && review.userId == currentUid
            val displayName = if (isCurrentUser) "${review.userName} (You)" else review.userName

            val tvName = TextView(context).apply {
                text = displayName
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1A2B4C"))
            }

            val tvMeta = TextView(context).apply {
                text = "${review.date} · ★ ${String.format(Locale.US, "%.1f", review.rating)}"
                textSize = 12f
                setTextColor(Color.parseColor("#FFA500"))
                setTypeface(null, Typeface.BOLD)
            }

            nameCol.addView(tvName)
            nameCol.addView(tvMeta)

            headerLayout.addView(tvAvatar)
            headerLayout.addView(nameCol)

            val tvBody = TextView(context).apply {
                text = review.comment
                textSize = 14f
                setTextColor(Color.parseColor("#4B5563"))
                setPadding(0, 20, 0, 0)
                setLineSpacing(0f, 1.2f)
            }

            cardLayout.addView(headerLayout)
            cardLayout.addView(tvBody)
            card.addView(cardLayout)

            layoutDynamicReviews.addView(card)
        }
    }

    //add the review
    private fun showAddReviewDialog() {
        val context = context ?: return
        val dialog = Dialog(context)
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

            val authUser = FirebaseAuth.getInstance().currentUser
            val authorName = authUser?.displayName?.takeIf { it.isNotBlank() }
                ?: authUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "Traveler"
            val uid = authUser?.uid ?: "anonymous_${System.currentTimeMillis()}"
            val reviewId = rtdbRef.push().key ?: "rev_${System.currentTimeMillis()}"
            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())

            val newReview = CloudReview(
                id = reviewId,
                userId = uid,
                userName = authorName,
                rating = rating,
                date = dateStr,
                timestamp = System.currentTimeMillis(),
                comment = text
            )

            // Save to Firebase RTDB node /reviews/{reviewId}
            val map = hashMapOf(
                "id" to newReview.id,
                "userId" to newReview.userId,
                "userName" to newReview.userName,
                "rating" to newReview.rating.toDouble(),
                "date" to newReview.date,
                "timestamp" to newReview.timestamp,
                "comment" to newReview.comment
            )
            rtdbRef.child(reviewId).setValue(map)  //save the review details

            // Add locally immediately
            reviewsList.add(0, newReview)
            saveCachedReviews(reviewsList)
            renderReviews()

            Toast.makeText(context, "Review submitted successfully! Shared with community.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}

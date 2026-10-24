package com.example.lankago.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Centralized manager ensuring persistent, per-user profile image storage.
 * Synchronizes across:
 * 1. User-specific local file cache (avatar_{uid}.jpg) for instant offline display
 * 2. Firebase Realtime Database (/users/{uid}/profileImageUrl) as Base64 Data URI (100% reliable)
 * 3. Firebase Cloud Storage (/profile_pictures/{uid}.jpg) when available
 */
object ProfileAvatarManager {

    private const val TAG = "ProfileAvatarManager"
    const val PREFS_PROFILE = "lankago_user_profile"
    private const val KEY_AVATAR_PATH_PREFIX = "key_avatar_path_"

    /**
     * Resolves the current unique user ID.
     * Uses FirebaseAuth UID if available; otherwise falls back to the logged in email.
     */
    fun getEffectiveUserId(context: Context): String {
        val authUser = FirebaseAuth.getInstance().currentUser
        if (authUser != null && authUser.uid.isNotBlank()) {
            return authUser.uid
        }
        val email = AuthManager.getLoggedInEmail(context)
        return email.lowercase().replace(Regex("[^a-z0-9]"), "_").ifBlank { "guest_user" }
    }

    /**
     * Loads the avatar for the current user into the given ImageView.
     */
    fun loadAvatar(
        fragment: Fragment,
        ivAvatar: ImageView?,
        placeholderRes: Int = R.drawable.ic_admin_profile,
        onLoaded: ((Boolean) -> Unit)? = null
    ) {
        if (ivAvatar == null) return
        val context = fragment.context ?: return
        val uid = getEffectiveUserId(context)
        val profilePrefs = context.getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE)

        // 1. Instant local file check (user-specific)
        val localAvatarFile = File(context.filesDir, "avatar_${uid}.jpg")
        if (localAvatarFile.exists() && localAvatarFile.length() > 0) {
            ivAvatar.imageTintList = null
            Glide.with(fragment)
                .load(localAvatarFile)
                .circleCrop()
                .placeholder(placeholderRes)
                .into(ivAvatar)
            onLoaded?.invoke(true)
        }

        // 2. Cloud lookup from Firebase Realtime Database
        val authUser = FirebaseAuth.getInstance().currentUser ?: return
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(authUser.uid)
                val snapshot = dbRef.get().await()
                val profileUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                if (!profileUrl.isNullOrBlank()) {
                    ivAvatar.imageTintList = null
                    if (profileUrl.startsWith("data:image/")) {
                        // Decode Base64 Data URI and cache to local file
                        withContext(Dispatchers.IO) {
                            try {
                                val base64Part = profileUrl.substringAfter(",")
                                val imageBytes = Base64.decode(base64Part, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                if (bitmap != null) {
                                    FileOutputStream(localAvatarFile).use { out ->
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    profilePrefs.edit()
                                        .putString("${KEY_AVATAR_PATH_PREFIX}$uid", localAvatarFile.absolutePath)
                                        .apply()

                                    withContext(Dispatchers.Main) {
                                        Glide.with(fragment)
                                            .load(localAvatarFile)
                                            .circleCrop()
                                            .into(ivAvatar)
                                        onLoaded?.invoke(true)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Base64 decode failed: ${e.message}")
                            }
                        }
                    } else {
                        // Standard HTTP / Firebase Storage URL
                        Glide.with(fragment)
                            .load(profileUrl)
                            .circleCrop()
                            .placeholder(placeholderRes)
                            .into(ivAvatar)
                        onLoaded?.invoke(true)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cloud avatar fetch failed: ${e.message}")
            }
        }
    }

    /**
     * Saves a selected image for the current user across local storage and Firebase.
     */
    fun saveAvatar(
        fragment: Fragment,
        imageUri: Uri,
        ivAvatar: ImageView?,
        onComplete: (Boolean, String) -> Unit
    ) {
        val context = fragment.requireContext()
        val uid = getEffectiveUserId(context)
        val profilePrefs = context.getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE)

        // Preview immediately
        if (ivAvatar != null) {
            ivAvatar.imageTintList = null
            Glide.with(fragment)
                .load(imageUri)
                .circleCrop()
                .into(ivAvatar)
        }

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            try {
                val (avatarFile, base64Uri) = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                        ?: throw IllegalStateException("Cannot open input stream for image")
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                        ?: throw IllegalStateException("Failed to decode image from Uri")
                    inputStream.close()

                    // Scale down to max 400x400 maintaining aspect ratio
                    val maxDimension = 400
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scale = if (width > height) {
                        maxDimension.toFloat() / width
                    } else {
                        maxDimension.toFloat() / height
                    }
                    val scaledBitmap = if (scale < 1.0f) {
                        Bitmap.createScaledBitmap(
                            originalBitmap,
                            (width * scale).toInt().coerceAtLeast(1),
                            (height * scale).toInt().coerceAtLeast(1),
                            true
                        )
                    } else {
                        originalBitmap
                    }

                    // Write user-specific local file
                    val userFile = File(context.filesDir, "avatar_${uid}.jpg")
                    val byteStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteStream)
                    val imageBytes = byteStream.toByteArray()

                    FileOutputStream(userFile).use { out ->
                        out.write(imageBytes)
                    }

                    val encodedBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    Pair(userFile, encodedBase64)
                }

                // Persist path in SharedPreferences for this specific user
                profilePrefs.edit()
                    .putString("${KEY_AVATAR_PATH_PREFIX}$uid", avatarFile.absolutePath)
                    .apply()

                // Persist to Firebase Realtime Database
                val authUser = FirebaseAuth.getInstance().currentUser
                if (authUser != null) {
                    val db = FirebaseDatabase.getInstance().reference
                    db.child("users").child(authUser.uid).child("profileImageUrl").setValue(base64Uri).await()

                    // Try Firebase Storage in background (if enabled, upgrades URL to storage downloadUrl)
                    launch(Dispatchers.IO) {
                        try {
                            val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${authUser.uid}.jpg")
                            storageRef.putFile(Uri.fromFile(avatarFile)).await()
                            val downloadUrl = storageRef.downloadUrl.await().toString()
                            db.child("users").child(authUser.uid).child("profileImageUrl").setValue(downloadUrl)
                        } catch (e: Exception) {
                            Log.d(TAG, "Storage upload skipped/fallback: ${e.message}")
                        }
                    }
                }

                onComplete(true, "Profile photo updated successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving avatar", e)
                onComplete(false, e.localizedMessage ?: "Failed to save photo")
            }
        }
    }
}

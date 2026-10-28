package com.example.lankago.utils

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.lankago.R
import com.google.android.material.button.MaterialButton

// Extension functions for standardizing UI Loading, Empty, and Content states across LankaGo.
//  Seamlessly pairs with [R.layout.layout_empty_state].

// Transitions the screen to a Loading state:
//  - Hides the main content view.
//  - Shows the empty state root with its circular progress spinner visible.
//  - Hides the empty state text and action buttons during progress.

fun View.showLoadingState(
    contentView: View? = null,
    emptyStateLayout: View? = null
) {
    contentView?.visibility = View.GONE
    val emptyRoot = emptyStateLayout ?: this.findViewById(R.id.layout_empty_state_root)
    emptyRoot?.visibility = View.VISIBLE

    val progressBar = emptyRoot?.findViewById<ProgressBar>(R.id.empty_state_progress_bar)
    val emptyContent = emptyRoot?.findViewById<View>(R.id.empty_state_content)

    progressBar?.visibility = View.VISIBLE
    emptyContent?.visibility = View.GONE
}

/**
 * Transitions the screen to a Content state:
 * - Shows the main content view (e.g. RecyclerView).
 * - Hides the entire empty state / progress spinner container.
 */
fun View.showContentState(
    contentView: View? = null,
    emptyStateLayout: View? = null
) {
    val emptyRoot = emptyStateLayout ?: this.findViewById(R.id.layout_empty_state_root)
    emptyRoot?.visibility = View.GONE
    contentView?.visibility = View.VISIBLE
}


// Transitions the screen to an Empty state ("No Data Found"):
//  - Hides the content view and loading spinner.
//  - Shows the empty state container with customizable title, subtitle, icon, and action button.

fun View.showEmptyState(
    contentView: View? = null,
    emptyStateLayout: View? = null,
    title: String = "No Data Found",
    message: String = "There is nothing to display right now.",
    iconRes: Int? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    contentView?.visibility = View.GONE
    val emptyRoot = emptyStateLayout ?: this.findViewById(R.id.layout_empty_state_root)
    emptyRoot?.visibility = View.VISIBLE

    val progressBar = emptyRoot?.findViewById<ProgressBar>(R.id.empty_state_progress_bar)
    val emptyContent = emptyRoot?.findViewById<View>(R.id.empty_state_content)
    val tvTitle = emptyRoot?.findViewById<TextView>(R.id.tv_empty_title)
    val tvMessage = emptyRoot?.findViewById<TextView>(R.id.tv_empty_message)
    val ivIcon = emptyRoot?.findViewById<ImageView>(R.id.iv_empty_icon)
    val btnAction = emptyRoot?.findViewById<MaterialButton>(R.id.btn_empty_action)

    progressBar?.visibility = View.GONE
    emptyContent?.visibility = View.VISIBLE

    tvTitle?.text = title
    tvMessage?.text = message

    if (iconRes != null && ivIcon != null) {
        ivIcon.setImageResource(iconRes)
    }

    if (btnAction != null) {
        if (!actionButtonText.isNullOrBlank() && onActionClick != null) {
            btnAction.text = actionButtonText
            btnAction.visibility = View.VISIBLE
            btnAction.setOnClickListener { onActionClick() }
        } else {
            btnAction.visibility = View.GONE
        }
    }
}

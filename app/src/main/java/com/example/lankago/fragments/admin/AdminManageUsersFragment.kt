package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminManageUsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_manage_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button
        view.findViewById<View>(R.id.btn_back_users)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Search Input listener
        val etSearch = view.findViewById<EditText>(R.id.et_search_users)
        etSearch?.setOnEditorActionListener { _, _, _ ->
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                Toast.makeText(requireContext(), "Searching for \"$query\"...", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Block / Unblock Action for User 1 (Kasun Perera)
        val btnBlock1 = view.findViewById<AppCompatButton>(R.id.btn_block_user_1)
        btnBlock1?.setOnClickListener {
            toggleUserBlockStatus(btnBlock1, "Kasun Perera")
        }

        // Block / Unblock Action for User 2 (Ananya Silva)
        val btnBlock2 = view.findViewById<AppCompatButton>(R.id.btn_block_user_2)
        btnBlock2?.setOnClickListener {
            toggleUserBlockStatus(btnBlock2, "Ananya Silva")
        }

        // Block / Unblock Action for User 3 (Dilan Jayasuriya)
        val btnBlock3 = view.findViewById<AppCompatButton>(R.id.btn_block_user_3)
        btnBlock3?.setOnClickListener {
            toggleUserBlockStatus(btnBlock3, "Dilan Jayasuriya")
        }
    }

    private fun toggleUserBlockStatus(button: AppCompatButton, userName: String) {
        val currentText = button.text.toString()
        if (currentText.equals("Block", ignoreCase = true)) {
            button.text = "Unblock"
            button.setBackgroundResource(R.drawable.bg_admin_chip_active)
            button.setTextColor(resources.getColor(R.color.white, null))
            Toast.makeText(requireContext(), "$userName has been blocked", Toast.LENGTH_SHORT).show()
        } else {
            button.text = "Block"
            button.setBackgroundResource(R.drawable.bg_button_danger_pill)
            button.setTextColor(resources.getColor(R.color.admin_danger, null))
            Toast.makeText(requireContext(), "$userName has been unblocked", Toast.LENGTH_SHORT).show()
        }
    }
}

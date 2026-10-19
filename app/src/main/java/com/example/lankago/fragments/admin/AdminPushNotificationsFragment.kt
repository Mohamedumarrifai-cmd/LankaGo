package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminPushNotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_push_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button navigation
        view.findViewById<View>(R.id.btn_back_push_notif)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Audience Dropdown (All Users, Tourists, Locals)
        val actAudience = view.findViewById<AutoCompleteTextView>(R.id.act_target_audience)
        val audiences = arrayOf("All Users", "Tourists & International", "Local Travelers", "Active Bookers")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, audiences)
        actAudience?.setAdapter(adapter)

        val etTitle = view.findViewById<EditText>(R.id.et_notification_title)
        val etMessage = view.findViewById<EditText>(R.id.et_notification_message)
        val btnSend = view.findViewById<AppCompatButton>(R.id.btn_send_notification)

        btnSend?.setOnClickListener {
            val title = etTitle?.text?.toString()?.trim() ?: ""
            val message = etMessage?.text?.toString()?.trim() ?: ""
            val target = actAudience?.text?.toString() ?: "All Users"

            if (title.isEmpty()) {
                etTitle?.error = "Please enter notification title"
                etTitle?.requestFocus()
                return@setOnClickListener
            }
            if (message.isEmpty()) {
                etMessage?.error = "Please enter notification message"
                etMessage?.requestFocus()
                return@setOnClickListener
            }

            // Simulate broadcasting notification to Firebase Cloud Messaging (FCM)
            Toast.makeText(
                requireContext(),
                "Broadcast sent successfully to $target!",
                Toast.LENGTH_LONG
            ).show()

            findNavController().navigateUp()
        }
    }
}

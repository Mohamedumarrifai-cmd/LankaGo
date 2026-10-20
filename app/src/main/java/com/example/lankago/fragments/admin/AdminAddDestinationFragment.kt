package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminAddDestinationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_add_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val etName = view.findViewById<EditText>(R.id.et_dest_name)
        val btnPublish = view.findViewById<AppCompatButton>(R.id.btn_publish_destination)

        btnPublish?.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            if (name.isEmpty()) {
                etName?.error = "Please enter destination name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Destination published successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}

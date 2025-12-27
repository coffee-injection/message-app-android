package com.coffeeinjection.message.presentation.dialog

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.coffeeinjection.message.databinding.DialogFragmentPermissionBinding

class PermissionDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentPermissionBinding? = null
    val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                dismiss()
            } else {
                // todo dialog 띄우기
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
                dismiss()
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFragmentPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        isCancelable = false
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding) {
        btnStart.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@setOnClickListener
            val permission = Manifest.permission.POST_NOTIFICATIONS

            requestNotificationPermissionLauncher.launch(permission)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

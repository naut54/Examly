package com.octal.examly.presentation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R

class ConfirmationDialogFragment : DialogFragment() {

    private var onConfirmListener: ((Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val message = arguments?.getString(ARG_MESSAGE) ?: ""
        val iconRes = arguments?.getInt(ARG_ICON, 0) ?: 0
        val positiveText = arguments?.getString(ARG_POSITIVE_TEXT)
            ?: getString(R.string.ok)
        val negativeText = arguments?.getString(ARG_NEGATIVE_TEXT)
            ?: getString(R.string.cancel)
        val isCancelable = arguments?.getBoolean(ARG_CANCELABLE, true) ?: true

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ ->
                onConfirmListener?.invoke(true)
                dismiss()
            }
            .setNegativeButton(negativeText) { _, _ ->
                onConfirmListener?.invoke(false)
                dismiss()
            }
            .setCancelable(isCancelable)

        if (iconRes != 0) {
            builder.setIcon(iconRes)
        }

        return builder.create()
    }

    override fun onDestroy() {
        super.onDestroy()
        onConfirmListener = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_ICON = "icon"
        private const val ARG_POSITIVE_TEXT = "positive_text"
        private const val ARG_NEGATIVE_TEXT = "negative_text"
        private const val ARG_CANCELABLE = "cancelable"

        fun newInstance(
            title: String,
            message: String,
            @DrawableRes icon: Int = 0,
            positiveButtonText: String? = null,
            negativeButtonText: String? = null,
            isCancelable: Boolean = true,
            onConfirm: (Boolean) -> Unit
        ): ConfirmationDialogFragment {
            return ConfirmationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putInt(ARG_ICON, icon)
                    positiveButtonText?.let { putString(ARG_POSITIVE_TEXT, it) }
                    negativeButtonText?.let { putString(ARG_NEGATIVE_TEXT, it) }
                    putBoolean(ARG_CANCELABLE, isCancelable)
                }
                this.onConfirmListener = onConfirm
            }
        }

        fun newDeleteConfirmation(
            itemName: String,
            onConfirm: (Boolean) -> Unit
        ): ConfirmationDialogFragment {
            return newInstance(
                title = "Eliminar $itemName",
                message = "¿Estás seguro de que quieres eliminar este $itemName? Esta acción no se puede deshacer.",
                icon = R.drawable.ic_delete,
                positiveButtonText = "Eliminar",
                negativeButtonText = "Cancelar",
                onConfirm = onConfirm
            )
        }

        fun newExitConfirmation(
            onConfirm: (Boolean) -> Unit
        ): ConfirmationDialogFragment {
            return newInstance(
                title = "Salir",
                message = "¿Estás seguro de que quieres salir? Los cambios no guardados se perderán.",
                icon = R.drawable.ic_warning,
                positiveButtonText = "Salir",
                negativeButtonText = "Cancelar",
                onConfirm = onConfirm
            )
        }

        fun newLogoutConfirmation(
            onConfirm: (Boolean) -> Unit
        ): ConfirmationDialogFragment {
            return newInstance(
                title = "Cerrar sesión",
                message = "¿Estás seguro de que quieres cerrar sesión?",
                icon = R.drawable.ic_logout,
                positiveButtonText = "Cerrar sesión",
                negativeButtonText = "Cancelar",
                onConfirm = onConfirm
            )
        }
    }
}

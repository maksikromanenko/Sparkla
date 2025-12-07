package com.example.sparkla.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sparkla.LoginActivity
import com.example.sparkla.R
import com.example.sparkla.api.*
import com.example.sparkla.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etAddressLine: EditText
    private lateinit var etCity: EditText
    private lateinit var etPostalCode: EditText
    private lateinit var btnEdit: ImageButton
    private lateinit var btnSave: ImageButton
    private var currentUserData: UserData? = null
    private var currentAddress: Address? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        etName = view.findViewById(R.id.et_name)
        etSurname = view.findViewById(R.id.et_surname)
        etAddressLine = view.findViewById(R.id.et_address_line)
        etCity = view.findViewById(R.id.et_city)
        etPostalCode = view.findViewById(R.id.et_postal_code)

        btnEdit = view.findViewById(R.id.btn_edit)
        btnSave = view.findViewById(R.id.btn_save)

        loadUserProfile()
        loadUserAddress()

        btnEdit.setOnClickListener {
            enableEditing(true)
        }

        btnSave.setOnClickListener {
            saveChanges()
        }

        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        val btnChangePassword = view.findViewById<Button>(R.id.btn_change_password)
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        return view
    }

    private fun enableEditing(enabled: Boolean) {
        etName.isEnabled = enabled
        etSurname.isEnabled = enabled
        etAddressLine.isEnabled = enabled
        etCity.isEnabled = enabled
        etPostalCode.isEnabled = enabled

        if (enabled) {
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
            etName.requestFocus()
        } else {
            btnEdit.visibility = View.VISIBLE
            btnSave.visibility = View.GONE
        }
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val response = ApiClient.instance.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    currentUserData = response.body()
                    etName.setText(currentUserData?.firstName)
                    etSurname.setText(currentUserData?.lastName)
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить профиль", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserAddress() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()
            if (token.isNullOrBlank()) {
                return@launch
            }

            try {
                val response = ApiClient.instance.getAddress("Bearer $token")
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    currentAddress = response.body()?.first()
                    etAddressLine.setText(currentAddress?.addressLine)
                    etCity.setText(currentAddress?.city)
                    etPostalCode.setText(currentAddress?.postalCode)
                }
            } catch (t: Throwable) {

            }
        }
    }

    private fun saveChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Update profile
            val newName = etName.text.toString()
            val newSurname = etSurname.text.toString()
            val updateProfileRequest = UpdateProfileRequest(firstName = newName, lastName = newSurname)
            var profileUpdateSuccess = false
            try {
                val profileResponse = ApiClient.instance.updateProfile("Bearer $token", updateProfileRequest)
                if (profileResponse.isSuccessful) {
                    profileUpdateSuccess = true
                    currentUserData = currentUserData?.copy(firstName = newName, lastName = newSurname)
                } else {
                    Toast.makeText(requireContext(), "Ошибка обновления профиля", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }

            // Update or create address
            val newAddressLine = etAddressLine.text.toString()
            val newCity = etCity.text.toString()
            val newPostalCode = etPostalCode.text.toString()
            val addressRequest = UpdateAddressRequest(
                addressLine = newAddressLine,
                city = newCity,
                postalCode = newPostalCode
            )
            var addressUpdateSuccess = false
            try {
                if (currentAddress != null) {
                    // Update existing address
                    val addressResponse = ApiClient.instance.updateAddress("Bearer $token", currentAddress!!.id, addressRequest)
                    if (addressResponse.isSuccessful) {
                        addressUpdateSuccess = true
                        currentAddress = currentAddress?.copy(
                            addressLine = newAddressLine,
                            city = newCity,
                            postalCode = newPostalCode
                        )
                    }
                } else {
                    // Create new address
                    val addressResponse = ApiClient.instance.createAddress("Bearer $token", addressRequest)
                    if (addressResponse.isSuccessful) {
                        addressUpdateSuccess = true
                        currentAddress = addressResponse.body() // Update currentAddress with the newly created one
                    }
                }

                if (!addressUpdateSuccess) {
                    Toast.makeText(requireContext(), "Ошибка обновления адреса", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }


            if (profileUpdateSuccess && addressUpdateSuccess) {
                Toast.makeText(requireContext(), "Данные обновлены", Toast.LENGTH_SHORT).show()
                enableEditing(false)
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = dialogView.findViewById<EditText>(R.id.et_confirm_new_password)
        val btnDialogCancel = dialogView.findViewById<Button>(R.id.btn_dialog_cancel)
        val btnDialogSave = dialogView.findViewById<Button>(R.id.btn_dialog_save)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)
            .create()

        btnDialogSave.setOnClickListener {
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmNewPassword = etConfirmNewPassword.text.toString()
            handleChangePassword(oldPassword, newPassword, confirmNewPassword)
            dialog.dismiss()
        }

        btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleChangePassword(old: String, new: String, confirm: String) {
        if (new != confirm) {
            Toast.makeText(requireContext(), "Новые пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()).first()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val request = ChangePasswordRequest(old, new, confirm)
            try {
                val response = ApiClient.instance.changePassword("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Не удалось изменить пароль", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext(), R.style.PurpleDialog)
            .setTitle("Выход")
            .setMessage("Вы действительно хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                logout()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            TokenManager.clearTokens(requireContext())
        }

        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
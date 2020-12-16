package com.chat.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import com.chat.R
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LoginActivity : BaseActivity(), Callback<User> {
    private var isUpdate = false
    private var isLogin = true
    private var imageUri: Uri? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        isUpdate = intent.getBooleanExtra("isUpdate", false)

        if (isUpdate) {
            edtName.visibility = View.VISIBLE
            edtPhone.visibility = View.VISIBLE
            edtConfirmPassword.visibility = View.VISIBLE
            btnLogin.text = getString(R.string.save)
            tvSignUp.visibility = View.GONE
            user = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).apply {
                edtName.setText(name)
                edtEmail.setText(email)
                edtPhone.setText(phone)
                edtPassword.setText(password)
                edtConfirmPassword.setText(password)
                if (image != null) {
                    Picasso.get().load(Constants.BASE_URL + image)
                        .resize(400, 400).centerCrop().placeholder(R.drawable.ic_app).into(imvLogo)
                }
            }
            imvLogo.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                    }
                } else {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 0)
                }
            }
        } else {
            tvSignUp.setOnClickListener {
                isLogin = !isLogin
                if (isLogin) {
                    btnLogin.text = getString(R.string.login)
                    tvSignUp.text = getString(R.string.register)
                    edtName.visibility = View.GONE
                    edtConfirmPassword.visibility = View.GONE
                    isLogin = true
                } else {
                    btnLogin.text = getString(R.string.register)
                    tvSignUp.text = getString(R.string.login)
                    edtName.visibility = View.VISIBLE
                    edtConfirmPassword.visibility = View.VISIBLE
                    isLogin = false
                }
            }
        }

        btnLogin.setOnClickListener {
            if (!isValid()) {
                return@setOnClickListener
            }
            showLoading(true)
            when {
                isUpdate -> {
                    var imageFile: MultipartBody.Part? = null
                    imageUri?.getRealPath()?.let {
                        val mediaType = RequestBody.create(MediaType.parse("image/*"), it)
                        imageFile = MultipartBody.Part.createFormData("image", it.name, mediaType)
                    }
                    val mediaType = MediaType.parse("text/plain")
                    Utility.apiClient.updateProfile(
                        RequestBody.create(mediaType, user?.id.toString()),
                        RequestBody.create(mediaType, edtName.text.toString().trim()),
                        RequestBody.create(mediaType, edtPhone.text.toString().trim()),
                        RequestBody.create(mediaType, edtEmail.text.toString().trim()),
                        RequestBody.create(mediaType, edtPassword.text.toString().trim()),
                        imageFile
                    ).enqueue(this)
                }
                isLogin -> {
                    Utility.apiClient.login(edtEmail.text.toString(), edtPassword.text.toString()).enqueue(this)
                }
                else -> {
                    Utility.apiClient.signUp(edtEmail.text.toString(), edtPassword.text.toString(), edtName.text.toString()).enqueue(this)
                }
            }
        }
    }

    override fun onResponse(call: Call<User>, response: Response<User>) {
        if (response.isSuccessful) {
            response.body()?.let {
                Utility.sharedPreferences.edit()
                    .putString(Constants.PREF_USER_NAME, it.email)
                    .putString(Constants.PREF_PASSWORD, it.password)
                    .putString(Constants.PREF_USER, Gson().toJson(it))
                    .apply()
                startActivity(
                    Intent(this@LoginActivity, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        } else {
            showAlert(response.errorBody()?.string())
        }
        showLoading(false)
    }

    override fun onFailure(call: Call<User>, t: Throwable) {
        showAlert(t)
        showLoading(false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 0) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            imvLogo.setImageURI(data.data)
            imageUri = data.data
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun Uri?.getRealPath(): File? {
        if (this != null) {
            val data = MediaStore.Images.Media.DATA
            contentResolver.query(this, arrayOf(data), null, null, null)?.apply {
                moveToFirst()
                return File(getString(getColumnIndexOrThrow(data)))
            }
        }
        return null
    }

    private fun isValid(): Boolean {
        var b = true
        if (edtEmail.text.toString().isBlank()) {
            edtEmail.error = getString(R.string.tv_please_enter_email)
            b = false
        }
        if (edtPassword.text.toString().isBlank()) {
            edtPassword.error = getString(R.string.tv_please_enter_password)
            b = false
        }
        if (isUpdate || !isLogin) {
            if (edtName.text.isBlank()) {
                edtName.error = getString(R.string.tv_please_enter_your_name)
                b = false
            }
            if (edtPassword.text.toString().trim() != edtConfirmPassword.text.toString().trim()) {
                edtConfirmPassword.error = getString(R.string.tv_invalid_confirm_password)
                b = false
            }
        }
        return b
    }
}
package styli.android.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import styli.android.R
import styli.android.api.HttpClient
import styli.android.api.dto.auth.RegisterForm
import styli.android.databinding.ActivitySignUpBinding
import styli.android.global.Constants
import styli.android.utils.Validators.Companion.isEmailValid
import styli.android.utils.Validators.Companion.isNameValid
import styli.android.utils.Validators.Companion.isPasswordValid
import styli.android.utils.Validators.Companion.isSurnameValid
import styli.android.utils.Validators.Companion.isUsernameValid
import java.io.IOException

class SignUpActivity : BaseActivity<ActivitySignUpBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpActionBarForReturnAction(
            binding.toolbarSignUpActivity,
            icon = R.drawable.back_arrow_black
        )
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    override fun getViewBinding() = ActivitySignUpBinding.inflate(layoutInflater)

    private fun registerUser() {
        val username: String = binding.etUsername.text.toString().trim()
        val email: String = binding.etEmail.text.toString().trim()
        val name: String = binding.etName.text.toString().trim()
        val surname: String = binding.etSurname.text.toString().trim()
        val password1: String = binding.etPassword1.text.toString().trim()
        val password2: String = binding.etPassword2.text.toString().trim()

        if (validateForm(username, email, name, surname, password1, password2)) {
            showProgressDialog()
            lifecycleScope.launch {
                val response = try {
                    HttpClient.api?.register(RegisterForm(username, email, password1, name, surname))
                } catch (e: IOException) {
                    Log.e(TAG, "IOException, possible lack of Internet connection. ${e.message}")
                    hideProgressDialog()
                    return@launch
                } catch (e: HttpException) {
                    Log.e(TAG, "HttpException, unexpected response. ${e.message}")
                    hideProgressDialog()
                    return@launch
                }
                if (response?.isSuccessful == true && response.body() != null) {
                    val loginIntent = Intent(this@SignUpActivity, SignInActivity::class.java)
                    loginIntent.putExtra(
                        Constants.Activity.REGISTRATION_SUCCESS,
                        "Registration successful"
                    )
                    startActivity(loginIntent)
                    finish()
                } else {
                    hideProgressDialog()
                    showErrorSnackBar(R.string.registration_unsuccessful)
                }
            }
        }
    }

    private fun validateForm(
        username: String,
        email: String,
        name: String,
        surname: String,
        password1: String,
        password2: String
    ): Boolean {
        return when {
            !username.isUsernameValid() -> {
                showErrorSnackBar(R.string.sign_up_username_error)
                false
            }
            !email.isEmailValid() -> {
                showErrorSnackBar(R.string.sign_up_email_error)
                false
            }
            !name.isNameValid() -> {
                showErrorSnackBar(R.string.sign_up_name_error)
                false
            }
            !surname.isSurnameValid() -> {
                showErrorSnackBar(R.string.sign_up_surname_error)
                false
            }
            !password1.isPasswordValid() -> {
                showErrorSnackBar(R.string.sign_up_password1_error)
                false
            }
            !password2.isPasswordValid() -> {
                showErrorSnackBar(R.string.sign_up_password2_error)
                false
            }
            else -> {
                if (password1 == password2) {
                    true
                } else {
                    showErrorSnackBar(R.string.sign_up_passwords_different_error)
                    false
                }
            }
        }
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}
package com.renote.renoteai.ui.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.renote.renoteai.ui.main.MainActivity
import com.renote.renoteai.R
import com.renote.renoteai.ui.activities.signup.SignUpActivity

import com.renote.renoteai.databinding.RegistrationDataBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.drive.Drive
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.koin.android.ext.android.inject

class RegistrationActivity : AppCompatActivity() {

    companion object {
        const val CONST_SIGN_IN = 34
    }

    val viewmodel: RegistrationViewModel by inject()
    lateinit var binding: RegistrationDataBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleAuth: GoogleSignInClient

    // private lateinit var txtCompany: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@RegistrationActivity, R.layout.activity_registration
        )
        binding.lifecycleOwner = this
        binding.viewmodel = viewmodel
        auth = FirebaseAuth.getInstance()


        val spannable1 = SpannableStringBuilder(getString(R.string.company_name))
        spannable1.setSpan(
            ForegroundColorSpan(getColor(R.color.green)), 0, 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
//        binding.txtCompany.text = spannable1

        val spannable2 = SpannableStringBuilder(getString(R.string.already_have_an_account))
        spannable2.setSpan(
            ForegroundColorSpan(getColor(R.color.green)), 25, 32, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        binding.txtAlreadySignIn.text = spannable2


        //initGoogleSignIn()
        observeData()
    }

//    fun initGoogleSignIn(){
//        val gso = GoogleSignInOptions
//            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
//            .requestIdToken(getString(R.string.clientid))
//            .requestEmail()
//            .build()
//        googleAuth=   GoogleSignIn.getClient(this@RegistrationActivity,gso)
//    }

    fun observeData() {
        viewmodel.resourseClick.observe(this) { integer ->
            when (integer) {
                R.id.btnSignUp -> {
                    startActivity(Intent(this@RegistrationActivity, SignUpActivity::class.java))
                }

                R.id.btnSignIn -> {
                    //startActivity(Intent(this@RegistrationActivity, SignInActivity::class.java))
                    googleSignIn()
                }

                R.id.signInWithGoogleAcoountLL -> {
                    googleSignIn()
                }

            }
        }
    }

    private fun googleSignIn() {
        //  val account = GoogleSignIn.getLastSignedInAccount(this@RegistrationActivity)
//        if(account == null){
//            val signInIntent = googleAuth.signInIntent
//            startActivityForResult(signInIntent, CONST_SIGN_IN)
//        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, CONST_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONST_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account.idToken!!)


            } catch (e: ApiException) {
                Toast.makeText(this@RegistrationActivity, "${e}", Toast.LENGTH_LONG).show()
                Log.d("Exception", "$e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, OnCompleteListener<AuthResult?> { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("sign in success", "signInWithCredential:success")
                    val user = auth.currentUser
                    startActivity(Intent(this@RegistrationActivity, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("sign in fail", "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                }
            })

    }


}
package it.polito.maddroid.lab3.common;


import android.animation.LayoutTransition;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    
    // references to view and view groups
    private RelativeLayout rlLoginTitle;
    private RelativeLayout rlLoginContent;
    private RelativeLayout rlSignupTitle;
    private RelativeLayout rlSignupContent;
    
    private ImageView ivLoginArrow;
    private ImageView ivSignupArrow;
    
    private CardView cvLogin;
    private CardView cvSignup;
    
    private FloatingActionButton fabSumbit;
    
    private EditText etMailSignup;
    private EditText etPassSignup;
    private EditText etRePassSignup;
    
    private EditText etMailLogin;
    private EditText etPassLogin;
    
    private ProgressBar pbLogin;
    
    // Firebase auth to manage signup and login
    private FirebaseAuth mAuth;
    
    // general purpose variables
    boolean loginContentVisible = false;
    boolean signupContentVisible = false;
    
    private static final String LOGIN_VISIBLE_KEY = "LOGIN_VISIBLE_KEY";
    private static final String SIGNUP_VISIBLE_KEY = "SIGNUP_VISIBLE_KEY";
    private static final String LOGIN_MAIL_KEY = "LOGIN_MAIL_KEY";
    private static final String LOGIN_PASS_KEY = "LOGIN_PASS_KEY";
    private static final String SIGNUP_MAIL_KEY = "SIGNUP_MAIL_KEY";
    private static final String SIGNUP_PASS_KEY = "SIGNUP_PASS_KEY";
    private static final String SIGNUP_REPASS_KEY = "SIGNUP_REPASS_KEY";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    
        // enable animation in layout changes even for layout resize
        ((ViewGroup) findViewById(R.id.clLoginMainContainer)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    
        
        // get references to view elements
        getReferencesToViews();
        
        // setup onclicks
        setupOnclicks();
        
        // setup firebase authentication
        mAuth = FirebaseAuth.getInstance();
        
        // check if user is cuurrently signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Snackbar.make(rlLoginTitle,"User already logged in", Snackbar.LENGTH_SHORT).show();
            enterApp();
        } else {
            Snackbar.make(rlLoginTitle,"You need to login to use the app", Snackbar.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);super.onSaveInstanceState(outState);
        
        outState.putBoolean(LOGIN_VISIBLE_KEY, loginContentVisible);
        outState.putBoolean(SIGNUP_VISIBLE_KEY, signupContentVisible);
        
        String loginMail = etMailLogin.getText().toString();
        if (!loginMail.isEmpty())
            outState.putString(LOGIN_MAIL_KEY, loginMail);
        
        String loginPass = etPassLogin.getText().toString();
        if (!loginPass.isEmpty())
            outState.putString(LOGIN_PASS_KEY, loginPass);
        
        String signupMail = etMailSignup.getText().toString();
        if (!signupMail.isEmpty())
            outState.putString(SIGNUP_MAIL_KEY, signupMail);
    
        String signupPass = etPassSignup.getText().toString();
        if (!signupPass.isEmpty())
            outState.putString(SIGNUP_PASS_KEY, signupPass);
    
        String signupRePass = etRePassSignup.getText().toString();
        if (!signupRePass.isEmpty())
            outState.putString(SIGNUP_REPASS_KEY, signupRePass);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        loginContentVisible = savedInstanceState.getBoolean(LOGIN_VISIBLE_KEY);
        signupContentVisible = savedInstanceState.getBoolean(SIGNUP_VISIBLE_KEY);
        
        if (loginContentVisible)
            setLoginFormVisible(true);
        if (signupContentVisible)
            setSignupFormVisible(true);
        
        String loginMail = savedInstanceState.getString(LOGIN_MAIL_KEY, "");
        if (!loginMail.isEmpty())
            etMailLogin.setText(loginMail);
        
        String loginPass = savedInstanceState.getString(LOGIN_PASS_KEY, "");
        if (!loginPass.isEmpty())
            etPassLogin.setText(loginPass);
    
        String signupMail = savedInstanceState.getString(SIGNUP_MAIL_KEY, "");
        if (!signupMail.isEmpty())
            etMailSignup.setText(signupMail);
    
        String signupPass = savedInstanceState.getString(SIGNUP_PASS_KEY, "");
        if (!signupPass.isEmpty())
            etPassSignup.setText(signupPass);
    
        String signupRePass = savedInstanceState.getString(SIGNUP_REPASS_KEY, "");
        if (!signupRePass.isEmpty())
            etRePassSignup.setText(signupRePass);
    }
    
    private void getReferencesToViews() {
        rlLoginContent = findViewById(R.id.rlLoginContentContainer);
        rlLoginTitle = findViewById(R.id.rlLoginTitleContainer);
        rlSignupContent = findViewById(R.id.rlSignupContentContainer);
        rlSignupTitle = findViewById(R.id.rlSignupTitleContainer);
        
        cvLogin = findViewById(R.id.cvLoginContainer);
        cvSignup = findViewById(R.id.cvSignupContainer);
        
        fabSumbit = findViewById(R.id.fabSubmit);
        ivLoginArrow = findViewById(R.id.ivLoginArrow);
        ivSignupArrow = findViewById(R.id.ivSignupArrow);
        
        etMailSignup = findViewById(R.id.etMailSignup);
        etPassSignup = findViewById(R.id.etPassSignup);
        etRePassSignup = findViewById(R.id.etRePassSignup);
        
        etMailLogin = findViewById(R.id.etMailLogin);
        etPassLogin = findViewById(R.id.etPassLogin);
        
        pbLogin = findViewById(R.id.pb_login);
    }
    
    private void setupOnclicks() {
        
        rlLoginTitle.setOnClickListener(v -> {
            
            setLoginFormVisible(!loginContentVisible);
            
            // toggle boolean
            loginContentVisible = !loginContentVisible;
            
            //close signup form
            if (signupContentVisible) {
                setSignupFormVisible(false);
                signupContentVisible = false;
            }
        });
        
        rlSignupTitle.setOnClickListener(v -> {
            setSignupFormVisible(!signupContentVisible);
            
            // toggle boolean
            signupContentVisible = !signupContentVisible;
    
            //close login form
            if (loginContentVisible) {
                setLoginFormVisible(false);
                loginContentVisible = false;
            }
        });
        
        fabSumbit.setOnClickListener(v -> {
            if (loginContentVisible) {
                manageLoginClick();
            } else if (signupContentVisible) {
                manageSignupClick();
            }
        });
    }
    
    private void setLoginFormVisible(boolean visible) {
        // also hide signup form and show fab
        if (visible) {
            rlLoginContent.setVisibility(View.VISIBLE);
            cvSignup.setVisibility(View.GONE);
            fabSumbit.show();
            
            //animate rotation of arrow
            rotateArrow(ivLoginArrow, true);
            
        } else {
            rlLoginContent.setVisibility(View.GONE);
            cvSignup.setVisibility(View.VISIBLE);
            fabSumbit.hide();
    
            //animate rotation of arrow
            rotateArrow(ivLoginArrow, false);
        }
    }
    
    private void setSignupFormVisible(boolean visible) {
        // also hide login form and show fab
        if (visible) {
            rlSignupContent.setVisibility(View.VISIBLE);
            cvLogin.setVisibility(View.GONE);
            fabSumbit.show();
    
            //animate rotation of arrow
            rotateArrow(ivSignupArrow, true);
        } else {
            rlSignupContent.setVisibility(View.GONE);
            cvLogin.setVisibility(View.VISIBLE);
            fabSumbit.hide();
    
            //animate rotation of arrow
            rotateArrow(ivSignupArrow, false);
        }
    }
    
    private void rotateArrow(View view, boolean forward) {
        if (forward)
            view.animate().rotationBy(90.0f).start();
        else
            view.animate().rotationBy(-90.0f).start();
    }
    
    private void manageSignupClick() {
        
        String email = etMailSignup.getText().toString();
        String pass = etPassSignup.getText().toString();
        String rePass = etRePassSignup.getText().toString();
        
        if (email.isEmpty() || pass.isEmpty() || rePass.isEmpty()) {
            Snackbar.make(rlLoginTitle, "You need to fill all the fields to continue", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        if (!validateEmail(email)) {
            Snackbar.make(rlLoginTitle, "The inserted email is not valid", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        if (!pass.equals(rePass)) {
            Snackbar.make(rlLoginTitle, "The two passwords inserted are different", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        if (!validatePassword(pass)) {
            showPasswordInvalidError();
            return;
        }
        
        // Everything ok
        
        setSignupFormVisible(false);
        signupContentVisible = false;
        pbLogin.setVisibility(View.VISIBLE);
        
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    pbLogin.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Snackbar.make(rlLoginTitle, "Successfully signed up", Snackbar.LENGTH_SHORT).show();
                            enterApp();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                        Snackbar.make(rlLoginTitle, "Error during signup", Snackbar.LENGTH_SHORT).show();
                        
                    }

                });
    }
    
    private void manageLoginClick() {
        
        String email = etMailLogin.getText().toString();
        
        String pass = etPassLogin.getText().toString();
    
        if (email.isEmpty() || pass.isEmpty()) {
            Snackbar.make(rlLoginTitle, "You need to fill all the fields to continue", Snackbar.LENGTH_SHORT).show();
            return;
        }
    
        setLoginFormVisible(false);
        loginContentVisible = false;
        pbLogin.setVisibility(View.VISIBLE);
    
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    pbLogin.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Snackbar.make(rlLoginTitle, "Successfully logged in", Snackbar.LENGTH_SHORT).show();
                            enterApp();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithEmail:failure", task.getException());
                        Snackbar.make(rlLoginTitle, "Error during login", Snackbar.LENGTH_SHORT).show();
                    }
                
                    // ...
                });
    }
    
    private boolean validateEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
        Pattern pattern;
        Matcher matcher;
    
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    
    }
    
    private boolean validatePassword(String password) {
        /**
         * Rules for a good password:
         * - at least 8 chars (20 max);
         * - at least one digit, one lowercase and one uppercase
         */
        
        String PASSWORD_PATTERN =
                "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20})";
        Pattern pattern;
        Matcher matcher;
    
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
    
    private void showPasswordInvalidError() {
        String msg = "The password must be at least 6 chars long (maximum 20) and needs to have at least one digit, one lowercase and one uppercase char";
        Snackbar.make(rlLoginTitle, msg, Snackbar.LENGTH_LONG).show();
    }
    
    private void enterApp() {
        Intent i = new Intent(getApplicationContext(), SplashScreenActivity.class);
        startActivity(i);
    }
    
}

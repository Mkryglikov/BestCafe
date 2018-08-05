package mkruglikov.bestcafe;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    public static final int SIGN_UP_ACTIVITY_REQUEST_CODE = 15;
    private static final int GOOGLE_SIGN_UP_ACTIVITY_REQUEST_CODE = 16;
    private FirebaseAuth firebaseAuth;
    private EditText etEmailSignUp, etPasswordSignUp;
    private GoogleSignInClient googleSignInClient;
    private TextInputLayout tilEmailSignUp, tilPasswordSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GoogleSignInClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        tilEmailSignUp = findViewById(R.id.tilEmailSignUp);
        tilPasswordSignUp = findViewById(R.id.tilPasswordSignUp);

        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);

        etEmailSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkUserEmail(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etPasswordSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkUserPassword(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button btnSubmitSignUp = findViewById(R.id.btnSubmitSignUp);
        btnSubmitSignUp.setOnClickListener(view -> addUser(etEmailSignUp.getText().toString(), etPasswordSignUp.getText().toString()));

        etPasswordSignUp.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addUser(etEmailSignUp.getText().toString(), etPasswordSignUp.getText().toString());
            }
            return false;
        });

        SignInButton btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        btnGoogleSignUp.setOnClickListener(view -> {
            Intent signUpIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signUpIntent, GOOGLE_SIGN_UP_ACTIVITY_REQUEST_CODE);
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_UP_ACTIVITY_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                firebaseAuthWithGoogle(task.getResult(ApiException.class));
            } catch (ApiException e) {
                Toast.makeText(this, getString(R.string.google_login_error_message), Toast.LENGTH_LONG).show();
                Log.w(MainActivity.TAG, "firebaseAuthWithGoogle: " + e.getLocalizedMessage());
            }
        }
    }

    private void addUser(String email, String password) {
        if (checkUserEmail(email) && checkUserPassword(password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    updateWidget();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, getString(R.string.authentication_error_message), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkUserEmail(String email) {
        boolean isValid = true;

        if (email.isEmpty()) {
            tilEmailSignUp.setError(getString(R.string.required_field_hint));
            isValid = false;
        } else if (!email.matches("^([a-zA-Z0-9_.-]+)@([a-zA-Z0-9_-]+)\\.([a-zA-Z]{2,6})$")) {
            tilEmailSignUp.setError(getString(R.string.not_an_email_error_hint));
            isValid = false;
        } else {
            tilEmailSignUp.setError("");
        }
        return isValid;
    }

    private boolean checkUserPassword(String password) {
        boolean isValid = true;

        if (password.isEmpty()) {
            tilPasswordSignUp.setError(getString(R.string.required_field_hint));
            isValid = false;
        } else if (password.length() < 8) {
            tilPasswordSignUp.setError(getString(R.string.password_8_characters_hint));
            isValid = false;
        } else if (!password.matches("[A-Za-z0-9]+")) {
            tilPasswordSignUp.setError(getString(R.string.password_wrong_symbols_hint));
            isValid = false;
        } else {
            tilPasswordSignUp.setError("");
        }

        return isValid;
    }

    private void updateWidget() {
        Intent intent = new Intent(this, Widget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), Widget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                updateWidget();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.google_login_error_message), Toast.LENGTH_LONG).show();
                Log.w(MainActivity.TAG, "Firebase Auth With Google failed: " + task.getException().getLocalizedMessage());
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}

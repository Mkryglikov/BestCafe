package mkruglikov.bestcafe;

import android.app.Activity;
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

public class SignInActivity extends AppCompatActivity {

    public static final int SIGN_IN_ACTIVITY_REQUEST_CODE = 13;
    public static final int GOOGLE_SIGN_IN_ACTIVITY_REQUEST_CODE = 14;

    private FirebaseAuth firebaseAuth;
    private EditText etEmailSignIn, etPasswordSignIn;
    private GoogleSignInClient googleSignInClient;
    private TextInputLayout tilEmailSignIn, tilPasswordSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GoogleSignInClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        etEmailSignIn = findViewById(R.id.etEmailSignIn);
        etPasswordSignIn = findViewById(R.id.etPasswordSignIn);

        etEmailSignIn.addTextChangedListener(new TextWatcher() {
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

        etPasswordSignIn.addTextChangedListener(new TextWatcher() {
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

        tilEmailSignIn = findViewById(R.id.tilEmailSignIn);
        tilPasswordSignIn = findViewById(R.id.tilPasswordSignIn);

        Button btnSubmitSignIn = findViewById(R.id.btnSubmitSignIn);
        btnSubmitSignIn.setOnClickListener(view -> signInUser(etEmailSignIn.getText().toString(), etPasswordSignIn.getText().toString()));

        etPasswordSignIn.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signInUser(etEmailSignIn.getText().toString(), etPasswordSignIn.getText().toString());
            }
            return false;
        });

        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_ACTIVITY_REQUEST_CODE);
        });
    }

    protected void signInUser(String email, String password) {
        if (checkUserEmail(email) && checkUserPassword(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    updateWidget();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_ACTIVITY_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                firebaseAuthWithGoogle(task.getResult(ApiException.class));

            } catch (ApiException e) {
                Toast.makeText(this, "Error login with Google", Toast.LENGTH_SHORT).show();
                Log.w(MainActivity.TAG, "firebaseAuthWithGoogle: " + e.getLocalizedMessage());
            }

        }
    }

    protected boolean checkUserEmail(String email) {
        boolean isValid = true;

        if (email.isEmpty()) {
            tilEmailSignIn.setError("Required");
            isValid = false;
        } else if (!email.matches("^([a-zA-Z0-9_.-]+)@([a-zA-Z0-9_-]+)\\.([a-zA-Z]{2,6})$")) {
            tilEmailSignIn.setError("Isn't Email address");
            isValid = false;
        } else {
            tilEmailSignIn.setError("");
        }
        return isValid;
    }

    protected boolean checkUserPassword(String password) {
        boolean isValid = true;

        if (password.isEmpty()) {
            tilPasswordSignIn.setError("Required");
            isValid = false;
        } else if (password.length() < 8) {
            tilPasswordSignIn.setError("At least 8 characters");
            isValid = false;
        } else if (!password.matches("[A-Za-z0-9]+")) {
            tilPasswordSignIn.setError("Password can contain only letters and numbers");
            isValid = false;
        } else {
            tilPasswordSignIn.setError("");
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
                Toast.makeText(this, "Login with Google failed", Toast.LENGTH_LONG).show();
                Log.w(MainActivity.TAG, "Firebase Auth With Google failed: " + task.getException().getLocalizedMessage());
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}

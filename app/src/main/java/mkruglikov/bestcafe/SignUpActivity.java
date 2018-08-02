package mkruglikov.bestcafe;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);

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
                //TODO
                Log.w(MainActivity.TAG, "firebaseAuthWithGoogle: " + e.getLocalizedMessage());
            }
        }
    }

    protected void addUser(String email, String password) {
        if (checkUserEmailPassword(email, password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    updateWidget();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //TODO
            Toast.makeText(SignUpActivity.this, "Info isn't correct", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean checkUserEmailPassword(String email, String password) {
        //TODO
        return (!email.isEmpty() && !password.isEmpty());
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
                //TODO
                Log.w(MainActivity.TAG, "Firebase Auth With Google failed: " + task.getException().getLocalizedMessage());
                Toast.makeText(this, "Authorization error", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}

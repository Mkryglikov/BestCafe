package mkruglikov.bestcafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    public static final int SIGN_IN_ACTIVITY_REQUEST_CODE = 13;
    public static final int GOOGLE_SIGN_IN_ACTIVITY_REQUEST_CODE = 14;

    private FirebaseAuth firebaseAuth;
    private EditText etEmailSignIn, etPasswordSignIn;
    private GoogleSignInClient googleSignInClient;

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
        if (checkUserEmailPassword(email, password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    //TODO
                    Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            //TODO
            Toast.makeText(SignInActivity.this, "Info isn't correct", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_ACTIVITY_REQUEST_CODE) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    protected boolean checkUserEmailPassword(String email, String password) {
        //TODO
        return (!email.isEmpty() && !password.isEmpty());
    }
}

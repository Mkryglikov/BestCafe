package mkruglikov.bestcafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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
        btnSubmitSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser(etEmailSignUp.getText().toString(), etPasswordSignUp.getText().toString());
            }
        });

        etPasswordSignUp.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addUser(etEmailSignUp.getText().toString(), etPasswordSignUp.getText().toString());
                }
                return false;
            }
        });

        SignInButton btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        btnGoogleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signUpIntent, GOOGLE_SIGN_UP_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_UP_ACTIVITY_REQUEST_CODE) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    protected void addUser(String email, String password) {
        if (checkUserEmailPassword(email, password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            //ToDo
            Toast.makeText(SignUpActivity.this, "Info isn't correct", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean checkUserEmailPassword(String email, String password) {
        //ToDo
        return (!email.isEmpty() && !password.isEmpty());
    }
}

package mkruglikov.bestcafe;

import android.app.Activity;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    public static final int SIGN_IN_ACTIVITY_REQUEST_CODE = 13;

    private FirebaseAuth firebaseAuth;
    private EditText etEmailSignIn, etPasswordSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();

        etEmailSignIn = findViewById(R.id.etEmailSignIn);
        etPasswordSignIn = findViewById(R.id.etPasswordSignIn);

        Button btnSubmitSignIn = findViewById(R.id.btnSubmitSignIn);
        btnSubmitSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(etEmailSignIn.getText().toString(), etPasswordSignIn.getText().toString());
            }
        });

        etPasswordSignIn.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    signInUser(etEmailSignIn.getText().toString(), etPasswordSignIn.getText().toString());
                }
                return false;
            }
        });
    }

    protected void signInUser(String email, String password) {
        if (checkUserEmailPassword(email, password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show(); //ToDo
                    }
                }
            });
        } else {
            //ToDo
            Toast.makeText(SignInActivity.this, "Info isn't correct", Toast.LENGTH_SHORT).show();
        }

    }

    protected boolean checkUserEmailPassword(String email, String password) {
        //ToDo
        return (!email.isEmpty() && !password.isEmpty());
    }
}

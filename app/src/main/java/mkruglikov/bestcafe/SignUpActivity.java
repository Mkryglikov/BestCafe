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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText etEmailSignUp, etPasswordSignUp;
    private Button btnSubmitSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);

        btnSubmitSignUp = findViewById(R.id.btnSubmitSignUp);
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
    }

    protected void addUser(String email, String password) {
        if (checkUserEmailPassword(email, password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
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

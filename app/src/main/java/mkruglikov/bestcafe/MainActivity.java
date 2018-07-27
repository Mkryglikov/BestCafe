package mkruglikov.bestcafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "FUCK";
    private FragmentManager fragmentManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        fragmentManager = getSupportFragmentManager();
    }


    @Override
    protected void onResume() {
        user = firebaseAuth.getCurrentUser();
        if (user == null)
            showFragmentNotSignedIn();
        else
            showFragmentSignedIn();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (SignInActivity.SIGN_IN_ACTIVITY_REQUEST_CODE):
                if (resultCode == Activity.RESULT_OK && data == null) {
                    user = firebaseAuth.getCurrentUser();
                    showFragmentSignedIn();
                } else if (resultCode == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        firebaseAuthWithGoogle(task.getResult(ApiException.class));
                    } catch (ApiException e) {
                        //ToDo
                        Log.w(TAG, "Google sign in failed", e);
                    }
                }
                break;
            case (BookingActivity.BOOKING_ACTIVITY_REQUEST_CODE):
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Booked!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = firebaseAuth.getCurrentUser();
                    showFragmentSignedIn();
                } else {
                    //Todo
                    Log.w(TAG, "Firebase Auth With Google failed");
                }
            }
        });
    }


    private void showFragmentSignedIn() {
        FragmentMainSignedIn fragment = new FragmentMainSignedIn();
        Bundle args = new Bundle();
        args.putString(FragmentMainSignedIn.USER_EMAIL_ARGUMENTS_KEY, user.getEmail());
        fragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.containerMain, fragment)
                .commitAllowingStateLoss();
    }

    private void showFragmentNotSignedIn() {
        FragmentMainNotSignedIn fragment = new FragmentMainNotSignedIn();
        fragmentManager.beginTransaction()
                .replace(R.id.containerMain, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (user != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSignOut:
                signOut();
                return true;
            default:
                return false;
        }
    }

    private void signOut() {
        firebaseAuth.signOut();
        user = firebaseAuth.getCurrentUser();
        if (user == null)
            showFragmentNotSignedIn();
        else
            Toast.makeText(this, "Error signing out", Toast.LENGTH_SHORT).show(); //ToDo
    }
}

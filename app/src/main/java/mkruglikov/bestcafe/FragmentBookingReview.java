package mkruglikov.bestcafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static mkruglikov.bestcafe.MainActivity.TAG;

public class FragmentBookingReview extends Fragment {

    private static final int GOOGLE_SIGN_IN_REVIEW_REQUEST_CODE = 18;

    private Button btnSignInReview, btnSignUpReview, btnSubmitBooking;
    private TextView tvEmailReview;
    private SignInButton btnGoogleReview;
    private ConstraintLayout layoutBookingReviewNotSignedIn, layoutBookingReviewSignedIn;
    private View rootView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    public FragmentBookingReview() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_booking_review, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GoogleSignInClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getActivity().getApplicationContext(), googleSignInOptions);

        layoutBookingReviewNotSignedIn = rootView.findViewById(R.id.layoutBookingReviewNotSignedIn);
        layoutBookingReviewSignedIn = rootView.findViewById(R.id.layoutBookingReviewSignedIn);

        changeLayout();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SignInActivity.SIGN_IN_ACTIVITY_REQUEST_CODE:
            case SignUpActivity.SIGN_UP_ACTIVITY_REQUEST_CODE:
            case GOOGLE_SIGN_IN_REVIEW_REQUEST_CODE:
                Log.i(TAG, "right case");
                if (resultCode == Activity.RESULT_OK && data == null) {
                    user = firebaseAuth.getCurrentUser();
                    changeLayout();
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
            default:
                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "onComplete: ");
                    user = firebaseAuth.getCurrentUser();
                    changeLayout();
                } else {
                    //Todo
                    Log.w(TAG, "Firebase Auth With Google failed");
                }
            }
        });
    }

    private void changeLayout() {
        if (user == null) {
            Log.i(TAG, "changeLayout: user null");
            btnSignInReview = rootView.findViewById(R.id.btnSignInReview);
            btnSignUpReview = rootView.findViewById(R.id.btnSignUpReview);
            btnGoogleReview = rootView.findViewById(R.id.btnGoogleReview);

            layoutBookingReviewSignedIn.setVisibility(View.GONE);
            layoutBookingReviewNotSignedIn.setVisibility(View.VISIBLE);

            btnSignInReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), SignInActivity.class), SignInActivity.SIGN_IN_ACTIVITY_REQUEST_CODE);
                }
            });

            btnSignUpReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(getActivity().getApplicationContext(), SignUpActivity.class), SignUpActivity.SIGN_UP_ACTIVITY_REQUEST_CODE);
                }
            });

            btnGoogleReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = googleSignInClient.getSignInIntent();
                    startActivityForResult(intent, GOOGLE_SIGN_IN_REVIEW_REQUEST_CODE);
                }
            });
        } else {
            Log.i(TAG, "changeLayout: user !=null");
            layoutBookingReviewSignedIn.setVisibility(View.VISIBLE);
            layoutBookingReviewNotSignedIn.setVisibility(View.GONE);

            tvEmailReview = rootView.findViewById(R.id.tvEmailReview);
            tvEmailReview.setText(user.getEmail());

            btnSubmitBooking = rootView.findViewById(R.id.btnSubmitBooking);
            btnSubmitBooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext(), "BOOK!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}

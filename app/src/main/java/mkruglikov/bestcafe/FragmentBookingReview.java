package mkruglikov.bestcafe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static mkruglikov.bestcafe.MainActivity.TAG;

public class FragmentBookingReview extends Fragment {

    private static final int GOOGLE_SIGN_IN_REVIEW_REQUEST_CODE = 18;
    static final String ON_BOOKING_SUBMIT_LISTENER_BUNDLE_KEY = "onBookingSubmitListener bundle key";

    private ConstraintLayout layoutBookingReviewNotSignedIn, layoutBookingReviewSignedIn;
    private View rootView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;
    private OnBookingSubmitListener onBookingSubmitListener;
    private ConnectivityManager connectivityManager;

    public FragmentBookingReview() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_booking_review, container, false);

        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        onBookingSubmitListener = getArguments().getParcelable(ON_BOOKING_SUBMIT_LISTENER_BUNDLE_KEY);
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
                if (resultCode == Activity.RESULT_OK && data == null) {
                    user = firebaseAuth.getCurrentUser();
                    changeLayout();
                } else if (resultCode == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        firebaseAuthWithGoogle(task.getResult(ApiException.class));
                    } catch (ApiException e) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.google_login_error_message, Toast.LENGTH_LONG).show();
                        Log.w(MainActivity.TAG, "Google sign in failed: " + e.getLocalizedMessage());
                    }
                }
                break;
            default:
                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = firebaseAuth.getCurrentUser();
                changeLayout();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), R.string.google_login_error_message, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Firebase Auth With Google failed: " + task.getException().getLocalizedMessage());
            }
        });
    }

    private void changeLayout() {
        if (user == null) {
            Button btnSignInReview = rootView.findViewById(R.id.btnSignInReview);
            Button btnSignUpReview = rootView.findViewById(R.id.btnSignUpReview);
            SignInButton btnGoogleReview = rootView.findViewById(R.id.btnGoogleReview);

            layoutBookingReviewSignedIn.setVisibility(View.GONE);
            layoutBookingReviewNotSignedIn.setVisibility(View.VISIBLE);

            btnSignInReview.setOnClickListener(view -> getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), SignInActivity.class), SignInActivity.SIGN_IN_ACTIVITY_REQUEST_CODE));
            btnSignUpReview.setOnClickListener(view -> startActivityForResult(new Intent(getActivity().getApplicationContext(), SignUpActivity.class), SignUpActivity.SIGN_UP_ACTIVITY_REQUEST_CODE));
            btnGoogleReview.setOnClickListener(view -> {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, GOOGLE_SIGN_IN_REVIEW_REQUEST_CODE);
            });
        } else {
            layoutBookingReviewSignedIn.setVisibility(View.VISIBLE);
            layoutBookingReviewNotSignedIn.setVisibility(View.GONE);

            TextView tvEmailReview = rootView.findViewById(R.id.tvEmailReview);
            tvEmailReview.setText(user.getEmail());

            Button btnSubmitBooking = rootView.findViewById(R.id.btnSubmitBooking);
            btnSubmitBooking.setOnClickListener(view ->
            {
                if (isNetworkConnected())
                    onBookingSubmitListener.onBookingSubmitted();
                else
                    Toast.makeText(getActivity().getApplicationContext(), R.string.no_internet_error_message, Toast.LENGTH_LONG).show();
            });
        }
    }

    interface OnBookingSubmitListener extends Parcelable {
        void onBookingSubmitted();
    }

    private boolean isNetworkConnected() {
        return connectivityManager.getActiveNetworkInfo() != null;
    }

}

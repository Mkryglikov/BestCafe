package mkruglikov.bestcafe;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentMainNotSignedIn extends Fragment {

    private ConnectivityManager connectivityManager;

    public FragmentMainNotSignedIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_not_signed_in, container, false);

        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        Button btnConnectMainNotSignedIn = rootView.findViewById(R.id.btnConnectMainNotSignedIn);
        btnConnectMainNotSignedIn.setOnClickListener(view -> startActivity(new Intent(getActivity().getApplicationContext(), ConnectActivity.class)));

        Button btnBookMainNotSignedIn = rootView.findViewById(R.id.btnBookMainNotSignedIn);
        btnBookMainNotSignedIn.setOnClickListener(view -> {
            if (isNetworkConnected())
                getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), BookingActivity.class), BookingActivity.BOOKING_ACTIVITY_REQUEST_CODE);
            else
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
        });

        TextView tvSignInMainNotSignedIn = rootView.findViewById(R.id.tvSignInMainNotSignedIn);
        tvSignInMainNotSignedIn.setOnClickListener(view -> {
            if (isNetworkConnected())
                getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), SignInActivity.class), SignInActivity.SIGN_IN_ACTIVITY_REQUEST_CODE);
            else
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
        });
        return rootView;
    }

    private boolean isNetworkConnected() {
        return connectivityManager.getActiveNetworkInfo() != null;
    }
}

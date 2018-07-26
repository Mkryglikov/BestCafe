package mkruglikov.bestcafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentMainSignedIn extends Fragment {

    public static final String USER_EMAIL_ARGUMENTS_KEY = "user_email_arguments_key";

    private Button btnConnectMainSignedIn, btnBookMainSignedIn;

    public FragmentMainSignedIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_signed_in, container, false);

        Toolbar toolbarMainSignedIn = rootView.findViewById(R.id.toolbarMainSignedIn);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarMainSignedIn);

        btnConnectMainSignedIn = rootView.findViewById(R.id.btnConnectMainSignedIn);
        btnConnectMainSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo
            }
        });

        btnBookMainSignedIn = rootView.findViewById(R.id.btnBookMainSignedIn);
        btnBookMainSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), BookingActivity.class), BookingActivity.BOOKING_ACTIVITY_REQUEST_CODE);
            }
        });

        return rootView;
    }
}

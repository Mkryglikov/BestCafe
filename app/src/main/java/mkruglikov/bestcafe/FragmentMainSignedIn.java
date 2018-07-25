package mkruglikov.bestcafe;

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

    private Button btnConnectMainNoBookings, btnBookMainNoBookings;

    public FragmentMainSignedIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_signed_in, container, false);

        Toolbar toolbarMainNoBookings = rootView.findViewById(R.id.toolbarMainNoBookings);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarMainNoBookings);

        btnConnectMainNoBookings = rootView.findViewById(R.id.btnConnectMainNoBookings);
        btnConnectMainNoBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo
            }
        });

        btnBookMainNoBookings = rootView.findViewById(R.id.btnBookMainNoBookings);
        btnBookMainNoBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo
            }
        });

        return rootView;
    }
}

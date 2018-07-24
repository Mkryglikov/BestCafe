package mkruglikov.bestcafe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentMainNotSignedIn extends Fragment {

    private TextView tvLogoMainNotSignedIn, tvSignInMainNotSignedIn;
    private Button btnConnectMainNotSignedIn, btnBookMainNotSignedIn;


    public FragmentMainNotSignedIn() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_not_signed_in, container, false);
        tvLogoMainNotSignedIn = rootView.findViewById(R.id.tvLogoMainNotSignedIn);
        tvSignInMainNotSignedIn = rootView.findViewById(R.id.tvSignInMainNotSignedIn);

        btnConnectMainNotSignedIn = rootView.findViewById(R.id.btnConnectMainNotSignedIn);
        btnConnectMainNotSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnBookMainNotSignedIn = rootView.findViewById(R.id.btnBookMainNotSignedIn);
        btnBookMainNotSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return rootView;
    }

}

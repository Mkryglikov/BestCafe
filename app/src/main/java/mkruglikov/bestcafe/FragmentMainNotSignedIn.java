package mkruglikov.bestcafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentMainNotSignedIn extends Fragment {

    public FragmentMainNotSignedIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_not_signed_in, container, false);

        Button btnConnectMainNotSignedIn = rootView.findViewById(R.id.btnConnectMainNotSignedIn);
        btnConnectMainNotSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo
            }
        });

        Button btnBookMainNotSignedIn = rootView.findViewById(R.id.btnBookMainNotSignedIn);
        btnBookMainNotSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo
            }
        });

        TextView tvSignInMainNotSignedIn = rootView.findViewById(R.id.tvSignInMainNotSignedIn);
        tvSignInMainNotSignedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), SignInActivity.class), SignInActivity.SIGN_IN_ACTIVITY_REQUEST_CODE);
            }
        });
        return rootView;
    }
}

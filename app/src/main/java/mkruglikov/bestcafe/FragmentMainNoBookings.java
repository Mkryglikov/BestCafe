package mkruglikov.bestcafe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentMainNoBookings extends Fragment {

    private Button btnConnectMainNoBookings, btnBookMainNoBookings;


    public FragmentMainNoBookings() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_no_bookings, container, false);

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

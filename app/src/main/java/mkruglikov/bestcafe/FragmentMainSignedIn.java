package mkruglikov.bestcafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import mkruglikov.bestcafe.adapters.BookingsAdapter;
import mkruglikov.bestcafe.models.Booking;

public class FragmentMainSignedIn extends Fragment {

    public static final String USER_ID_ARGUMENTS_KEY = "user_id_arguments_key";

    private Button btnConnectMainSignedIn, btnBookMainSignedIn;
    private TextView tvBookMoreMain;
    private FloatingActionButton fabConnectMainSignedIn;
    private boolean isBookingsAvailable;
    private List<Booking> bookings;
    private ConstraintLayout layoutMainNoBookings, layoutMainBookings, layoutLoadingMain;
    private View rootView;

    public FragmentMainSignedIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_signed_in, container, false);

        layoutMainNoBookings = rootView.findViewById(R.id.layoutMainNoBookings);
        layoutMainBookings = rootView.findViewById(R.id.layoutBookingsMain);
        layoutLoadingMain = rootView.findViewById(R.id.layoutLoadingMain);

        Toolbar toolbarMainSignedIn = rootView.findViewById(R.id.toolbarMainSignedIn);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarMainSignedIn);

        updateViews();

        return rootView;
    }

    private void updateViews() {
        FirestoreUtils.getBookings(getArguments().getString(USER_ID_ARGUMENTS_KEY), (downloadedBookings, exceptionMessage) -> {
            if (exceptionMessage != null) {
                isBookingsAvailable = false;
                Log.w(MainActivity.TAG, exceptionMessage);
            } else if (downloadedBookings != null && !downloadedBookings.isEmpty()) {
                isBookingsAvailable = true;
                bookings = downloadedBookings;
            } else {
                isBookingsAvailable = false;
            }

            if (isBookingsAvailable) {
                layoutLoadingMain.setVisibility(View.GONE);
                layoutMainNoBookings.setVisibility(View.GONE);
                layoutMainBookings.setVisibility(View.VISIBLE);

                RecyclerView rvMainBookings = rootView.findViewById(R.id.rvBookingsMain);
                rvMainBookings.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
                rvMainBookings.setAdapter(new BookingsAdapter(getActivity(), bookings, (isSuccessful, exceptionMessage1) -> {
                    if (isSuccessful)
                        updateViews();
                }));

                tvBookMoreMain = rootView.findViewById(R.id.tvBookMoreMain);
                tvBookMoreMain.setOnClickListener(view -> getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), BookingActivity.class), BookingActivity.BOOKING_ACTIVITY_REQUEST_CODE));

                fabConnectMainSignedIn = rootView.findViewById(R.id.fabConnectMainSignedIn);
                fabConnectMainSignedIn.setOnClickListener(view -> startActivity(new Intent(getActivity().getApplicationContext(), ConnectActivity.class)));
            } else {
                layoutLoadingMain.setVisibility(View.GONE);
                layoutMainNoBookings.setVisibility(View.VISIBLE);
                layoutMainBookings.setVisibility(View.GONE);

                btnConnectMainSignedIn = rootView.findViewById(R.id.btnConnectMainSignedIn);
                btnConnectMainSignedIn.setOnClickListener(view -> startActivity(new Intent(getActivity().getApplicationContext(), ConnectActivity.class)));

                btnBookMainSignedIn = rootView.findViewById(R.id.btnBookMainSignedIn);
                btnBookMainSignedIn.setOnClickListener(view -> getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), BookingActivity.class), BookingActivity.BOOKING_ACTIVITY_REQUEST_CODE));
            }
        });
    }
}

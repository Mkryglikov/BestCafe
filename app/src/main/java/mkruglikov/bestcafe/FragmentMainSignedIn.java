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

public class FragmentMainSignedIn extends Fragment {

    public static final String USER_ID_ARGUMENTS_KEY = "user_id_arguments_key";

    private Button btnConnectMainSignedIn, btnBookMainSignedIn;
    private TextView tvBookMoreMain;
    private FloatingActionButton fabConnectMainSignedIn;
    private boolean isBookingsAvailable;
    private List<Booking> bookings;
    private ConstraintLayout layoutMainNoBookings, layoutMainBookings;
    private View rootView;

    public FragmentMainSignedIn() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_signed_in, container, false);

        layoutMainNoBookings = rootView.findViewById(R.id.layoutMainNoBookings);
        layoutMainBookings = rootView.findViewById(R.id.layoutBookingsMain);

        Toolbar toolbarMainSignedIn = rootView.findViewById(R.id.toolbarMainSignedIn);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbarMainSignedIn);

        updateViews();

        return rootView;
    }

    private void updateViews() {
        FirestoreUtils.getBookings(getArguments().getString(USER_ID_ARGUMENTS_KEY), new FirestoreUtils.OnGetBookingsListener() {
            @Override
            public void onGotBookings(List<Booking> downloadedBookings, String exceptionMessage) {
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
                    layoutMainNoBookings.setVisibility(View.GONE);
                    layoutMainBookings.setVisibility(View.VISIBLE);

                    RecyclerView rvMainBookings = rootView.findViewById(R.id.rvBookingsMain);
                    rvMainBookings.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
                    rvMainBookings.setAdapter(new BookingsAdapter(getActivity(), bookings, new FirestoreUtils.OnDeleteBookingListener() {
                        @Override
                        public void onBookingDeleted(boolean isSuccessful, String exceptionMessage) {
                            if (isSuccessful) {
                                updateViews();
                            }
                        }
                    }));

                    tvBookMoreMain = rootView.findViewById(R.id.tvBookMoreMain);
                    tvBookMoreMain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().startActivityForResult(new Intent(getActivity().getApplicationContext(), BookingActivity.class), BookingActivity.BOOKING_ACTIVITY_REQUEST_CODE);
                        }
                    });

                    fabConnectMainSignedIn = rootView.findViewById(R.id.fabConnectMainSignedIn);
                    fabConnectMainSignedIn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //ToDo
                        }
                    });
                } else {
                    layoutMainNoBookings.setVisibility(View.VISIBLE);
                    layoutMainBookings.setVisibility(View.GONE);

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
                }
            }
        });
    }
}

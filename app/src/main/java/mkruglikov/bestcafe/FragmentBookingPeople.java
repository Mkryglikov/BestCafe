package mkruglikov.bestcafe;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mkruglikov.bestcafe.adapters.BookingPeopleAdapter;

public class FragmentBookingPeople extends Fragment {

    private static final int MAX_PEOPLE_COUNT = 6;
    public static final String ON_PEOPLE_COUNT_SELECTED_LISTENER_FRAGMENT_BOOKING_PEOPLE_BUNDLE_KEY = "OnPeopleCountSelectedListener FragmentBookingPeople Bundle key";
    public static final String SELECTED_PEOPLE_COUNT_FRAGMENT_BOOKING_PEOPLE_BUNDLE_KEY = "selectedPeopleCount FragmentBookingPeople Bundle key";

    private RecyclerView rvPeopleBooking;
    private BookingOnPeopleCountSelectedListener onPeopleCountSelectedListener;

    public FragmentBookingPeople() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fragment_booking_people, container, false);
        onPeopleCountSelectedListener = getArguments().getParcelable(ON_PEOPLE_COUNT_SELECTED_LISTENER_FRAGMENT_BOOKING_PEOPLE_BUNDLE_KEY);
        int selectedPeopleCount = getArguments().getInt(SELECTED_PEOPLE_COUNT_FRAGMENT_BOOKING_PEOPLE_BUNDLE_KEY);

        rvPeopleBooking = rootView.findViewById(R.id.rvPeopleBooking);
        rvPeopleBooking.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 3));
        rvPeopleBooking.setHasFixedSize(true);
        rvPeopleBooking.setAdapter(new BookingPeopleAdapter(getActivity().getApplicationContext(), MAX_PEOPLE_COUNT, selectedPeopleCount, noOfPeople -> {
            onPeopleCountSelectedListener.onPeopleCountSelected(noOfPeople);
            RecyclerView.LayoutManager layoutManager = rvPeopleBooking.getLayoutManager();
            for (int i = 0; i < MAX_PEOPLE_COUNT; i++) {
                if (i + 1 != noOfPeople) {
                    Button btn = layoutManager.getChildAt(i).findViewById(R.id.btnItemBookingPeople);
                    btn.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.button_round_white));
                    btn.setTextColor(Color.BLACK);
                }
            }
        }));
        return rootView;
    }

    public interface BookingOnPeopleCountSelectedListener extends Parcelable {
        void onPeopleCountSelected(int count);
    }

}

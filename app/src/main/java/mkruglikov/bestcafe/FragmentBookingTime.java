package mkruglikov.bestcafe;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

public class FragmentBookingTime extends Fragment {

    public static final String ON_TIME_SELECTED_LISTENER_FRAGMENT_BOOKING_TIME_BUNDLE_KEY = "OnTimeSelectedListener FragmentBookingTime Bundle key";

    private TimePicker timePickerBooking;
    private BookingOnTimeSelectedListener onTimeSelectedListener;

    public FragmentBookingTime() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking_time, container, false);
        onTimeSelectedListener = getArguments().getParcelable(ON_TIME_SELECTED_LISTENER_FRAGMENT_BOOKING_TIME_BUNDLE_KEY);

        timePickerBooking = rootView.findViewById(R.id.timePickerBooking);
        timePickerBooking.setIs24HourView(DateFormat.is24HourFormat(getActivity().getApplicationContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerBooking.setHour(BookingActivity.getSelectedHour());
            timePickerBooking.setMinute(BookingActivity.getSelectedMinute());
        }
        timePickerBooking.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                onTimeSelectedListener.onTimeSelected(hourOfDay, minute);
            }
        });
        return rootView;
    }

    public interface BookingOnTimeSelectedListener extends Parcelable {
        void onTimeSelected(int hourOfDay, int minute);
    }
}

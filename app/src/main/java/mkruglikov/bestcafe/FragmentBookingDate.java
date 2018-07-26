package mkruglikov.bestcafe;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;

public class FragmentBookingDate extends Fragment {

    public static final String ON_DATE_SELECTED_LISTENER_FRAGMENT_BOOKING_DATE_BUNDLE_KEY = "OnDateSelectedListener FragmentBookingDate Bundle key";

    private MaterialCalendarView calendarBooking;
    private BookingOnDateSelectedListener onDateSelectedListener;

    public FragmentBookingDate() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking_date, container, false);
        onDateSelectedListener = getArguments().getParcelable(ON_DATE_SELECTED_LISTENER_FRAGMENT_BOOKING_DATE_BUNDLE_KEY);
        calendarBooking = rootView.findViewById(R.id.calendarBooking);
        calendarBooking.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                onDateSelectedListener.onDateSelected(calendarDay);
            }
        });
        Calendar selectedDate = BookingActivity.getSelectedDate();
        if (selectedDate != null) {
            calendarBooking.setCurrentDate(selectedDate);
            calendarBooking.setDateSelected(selectedDate, true);
        }

        return rootView;
    }

    public interface BookingOnDateSelectedListener extends Parcelable {
        void onDateSelected(@NonNull CalendarDay calendarDay);
    }
}

package mkruglikov.bestcafe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;

@SuppressLint("ParcelCreator")
public class BookingActivity extends AppCompatActivity implements FragmentBookingDate.BookingOnDateSelectedListener {

    public static final int BOOKING_ACTIVITY_REQUEST_CODE = 17;
    private FragmentManager fragmentManager;
    private ImageView ivBookingDateIcon, ivBookingTimeIcon, ivBookingPeopleIcon, ivBookingNextStepIcon;
    private TextView tvToolbarBooking, tvBookingDateText, tvBookingTimeText, tvBookingPeopleText, tvBookingNextStepNo, tvBookingNextStepHint;
    private ConstraintLayout clBookingNextStep;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd ");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Toolbar toolbarBooking = findViewById(R.id.toolbarBooking);
        setSupportActionBar(toolbarBooking);

        fragmentManager = getSupportFragmentManager();

        tvToolbarBooking = findViewById(R.id.tvToolbarBooking);

        ivBookingDateIcon = findViewById(R.id.ivBookingDateIcon);
        ivBookingTimeIcon = findViewById(R.id.ivBookingTimeIcon);
        ivBookingPeopleIcon = findViewById(R.id.ivBookingPeopleIcon);

        ivBookingNextStepIcon = findViewById(R.id.ivBookingNextStepIcon);

        tvBookingDateText = findViewById(R.id.tvBookingDateText);
        tvBookingTimeText = findViewById(R.id.tvBookingTimeText);
        tvBookingPeopleText = findViewById(R.id.tvBookingPeopleText);

        tvBookingNextStepNo = findViewById(R.id.tvBookingNextStepNo);
        tvBookingNextStepHint = findViewById(R.id.tvBookingNextStepHint);

        clBookingNextStep = findViewById(R.id.clBookingNextStep);
        clBookingNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ToDo
            }
        });


        FragmentBookingDate fragmentBookingDate = new FragmentBookingDate();
        Bundle args = new Bundle();
        args.putParcelable(FragmentBookingDate.ON_DATE_SELECTED_LISTENER_FRAGMENT_BOOKING_DATE_BUNDLE_KEY, (FragmentBookingDate.BookingOnDateSelectedListener) this);
        fragmentBookingDate.setArguments(args);
        fragmentManager.beginTransaction()
                .add(R.id.containerBooking, fragmentBookingDate)
                .commit();

        ivBookingNextStepIcon.setImageResource(R.drawable.ic_clock);
        tvBookingNextStepNo.setText("2");
        tvBookingNextStepHint.setText(R.string.booking_time_hint);
        tvToolbarBooking.setText(R.string.booking_date_hint);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        tvBookingDateText.setText(sdf.format(calendarDay.getCalendar().getTime()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}

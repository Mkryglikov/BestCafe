package mkruglikov.bestcafe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressLint("ParcelCreator")
public class BookingActivity extends AppCompatActivity implements
        FragmentBookingDate.BookingOnDateSelectedListener,
        FragmentBookingTime.BookingOnTimeSelectedListener,
        FragmentBookingPeople.BookingOnPeopleCountSelectedListener {

    public static final int BOOKING_ACTIVITY_REQUEST_CODE = 17;
    public static final int CAFES_OPENING_HOUR = 9;
    public static final int CAFES_CLOSING_HOUR = 22;

    private FragmentManager fragmentManager;
    private ImageView ivBookingDateIcon, ivBookingTimeIcon, ivBookingPeopleIcon, ivBookingNextStepIcon;
    private TextView tvToolbarBooking, tvBookingDateText, tvBookingTimeText, tvBookingPeopleText, tvBookingNextStepNo, tvBookingNextStepHint;
    private ConstraintLayout clBookingNextStep;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd ");
    private static Calendar selectedDate;
    private static int selectedHour = 24, selectedMinute = 60, selectedPeopleCount;
    private static int currentStep = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Toolbar toolbarBooking = findViewById(R.id.toolbarBooking);
        setSupportActionBar(toolbarBooking);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();

        tvToolbarBooking = findViewById(R.id.tvToolbarBooking);

        ivBookingDateIcon = findViewById(R.id.ivBookingDateIcon);
        ivBookingDateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFirstStep();
            }
        });
        ivBookingTimeIcon = findViewById(R.id.ivBookingTimeIcon);
        ivBookingTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSecondStep();
            }
        });
        ivBookingPeopleIcon = findViewById(R.id.ivBookingPeopleIcon);
        ivBookingPeopleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThirdStep();
            }
        });

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
                nextStep();
            }
        });
        nextStep();
    }

    private void nextStep() {
        switch (currentStep) {
            case 0:
                showFirstStep();
                break;
            case 1:
                if (selectedDate != null)
                    showSecondStep();
                else
                    Toast.makeText(this, "Choose the day", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                if (selectedHour == 24 || selectedMinute == 60)
                    Toast.makeText(this, "Choose the time", Toast.LENGTH_SHORT).show();
                else if (selectedHour >= CAFES_OPENING_HOUR && selectedHour < CAFES_CLOSING_HOUR)
                    showThirdStep();
                else
                    Toast.makeText(this, "We're open from " + CAFES_OPENING_HOUR + " to " + CAFES_CLOSING_HOUR, Toast.LENGTH_SHORT).show();
                break;
            case 3:
                if (selectedPeopleCount > 0)
                    showFourthStep();
                else
                    Toast.makeText(this, "Choose people count", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep == 1) {
            super.onBackPressed();
            finish();
        } else if (currentStep > 1) {
            currentStep -= 2;
            nextStep();
        }
    }

    private void showFirstStep() {
        FragmentBookingDate fragmentBookingDate = new FragmentBookingDate();
        Bundle args = new Bundle();
        args.putParcelable(FragmentBookingDate.ON_DATE_SELECTED_LISTENER_FRAGMENT_BOOKING_DATE_BUNDLE_KEY, this);
        fragmentBookingDate.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.containerBooking, fragmentBookingDate)
                .addToBackStack(null)
                .commit();

        clBookingNextStep.setVisibility(View.VISIBLE);

        ivBookingDateIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));
        ivBookingTimeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        ivBookingPeopleIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));

        ivBookingNextStepIcon.setImageResource(R.drawable.ic_clock);
        tvBookingNextStepNo.setText("2");
        tvBookingNextStepHint.setText(R.string.booking_time_hint);
        tvToolbarBooking.setText(R.string.booking_date_hint);
        currentStep = 1;
    }

    private void showSecondStep() {
        FragmentBookingTime fragmentBookingTime = new FragmentBookingTime();
        Bundle args = new Bundle();
        args.putParcelable(FragmentBookingTime.ON_TIME_SELECTED_LISTENER_FRAGMENT_BOOKING_TIME_BUNDLE_KEY, this);
        fragmentBookingTime.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.containerBooking, fragmentBookingTime)
                .addToBackStack(null)
                .commit();

        clBookingNextStep.setVisibility(View.VISIBLE);

        ivBookingDateIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        ivBookingTimeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));
        ivBookingPeopleIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));

        tvToolbarBooking.setText(R.string.booking_time_hint);
        ivBookingNextStepIcon.setImageResource(R.drawable.ic_people);
        tvBookingNextStepNo.setText("3");
        tvBookingNextStepHint.setText(R.string.booking_people_hint);
        currentStep = 2;
    }

    private void showThirdStep() {
        FragmentBookingPeople fragmentBookingPeople = new FragmentBookingPeople();
        Bundle args = new Bundle();
        args.putParcelable(FragmentBookingPeople.ON_PEOPLE_COUNT_SELECTED_LISTENER_FRAGMENT_BOOKING_PEOPLE_BUNDLE_KEY, this);
        args.putInt(FragmentBookingPeople.SELECTED_PEOPLE_COUNT_FRAGMENT_BOOKING_PEOPLE_BUNDLE_KEY, selectedPeopleCount);
        fragmentBookingPeople.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.containerBooking, fragmentBookingPeople)
                .addToBackStack(null)
                .commit();

        clBookingNextStep.setVisibility(View.VISIBLE);

        ivBookingDateIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        ivBookingTimeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        ivBookingPeopleIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));

        tvToolbarBooking.setText(R.string.booking_people_hint);
        ivBookingNextStepIcon.setImageResource(R.drawable.ic_review);
        tvBookingNextStepNo.setText("4");
        tvBookingNextStepHint.setText(R.string.booking_review_hint);
        currentStep = 3;
    }

    private void showFourthStep() {
//        FragmentBookingReview fragmentBookingReview = new FragmentBookingReview();
//        fragmentManager.beginTransaction()
//                .replace(R.id.containerBooking, fragmentBookingReview)
//                .addToBackStack(null)
//                .commit();

        ivBookingDateIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        ivBookingTimeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        ivBookingPeopleIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorLightGrey));
        clBookingNextStep.setVisibility(View.GONE);

        tvToolbarBooking.setText(R.string.booking_review_hint);
        currentStep = 4;
    }

    @Override
    public void onDateSelected(@NonNull CalendarDay calendarDay) {
        selectedDate = calendarDay.getCalendar();
        tvBookingDateText.setText(sdf.format(calendarDay.getCalendar().getTime()));
    }

    @Override
    public void onTimeSelected(int hourOfDay, int minute) {
        selectedHour = hourOfDay;
        selectedMinute = minute;
        tvBookingTimeText.setText((hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute));
    }

    @Override
    public void onPeopleCountSelected(int count) {
        selectedPeopleCount = count;
        tvBookingPeopleText.setText(String.valueOf(count));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    @Nullable
    public static Calendar getSelectedDate() {
        return selectedDate;
    }

    public static int getSelectedHour() {
        return selectedHour;
    }

    public static int getSelectedMinute() {
        return selectedMinute;
    }
}
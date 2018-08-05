package mkruglikov.bestcafe;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import mkruglikov.bestcafe.models.Booking;

public class Widget extends AppWidgetProvider {

    private static final String ONCLICK_SIGNUP = "onclick_signup";
    private static final String ONCLICK_SIGNIN = "onclick_signin";
    private static final String ONCLICK_BOOK = "onclick_book";
    private static final String ONCLICK_CONNECT = "onclick_connect";
    private static final String ONCLICK_CANCEL = "onclick_cancel";

    private static final String WIDGET_ID_INTENT_EXTRA_KEY = "widget_id_intent_extra_key";
    private static final String WIDGET_BOOKING_ID_INTENT_EXTRA_KEY = "widget_booking_id_intent_extra_key";

    private RemoteViews widgetView;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM, d", Locale.US);


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        int widgetId = intent.getIntExtra(WIDGET_ID_INTENT_EXTRA_KEY, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.i(MainActivity.TAG, "onReceive widgetId: " + String.valueOf(widgetId));
        switch (intent.getAction()) {
            case ONCLICK_SIGNUP:
                if (isNetworkConnected(context)) {
                    Intent signUpIntent = new Intent(context, SignUpActivity.class);
                    signUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(signUpIntent);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
                }
                break;
            case ONCLICK_SIGNIN:
                if (isNetworkConnected(context)) {
                    Intent signInIntent = new Intent(context, SignInActivity.class);
                    signInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(signInIntent);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
                }
                break;
            case ONCLICK_BOOK:
                if (isNetworkConnected(context)) {
                    Intent bookingIntent = new Intent(context, BookingActivity.class);
                    bookingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(bookingIntent);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
                }
                break;
            case ONCLICK_CONNECT:
                Intent connectIntent = new Intent(context, ConnectActivity.class);
                connectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(connectIntent);
                break;
            case ONCLICK_CANCEL:
                if (isNetworkConnected(context)) {
                    String bookingId = intent.getExtras().getString(WIDGET_BOOKING_ID_INTENT_EXTRA_KEY);
                    Log.i(MainActivity.TAG, "onReceive bookingId: " + bookingId);
                    FirestoreUtils.deleteBooking(bookingId, (isSuccessful, exceptionMessage) -> {
                        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                            Log.w(MainActivity.TAG, "Error deleting booking: " + exceptionMessage);
                            Toast.makeText(context, context.getString(R.string.booking_deleting_error_message), Toast.LENGTH_LONG).show();
                            return;
                        }
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        updateWidget(context, appWidgetManager, widgetId);
                        Toast.makeText(context, context.getString(R.string.booking_canceled_message), Toast.LENGTH_LONG).show();
                    });
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet_error_message), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private PendingIntent getPendingIntent(Context context, String action, int widgetId, String bookingId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);

        if (bookingId != null)
            intent.putExtra(WIDGET_BOOKING_ID_INTENT_EXTRA_KEY, bookingId);

        intent.putExtra(WIDGET_ID_INTENT_EXTRA_KEY, widgetId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);

        if (user == null) {
            Log.i(MainActivity.TAG, "User is null");
            widgetView.setTextViewText(R.id.tvWidgetText, context.getString(R.string.login_to_view_bookings_message));
            widgetView.setTextViewText(R.id.btnWidgetLeft, context.getString(R.string.sign_up));
            widgetView.setTextViewText(R.id.btnWidgetRight, context.getString(R.string.sign_up));
            widgetView.setOnClickPendingIntent(R.id.btnWidgetLeft, getPendingIntent(context, ONCLICK_SIGNUP, widgetId, null));
            widgetView.setOnClickPendingIntent(R.id.btnWidgetRight, getPendingIntent(context, ONCLICK_SIGNIN, widgetId, null));
            appWidgetManager.updateAppWidget(widgetId, widgetView);

        } else {
            Log.i(MainActivity.TAG, "User is not null");

            FirestoreUtils.getBookings(user.getUid(), (bookings, exceptionMessage) -> {
                if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                    Log.w(MainActivity.TAG, "Error getting bookings: " + exceptionMessage);
                    return;
                }

                if (bookings.isEmpty()) {
                    Log.i(MainActivity.TAG, "Bookings is empty");

                    widgetView.setTextViewText(R.id.tvWidgetText, context.getString(R.string.no_active_bookings_message));
                    widgetView.setTextViewText(R.id.btnWidgetLeft, context.getString(R.string.book_a_table));
                    widgetView.setTextViewText(R.id.btnWidgetRight, context.getString(R.string.connect_to_table));
                    widgetView.setOnClickPendingIntent(R.id.btnWidgetLeft, getPendingIntent(context, ONCLICK_BOOK, widgetId, null));
                    widgetView.setOnClickPendingIntent(R.id.btnWidgetRight, getPendingIntent(context, ONCLICK_CONNECT, widgetId, null));
                } else {
                    Log.i(MainActivity.TAG, "Bookings is not empty");

                    Booking booking = bookings.get(0);

                    Log.i(MainActivity.TAG, "booking id: " + booking.getId());

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(String.valueOf(booking.getDay())));
                    calendar.set(Calendar.MONTH, Integer.parseInt(String.valueOf(booking.getMonth())) - 1);
                    calendar.set(Calendar.YEAR, Integer.parseInt(String.valueOf(booking.getYear())));
                    sdf.setTimeZone(calendar.getTimeZone());

                    widgetView.setTextViewText(R.id.tvWidgetText, context.getString(R.string.you_have_a_booking_on) + " " + sdf.format(calendar.getTime()) + " " + context.getString(R.string.at) + " " + String.valueOf(booking.getHour()) + ":" + String.valueOf(booking.getMinute()));
                    widgetView.setTextViewText(R.id.btnWidgetLeft, context.getString(R.string.cancel_booking_text));
                    widgetView.setTextViewText(R.id.btnWidgetRight, context.getString(R.string.connect_to_table));
                    Log.i(MainActivity.TAG, "Setting booking id: " + booking.getId());
                    widgetView.setOnClickPendingIntent(R.id.btnWidgetLeft, getPendingIntent(context, ONCLICK_CANCEL, widgetId, booking.getId()));
                    widgetView.setOnClickPendingIntent(R.id.btnWidgetRight, getPendingIntent(context, ONCLICK_CONNECT, widgetId, null));
                }
                appWidgetManager.updateAppWidget(widgetId, widgetView);
            });
        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }
}

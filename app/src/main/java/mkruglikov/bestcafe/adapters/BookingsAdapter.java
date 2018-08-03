package mkruglikov.bestcafe.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import mkruglikov.bestcafe.FirestoreUtils;
import mkruglikov.bestcafe.R;
import mkruglikov.bestcafe.models.Booking;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {

    private final Context context;
    private final List<Booking> bookings;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM, d ", Locale.US);
    private final FirestoreUtils.OnDeleteBookingListener onDeleteBookingListener;

    public BookingsAdapter(Context context, List<Booking> bookings, FirestoreUtils.OnDeleteBookingListener onDeleteBookingListener) {
        this.context = context;
        this.bookings = bookings;
        this.onDeleteBookingListener = onDeleteBookingListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBookingItemPeople, tvBookingItemDate, tvBookingItemTime;
        private final Button btnBookingItemCancel;

        ViewHolder(View itemView) {
            super(itemView);
            tvBookingItemPeople = itemView.findViewById(R.id.tvBookingItemPeople);
            tvBookingItemDate = itemView.findViewById(R.id.tvBookingItemDate);
            tvBookingItemTime = itemView.findViewById(R.id.tvBookingItemTime);
            btnBookingItemCancel = itemView.findViewById(R.id.btnBookingItemCancel);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Booking booking = bookings.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(String.valueOf(booking.getDay())));
        calendar.set(Calendar.MONTH, Integer.parseInt(String.valueOf(booking.getMonth())) - 1);
        calendar.set(Calendar.YEAR, Integer.parseInt(String.valueOf(booking.getYear())));
        sdf.setTimeZone(calendar.getTimeZone());
        holder.tvBookingItemDate.setText(sdf.format(calendar.getTime()));

        String hour = booking.getHour() < 10 ? "0" + String.valueOf(booking.getHour()) : String.valueOf(booking.getHour());
        String minute = booking.getMinute() < 10 ? "0" + String.valueOf(booking.getMinute()) : String.valueOf(booking.getMinute());
        holder.tvBookingItemTime.setText(String.format("%s:%s", hour, minute));
        holder.tvBookingItemPeople.setText(String.valueOf(booking.getPeople()));

        holder.btnBookingItemCancel.setOnClickListener(view -> {
            AlertDialog alert = new AlertDialog.Builder(context)
                    .setTitle(R.string.confirmation_title)
                    .setMessage(R.string.booking_cancel_confirmation)
                    .setCancelable(true)
                    .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel())
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> FirestoreUtils.deleteBooking(booking.getId(), onDeleteBookingListener))
                    .create();
            alert.show();
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }
}

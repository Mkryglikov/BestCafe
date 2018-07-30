package mkruglikov.bestcafe;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mkruglikov.bestcafe.models.Booking;

public class FirestoreUtils {

    static final String FIRESTORE_BOOKINGS_COLLECTION = "bookings";
    static final String FIRESTORE_COUNTER_DOCUMENT = "counter";
    static final String FIRESTORE_COUNTER_FIELD = "lastId";

    static final String FIRESTORE_USERID_FIELD = "userId";
    static final String FIRESTORE_DAY_FIELD = "day";
    static final String FIRESTORE_MONTH_FIELD = "month";
    static final String FIRESTORE_YEAR_FIELD = "year";
    static final String FIRESTORE_HOUR_FIELD = "hour";
    static final String FIRESTORE_MINUTE_FIELD = "minute";
    static final String FIRESTORE_PEOPLE_FIELD = "people";
    static final String FIRESTORE_ACTIVE_FIELD = "active";

    private static FirebaseFirestore db;
    private static OnAddBookingListener onAddBookingListener;
    private static OnGetBookingsListener onGetBookingsListener;
    private static OnDeleteBookingListener onDeleteBookingListener;

    public static void addBooking(Map<String, Object> booking, OnAddBookingListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onAddBookingListener = listener;

        db.collection(FIRESTORE_BOOKINGS_COLLECTION)
                .add(booking)
                .addOnSuccessListener(documentReference -> db.collection(FIRESTORE_BOOKINGS_COLLECTION)
                        .document(FIRESTORE_COUNTER_DOCUMENT)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    db.collection(FIRESTORE_BOOKINGS_COLLECTION)
                                            .document(FIRESTORE_COUNTER_DOCUMENT)
                                            .update(FIRESTORE_COUNTER_FIELD, ((long) document.getData().get(FIRESTORE_COUNTER_FIELD)) + 1)
                                            .addOnSuccessListener(aVoid -> onAddBookingListener.onBookingAdded(true, null))
                                            .addOnFailureListener(e -> onAddBookingListener.onBookingAdded(false, "Counter updating failed: " + e.getLocalizedMessage()));
                                } else {
                                    onAddBookingListener.onBookingAdded(false, "Counter updating failed: Document doesn't exist");
                                }
                            } else {
                                onAddBookingListener.onBookingAdded(false, "Error getting counter document " + task.getException().getLocalizedMessage());
                            }
                        }))
                .addOnFailureListener(e -> onAddBookingListener.onBookingAdded(false, "Booking failed: " + e.getLocalizedMessage()));
    }

    public interface OnAddBookingListener {
        void onBookingAdded(boolean isSuccessful, String exceptionMessage);
    }

    public static void getBookings(String userId, OnGetBookingsListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onGetBookingsListener = listener;

        CollectionReference bookingsRef = db.collection(FIRESTORE_BOOKINGS_COLLECTION);
        Query query = bookingsRef.whereEqualTo(FIRESTORE_USERID_FIELD, userId).whereEqualTo(FIRESTORE_ACTIVE_FIELD, true);
        query.get().addOnCompleteListener(task -> {
            List<Booking> bookings = new ArrayList<>();
            QuerySnapshot querySnapshot = task.getResult();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                bookings.add(new Booking(
                        document.getId(),
                        (String) document.get(FIRESTORE_USERID_FIELD),
                        (long) document.get(FIRESTORE_DAY_FIELD),
                        (long) document.get(FIRESTORE_MONTH_FIELD),
                        (long) document.get(FIRESTORE_YEAR_FIELD),
                        (long) document.get(FIRESTORE_HOUR_FIELD),
                        (long) document.get(FIRESTORE_MINUTE_FIELD),
                        (long) document.get(FIRESTORE_PEOPLE_FIELD),
                        (boolean) document.get(FIRESTORE_ACTIVE_FIELD)));
            }
            onGetBookingsListener.onGotBookings(bookings, null);
        }).addOnFailureListener(e -> onGetBookingsListener.onGotBookings(null, e.getLocalizedMessage()));
    }

    public interface OnGetBookingsListener {
        void onGotBookings(List<Booking> bookings, String exceptionMessage);
    }

    public static void deleteBooking(String bookingId, OnDeleteBookingListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onDeleteBookingListener = listener;

        db.collection(FIRESTORE_BOOKINGS_COLLECTION)
                .document(bookingId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        onDeleteBookingListener.onBookingDeleted(true, null);
                    else
                        onDeleteBookingListener.onBookingDeleted(false, task.getException().getLocalizedMessage());
                });
    }

    public interface OnDeleteBookingListener {
        void onBookingDeleted(boolean isSuccessful, String exceptionMessage);
    }

}

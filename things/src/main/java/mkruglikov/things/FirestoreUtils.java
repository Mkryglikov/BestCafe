package mkruglikov.things;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirestoreUtils {

    static final String FIRESTORE_MENU_COLLECTION = "menu";
    static final String FIRESTORE_ORDERS_COLLECTION = "orders";

    static final String FIRESTORE_ID_FIELD = "id";
    static final String FIRESTORE_NAME_FIELD = "name";
    static final String FIRESTORE_CATEGORY_FIELD = "category";
    static final String FIRESTORE_DESCRIPTION_FIELD = "description";
    static final String FIRESTORE_PRICE_FIELD = "price";

    static final String FIRESTORE_TABLE_FIELD = "table";
    static final String FIRESTORE_ITEMS_FIELD = "items";
    static final String FIRESTORE_STATUS_FIELD = "status";
    static final String FIRESTORE_CALL_WAITER_FIELD = "callWaiter";

    static final String FIRESTORE_STATUS_PREPARING = "preparing";
    static final String FIRESTORE_STATUS_EATS = "eats";
    static final String FIRESTORE_STATUS_DONE = "done";

    private static FirebaseFirestore db;
    private static OnGetMenuListener onGetMenuListener;
    private static OnAddOrderListener onAddOrderListener;
    private static OnCheckIsTableActiveListener onCheckIsTableActiveListener;
    private static OnGetOrderItemsListener onGetOrderItemsListener;
    private static OnAddExtraItemsListener onAddExtraitemsListener;

    public static void getMenu(OnGetMenuListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();

        onGetMenuListener = listener;

        db.collection(FIRESTORE_MENU_COLLECTION).get().addOnCompleteListener(task -> {
            List<MenuItem> menu = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                try {
                    menu.add(new MenuItem(
                            document.getId(),
                            (String) document.get(FIRESTORE_NAME_FIELD),
                            (String) document.get(FIRESTORE_CATEGORY_FIELD),
                            (String) document.get(FIRESTORE_DESCRIPTION_FIELD),
                            (long) document.get(FIRESTORE_PRICE_FIELD)));
                } catch (ClassCastException e) {
                    menu.add(new MenuItem(
                            document.getId(),
                            (String) document.get(FIRESTORE_NAME_FIELD),
                            (String) document.get(FIRESTORE_CATEGORY_FIELD),
                            (String) document.get(FIRESTORE_DESCRIPTION_FIELD),
                            Long.valueOf((String) document.get(FIRESTORE_PRICE_FIELD))));
                }
            }
            Log.i(MainActivity.TAG, "Menu downloaded");
            onGetMenuListener.onGotMenu(menu, null);
        }).addOnFailureListener(e -> onGetMenuListener.onGotMenu(null, e.getLocalizedMessage()));
    }

    public interface OnGetMenuListener {
        void onGotMenu(List<MenuItem> menu, String exceptionMessage);
    }

    public static void addOrder(Map<String, Object> order, OnAddOrderListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onAddOrderListener = listener;
        db.collection(FIRESTORE_ORDERS_COLLECTION)
                .add(order)
                .addOnSuccessListener(documentReference -> onAddOrderListener.onOrderAdded(documentReference.getId(), null))
                .addOnFailureListener(e -> onAddOrderListener.onOrderAdded(null, "Adding order failed: " + e.getLocalizedMessage()));
    }

    public interface OnAddOrderListener {
        void onOrderAdded(String orderId, String exceptionMessage);
    }

    public static void checkIsTableActive(int table, OnCheckIsTableActiveListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onCheckIsTableActiveListener = listener;

        CollectionReference orders = db.collection(FIRESTORE_ORDERS_COLLECTION);
        orders.whereEqualTo(FIRESTORE_TABLE_FIELD, table)
                .whereEqualTo(FIRESTORE_STATUS_FIELD, FIRESTORE_STATUS_PREPARING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.getDocuments().size() > 0) {
                        onCheckIsTableActiveListener.onTableChecked(
                                true,
                                null,
                                queryDocumentSnapshots.getDocuments().get(0).getId(),
                                queryDocumentSnapshots.getDocuments().get(0).getString(FIRESTORE_STATUS_FIELD),
                                ((List<String>) queryDocumentSnapshots.getDocuments().get(0).get(FIRESTORE_ITEMS_FIELD)).size());
                    } else {
                        orders.whereEqualTo(FIRESTORE_TABLE_FIELD, table)
                                .whereEqualTo(FIRESTORE_STATUS_FIELD, FIRESTORE_STATUS_EATS)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (queryDocumentSnapshots1.getDocuments().size() > 0) {
                                        onCheckIsTableActiveListener.onTableChecked(
                                                true,
                                                null,
                                                queryDocumentSnapshots1.getDocuments().get(0).getId(),
                                                queryDocumentSnapshots1.getDocuments().get(0).getString(FIRESTORE_STATUS_FIELD),
                                                ((List<String>) queryDocumentSnapshots1.getDocuments().get(0).get(FIRESTORE_ITEMS_FIELD)).size());
                                    } else {
                                        onCheckIsTableActiveListener.onTableChecked(
                                                false,
                                                null,
                                                null,
                                                null,
                                                0);
                                    }
                                })
                                .addOnFailureListener(e -> onCheckIsTableActiveListener.onTableChecked(false, e.getLocalizedMessage(), null, null, 0));
                    }
                })
                .addOnFailureListener(e -> onCheckIsTableActiveListener.onTableChecked(false, e.getLocalizedMessage(), null, null, 0));
    }

    public interface OnCheckIsTableActiveListener {
        void onTableChecked(boolean isActive, String exceptionMessage, String orderId, String orderStatus, int itemsCount);
    }

    public static void getOrderRealtimeUpdates(String orderId, EventListener<DocumentSnapshot> listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_ORDERS_COLLECTION).document(orderId).addSnapshotListener(listener);
    }

    public static void getIsActiveRealtimeUpdates(OnTableStatusUpdateListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        Query query = db.collection(FIRESTORE_ORDERS_COLLECTION).whereEqualTo(FIRESTORE_TABLE_FIELD, MainActivity.TABLE_NUMBER);
        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            boolean isActive = false;
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    if (!documentSnapshot.getString(FIRESTORE_STATUS_FIELD).equals(FIRESTORE_STATUS_DONE)) {
                        isActive = true;
                        break;
                    }
                }
                listener.onTableStatusUpdated(isActive);
            }
        });

    }

    public interface OnTableStatusUpdateListener {
        void onTableStatusUpdated(boolean isActive);
    }

    public static void getWaiterStatusRealtimeUpdates(String orderId, EventListener<DocumentSnapshot> listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        DocumentReference document = db.collection(FIRESTORE_ORDERS_COLLECTION).document(orderId);
        document.addSnapshotListener(listener);
        document.get().addOnSuccessListener(documentSnapshot -> {
            listener.onEvent(documentSnapshot, null);
        });
    }

    public static void callTheWaiter(String orderId) {
        if (db == null)
            db = FirebaseFirestore.getInstance();

        DocumentReference orderDocument = db.collection(FIRESTORE_ORDERS_COLLECTION).document(orderId);
        orderDocument.update(FIRESTORE_CALL_WAITER_FIELD, true);
    }

    public static void getOrderItems(String orderId, OnGetOrderItemsListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();

        onGetOrderItemsListener = listener;

        db.collection(FIRESTORE_ORDERS_COLLECTION).document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> onGetOrderItemsListener.onGotItems(
                        ((List<String>) documentSnapshot.get(FIRESTORE_ITEMS_FIELD)),
                        documentSnapshot.getString(FIRESTORE_STATUS_FIELD).equals(FIRESTORE_STATUS_PREPARING),
                        null))
                .addOnFailureListener(e -> onGetOrderItemsListener.onGotItems(null, false, e.getLocalizedMessage()));

    }

    public interface OnGetOrderItemsListener {
        void onGotItems(List<String> items, boolean isCooking, String exceptionMessage);
    }

    public static void addExtraItems(String orderId, List<String> items, OnAddExtraItemsListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onAddExtraitemsListener = listener;

        db.collection(FIRESTORE_ORDERS_COLLECTION)
                .document(orderId)
                .update(FIRESTORE_ITEMS_FIELD, items, FIRESTORE_STATUS_FIELD, FIRESTORE_STATUS_PREPARING)
                .addOnSuccessListener(aVoid -> onAddExtraitemsListener.onExtraItemsAdded(null))
                .addOnFailureListener(e -> onAddExtraitemsListener.onExtraItemsAdded(e.getLocalizedMessage()));
    }

    public interface OnAddExtraItemsListener {
        void onExtraItemsAdded(String exceptionMessage);
    }
}

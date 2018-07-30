package mkruglikov.things;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirestoreUtils {

    static final String FIRESTORE_MENU_COLLECTION = "menu";

    static final String FIRESTORE_NAME_FIELD = "name";
    static final String FIRESTORE_CATEGORY_FIELD = "category";
    static final String FIRESTORE_DESCRIPTION_FIELD = "description";
    static final String FIRESTORE_PRICE_FIELD = "price";
    static final String FIRESTORE_IS_AVAILABLE_FIELD = "is_available";

    private static FirebaseFirestore db;
    private static OnGetMenuListener onGetMenuListener;

    public static void getMenu(OnGetMenuListener listener) {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        onGetMenuListener = listener;

        db.collection(FIRESTORE_MENU_COLLECTION).get().addOnCompleteListener(task -> {
            List<MenuItem> menu = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                menu.add(new MenuItem(
                        document.getId(),
                        (String) document.get(FIRESTORE_NAME_FIELD),
                        (String) document.get(FIRESTORE_CATEGORY_FIELD),
                        (String) document.get(FIRESTORE_DESCRIPTION_FIELD),
                        (long) document.get(FIRESTORE_PRICE_FIELD),
                        (boolean) document.get(FIRESTORE_IS_AVAILABLE_FIELD)));
            }
            onGetMenuListener.onGotMenu(menu, null);
        }).addOnFailureListener(e -> onGetMenuListener.onGotMenu(null, e.getLocalizedMessage()));
    }

    public interface OnGetMenuListener {
        void onGotMenu(List<MenuItem> menu, String exceptionMessage);
    }
}

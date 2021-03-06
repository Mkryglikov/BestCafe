package mkruglikov.bestcafe;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mkruglikov.bestcafe.adapters.OrderTabsAdapter;
import mkruglikov.bestcafe.adapters.SelectedMenuItemsAdapter;
import mkruglikov.bestcafe.models.MenuItem;

public class FragmentOrder extends Fragment {

    public static final String ORDER_FRAGMENT_MENU_ARGUMENTS_KEY = "order_fragment_menu_arguments_key";
    public static final String THINGS_ENDPOINT_ID_ARGUMENTS_KEY = "things_endpoint_id_arguments_key";
    public static final String IS_NEW_ORDER_ARGUMENTS_KEY = "is_new_order_arguments_key";

    private List<MenuItem> menu;
    private List<MenuItem> selectedItems;
    private List<String> categories;
    private TextView tvTotalOrderBottomSheet, tvNoItemsOrderBottomSheet;
    private Button btnSubmitOrder;
    private RecyclerView rvOrderBottomSheet;
    private BottomSheetBehavior behaviorBottomSheet;
    private ImageView ivArrowOrderBottomSheet;
    private View tintViewOrder;
    private String thingsEndpointId;
    private ProgressBar pbSubmitOrder;
    private boolean isNewOrder;
    private int total = 0;

    public FragmentOrder() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        String menuString = getArguments().getString(ORDER_FRAGMENT_MENU_ARGUMENTS_KEY);
        thingsEndpointId = getArguments().getString(THINGS_ENDPOINT_ID_ARGUMENTS_KEY);
        isNewOrder = getArguments().getBoolean(IS_NEW_ORDER_ARGUMENTS_KEY);

        menu = new ArrayList<>();
        selectedItems = new ArrayList<>();
        try {
            JSONArray menuJsonArray = new JSONArray(menuString);
            Gson gson = new Gson();
            for (int i = 0; i < menuJsonArray.length(); i++) {
                menu.add(gson.fromJson(menuJsonArray.getJSONObject(i).toString(), MenuItem.class));
            }
        } catch (JSONException e) {
            Log.w(MainActivity.TAG, e.getLocalizedMessage());
        }
        createMenuCategoriesList();

        TabLayout tlOrder = rootView.findViewById(R.id.tlOrder);
        ViewPager vpOrder = rootView.findViewById(R.id.vpOrder);

        vpOrder.setAdapter(new OrderTabsAdapter(getActivity().getSupportFragmentManager(), menu, categories, onMenuItemSelectListener));
        tlOrder.setupWithViewPager(vpOrder);

        ivArrowOrderBottomSheet = rootView.findViewById(R.id.ivArrowOrderBottomSheet);
        tvTotalOrderBottomSheet = rootView.findViewById(R.id.tvTotalOrderBottomSheet);
        tvNoItemsOrderBottomSheet = rootView.findViewById(R.id.tvNoItemsOrderBottomSheet);
        TextView tvDetailsHintOrderBottomSheet = rootView.findViewById(R.id.tvDetailsHintOrderBottomSheet);
        if (!isNewOrder)
            tvDetailsHintOrderBottomSheet.setText(getString(R.string.extra_items_bottom_sheet_title));
        RelativeLayout layoutOrderBottomSheet = rootView.findViewById(R.id.layoutOrderBottomSheet);
        tintViewOrder = rootView.findViewById(R.id.tintViewOrder);

        pbSubmitOrder = rootView.findViewById(R.id.pbSubmitOrder);

        btnSubmitOrder = rootView.findViewById(R.id.btnSubmitOrder);
        if (!isNewOrder)
            btnSubmitOrder.setText(getString(R.string.add_extra_items_bottom_sheet_button_text));
        btnSubmitOrder.setOnClickListener(view -> {  //Send order to Things and add to Firestore there
            btnSubmitOrder.setVisibility(View.GONE);
            pbSubmitOrder.setVisibility(View.VISIBLE);
            JSONArray jsonArrayItems = new JSONArray();
            for (MenuItem menuItem : selectedItems) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(FirestoreUtils.FIRESTORE_ID_FIELD, menuItem.getId());
                    jsonObject.put(FirestoreUtils.FIRESTORE_NAME_FIELD, menuItem.getName());
                    jsonObject.put(FirestoreUtils.FIRESTORE_CATEGORY_FIELD, menuItem.getCategory());
                    jsonObject.put(FirestoreUtils.FIRESTORE_DESCRIPTION_FIELD, menuItem.getDescription());
                    jsonObject.put(FirestoreUtils.FIRESTORE_PRICE_FIELD, menuItem.getPrice());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArrayItems.put(jsonObject);
            }
            Payload payload;
            if (isNewOrder) {
                payload = Payload.fromBytes(jsonArrayItems.toString().getBytes());
            } else {
                payload = Payload.fromBytes(("extraItems" + jsonArrayItems.toString()).getBytes());
            }
            Nearby.getConnectionsClient(getActivity().getApplicationContext())
                    .sendPayload(thingsEndpointId, payload)
                    .addOnSuccessListener(aVoid -> Log.i(MainActivity.TAG, "Order payload sent"))
                    .addOnFailureListener(e -> Log.w(MainActivity.TAG, "Order payload isn't sent: " + e.getLocalizedMessage()));
        });

        rvOrderBottomSheet = rootView.findViewById(R.id.rvOrderBottomSheet);
        rvOrderBottomSheet.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        rvOrderBottomSheet.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL));

        behaviorBottomSheet = BottomSheetBehavior.from(layoutOrderBottomSheet);

        behaviorBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        ivArrowOrderBottomSheet.setImageResource(R.drawable.ic_arrow_down);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        tintViewOrder.setVisibility(View.GONE);
                        ivArrowOrderBottomSheet.setImageResource(R.drawable.ic_arrow_up);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        tintViewOrder.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                tintViewOrder.setAlpha(slideOffset / 2);
            }
        });

        tintViewOrder.setOnClickListener(view -> behaviorBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED));

        return rootView;
    }

    private void createMenuCategoriesList() {
        categories = new ArrayList<>();

        for (MenuItem menuItem : menu) {
            if (!categories.contains(menuItem.getCategory()))
                categories.add(menuItem.getCategory());
        }
    }

    private final OnMenuItemSelectListener onMenuItemSelectListener = new OnMenuItemSelectListener() {
        @Override
        public void onMenuItemSelected(MenuItem item) {
            selectedItems.add(item);
            total += item.getPrice();
            tvTotalOrderBottomSheet.setText(getResources().getString(R.string.currency_label) + total);
            rvOrderBottomSheet.setAdapter(new SelectedMenuItemsAdapter(getActivity().getApplicationContext(), selectedItems, onMenuItemDeleteListener));

            tvNoItemsOrderBottomSheet.setVisibility(View.GONE);
            rvOrderBottomSheet.setVisibility(View.VISIBLE);
            btnSubmitOrder.setVisibility(View.VISIBLE);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

        }
    };

    private final OnMenuItemDeleteListener onMenuItemDeleteListener = new OnMenuItemDeleteListener() {
        @Override
        public void onMenuItemDeleted(MenuItem item) {
            selectedItems.remove(item);
            total -= item.getPrice();
            tvTotalOrderBottomSheet.setText(getResources().getString(R.string.currency_label) + total);
            rvOrderBottomSheet.setAdapter(new SelectedMenuItemsAdapter(getActivity().getApplicationContext(), selectedItems, onMenuItemDeleteListener));

            if (!selectedItems.isEmpty()) {
                tvNoItemsOrderBottomSheet.setVisibility(View.GONE);
                rvOrderBottomSheet.setVisibility(View.VISIBLE);
                btnSubmitOrder.setVisibility(View.VISIBLE);
            } else {
                tvNoItemsOrderBottomSheet.setVisibility(View.VISIBLE);
                rvOrderBottomSheet.setVisibility(View.GONE);
                btnSubmitOrder.setVisibility(View.GONE);
            }

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

        }
    };

    public interface OnMenuItemSelectListener extends Parcelable {
        void onMenuItemSelected(MenuItem item);
    }

    public interface OnMenuItemDeleteListener extends Parcelable {
        void onMenuItemDeleted(MenuItem item);
    }

}

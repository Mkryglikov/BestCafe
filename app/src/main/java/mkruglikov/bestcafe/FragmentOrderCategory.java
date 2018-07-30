package mkruglikov.bestcafe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.parceler.Parcels;

import java.util.List;

import mkruglikov.bestcafe.adapters.CategoryItemsAdapter;
import mkruglikov.bestcafe.models.MenuItem;

public class FragmentOrderCategory extends Fragment {

    public static final String FRAGMENT_ORDER_TAB_ITEMS_ARGUMENTS_KEY = "fragment_order_tab_items_arguments_key";
    public static final String ON_MENU_ITEM_SELECTED_LISTENER_ARGUMENTS_KEY = "on_menu_item_selected_arguments_key";

    private List<MenuItem> menuCategory;
    private RecyclerView rvOrderCategory;
    private FragmentOrder.OnMenuItemSelectListener onMenuItemSelectListener;

    public FragmentOrderCategory() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order_category, container, false);

        menuCategory = Parcels.unwrap(getArguments().getParcelable(FRAGMENT_ORDER_TAB_ITEMS_ARGUMENTS_KEY));
        onMenuItemSelectListener = getArguments().getParcelable(ON_MENU_ITEM_SELECTED_LISTENER_ARGUMENTS_KEY);

        rvOrderCategory = rootView.findViewById(R.id.rvOrderCategory);
        rvOrderCategory.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        rvOrderCategory.addItemDecoration(new DividerItemDecoration(rvOrderCategory.getContext(), DividerItemDecoration.VERTICAL));
        rvOrderCategory.setAdapter(new CategoryItemsAdapter(menuCategory, onMenuItemSelectListener));
        return rootView;
    }

}

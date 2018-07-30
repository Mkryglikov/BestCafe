package mkruglikov.bestcafe;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import mkruglikov.bestcafe.adapters.OrderTabsAdapter;
import mkruglikov.bestcafe.models.MenuItem;

public class FragmentOrder extends Fragment{

    public static final String ORDER_FRAGMENT_MENU_ARGUMENTS_KEY = "order_fragment_menu_arguments_key";

    private List<MenuItem> menu;
    private List<String> categories;
    private TabLayout tlOrder;
    private ViewPager vpOrder;

    public FragmentOrder() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        String menuString = getArguments().getString(ORDER_FRAGMENT_MENU_ARGUMENTS_KEY);
        menu = new ArrayList<>();
        try {
            JSONArray menuJsonArray = new JSONArray(menuString);
            Gson gson = new Gson();
            for (int i = 0; i < menuJsonArray.length(); i++) {
                menu.add(gson.fromJson(menuJsonArray.getJSONObject(i).toString(), MenuItem.class));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        createMenuCategoriesList();

        tlOrder = rootView.findViewById(R.id.tlOrder);
        vpOrder = rootView.findViewById(R.id.vpOrder);

        vpOrder.setAdapter(new OrderTabsAdapter(getActivity().getSupportFragmentManager(), menu, categories));
        tlOrder.setupWithViewPager(vpOrder);

        return rootView;
    }

    private void createMenuCategoriesList() {
        categories = new ArrayList<>();

        for (MenuItem menuItem : menu) {
            if (!categories.contains(menuItem.getCategory()))
                categories.add(menuItem.getCategory());
        }
    }
}

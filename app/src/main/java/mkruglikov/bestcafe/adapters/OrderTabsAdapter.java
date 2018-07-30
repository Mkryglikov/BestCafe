package mkruglikov.bestcafe.adapters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import mkruglikov.bestcafe.FragmentOrder;
import mkruglikov.bestcafe.FragmentOrderCategory;
import mkruglikov.bestcafe.models.MenuItem;

public class OrderTabsAdapter extends FragmentPagerAdapter {
    private List<MenuItem> menu;
    private List<String> categories;
    private FragmentOrder.OnMenuItemSelectListener onMenuItemSelectListener;

    public OrderTabsAdapter(FragmentManager fm, List<MenuItem> menu, List<String> categories, FragmentOrder.OnMenuItemSelectListener onMenuItemSelectListener) {
        super(fm);
        this.menu = menu;
        this.categories = categories;
        this.onMenuItemSelectListener = onMenuItemSelectListener;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return categories.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        String category = categories.get(position);
        FragmentOrderCategory fragmentOrderTab = new FragmentOrderCategory();
        Bundle args = new Bundle();
        List<MenuItem> itemsForCategory = new ArrayList<>();

        for (MenuItem menuItem : menu) {
            if (menuItem.getCategory().equals(category))
                itemsForCategory.add(menuItem);
        }
        args.putParcelable(FragmentOrderCategory.FRAGMENT_ORDER_TAB_ITEMS_ARGUMENTS_KEY, Parcels.wrap(itemsForCategory));

        args.putParcelable(FragmentOrderCategory.ON_MENU_ITEM_SELECTED_LISTENER_ARGUMENTS_KEY, onMenuItemSelectListener);
        fragmentOrderTab.setArguments(args);
        return fragmentOrderTab;
    }

    @Override
    public int getCount() {
        return categories.size();
    }
}

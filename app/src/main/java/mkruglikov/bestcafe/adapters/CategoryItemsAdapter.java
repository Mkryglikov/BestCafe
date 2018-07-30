package mkruglikov.bestcafe.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import mkruglikov.bestcafe.FragmentOrder;
import mkruglikov.bestcafe.R;
import mkruglikov.bestcafe.models.MenuItem;

public class CategoryItemsAdapter extends RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder> {

    private List<MenuItem> menuCategory;
    private FragmentOrder.OnMenuItemSelectListener onMenuItemSelectListener;

    public CategoryItemsAdapter(List<MenuItem> menuCategory, FragmentOrder.OnMenuItemSelectListener onMenuItemSelectListener) {
        this.menuCategory = menuCategory;
        this.onMenuItemSelectListener = onMenuItemSelectListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMenuItemName, tvMenuItemDescription;
        private final Button btnMenuItemSelect;

        ViewHolder(View itemView) {
            super(itemView);
            tvMenuItemName = itemView.findViewById(R.id.tvMenuItemName);
            tvMenuItemDescription = itemView.findViewById(R.id.tvMenuItemDescription);
            btnMenuItemSelect = itemView.findViewById(R.id.btnMenuItemSelect);
        }
    }

    @NonNull
    @Override
    public CategoryItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryItemsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemsAdapter.ViewHolder holder, int position) {
        final MenuItem menuItem = menuCategory.get(position);
        holder.tvMenuItemName.setText(menuItem.getName());
        String description = menuItem.getDescription();
        if (description != null && !description.isEmpty())
            holder.tvMenuItemDescription.setText(description);
        holder.btnMenuItemSelect.setText(menuItem.getPrice() + "$");
        holder.btnMenuItemSelect.setOnClickListener(view -> onMenuItemSelectListener.onMenuItemSelected(menuItem));

    }

    @Override
    public int getItemCount() {
        return menuCategory.size();
    }
}

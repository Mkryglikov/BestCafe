package mkruglikov.bestcafe.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import mkruglikov.bestcafe.FragmentOrder;
import mkruglikov.bestcafe.R;
import mkruglikov.bestcafe.models.MenuItem;

public class SelectedMenuItemsAdapter extends RecyclerView.Adapter<SelectedMenuItemsAdapter.ViewHolder> {

    private List<MenuItem> items;
    private FragmentOrder.OnMenuItemDeleteListener onMenuItemDeleteListener;

    public SelectedMenuItemsAdapter(List<MenuItem> items, FragmentOrder.OnMenuItemDeleteListener onMenuItemDeleteListener) {
        this.items = items;
        this.onMenuItemDeleteListener = onMenuItemDeleteListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSelectedMenuItemName, tvSelectedMenuItemPrice;
        private final ImageButton btnDeleteSelectedMenuItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvSelectedMenuItemName = itemView.findViewById(R.id.tvSelectedMenuItemName);
            tvSelectedMenuItemPrice = itemView.findViewById(R.id.tvSelectedMenuItemPrice);
            btnDeleteSelectedMenuItem = itemView.findViewById(R.id.btnDeleteSelectedMenuItem);
        }
    }

    @NonNull
    @Override
    public SelectedMenuItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectedMenuItemsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedMenuItemsAdapter.ViewHolder holder, int position) {
        MenuItem item = items.get(position);
        holder.tvSelectedMenuItemName.setText(item.getName());
        holder.tvSelectedMenuItemPrice.setText("$" + String.valueOf(item.getPrice()));
        holder.btnDeleteSelectedMenuItem.setOnClickListener(view -> onMenuItemDeleteListener.onMenuItemDeleted(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

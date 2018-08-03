package mkruglikov.bestcafe.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import mkruglikov.bestcafe.FragmentOrder;
import mkruglikov.bestcafe.R;
import mkruglikov.bestcafe.models.MenuItem;

public class SelectedMenuItemsAdapter extends RecyclerView.Adapter<SelectedMenuItemsAdapter.ViewHolder> {

    private final List<MenuItem> items;
    private final FragmentOrder.OnMenuItemDeleteListener onMenuItemDeleteListener;
    private final Context context;

    public SelectedMenuItemsAdapter(Context context, List<MenuItem> items, FragmentOrder.OnMenuItemDeleteListener onMenuItemDeleteListener) {
        this.context = context;
        this.items = items;
        this.onMenuItemDeleteListener = onMenuItemDeleteListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSelectedMenuItemName, tvSelectedMenuItemPrice;
        private final ImageButton btnDeleteSelectedMenuItem;
        private final ConstraintLayout layoutSelectedItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvSelectedMenuItemName = itemView.findViewById(R.id.tvSelectedMenuItemName);
            tvSelectedMenuItemPrice = itemView.findViewById(R.id.tvSelectedMenuItemPrice);
            btnDeleteSelectedMenuItem = itemView.findViewById(R.id.btnDeleteSelectedMenuItem);
            layoutSelectedItem = itemView.findViewById(R.id.layoutSelectedItem);
        }
    }

    @NonNull
    @Override
    public SelectedMenuItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectedMenuItemsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedMenuItemsAdapter.ViewHolder holder, int position) {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        MenuItem item = items.get(position);
        holder.tvSelectedMenuItemName.setText(item.getName());
        holder.tvSelectedMenuItemPrice.setText(context.getString(R.string.currency_label) + String.valueOf(item.getPrice()));
        holder.btnDeleteSelectedMenuItem.setOnClickListener(view -> {
            holder.layoutSelectedItem.startAnimation(fadeOutAnimation);
            new Handler().postDelayed(() -> onMenuItemDeleteListener.onMenuItemDeleted(item), 150);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

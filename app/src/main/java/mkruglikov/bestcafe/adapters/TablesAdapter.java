package mkruglikov.bestcafe.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import mkruglikov.bestcafe.R;
import mkruglikov.bestcafe.models.Table;

public class TablesAdapter extends RecyclerView.Adapter<TablesAdapter.ViewHolder> {

    private final List<Table> tables;
    private final OnSelectTableToConnectListener onSelectTableToConnectListener;

    public TablesAdapter(List<Table> tables, OnSelectTableToConnectListener onSelectTableToConnectListener) {
        this.tables = tables;
        this.onSelectTableToConnectListener = onSelectTableToConnectListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvConnectTableName;
        private final Button btnConnectingConnectToTable;

        ViewHolder(View itemView) {
            super(itemView);
            tvConnectTableName = itemView.findViewById(R.id.tvConnectTableName);
            btnConnectingConnectToTable = itemView.findViewById(R.id.btnConnectingConnectToTable);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_to_connect, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Table table = tables.get(position);
        holder.tvConnectTableName.setText(table.getName());
        holder.btnConnectingConnectToTable.setOnClickListener(view -> onSelectTableToConnectListener.onTableSelected(table.getId(), table.getName()));
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    public interface OnSelectTableToConnectListener {
        void onTableSelected(String tableId, String tableName);
    }

}

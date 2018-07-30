package mkruglikov.bestcafe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.List;

import mkruglikov.bestcafe.ConnectActivity.ConnectionLifecycleCallback;
import mkruglikov.bestcafe.adapters.TablesAdapter;
import mkruglikov.bestcafe.models.Table;

public class FragmentConnectConnecting extends Fragment {

    public static final String CONNECTION_LIFECYCLE_CALLBACK_ARGUMENTS_KEY = "connection_lifecycle_callback_arguments_key";

    private TextView tvConnectingHint;
    private ProgressBar pbConnecting;
    private RecyclerView rvTablesConnect;
    private List<Table> availableTables;
    private ConnectionsClient nearbyConnectionsClient;
    private ConnectionLifecycleCallback connectionLifecycleCallback;

    public FragmentConnectConnecting() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connect_connecting, container, false);

        tvConnectingHint = rootView.findViewById(R.id.tvConnectingHint);
        pbConnecting = rootView.findViewById(R.id.pbConnecting);

        rvTablesConnect = rootView.findViewById(R.id.rvTablesConnect);
        rvTablesConnect.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        rvTablesConnect.addItemDecoration(new DividerItemDecoration(rvTablesConnect.getContext(), DividerItemDecoration.VERTICAL));

        connectionLifecycleCallback = getArguments().getParcelable(CONNECTION_LIFECYCLE_CALLBACK_ARGUMENTS_KEY);
        nearbyConnectionsClient = Nearby.getConnectionsClient(getActivity().getApplicationContext());
        startDiscovery();
        return rootView;
    }

    private void startDiscovery() {
        nearbyConnectionsClient.startDiscovery(
                BuildConfig.NearbyServiceId,
                endpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(
                        unusedResult -> availableTables = new ArrayList<>())
                .addOnFailureListener(e -> {
                    Log.w(MainActivity.TAG, "App unable to discover: " + e.getLocalizedMessage());
                    tvConnectingHint.setText("App is unable to discover tables");
                    pbConnecting.setVisibility(View.INVISIBLE);
                });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, final DiscoveredEndpointInfo discoveredEndpointInfo) {
            availableTables.add(new Table(endpointId, discoveredEndpointInfo.getEndpointName()));
            rvTablesConnect.setAdapter(new TablesAdapter(availableTables, onSelectTableToConnectListener));
        }

        @Override
        public void onEndpointLost(String endpointId) {
            for (Table table : availableTables) {
                if (table.getId().equals(endpointId)) {
                    availableTables.remove(table);
                    break;
                }
            }
            rvTablesConnect.setAdapter(new TablesAdapter(availableTables, onSelectTableToConnectListener));
        }
    };

    @SuppressLint("MissingPermission")
    private TablesAdapter.OnSelectTableToConnectListener onSelectTableToConnectListener = (tableId, tableName) -> {
        rvTablesConnect.setVisibility(View.INVISIBLE);
        tvConnectingHint.setText("Connecting to " + tableName);
        nearbyConnectionsClient.requestConnection(((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(), tableId, connectionLifecycleCallback);
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        nearbyConnectionsClient.stopDiscovery();
    }

}

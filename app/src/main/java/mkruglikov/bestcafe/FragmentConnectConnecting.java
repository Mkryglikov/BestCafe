package mkruglikov.bestcafe;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import mkruglikov.bestcafe.adapters.TablesAdapter;
import mkruglikov.bestcafe.models.Table;

import static mkruglikov.bestcafe.MainActivity.TAG;

public class FragmentConnectConnecting extends Fragment {

    private TextView tvConnectingHint;
    private ProgressBar pbConnecting;
    private RecyclerView rvTablesConnect;
    private List<Table> availableTables;

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
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startDiscovery();
    }

    private void startDiscovery() {
        Log.i(TAG, "startDiscovery");
        Nearby.getConnectionsClient(getActivity().getApplicationContext()).startDiscovery(
                BuildConfig.NearbyServiceId,
                endpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're discovering!
                                Log.i(TAG, "App discovering");
                                availableTables = new ArrayList<>();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "App unable to discover: " + e.getLocalizedMessage());
                                tvConnectingHint.setText("App is unable to discover tables");
                                pbConnecting.setVisibility(View.INVISIBLE);
                            }
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
            // A previously discovered endpoint has gone away.
            for (Table table : availableTables) {
                if (table.getId().equals(endpointId)) {
                    availableTables.remove(table);
                    break;
                }
            }
            rvTablesConnect.setAdapter(new TablesAdapter(availableTables, onSelectTableToConnectListener));
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {

        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            // Automatically accept the connection on both sides.
            Nearby.getConnectionsClient(getActivity().getApplicationContext()).acceptConnection(endpointId, new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {

                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                }
            });
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    // The connection broke before it was able to be accepted.
                    break;
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
        }
    };

    private TablesAdapter.OnSelectTableToConnectListener onSelectTableToConnectListener = new TablesAdapter.OnSelectTableToConnectListener() {
        @Override
        public void onTableSelected(final String tableId, final String tableName) {
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "NO PERMISSIONS");
                return;
            }
            Nearby.getConnectionsClient(getActivity().getApplicationContext()).requestConnection(((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(),
                    tableId,
                    connectionLifecycleCallback)
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unusedResult) {
                                    // We successfully requested a connection. Now both sides
                                    // must accept before the connection is established.
                                    Toast.makeText(getActivity().getApplicationContext(), tableName, Toast.LENGTH_LONG).show();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Nearby Connections failed to request the connection.
                                }
                            });
        }
    };
}

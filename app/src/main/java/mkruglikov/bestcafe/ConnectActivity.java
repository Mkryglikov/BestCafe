package mkruglikov.bestcafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.euzee.permission.CallbackBuilder;
import com.github.euzee.permission.PermissionUtil;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.UnsupportedEncodingException;

@SuppressLint("ParcelCreator")
public class ConnectActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private ConnectionsClient nearbyConnectionsClient;
    private String endpointId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        PermissionUtil.checkGroup(this, new CallbackBuilder()
                .onGranted(() -> {
                    FragmentConnect fragmentConnect = new FragmentConnect();
                    Bundle args = new Bundle();
                    args.putParcelable(FragmentConnect.CONNECTION_LIFECYCLE_CALLBACK_ARGUMENTS_KEY, new ConnectionLifecycleCallback());
                    fragmentConnect.setArguments(args);
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.containerConnect, fragmentConnect)
                            .commit();
                    nearbyConnectionsClient = Nearby.getConnectionsClient(this);
                })
                .build(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE});
    }

    public class ConnectionLifecycleCallback extends com.google.android.gms.nearby.connection.ConnectionLifecycleCallback implements Parcelable {
        String orderId, estimatedTime;

        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            nearbyConnectionsClient.stopDiscovery();
            nearbyConnectionsClient.stopAdvertising();
            nearbyConnectionsClient.acceptConnection(endpointId, new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                    String payloadString;
                    try {
                        payloadString = new String(payload.asBytes(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e(MainActivity.TAG, "Error converting payload to String: " + e.getLocalizedMessage());
                        return;
                    }

                    if (payloadString.substring(0, 4).equals("menu")) { //There is a menu JSON array. Create order fragment and send menu there
                        payloadString = payloadString.substring(4);
                        FragmentOrder fragmentOrder = new FragmentOrder();
                        Bundle args = new Bundle();
                        args.putString(FragmentOrder.ORDER_FRAGMENT_MENU_ARGUMENTS_KEY, payloadString);
                        args.putString(FragmentOrder.THINGS_ENDPOINT_ID_ARGUMENTS_KEY, endpointId);
                        args.putBoolean(FragmentOrder.IS_NEW_ORDER_ARGUMENTS_KEY, true);
                        fragmentOrder.setArguments(args);
                        fragmentManager.beginTransaction()
                                .replace(R.id.containerConnect, fragmentOrder)
                                .commit();
                    } else if (payloadString.substring(0, 7).equals("orderId")) { //There is an orderId. It means table is already active and tries to reconnect or client just made an order.
                        orderId = payloadString.substring(7);
                        Log.i(MainActivity.TAG, "Order ID received: " + orderId);

                        //Wait for estimated time
                        new Handler().postDelayed(() -> {
                            //If there is no estimated time, it means that table is already active, tries to reconnect and order status is EATS.
                            if (estimatedTime == null || estimatedTime.isEmpty()) {
                                Intent activeOrderActivityIntent = new Intent(ConnectActivity.this, ActiveOrderActivity.class);
                                activeOrderActivityIntent.putExtra(ActiveOrderActivity.ACTIVE_ORDER_ACTIVITY_ENDPOINT_ID_EXTRA_KEY, endpointId);
                                if (nearbyConnectionsClient != null) {
                                    Log.i(MainActivity.TAG, "Disconnecting by connect Activity");
                                    nearbyConnectionsClient.disconnectFromEndpoint(endpointId);
                                    nearbyConnectionsClient.stopDiscovery();
                                    nearbyConnectionsClient.stopAdvertising();
                                    nearbyConnectionsClient = null;
                                }
                                startActivity(activeOrderActivityIntent);
                                finish();
                            }
                        }, 1000);
                    } else if (payloadString.substring(0, 13).equals("estimatedTime")) { //There is an estimated time. It means table is already active, tries to reconnect and order status is PREPARING or client just made an order.
                        estimatedTime = payloadString.substring(13);
                        Log.i(MainActivity.TAG, "Estimated time received: " + estimatedTime);

                        Intent activeOrderActivityIntent = new Intent(ConnectActivity.this, ActiveOrderActivity.class);
                        activeOrderActivityIntent.putExtra(ActiveOrderActivity.ACTIVE_ORDER_ACTIVITY_ENDPOINT_ID_EXTRA_KEY, endpointId);
                        if (nearbyConnectionsClient != null) {
                            Log.i(MainActivity.TAG, "Disconnecting by connect Activity");
                            nearbyConnectionsClient.disconnectFromEndpoint(endpointId);
                            nearbyConnectionsClient.stopDiscovery();
                            nearbyConnectionsClient.stopAdvertising();
                            nearbyConnectionsClient = null;
                        }
                        startActivity(activeOrderActivityIntent);
                        finish();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                }
            });
        }

        @Override
        public void onConnectionResult(@NonNull String id, @NonNull ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    endpointId = id;
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    break;
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

        }
    }

    @Override
    protected void onDestroy() {
        //Have to disconnect there and connect again in ActiveOrderActivity since we want to get payload in there but can't get current connection or update existing PayloadCallback
        if (nearbyConnectionsClient != null) {
            Log.i(MainActivity.TAG, "Disconnecting by connect Activity");
            nearbyConnectionsClient.disconnectFromEndpoint(endpointId);
            nearbyConnectionsClient.stopDiscovery();
            nearbyConnectionsClient.stopAdvertising();
        }

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();

        super.onDestroy();
    }
}

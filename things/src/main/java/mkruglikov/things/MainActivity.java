package mkruglikov.things;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    public static final String TAG = "FUCK";
    public static final int TABLE_NUMBER = 5;
    private ConnectionsClient nearbyConnectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nearbyConnectionsClient = Nearby.getConnectionsClient(this);
        startAdvertising();
    }

    private void startAdvertising() {
        nearbyConnectionsClient.startAdvertising(
                "Table #" + String.valueOf(TABLE_NUMBER),
                BuildConfig.NearbyServiceId,
                connectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR));
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {

        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {

            nearbyConnectionsClient.acceptConnection(endpointId, new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {

                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                }
            });
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    nearbyConnectionsClient.stopAdvertising();
                    FirestoreUtils.getMenu((menu, exceptionMessage) -> {
                        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                            //todo
                            Log.w(TAG, "Error getting menu: " + exceptionMessage);
                            return;
                        }
                        JSONArray jsonArrayMenu = new JSONArray();
                        for (MenuItem menuItem : menu) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put(FirestoreUtils.FIRESTORE_NAME_FIELD, menuItem.getName());
                                jsonObject.put(FirestoreUtils.FIRESTORE_CATEGORY_FIELD, menuItem.getCategory());
                                jsonObject.put(FirestoreUtils.FIRESTORE_DESCRIPTION_FIELD, menuItem.getDescription());
                                jsonObject.put(FirestoreUtils.FIRESTORE_PRICE_FIELD, menuItem.getPrice());
                                jsonObject.put(FirestoreUtils.FIRESTORE_IS_AVAILABLE_FIELD, menuItem.getIsAvailable());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            jsonArrayMenu.put(jsonObject);
                        }
                        Payload payload = Payload.fromBytes(jsonArrayMenu.toString().getBytes());
                        nearbyConnectionsClient.sendPayload(endpointId, payload)
                                .addOnFailureListener(e -> Log.w(TAG, "payload isn't sent: " + e.getLocalizedMessage()));
                    });
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            startAdvertising();
        }
    };
}

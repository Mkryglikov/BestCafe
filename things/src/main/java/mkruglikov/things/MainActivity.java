package mkruglikov.things;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
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
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    public static final String TAG = "FUCK";
    public static final String LAST_CONNECTED_ENDPOINT_NAME = "last_connected_endpoint_name";
    public static final String MODULE_STATUS_ACTIVE = "module_status_active";
    public static final String MODULE_STATUS_INACTIVE = "module_status_inactive";
    public static final int TABLE_NUMBER = 5;

    private ConnectionsClient nearbyConnectionsClient;
    private SharedPreferences sharedPreferences;
    private String currentOrderStatus, currentModuleStatus;
    private String lastEndpointName, orderId, orderStatus;
    private int itemsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.setName("Table #" + TABLE_NUMBER);

        nearbyConnectionsClient = Nearby.getConnectionsClient(this);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        FirestoreUtils.checkIsTableActive(TABLE_NUMBER, onCheckIsTableActiveListener);
        FirestoreUtils.getIsActiveRealtimeUpdates(isActive -> {
            if (isActive) {
                if (currentModuleStatus != null && currentModuleStatus.equals(MODULE_STATUS_INACTIVE)) {
                    currentModuleStatus = MODULE_STATUS_ACTIVE;
                    nearbyConnectionsClient.stopAdvertising();
                    connectToLastEndpoint(lastEndpointName, orderId, orderStatus, itemsCount);
                }
            } else if (currentModuleStatus != null && currentModuleStatus.equals(MODULE_STATUS_ACTIVE)) {
                currentModuleStatus = MODULE_STATUS_INACTIVE;
                nearbyConnectionsClient.stopDiscovery();
                startAdvertising();
            }
        });
    }


    private void startAdvertising() {
        nearbyConnectionsClient.startAdvertising(
                "Table #" + String.valueOf(TABLE_NUMBER),
                BuildConfig.NearbyServiceId,
                connectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_CLUSTER));
        Log.i(TAG, "Advertising");
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {

        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            Log.i(TAG, "Connection initiated by " + connectionInfo.getEndpointName());
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putString(LAST_CONNECTED_ENDPOINT_NAME, connectionInfo.getEndpointName());
            ed.apply();

            nearbyConnectionsClient.acceptConnection(endpointId, new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    try {
                        String jsonSelectedItems = new String(payload.asBytes(), "UTF-8");
                        List<String> selectedItems = new ArrayList<>();

                        Log.i(TAG, "jsonSelectedItems: " + jsonSelectedItems);

                        JSONArray menuJsonArray = new JSONArray(jsonSelectedItems);
                        Gson gson = new Gson();
                        for (int i = 0; i < menuJsonArray.length(); i++) {
                            MenuItem item = gson.fromJson(menuJsonArray.getJSONObject(i).toString(), MenuItem.class);
                            Log.i(TAG, "Item ID: " + item.getId());
                            selectedItems.add(item.getId());
                        }

                        Map<String, Object> orderMap = new HashMap<>();

                        orderMap.put(FirestoreUtils.FIRESTORE_TABLE_FIELD, TABLE_NUMBER);
                        orderMap.put(FirestoreUtils.FIRESTORE_STATUS_FIELD, FirestoreUtils.FIRESTORE_STATUS_PREPARING);
                        orderMap.put(FirestoreUtils.FIRESTORE_ITEMS_FIELD, selectedItems);

                        FirestoreUtils.addOrder(orderMap, (orderId, exceptionMessage) -> {
                            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                                Log.w(TAG, "Error adding order: " + exceptionMessage);
                            } else {
                                Payload payloadOrderId = Payload.fromBytes(("orderId" + orderId).getBytes());
                                nearbyConnectionsClient.sendPayload(endpointId, payloadOrderId)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.i(TAG, "Order ID sent");
                                            setRealtimeOrderUpdates(endpointId, orderId);
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Order ID isn't sent: " + e.getLocalizedMessage()));

                                Payload payloadEstimatedTime = Payload.fromBytes(("estimatedTime" + getCookingTime(selectedItems.size())).getBytes());
                                nearbyConnectionsClient.sendPayload(endpointId, payloadEstimatedTime)
                                        .addOnSuccessListener(aVoid -> Log.i(TAG, "Estimated time sent"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Estimated time isn't sent: " + e.getLocalizedMessage()));


                            }
                            finish();
                        });
                    } catch (UnsupportedEncodingException | JSONException e) {
                        Log.w(TAG, e.getLocalizedMessage());
                    }
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
                    Log.i(TAG, "Connected");
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
                                jsonObject.put(FirestoreUtils.FIRESTORE_ID_FIELD, menuItem.getId());
                                jsonObject.put(FirestoreUtils.FIRESTORE_NAME_FIELD, menuItem.getName());
                                jsonObject.put(FirestoreUtils.FIRESTORE_CATEGORY_FIELD, menuItem.getCategory());
                                jsonObject.put(FirestoreUtils.FIRESTORE_DESCRIPTION_FIELD, menuItem.getDescription());
                                jsonObject.put(FirestoreUtils.FIRESTORE_PRICE_FIELD, menuItem.getPrice());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            jsonArrayMenu.put(jsonObject);
                        }
                        Payload payloadMenu = Payload.fromBytes(("menu" + jsonArrayMenu.toString()).getBytes());
                        nearbyConnectionsClient.sendPayload(endpointId, payloadMenu)
                                .addOnSuccessListener(aVoid -> Log.i(TAG, "Menu sent"))
                                .addOnFailureListener(e -> Log.w(TAG, "Menu isn't sent: " + e.getLocalizedMessage()));
                    });
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.i(TAG, "Disconnected");
            FirestoreUtils.checkIsTableActive(TABLE_NUMBER, onCheckIsTableActiveListener);
        }
    };

    private int getCookingTime(int itemsCount) {
        //5 minutes of cooking for every selected item. For real cafe it can be added to every item in menu and added
        return itemsCount * 5;
    }

    private FirestoreUtils.OnCheckIsTableActiveListener onCheckIsTableActiveListener = new FirestoreUtils.OnCheckIsTableActiveListener() {
        @Override
        public void onTableChecked(boolean isActive, String exceptionMessage, String oId, String oStatus, int iCount) {
            String eName = sharedPreferences.getString(LAST_CONNECTED_ENDPOINT_NAME, null);
            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                Log.w(TAG, "Can't check if table is active: " + exceptionMessage);
            } else if (isActive && eName != null && !eName.isEmpty()) {
                currentModuleStatus = MODULE_STATUS_ACTIVE;
                lastEndpointName = eName;
                orderId = oId;
                orderStatus = oStatus;
                itemsCount = iCount;
                connectToLastEndpoint(eName, oId, oStatus, iCount);
            } else {
                currentModuleStatus = MODULE_STATUS_INACTIVE;
                startAdvertising();
            }
        }
    };

    private void connectToLastEndpoint(String eName, String oId, String oStatus, int iCount) {
        nearbyConnectionsClient.startDiscovery(
                BuildConfig.NearbyServiceId,
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                        if (discoveredEndpointInfo.getEndpointName().equals(eName)) {
                            nearbyConnectionsClient.requestConnection(String.valueOf(TABLE_NUMBER), endpointId, new ConnectionLifecycleCallback() {
                                @Override
                                public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
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
                                public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                                    switch (result.getStatus().getStatusCode()) {
                                        case ConnectionsStatusCodes.STATUS_OK:
                                            nearbyConnectionsClient.stopDiscovery();

                                            Payload payloadOrderId = Payload.fromBytes(("orderId" + oId).getBytes());
                                            nearbyConnectionsClient.sendPayload(endpointId, payloadOrderId)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.i(TAG, "Order ID sent");
                                                        setRealtimeOrderUpdates(endpointId, oId);
                                                    })
                                                    .addOnFailureListener(e -> Log.w(TAG, "Order ID isn't sent: " + e.getLocalizedMessage()));
                                            if (oStatus.equals(FirestoreUtils.FIRESTORE_STATUS_PREPARING)) {
                                                Payload payloadEstimatedTime = Payload.fromBytes(("estimatedTime" + getCookingTime(iCount)).getBytes());
                                                nearbyConnectionsClient.sendPayload(endpointId, payloadEstimatedTime)
                                                        .addOnSuccessListener(aVoid -> Log.i(TAG, "Estimated time sent"))
                                                        .addOnFailureListener(e -> Log.w(TAG, "Estimated time isn't sent: " + e.getLocalizedMessage()));
                                            }
                                            break;
                                    }
                                }

                                @Override
                                public void onDisconnected(@NonNull String endpointId) {
                                    connectionLifecycleCallback.onDisconnected(endpointId);
                                }
                            });
                        }

                    }

                    @Override
                    public void onEndpointLost(@NonNull String endpointId) {

                    }
                },
                new DiscoveryOptions(Strategy.P2P_CLUSTER)
        );
        Log.i(TAG, "Table is active. Discovering for " + eName);
    }

    private void setRealtimeOrderUpdates(String endpointId, String orderId) {
        FirestoreUtils.getOrderRealtimeUpdates(orderId, (document, e) -> {
            if (e != null) {
                Log.w(MainActivity.TAG, "Realtime order updates error: " + e.getLocalizedMessage());
                return;
            }
            if (document == null || !document.exists()) {
                Log.w(MainActivity.TAG, "Realtime order updates error: document is null");
                return;
            }
            String newOrderStatus = document.getString(FirestoreUtils.FIRESTORE_STATUS_FIELD);
            if (newOrderStatus.equals(currentOrderStatus)) {
                return;
            }
            currentOrderStatus = newOrderStatus;
            switch (newOrderStatus) {
                case (FirestoreUtils.FIRESTORE_STATUS_PREPARING):
                    Payload payloadOrderStatusPreparing = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_PREPARING).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadOrderStatusPreparing)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                            .addOnFailureListener(ePrep -> Log.w(TAG, "Order updates isn't sent: " + ePrep.getLocalizedMessage()));
                    break;
                case (FirestoreUtils.FIRESTORE_STATUS_EATS):
                    Payload payloadOrderStatusEats = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_EATS).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadOrderStatusEats)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                            .addOnFailureListener(eEats -> Log.w(TAG, "Order updates isn't sent: " + eEats.getLocalizedMessage()));
                    break;
                case (FirestoreUtils.FIRESTORE_STATUS_DONE):
                    Payload payloadOrderStatusDone = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_DONE).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadOrderStatusDone)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                            .addOnFailureListener(eDone -> Log.w(TAG, "Order updates isn't sent: " + eDone.getLocalizedMessage()));
                    break;
            }
        });
    }


}

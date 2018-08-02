package mkruglikov.things;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnFailureListener;
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
    public static final int TABLE_NUMBER = 5;

    private ConnectionsClient nearbyConnectionsClient;
    private SharedPreferences sharedPreferences;
    private String currentOrderStatus;
    private boolean currentWaiterStatus;
    private String currentOrderId, currentEndpointId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.setName("Table #" + TABLE_NUMBER);

        nearbyConnectionsClient = Nearby.getConnectionsClient(this);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        FirestoreUtils.checkIsTableActive(TABLE_NUMBER, onCheckIsTableActiveListener);
    }

    private void startAdvertising() {
        nearbyConnectionsClient.stopDiscovery();
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
                    Log.i(TAG, "Payload Received");
                    try {
                        String payloadString = new String(payload.asBytes(), "UTF-8");
                        List<String> selectedItems = new ArrayList<>();
                        int total = 0;

                        Log.i(TAG, "jsonSelectedItems: " + payloadString);

                        JSONArray menuJsonArray = new JSONArray(payloadString);
                        Gson gson = new Gson();
                        for (int i = 0; i < menuJsonArray.length(); i++) {
                            MenuItem item = gson.fromJson(menuJsonArray.getJSONObject(i).toString(), MenuItem.class);
                            Log.i(TAG, "Item ID: " + item.getId());
                            selectedItems.add(item.getId());
                            total += item.getPrice();
                        }
                        Map<String, Object> orderMap = new HashMap<>();
                        orderMap.put(FirestoreUtils.FIRESTORE_TABLE_FIELD, TABLE_NUMBER);
                        orderMap.put(FirestoreUtils.FIRESTORE_STATUS_FIELD, FirestoreUtils.FIRESTORE_STATUS_PREPARING);
                        orderMap.put(FirestoreUtils.FIRESTORE_ITEMS_FIELD, selectedItems);
                        orderMap.put(FirestoreUtils.FIRESTORE_CALL_WAITER_FIELD, false);
                        orderMap.put(FirestoreUtils.FIRESTORE_TOTAL_FIELD, total);

                        FirestoreUtils.addOrder(orderMap, (orderId, exceptionMessage) -> {
                            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                                Log.w(TAG, "Error adding order: " + exceptionMessage);
                                return;
                            }

                            Payload payloadOrderId = Payload.fromBytes(("orderId" + orderId).getBytes());
                            nearbyConnectionsClient.sendPayload(endpointId, payloadOrderId)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.i(TAG, "Order ID sent 1: " + orderId);
                                        currentOrderId = orderId;
                                        setRealtimeOrderUpdates(endpointId, orderId);
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "Order ID isn't sent: " + e.getLocalizedMessage()));

                            Payload payloadEstimatedTime = Payload.fromBytes(("estimatedTime" + getCookingTime(selectedItems.size())).getBytes());
                            nearbyConnectionsClient.sendPayload(endpointId, payloadEstimatedTime)
                                    .addOnSuccessListener(aVoid -> Log.i(TAG, "Estimated time sent"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Estimated time isn't sent: " + e.getLocalizedMessage()));


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
                    nearbyConnectionsClient.stopDiscovery();
                    FirestoreUtils.getMenu((menu, exceptionMessage) -> {
                        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                            //TODO
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
                    currentOrderId = oId;
                connectToLastEndpoint(eName, currentOrderId, oStatus, iCount);
            } else {
                startAdvertising();
            }
        }
    };

    private void connectToLastEndpoint(String eName, String oId, String oStatus, int iCount) {
        if (eName == null) {
            eName = sharedPreferences.getString(LAST_CONNECTED_ENDPOINT_NAME, null);
        }
        String finalEName = eName;
        nearbyConnectionsClient.startDiscovery(
                BuildConfig.NearbyServiceId,
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                        currentEndpointId = endpointId;
                        Log.i(TAG, finalEName + ":" + discoveredEndpointInfo.getEndpointName());
                        if (discoveredEndpointInfo.getEndpointName().equals(finalEName)) {
                            Log.i(TAG, "onEndpointFound: equal names");
                            nearbyConnectionsClient.requestConnection(String.valueOf(TABLE_NUMBER), endpointId, new ConnectionLifecycleCallback() {
                                @Override
                                public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                                    Log.i(TAG, "Connection Initiated");
                                    nearbyConnectionsClient.acceptConnection(endpointId, new PayloadCallback() {
                                        @Override
                                        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                                            try {
                                                String payloadString = new String(payload.asBytes(), "UTF-8");
                                                if (payloadString.length() >= 13 && payloadString.substring(0, 13).equals("callTheWaiter")) {
                                                    Log.i(TAG, "callTheWaiter PayloadReceived");
                                                    FirestoreUtils.callTheWaiter(payloadString.substring(13));
                                                } else if (payloadString.length() >= 7 && payloadString.substring(0, 7).equals("getMenu")) {
                                                    FirestoreUtils.getMenu((menu, exceptionMessage) -> {
                                                        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
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
                                                } else if (payloadString.length() >= 10 && payloadString.substring(0, 10).equals("extraItems")) {
                                                    try {
                                                        String extraItemsString = payloadString.substring(10);
                                                        List<String> extraItems = new ArrayList<>();

                                                        Log.i(TAG, "jsonExtraItems: " + extraItemsString);

                                                        JSONArray menuJsonArray = new JSONArray(extraItemsString);
                                                        Gson gson = new Gson();
                                                        int tempTotal = 0;
                                                        for (int i = 0; i < menuJsonArray.length(); i++) {
                                                            MenuItem item = gson.fromJson(menuJsonArray.getJSONObject(i).toString(), MenuItem.class);
                                                            Log.i(TAG, "Item ID: " + item.getId());
                                                            extraItems.add(item.getId());
                                                            tempTotal += item.getPrice();
                                                        }
                                                        final int extraTotal = tempTotal;
                                                        FirestoreUtils.getOrderItems(currentOrderId, (items, currentTotal, isCooking, exceptionMessage) -> {
                                                            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                                                                Log.w(TAG, "Error getting order items: " + exceptionMessage);
                                                                return;
                                                            }
                                                            items.addAll(extraItems);
                                                            long newTotal = currentTotal + extraTotal;
                                                            FirestoreUtils.addExtraItems(currentOrderId, items, newTotal, exceptionMessage1 -> {
                                                                if (exceptionMessage1 != null && !exceptionMessage1.isEmpty()) {
                                                                    Log.w(TAG, "Error adding extra items: " + exceptionMessage1);
                                                                    return;
                                                                }

                                                                Payload payloadExtraItemsAdded = Payload.fromBytes("extraItemsAdded".getBytes());
                                                                nearbyConnectionsClient.sendPayload(endpointId, payloadExtraItemsAdded)
                                                                        .addOnSuccessListener(aVoid -> Log.i(TAG, "extraItemsAdded sent"))
                                                                        .addOnFailureListener(e -> Log.w(TAG, "extraItemsAdded isn't sent: " + e.getLocalizedMessage()));

                                                                //TODO Orders status was "eats", added extra item, status set to "preparing" with time only for the new items.
                                                                //TODO Add one more item from there and time is calculating for all items (because current status is "preparing"),
                                                                //TODO instead of calculating for 2nd and 3rd items adding.

                                                                Payload payloadEstimatedTime = Payload.fromBytes(("estimatedTime" + getCookingTime(isCooking ? items.size() : extraItems.size())).getBytes());
                                                                nearbyConnectionsClient.sendPayload(endpointId, payloadEstimatedTime)
                                                                        .addOnSuccessListener(aVoid -> Log.i(TAG, "Estimated time sent"))
                                                                        .addOnFailureListener(e -> Log.w(TAG, "Estimated time isn't sent: " + e.getLocalizedMessage()));
                                                            });
                                                        });

                                                    } catch (JSONException e) {
                                                        Log.w(TAG, e.getLocalizedMessage());
                                                    }
                                                } else if (payloadString.length() >= 8 && payloadString.substring(0, 8).equals("getTotal")) {
                                                    String getTotalOrderId = payloadString.substring(8);
                                                    FirestoreUtils.getOrderTotal(getTotalOrderId, (total, exceptionMessage) -> {
                                                        Payload orderTotalPayload = Payload.fromBytes(("orderTotal" + String.valueOf(total)).getBytes());
                                                        nearbyConnectionsClient.sendPayload(endpointId, orderTotalPayload)
                                                                .addOnSuccessListener(aVoid -> Log.i(TAG, "Order total sent"))
                                                                .addOnFailureListener(e -> Log.w(TAG, "Order total isn't sent: " + e.getLocalizedMessage()));
                                                    });
                                                } else if (payloadString.substring(0, 9).equals("orderDone")) {
                                                    FirestoreUtils.closeOrder(currentOrderId, exceptionMessage -> {
                                                        if (exceptionMessage != null && !exceptionMessage.isEmpty())
                                                            Log.w(TAG, "Error closing order: " + exceptionMessage);
                                                    });
                                                }
                                            } catch (UnsupportedEncodingException e) {
                                                Log.w(TAG, e.getLocalizedMessage());
                                            }
                                        }

                                        @Override
                                        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                                        }
                                    })
                                            .addOnSuccessListener(aVoid -> FirestoreUtils.getWaiterStatusRealtimeUpdates(oId, (documentSnapshot, e) -> {
                                                if (e != null) {
                                                    Log.w(TAG, "Error getting realtime call the waiter updates" + e.getLocalizedMessage());
                                                } else if (documentSnapshot != null && documentSnapshot.getBoolean(FirestoreUtils.FIRESTORE_CALL_WAITER_FIELD) != currentWaiterStatus) {
                                                    currentWaiterStatus = documentSnapshot.getBoolean(FirestoreUtils.FIRESTORE_CALL_WAITER_FIELD);
                                                    Payload waiterUpdatePayload = Payload.fromBytes(("waiterUpdate" + documentSnapshot.getBoolean(FirestoreUtils.FIRESTORE_CALL_WAITER_FIELD)).getBytes());
                                                    nearbyConnectionsClient.sendPayload(endpointId, waiterUpdatePayload)
                                                            .addOnFailureListener(e12 -> {
                                                                Log.w(TAG, "Fail sending waiter update payload: " + e12.getLocalizedMessage());
                                                                new Handler().postDelayed(
                                                                        () -> nearbyConnectionsClient.sendPayload(endpointId, waiterUpdatePayload)
                                                                                .addOnFailureListener(e1 -> Log.w(TAG, "Fail sending waiter update payload again: " + e1.getLocalizedMessage())), 500);
                                                            });
                                                }
                                            }))
                                            .addOnFailureListener(e -> Log.w(TAG, "Connection isn't accepted: " + e.getLocalizedMessage()));
                                }

                                @Override
                                public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                                    switch (result.getStatus().getStatusCode()) {
                                        case ConnectionsStatusCodes.STATUS_OK:
                                            nearbyConnectionsClient.stopDiscovery();

                                            Payload payloadOrderId = Payload.fromBytes(("orderId" + oId).getBytes());
                                            nearbyConnectionsClient.sendPayload(endpointId, payloadOrderId)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.i(TAG, "Order ID sent 2: " + oId);
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
                                        default:
                                            Log.w(TAG, "Connection status isn't OK");
                                            break;
                                    }
                                }

                                @Override
                                public void onDisconnected(@NonNull String endpointId) {
                                    Log.i(MainActivity.TAG, "Disconnecting by Things MainActivity");
                                    connectionLifecycleCallback.onDisconnected(endpointId);
                                }
                            });
                        } else {
                            Log.i(TAG, "onEndpointFound: Different names");
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

            OnFailureListener orderUpdateOnFailureListener = e1 -> { //Try again with the previously saved endpointId
                switch (newOrderStatus) {
                    case (FirestoreUtils.FIRESTORE_STATUS_PREPARING):
                        Payload payloadEstimatedTime = Payload.fromBytes(("estimatedTime" + getCookingTime(((List<String>) document.get(FirestoreUtils.FIRESTORE_ITEMS_FIELD)).size())).getBytes());
                        nearbyConnectionsClient.sendPayload(currentEndpointId, payloadEstimatedTime)
                                .addOnSuccessListener(aVoid -> Log.i(TAG, "Estimated time sent"))
                                .addOnFailureListener(eTime -> Log.w(TAG, "Estimated time isn't sent: " + eTime.getLocalizedMessage()));

                        Payload payloadOrderStatusPreparing = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_PREPARING).getBytes());
                        nearbyConnectionsClient.sendPayload(currentEndpointId, payloadOrderStatusPreparing)
                                .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                                .addOnFailureListener(ePrep -> Log.w(TAG, "Order updates isn't sent: " + ePrep.getLocalizedMessage()));
                        break;
                    case (FirestoreUtils.FIRESTORE_STATUS_EATS):
                        Payload payloadOrderStatusEats = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_EATS).getBytes());
                        nearbyConnectionsClient.sendPayload(currentEndpointId, payloadOrderStatusEats)
                                .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                                .addOnFailureListener(eEats -> Log.w(TAG, "Order updates isn't sent: " + eEats.getLocalizedMessage()));
                        break;
                    case (FirestoreUtils.FIRESTORE_STATUS_DONE):
                        Payload payloadOrderStatusDone = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_DONE).getBytes());
                        nearbyConnectionsClient.sendPayload(currentEndpointId, payloadOrderStatusDone)
                                .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                                .addOnFailureListener(eDone -> Log.w(TAG, "Order updates isn't sent: " + eDone.getLocalizedMessage()));
                        break;
                }
            };


            currentOrderStatus = newOrderStatus;
            switch (newOrderStatus) {
                case (FirestoreUtils.FIRESTORE_STATUS_PREPARING):
                    Payload payloadEstimatedTime = Payload.fromBytes(("estimatedTime" + getCookingTime(((List<String>) document.get(FirestoreUtils.FIRESTORE_ITEMS_FIELD)).size())).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadEstimatedTime)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Estimated time sent"))
                            .addOnFailureListener(orderUpdateOnFailureListener);

                    Payload payloadOrderStatusPreparing = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_PREPARING).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadOrderStatusPreparing)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                            .addOnFailureListener(orderUpdateOnFailureListener);
                    break;
                case (FirestoreUtils.FIRESTORE_STATUS_EATS):
                    Payload payloadOrderStatusEats = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_EATS).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadOrderStatusEats)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                            .addOnFailureListener(orderUpdateOnFailureListener);
                    break;
                case (FirestoreUtils.FIRESTORE_STATUS_DONE):
                    Payload payloadOrderStatusDone = Payload.fromBytes(("orderStatusUpdate" + FirestoreUtils.FIRESTORE_STATUS_DONE).getBytes());
                    nearbyConnectionsClient.sendPayload(endpointId, payloadOrderStatusDone)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Order updates sent"))
                            .addOnFailureListener(orderUpdateOnFailureListener);
                    break;
            }


        });
    }
}

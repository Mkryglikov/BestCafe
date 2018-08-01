package mkruglikov.bestcafe;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.UnsupportedEncodingException;

public class ActiveOrderActivity extends AppCompatActivity {

    public static final String ACTIVE_ORDER_ACTIVITY_ORDER_ID_EXTRA_KEY = "ActiveOrderActivity orderId extra key";
    public static final String ACTIVE_ORDER_ACTIVITY_ESTIMATED_TIME_EXTRA_KEY = "ActiveOrderActivity estimatedTime extra key";
    public static final String ACTIVE_ORDER_ACTIVITY_ENDPOINT_ID_EXTRA_KEY = "ActiveOrderActivity endpointId extra key";

    private String orderId, estimatedTime, thingsEndpointId;
    private ConstraintLayout layoutOrderCooking, layoutOrderEats;
    private TextView tvOrderCookingTime;
    private ProgressBar pbActiveOrderLoading;
    private Button btnCallWaiterOrderCooking, btnCallWaiterOrderEats;
    private NotificationManager notificationManager;
    private ConnectionsClient nearbyConnectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_order);

        orderId = getIntent().getExtras().getString(ACTIVE_ORDER_ACTIVITY_ORDER_ID_EXTRA_KEY);
        estimatedTime = getIntent().getExtras().getString(ACTIVE_ORDER_ACTIVITY_ESTIMATED_TIME_EXTRA_KEY);
        thingsEndpointId = getIntent().getExtras().getString(ACTIVE_ORDER_ACTIVITY_ENDPOINT_ID_EXTRA_KEY);

        layoutOrderCooking = findViewById(R.id.layoutOrderCooking);
        layoutOrderEats = findViewById(R.id.layoutOrderEats);

        pbActiveOrderLoading = findViewById(R.id.pbActiveOrderLoading);

        btnCallWaiterOrderCooking = findViewById(R.id.btnCallWaiterOrderCooking);
        btnCallWaiterOrderEats = findViewById(R.id.btnCallWaiterOrderEats);

        View.OnClickListener callWaiterButtonListener = view -> {
            Payload callTheWaiterPayload = Payload.fromBytes(("callTheWaiter" + orderId).getBytes());
            nearbyConnectionsClient.sendPayload(thingsEndpointId, callTheWaiterPayload)
                    .addOnSuccessListener(aVoid -> {
                        Log.i(MainActivity.TAG, "Waiter payload sent fo order " + orderId);
                        if (btnCallWaiterOrderCooking.getVisibility() == View.VISIBLE) {
                            btnCallWaiterOrderCooking.setText("Waiter is called");
                            btnCallWaiterOrderCooking.setActivated(false);
                        }
                        if (btnCallWaiterOrderEats.getVisibility() == View.VISIBLE) {
                            btnCallWaiterOrderEats.setText("Waiter is called");
                            btnCallWaiterOrderEats.setActivated(false);
                        }
                    });
        };

        btnCallWaiterOrderCooking.setOnClickListener(callWaiterButtonListener);
        btnCallWaiterOrderEats.setOnClickListener(callWaiterButtonListener);

        if (estimatedTime != null)
            showCookingLayout();
        else
            showEatsLayout();

        nearbyConnectionsClient = Nearby.getConnectionsClient(this);

        //Have to connect again since we want to get payload here but can't get current connection or update existing PayloadCallback
        startAdvertising();
    }

    @SuppressLint("MissingPermission")
    private void startAdvertising() {
        layoutOrderCooking.setVisibility(View.GONE);
        layoutOrderEats.setVisibility(View.GONE);
        pbActiveOrderLoading.setVisibility(View.VISIBLE);

        String nickname = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        nearbyConnectionsClient.startAdvertising(
                nickname,
                BuildConfig.NearbyServiceId,
                connectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(aVoid -> Log.i(MainActivity.TAG, "Advertising with nickname " + nickname))
                .addOnFailureListener(e -> Log.w(MainActivity.TAG, "Unable to start advertising: " + e.getLocalizedMessage()));
    }

    private void showCookingLayout() {
        pbActiveOrderLoading.setVisibility(View.GONE);
        layoutOrderEats.setVisibility(View.GONE);
        layoutOrderCooking.setVisibility(View.VISIBLE);
        tvOrderCookingTime = findViewById(R.id.tvOrderCookingTime);
        tvOrderCookingTime.setText(estimatedTime);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ActiveOrderActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Your order will be cooked in " + estimatedTime + " minutes")
                .setAutoCancel(false)
                .setOngoing(true);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(30, builder.build());
    }

    private void showEatsLayout() {
        pbActiveOrderLoading.setVisibility(View.GONE);
        layoutOrderCooking.setVisibility(View.GONE);
        layoutOrderEats.setVisibility(View.VISIBLE);
        if (notificationManager != null)
            notificationManager.cancelAll();
    }

    @Override
    protected void onDestroy() {
        if (notificationManager != null)
            notificationManager.cancelAll();
        if (nearbyConnectionsClient != null) {
            Log.i(MainActivity.TAG, "Disconnecting by active order Activity");
            nearbyConnectionsClient.disconnectFromEndpoint(thingsEndpointId);
            nearbyConnectionsClient.stopDiscovery();
            nearbyConnectionsClient.stopAdvertising();
        }
        super.onDestroy();
    }

    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
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
                    Log.i(MainActivity.TAG, "Payload Received: " + payloadString);

                    if (payloadString.substring(0, 7).equals("orderId")) {
                        orderId = payloadString.substring(7);

                        //Wait for estimated time
                        new Handler().postDelayed(() -> {
                            if (estimatedTime != null)
                                showCookingLayout();
                            else
                                showEatsLayout();
                        }, 500);
                    }

                    if (payloadString.substring(0, 13).equals("estimatedTime")) {
                        estimatedTime = payloadString.substring(13);
                    }

                    if (payloadString.length() > 17 && payloadString.substring(0, 17).equals("orderStatusUpdate")) {
                        payloadString = payloadString.substring(17);
                        switch (payloadString) {
                            case (FirestoreUtils.FIRESTORE_STATUS_PREPARING):
                                showCookingLayout();
                                break;
                            case (FirestoreUtils.FIRESTORE_STATUS_EATS):
                                showEatsLayout();
                                break;
                            case (FirestoreUtils.FIRESTORE_STATUS_DONE):
                                //todo
                                break;
                        }
                    } else if (payloadString.length() > 12 && payloadString.substring(0, 12).equals("waiterUpdate")) {
                        String isWaiterCalled = payloadString.substring(12);
                        Log.i(MainActivity.TAG, "Waiter update: " + isWaiterCalled);
                        if (Boolean.valueOf(isWaiterCalled)) {
                            Log.i(MainActivity.TAG, "Waiter is called");
                            if (btnCallWaiterOrderCooking.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderCooking.setText("Waiter is called");
                                btnCallWaiterOrderCooking.setActivated(false);
                            }
                            if (btnCallWaiterOrderEats.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderEats.setText("Waiter is called");
                                btnCallWaiterOrderEats.setActivated(false);
                            }
                        } else {
                            Log.i(MainActivity.TAG, "Waiter is not called");
                            if (btnCallWaiterOrderCooking.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderCooking.setText(getString(R.string.call_the_waiter_text));
                                btnCallWaiterOrderCooking.setActivated(true);
                            }
                            if (btnCallWaiterOrderEats.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderEats.setText(getString(R.string.call_the_waiter_text));
                                btnCallWaiterOrderEats.setActivated(true);
                            }
                        }
                    }
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                }
            })
                    .addOnSuccessListener(aVoid -> {
                        thingsEndpointId = endpointId;
                        Log.i(MainActivity.TAG, "Connecting from active order activity done");
                        nearbyConnectionsClient.stopAdvertising();
                    })
                    .addOnFailureListener(e -> Log.w(MainActivity.TAG, "Connecting from active order activity failed: " + e.getLocalizedMessage()));
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {

        }

        @Override
        public void onDisconnected(@NonNull String s) {
            startAdvertising();
        }
    };
}

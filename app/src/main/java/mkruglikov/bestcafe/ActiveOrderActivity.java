package mkruglikov.bestcafe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class ActiveOrderActivity extends AppCompatActivity {

    public static final String ACTIVE_ORDER_ACTIVITY_ENDPOINT_ID_EXTRA_KEY = "ActiveOrderActivity endpointId extra key";
    public static final String ACTIVE_ORDER_ACTIVITY_IS_WANT_TO_CONNECT_WIFI_EXTRA_KEY = "ActiveOrderActivity isWantToConnectWifi extra key";
    public static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 666;

    private String orderId, estimatedTime, thingsEndpointId, thingsEndpointName;
    private ConstraintLayout layoutOrderCooking, layoutOrderEats, layoutOrderConnecting, containerExtraItems;
    private TextView tvOrderCookingTime, tvOrderConnecting;
    private Button btnCallWaiterOrderCooking, btnCallWaiterOrderEats, btnAddExtraOrderEats, btnAddExtraOrderCooking, btnCloseOrderCooking;
    private NotificationManager notificationManager;
    private ConnectionsClient nearbyConnectionsClient;
    private FragmentManager fragmentManager;
    private PaymentsClient paymentsClient;
    private Toolbar toolbarActiveOrder;
    private boolean isWantToConnectWifi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_order);

        toolbarActiveOrder = findViewById(R.id.toolbarActiveOrder);
        setSupportActionBar(toolbarActiveOrder);

        thingsEndpointId = getIntent().getExtras().getString(ACTIVE_ORDER_ACTIVITY_ENDPOINT_ID_EXTRA_KEY);
        isWantToConnectWifi = getIntent().getExtras().getBoolean(ACTIVE_ORDER_ACTIVITY_IS_WANT_TO_CONNECT_WIFI_EXTRA_KEY);

        layoutOrderCooking = findViewById(R.id.layoutOrderCooking);
        layoutOrderEats = findViewById(R.id.layoutOrderEats);
        layoutOrderConnecting = findViewById(R.id.layoutOrderConnecting);
        containerExtraItems = findViewById(R.id.containerExtraItems);

        tvOrderConnecting = findViewById(R.id.tvOrderConnecting);

        btnCallWaiterOrderCooking = findViewById(R.id.btnCallWaiterOrderCooking);
        btnCallWaiterOrderEats = findViewById(R.id.btnCallWaiterOrderEats);

        View.OnClickListener callWaiterButtonListener = view -> callTheWaiter();

        btnCallWaiterOrderCooking.setOnClickListener(callWaiterButtonListener);
        btnCallWaiterOrderEats.setOnClickListener(callWaiterButtonListener);

        btnAddExtraOrderEats = findViewById(R.id.btnAddExtraOrderEats);
        btnAddExtraOrderCooking = findViewById(R.id.btnAddExtraOrderCooking);

        View.OnClickListener addExtraButtonListener = view -> showExtraItemsFragment();

        btnAddExtraOrderEats.setOnClickListener(addExtraButtonListener);
        btnAddExtraOrderCooking.setOnClickListener(addExtraButtonListener);

        btnCloseOrderCooking = findViewById(R.id.btnCloseOrderCooking);
        btnCloseOrderCooking.setOnClickListener(view -> {
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to close the order and pay?")
                    .setCancelable(true)
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel())
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                    {
                        Payload getTotalPayload = Payload.fromBytes(("getTotal" + orderId).getBytes());
                        nearbyConnectionsClient.sendPayload(thingsEndpointId, getTotalPayload)
                                .addOnFailureListener(e -> Log.w(MainActivity.TAG, "Error sending getTotal payload: " + e.getLocalizedMessage()));
                    })
                    .create();
            alert.show();

        });

        fragmentManager = getSupportFragmentManager();

        if (estimatedTime != null)
            showCookingLayout();
        else
            showEatsLayout();

        nearbyConnectionsClient = Nearby.getConnectionsClient(this);

        //Have to connect again since we want to get payload here but can't get current connection or update existing PayloadCallback
        startAdvertising();

        paymentsClient = Wallet.getPaymentsClient(
                this,
                new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String currentSSID = wifiManager.getConnectionInfo().getSSID();
        currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
        if (!currentSSID.equals(BuildConfig.WifiSSID)) {
            getMenuInflater().inflate(R.menu.menu_wifi, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuConnectToWifi:
                isWantToConnectWifi = true;
                connectToWifi();
                return true;
            default:
                return false;
        }
    }

    private void callTheWaiter() {
        Payload callTheWaiterPayload = Payload.fromBytes(("callTheWaiter" + orderId).getBytes());
        nearbyConnectionsClient.sendPayload(thingsEndpointId, callTheWaiterPayload)
                .addOnSuccessListener(aVoid -> {
                    Log.i(MainActivity.TAG, "Waiter payload sent fo order " + orderId);
                    if (btnCallWaiterOrderCooking.getVisibility() == View.VISIBLE) {
                        btnCallWaiterOrderCooking.setText("Waiter is called");
                        btnCallWaiterOrderCooking.setEnabled(false);
                        btnCallWaiterOrderCooking.setBackgroundColor(Color.TRANSPARENT);
                    }
                    if (btnCallWaiterOrderEats.getVisibility() == View.VISIBLE) {
                        btnCallWaiterOrderEats.setText("Waiter is called");
                        btnCallWaiterOrderEats.setEnabled(false);
                        btnCallWaiterOrderEats.setBackgroundColor(Color.TRANSPARENT);
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startAdvertising() {
        showConnectingLayout();

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
        hideConnectingLayout();
        layoutOrderEats.setVisibility(View.GONE);
        containerExtraItems.setVisibility(View.GONE);
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
        hideConnectingLayout();
        layoutOrderCooking.setVisibility(View.GONE);
        containerExtraItems.setVisibility(View.GONE);
        layoutOrderEats.setVisibility(View.VISIBLE);
        if (notificationManager != null)
            notificationManager.cancelAll();
    }

    private void showExtraItemsFragment() {
        hideConnectingLayout();
        layoutOrderCooking.setVisibility(View.GONE);
        layoutOrderEats.setVisibility(View.GONE);
        containerExtraItems.setVisibility(View.VISIBLE);
        if (notificationManager != null)
            notificationManager.cancelAll();

        nearbyConnectionsClient.sendPayload(thingsEndpointId, Payload.fromBytes("getMenu".getBytes()))
                .addOnFailureListener(e -> Log.w(MainActivity.TAG, "Fail getting menu: " + e.getLocalizedMessage()));
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
            nearbyConnectionsClient.stopAdvertising();
            thingsEndpointName = connectionInfo.getEndpointName();
            thingsEndpointId = endpointId;
            connectToWifi();
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
                            if (estimatedTime != null && !estimatedTime.isEmpty())
                                showCookingLayout();
                            else
                                showEatsLayout();
                        }, 500);
                    } else if (payloadString.length() >= 15 && payloadString.equals("extraItemsAdded")) {

                        //Wait for estimated time
                        new Handler().postDelayed(() -> {
                            if (estimatedTime != null && !estimatedTime.isEmpty())
                                showCookingLayout();
                            else
                                showEatsLayout();
                        }, 1000);
                    } else if (payloadString.length() > 13 && payloadString.substring(0, 13).equals("estimatedTime")) {
                        estimatedTime = payloadString.substring(13);
                    } else if (payloadString.length() > 17 && payloadString.substring(0, 17).equals("orderStatusUpdate")) {
                        payloadString = payloadString.substring(17);
                        switch (payloadString) {
                            case (FirestoreUtils.FIRESTORE_STATUS_PREPARING):
                                showCookingLayout();
                                break;
                            case (FirestoreUtils.FIRESTORE_STATUS_EATS):
                                showEatsLayout();
                                break;
                            case (FirestoreUtils.FIRESTORE_STATUS_DONE):
                                Toast.makeText(ActiveOrderActivity.this, "Thank you for coming", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ActiveOrderActivity.this, MainActivity.class));
                                finish();
                                break;
                        }
                    } else if (payloadString.length() > 12 && payloadString.substring(0, 12).equals("waiterUpdate")) {
                        String isWaiterCalled = payloadString.substring(12);
                        Log.i(MainActivity.TAG, "Waiter update: " + isWaiterCalled);
                        if (Boolean.valueOf(isWaiterCalled)) {
                            Log.i(MainActivity.TAG, "Waiter is called");
                            if (btnCallWaiterOrderCooking.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderCooking.setText("Waiter is called");
                                btnCallWaiterOrderCooking.setEnabled(false);
                                btnCallWaiterOrderCooking.setBackgroundColor(Color.TRANSPARENT);
                            }
                            if (btnCallWaiterOrderEats.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderEats.setText("Waiter is called");
                                btnCallWaiterOrderEats.setEnabled(false);
                                btnCallWaiterOrderEats.setBackgroundColor(Color.TRANSPARENT);
                            }
                        } else {
                            Log.i(MainActivity.TAG, "Waiter is not called");
                            if (btnCallWaiterOrderCooking.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderCooking.setText(getString(R.string.call_the_waiter_text));
                                btnCallWaiterOrderCooking.setEnabled(true);
                                btnCallWaiterOrderCooking.setBackgroundResource(R.drawable.button_rounded_white);
                            }
                            if (btnCallWaiterOrderEats.getVisibility() == View.VISIBLE) {
                                btnCallWaiterOrderEats.setText(getString(R.string.call_the_waiter_text));
                                btnCallWaiterOrderEats.setEnabled(true);
                                btnCallWaiterOrderEats.setBackgroundResource(R.drawable.button_rounded_white);

                            }
                        }
                    } else if (payloadString.substring(0, 4).equals("menu")) {
                        String menuString = payloadString.substring(4);
                        FragmentOrder fragmentOrder = new FragmentOrder();
                        Bundle args = new Bundle();
                        args.putString(FragmentOrder.ORDER_FRAGMENT_MENU_ARGUMENTS_KEY, menuString);
                        args.putString(FragmentOrder.THINGS_ENDPOINT_ID_ARGUMENTS_KEY, endpointId);
                        args.putBoolean(FragmentOrder.IS_NEW_ORDER_ARGUMENTS_KEY, false);
                        fragmentOrder.setArguments(args);
                        fragmentManager.beginTransaction()
                                .replace(R.id.containerExtraItems, fragmentOrder)
                                .commit();
                    } else if (payloadString.substring(0, 10).equals("orderTotal")) {
                        int total = Integer.valueOf(payloadString.substring(10));

                        String[] payOptions = new String[]{"With cash / credit card", "Google Pay"};
                        new AlertDialog.Builder(ActiveOrderActivity.this)
                                .setTitle("How do you want to pay?")
                                .setItems(payOptions, (dialog, item) -> {
                                    switch (item) {
                                        case 0:
                                            callTheWaiter();
                                            Toast.makeText(ActiveOrderActivity.this, "Waiter is called", Toast.LENGTH_LONG).show();
                                            break;
                                        case 1:
                                            IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                                                    .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                                                    .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                                                    .build();
                                            Task<Boolean> task = paymentsClient.isReadyToPay(request);
                                            task.addOnCompleteListener(
                                                    task1 -> {
                                                        try {
                                                            if (task1.getResult(ApiException.class))
                                                                payWithGooglePay(total);
                                                            else
                                                                Toast.makeText(ActiveOrderActivity.this, "Can't pay with Google Pay", Toast.LENGTH_LONG).show();
                                                        } catch (ApiException exception) {
                                                            Log.w(MainActivity.TAG, "payWithGooglePay exception: " + exception.getLocalizedMessage());
                                                            Toast.makeText(ActiveOrderActivity.this, "payWithGooglePay exception: " + exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                            break;
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
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

    private void showConnectingLayout() {
        layoutOrderCooking.setVisibility(View.GONE);
        layoutOrderEats.setVisibility(View.GONE);
        layoutOrderConnecting.setVisibility(View.VISIBLE);
        toolbarActiveOrder.setVisibility(View.INVISIBLE);
        if (thingsEndpointName != null && !thingsEndpointId.isEmpty()) {
            tvOrderConnecting.append(" to table #" + thingsEndpointName);
        }
    }

    private void hideConnectingLayout() {
        layoutOrderConnecting.setVisibility(View.GONE);
        toolbarActiveOrder.setVisibility(View.VISIBLE);
    }

    private void payWithGooglePay(int total) {
        PaymentDataRequest.Builder request = PaymentDataRequest.newBuilder()
                .setTransactionInfo(
                        TransactionInfo.newBuilder()
                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                .setTotalPrice(String.valueOf(total) + ".00")
                                .setCurrencyCode("USD")
                                .build())
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setCardRequirements(CardRequirements.newBuilder()
                        .addAllowedCardNetworks(
                                Arrays.asList(
                                        WalletConstants.CARD_NETWORK_AMEX,
                                        WalletConstants.CARD_NETWORK_DISCOVER,
                                        WalletConstants.CARD_NETWORK_VISA,
                                        WalletConstants.CARD_NETWORK_MASTERCARD))
                        .build());

        //Parameters for testing
        PaymentMethodTokenizationParameters params = PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "example")
                .addParameter("gatewayMerchantId", "exampleGatewayMerchantId")
                .build();

        request.setPaymentMethodTokenizationParameters(params);
        PaymentDataRequest requestResult = request.build();
        if (requestResult != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(requestResult),
                    this,
                    LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    String token = paymentData.getPaymentMethodToken().getToken();
                    Payload orderDonePayload = Payload.fromBytes("orderDone".getBytes());
                    nearbyConnectionsClient.sendPayload(thingsEndpointId, orderDonePayload)
                            .addOnFailureListener(e -> Log.w(MainActivity.TAG, "Error sending orderDonePayload payload: " + e.getLocalizedMessage()));
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    break;
            }
        }
    }

    private void connectToWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String currentSSID = wifiManager.getConnectionInfo().getSSID();
        currentSSID = currentSSID.substring(1, currentSSID.length() - 1);

        if (!currentSSID.equals(BuildConfig.WifiSSID) && isWantToConnectWifi) {
            AlertDialog alert = new AlertDialog.Builder(ActiveOrderActivity.this)
                    .setTitle("BestCafe")
                    .setMessage("Do you want to connect to our WiFi network?")
                    .setCancelable(true)
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel())
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        boolean isWifiEnabled = wifiManager.isWifiEnabled();
                        if (!isWifiEnabled)
                            wifiManager.setWifiEnabled(true);

                        //Waiting for Wifi to turn on and connect
                        new Handler().postDelayed(() -> {
                            WifiConfiguration conf = new WifiConfiguration();
                            conf.SSID = "\"" + BuildConfig.WifiSSID + "\"";
                            conf.preSharedKey = "\"" + BuildConfig.WifiPassword + "\"";
                            wifiManager.addNetwork(conf);
                            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                            for (WifiConfiguration i : list) {
                                if (i.SSID != null && i.SSID.equals("\"" + BuildConfig.WifiSSID + "\"")) {
                                    wifiManager.disconnect();
                                    wifiManager.enableNetwork(i.networkId, true);
                                    wifiManager.reconnect();
                                    break;
                                }
                            }
                            toolbarActiveOrder.getMenu().findItem(R.id.menuConnectToWifi).setEnabled(false);

                        }, 2000);
                    })
                    .create();
            alert.show();
        }
    }
}

package mkruglikov.bestcafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        PermissionUtil.checkGroup(this, new CallbackBuilder()
                .onGranted(() -> {
                    FragmentConnectConnecting fragment = new FragmentConnectConnecting();
                    Bundle args = new Bundle();
                    args.putParcelable(FragmentConnectConnecting.CONNECTION_LIFECYCLE_CALLBACK_ARGUMENTS_KEY, new ConnectionLifecycleCallback());
                    fragment.setArguments(args);
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.containerConnect, fragment)
                            .commit();
                    nearbyConnectionsClient = Nearby.getConnectionsClient(this);
                })
                .build(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE});
    }

    public class ConnectionLifecycleCallback extends com.google.android.gms.nearby.connection.ConnectionLifecycleCallback implements Parcelable {

        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            nearbyConnectionsClient.acceptConnection(endpointId, new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                    try {
                        FragmentOrder fragment = new FragmentOrder();
                        Bundle args = new Bundle();
                        args.putString(FragmentOrder.ORDER_FRAGMENT_MENU_ARGUMENTS_KEY, new String(payload.asBytes(), "UTF-8"));
                        fragment.setArguments(args);
                        fragmentManager.beginTransaction()
                                .replace(R.id.containerConnect, fragment)
                                .commit();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                }
            });
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
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
}

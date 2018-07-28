package mkruglikov.bestcafe;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.euzee.permission.CallbackBuilder;
import com.github.euzee.permission.PermissionDenied;
import com.github.euzee.permission.PermissionGranted;
import com.github.euzee.permission.PermissionUtil;

public class ConnectActivity extends AppCompatActivity {

    public static final int CONNECT_ACTIVITY_REQUEST_CODE = 19;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        PermissionUtil.locationCoarse(this, new CallbackBuilder()
                .onGranted(new PermissionGranted() {
                    @Override
                    public void onPermissionGranted() {
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .add(R.id.containerConnect, new FragmentConnectConnecting())
                                .commit();
                    }
                })
                .onDenied(new PermissionDenied() {
                    @Override
                    public void onPermissionDenied() {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                })
                .build());
    }
}

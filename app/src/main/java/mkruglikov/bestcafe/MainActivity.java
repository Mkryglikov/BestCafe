package mkruglikov.bestcafe;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.containerMain) != null) {
            if (savedInstanceState != null) return;
            fragmentManager = getSupportFragmentManager();
            FragmentMainNotSignedIn fragmentMainNotSignedIn = new FragmentMainNotSignedIn();
            fragmentManager.beginTransaction()
                    .add(R.id.containerMain, fragmentMainNotSignedIn)
                    .commit();
        }
    }
}

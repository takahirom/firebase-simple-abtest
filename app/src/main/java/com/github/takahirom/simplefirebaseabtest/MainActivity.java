package com.github.takahirom.simplefirebaseabtest;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    public static final String REMOTE_CONFIG_BUTTON_COLOR_KEY = "button_color";
    public static final String ANALYTICS_USER_PROPERTY_BUTTON_COLOR_KEY = "button_color";

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAnalytics firebaseAnalytics;

    private Button helloButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initViews();
        initRemoteConfig();
    }

    private void initViews() {
        helloButton = (Button) findViewById(R.id.hello_button);
        helloButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Bundle parameter = new Bundle();
                parameter.putString(FirebaseAnalytics.Param.ITEM_NAME, "button");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, parameter);

                Toast.makeText(MainActivity.this, "Button tapped", Toast.LENGTH_SHORT).show();
            }
        });
        applyButtonColor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "button");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    private void initRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        long cacheExpiration = 3600; // 1 hour in seconds.
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    firebaseRemoteConfig.activateFetched();
                }
                applyButtonColor();
            }
        });
    }


    private void applyButtonColor() {
        final String color = getButtonColor();
        final String colorForUserProperty = color.replace("#", "");
        firebaseAnalytics.setUserProperty(ANALYTICS_USER_PROPERTY_BUTTON_COLOR_KEY, colorForUserProperty);
        helloButton.setBackgroundColor(Color.parseColor(color));
    }

    public String getButtonColor() {
        return firebaseRemoteConfig.getString(REMOTE_CONFIG_BUTTON_COLOR_KEY);
    }
}

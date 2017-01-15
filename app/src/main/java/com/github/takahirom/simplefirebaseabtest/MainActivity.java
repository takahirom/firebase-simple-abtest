package com.github.takahirom.simplefirebaseabtest;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.appcompat.*;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.takahirom.fireannotation.annotation.FireEventLog;
import com.github.takahirom.fireannotation.annotation.FireUserProperty;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import static com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT;
import static com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_ITEM;
import static com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE;

public class MainActivity extends AppCompatActivity {

    public static final String REMOTE_CONFIG_BUTTON_COLOR_KEY = "button_color";

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAnalytics firebaseAnalytics;

    private Button helloButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FirebaseAnalyticsのインスタンス取得(随時getInstanceしても良いみたい)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initRemoteConfig();
        initViews();
    }


    // ボタンを見たという情報をFirebase Analyticsに送信
    @FireEventLog(event = VIEW_ITEM, parameter = CONTENT_TYPE + ":" + "button")
    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initViews() {
        helloButton = (Button) findViewById(R.id.hello_button);
        helloButton.setOnClickListener(new View.OnClickListener() {
            // クリックしたらFirebase Analyticsに送信
            // ここではパラメーターとして色は送らず、UserPropertyとして送る
            @FireEventLog(event = SELECT_CONTENT, parameter = CONTENT_TYPE + ":" + "button")
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Button tapped", Toast.LENGTH_SHORT).show();
            }
        });

        // 一度でもRemoteConfigから取得(fetch)完了していたら取得した色を表示
        // 取得されていなければxmlで定義しているデフォルトの色を表示
        applyButtonColor();
    }

    // FirebaseのUserPropertyで色を送信する
    @FireUserProperty(customProperty = CustomUserPropertyCreator.class)
    private void applyButtonColor() {
        final String color = getButtonColor();
        helloButton.setBackgroundColor(Color.parseColor(color));
    }

    private void initRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        final FirebaseRemoteConfigSettings.Builder settingBuilder = new FirebaseRemoteConfigSettings.Builder();
        if (BuildConfig.DEBUG) {
            // デバッグビルドのときはDeveloperModeを有効にする
            settingBuilder.setDeveloperModeEnabled(true);
        }
        firebaseRemoteConfig.setConfigSettings(settingBuilder.build());
        // デフォルトの値を設定
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        // キャッシュの保持時間
        long cacheExpiration = 3600; // 一時間
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // 取得した値を利用するように設定(次回起動時に反映なども可能)
                    firebaseRemoteConfig.activateFetched();
                }
                applyButtonColor();
            }
        });
    }

    String getButtonColor() {
        // RemoteConfigから色の文字列を取得する
        return firebaseRemoteConfig.getString(REMOTE_CONFIG_BUTTON_COLOR_KEY);
    }
}

package com.farmerbb.taskbar.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.farmerbb.taskbar.MainActivity;
import com.farmerbb.taskbar.R;

public class ImportSettingsActivity extends Activity {

    boolean broadcastSent = false;

    private BroadcastReceiver settingsReceivedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent restartIntent = new Intent(ImportSettingsActivity.this, MainActivity.class);
            restartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(restartIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            System.exit(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_settings);
        setFinishOnTouchOutside(false);

        LocalBroadcastManager.getInstance(this).registerReceiver(settingsReceivedReceiver, new IntentFilter("com.farmerbb.taskbar.IMPORT_FINISHED"));

        if(!broadcastSent) {
            sendBroadcast(new Intent("com.farmerbb.taskbar.RECEIVE_SETTINGS"));
            broadcastSent = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(settingsReceivedReceiver);
    }

    // Disable back button
    @Override
    public void onBackPressed() {}
}

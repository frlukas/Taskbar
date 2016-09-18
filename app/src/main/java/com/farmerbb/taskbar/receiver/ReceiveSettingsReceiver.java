package com.farmerbb.taskbar.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.farmerbb.taskbar.BuildConfig;
import com.farmerbb.taskbar.util.AppEntry;
import com.farmerbb.taskbar.util.Blacklist;
import com.farmerbb.taskbar.util.BlacklistEntry;
import com.farmerbb.taskbar.util.PinnedBlockedApps;
import com.farmerbb.taskbar.util.SavedWindowSizes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceiveSettingsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ignore this broadcast if this is the free version
        if(!BuildConfig.APPLICATION_ID.equals(BuildConfig.BASE_APPLICATION_ID)) {
            // Get pinned and blocked apps
            PinnedBlockedApps pba = PinnedBlockedApps.getInstance(context);
            pba.clear(context);

            String[] pinnedAppsPackageNames = intent.getStringArrayExtra("pinned_apps_package_names");
            String[] pinnedAppsComponentNames = intent.getStringArrayExtra("pinned_apps_component_names");
            String[] pinnedAppsLabels = intent.getStringArrayExtra("pinned_apps_labels");

            if(pinnedAppsPackageNames != null && pinnedAppsComponentNames != null && pinnedAppsLabels != null)
                for(int i = 0; i < pinnedAppsPackageNames.length; i++) {
                    Intent throwaway = new Intent();
                    throwaway.setComponent(ComponentName.unflattenFromString(pinnedAppsComponentNames[i]));

                    pba.addPinnedApp(context, new AppEntry(
                            pinnedAppsPackageNames[i],
                            pinnedAppsComponentNames[i],
                            pinnedAppsLabels[i],
                            throwaway.resolveActivityInfo(context.getPackageManager(), 0).loadIcon(context.getPackageManager()),
                            true
                    ));
                }

            String[] blockedAppsPackageNames = intent.getStringArrayExtra("blocked_apps_package_names");
            String[] blockedAppsComponentNames = intent.getStringArrayExtra("blocked_apps_component_names");
            String[] blockedAppsLabels = intent.getStringArrayExtra("blocked_apps_labels");

            if(blockedAppsPackageNames != null && blockedAppsComponentNames != null && blockedAppsLabels != null)
                for(int i = 0; i < blockedAppsPackageNames.length; i++) {
                    pba.addBlockedApp(context, new AppEntry(
                            blockedAppsPackageNames[i],
                            blockedAppsComponentNames[i],
                            blockedAppsLabels[i],
                            null,
                            false
                    ));
                }

            // Get blacklist
            Blacklist blacklist = Blacklist.getInstance(context);
            blacklist.clear(context);

            String[] blacklistPackageNames = intent.getStringArrayExtra("blacklist_package_names");
            String[] blacklistLabels = intent.getStringArrayExtra("blacklist_labels");

            if(blacklistPackageNames != null && blacklistLabels != null)
                for(int i = 0; i < blacklistPackageNames.length; i++) {
                    blacklist.addBlockedApp(context, new BlacklistEntry(
                            blacklistPackageNames[i],
                            blacklistLabels[i]
                    ));
                }

            // Get saved window sizes
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SavedWindowSizes savedWindowSizes = SavedWindowSizes.getInstance(context);
                savedWindowSizes.clear(context);

                String[] savedWindowSizesComponentNames = intent.getStringArrayExtra("saved_window_sizes_component_names");
                String[] savedWindowSizesWindowSizes = intent.getStringArrayExtra("saved_window_sizes_window_sizes");

                if(savedWindowSizesComponentNames != null && savedWindowSizesWindowSizes != null)
                    for(int i = 0; i < savedWindowSizesComponentNames.length; i++) {
                        savedWindowSizes.setWindowSize(context,
                                savedWindowSizesComponentNames[i],
                                savedWindowSizesWindowSizes[i]
                        );
                    }
            }

            // Get shared preferences
            String contents = intent.getStringExtra("preferences");
            if(contents.length() > 0)
                try {
                    File file = new File(context.getFilesDir().getParent() + "/shared_prefs/" + BuildConfig.APPLICATION_ID + "_preferences.xml");
                    FileOutputStream output = new FileOutputStream(file);
                    output.write(contents.getBytes());
                    output.close();
                } catch (IOException e) { /* Gracefully fail */ }

            try {
                File file = new File(context.getFilesDir() + File.separator + "imported_successfully");
                if(file.createNewFile())
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.farmerbb.taskbar.IMPORT_FINISHED"));
            } catch (IOException e) { /* Gracefully fail */ }
        }
    }
}

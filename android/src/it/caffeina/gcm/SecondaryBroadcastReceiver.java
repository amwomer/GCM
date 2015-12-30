package it.caffeina.gcm;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

public class SecondaryBroadcastReceiver extends android.content.BroadcastReceiver {

    private static final String LCAT = "it.caffeina.gcm.GCMIntentService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent launcherIntent;
        TiApplication instance = TiApplication.getInstance();
        String pkg = instance.getApplicationContext().getPackageName();

        if (instance.getCurrentActivity() == null) {
            launcherIntent = instance.getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkg);
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            if (intent.hasExtra("notification")) {
                launcherIntent.putExtra("notification", intent.getStringExtra("notification"));
            }

            context.startActivity(launcherIntent);
        } else {
            launcherIntent = new Intent(pkg + ".push.RECEIVED");

            if (intent.hasExtra("notification")) {
                launcherIntent.putExtra("notification", intent.getStringExtra("notification"));
            }

            launcherIntent.putExtra("inBackground", !TiApplication.isCurrentActivityInForeground());
            context.sendBroadcast(launcherIntent);
        }
    }
}
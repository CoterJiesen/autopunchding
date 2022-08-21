package com.ajiew.autopunchding.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ajiew.autopunchding.service.KeepRunningService;
import com.ajiew.autopunchding.util.AppUtil;

public class AutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(AppUtil.class.getSimpleName(), "AutoStartReceiver Intent:"+intent.getAction() );
        Intent punchIntent = new Intent(context, KeepRunningService.class);
        context.startService(punchIntent);
    }
}

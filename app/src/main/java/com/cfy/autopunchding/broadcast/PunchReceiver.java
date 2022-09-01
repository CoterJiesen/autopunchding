package com.cfy.autopunchding.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cfy.autopunchding.service.PunchService;
import com.cfy.autopunchding.util.AppUtil;

/**
 * author: aaron.chen
 * created on: 2018/9/9 15:33
 * description:
 */
public class PunchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(AppUtil.class.getSimpleName(), "PunchReceiver Intent:"+intent.getAction() );
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_TIME_TICK))
            context.startService(new Intent(context, PunchService.class));
    }
}

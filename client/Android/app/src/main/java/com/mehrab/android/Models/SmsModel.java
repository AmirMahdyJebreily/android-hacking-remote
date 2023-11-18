package com.mehrab.android.Models;

import java.util.ArrayList;
import java.util.Date;

public class SmsModel {
    public int type;
    public String sender;
    public String message;
    public Date timestamp;

        public SmsModel(int type ,String sender, String message) {
            this.type = type;
            this.sender = sender;
            this.message = message;
            this.timestamp = new Date();
        }

}

package com.hcmute.endsemesterproject.Utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class CommonConst {
    public static final String PREFIX = "+84";
    public static final String DOMAIN_PART_GMAIL = "@gmail.com";
    public static final String DEFAULT_CLIENT_ID = "347117574370-8fsbb7l20cmdk226ms491fcqv5ijugnt.apps.googleusercontent.com";

    public static boolean isValidPassword(String pass, Context context){
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(context , "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(pass.trim().length() < 6){
            Toast.makeText(context , "Length must >= 6", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(pass.trim().contains(" ")){
            Toast.makeText(context, "Space character is not allowed", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}

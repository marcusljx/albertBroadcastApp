package com.xjlm.albertbroadcast;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

/**
 * Created by Marcusljx on 7/23/2015.
 */
public class ParseApplication extends Application {

  // Global Variable - Store ID
  private String g_StoreID = "xFrTor9FsW";
  public String get_StoreID() {
    return g_StoreID;
  }
  public void set_StoreID(String text) {
    this.g_StoreID = text;
  }

  // Global Variable - Store Name
  private String g_StoreName = "Cheap As Chips";
  public String get_StoreName() {
    return g_StoreName;
  }
  public void set_StoreName(String text) {
    this.g_StoreName = text;
  }


  String AppID = "hZxPSnEIyoluaOtpj8BXidFbNCJhV5B5MHY91zIW";
  String ClientID = "pp6BQ7rivLT0cAvMiaybEs1sYSv8ju9kQ9KPRBex";

  @Override
  public void onCreate() {
    super.onCreate();

    //Init Parse
    ParseCrashReporting.enable(this);
    Parse.enableLocalDatastore(this);
    Parse.initialize(this, AppID, ClientID);
    Log.d("Parse", "Initialized Parse Connection");
    ParseInstallation.getCurrentInstallation().saveInBackground();

  }
}

package com.xjlm.albertbroadcast;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.led.LedPhase;
import com.aevi.led.LedSequenceParams;
import com.aevi.led.LedService;
import com.aevi.led.LedServiceProvider;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import au.com.commbank.albert.merchantmenu.MerchantMenu;
import au.com.commbank.albert.merchantmenu.MerchantMenuBundle;
import au.com.commbank.albert.merchantmenu.MerchantMenuItem;
import au.com.commbank.albert.merchantmenu.events.OnMerchantMenuEventListener;
import au.com.commbank.albert.merchantmenu.events.OnMerchantMenuItemClickListener;


public class BroadcastActivity extends ActionBarActivity implements View.OnClickListener, EditText.OnEditorActionListener {

  Button broadcastButton;
  EditText broadcastEditText;
  ListView mainListView;
  ArrayAdapter mArrayAdapter;
  ArrayList mNameList = new ArrayList();
  ParseObject parse_agent;

  EditText storeID_EditText;
  TextView storeName_TextView;

  // Social buttons toggles
  Boolean fb_tog = false;
  Boolean ig_tog = false;
  Boolean tw_tog = false;

  // MERCHANT MENU


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_broadcast);

    //Initialise the merchant menu




    // Retrieve Store Name
    final String local_storeID = ((ParseApplication) this.getApplication()).get_StoreID();  // get from global
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
    query.getInBackground(local_storeID, new GetCallback<ParseObject>() {
      public void done(ParseObject object, ParseException e) {
        if (e == null) { //Success
          String local_ShopName = object.getString("name");
          storeName_TextView.setText("Store: " + local_ShopName);
          storeID_EditText.setText("");
        } else {
          storeName_TextView.setText("ERROR: local_storeID not found. Broadcasts will not be delivered.");
          Log.d("Parse", "Error attempting to check local_storeID");
          storeID_EditText.setText(local_storeID);
        }
      }
    });


    //Listen to store id edittext field
    storeID_EditText = (EditText) findViewById(R.id.edittext_store_id);
    storeID_EditText.setOnEditorActionListener(this);

    storeName_TextView = (TextView) findViewById(R.id.text_store_name);

    // Broadcast
    broadcastEditText = (EditText) findViewById(R.id.broadcast_message_edit_text);


    // Broadcast Button Listener
    broadcastButton = (Button) findViewById(R.id.create_message_broadcast);
    broadcastButton.setOnClickListener(this);

    // Access the ListView
    mainListView = (ListView) findViewById(R.id.main_listview);
    // create ArrayAdapter for the ListView
    mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mNameList);
    // set the ListView to use the ArrayAdapter
    mainListView.setAdapter(mArrayAdapter);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item= menu.findItem(R.id.action_settings);
    item.setVisible(false);
    super.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_broadcast, menu);
    return true;
  }

  public void checkStoreID() {

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    switch (id) {
      case R.id.action_settings:
        return true;

      case R.id.menu_goto_POS:  // "Go To POS System" menu item clicked
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {

    //Only operate if space has stuff
    final String broadcastMessage = broadcastEditText.getText().toString();
    if(! broadcastMessage.equals("")) {
      // Get date/time
      Calendar rightNow = Calendar.getInstance();
      SimpleDateFormat dateTimeStringFormat = new SimpleDateFormat("EEE \t dd-MMM-yyyy \t HH:mm a");
      String dateTimeString = dateTimeStringFormat.format(rightNow.getTime());
      // Add value to top of mNameList
      mNameList.add(0, dateTimeString + "\n" + broadcastMessage);
      mArrayAdapter.notifyDataSetChanged();


      final String local_storeID = ((ParseApplication) this.getApplication()).get_StoreID();  // get from global
      // Query Server for Store entry
      final ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
      query.getInBackground(local_storeID, new GetCallback<ParseObject>() {
        public void done(ParseObject object, ParseException e) {
          if (e == null) { //Success
            String ShopName = object.getString("name");

            // Update "Specials" Entry
            object.put("specials", broadcastMessage);
            object.saveInBackground();
            Log.d("Parse", "Attempted updating Parse server entry.");

            // Create JSON Notification Object
            JSONObject notifObject = new JSONObject();
            try {
              notifObject.put("action", "ShopDetailsActivity");
              notifObject.put("shop", local_storeID);
              notifObject.put("alert", broadcastMessage);
              notifObject.put("title", ShopName);
            } catch (JSONException je) {
              je.printStackTrace();
            }
            // Send Push Notification to Parse Channel
            ParsePush push = new ParsePush();
            push.setChannel("");
            push.setData(notifObject);
            push.sendInBackground();


          } else {
            Log.d("Parse", "Error attempting to update Parse Server Entry");
          }
        }
      });




      // Clear the Broadcast Message Edit Text field
      broadcastEditText.setText("");
    }
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    storeName_TextView.setText("Searching Database...");

    // Set on Global Store ID
    final String local_storeID = storeID_EditText.getText().toString();
    ((ParseApplication) this.getApplication()).set_StoreID(local_storeID);


    ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
    query.getInBackground(local_storeID, new GetCallback<ParseObject>() {
      public void done(ParseObject object, ParseException e) {
        if (e == null) { //Success
          String local_ShopName = object.getString("name");
          storeName_TextView.setText("Store: " + local_ShopName);
          storeID_EditText.setText("");
        } else {
          storeName_TextView.setText("ERROR: local_storeID not found. Broadcasts will not be delivered.");
          Log.d("Parse", "Error attempting to check local_storeID");
          storeID_EditText.setText(local_storeID);
        }
      }
    });


    return false;
  }

  // GOTO Activity
  public void goto_pos(MenuItem mi) {
    Intent intent = new Intent(this, PointOfSaleActivity.class);
    startActivity(intent);
  }

  // Social Media Buttons
  public void toggleFBButton(View view) {
    ImageButton fbButton = (ImageButton) findViewById(R.id.socialButton_fb);
    if(!fb_tog) {
      fbButton.setBackgroundColor(getResources().getColor(R.color.pop));
      fb_tog = true;
    } else {
      fbButton.setBackgroundColor(getResources().getColor(R.color.grey));
      fb_tog = false;
    }
  }
  public void toggleIGButton(View view) {
    ImageButton igButton = (ImageButton) findViewById(R.id.socialButton_ig);
    if(!ig_tog) {
      igButton.setBackgroundColor(getResources().getColor(R.color.pop));
      ig_tog = true;
    } else {
      igButton.setBackgroundColor(getResources().getColor(R.color.grey));
    }
  }
  public void toggleTWButton(View view) {
    ImageButton twButton = (ImageButton) findViewById(R.id.socialButton_tw);
    if(!tw_tog) {
      twButton.setBackgroundColor(getResources().getColor(R.color.pop));
      tw_tog = true;
    } else {
      twButton.setBackgroundColor(getResources().getColor(R.color.grey));
      tw_tog = false;
    }
  }


  //=================MERCHANT MENU SECTION

}
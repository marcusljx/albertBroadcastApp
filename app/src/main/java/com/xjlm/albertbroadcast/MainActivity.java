package com.xjlm.albertbroadcast;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

  Button broadcastButton;
  EditText broadcastEditText;
  ListView mainListView;
  ArrayAdapter mArrayAdapter;
  ArrayList mNameList = new ArrayList();
  ParseObject parse_agent;

  // Parse Server Items
  String ParseServerObjID = "xFrTor9FsW";



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
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



      // Query Server for Store entry
      final ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
      query.getInBackground(ParseServerObjID, new GetCallback<ParseObject>() {
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
              notifObject.put("shop", ParseServerObjID);
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
}

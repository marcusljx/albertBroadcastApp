package com.xjlm.albertbroadcast;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.aevi.payment.PaymentRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;


public class PointOfSaleActivity extends ActionBarActivity {

  EditText g_payAmount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_point_of_sale);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_point_of_sale, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    switch (id) {
      case R.id.action_settings:  // settings item clicked
        return true;

      case R.id.menu_goto_BROADCAST:  // "Go To POS System" menu item clicked
        return true;
    }


    return super.onOptionsItemSelected(item);
  }

  // CALLBACK: ALBERT BUTTON CLICK
  public void onAlbertPayButtonClick(View view) {
    // Construct a new payment
    // Get value from text box
    g_payAmount   = (EditText) findViewById(R.id.pay_amount);
    String str_amt = g_payAmount.getText().toString();

    BigDecimal amt = new BigDecimal(str_amt);
    // Round the amount to 2 decimal places
    amt = amt.setScale(2, RoundingMode.CEILING);

    PaymentRequest payment = new PaymentRequest(amt);
    payment.setCurrency(Currency.getInstance("USD"));

    // Launch the Payment app.
    startActivityForResult(payment.createIntent(), 0);
  }
//
//  @Override
//  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    // Obtain the transaction result from the returned data.
//    TransactionResult result = TransactionResult.fromIntent(data);
//    // Use a toast to show the transaction result.
//    Toast.makeText(this,"Transaction result: " + result.getTransactionStatus(), Toast.LENGTH_LONG).show();
//  }

  // CALLBACK: CASH BUTTON CLICK
  public void onCashButtonClick(View view) {
  }
}

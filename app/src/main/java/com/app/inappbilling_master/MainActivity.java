package com.app.inappbilling_master;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {


    //Product id:
    static final String ITEM_SKU_ADREMOVAL = "purchase_premium";

    private Button mBuyButton;
    private String mAdRemovalPrice;

    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBillingClient = BillingClient.newBuilder(MainActivity.this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == BillingClient.BillingResponse.OK)
                {
                    //the client is ready
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //TODO implement your own retry policy
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


        List skuList = new ArrayList();
        skuList.add(ITEM_SKU_ADREMOVAL);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                if (responseCode == BillingClient.BillingResponse.OK
                        && skuDetailsList != null) {
                    for (Object skuDetailsObject : skuDetailsList) {
                        SkuDetails skuDetails = (SkuDetails) skuDetailsObject;
                        String sku = skuDetails.getSku();
                        String price = skuDetails.getPrice();
                        if (ITEM_SKU_ADREMOVAL.equals(sku)) {
                            mAdRemovalPrice = price;
                        }
                    }


                }
            }
        });


        mBuyButton = (Button) findViewById(R.id.btnBuy);
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSku(ITEM_SKU_ADREMOVAL)
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                int responseCode = mBillingClient.launchBillingFlow(MainActivity.this, flowParams);
            }
        });

    }



    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
        else
        if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.d("", "User Canceled" + responseCode);
        }
        else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {

            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
        else {
            Log.d("", "Other code" + responseCode);
            // Handle any other error codes.
        }


    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {
            ///mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
            ///setAdFree(true);
            Toast.makeText(this, "Purchase done. you are now a premium member.", Toast.LENGTH_SHORT).show();
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
    }


}
package com.ecommerce.runner.fypproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ecommerce.runner.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class SplashScreenActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 0;
    private ProgressDialog progressDialog;

    //String VerifyRolePathOnServer = "http://10.0.2.2/cashierbookPHP/Eric/staff_login.php";
    private String VerifyRolePathOnServer = "https://ecommercefyp.000webhostapp.com/retailer/staff_login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (isOnline()) {
            Fabric.with(this, new Crashlytics());
            firebaseAuth = FirebaseAuth.getInstance();
            //if user ady login, redirect to main activity
            if (firebaseAuth.getCurrentUser() != null) {
                CashierbookServerAuth();
            } else {
                startFirebaseAuth();
            }
        }else{
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.error)
                    .setTitle("Error")
                    .setMessage("No internet connection")
                    .setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //SplashScreenActivity.this.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    }).setNeutralButton("SETTING", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS),1);
                }
            }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode== RESULT_OK){
                //if firebase auth success, then verify roles at our server
                CashierbookServerAuth();
            }else{
                //login again or exit app
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Exit")
                        .setMessage("Are you sure you want to close this app?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }

                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startFirebaseAuth();
                            }
                        })
                        .show();
                //sent answer to crashlytic
                Answers.getInstance().logLogin(new LoginEvent()
                        .putSuccess(false)
                        .putMethod("Firebase auth exit"));
            }
        }else if(requestCode==1){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    //send request to server for verifying roles and user auth
    private void CashierbookServerAuth() {
        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(SplashScreenActivity.this,"Authenticating user...","Please Wait",false,false);
            }
            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("REsponse", response);
                progressDialog.dismiss();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        //if server and firebase auth success, then subscribe user id for push notification at this device
                        String topic = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        Answers.getInstance().logLogin(new LoginEvent()
                                .putSuccess(true)
                                .putMethod("Firebase and server authentication success"));
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else {
                        //if server auth failed, sign out the firebase auth also
                        FirebaseAuth.getInstance().signOut();

                        // user is now signed out
                        //send answer to crashyltic
                        Answers.getInstance().logLogin(new LoginEvent()
                                .putSuccess(false)
                                .putMethod("Verify Role Failed"));
                        try {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                        } catch (IOException e) {
                            Crashlytics.logException(e);
                            e.printStackTrace();
                        }

                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(SplashScreenActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), SplashScreenActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    // handle your exception here!
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String, String> HashMapParams = new HashMap<>();
                HashMapParams.put("verifyRole", uid);
                String FinalData = ProcessClass.HttpRequest(VerifyRolePathOnServer, HashMapParams);
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }
    //start the firebase auth activity and set the theme and providers
    public void startFirebaseAuth() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setLogo(R.drawable.logo)
                .setIsSmartLockEnabled(false,true)
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(false).build()))
                .build(), RC_SIGN_IN);
    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}

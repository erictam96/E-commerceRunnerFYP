package com.ecommerce.runner.fypproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.runner.fypproject.adapter.PickItem;
import com.ecommerce.runner.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button btnSignOut;

    ImageView pickBut,QRBut,deliveryBut;
    private FirebaseAuth firebaseAuth;
    private String uid;
    //ImageView imageView;
    //EditText editText;
    public static  final int REQUEST_CODE=100;
    public static  final int PERMISSION_REQUEST=200;
    private  String PHPURL="https://ecommercefyp.000webhostapp.com/retailer/customer_function.php";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        GetFirebaseAuth();
        QRBut=findViewById(R.id.QRBut);

        pickBut=findViewById(R.id.pickItemBut);
        deliveryBut=findViewById(R.id.deliveringBut);
        //imageView=findViewById(R.id.qr_img);
       // editText=findViewById(R.id.qr_editTxt);
       // generateBut=findViewById(R.id.qr_generateBut);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage("Checking out delivered item");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        btnSignOut = findViewById(R.id.btn_signout);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }

                // user is now signed out
                Intent myIntent = new Intent(MainActivity.this, SplashScreenActivity.class);
                myIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);
            }
        });

        setTitle("Cashierbook Runner");


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST);
        }

        QRBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ScannerActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });



        pickBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PickItemActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        deliveryBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeliveryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                final Barcode barcode = data.getParcelableExtra("barcode");
                JSON_HTTP_CALL(barcode.displayValue);

            }
        }




    }


    public void JSON_HTTP_CALL(final  String custid) {

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                progressDialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Item Update");
                builder.setMessage(response);
                builder.setIcon(R.mipmap.ic_box);
                builder.setPositiveButton("Scan QR code", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        Intent intent = new Intent(MainActivity.this,ScannerActivity.class);
                        startActivityForResult(intent,REQUEST_CODE);

                    }
                });
                builder.setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();





                Log.d("ResponseDropItem", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();


                JSONObject objectDetail = new JSONObject();
                JSONArray array = new JSONArray();
                try {

                    objectDetail.put("runnerid", uid); //change to runner id in future
                    objectDetail.put("userid", custid);



                    array.put(objectDetail);
                    array.toString();
                }catch (Exception e){

                }



                HashMapParams.put("dropItem",array.toString());
                String FinalData = ProcessClass.HttpRequest(PHPURL, HashMapParams);
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void GetFirebaseAuth(){
        firebaseAuth=FirebaseAuth.getInstance();//get firebase object
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this,R.string.sessionexp,Toast.LENGTH_LONG).show();
        }else uid = firebaseAuth.getCurrentUser().getUid();
    }

}

package com.ecommerce.runner.fypproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ecommerce.runner.fypproject.adapter.DeliveryAdapter;
import com.ecommerce.runner.fypproject.adapter.PickItem;
import com.ecommerce.runner.fypproject.adapter.PickItemAdapter;
import com.ecommerce.runner.fypproject.adapter.UploadProcess;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeliveryActivity extends AppCompatActivity {
    Toolbar contenttoolbar;
    RecyclerView recyclerView;
    DeliveryAdapter deliveryAdapter;
    private  RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private ProgressDialog progressDialog;
    boolean doneLoading=false,isEmpty=false;
    private List<PickItem> pickItemList;
    private  String PHPURL="https://ecommercefyp.000webhostapp.com/retailer/customer_function.php";
    private RecyclerView.Adapter recyclerViewadapter;
    private FirebaseAuth firebaseAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        GetFirebaseAuth();
        recyclerView=findViewById(R.id.deliveryRecycler);
        recyclerView.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

        contenttoolbar = findViewById(R.id.deliveryToolbar);
        setSupportActionBar(contenttoolbar);
        getSupportActionBar().setTitle("Delivery item");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage("preparing delivery item list");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        JSON_HTTP_CALL();
    }

    public void JSON_HTTP_CALL() {

        progressDialog.show();
        doneLoading=false;

        pickItemList=new ArrayList<>();



        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                try {
                    if(response.equalsIgnoreCase("[]")){
                        isEmpty=false;
                    }else{
                        isEmpty=true;
                    }
                    ParseJSonResponse(response);
                } catch (JSONException e) {
                    // Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }
                Log.d("ResponseCheckout", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("getDeliveryList",uid);
                String FinalData = ProcessClass.HttpRequest(PHPURL, HashMapParams);
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }
    //get response string and set into recyclerView
    public void ParseJSonResponse(String array) throws JSONException {
        pickItemList=new ArrayList<>();
        deliveryAdapter=null;
        JSONArray jarr = new JSONArray(array);//lv 1 array


        for(int a=0;a<jarr.length();a++){
            PickItem x=new PickItem();
            JSONObject json = null;
            json=jarr.getJSONObject(a);



            x.setOrderid(json.getString("orderid"));
            x.setCustname(json.getString("custname"));
            x.setItem(json.getString("item"));
            x.setVariant(json.getString("var"));
            x.setQuantity(json.getString("qty"));

            x.setShop(json.getString("shopname"));
            x.setAddress(json.getString("address"));
            x.setRid(json.getString("rid"));
            x.setProdcode(json.getString("prodcode"));
            x.setDeliveryAddress(json.getString("deliveryAddress"));


            pickItemList.add(x);
        }



        deliveryAdapter= new DeliveryAdapter(pickItemList, this);
        recyclerViewadapter =deliveryAdapter;
        recyclerView.setAdapter(recyclerViewadapter);

//        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//            }
//
//        });

        progressDialog.dismiss();
        doneLoading=true;

    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
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

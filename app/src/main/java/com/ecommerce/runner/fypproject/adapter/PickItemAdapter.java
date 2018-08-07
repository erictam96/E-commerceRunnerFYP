package com.ecommerce.runner.fypproject.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.runner.fypproject.R;
import com.ecommerce.runner.fypproject.SplashScreenActivity;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PickItemAdapter extends RecyclerView.Adapter<PickItemAdapter.ViewHolder> {
    List<PickItem> dataAdapters;
    Context context;
    private OnItemClick mCallback;
    boolean[] checked;
    int currentPosition=0;
    private  String PHPURL="https://ecommercefyp.000webhostapp.com/retailer/customer_function.php";

    //http://10.0.2.2/cashierbookPHP/Eric/
    //private String cancelURL="http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    private String cancelURL="https://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //String PHPURL="http://10.0.2.2/cashierbookPHP/Eric/customer_function.php";
    private int selectedIndex=0;
    private FirebaseAuth firebaseAuth;
    private String uid;
    private Dialog delayCancelDialog;
    private Button confirmCancelBut,cancelBut;
    private ProgressBar progressBar;
    int clickedposition=0;
    private CountDownTimer previousTimer;
    private ProgressDialog canceldialog;

    public  PickItemAdapter(List<PickItem> getDataAdapter, Context context, OnItemClick listener){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
        this.mCallback = listener;
        checked=new boolean[dataAdapters.size()];
        if(dataAdapters.size()>0){
            checked[0]=true;

        }

    }
    @Override
    public PickItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_item_card, parent, false);

        PickItemAdapter.ViewHolder viewHolder = new PickItemAdapter.ViewHolder(view);
        GetFirebaseAuth();
        delayCancelDialog();
        canceldialog=new ProgressDialog(context);
        canceldialog.setCancelable(false);
        canceldialog.setTitle(context.getResources().getString(R.string.cancelOrder));
        canceldialog.setMessage(context.getResources().getString(R.string.cancelorderMsg));
        canceldialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PickItemAdapter.ViewHolder Viewholder, final int position) {

        // checked=new boolean[dataAdapters.size()];
        final PickItem dataAdapterOBJ = dataAdapters.get(position);
        // Viewholder.shopIcon.setImageBitmap(dataAdapterOBJ.getprofBitmap());
        // Viewholder.itemImage.setImageBitmap(dataAdapterOBJ.getCoverBitmap());
        Viewholder.orderid.setText(dataAdapterOBJ.getOrderid());
        Viewholder.custname.setText(dataAdapterOBJ.getCustname());
        Viewholder.item.setText(dataAdapterOBJ.getItem());
        Viewholder.variant.setText(dataAdapterOBJ.getVariant());
        Viewholder.quantity.setText(dataAdapterOBJ.getQuantity());
        Viewholder.shop.setText(dataAdapterOBJ.getShop());
        Viewholder.address.setText(dataAdapterOBJ.getAddress());
        Viewholder.deliveryAddress.setText(dataAdapterOBJ.getDeliveryAddress());

        Viewholder.pickBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //http call here
                selectedIndex=position;

                new AlertDialog.Builder(context)
                        .setMessage("Are you sure picking up:\n\nItem: "+dataAdapterOBJ.getItem()+"\nVariant: "+dataAdapterOBJ.getVariant()+"\nQuantity: "+dataAdapterOBJ.getQuantity())
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mCallback.onClick("picking");
                                JSON_HTTP_CALL();
                                Toast.makeText(context,"picked but",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.no), null).show();





            }
        });
//            if(position==0){
//                mCallback.onClick(Integer.toString(Viewholder.addressChk.getId()));
//                Viewholder.addressChk.setChecked(true);
//            }

        Viewholder.soldoutbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dataAdapterOBJ.getOrderId();


                PopupMenu popup = new PopupMenu(context, Viewholder.soldoutbut);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //Toast.makeText(context,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        if(item.getTitle().toString().equalsIgnoreCase("Delete order")){

                            clickedposition=position;
                            delayCancelDialog.show();
                            progressBar.setVisibility(View.VISIBLE);
                            confirmCancelBut.setEnabled(false);
                            previousTimer=new CountDownTimer(10000, 50) {

                                public void onTick(long millisUntilFinished) {
                                    confirmCancelBut.setText(context.getResources().getString(R.string.confirm) + millisUntilFinished / 1000);
                                    confirmCancelBut.setTextColor(context.getResources().getColor(R.color.transparentBlack));
                                    Long x=10000-millisUntilFinished;
                                    progressBar.setProgress(x.intValue());
                                }

                                public void onFinish() {
                                    confirmCancelBut.setEnabled(true);
                                    confirmCancelBut.setText(context.getResources().getString(R.string.confirm));
                                    confirmCancelBut.setTextColor(context.getResources().getColor(R.color.colorWhite));
                                    progressBar.setVisibility(View.GONE);
                                }
                            }.start();
                        }
                        return true;
                    }
                });

                popup.show();

            }
        });





    }
    public void JSON_HTTP_CALL() {

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                mCallback.onClick("picked");
                Log.d("ResponsePickItem", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();


                JSONObject objectDetail = new JSONObject();
                JSONArray array = new JSONArray();
                try {

                    objectDetail.put("runnerid", uid);//change to runner id in future
                    objectDetail.put("orderid", dataAdapters.get(selectedIndex).getOrderid());
                    objectDetail.put("variant", dataAdapters.get(selectedIndex).getVariant());
                    objectDetail.put("prodname", dataAdapters.get(selectedIndex).getItem());


                    array.put(objectDetail);
                    array.toString();
                }catch (Exception e){

                }



                HashMapParams.put("pickItem",array.toString());
                String FinalData = ProcessClass.HttpRequest(PHPURL, HashMapParams);
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }


    class ViewHolder extends RecyclerView.ViewHolder{


        TextView orderid,custname,item,variant,quantity,shop,address,deliveryAddress;
        Button pickBut;
        ImageButton soldoutbut;




        public ViewHolder(View itemView) {

            super(itemView);

            orderid=itemView.findViewById(R.id.pickOrderID);
            custname=itemView.findViewById(R.id.pickCustName);
            item=itemView.findViewById(R.id.itemText);
            variant=itemView.findViewById(R.id.variantText);
            quantity=itemView.findViewById(R.id.qtyText);
            shop=itemView.findViewById(R.id.shopText);
            address=itemView.findViewById(R.id.addressText);
            pickBut=itemView.findViewById(R.id.pickBut);
            soldoutbut=itemView.findViewById(R.id.soldOutBut);
            deliveryAddress=itemView.findViewById(R.id.destinationText);
        }
    }
    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }

    private void GetFirebaseAuth(){
        firebaseAuth= FirebaseAuth.getInstance();//get firebase object
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(context, SplashScreenActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Toast.makeText(context,R.string.sessionexp,Toast.LENGTH_LONG).show();
        }else uid = firebaseAuth.getCurrentUser().getUid();
    }

    public void delayCancelDialog() {
        delayCancelDialog = new Dialog(context, R.style.MaterialDialogSheet);
        delayCancelDialog.setContentView(R.layout.delay_confirm_ui); // your custom view.
        delayCancelDialog.setCancelable(true);
        delayCancelDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        delayCancelDialog.getWindow().setGravity(Gravity.CENTER);

        confirmCancelBut = delayCancelDialog.findViewById(R.id.shortsellConfirmBut);
        cancelBut=delayCancelDialog.findViewById(R.id.shortsellCancelBut);
        progressBar=delayCancelDialog.findViewById(R.id.shortSellProgressBar);


        cancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delayCancelDialog.dismiss();
            }
        });
        confirmCancelBut.setEnabled(false);
        confirmCancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String prodvariant=dataAdapters.get(clickedposition).getVariant();
                final String prodcode= dataAdapters.get(clickedposition).getProdcode();
                final String retailerid=dataAdapters.get(clickedposition).getRid();
                //mCallback.onClick("cancel",prodvariant,prodcode,retailerid);
                delayCancelDialog.dismiss();
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.cancelOrderWarning))
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                JSON_CANCEL(prodvariant,prodcode,retailerid);
                                Log.e("cancel detail","prodvar:"+prodvariant+" prodcode: "+prodcode+" rid: "+retailerid);
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.no), null).show();
            }
        });
        delayCancelDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                previousTimer.cancel();
            }
        });

    }

    public void JSON_CANCEL(final String prodvar, final String prodcode, final String rid){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response",response);


                canceldialog.dismiss();
                Toast.makeText(context,context.getResources().getString(R.string.doneCancel),Toast.LENGTH_SHORT).show();
                mCallback.onClick("refresh");
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("prodvariant",prodvar);
                HashMapParams.put("prodcode",prodcode);
                HashMapParams.put("retailerid",rid);

                String FinalData = imageProcessClass.HttpRequest(cancelURL, HashMapParams);
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }


}
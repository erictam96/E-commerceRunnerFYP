package com.ecommerce.runner.fypproject.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.runner.fypproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {
    List<PickItem> dataAdapters;
    Context context;

    public  DeliveryAdapter(List<PickItem> getDataAdapter, Context context){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;

    }
    @Override
    public DeliveryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_item_card, parent, false);

        DeliveryAdapter.ViewHolder viewHolder = new DeliveryAdapter.ViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final DeliveryAdapter.ViewHolder Viewholder, final int position) {

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


//            if(position==0){
//                mCallback.onClick(Integer.toString(Viewholder.addressChk.getId()));
//                Viewholder.addressChk.setChecked(true);
//            }

        Viewholder.soldoutbut.setVisibility(View.INVISIBLE);
        Viewholder.pickBut.setVisibility(View.GONE);
        Viewholder.line.setVisibility(View.GONE);




    }

    class ViewHolder extends RecyclerView.ViewHolder{


        TextView orderid,custname,item,variant,quantity,shop,address,deliveryAddress;
        Button pickBut;
        ImageButton soldoutbut;
        View line;




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
            line=itemView.findViewById(R.id.line_below_delivery_address);
        }
    }
    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }

}

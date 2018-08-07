package com.ecommerce.runner.fypproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.ecommerce.runner.fypproject.adapter.BoxDetector;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScannerActivity extends AppCompatActivity{
SurfaceView cameraView;
BarcodeDetector barcode;
CameraSource cameraSource;
SurfaceHolder surfaceHolder;
BarcodeDetector barcodeDetector;
BoxDetector boxDetector;
    Toolbar contenttoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        contenttoolbar = findViewById(R.id.scannerToolbar);
        setSupportActionBar(contenttoolbar);
        getSupportActionBar().setTitle("Drop item");
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        cameraView=findViewById(R.id.cameraView);
        cameraView.setZOrderMediaOverlay(true);
        surfaceHolder=cameraView.getHolder();

        barcodeDetector = new BarcodeDetector.Builder(this).build();
        if(!barcodeDetector.isOperational()){
            Toast.makeText(this,"setup camera fail",Toast.LENGTH_SHORT).show();
            finish();
        }
        boxDetector = new BoxDetector(barcodeDetector, 800, 800); //QR scanner frame size

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        cameraSource=new CameraSource
                .Builder(this,boxDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(height,width)

                .build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    if(ContextCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        cameraSource.start(cameraView.getHolder());
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


        Detector.Processor detect=new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes =  detections.getDetectedItems();

                if(barcodes.size() > 0){
                    Intent intent = new Intent();
                    intent.putExtra("barcode", barcodes.valueAt(0));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }


        };

        boxDetector.setProcessor(detect);







    }
    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

}

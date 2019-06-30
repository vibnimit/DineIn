package com.example.vibhu.dinein;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Boolean listIsEmpty = true;
    static List<TextDetection> textDetections;
    Bitmap bitmapOrg;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    protected Location mLastLocation;
    private final ArrayList<String> restaurants = new ArrayList<>();
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ListView listView=(ListView)findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, restaurants);

        final Intent intent = new Intent(this, CameraActivity.class);
//assign adapter to listview
        listView.setAdapter(arrayAdapter);

//add listener to listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(MainActivity.this,"clicked item:"+i+" "+restaurants.get(i).toString(),Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
        });


    // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:fccc8659-b7e0-4a39-a8ef-f1702979ead2", // Identity pool ID
                Regions.US_EAST_1 // Region
        );


        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("AWSINIT", "onResult: " + userStateDetails.getUserState());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("AWSINIT", "Initialization error.", e);
                    }
                }
        );

        bitmapOrg = BitmapFactory.decodeResource(getResources(),  R.drawable.image);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();


        AmazonRekognition client = new AmazonRekognitionClient(credentialsProvider);


        Pack p = new Pack(client, bao);

        new GetResultsfromRekognition().execute(p);

    }

    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload("crapatangelhack",
                        "menu.jpg",
                        new File("/path/to/file/localFile.txt"));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("AWS_UPLOAD", "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d("AWS_UPLOAD", "Bytes Total: " + uploadObserver.getBytesTotal());
    }

        @Override
        public void onStart() {
            super.onStart();

            if (!checkPermissions()) {
                requestPermissions();
                getLastLocation();
            } else {
                getLastLocation();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            getLastLocation();
//        Toast.makeText(getApplicationContext()," *********************** "+permissions ,Toast.LENGTH_LONG).show();

        }

        private boolean checkPermissions() {
            int permissionState = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return permissionState == PackageManager.PERMISSION_GRANTED;
        }


        private void requestPermissions() {
            boolean shouldProvideRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION);
            System.out.println(shouldProvideRationale);

            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
//        }
        }


        private void startLocationPermissionRequest() {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }

        private void getLastLocation() {
            int permissionState = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if(permissionState == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    mLastLocation = task.getResult();
//                                Toast.makeText(getApplicationContext()," "+mLastLocation ,Toast.LENGTH_SHORT).show();

                                    restaurants.add("Madras Cafe");
                                    restaurants.add("Sakoon");
                                    restaurants.add("Shaan");

                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        });
            }
        }

    class GetResultsfromRekognition extends AsyncTask<Pack, Integer, DetectTextResult> {
        protected DetectTextResult doInBackground(Pack... packs) {

            S3Object s3Object = new S3Object();
            s3Object.setBucket("crapatangelhack");
            s3Object.setName("image.jpg");

            DetectTextRequest request = new DetectTextRequest()
                    .withImage(new Image().withS3Object(s3Object));

            DetectTextResult resu = packs[0].getAr().detectText(request);
            textDetections = resu.getTextDetections();

            Log.d("AWSREKOG", "List : " + textDetections);
            if (textDetections.isEmpty()) {
                listIsEmpty = true;
                Log.d("AWSREKOG", "List is Empty");
            }

            for (TextDetection text : textDetections) {
                Log.d("AWSREKOGnition", text.getDetectedText());
                //rekognitionMap.put(text.getDetectedText(), text.getConfidence().toString());
            }
            return resu;
        }
    }
}


class Pack{

    private AmazonRekognition ar;
    private ByteArrayOutputStream ba;

    Pack(AmazonRekognition a, ByteArrayOutputStream b){
        this.ar = a;
        this.ba = b;
    }

    public AmazonRekognition getAr() {
        return ar;
    }

    public ByteArrayOutputStream getBa(){
        return ba;
    }
}
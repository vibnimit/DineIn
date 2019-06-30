package com.example.vibhu.dinein;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.support.constraint.Constraints.TAG;

public class CameraActivity extends Activity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private static final int CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE = 28;
    private static final int STORAGE_REQUEST_PERMISSIONS_REQUEST_CODE = 24;
    CognitoCachingCredentialsProvider credentialsProvider;

    Boolean listIsEmpty = true;
    static List<TextDetection> textDetections;
    Bitmap bitmapOrg;
    Boolean gotResults;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_fragment);
//        Toast.makeText(this, "Please provideD the permission", Toast.LENGTH_LONG).show();

        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE);
//            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{}, STORAGE_REQUEST_PERMISSIONS_REQUEST_CODE);
        }
        else {
            // Create an instance of Camera
            startCamera();
        }

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE)
        startCamera();
//        Toast.makeText(getApplicationContext()," *********************** "+permissions ,Toast.LENGTH_LONG).show();

    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DineIn");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@ Direc  :"+ mediaStorageDir);
//        Toast.makeText(.(), "Directory", Toast.LENGTH_LONG).show();
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("TAG", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@ File  :"+ mediaFile);
        return mediaFile;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            Toast.makeText(this, "Take", Toast.LENGTH_LONG).show();
            System.out.print("@@@@@@@@@@@@@@@@@@@@@@ Taken");
            if (pictureFile == null){
                Log.d("TAG", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                // TODO : Make s3 upload call
                uploadWithTransferUtility(pictureFile);

            } catch (FileNotFoundException e) {
                Log.d("TAG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }

            System.out.print("@@@@@@@@@@@@@@@@@@@@@@ Ended");
        }
    };

    private void startCamera(){
        mCamera = getCameraInstance();
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        Button captureButton = (Button) findViewById(R.id.button_capture);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                Log.d("TAG", "Firing");
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }



    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public void uploadWithTransferUtility(File uploadFile) {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload("crapatangelhack",
                        "menu.jpg",
                        uploadFile);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d("TAG", "Upload complete");
                    startRekog();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("TAG", "ID:" + id + " bytesCurrent: " + bytesCurrent
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

        Log.d("TAG", "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d("TAG", "Bytes Total: " + uploadObserver.getBytesTotal());
    }


    private void startRekog(){

        AmazonRekognition client = new AmazonRekognitionClient(credentialsProvider);

        Pack p = new Pack(client);

        new GetResultsfromRekognition().execute(p);
    }

    private void apiCall(){

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://d272a717.ngrok.io";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", "API SUCCESSS");
                        // TODO : Collect all data and send using intent
                        // hashmap, string
                        //Log.d("TAG", response);

                        String[] s = {"Idly", "Upma", "Podi"};

                        // TODO : CHANGE ACTIVITY NAME
                        Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                        i.putExtra("api", response);
                        i.putExtra("rekog", s);
                        startActivity(i);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Error
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    class GetResultsfromRekognition extends AsyncTask<Pack, Integer, DetectTextResult> {
        protected DetectTextResult doInBackground(Pack... packs) {

            S3Object s3Object = new S3Object();
            s3Object.setBucket("crapatangelhack");
            s3Object.setName("menu.jpg");

            DetectTextRequest request = new DetectTextRequest()
                    .withImage(new Image().withS3Object(s3Object));

            DetectTextResult resu = packs[0].getAr().detectText(request);
            textDetections = resu.getTextDetections();
            gotResults = true;

            Log.d("TAG", "List : " + textDetections);
            if (textDetections.isEmpty()) {
                listIsEmpty = true;
                Log.d("TAG", "List is Empty");
            }

            for (TextDetection text : textDetections) {
                Log.d("TAG", text.getDetectedText());
                apiCall();
                //rekognitionMap.put(text.getDetectedText(), text.getConfidence().toString());
            }
            return resu;
        }
    }

}



class Pack{

    private AmazonRekognition ar;

    Pack(AmazonRekognition a){
        this.ar = a;
    }

    public AmazonRekognition getAr() {
        return ar;
    }
}
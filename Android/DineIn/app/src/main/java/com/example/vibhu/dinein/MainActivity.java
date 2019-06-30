package com.example.vibhu.dinein;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Boolean listIsEmpty = true;
    static List<TextDetection> textDetections;
    Bitmap bitmapOrg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
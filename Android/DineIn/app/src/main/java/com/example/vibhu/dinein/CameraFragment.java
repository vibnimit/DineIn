//package com.example.vibhu.dinein;
//
//import android.hardware.Camera;
//import android.support.v4.app.Fragment;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.ImageFormat;
//import android.graphics.Matrix;
//import android.hardware.Camera;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.Toast;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.List;
//
//
//public class CameraFragment extends Fragment implements SurfaceHolder.Callback {
//
//    Camera camera;
//
//
//    Camera.PictureCallback jpegCallback;
//
//    SurfaceView mSurfaceView;
//    SurfaceHolder mSurfaceHolder;
//
//
//
//    final int CAMERA_REQUEST_CODE = 1;
//    public static CameraFragment newInstance(){
//        CameraFragment fragment = new CameraFragment();
//        return fragment;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.camera_fragment , container, false);
//        Button mCapture = view.findViewById(R.id.capture);
//        mSurfaceView = view.findViewById(R.id.surfaceView);
//        mSurfaceHolder = mSurfaceView.getHolder();
//
//        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(getActivity(), new String[] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
//        }else{
//            mSurfaceHolder.addCallback(this);
//            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        }
//
////        Button mLogout = view.findViewById(R.id.logout);
////        Button mFindUsers = view.findViewById(R.id.findUsers);
////        Button mCapture = view.findViewById(R.id.capture);
////
////        mLogout.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                LogOut();
////            }
////        });
////        mLogout.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                LogOut();
////            }
////        });
////        mFindUsers.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                FindUsers();
////            }
////        });
//        mCapture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                captureImage();
//            }
//        });
//
//
//        jpegCallback = new Camera.PictureCallback(){
//            @Override
//            public void onPictureTaken(byte[] bytes, Camera camera) {
//
//                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//                System.out.println(decodedBitmap);
//
////                Bitmap rotateBitmap = rotate(decodedBitmap);
////
////                String fileLocation = SaveImageToStorage(rotateBitmap);
////                if(fileLocation!= null){
////                    Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
////                    startActivity(intent);
////                    return;
////                }
//            }
//        };
//
//        return view;
//    }
//
//    private void captureImage() {
//        camera.takePicture(null, null, jpegCallback);
//    }
//
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        camera = Camera.open();
//
//        Camera.Parameters parameters;
//        parameters = camera.getParameters();
//
//        camera.setDisplayOrientation(90);
//        parameters.setPreviewFrameRate(30);
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//
//
//        Camera.Size bestSize = null;
//        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
//        bestSize = sizeList.get(0);
//        for(int i = 1; i < sizeList.size(); i++){
//            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
//                bestSize = sizeList.get(i);
//            }
//        }
//
//        parameters.setPictureSize(bestSize.width, bestSize.height);
//
//
//        camera.setParameters(parameters);
//
//        try {
//            camera.setPreviewDisplay(mSurfaceHolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        camera.startPreview();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//            case CAMERA_REQUEST_CODE:{
//                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    mSurfaceHolder.addCallback(this);
//                    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//                }else{
//                    Toast.makeText(getContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
//                }
//                break;
//            }
//        }
//    }
//
//}

//package com.example.wang.opencvdemo1.store;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.ImageView;
//
//import com.example.wang.opencvdemo1.R;
//
//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.CameraBridgeViewBase;
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;
//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;
//
//public class MainActivitySource extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//
//
////    static{
////        if(OpenCVLoader.initDebug()){
////
////        }else{
////            System.loadLibrary("my_jni_lib1");
////            System.loadLibrary("my_jni_lib2");
////        }
////    }
//    private static final String TAG = "MainActivity";
//
//    private static boolean flag = true;
//    private static boolean isFirst = true;
//    private Bitmap srcBitmap;
//    private Bitmap grayBitmap;
//    private ImageView imageView;
//    private Button button;
//    private Button button1;
//    private CameraBridgeViewBase mOpenCvCameraView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setContentView(R.layout.activity_main);
//
////        Mat img = imread(filename);
//
//        button = (Button) findViewById(R.id.btn);
//        button1 = (Button) findViewById(R.id.btn1);
//        imageView = (ImageView) findViewById(R.id.iv);
//        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
//        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
//        mOpenCvCameraView.setCvCameraViewListener(this);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(isFirst)
//                {
//                    procSrc2Gray();
//                    isFirst = false;
//                }
//                if(flag){
//                    imageView.setImageBitmap(grayBitmap);
//                    button1.setText("Origin");
//                    flag = false;
//                }
//                else{
//                    imageView.setImageBitmap(srcBitmap);
//                    button1.setText("Grey");
//                    flag = true;
//                }
//            }
//        });
//
//    }
//
//    public void procSrc2Gray(){
//        Mat rgbMat = new Mat();
//        Mat grayMat = new Mat();
//        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
//        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
//        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
//        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
//        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
//        Log.i(TAG, "procSrc2Gray sucess...");
//    }
//    @Override
//    protected void onResume() {
//        // TODO Auto-generated method stub
//        super.onResume();
//        //load OpenCV engine and init OpenCV library
//        isFirst = true;
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
//        Log.i(TAG, "onResume sucess load OpenCV...");
//
//    }
//
//    @Override
//    public void onPause()
//    {
//        super.onPause();
//        if (mOpenCvCameraView != null)
//            mOpenCvCameraView.disableView();
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        if (mOpenCvCameraView != null)
//            mOpenCvCameraView.disableView();
//    }
//
//    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//
//        @Override
//        public void onManagerConnected(int status) {
//            // TODO Auto-generated method stub
//            switch (status) {
//                case BaseLoaderCallback.SUCCESS:
//                    Log.i(TAG, "Load success");
//                    mOpenCvCameraView.enableView();
//                    break;
//                default:
//                    super.onManagerConnected(status);
//                    Log.i(TAG, "Load fail");
//                    break;
//            }
//
//        }
//    };
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        return inputFrame.rgba();
//    }
//}

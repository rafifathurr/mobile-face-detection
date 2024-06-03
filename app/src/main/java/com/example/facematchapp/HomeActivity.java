package com.example.facematchapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends CameraActivity {

    JavaCameraView cameraBridgeViewBase;
    ImageButton flipBtn;
    int cameraIndex;
    File caseFile;
    CascadeClassifier faceDetector;
    private Mat mRgba, mGrey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cameraBridgeViewBase = findViewById(R.id.opencvCameraView);
        flipBtn = findViewById(R.id.switchBtn);

        cameraIndex = 0;

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                mRgba = new Mat();
                mGrey = new Mat();
            }

            @Override
            public void onCameraViewStopped() {
                mRgba.release();
                mGrey.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                mRgba = inputFrame.rgba();
                mGrey = inputFrame.gray();

                //detect Face
                MatOfRect facedetections = new MatOfRect();
                faceDetector.detectMultiScale(mRgba, facedetections);

                for (Rect react : facedetections.toArray()) {
                    Imgproc.rectangle(mRgba, new Point(react.x, react.y),
                            new Point(react.x + react.width, react.y + react.height),
                            new Scalar(255, 0, 0));
                }

                return mRgba;
            }
        });

        flipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraIndex == 0) {
                    cameraIndex = 1;
                } else {
                    cameraIndex = 0;
                }

                cameraBridgeViewBase.disableView();
                cameraBridgeViewBase.setCameraIndex(cameraIndex);
                cameraBridgeViewBase.enableView();
            }
        });

        if (OpenCVLoader.initLocal()) {
            try {
                InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                caseFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

                FileOutputStream fos = new FileOutputStream(caseFile);

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                is.close();
                fos.close();

                faceDetector = new CascadeClassifier(caseFile.getAbsolutePath());
                if (faceDetector.empty()) {
                    faceDetector = null;
                } else {
                    cascadeDir.delete();
                }

                cameraBridgeViewBase.disableView();
                cameraBridgeViewBase.setCameraIndex(cameraIndex);
                cameraBridgeViewBase.enableView();
            } catch (IOException e) {
                Log.e("ERROR DETECT", "onCreate: " + e.getMessage() );
            }
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

}
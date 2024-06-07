package com.example.facematchapp;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.FaceDetectorYN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends CameraActivity {
    int cameraIndex = CameraBridgeViewBase.CAMERA_ID_FRONT;
    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    CascadeClassifier cascadeClassifier;
    private Mat rgba, gray, transpose_rgba, transpose_gray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Window Manager
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_home);

        // Setting Up Camera View
        mOpenCvCameraView = findViewById(R.id.opencvCameraView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);

        // Initialize camera view listener
        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                rgba = new Mat();
                gray = new Mat();
            }

            @Override
            public void onCameraViewStopped() {
                rgba.release();
                gray.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                rgba = inputFrame.rgba();
                gray = inputFrame.gray();

                // Setup min size scope face detection
                int height = gray.rows();
                int faceSize = Math.round(height * 0.7F);

                // Transpose gray scale flip for selfie camera
                transpose_gray = gray.clone();
                Core.transpose(gray, transpose_gray);
                Core.flip(transpose_gray, transpose_gray, -1);

                // Transpose rgba scale flip for selfie camera
                transpose_rgba = rgba.clone();
                Core.transpose(rgba, transpose_rgba);
                Core.flip(transpose_rgba, transpose_rgba, -1);

                // Setup Rect coordinate
                MatOfRect rects = new MatOfRect();

                // Detection Face Begin
                cascadeClassifier.detectMultiScale(transpose_gray, rects, 1.3, 5, 0, new Size(faceSize, faceSize), new Size());

                // Each Rect
                for (Rect rect : rects.toList()) {

                    // Draw rectangle face detection
                    Imgproc.rectangle(transpose_rgba, rect,
                            new Scalar(0, 255, 0), 5);
                }

                // Return RGBA camera view transpose
                return transpose_rgba.t();
            }
        });

        // Important for configuration camera
        if (OpenCVLoader.initLocal()) {

            // Open and set camera view
            mOpenCvCameraView.setCameraIndex(cameraIndex);
            mOpenCvCameraView.enableView();

            try {

                // Open stream haarcascade model face detector
                InputStream inputStream = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                File file = new File(getDir("cascade", MODE_PRIVATE), "haarcascade_frontalface_default.xml");
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] data = new byte[4096];
                int read_byte;

                // Generate byte output stream
                while ((read_byte = inputStream.read(data)) != -1) {
                    fileOutputStream.write(data, 0, read_byte);
                }

                // Initialize cascade file
                cascadeClassifier = new CascadeClassifier(file.getAbsolutePath());

                if (cascadeClassifier.empty()) {
                    cascadeClassifier = null;
                }

                // Close all file input and output stream
                inputStream.close();
                fileOutputStream.close();
                file.delete();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }
}
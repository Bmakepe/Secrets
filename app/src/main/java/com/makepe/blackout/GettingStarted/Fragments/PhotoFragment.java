package com.makepe.blackout.GettingStarted.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.YuvImage;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.R;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

public class PhotoFragment extends Fragment {

    private CameraView cameraView;
    private ImageView flipCamera, flashBTN, photoButton;
    private TextView cameraFlashState, timerText;
    private byte[] imageByte;
    private RelativeLayout cameraFlashArea, cameraTimerArea, addFilterArea, flipCameraArea;
    private RadioGroup imageVideoBTN;
    private RadioButton selectedMediaType;
    private boolean isRecording = false;

    private int[] colorIntArray = {R.color.colorBlack, R.color.colorBlack};
    private int[] iconIntArray = {R.drawable.ic_camera_black_24dp, R.drawable.ic_baseline_fiber_manual_record_24};


    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        cameraView = view.findViewById(R.id.cameraKit);
        photoButton = view.findViewById(R.id.takePic);
        flipCamera = view.findViewById(R.id.flipCamera);
        flashBTN = view.findViewById(R.id.switchFlashButton);
        cameraFlashState = view.findViewById(R.id.cameraFlashState);
        cameraFlashArea = view.findViewById(R.id.cameraFlashArea);
        cameraTimerArea = view.findViewById(R.id.cameraTimerArea);
        addFilterArea = view.findViewById(R.id.addFilterArea);
        flipCameraArea = view.findViewById(R.id.flipCameraArea);
        timerText = view.findViewById(R.id.timerText);
        imageVideoBTN = view.findViewById(R.id.mediaSelector);

        flipCamera.setTag("back_facing");
        flashBTN.setTag("flash_off");

        cameraView.setZoom(CameraKit.Constants.ZOOM_PINCH);
        cameraView.setFocus(CameraKit.Constants.FOCUS_TAP);

        imageVideoBTN.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.photoMediaBTN:
                        photoButton.setTag("photoSelected");
                        //photoButton.setImageResource(R.drawable.ic_camera_black_24dp);
                        animatePhotoBTN(0);
                        break;
                    case R.id.videoMediaBTN:
                        photoButton.setTag("videoSelected");
                        animatePhotoBTN(1);
                        //photoButton.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
                        break;

                    default:
                        Toast.makeText(getContext(), "Unknown media type selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened() {
                super.onCameraOpened();
            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);

                imageByte = jpeg;
                //Bitmap result = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

                getByteImage(jpeg);

                Intent camIntent = new Intent(getActivity(), PostActivity.class);
                startActivity(camIntent);
            }

            @Override
            public void onPictureTaken(YuvImage yuv) {
                super.onPictureTaken(yuv);
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (photoButton.getTag().equals("photoSelected")){

                    if (timerText.getText().toString().equals("00:00"))
                        cameraView.captureImage();
                    else
                        Toast.makeText(getActivity(), "The count down will begin and capture your picture", Toast.LENGTH_SHORT).show();

                }else if (photoButton.getTag().equals("videoSelected")){

                    if (!isRecording){
                        cameraView.startRecordingVideo();
                        photoButton.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                    }else{
                        cameraView.stopRecordingVideo();
                        photoButton.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
                    }

                }

            }
        });

        flipCameraArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flipCamera.getTag().equals("front_facing")){

                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    flipCamera.setTag("back_facing");
                    cameraFlashArea.setVisibility(View.VISIBLE);

                }else if (flipCamera.getTag().equals("back_facing")){

                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    flipCamera.setTag("front_facing");

                    if(!hasFlash(requireContext())) {
                        cameraFlashArea.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        cameraFlashArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(flashBTN.getTag().equals("flash_off")){

                    flashBTN.setTag("flash_auto");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_off_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                    cameraFlashState.setText(flashBTN.getTag().toString());

                }else if (flashBTN.getTag().equals("flash_auto")){

                    flashBTN.setTag("flash_on");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_auto_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);
                    cameraFlashState.setText(flashBTN.getTag().toString());

                }else if(flashBTN.getTag().equals("flash_on")){

                    flashBTN.setTag("flash_torch");
                    flashBTN.setImageResource(R.drawable.ic_baseline_flash_on_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                    cameraFlashState.setText(flashBTN.getTag().toString());

                }else if(flashBTN.getTag().equals("flash_torch")){

                    flashBTN.setTag("flash_off");
                    flashBTN.setImageResource(R.drawable.ic_baseline_highlight_24);

                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                    cameraFlashState.setText(flashBTN.getTag().toString());
                }
            }
        });

        addFilterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "You will be able to add filters", Toast.LENGTH_SHORT).show();

            }
        });

        cameraTimerArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimerMenu();
            }
        });

        return view;
    }

    private void animatePhotoBTN(int position){
        photoButton.clearAnimation();

        ScaleAnimation shrink = new ScaleAnimation(1f, 0.1f, 1f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(100);
        shrink.setInterpolator(new AccelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                photoButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), colorIntArray[position]));
                photoButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), iconIntArray[position]));

                // Rotate Animation
                Animation rotate = new RotateAnimation(60.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                rotate.setDuration(150);
                rotate.setInterpolator(new DecelerateInterpolator());

                // Scale up animation
                ScaleAnimation expand = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(150);     // animation duration in milliseconds
                expand.setInterpolator(new DecelerateInterpolator());

                // Add both animations to animation state
                AnimationSet s = new AnimationSet(false); //false means don't share interpolators
                s.addAnimation(rotate);
                s.addAnimation(expand);
                photoButton.startAnimation(s);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        photoButton.startAnimation(shrink);
    }

    private boolean hasFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void showTimerMenu() {
        PopupMenu popupMenu = new PopupMenu(getActivity(), cameraTimerArea, Gravity.HORIZONTAL_GRAVITY_MASK);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Default");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "3 Seconds");
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "5 Seconds");
        popupMenu.getMenu().add(Menu.NONE, 3, 0, "10 Seconds");
        popupMenu.getMenu().add(Menu.NONE, 4, 0, "30 Seconds");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case 0:
                        timerText.setText("00:00");
                        timerText.setVisibility(View.GONE);
                        break;
                    case 1:
                        timerText.setText("00:03");
                        timerText.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        timerText.setText("00:05");
                        timerText.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        timerText.setText("00:10");
                        timerText.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        timerText.setText("00:30");
                        timerText.setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public byte[] getByteImage(byte[] jpeg) {
        return jpeg;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }
}
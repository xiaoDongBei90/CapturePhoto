package com.fusheng.capturephoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author LiXiaoWei
 * @date 2018/9/10
 * desc:
 */

public class MaintainActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomSheetDialog bsd;
    private ImageView ivResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initView() {
        ivResult = findViewById(R.id.iv_result);
        initDialog();
    }

    private void initDialog() {
        View bsdView = LayoutInflater.from(this).inflate(R.layout.dialog_capture_photo, null);
        bsd = new BottomSheetDialog(this);
        bsd.setContentView(bsdView);
        bsdView.findViewById(R.id.tv_take_photo).setOnClickListener(this);
        bsdView.findViewById(R.id.tv_select_photo).setOnClickListener(this);
        bsdView.findViewById(R.id.tv_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_take_photo:
                checkCameraPermission(new OnCheckCameraPermission() {
                    @Override
                    public void onCheckCameraPression(boolean haspermission) {
                        if (haspermission) {
                            checkStoragePermission(new OnCheckStoragePermission() {
                                @Override
                                public void onCheckStoragePression(boolean haspermission) {
                                    if (haspermission) {
                                        takePhoto();
                                    }
                                }
                            });
                        }
                    }
                });
                Toast.makeText(MaintainActivity.this, "拍照", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_select_photo:
                checkStoragePermission(new OnCheckStoragePermission() {
                    @Override
                    public void onCheckStoragePression(boolean haspermission) {
                        if (haspermission) {
                            selectPhoto();
                        }
                    }
                });
                Toast.makeText(MaintainActivity.this, "从相册选取", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_cancel:
                if (bsd != null && bsd.isShowing()) {
                    bsd.dismiss();
                }
                break;
            default:
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MaintainActivity.this, CustomCameraActivity.class);
        startActivityForResult(intent, Constant.TAKE_PHOTO_REQUEST);

       /* File picFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/test/" + System.currentTimeMillis() + ".jpg");
        picFile.getParentFile().mkdirs();
        Uri uri = Uri.fromFile(picFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);*/
    }

    private void selectPhoto() {

    }

    private void initListener() {
        findViewById(R.id.btn_change_portrait).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission(new OnCheckCameraPermission() {
                    @Override
                    public void onCheckCameraPression(boolean haspermission) {
                        if (haspermission) {
                            checkStoragePermission(new OnCheckStoragePermission() {
                                @Override
                                public void onCheckStoragePression(boolean haspermission) {
                                    if (haspermission) {
                                        takePhoto();
                                    }
                                }
                            });
                        }
                    }
                });
                /*if (bsd != null && !bsd.isShowing()) {
                    bsd.show();
                }*/
            }
        });
    }

    private OnCheckCameraPermission mOnCheckCameraPermission;
    private OnCheckStoragePermission mOnCheckStoragePermission;
    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public void checkCameraPermission(OnCheckCameraPermission listener) {
        mOnCheckCameraPermission = listener;
        int checkSelfPermission = ContextCompat.checkSelfPermission(this, permission[0]);
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            mOnCheckCameraPermission.onCheckCameraPression(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constant.PERMISSION_CAMERA);
        }
    }

    public void checkStoragePermission(OnCheckStoragePermission listener) {
        mOnCheckStoragePermission = listener;
        int checkSelfPermission = ContextCompat.checkSelfPermission(this, permission[1]);
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            mOnCheckStoragePermission.onCheckStoragePression(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.PERMISSION_STORAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.TAKE_PHOTO_REQUEST) {
            String picPath = data.getStringExtra("picPath");
            try {
                FileInputStream fis = new FileInputStream(picPath);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ivResult.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permission = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case Constant.PERMISSION_CAMERA:
                if (mOnCheckCameraPermission != null) {
                    mOnCheckCameraPermission.onCheckCameraPression(permission);
                }
                break;
            case Constant.PERMISSION_STORAGE:
                if (mOnCheckStoragePermission != null) {
                    mOnCheckStoragePermission.onCheckStoragePression(permission);
                }
                break;
            default:
        }
    }

    /**
     * 拍照权限申请后回调
     **/
    public interface OnCheckCameraPermission {
        /**
         * @param haspermission true 允许  false 拒绝
         **/
        void onCheckCameraPression(boolean haspermission);
    }

    /**
     * 读写SD卡权限申请后回调
     **/
    public interface OnCheckStoragePermission {
        /**
         * @param haspermission true 允许  false 拒绝
         **/
        void onCheckStoragePression(boolean haspermission);
    }
}

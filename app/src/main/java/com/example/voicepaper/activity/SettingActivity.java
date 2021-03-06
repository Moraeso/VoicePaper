package com.example.voicepaper.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voicepaper.R;
import com.example.voicepaper.fragment.login.SignUpFragment;
import com.example.voicepaper.fragment.setting.PasswordSettingFragment;
import com.example.voicepaper.manager.AppManager;
import com.example.voicepaper.manager.ImageManager;
import com.example.voicepaper.network.AsyncCallback;
import com.example.voicepaper.network.UploadFile;
import com.example.voicepaper.util.ConfirmDialog;

import java.io.IOException;

public class SettingActivity extends AppCompatActivity implements Button.OnClickListener {
    ImageView iv_userImage;

    TextView tv_userId;
    TextView tv_userName;

    Button btn_changeImage;
    Button btn_changePw;
    Button btn_inquire;
    Button btn_about;
    Button btn_signOut;

    private String albumImagePath;
    private ConfirmDialog confirmDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppManager.getInstance().setContext(this);
        AppManager.getInstance().setResources(getResources());

        setContentView(R.layout.activity_setting);

        initView();
        initListener();
    }

    void initView(){
        iv_userImage = findViewById(R.id.iv_userImage);
        tv_userId = findViewById(R.id.tv_userId);
        tv_userName = findViewById(R.id.tv_userName);

        btn_changeImage = findViewById(R.id.btn_changeImage);
        btn_changePw = findViewById(R.id.btn_changePw);
        btn_inquire = findViewById(R.id.btn_inquire);
        btn_about = findViewById(R.id.btn_about);
        btn_signOut = findViewById(R.id.btn_signOut);

        tv_userId.setText(AppManager.getInstance().getUser().getID());
        tv_userName.setText(AppManager.getInstance().getUser().getName());

        if (AppManager.getInstance().getUser().getProfileString().equals("undefined")) {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_user_main);
            iv_userImage.setImageBitmap(((BitmapDrawable) drawable).getBitmap());
        } else {
            String url = ImageManager.getInstance().getFullImageString(AppManager.getInstance().getUser().getProfileString(), "file/userimage");
            ImageManager.getInstance().GlideInto(AppManager.getInstance().getContext(), iv_userImage, url);
        }
    }

    void initListener(){
        btn_changeImage.setOnClickListener(this);
        btn_changePw.setOnClickListener(this);
        btn_inquire.setOnClickListener(this);
        btn_about.setOnClickListener(this);
        btn_signOut.setOnClickListener(this);

        confirmDialog = new ConfirmDialog(AppManager.getInstance().getContext());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_changeImage:
                doTakeAlbumAction();
                break;
            case R.id.btn_changePw:
                changePassword();
                break;
            case R.id.btn_inquire:
                inquire();
                break;
            case R.id.btn_about:
                about();
                break;
            case R.id.btn_signOut:
                AppManager.getInstance().getMainActivity().finish();
                Intent intent = new Intent(AppManager.getInstance().getContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    // 앨범에서 이미지 가져오기
    private void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, ImageManager.PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == ((Activity) AppManager.getInstance().getContext()).RESULT_OK) {
            if (requestCode == ImageManager.PICK_FROM_ALBUM) {
                //앨범 선택
                Uri uri = data.getData();
                ExifInterface exif = null;
                albumImagePath = ImageManager.getInstance().getRealPathFromURI(AppManager.getInstance().getContext(), uri);
                try {
                    exif = new ExifInterface(albumImagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = ImageManager.getInstance().exifOrientationToDegrees(exifOrientation);
                iv_userImage.setBackgroundColor(Color.WHITE);
                Bitmap bitmap = BitmapFactory.decodeFile(albumImagePath);
                iv_userImage.setImageBitmap(ImageManager.getInstance().rotate(bitmap, exifDegree));

                GradientDrawable drawable =
                        (GradientDrawable) AppManager.getInstance().getContext().getDrawable(R.drawable.custom_rounded);
                iv_userImage.setBackground(drawable);
                iv_userImage.setClipToOutline(true);

                uploadUserImage(AppManager.getInstance().getUser().getID());
            }
        }
    }

    private void uploadUserImage(String userId) {
        ContentValues values = new ContentValues();
        values.put("userId", userId);

        UploadFile uploadFile = new UploadFile(UploadFile.UPLOAD_IMAGE_USER, values,
                albumImagePath, new AsyncCallback() {
            @Override
            public void onSuccess(Object object) {
                confirmDialog.setMessage("이미지 변경 완료!");
                confirmDialog.show();
                Log.d("smh:changeimageurl",""+(String)object);
                AppManager.getInstance().getUser().setProfileString((String)object);
            }
            @Override
            public void onFailure(Exception e) {
                confirmDialog.setMessage("이미지 변경 실패!");
                confirmDialog.show();
            }
        });
        uploadFile.execute();
    }

    private void inquire(){
        confirmDialog.setMessage(
                "전화번호 : 010 - xxxx - xxxx\n" +
                        "문의메일: audgk562@naver.com\n");
        confirmDialog.show();
    }

    private void about(){
        confirmDialog.setMessage(
                "Voice Paper 1.0\n\n" +
                        "제작자: 김형수, 서유식, 송상현, 신명하\n");
        confirmDialog.show();
    }

    private void changePassword(){
        PasswordSettingFragment passwordSettingFragment = PasswordSettingFragment.newInstance();
        passwordSettingFragment.show(getSupportFragmentManager(),null);
    }
}
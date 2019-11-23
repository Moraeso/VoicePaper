package com.example.voicepaper.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.voicepaper.R;
import com.example.voicepaper.adapter.RoomSlidePagerAdapter;
import com.example.voicepaper.data.Room;
import com.example.voicepaper.data.User;
import com.example.voicepaper.fragment.main.CreateRoomFragment;
import com.example.voicepaper.manager.AppManager;
import com.example.voicepaper.util.Constants;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnDismissListener {

    private CreateRoomFragment createRoomFragment;

    private ViewPager roomPager;
    private RoomSlidePagerAdapter roomPagerAdapter;
    private Button createRoomBtn, enterRoomBtn;

    private final static int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppManager.getInstance().setContext(this);
        AppManager.getInstance().setResources(getResources());

        setContentView(R.layout.activity_main);

        ///////////////// 임시 유저 ////////////////////////
        User user = new User();
        AppManager.getInstance().setUser(user);
        AppManager.getInstance().getUser().setID("user1");
        ////////////////////////////////////////////////////

        initView();
        initListener();
        initMyRoomList();
        initRoomPagerAdapter();


        // 앨범 접근 허용(나중에 옮기기)
        checkPermission();
    }

    void initView() {
        roomPager = (ViewPager) findViewById(R.id.roomViewPager);

        createRoomBtn = (Button) findViewById(R.id.btn_createRoom);
    }

    void initListener() {
        createRoomBtn.setOnClickListener(this);
    }

    void initMyRoomList() {
        ArrayList<Room> rooms = new ArrayList<>();

        Drawable drawable = getResources().getDrawable(R.drawable.ic_user_main);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        rooms.add(new Room(1001, "1Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅎㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "2Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "3Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "4Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "5Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "6Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "7Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "8Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        rooms.add(new Room(1001, "9Room", bitmap, "abc.url",
                Constants.VOICE_PUBLIC, "ㅂㅇ", AppManager.getInstance().getUser().getID()));

        AppManager.getInstance().setRoomList(rooms);
    }


    void initRoomPagerAdapter() {
        roomPagerAdapter = new RoomSlidePagerAdapter(getSupportFragmentManager());

        setSlidePagerAdapter();

        roomPager.setAdapter(roomPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_createRoom:
                createRoomFragment = CreateRoomFragment.newInstance();
                createRoomFragment.show(getSupportFragmentManager(), "CreateRoom");
                getSupportFragmentManager().executePendingTransactions();
                break;
            case R.id.btn_enterRoom:

                break;
            case R.id.btn_setting:

                break;
        }
    }

    // CreateRoomFragment에서 앨범에서 사진 받아왔을 때
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    //갤러리 접근 권한 설정
    private void checkPermission() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);

            Log.v("갤러리 권한", "갤러리 사용을 위해 권한이 필요합니다.");
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d("sssong:MainActivity", "===============");
        Log.d("sssong:MainActivity", "dismiss event");
        // 다시 set
        setSlidePagerAdapter();
    }

    public void setSlidePagerAdapter() {

        ArrayList<Room> rooms = AppManager.getInstance().getRoomList();

        roomPagerAdapter.setRoomList(rooms);
        roomPagerAdapter.setMaxPages((rooms.size() - 1) / 4 + 1);

        roomPagerAdapter.initFragments();

        for (int i=0; i<roomPagerAdapter.getCount(); i++) {
            roomPagerAdapter.addItem(i);
        }

        roomPagerAdapter.notifyDataSetChanged();
    }
}
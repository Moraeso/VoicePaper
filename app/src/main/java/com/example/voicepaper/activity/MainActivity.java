package com.example.voicepaper.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.voicepaper.R;
import com.example.voicepaper.adapter.RoomSlidePagerAdapter;
import com.example.voicepaper.data.Room;
import com.example.voicepaper.fragment.main.CreateRoomFragment;
import com.example.voicepaper.fragment.main.InputRoomCodeFragment;
import com.example.voicepaper.manager.AppManager;
import com.example.voicepaper.manager.ImageManager;
import com.example.voicepaper.network.AsyncCallback;
import com.example.voicepaper.network.ExitRoomTask;
import com.example.voicepaper.network.UpdateRoomListTask;
import com.example.voicepaper.util.ConfirmDialog;
import com.example.voicepaper.util.Constants;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, DialogInterface.OnDismissListener {

    private CreateRoomFragment createRoomFragment;
    private InputRoomCodeFragment inputRoomCodeFragment;

    private SwipeRefreshLayout swipeRefresh;

    private ViewPager roomPager;
    private RoomSlidePagerAdapter roomPagerAdapter;
    private Button createRoomBtn, inputRoomCodeBtn;
    private ImageButton settingBtn;
    private TextView introTv;

    private ConfirmDialog confirmDialog;
    private int selectedRoomPos;

    private ImageView iv_userImage;

    public static final int DELETE_OFF = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppManager.getInstance().setContext(this);
        AppManager.getInstance().setResources(getResources());

        setContentView(R.layout.activity_main);

        initView();
        initListener();

        loadUserRoomList();
        initRoomPagerAdapter();

        setSwipeRefresh();

        AppManager.getInstance().setMainActivity(this);

        selectedRoomPos = DELETE_OFF;

        // 앨범 접근 허용(나중에 옮기기)
        //checkPermission();
    }

    void initView() {
        roomPager = (ViewPager) findViewById(R.id.roomViewPager);

        createRoomBtn = (Button) findViewById(R.id.btn_createRoom);
        inputRoomCodeBtn = (Button) findViewById(R.id.btn_inputRoomCode);
        settingBtn = (ImageButton) findViewById(R.id.btn_setting);


        introTv = (TextView) findViewById(R.id.tv_intro);
        introTv.setText(AppManager.getInstance().getUser().getName() +
                "님 반갑습니다.\n목소리를 공유해 보세요!");

        confirmDialog = new ConfirmDialog(AppManager.getInstance().getContext());

        iv_userImage = (ImageView) findViewById(R.id.iv_profile);

        if (AppManager.getInstance().getUser().getProfileString().equals("undefined")) {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_user_main);
            iv_userImage.setImageBitmap(((BitmapDrawable) drawable).getBitmap());
        } else {
            String url = ImageManager.getInstance().getFullImageString(AppManager.getInstance().getUser().getProfileString(), "file/userimage");
            Log.d("smh:userimage",""+url);
            ImageManager.getInstance().GlideInto(AppManager.getInstance().getContext(), iv_userImage, url);
        }
    }

    void initListener() {
        createRoomBtn.setOnClickListener(this);
        inputRoomCodeBtn.setOnClickListener(this);
        confirmDialog.getOkBtn().setOnClickListener(this);
        settingBtn.setOnClickListener(this);
    }


    void initRoomPagerAdapter() {
        roomPagerAdapter = new RoomSlidePagerAdapter(getSupportFragmentManager());

        setRoomPagerAdapter();
        roomPager.setAdapter(roomPagerAdapter);

        initListener();
    }

    private void loadUserRoomList() {
        ContentValues values = new ContentValues();
        values.put("userID", AppManager.getInstance().getUser().getID());
        UpdateRoomListTask updateRoomListTask = new UpdateRoomListTask(values, new AsyncCallback() {
            @Override
            public void onSuccess(Object object) {
                AppManager.getInstance().getRoomList().clear();
                AppManager.getInstance().getRoomList().addAll((ArrayList<Room>)object);
                setRoomPagerAdapter();                            }

            @Override
            public void onFailure(Exception e) {
            }
        });
        updateRoomListTask.execute();
    }

    private void setSwipeRefresh() {
        // 새로고침
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.layout_swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);

                        loadUserRoomList();
                    }
                }, 1000);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        AppManager.getInstance().setContext(this);
        AppManager.getInstance().setResources(getResources());

        loadUserRoomList();

        roomPagerAdapter.notifyDataSetChanged();

        String url = ImageManager.getInstance().getFullImageString(AppManager.getInstance().getUser().getProfileString(), "file/userimage");
        ImageManager.getInstance().GlideInto(AppManager.getInstance().getContext(), iv_userImage, url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_createRoom:
                if (AppManager.getInstance().getRoomList().size() >= Constants.MAX_ROOMS) {
                    showDialog("방을 더 생성할 수 없습니다.");
                } else {
                    createRoomFragment = CreateRoomFragment.newInstance();
                    createRoomFragment.show(getSupportFragmentManager(), "CreateRoom");
                    getSupportFragmentManager().executePendingTransactions();
                }
                break;
            case R.id.btn_inputRoomCode:
                inputRoomCodeFragment = InputRoomCodeFragment.newInstance();
                inputRoomCodeFragment.show(getSupportFragmentManager(), "InputRoomCode");
                getSupportFragmentManager().executePendingTransactions();
                break;
            case R.id.btn_setting:
                Intent intent = new Intent(AppManager.getInstance().getContext(),SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_roomProfile1:
                enterRoomAt(roomPager.getCurrentItem() * 4);
                break;
            case R.id.iv_roomProfile2:
                enterRoomAt(roomPager.getCurrentItem() * 4 + 1);
                break;
            case R.id.iv_roomProfile3:
                enterRoomAt(roomPager.getCurrentItem() * 4 + 2);
                break;
            case R.id.iv_roomProfile4:
                enterRoomAt(roomPager.getCurrentItem() * 4 + 3);
                break;
            case R.id.btn_ok_dialog:
                if (selectedRoomPos != DELETE_OFF) {
                    deleteRoomAt(selectedRoomPos);
                    selectedRoomPos = DELETE_OFF;
                }
                confirmDialog.dismiss();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.iv_roomProfile1:
                selectedRoomPos = roomPager.getCurrentItem() * 4;
                showDeleteRoomDialog();
                break;
            case R.id.iv_roomProfile2:
                selectedRoomPos = roomPager.getCurrentItem() * 4 + 1;
                showDeleteRoomDialog();
                break;
            case R.id.iv_roomProfile3:
                selectedRoomPos = roomPager.getCurrentItem() * 4 + 2;
                showDeleteRoomDialog();
                break;
            case R.id.iv_roomProfile4:
                selectedRoomPos = roomPager.getCurrentItem() * 4 + 3;
                showDeleteRoomDialog();
                break;
        }

        return true; // 이벤트 계속 진행 false, 이벤트 완료 true
    }

    // CreateRoomFragment에서 앨범에서 사진 받아왔을 때
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        AppManager.getInstance().setContext(this);
        AppManager.getInstance().setResources(getResources());

        // 다시 set
        loadUserRoomList();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AppManager.getInstance().onBackPressed();
    }

    public void showRoomParticipateSuccessDialog() {
        showDialog("방 생성 완료!");
    }

    private void setRoomPagerAdapter() {

        ArrayList<Room> rooms = AppManager.getInstance().getRoomList();

        roomPagerAdapter.setRoomList(rooms);
        roomPagerAdapter.setMaxPages((rooms.size() - 1) / 4 + 1);

        roomPagerAdapter.initFragments();

        for (int i=0; i<roomPagerAdapter.getCount(); i++) {
            roomPagerAdapter.addItem(i);
        }

        roomPagerAdapter.notifyDataSetChanged();
    }

    private void enterRoomAt(int pos) {
        Intent intent = new Intent(this, RoomActivity.class);

        intent.putExtra("id", AppManager.getInstance().getRoomList().get(pos).getId());

        startActivity(intent);

    }

    private void showDeleteRoomDialog() {
        confirmDialog.setMessage("방을 삭제하시겠습니까?\n"
                + "[" + AppManager.getInstance().getRoomList().get(selectedRoomPos).getTitle() + "]");
        confirmDialog.show();
    }

    private void showDialog(String message) {
        confirmDialog.setMessage(message);
        confirmDialog.show();
    }

    private void deleteRoomAt(int pos) {
        //Log.d("sssong:MainActivity", "delete room number : " + pos);

        ContentValues values = new ContentValues();
        values.put("userID", AppManager.getInstance().getUser().getID());
        values.put("roomID", AppManager.getInstance().getRoomList().get(pos).getId());
        values.put("roomPos", pos);

        ExitRoomTask exitRoomTask = new ExitRoomTask(values, new AsyncCallback() {
            @Override
            public void onSuccess(Object pos) {
                showDialog("방이 삭제되었습니다.");
                loadUserRoomList();
            }

            @Override
            public void onFailure(Exception e) {
                showDialog("방 삭제에 실패하였습니다.");
            }
        });
        exitRoomTask.execute();
    }
}
package com.example.zxnet.mybluetoothdome;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    // 本地蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // 搜索按钮
    private Button btn_search;
    //搜索到蓝牙显示列表
    private ListView mylistview;
    // listview的adapter
    private ArrayAdapter<String> mylistAdapter;
    // 存储搜索到的蓝牙
    private List<String> bluetoothdeviceslist = new ArrayList<String>();

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BluetoothDevice device;
    private String pin = "0000";
    private ClsUtils cutl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBluetoothPermission();
        initview();
    }

    private void initview() {
        mylistview = (ListView) findViewById(R.id.mylistview);
        btn_search = (Button) findViewById(R.id.btn_search);

        // 获取本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 设置listview的adapter
        mylistAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, bluetoothdeviceslist);
        mylistview.setAdapter(mylistAdapter);
        mylistview.setOnItemClickListener(this);
        cutl = new ClsUtils();

        ifmBluetoothAdapter();
        myIntentFilter();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ifOpenBluetooth();
                setTitle("正在搜索...");
                // 判断是否在搜索,如果在搜索，就取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                // 开始搜索
                mBluetoothAdapter.startDiscovery();
                getyesBluetoot();
            }
        });
    }

    /**
     * 注册广播
     */
    public void myIntentFilter() {
        // 找到设备的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // 注册广播
        registerReceiver(myreceiver, filter);
        // 搜索完成的广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(myreceiver, filter);
    }

    /**
     * 判断有没有蓝牙硬件支持
     */
    public void ifmBluetoothAdapter() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 判断是否打开了蓝牙
     */
    public void ifOpenBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
            // 那么将会收到RESULT_OK的结果，
            // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
            // 弹出对话框提示用户是否打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
            // 没有提示强制打开蓝牙(用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限)
            // mBluetoothAdapter.enable();
        }
    }

    /**
     * 获的已经配对的设备
     */
    public void getyesBluetoot() {
        Set<BluetoothDevice> myDevices = mBluetoothAdapter.getBondedDevices();
        /// 判断是否有配对过的设备
        if (myDevices.size() > 0) {
            for (BluetoothDevice device : myDevices) {
                // 遍历到列表中
                if (device.getName() != null && !bluetoothdeviceslist.contains("Name:" + device.getName() + "---" + "Mac:" + device.getAddress())) {
                    bluetoothdeviceslist.add("Name:" + device.getName() + "---" + "Mac:" + device.getAddress());
                }
            }
        }
    }

    /**
     * 获得未配对设备
     *
     * @param intent
     */
    public void getnoBluetoot(Intent intent) {
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        // 判断是否配对过
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            if (device.getName() != null) {
                // 添加到列表
                if (device.getName() != null && !bluetoothdeviceslist.contains("Name:" + device.getName() + "---" + "Mac:" + device.getAddress())) {
                    bluetoothdeviceslist.add("Name:" + device.getName() + "---" + "Mac:" + device.getAddress());
                    mylistAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    // 广播接收器
    private final BroadcastReceiver myreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();
            // 发现设备的广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                getnoBluetoot(intent);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle("搜索完成！");
            }
        }
    };

    /**
     * 检查蓝牙权限(6.0动态申请权限)
     */

    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "蓝牙权限已开启", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * list点击事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 先获得蓝牙的地址和设备名
        String s = mylistAdapter.getItem(position);
        // 单独解析地址
        String address = s.substring(s.indexOf("c") + 2).trim();

        BluetoothDevice mydevice=mBluetoothAdapter.getRemoteDevice(address);
       // if (device.getAddress() == address) {
            try {
                cutl.createBond(device.getClass(), mydevice);
                //1.确认配对
                cutl.setPairingConfirmation(mydevice.getClass(),mydevice, true);
                cutl.setPin(mydevice.getClass(), mydevice, pin);
                Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
      //  }

    }


}

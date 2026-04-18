/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.io_ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    
    
    boolean connect_status_bit=false;
    
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000;
    
    private int i = 0;  
    private int TIME = 1000; 
    

    ImageButton switch_button1;
    ImageButton switch_button2;
    ImageButton switch_button3;
    ImageButton switch_button4;
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                
                
                connect_status_bit=true;
                
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                
                updateConnectionState(R.string.disconnected);
                connect_status_bit=false;
                show_view(false);
                invalidateOptionsMenu();
               // clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                	
//                	Log.i("tag", "uu");
//                    if (mGattCharacteristics != null) {
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics.get(groupPosition).get(childPosition);
//                        final int charaProp = characteristic.getProperties();
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            // If there is an active notification on a characteristic, clear
//                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic != null) {
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
//                                mNotifyCharacteristic = null;
//                            }
//                            mBluetoothLeService.readCharacteristic(characteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic = characteristic;
//                            mBluetoothLeService.setCharacteristicNotification(
//                                    characteristic, true);
//                        }
//                        return true;
//                    }
                    return false;
                }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    Button send_button;
    Button enable_button;
    Button IBeacon_set_button;
    
    EditText txd_txt,uuid_1001_ed;
    
    EditText ibeacon_uuid;
    EditText mayjor_txt,minor_txt;
    
    EditText dev_Name;
    Button name_button;
    
    EditText password_ed;//密码值
    Button password_enable_bt;//密码开关
    Button password_wrt;//密码写入Button
    
    Button adv_time1,adv_time2,adv_time3,adv_time4;
    
    boolean pass_en=false;
    
    
    boolean swt1_st = false;
    boolean swt2_st = false;
    boolean swt3_st = false;
    boolean swt4_st = false;
    
    
    private Button IO_H_button,IO_L_button;//out io
    Timer timer = new Timer();  

//    Button id_btt;
//    Button pass_btt;
//    Button name_btt;
    
    String dme ="";
    
    void show_view( boolean p )
    {
    	if(p){
    		send_button.setEnabled(true);
    	}else{
    		send_button.setEnabled(false);
    	}
    }
    
    public void delay(int ms){
		try {
            Thread.currentThread();
			Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
	 }	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
       // mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        
        switch_button1=(ImageButton)findViewById(R.id.imageButton1);
        switch_button2=(ImageButton)findViewById(R.id.imageButton2);
        switch_button3=(ImageButton)findViewById(R.id.imageButton3);
        switch_button4=(ImageButton)findViewById(R.id.imageButton4);
        switch_button1.setOnClickListener(listener);
        switch_button2.setOnClickListener(listener);
        switch_button3.setOnClickListener(listener);
        switch_button4.setOnClickListener(listener);
        
        switch_button1.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
        switch_button2.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
        switch_button3.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
        switch_button4.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
        
        switch_button1.setEnabled(false);
        switch_button2.setEnabled(false);
        switch_button3.setEnabled(false);
        switch_button4.setEnabled(false);
        
        send_button=(Button)findViewById(R.id.tx_button);//send data 1002
        send_button.setOnClickListener(listener);//设置监听  
        
        txd_txt=(EditText)findViewById(R.id.tx_text);//1002 data
        txd_txt.setText("55aa008900023209c555aa02860001038b");
        
//        id_btt=(Button)findViewById(R.id.id_button);//send data 1002
//        id_btt.setOnClickListener(listener);//设置监听  
//        //id_btt.setTag(0);
//        pass_btt=(Button)findViewById(R.id.pass_button);//send data 1002
//        pass_btt.setOnClickListener(listener);//设置监听  
//        name_btt=(Button)findViewById(R.id.name_button);//send data 1002
//        name_btt.setOnClickListener(listener);//设置监听  
        
        show_view(false);
        mHandler = new Handler();
        
        timer.schedule(task, 1000, 1000); // 1s后执行task,经过1s再次执行  
        
        boolean sg;
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //getActionBar().setTitle( "="+BluetoothLeService );
        //mDataField.setText("="+sg );
        updateConnectionState(R.string.connecting);
    }
    
    Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
        	if (msg.what == 1) {  
                //tvShow.setText(Integer.toString(i++));  
            	//scanLeDevice(true);
            	if (mBluetoothLeService != null) {
            		if( connect_status_bit==false )
            		{
            			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            			Log.d(TAG, "Connect request result=" + result);
            		}
                }
            }  
            super.handleMessage(msg);  
        };  
    };  
    TimerTask task = new TimerTask() {  
    	  
        @Override  
        public void run() {  
            // 需要做的事:发送消息  
            Message message = new Message();  
            message.what = 1;  
            handler.sendMessage(message);  
        }  
    }; 

    

    
    Button.OnClickListener listener = new Button.OnClickListener(){//创建监听对象    
        public void onClick(View v){    
            //String strTmp="点击Button02";    
            //Ev1.setText(strTmp);   
        	switch( v.getId())
        	{
        	case R.id.imageButton1:
        	{
        		if( swt1_st==true)
        		{
        			mBluetoothLeService.send_io_data( "f100" );
        		}else{
        			mBluetoothLeService.send_io_data( "f101" );
        		}
				 try {  
			            Thread.currentThread();  
			            Thread.sleep(100);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }
				 mBluetoothLeService.send_io_data( "f6" );//IO输出功能功能
        		
        	}break;
        	case R.id.imageButton2:
        	{
        		if( swt2_st==true)
        		{
        			mBluetoothLeService.send_io_data( "f200" );
        		}else{
        			mBluetoothLeService.send_io_data( "f201" );
        		}
				 try {  
			            Thread.currentThread();  
			            Thread.sleep(100);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }
				 mBluetoothLeService.send_io_data( "f6" );//IO输出功能功能
        		
        	}break;
        	case R.id.imageButton3:
        	{
        		if( swt3_st==true)
        		{
        			mBluetoothLeService.send_io_data( "f300" );
        		}else{
        			mBluetoothLeService.send_io_data( "f301" );
        		}
				 try {  
			            Thread.currentThread();  
			            Thread.sleep(100);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }
				 mBluetoothLeService.send_io_data( "f6" );//IO输出功能功能
        		
        	}break;
        	case R.id.imageButton4:
        	{
        		if( swt4_st==true)
        		{
        			mBluetoothLeService.send_io_data( "f400" );
        		}else{
        			mBluetoothLeService.send_io_data( "f401" );
        		}
				 try {  
			            Thread.currentThread();  
			            Thread.sleep(100);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }
				 mBluetoothLeService.send_io_data( "f6" );//IO输出功能功能
        		
        	}break;
        	case R.id.tx_button ://uuid1002 数传通道发送数据
        		if( connect_status_bit )
      		  {
        			dme = "";
        			
            		String tx_string=txd_txt.getText().toString().trim();
            		mBluetoothLeService.txxx( "55aa008900020ae77b" );
     				 try {  
 			            Thread.currentThread();  
 			            Thread.sleep(100);  
 			        } catch (InterruptedException e) {  
 			            e.printStackTrace();  
 			        }
     				mBluetoothLeService.txxx( "55aa018900020adb70" );
        			  
      		  }else{
      			  //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show(); 
      			  Toast toast = Toast.makeText(DeviceControlActivity.this, "设备没有连接！", Toast.LENGTH_SHORT); 
      			  toast.show(); 
      		  }
        		break;
//        	case R.id.id_button ://用于读取设备ID
//        	{
//        		String tx_string="aa5510eb3ced8e43e21e10a07f9607dd9709f1"; ;
//        		mBluetoothLeService.txxx(tx_string);
//        		Toast toast = Toast.makeText(DeviceControlActivity.this, "用于读取设备ID！", Toast.LENGTH_SHORT); 
//    			toast.show(); 
//        		break;
//        	}
//        	case R.id.pass_button ://用于更改密码
//        	{
//        		Toast toast = Toast.makeText(DeviceControlActivity.this, "用于更改密码！", Toast.LENGTH_SHORT); 
//    			toast.show(); 
//        		break;
//        	}
//        	case R.id.name_button ://用于更改设备名
//        	{
//        		Toast toast = Toast.makeText(DeviceControlActivity.this, "用于更改设备名！", Toast.LENGTH_SHORT); 
//    			toast.show(); 
//        		break;
//        	}
        		default :
        			break;
        	}
        }    
  
    };  
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
        	
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        timer.cancel();
        timer=null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    } 
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData( String data1 ) {

		
		//head1=data1.substring(0,2);
		//data_0=data1.substring(2);
    	//mDataField.setText( data1 + data1.length() );
    	dme += data1;
    	
    	if( data1==null)return;
    	if( data1.length()==10)
    	{
    		byte[]by = mBluetoothLeService.hex2byte( data1.getBytes() );
    		//mDataField.setText( data1 + by[0] );
    		
    		{
    			mDataField.setText( data1);
    			if( by[1]==0x01 )
    			{
    				swt1_st = true;
    				switch_button1.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
    			}else{
    				swt1_st = false;
    				switch_button1.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
    			}
    			if( by[2]==0x01 )
    			{
    				swt2_st = true;
    				switch_button2.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
    			}else{
    				swt2_st = false;
    				switch_button2.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
    			}
    			if( by[3]==0x01 )
    			{
    				swt3_st = true;
    				switch_button3.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
    			}else{
    				swt3_st = false;
    				switch_button3.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
    			}
    			if( by[4]==0x01 )
    			{
    				swt4_st = true;
    				switch_button4.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
    			}else{
    				swt4_st = false;
    				switch_button4.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
    			}
    			
    		}
    	}
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
	        if( connect_status_bit )
			  {
	        	mConnected = true;
	        	
	            switch_button1.setEnabled(true);
	            switch_button2.setEnabled(true);
	            switch_button3.setEnabled(true);
	            switch_button4.setEnabled(true);
	        	show_view( true );
				mBluetoothLeService.enable_JDY_ble(true,1);//IO输出功能功能
				 try {  
			            Thread.currentThread();  
			            Thread.sleep(100);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }
				 mBluetoothLeService.send_io_data( "f6" );//IO输出功能功能
				 updateConnectionState(R.string.connected);
			  }else{
				  //Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_LONG).show(); 
				  //Toast toast = Toast.makeText(DeviceControlActivity.this, "设备没有连接！", Toast.LENGTH_SHORT); 
				  //toast.show(); 
			  }
        }
        
        
//        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//                this,
//                gattServiceData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 },
//                gattCharacteristicData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 }
//        );
//        
//        mGattServicesList.setAdapter(gattServiceAdapter);
        
    }
 
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}

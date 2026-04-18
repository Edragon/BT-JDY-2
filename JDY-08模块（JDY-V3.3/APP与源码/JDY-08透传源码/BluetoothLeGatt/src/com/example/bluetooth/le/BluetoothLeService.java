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

package com.example.bluetooth.le;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_UUID =
            "com.example.bluetooth.le.uuid_DATA";
    public final static String EXTRA_NAME =
            "com.example.bluetooth.le.name_DATA";
    public final static String EXTRA_PASSWORD =
            "com.example.bluetooth.le.password_DATA";
    

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    
    public static String Service_uuid = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String Characteristic_uuid_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String Characteristic_uuid_FUNCTION = "0000ffe1-0000-1000-8000-00805f9b34fb";
	
    
    

    public  String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
    public  byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }
    
    
    
    
    
    byte[] WriteBytes = new byte[20];
	public void txxx(String g){
		g=""+g;
		WriteBytes= hex2byte(g.toString().getBytes());
		
		BluetoothGattCharacteristic gg;
		
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
		//byte t[]={51,1,2};
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
		//mBluetoothGatt.setCharacteristicNotification(gg, true);
		
		//gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//mBluetoothGatt.setCharacteristicNotification(gg, true);
	}
	
	public void enable_noty()
	{
		BluetoothGattService service =mBluetoothGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
		BluetoothGattCharacteristic ale =service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
		boolean set = mBluetoothGatt.setCharacteristicNotification(ale, true);
		Log.d(TAG," setnotification = " + set);
		BluetoothGattDescriptor dsc =ale.getDescriptor(UUID.fromString(  "00002902-0000-1000-8000-00805f9b34fb"));
		byte[]bytes = {0x01,0x00};
		dsc.setValue(bytes);
		mBluetoothGatt.writeDescriptor(dsc);
	}
	
	
    public void enable_JDY_ble( boolean p){
    	
    	try {
		if(p)
	    {
			BluetoothGattService service =mBluetoothGatt.getService(UUID.fromString(Service_uuid));
			BluetoothGattCharacteristic ale;// =service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
			switch( 0 )
			{
				case 0://0xFFE1 //透传
				{
					ale =service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
				}break;
				case 1:// 0xFFE2 //iBeacon_UUID
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb"));
				}break;
				case 2://0xFFE3 //iBeacon_Major
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb"));
				}break;
				case 3://0xFFE4 //iBeacon_Minor
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"));
				}break;
				case 4://0xFFE5 //广播间隔
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb"));
				}break;
				case 5://0xFFE6 //密码功能
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe6-0000-1000-8000-00805f9b34fb"));
				}break;
				case 6:// 0xFFE7 //设备名功能
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe7-0000-1000-8000-00805f9b34fb"));
				}break;
				case 7:// 0xFFE8 //IO输出功能功能
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe8-0000-1000-8000-00805f9b34fb"));
				}break;
				case 8:// 0xFFE9 //PWM功能
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb"));
				}break;
				case 9:// 0xFFEA //复位模块
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffea-0000-1000-8000-00805f9b34fb"));
				}break;
				case 10:// 0xFFEB //发射功率
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffeb-0000-1000-8000-00805f9b34fb"));
				}break;
				case 11:// 0xFFEC //RTC功能
				{
					ale =service.getCharacteristic(UUID.fromString("0000ffec-0000-1000-8000-00805f9b34fb"));
				}break;
				default:
					ale =service.getCharacteristic(UUID.fromString(Characteristic_uuid_TX));
					break;
			} 
			boolean set = mBluetoothGatt.setCharacteristicNotification(ale, true);
			Log.d(TAG," setnotification = " + set);
			BluetoothGattDescriptor dsc =ale.getDescriptor(UUID.fromString(  "00002902-0000-1000-8000-00805f9b34fb"));
			byte[]bytes = {0x01,0x00};
			dsc.setValue(bytes);
			boolean success =mBluetoothGatt.writeDescriptor(dsc);
			Log.d(TAG, "writing enabledescriptor:" + success);
	    }
	    else
	    {
		   BluetoothGattService service =mBluetoothGatt.getService(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
		   BluetoothGattCharacteristic ale =service.getCharacteristic(UUID.fromString(Service_uuid));
		   boolean set = mBluetoothGatt.setCharacteristicNotification(ale, false);
		   Log.d(TAG," setnotification = " + set);
		   BluetoothGattDescriptor dsc =ale.getDescriptor(UUID.fromString(Characteristic_uuid_TX));
		   byte[]bytes = {0x00, 0x00};
		   dsc.setValue(bytes);
		   boolean success =mBluetoothGatt.writeDescriptor(dsc);
		   Log.d(TAG, "writing enabledescriptor:" + success);
	    }
        	
        	
//        	jdy=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
//        	mBluetoothGatt.setCharacteristicNotification(jdy, p);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
    }
    public void read_uuid(  ){
    	String txt="AAE50111";
    	WriteBytes= hex2byte(txt.toString().getBytes());
    	
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//byte t[]={51,1,2};
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    public Boolean set_uuid( String txt ){//uuid
    	
    	if( txt.length()==36 )
		{
			String v1="",v2="",v3="",v4="";
			v1=txt.substring(8,9);
			v2=txt.substring(13,14);
			v3=txt.substring(18,19);
			v4=txt.substring(23,24);
			if( v1.equals("-")&&v2.equals("-")&&v3.equals("-")&&v4.equals("-") )
			{
				txt=txt.replace("-","");
		    	txt="AAF1"+txt;
		    	WriteBytes= hex2byte(txt.toString().getBytes());
		    	
		    	BluetoothGattCharacteristic gg;
				gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
				//byte t[]={51,1,2};
				gg.setValue(WriteBytes);
				mBluetoothGatt.writeCharacteristic(gg);
				return true;
			}else{
				//Toast toast = Toast.makeText(DeviceControlActivity.this, "提示！UUID输入格式不对", Toast.LENGTH_SHORT); 
    			//toast.show(); 
				return false;
			}
		}else {
			//Toast toast = Toast.makeText(DeviceControlActivity.this, "提示！UUID输入不对", Toast.LENGTH_SHORT); 
			//toast.show(); 
			return false;
		}
    	
    }
    public void set_func( String mayjor0,String minor0 ){//mayjor minor
    	
    	String mayjor="",minor="";
		String sss=mayjor0;
		int i = Integer.valueOf(sss).intValue();
		String vs=String.format("%02x", i);
		if( vs.length()==2)vs="00"+vs;
		else if( vs.length()==3)vs="0"+vs;
		
		mayjor=vs;
		
		sss=minor0;
		i = Integer.valueOf(sss).intValue();
		vs=String.format("%02x", i);
		if( vs.length()==2)vs="00"+vs;
		else if( vs.length()==3)vs="0"+vs;
		minor=vs;
    	
    	
    	String txt="AAF21AFF4C000215"+mayjor+minor+"CD00";
    	WriteBytes= hex2byte(txt.toString().getBytes());
    	
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		//byte t[]={51,1,2};
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void uuid_1001_send_data( String value )
    {
    	WriteBytes= hex2byte(value.toString().getBytes());
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));

		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
	
    public void set_dev_name( String name ){
    	int length=name.length();
    	String len=String.valueOf(length);
    	int ilen=len.length();
    	String he=String.format("%02X", length);
    	
    	name=bin2hex(name);
    	String txt="AAE4"+he+name;
    	WriteBytes= hex2byte(txt.toString().getBytes());
    	
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    public void out_io_set( String value )
    {
    	WriteBytes= hex2byte(value.toString().getBytes());
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    
    public void set_password( String value ){
    	String st1=bin2hex(value);
    	st1="AAE2"+st1;
    	WriteBytes= hex2byte(st1.toString().getBytes());
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void set_adv_time( int i )
    {
    	if( i==0 ){
			out_io_set("AA0900");
		}else if( i==0 ){
			out_io_set("AA0901");
		}else if( i==0 ){
			out_io_set("AA0902");
		}else if( i==0 ){
			out_io_set("AA0903");
		}else {
			out_io_set("AA0901");
		}
    }
    
    public void password_value( String value )
    {
    	//String txt="AAE2"+he+name;
    	//WriteBytes= hex2byte(value.toString().getBytes());
    	String txt="AAE2";
    	value=bin2hex(value);
    	txt=txt+value;
    	WriteBytes= hex2byte( txt.toString().getBytes() );
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void password_enable( boolean p )
    {
    	String g_pass="";
    	if(p){
    		g_pass="AAE101";
    	}else{
    		g_pass="AAE100";
    	}
    	WriteBytes= hex2byte(g_pass.toString().getBytes());
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    public void userkey( String key )
    {
    	String g_pass="AA20";
    	key=bin2hex(key);
    	g_pass+=key;
    	WriteBytes= hex2byte(g_pass.toString().getBytes());
    	BluetoothGattCharacteristic gg;
		gg=mBluetoothGatt.getService(UUID.fromString(Service_uuid)).getCharacteristic(UUID.fromString(Characteristic_uuid_FUNCTION));
		gg.setValue(WriteBytes);
		mBluetoothGatt.writeCharacteristic(gg);
    }
    
    
    public int get_connected_status( List<BluetoothGattService> gattServices )
    {
        final String LIST_NAME1 = "NAME";
        final String LIST_UUID1 = "UUID";
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        //mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        
        int count_char = 0;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME1, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID1, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME1, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID1, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                count_char++;
            }
            //mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        return count_char;
    }
    
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
        	//Log.i(TAG, "8");
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                   stringBuilder.append(String.format("%02X", byteChar));
                intent.putExtra(EXTRA_DATA,stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        
        mBluetoothGatt.disconnect();
    }
    public boolean isconnect() {
       
        
       return mBluetoothGatt.connect();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}

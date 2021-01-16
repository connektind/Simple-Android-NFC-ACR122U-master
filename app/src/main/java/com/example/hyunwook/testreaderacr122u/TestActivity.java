package com.example.hyunwook.testreaderacr122u;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.smartcard.Features;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import jnasmartcardio.Smartcardio;

public class TestActivity extends AppCompatActivity {

    private static final int TIMEOUT = 5000;
    private UsbManager mManager;

    private Reader mReader; //출근 장치    Attendance device
    private Reader mReader2;

    private ArrayAdapter<String> mReaderAdapter; //장치 목록 어댑  Device list adapter
    private PendingIntent mPermissionIntent;
    private Intent myIntent;

    private Spinner mReaderSpinner;

    private Features mFeatures = new Features();
    String deviceName2;
    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

    static final String TAG = TestActivity.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public boolean isOpened = false; //퇴근 장치 오픈되면 true  When the work device is open
    TextView resText, deviceNametxt;
    Button connectNFC, readNFC, writeNFC, lockNFC,unlockNFC,warmreset;
    EditText edWrite;
    private static final int MAX_LINES = 25;
    String addData;
    byte[] readArray, writeArray;

    //Test
    UsbEndpoint epOut = null, epIn = null;
    UsbInterface usbInterface;
    StringBuilder result;
    UsbDevice mdevice;
    UsbDeviceConnection connection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        listener();

        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        //Initialize reader
        mReader = new Reader(mManager);

        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string changes
                final String outputString = "Slot " + slotNum + ": "
                        + stateStrings[prevState] + " -> "
                        + stateStrings[currState];


                if (currState == Reader.CARD_PRESENT)
                {
                    //Log.d(TAG, "Ready to read... 출근입니다.");
                    Log.d(TAG, "Ready to read... I am going to work.");
                    final byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};

                    final byte[] response = new byte[256];
                    try {
                        int byteCount = mReader.control(slotNum, Reader.IOCTL_CCID_ESCAPE,
                                command, command.length, response, response.length);

                        //get UID
                        StringBuffer uid = new StringBuffer();

                        for (int i = 0; i < (byteCount -2); i++) {
                            uid.append(String.format("%02X", response[i]));

                            if (i < byteCount -3) {

                            }
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override

                                    public void run() {
                                        Long result = Long.parseLong(uid.toString(), 16);
                                        Log.d(TAG, "Data --->" + result);
//                                                          Long result = Long.parseLong(uid.toString(), 16));
                                        //resText.setText(String.valueOf(result) + "--" + "출근");
                                        //resText.setText(String.valueOf(result) + "--" + "Go to work");
                                        try {

                                            resText.setText(String.valueOf(result) + "--" + "Off work" + "--" );
                                        }catch (Exception e) {e.printStackTrace();}
                                    }
                                });
                            } catch (NumberFormatException e) {
                                Looper.prepare();
                                //Toast.makeText(getApplicationContext(), "인식 실패, 다시 찍어주세요.", Toast.LENGTH_LONG);
                                Toast.makeText(getApplicationContext(), "Recognition failure, please take a picture again.", Toast.LENGTH_LONG);
                                Looper.loop();
                            }
                        }

                    } catch (ReaderException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        //Initialize reader
        mReader2 = new Reader(mManager);
        mReader2.setOnStateChangeListener(new Reader.OnStateChangeListener() {
            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                if (currState == Reader.CARD_PRESENT) {
                    //Log.d(TAG, "Ready to read2... 퇴근입니다.");
                    Log.d(TAG, "Ready to read2... I'm off work.");
                    final byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};

                    final byte[] response = new byte[256];
                    try {
                        int byteCount = mReader2.control(slotNum, Reader.IOCTL_CCID_ESCAPE,
                                command, command.length, response, response.length);

                        //get UID
                        StringBuffer uid = new StringBuffer();

                        for (int i = 0; i < (byteCount -2); i++) {
                            uid.append(String.format("%02X", response[i]));

                            if (i < byteCount -3) {

                            }
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override

                                    public void run() {
                                        Long result = Long.parseLong(uid.toString(), 16);
                                        Log.d(TAG, "Data --->" + result);
//                                                          Long result = Long.parseLong(uid.toString(), 16));
                                        //resText.setText(String.valueOf(result) + "--" + "퇴근");
                                        try {


                                            resText.setText(String.valueOf(result) + "--" + "Off work" + "--" );
                                        }catch (Exception e) {e.printStackTrace();}
                                    }
                                });
//                                Log.d(TAG, "Data2 --->" + Long.parseLong(uid.toString(), 16));
                            } catch (NumberFormatException e) {
                                Looper.prepare();
                                //Toast.makeText(getApplicationContext(), "인식 실패, 다시 찍어주세요.", Toast.LENGTH_LONG);
                                Toast.makeText(getApplicationContext(), "Recognition failure, please take a picture again.", Toast.LENGTH_LONG);
                                Looper.loop();
                            }
                        }

                    } catch (ReaderException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        myIntent = new Intent(ACTION_USB_PERMISSION);
        //Register receiver for USB permission
        //mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);

        //Initialize reader spinner
        mReaderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device))
            {
                mReaderAdapter.add(device.getDeviceName());
                deviceNametxt.setText(device.getDeviceName()+"\n");
                Log.d(TAG, "device name -->" + device.getDeviceName());
                mdevice = device;
            }
        }

        //disable open button
//        String deviceName = (String) mReaderSpinner.getSelectedItem();

        Log.d(TAG, "count ->" + mReaderAdapter.getCount());
        int deviceCount = mReaderAdapter.getCount();
        if (deviceCount == 0) {
            //Toast.makeText(getApplicationContext(), "카드 리더기 오류, 재 연결 해주세요.", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Card reader error, please reconnect.", Toast.LENGTH_LONG).show();
        } else if (deviceCount == 1) {
            String deviceName = mReaderAdapter.getItem(0);

            Log.d(TAG, "Open Test --->" + deviceName); //선택된 장치  Selected device
            if (deviceName != null) {

                for (UsbDevice device : mManager.getDeviceList().values()) {

                    //device name is found
                    if (deviceName.equals(device.getDeviceName())) {

                        //Request permission
                        mManager.requestPermission(device, mPermissionIntent);

                        break;
                    }
                }
            }

        } else if (deviceCount == 2) {
            //2개  2
            String deviceName = mReaderAdapter.getItem(0);
            deviceName2 = mReaderAdapter.getItem(1);
            //deviceNametxt.setText("2"+deviceName2);
            Log.d(TAG, "Open Test2 --->" + deviceName); // 선택된 장치   Selected device
            Log.d(TAG, "Open Test2 --->" + deviceName2); // 선택된 장치  Selected device

            if (deviceName != null && deviceName2 != null) {
                for (UsbDevice device : mManager.getDeviceList().values()) {
                    Log.d(TAG, "device ----->" + device.getDeviceName());

                    //device name is found.
                    if (deviceName.equals(device.getDeviceName())) {
                        //Request permission
                        Log.d(TAG, "Device 1 -> " +device.getDeviceName());
                        mManager.requestPermission(device, mPermissionIntent);

                        break;
                    }
                }
            }
        }
    }

    public void init()
    {
        resText = findViewById(R.id.resultText);
        deviceNametxt = findViewById(R.id.deviceName);
        edWrite = findViewById(R.id.edWrite);
        connectNFC = findViewById(R.id.connectNFC);
        readNFC = findViewById(R.id.readNFC);
        writeNFC = findViewById(R.id.writeNFC);
        lockNFC = findViewById(R.id.lockNFC);
        unlockNFC = findViewById(R.id.unlockNFC);
        warmreset = findViewById(R.id.warmreset);
        readArray = new byte[]{0x04,0x08,0x12, 0x16, 0x20,0x24,0x28,0x32,0x36,0x40};
        writeArray = new byte[]{0x04,0x05,0x06,0x07,0x08,0x09,0x10,0x11,0x12,0x13,0x014,0x15,0x16, 0x17,0x18,0x19,0x20,
                0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x40};


    }

    public void listener()
    {
        connectNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //writeCard();
                openConnection(mdevice);
            }
        });
        readNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte[] response = new byte[300];

                int responseLength;
                try {
                    //readNewCard();
                    //read(connection,epIn);
                    readNfcCardDevice();
//                    byte[] command2 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x06, (byte) 0x10};
//                    responseLength = mReader.transmit(0, command2, command2.length, response, response.length);
//
//                    byte[] command3 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x07, (byte) 0x10};
//                    responseLength = mReader.transmit(0, command3, command3.length, response, response.length);
////
//                    String test = new String(response, StandardCharsets.US_ASCII);
//                    resText.setText(test);
//                    Toast.makeText(TestActivity.this, "Response Length : " + responseLength + test, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }
        });
        warmreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mReader.power(0, Reader.CARD_COLD_RESET);
                    mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                }catch (Exception e){e.printStackTrace();}
            }
        });
        writeNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String mydata = edWrite.getText().toString();
                //byte[] response = new byte[300];
                //response = mydata.getBytes();
                int responseLength;
                try {
                    //writeNewCard();
                    String test = "Connekt Techo";
                    writeNfcCardDevice(test);

//                    String command = "eyJhbGciOiJIUzI1NiJ9.ZVVXemtMVVhSYQ.PLBeVXUY6XvounA9KpAGudJFQ8Xj2njb_yRTfk1FL9M";
//                    String fullCommand = "@" + command + "\r\n"; /*+ "\r\n"*/
//                    byte [] bytes = fullCommand.getBytes();
//                    write(connection,epOut,bytes);
//                    byte[] data = {(byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64};  //a b c d
//                    byte[] command = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[0], (byte)
//                            data[1], (byte) data[2], (byte) data[3]};
//                    responseLength = mReader.transmit(0, command, command.length, response, response.length);
//
//
//
//                    byte[] data1 = {(byte) 0x65, (byte) 0x62, (byte) 0x63, (byte) 0x64};  //e b c d
//                    byte[] command1 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data1[0], (byte)
//                            data1[1], (byte) data1[2], (byte) data1[3]};
//                    responseLength = mReader.transmit(0, command1, command1.length, response, response.length);
//                    String s = new String(data1, StandardCharsets.UTF_8);
//
//                    Toast.makeText(TestActivity.this, "Response Length" + s + responseLength, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });

        lockNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                //mReader.power(0, Reader.CARD_POWER_DOWN);
                //mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
            }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //USB Receiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {
                            //Open Reader
                            Log.d(TAG, "OpenTask ...>" + device.getDeviceName());
                            /*if (isOpened) {
                                new OpenTask2().execute(device);
                            } else {*/
                            new OpenTask().execute(device);
//                            }

                        }

                    } else {

                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    //update reader list
                    Log.d(TAG, "update reader list...");
                    mReaderAdapter.clear();

                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {
                        new CloseTask().execute();

                    }
                }
            }
        }
    };

    //Reader Device OpenTask
    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {
            Exception result = null;
            Log.d(TAG, "params -> " + isOpened);

            try {
                if (!isOpened) {
                    mReader.open(params[0]);
                    //deviceNametxt.setText("Reader"+ mReader.getReaderName()+"\n");
                } else {
                    mReader.open(params[0]);
                    //mReader2.open(params[0]);

                }
            } catch (Exception e) {
                result = e;
            }

            return result;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {

            } else {
                Log.d(TAG, "Reader Name -->" + mReader.getReaderName());
                deviceNametxt.setText("Reader Name: "+ mReader.getReaderName()+"\n");
                int numSlots = mReader.getNumSlots();
                Log.d(TAG, "Number of slots: " + numSlots);

                //Add Slot items;
//                mslo
                // Remove all control codes
//                mFeatures.clear();

                Log.d(TAG, "next device -> " + deviceName2);
                Log.d(TAG, "isOpened state -> " + isOpened);

                if (!isOpened) {

                    for (UsbDevice device : mManager.getDeviceList().values()) {

//                        if (deviceName2.equals(device.getDeviceName())) {
//                            //Request permission
//                            isOpened =true;
//                            Log.d(TAG, "Device 2 -> " + device.getDeviceName());
//                            mManager.requestPermission(device, mPermissionIntent);
//
//                            break;
//                        }

                    }
                }
            }
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mReader.close();
            mReader2.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    @Override
    protected void onDestroy() {
        mReader.close();
        mReader2.close();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void writeCard() {
        try {
            //String test = "  eyJhbGciOiJIUzI1NiJ9.ZVVXemtMVVhSYQ.PLBeVXUY6XvounA9KpAGudJFQ8Xj2njb_yRTfk1FL9M";
//            String test = "  eyJh";
//            char[] cArray = test.toCharArray();
//            resText.setText("String Length: " + test.length());
//            byte[] response = test.getBytes();
        byte[] response = new byte[300];

            int responseLength;

//            for (int i = 0; i < cArray.length; i++) {
//                response[i] = (byte) cArray[i];
//                Log.e("Index Char: ", cArray[i] + "");
//                Log.e("Response Char: ", response[i] + "");
//                String hexa = Integer.toHexString(response[i]);
//                Log.e("Format String: ", hexa + "");

                String formatString = null;
//                if(hexa.length()==2){
//                    formatString = "0x"+hexa;
//                }
//                else if(hexa.length()==1)
//                {
//                    formatString = "0x0"+hexa;
//                }
                //Log.e("New Format", hexa.getBytes()+"");
                //byte data[] = hexa.getBytes();


                //Test
                byte[] data = {(byte) 0x65, (byte) 0x68, (byte) 0x4A, (byte) 0x68};  //a b c d
                byte[] command = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0],
                       (byte) data[1],(byte) data[2],(byte) data[3]};
                responseLength = mReader.transmit(0, command, command.length, response, response.length);
                String s = new String(response, StandardCharsets.US_ASCII);
                Toast.makeText(TestActivity.this, "Response Length" + s + responseLength, Toast.LENGTH_SHORT).show();
            //}


        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void testWrite()
    {

    }

    public void writeNewCard()
    {
        byte[] response = new byte[300];
        int responseLength;
        try {
//            String test = "eyJhbGciOiJIUzI1NiJ9.ZVVXemtMVVhSYQ.PLBeVXUY6XvounA9KpAGudJFQ8Xj2njb_yRTfk1FL9M";
//            char[] cArray = test.toCharArray();
//            for(int x = 0; x<cArray.length;x++)
//            {
//                if(cArray[x] >= 0 && cArray[x] <= 3)
//                {
//                    char ch = cArray[x];
//                    int i = (char)ch;
//                    String data = Integer.toHexString(i);
//                    Log.e("CH: ", ch+"");
//                    Log.e("data",data);
//                }
//
//
//            }
//            byte[] data = {(byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64};  //a b c d
//            byte[] command = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0],
//                    (byte) data[1], (byte) data[2], (byte) data[3]}; //page 4
//            responseLength = mReader.transmit(0, command, command.length, response, response.length);
//            String s = new String(data, StandardCharsets.US_ASCII);
//            Log.e("PAge 4",s);
//            Toast.makeText(TestActivity.this, "Response Length" + s , Toast.LENGTH_SHORT).show();
//
//            byte[] data1 = {(byte) 0x65, (byte) 0x66, (byte) 0x67, (byte) 0x68};  //e f g h
//            byte[] command1 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data1[0],
//                    (byte) data1[1], (byte) data1[2], (byte) data1[3]}; //page 5
//            responseLength = mReader.transmit(0, command1, command1.length, response, response.length);
//            String s5 = new String(data1, StandardCharsets.US_ASCII);
//            Log.e("PAge 5",s5);
//            Toast.makeText(TestActivity.this, "Response Length" + s5 , Toast.LENGTH_SHORT).show();
//
//
//            byte[] data2 = {(byte) 0x69, (byte) 0x6A, (byte) 0x6B, (byte) 0x6C};  //i j k l
//            byte[] command2 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data2[0],
//                    (byte) data2[1], (byte) data2[2], (byte) data2[3]}; //page 6
//            responseLength = mReader.transmit(0, command2, command2.length, response, response.length);
//            String s6 = new String(data2, StandardCharsets.US_ASCII);
//            Log.e("PAge 6",s6);
//            Toast.makeText(TestActivity.this, "Response Length" + s6 , Toast.LENGTH_SHORT).show();

            byte[] data3 = {(byte) 0x6D, (byte) 0x6E, (byte) 0x6F, (byte) 0x70};  //m n o p
            byte[] command3 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data3[0],
                    (byte) data3[1], (byte) data3[2], (byte) data3[3]}; //page 7
            responseLength = mReader.transmit(0, command3, command3.length, response, response.length);
            String s7 = new String(data3, StandardCharsets.US_ASCII);
            Log.e("PAge 7",s7);
            Toast.makeText(TestActivity.this, "Response Length" + s7 , Toast.LENGTH_SHORT).show();

//            byte[] data4 = {(byte) 0x71, (byte) 0x72, (byte) 0x73, (byte) 0x74};  //q r s t
//            byte[] command4 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data4[0],
//                    (byte) data4[1], (byte) data4[2], (byte) data4[3]}; //page 8
//            responseLength = mReader.transmit(0, command4, command4.length, response, response.length);
//            String s8 = new String(data4, StandardCharsets.US_ASCII);
//            Log.e("PAge 8",s8);
//            Toast.makeText(TestActivity.this, "Response Length" + s8 , Toast.LENGTH_SHORT).show();
//
//            byte[] data5 = {(byte) 0x75, (byte) 0x76, (byte) 0x77, (byte) 0x78};  //u v w x
//            byte[] command5 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data5[0],
//                    (byte) data5[1], (byte) data5[2], (byte) data5[3]}; //page 9
//            responseLength = mReader.transmit(0, command5, command5.length, response, response.length);
//            String s9 = new String(data5, StandardCharsets.US_ASCII);
//            Log.e("PAge 9",s9);
//            Toast.makeText(TestActivity.this, "Response Length" + s9 , Toast.LENGTH_SHORT).show();
//
//            byte[] data6 = {(byte) 0x79, (byte) 0x7A, (byte) 0x41, (byte) 0x42};  //y z A B
//            byte[] command6 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data6[0],
//                    (byte) data6[1], (byte) data6[2], (byte) data6[3]}; //page 10
//            responseLength = mReader.transmit(0, command6, command6.length, response, response.length);
//            String s10 = new String(data6, StandardCharsets.US_ASCII);
//            Log.e("PAge 10",s10);
//            Toast.makeText(TestActivity.this, "Response Length" + s10 , Toast.LENGTH_SHORT).show();
//
//            byte[] data7 = {(byte) 0x43, (byte) 0x44, (byte) 0x45, (byte) 0x46};  //C D E F
//            byte[] command7 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data7[0],
//                    (byte) data7[1], (byte) data7[2], (byte) data7[3]}; //page 11
//            responseLength = mReader.transmit(0, command7, command7.length, response, response.length);
//            String s11 = new String(data7, StandardCharsets.US_ASCII);
//            Log.e("PAge 11",s11);
//            Toast.makeText(TestActivity.this, "Response Length" + s11 , Toast.LENGTH_SHORT).show();
//
//            byte[] data8 = {(byte) 0x47, (byte) 0x48, (byte) 0x49, (byte) 0x4A};  //G H I J
//            byte[] command8 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data8[0],
//                    (byte) data8[1], (byte) data8[2], (byte) data8[3]}; //page 12
//            responseLength = mReader.transmit(0, command8, command8.length, response, response.length);
//            String s12 = new String(data8, StandardCharsets.US_ASCII);
//            Log.e("PAge 12",s12);
//            Toast.makeText(TestActivity.this, "Response Length" + s12 , Toast.LENGTH_SHORT).show();
//
//            byte[] data9 = {(byte) 0x4B, (byte) 0x4C, (byte) 0x4D, (byte) 0x4E};  //K L M N
//            byte[] command9 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data9[0],
//                    (byte) data9[1], (byte) data9[2], (byte) data9[3]}; //page 13
//            responseLength = mReader.transmit(0, command9, command9.length, response, response.length);
//            String s13 = new String(data9, StandardCharsets.US_ASCII);
//            Log.e("PAge 13",s13);
//            Toast.makeText(TestActivity.this, "Response Length" + s13 , Toast.LENGTH_SHORT).show();
//
//            byte[] data10 = {(byte) 0x4F, (byte) 0x50, (byte) 0x51, (byte) 0x52};  //O P Q R
//            byte[] command10 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data10[0],
//                    (byte) data10[1], (byte) data10[2], (byte) data10[3]}; //page 14
//            responseLength = mReader.transmit(0, command10, command10.length, response, response.length);
//            String s14 = new String(data10, StandardCharsets.US_ASCII);
//            Log.e("PAge 14",s14);
//            Toast.makeText(TestActivity.this, "Response Length" + s14 , Toast.LENGTH_SHORT).show();
//
//            byte[] data11 = {(byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56};  //S T U V
//            byte[] command11 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data11[0],
//                    (byte) data11[1], (byte) data11[2], (byte) data11[3]}; //page 15
//            responseLength = mReader.transmit(0, command11, command11.length, response, response.length);
//            String s15 = new String(data11, StandardCharsets.US_ASCII);
//            Log.e("PAge 15",s15);
//            Toast.makeText(TestActivity.this, "Response Length" + s15 , Toast.LENGTH_SHORT).show();
//
//            byte[] data12 = {(byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A};  //W X Y Z
//            byte[] command12 = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data12[0],
//                    (byte) data12[1], (byte) data12[2], (byte) data12[3]}; //page 16
//            responseLength = mReader.transmit(0, command12, command12.length, response, response.length);
//            String s16 = new String(data12, StandardCharsets.US_ASCII);
//            Log.e("PAge 16",s16);
//            Toast.makeText(TestActivity.this, "Response Length" + s16 , Toast.LENGTH_SHORT).show();


        }catch (Exception e)
        { e.printStackTrace();}
    }

    public void readNewCard()
    {
            byte[] response = new byte[300];
            int responseLength;
            try {
                byte[] command = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x04, (byte) 0x10};
                responseLength = mReader.transmit(0, command, command.length, response, response.length);
                String s = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 4",s);
                Toast.makeText(TestActivity.this, "Response Length Read " + s , Toast.LENGTH_SHORT).show();

                byte[] command1 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x05, (byte) 0x10};
                responseLength = mReader.transmit(0, command1, command1.length, response, response.length);
                String s5 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 5",s5);
                Toast.makeText(TestActivity.this, "Response Length Read " + s5 , Toast.LENGTH_SHORT).show();


                byte[] command2 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x06, (byte) 0x10};
                responseLength = mReader.transmit(0, command2, command2.length, response, response.length);
                String s6 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 6",s6);
                Toast.makeText(TestActivity.this, "Read Response Length" + s6 , Toast.LENGTH_SHORT).show();

                byte[] command3 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x07, (byte) 0x10};
                responseLength = mReader.transmit(0, command3, command3.length, response, response.length);
                String s7 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 7",s7);
                Toast.makeText(TestActivity.this, "Read Response Length" + s7 , Toast.LENGTH_SHORT).show();

                byte[] command4 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x08, (byte) 0x10};
                responseLength = mReader.transmit(0, command4, command4.length, response, response.length);
                String s8 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 8",s8);
                Toast.makeText(TestActivity.this, "Read Response Length" + s8 , Toast.LENGTH_SHORT).show();

                byte[] command5 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x09, (byte) 0x10};
                responseLength = mReader.transmit(0, command5, command5.length, response, response.length);
                String s9 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 9",s9);
                Toast.makeText(TestActivity.this, "Read Response Length" + s9 , Toast.LENGTH_SHORT).show();

                byte[] command6 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x10, (byte) 0x10};
                responseLength = mReader.transmit(0, command6, command6.length, response, response.length);
                String s10 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 10",s10);
                Toast.makeText(TestActivity.this, "Read Response Length" + s10 , Toast.LENGTH_SHORT).show();

                byte[] command7 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x11, (byte) 0x10};
                responseLength = mReader.transmit(0, command7, command7.length, response, response.length);
                String s11 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 11",s11);
                Toast.makeText(TestActivity.this, "Read Response Length" + s11 , Toast.LENGTH_SHORT).show();

                byte[] command8 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x12, (byte) 0x10};
                responseLength = mReader.transmit(0, command8, command8.length, response, response.length);
                String s12 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 12",s12);
                Toast.makeText(TestActivity.this, "Read Response Length" + s12 , Toast.LENGTH_SHORT).show();

                byte[] command9 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x13, (byte) 0x10};
                responseLength = mReader.transmit(0, command9, command9.length, response, response.length);
                String s13 = new String(response, StandardCharsets.US_ASCII);
                Log.e("PAge 13",s13);
                Toast.makeText(TestActivity.this, "Response Length" + s13 , Toast.LENGTH_SHORT).show();

                byte[] command10 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x14, (byte) 0x10};
                responseLength = mReader.transmit(0, command10, command10.length, response, response.length);
                String s14 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 14",s14);
                Toast.makeText(TestActivity.this, "Read Response Length" + s14 , Toast.LENGTH_SHORT).show();

                byte[] command11 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x15, (byte) 0x10};
                responseLength = mReader.transmit(0, command11, command11.length, response, response.length);
                String s15 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 15",s15);
                Toast.makeText(TestActivity.this, "Read Response Length" + s15 , Toast.LENGTH_SHORT).show();

                byte[] command12 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x16, (byte) 0x10};
                responseLength = mReader.transmit(0, command12, command12.length, response, response.length);
                String s16 = new String(response, StandardCharsets.US_ASCII);
                Log.e("Read PAge 16",s16);
                Toast.makeText(TestActivity.this, "Read Response Length" + s16 , Toast.LENGTH_SHORT).show();


            }catch (Exception e)
            { e.printStackTrace();}

    }
    public void readCard() {
        try {
            int responseLength;
            byte[] response = new byte[600];
            //for (int x = 0; x < writeArray.length; x++) {
                byte[] command = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x06, (byte) 0x10};
                responseLength = mReader.transmit(0, command, command.length, response, response.length);

//                byte[] command2 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x06, (byte) 0x10};
//                responseLength = mReader.transmit(0, command2, command2.length, response, response.length);
//
//                byte[] command3 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x07, (byte) 0x10};
//                responseLength = mReader.transmit(0, command3, command3.length, response, response.length);
//
//                byte[] command4 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, 0x08, (byte) 0x10};
//                responseLength = mReader.transmit(0, command4, command2.length, response, response.length);




                String test = new String(response, StandardCharsets.US_ASCII);
                resText.setText(test);
                Toast.makeText(TestActivity.this, "Response Length : " + test, Toast.LENGTH_SHORT).show();
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * Logs the contents of buffer.
//     *
//     * @param buffer
//     *            the buffer.
//     * @param bufferLength
//     *            the buffer length.
//     */
//    private void logBuffer(byte[] buffer, int bufferLength) {
//
//        String bufferString = "";
//
//        for (int i = 0; i < bufferLength; i++) {
//
//            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
//            if (hexChar.length() == 1) {
//                hexChar = "0" + hexChar;
//            }
//
//            if (i % 16 == 0) {
//
//                if (bufferString != "") {
//
//                    logMsg(bufferString);
//                    bufferString = "";
//                }
//            }
//
//            bufferString += hexChar.toUpperCase() + " ";
//        }
//
//        if (bufferString != "") {
//            logMsg(bufferString);
//        }
//    }
//
//    /**
//     * Converts the HEX string to byte array.
//     *
//     * @param hexString
//     *            the HEX string.
//     * @return the byte array.
//     */
//    private byte[] toByteArray(String hexString) {
//
//        int hexStringLength = hexString.length();
//        byte[] byteArray = null;
//        int count = 0;
//        char c;
//        int i;
//
//        // Count number of hex characters
//        for (i = 0; i < hexStringLength; i++) {
//
//            c = hexString.charAt(i);
//            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
//                    && c <= 'f') {
//                count++;
//            }
//        }
//
//        byteArray = new byte[(count + 1) / 2];
//        boolean first = true;
//        int len = 0;
//        int value;
//        for (i = 0; i < hexStringLength; i++) {
//
//            c = hexString.charAt(i);
//            if (c >= '0' && c <= '9') {
//                value = c - '0';
//            } else if (c >= 'A' && c <= 'F') {
//                value = c - 'A' + 10;
//            } else if (c >= 'a' && c <= 'f') {
//                value = c - 'a' + 10;
//            } else {
//                value = -1;
//            }
//
//            if (value >= 0) {
//
//                if (first) {
//
//                    byteArray[len] = (byte) (value << 4);
//
//                } else {
//
//                    byteArray[len] |= value;
//                    len++;
//                }
//
//                first = !first;
//            }
//        }
//
//        return byteArray;
//    }
//
//    /**
//     * Converts the integer to HEX string.
//     *
//     * @param i
//     *            the integer.
//     * @return the HEX string.
//     */
//    private String toHexString(int i) {
//
//        String hexString = Integer.toHexString(i);
//        if (hexString.length() % 2 != 0) {
//            hexString = "0" + hexString;
//        }
//
//        return hexString.toUpperCase();
//    }
//
//    /**
//     * Converts the byte array to HEX string.
//     *
//     * @param buffer
//     *            the buffer.
//     * @return the HEX string.
//     */
//    private String toHexString(byte[] buffer) {
//
//        String bufferString = "";
//
//        for (int i = 0; i < buffer.length; i++) {
//
//            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
//            if (hexChar.length() == 1) {
//                hexChar = "0" + hexChar;
//            }
//
//            bufferString += hexChar.toUpperCase() + " ";
//        }
//
//        return bufferString;
//    }


    public void openConnection(UsbDevice device)
    {
        connection = mManager.openDevice(device);

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            usbInterface = device.getInterface(i);
            connection.claimInterface(usbInterface, true);

            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint ep = usbInterface.getEndpoint(j);

                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                        // from host to device
                        Log.e("from host to device","");
                        epOut = ep;

                    } else if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                        // from device to host
                        Log.e("from device to host","");
                        epIn = ep;
                    }
                }
            }
        }
    }

    public void write(UsbDeviceConnection connection, UsbEndpoint epOut, byte[] command) {
       result = new StringBuilder();
        connection.bulkTransfer(epOut, command, command.length, TIMEOUT);
        //For Printing logs you can use result variable
        for (byte bb : command) {
            Log.e("Data", result+"");
            result.append(String.format(" %02X ", bb));
        }
    }

    public int read(UsbDeviceConnection connection, UsbEndpoint epIn) {
        result = new StringBuilder();
        final byte[] buffer = new byte[epIn.getMaxPacketSize()];
        int byteCount = 0;
        byteCount = connection.bulkTransfer(epIn, buffer, buffer.length, TIMEOUT);

//For Printing logs you can use result variable
        if (byteCount >= 0) {
            for (byte bb : buffer) {
                result.append(String.format(" %02X ", bb));
                resText.setText(result);
            }

            //Buffer received was : result.toString()
        } else {
            //Something went wrong as count was : " + byteCount
            resText.setText("Something went wrong as count was :" + byteCount);
        }

        return byteCount;
    }

    private boolean writeNfcCardDevice(String nfcTagUid) {
        try {
            mReader.power(0, Reader.CARD_WARM_RESET);
            mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);

            byte[] response = new byte[10];
            byte[] data = nfcTagUid.getBytes();

            byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
            byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
            byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};

            mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
            mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
            mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);

            String test = new String(response, StandardCharsets.US_ASCII).trim();
            //this.emitEvent(ON_SEND_LOG, "writeNfcCardDeviceFunc", test);
            Toast.makeText(this, "writeNfcCardDeviceFunc", Toast.LENGTH_SHORT).show();
            // If the card is not written somehow
            return !test.equals("c");
        } catch (Exception e) {
            //this.emitEvent(ON_SEND_LOG, "writeNfcCardCatch", e.getMessage() != null ? e.getMessage() : "");
            Toast.makeText(this, "writeNfcCardCatch"+ e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }

    private String readNfcCardDevice() {
        try {
            mReader.power(0, Reader.CARD_WARM_RESET);
            mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
            String test;

            byte[] response = new byte[18];
            byte[] command = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x04, (byte) 0x10};
            mReader.transmit(0, command, command.length, response, response.length);
            test = new String(response, StandardCharsets.US_ASCII);

            if (String.valueOf(response[0]).equals("0"))
                return "undefined";
            else
                return test.substring(0, 12);

        } catch (Exception e) {
            e.printStackTrace();
            //this.emitEvent(ON_SEND_LOG, "readNfcCardCatch", e.getLocalizedMessage());
            Toast.makeText(this, "readNfcCardCatch"+ e, Toast.LENGTH_SHORT).show();
            return "undefined";
        }
    }

}

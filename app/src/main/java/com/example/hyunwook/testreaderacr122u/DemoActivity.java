package com.example.hyunwook.testreaderacr122u;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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


import org.nfctools.utils.CardTerminalUtils;

import java.nio.charset.StandardCharsets;

import apdu4j.p.CardTerminalApp;
import apdu4j.p.CardTerminalProvider;
import apdu4j.terminals.LoggingCardTerminal;
import jnasmartcardio.Smartcardio;

public class DemoActivity extends AppCompatActivity {

    private UsbManager mManager;

    private Reader mReader; //출근 장치    Attendance device
    private Reader mReader2;

    private ArrayAdapter<String> mReaderAdapter; //장치 목록 어댑  Device list adapter
    private PendingIntent mPermissionIntent;
    private Intent myIntent;

    private Spinner mReaderSpinner;

    private Features mFeatures = new Features();
    String deviceName2, deviceName;
    private static final String[] stateStrings = {"Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    static final String TAG = DemoActivity.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public boolean isOpened = false; //퇴근 장치 오픈되면 true  When the work device is open
    TextView resText, deviceNametxt;
    Button connectNFC, readNFC, writeNFC, lockNFC, unlockNFC, warmreset;
    EditText edWrite;
    private static final int MAX_LINES = 25;
    String addData;
    byte[] readArray, writeArray;


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


                if (currState == Reader.CARD_PRESENT) {
                    //Log.d(TAG, "Ready to read... 출근입니다.");
                    Log.d(TAG, "Ready to read... I am going to work.");
                    final byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};

                    final byte[] response = new byte[256];
                    try {
                        int byteCount = mReader.control(slotNum, Reader.IOCTL_CCID_ESCAPE,
                                command, command.length, response, response.length);

                        //get UID
                        StringBuffer uid = new StringBuffer();

                        for (int i = 0; i < (byteCount - 2); i++) {
                            uid.append(String.format("%02X", response[i]));

                            if (i < byteCount - 3) {

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

                                            resText.setText(String.valueOf(result) + "--" + "Off work" + "--");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
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

                        for (int i = 0; i < (byteCount - 2); i++) {
                            uid.append(String.format("%02X", response[i]));

                            if (i < byteCount - 3) {

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


                                            resText.setText(String.valueOf(result) + "--" + "Off work" + "--");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
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
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
                deviceNametxt.setText(device.getDeviceName() + "\n");
                Log.d(TAG, "device name -->" + device.getDeviceName());

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
            deviceName = mReaderAdapter.getItem(0);

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
            deviceName = mReaderAdapter.getItem(0);
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
                        Log.d(TAG, "Device 1 -> " + device.getDeviceName());
                        mManager.requestPermission(device, mPermissionIntent);

                        break;
                    }
                }
            }
        }
    }

    public void init() {
        resText = findViewById(R.id.resultText);
        deviceNametxt = findViewById(R.id.deviceName);
        edWrite = findViewById(R.id.edWrite);
        connectNFC = findViewById(R.id.connectNFC);
        readNFC = findViewById(R.id.readNFC);
        writeNFC = findViewById(R.id.writeNFC);
        lockNFC = findViewById(R.id.lockNFC);
        unlockNFC = findViewById(R.id.unlockNFC);
        warmreset = findViewById(R.id.warmreset);
        readArray = new byte[]{0x04, 0x08, 0x12, 0x16, 0x20, 0x24, 0x28, 0x32, 0x36, 0x40};
        writeArray = new byte[]{0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x014, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
                0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40};


    }

    public void listener() {
        connectNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        readNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //byte[] response = new byte[300];

                //int responseLength;
                try {
                    readNfcCardDevice();

//                    byte[] command2 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x06, (byte) 0x10};
//                    responseLength = mReader.transmit(0, command2, command2.length, response, response.length);
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
//                    mReader.power(0, Reader.CARD_WARM_RESET);
//                    mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                    readSerialNoDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        writeNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ;
                try {
                    String command = edWrite.getText().toString();
                    //byte[] response = new byte[300];
                    //response = mydata.getBytes();
                    int responseLength;
                    //String command = "S";
                    //String command = "Somya Dembla....";
                    //String command = "eyJhbGciOiJIUzI1NiJ9.ZVVXemtMVVhSYQ.PLBeVXUY6XvounA9 KpAGudJFQ8Xj2njb_yRTfk1FL9M";
                    //String command = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz9876";  //56
                    //String command = "1234ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz9876";  //60
                    //String command = "1234ABCDEFGHIJKLMNOPQRSTUVWXYZ5678abcdefghijklmnopqrstuvwxyz9876";  //64
                    //String command = "1234ABCDEFGHIJKLMNOPQRSTUVWXYZ5678abcdefghijklmnopqrstuvwxyz9876####";  //68
                    //String command = "1234ABCDEFGHIJKLMNOPQRSTUVWXYZ5678****abcdefghijklmnopqrstuvwxyz9876####";  //72
                    //String command = "1234ABCDEFGHIJKLMNOPQRSTUVWXYZ@@@@5678****abcdefghijklmnopqrstuvwxyz9876####";  //76
                    //String command = "1234&&&&ABCDEFGHIJKLMNOPQRSTUVWXYZ@@@@5678****abcdefghijklmnopqrstuvwxyz9876####";  //78
                    //String command = "1234&&&&ABCDEFGHIJKLMNOPQRSTUVWXYZ@@@@5678****abcdefghijklmnopqrstuvwxyz!9876####";  //79


                    //writeData(command);
                    writeNfcCardDevice(command);

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });

        lockNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String pass = "Hell";
                    setPasswordDevice(pass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        unlockNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = "Hell";
                unlockCard(pass);
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
                    Log.e("InsideIf",params+"");
                    Log.d(TAG, "params If-> " + isOpened);
                    //deviceNametxt.setText("Reader"+ mReader.getReaderName()+"\n");
                } else {
                    mReader.open(params[0]);
                    Log.e("Inside Else",params+"");
                    Log.d(TAG, "params Else-> " + isOpened);
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
                deviceNametxt.setText("Reader Name: " + mReader.getReaderName() + "\n");
                int numSlots = mReader.getNumSlots();
                Log.d(TAG, "Number of slots: " + numSlots);

                //Add Slot items;
//                mslo
                // Remove all control codes
//                mFeatures.clear();

                Log.d(TAG, "next device -> " + deviceName2);
                Log.d(TAG, "isOpened state -> " + isOpened);
                int deviceCount = mReaderAdapter.getCount();
                try {
                    byte[] response = new byte[256];
                    byte[] data = {(byte)0xE0,(byte)0x00,(byte)0x00,(byte)0x21,(byte)0x01,(byte)0x77};
                    int byteCount = mReader.control(0, Reader.IOCTL_GET_FEATURE_REQUEST,
                            data, data.length, response, response.length);
                    if (!isOpened) {


//                        for (UsbDevice device : mManager.getDeviceList().values()) {
//
//                            //device name is found
//                            if (deviceName.equals(device.getDeviceName())) {
//                                //Request permission
//                                isOpened = true;
//                                Log.d(TAG, "Device New Block -> " + device.getDeviceName());
//                                mManager.requestPermission(device, mPermissionIntent);
//
//                                break;
//                            }
//
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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


        // Read tag data from ACR122u NFC reader device
        private String readNfcCardDevice() {
            try {

                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                String test, test1, test2, test3, test4, test5, test6;


                byte[] response = new byte[28];
                byte[] command = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x04, (byte) 0x10}; // 4 5 6 7
                mReader.transmit(0, command, command.length, response, response.length);

                byte[] response1 = new byte[28];
                byte[] command1 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x08, (byte) 0x08}; // 8 9
                mReader.transmit(0, command1, command1.length, response1, response1.length);

                byte[] response2 = new byte[28];
                byte[] command2 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x0F, (byte) 0x10}; // 15 16 17 18
                mReader.transmit(0, command2, command2.length, response2, response2.length);


                byte[] response3 = new byte[28];
                byte[] command3 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x13, (byte) 0x10}; //19 20 21 22
                mReader.transmit(0, command3, command3.length, response3, response3.length);

                byte[] response4 = new byte[28];
                byte[] command4 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x17, (byte) 0x10}; //23 24 25 26
                mReader.transmit(0, command4, command4.length, response4, response4.length);

                byte[] response5 = new byte[28];
                byte[] command5 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x1B, (byte) 0x08}; // 27 28
                mReader.transmit(0, command5, command5.length, response5, response5.length);

                byte[] response6 = new byte[28];
                byte[] command6 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x1D, (byte) 0x10}; //  29
                mReader.transmit(0, command6, command6.length, response6, response6.length);


                test = new String(response, StandardCharsets.US_ASCII).trim();
                test1 = new String(response1, StandardCharsets.US_ASCII).trim();
                test2 = new String(response2, StandardCharsets.US_ASCII).trim();
                test3 = new String(response3, StandardCharsets.US_ASCII).trim();
                test4 = new String(response4, StandardCharsets.US_ASCII).trim();
                test5 = new String(response5, StandardCharsets.US_ASCII).trim();
                test6 = new String(response5, StandardCharsets.US_ASCII).trim();

                resText.setText("Test Length:" + test.length() + "Response Length:" + response.length);
                String finalStr = "";
                if (response.length != 0) {
                    int x = test.length();
                    finalStr += test.substring(0, x - 1);
                }
                if (response1.length != 0) {
                    int x = test1.length();
                    finalStr += test1.substring(0, x - 1);
                }
                if (response2.length != 0) {
                    int x = test2.length();
                    finalStr += test2.substring(0, x - 1);
                }
                if (response3.length != 0) {
                    int x = test3.length();
                    finalStr += test3.substring(0, x - 1);
                }
                if (response4.length != 0) {
                    int x = test4.length();
                    finalStr += test4.substring(0, x - 1);
                }
                if (response5.length != 0) {
                    int x = test5.length();
                    finalStr += test5.substring(0, x - 1);
                }
                if (response6.length != 0) {
                    int x = test6.length();
                    finalStr += test6.substring(0, x - 1);
                }
                if (String.valueOf(response[0]).equals("0"))
                    return "undefined";
                else
                    resText.setText("Res : " + test +
                            "\nRes 1: " + test1 +
                            "\nRes 2: " + test2 +
                            "\nRes 3: " + test3 +
                            "\nRes 4: " + test4 +
                            "\nRes 5: " + test5 +
                            "\nRes 6: " + test6 +
                            "\nFinal: " + finalStr);


                return test.substring(0, 12);

            } catch (Exception e) {
                e.printStackTrace();
                //this.emitEvent(ON_SEND_LOG, "readNfcCardCatch", e.getLocalizedMessage());
                Toast.makeText(this, "readNfcCardCatch" + e, Toast.LENGTH_SHORT).show();
                return "undefined";
            }
        }


        private boolean writeNfcCardDevice(String nfcTagUid) {

            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");

                if (nfcTagUid.length() == 1) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 2) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 3) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 4) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 5) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 6) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 7) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 8) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 9) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 10) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                }
            } catch (Exception e) {
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 11) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 12) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 13) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x4, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 14) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x4, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 15) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x4, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 16) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x4, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 17) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 18) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 19) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 20) {

                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);

                }
            } catch (Exception e) {
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 21) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 22) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 23) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 24) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 25) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 26) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 27) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 28) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 29) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 30) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                }
            } catch (Exception e) {
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 31) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 32) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 33) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) 0x00, (byte) 0x00, (byte) 0x00};

                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 34) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) 0x00, (byte) 0x00};

                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 35) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) 0x00};

                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);

                }
            } catch (Exception e) {
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 36) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};

                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 37) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 38) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 39) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 40) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 41) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 42) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 43) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 44) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);

                }
                if (nfcTagUid.length() == 45) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 46) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 47) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 48) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 49) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 50) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 51) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 52) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 53) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 54) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 55) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 56) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 57) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 58) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 59) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 60) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 61) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 62) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 63) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 64) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 65) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 66) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");

                if (nfcTagUid.length() == 67) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 68) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                }
                if (nfcTagUid.length() == 69) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 70) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "writeNfcCardCatch 70" + e, Toast.LENGTH_SHORT).show();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 71) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 72) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                }

                if (nfcTagUid.length() == 73) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                }

                if (nfcTagUid.length() == 74) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) 0x00, (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "writeNfcCardCatch 74" + e, Toast.LENGTH_SHORT).show();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 75) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                }
                if (nfcTagUid.length() == 76) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                }

                if (nfcTagUid.length() == 77) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) 0x00, (byte) 0x00, 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                }

                if (nfcTagUid.length() == 78) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) 0x00, 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                }

                if (nfcTagUid.length() == 79) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) data[78], 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                }


                if (nfcTagUid.length() == 80) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) data[78], data[79]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "writeNfcCardCatch 80" + e, Toast.LENGTH_SHORT).show();
            }


            //String test = new String(response, StandardCharsets.UTF_8).trim();
            //resText.setText("Write"+test);
            Toast.makeText(this, "writeNfcCardDeviceFunc", Toast.LENGTH_SHORT).show();
            // If the card is not written somehow
            //return !test.equals("c");

            return true;
        }

        public void writeAbove80(String nfcTagUid) {
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 81) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) data[78], data[79]};
                    byte[] twentyNinePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1D, (byte) 0x04, (byte) data[80], (byte) 0x00, (byte) 0x00, 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                    mReader.transmit(0, twentyNinePage, twentyNinePage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");

                if (nfcTagUid.length() == 82) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) data[78], data[79]};
                    byte[] twentyNinePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1D, (byte) 0x04, (byte) data[80], (byte) data[81], (byte) 0x00, 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                    mReader.transmit(0, twentyNinePage, twentyNinePage.length, response, response.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                byte[] response = new byte[356];
                byte[] data = nfcTagUid.getBytes("UTF-8");
                if (nfcTagUid.length() == 83) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) data[78], data[79]};
                    byte[] twentyNinePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1D, (byte) 0x04, (byte) data[80], (byte) data[81], (byte) data[82], 0x00};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                    mReader.transmit(0, twentyNinePage, twentyNinePage.length, response, response.length);
                }

                if (nfcTagUid.length() == 84) {
                    byte[] fourthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x04, (byte) 0x04, (byte) data[0], (byte) data[1], (byte) data[2], (byte) data[3]};
                    byte[] fifthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) data[4], (byte) data[5], (byte) data[6], (byte) data[7]};
                    byte[] sixthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x06, (byte) 0x04, (byte) data[8], (byte) data[9], (byte) data[10], (byte) data[11]};
                    byte[] seventhPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x07, (byte) 0x04, (byte) data[12], (byte) data[13], (byte) data[14], (byte) data[15]};
                    byte[] eighthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) data[16], (byte) data[17], (byte) data[18], (byte) data[19]};
                    byte[] ninthPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x09, (byte) 0x04, (byte) data[20], (byte) data[21], (byte) data[22], (byte) data[23]};
                    byte[] fifteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x0F, (byte) 0x04, (byte) data[24], (byte) data[25], (byte) data[26], (byte) data[27]};
                    byte[] sixteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x10, (byte) 0x04, (byte) data[28], (byte) data[29], (byte) data[30], (byte) data[31]};
                    byte[] seventeenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x11, (byte) 0x04, (byte) data[32], (byte) data[33], (byte) data[34], (byte) data[35]};
                    byte[] eigthteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x12, (byte) 0x04, (byte) data[36], (byte) data[37], (byte) data[38], (byte) data[39]};
                    byte[] ninteenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x13, (byte) 0x04, (byte) data[40], (byte) data[41], (byte) data[42], (byte) data[43]};
                    byte[] twentyPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x14, (byte) 0x04, (byte) data[44], (byte) data[45], (byte) data[46], (byte) data[47]};
                    byte[] twentyOnePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x15, (byte) 0x04, (byte) data[48], (byte) data[49], (byte) data[50], (byte) data[51]};
                    byte[] twentyTwoPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x16, (byte) 0x04, (byte) data[52], (byte) data[53], (byte) data[54], (byte) data[55]};
                    byte[] twentyThreePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x17, (byte) 0x04, (byte) data[56], (byte) data[57], (byte) data[58], (byte) data[59]};
                    byte[] twentyFourPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) data[60], (byte) data[61], (byte) data[62], (byte) data[63]};
                    byte[] twentyFivePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x19, (byte) 0x04, (byte) data[64], (byte) data[65], (byte) data[66], (byte) data[67]};
                    byte[] twentySixPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1A, (byte) 0x04, (byte) data[68], (byte) data[69], (byte) data[70], (byte) data[71]};
                    byte[] twentySevenPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1B, (byte) 0x04, (byte) data[72], (byte) data[73], (byte) data[74], (byte) data[75]};
                    byte[] twentyEightPage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1C, (byte) 0x04, (byte) data[76], (byte) data[77], (byte) data[78], data[79]};
                    byte[] twentyNinePage = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x1D, (byte) 0x04, (byte) data[80], (byte) data[81], (byte) data[82], data[83]};
                    mReader.transmit(0, fourthPage, fourthPage.length, response, response.length);
                    mReader.transmit(0, fifthPage, fifthPage.length, response, response.length);
                    mReader.transmit(0, sixthPage, sixthPage.length, response, response.length);
                    mReader.transmit(0, seventhPage, seventhPage.length, response, response.length);
                    mReader.transmit(0, eighthPage, eighthPage.length, response, response.length);
                    mReader.transmit(0, ninthPage, ninthPage.length, response, response.length);
                    mReader.transmit(0, fifteenPage, fifteenPage.length, response, response.length);
                    mReader.transmit(0, sixteenPage, sixteenPage.length, response, response.length);
                    mReader.transmit(0, seventeenPage, seventeenPage.length, response, response.length);
                    mReader.transmit(0, eigthteenPage, eigthteenPage.length, response, response.length);
                    mReader.transmit(0, ninteenPage, ninteenPage.length, response, response.length);
                    mReader.transmit(0, twentyPage, twentyPage.length, response, response.length);
                    mReader.transmit(0, twentyOnePage, twentyOnePage.length, response, response.length);
                    mReader.transmit(0, twentyTwoPage, twentyTwoPage.length, response, response.length);
                    mReader.transmit(0, twentyThreePage, twentyThreePage.length, response, response.length);
                    mReader.transmit(0, twentyFourPage, twentyFourPage.length, response, response.length);
                    mReader.transmit(0, twentyFivePage, twentyFivePage.length, response, response.length);
                    mReader.transmit(0, twentySixPage, twentySixPage.length, response, response.length);
                    mReader.transmit(0, twentySevenPage, twentySevenPage.length, response, response.length);
                    mReader.transmit(0, twentyEightPage, twentyEightPage.length, response, response.length);
                    mReader.transmit(0, twentyNinePage, twentyNinePage.length, response, response.length);
                }


            } catch (Exception e) {
                Toast.makeText(this, "writeNfcCardCatch 84" + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void setPasswordDevice(String password) {
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);

                byte[] response = new byte[20];
                byte[] pwd = password.getBytes();
                byte[] pack = "ok".getBytes();

                // write pwd to the 43rd page (4 bytes)
                byte[] pwdCommand = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x2B, (byte) 0x04, (byte) pwd[0], (byte) pwd[1], (byte) pwd[2], (byte) pwd[3]};
                mReader.transmit(0, pwdCommand, pwdCommand.length, response, response.length);

                // write pack to the 44th page (2 bytes)
                byte[] packCommand = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x2C, (byte) 0x04, (byte) pack[0], (byte) pack[1], (byte) 0x00, (byte) 0x00};
                mReader.transmit(0, packCommand, packCommand.length, response, response.length);

                // Read 41st byte (Auth page)
                byte[] readAuthCommand = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x29, (byte) 0x10};
                mReader.transmit(0, readAuthCommand, readAuthCommand.length, response, response.length);

                // write auth0 to 00 in the 41th page
                byte[] writeAuthCommand = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, (byte) 0x29, (byte) 0x04, (byte) response[0], (byte) response[1], (byte) response[2], (byte) 0x00};
                mReader.transmit(0, writeAuthCommand, writeAuthCommand.length, response, response.length);

                //toast("Locked");
                String s2 = new String(response, StandardCharsets.US_ASCII).trim();
                Toast.makeText(this, "Locked" + s2, Toast.LENGTH_SHORT).show();
                //this.emitEvent(ON_SHOW_QR, "", "");

            } catch (Exception e) {
                Toast.makeText(this, "LockedCatch" + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void unlockCard(String password) {

            try {

                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);

                byte[] response = new byte[20];
                byte[] pwd = password.getBytes();

                // Authorise with correct pwd
                byte[] pwdCommand = {(byte) 0x1B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                //byte[] pwdCommand = {(byte) 0x1B, (byte) pwd[0], (byte) pwd[1], (byte) pwd[2], (byte) pwd[3]};
                mReader.transmit(0, pwdCommand, pwdCommand.length, response, response.length);


                Toast.makeText(this, "UNLOCKED " + password, Toast.LENGTH_SHORT).show();
                String s = new String(response, StandardCharsets.US_ASCII).trim();
                Toast.makeText(this, "UNLOCKED 1" + s, Toast.LENGTH_SHORT).show();

                // Read 41st page of Nfc card
                byte[] writeAuthCommand = {(byte) 0x30, (byte) 41};
                mReader.transmit(0, writeAuthCommand, writeAuthCommand.length, response, response.length);

                String s1 = new String(response, StandardCharsets.UTF_8).trim();
                Toast.makeText(this, "UNLOCKED 2 " + s1, Toast.LENGTH_SHORT).show();
                String respStr = new String(response);
                if (respStr.length() >= 16) {
                    int auth0 = 0;
                    // Set auth0 to FF to unlock the card
                    byte[] packCommand = {(byte) 0xA2, (byte) 41, response[0], response[1], response[2], (byte) 0xff};
                    mReader.transmit(0, packCommand, packCommand.length, response, response.length);
                    String s2 = new String(response, StandardCharsets.UTF_8).trim();
                    Toast.makeText(this, "UNLOCKED 3" + s2, Toast.LENGTH_SHORT).show();


                }

            } catch (Exception e) {

                Toast.makeText(this, "unlockCardCatch" + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


        // Read Serial no of tag from ACR122u NFC reader device
        private String readSerialNoDevice() {
            byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            byte[] response = new byte[9];
            try {
                mReader.power(0, Reader.CARD_WARM_RESET);
                mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                mReader.transmit(0, command, command.length, response, response.length);

                String str = TextUtils.join(":", toReversedHex(response).substring(0, 21).trim().split(" "));
                resText.setText(str);
                return str;
            } catch (Exception e) {
                e.printStackTrace();
                return "Couldn't read serial no";
            }
        }

        private String toReversedHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; ++i) {
                if (i > 0) {
                    sb.append(" ");
                }
                int b = bytes[i] & 0xff;
                if (b < 0x10)
                    sb.append('0');
                sb.append(Integer.toHexString(b));
            }
            return sb.toString();
        }


        // clear memory buffers
        public void clearBuffers(byte[] response) {
            long indx;
            for (indx = 0; indx <= 262; indx++) {
                response[(int) indx] = 0;

            }
        }



}

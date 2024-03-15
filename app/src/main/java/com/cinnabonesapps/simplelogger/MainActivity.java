package com.cinnabonesapps.simplelogger;

import static android.provider.MediaStore.createWriteRequest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //и вот все ниже мне комментить теперь...
    private static final String TAG = "YarikRazrabotchik";//Метка в логах,как ими пользоваться еще сам +- понял
    private boolean WriteOnAccel,WriteOnGyro;//Указывают какие датчики записываются в логи
    private boolean WriteOn;//Указывает записываются ли наши логи
    private SensorManager msensorManager;//инициализируем менеджер по управлению датчиками

    //константы
    private final int Accel = 0;
    private final int Gyro = 1;

    //инициализируем кнопочки
    private Button btnActTwo;

    //свитчи
    Switch textViewAccel_main;
    Switch textViewGyro_main;

    //инициализруем текстовые блоки
    private TextView text_main2;

    //момент времени с начала измерения
    private long time_0;

    //Содержимое файла для асселерометра
    private float[] accelData;
    private String LogAccel;

    //Содержимое файла для гироскопа
    private float[] gyrData;
    private String LogGyr;

    //так удобнее
    private Activity thisActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Проверка разрешения доступа к памяти для android 6.0+
        if (Build.VERSION.SDK_INT >= 23) {
            //динамическое получение прав на доступ к памяти
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted");
            } else {
                Log.d(TAG, "Permission is revoked");
                //запрашиваем разрешение
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            //если доступ не дали
        }

        //ну тут тонна инициализации
        accelData = new float[3];
        msensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        WriteOnGyro = WriteOnAccel = false;
        //запускаем главный слой
        setContentView(R.layout.activity_main);

        //привязываемся к элементам XML файла
        btnActTwo = findViewById(R.id.button1);
        textViewAccel_main = findViewById(R.id.switchAccel);
        textViewGyro_main = findViewById(R.id.switchGyro);
        text_main2 = findViewById(R.id.textViewDebug_main);

        //привязываем обработчик нажатий к общему интерфейсу onClick
        btnActTwo.setOnClickListener(v -> {
            //-----------------------------------------------
            if(!WriteOn){
                LogAccel = LogGyr = "time(ms),Fx,Fy,Fz";
                WriteOn = true;
                WriteOnAccel = WriteOnGyro = true;

                time_0 = System.currentTimeMillis();
                btnActTwo.setText("STOP");
            }else {
                SaveFile(Accel);
                SaveFile(Gyro);
                WriteOnAccel = WriteOnGyro = false;
                btnActTwo.setText("START RECORDING");
            }
            //------------------------------------------------
        });
    }


    @Override
    protected void onResume(){
        super.onResume();
        msensorManager.registerListener(this,msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        msensorManager.registerListener(this,msensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause(){
        super.onPause();
        msensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int type = event.sensor.getType(); //Определяем тип датчика
        switch (type){
            case Sensor.TYPE_ACCELEROMETER:  //Если акселерометр
                accelData = event.values.clone();
                textViewAccel_main.setText("X = " + accelData[0] + "\nY = " + accelData[1] + "\nZ = " + accelData[2]);
                if(WriteOnAccel){
                    LogAccel += "\n" + (System.currentTimeMillis()-time_0) + "," + accelData[0] + "," + accelData[1] + "," + accelData[2];
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyrData = event.values.clone();
                textViewGyro_main.setText("X = " + gyrData[0] + "\nY = " + gyrData[1] + "\nZ = " + gyrData[2]);
                if(WriteOnGyro){
                    LogGyr += "\n" + (System.currentTimeMillis()-time_0) + "," + gyrData[0] + "," + gyrData[1] + "," + gyrData[2];
                }
                break;
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //просто есть в интерфейсе,без понятий для чего оно,но нужно оставить
    }

    public void SaveFile(int NameSensor){
        Date date = new Date();
        String filename;
        switch(NameSensor){
            case Accel:
                filename = "logAccel";
                break;
            case Gyro:
                filename = "logGyro";
                break;
            default:
                filename = "logGyroDef.csv";

        }

        Context context = getApplicationContext();
        File dir = new File(context.getFilesDir(), "logs");
        if(!dir.exists()){
            dir.mkdir();
        }

        String body = "";
        switch(NameSensor){
            case Accel:
                Log.i("test", "accelerometer");
                body = LogAccel;
                break;
            case Gyro:
                Log.i("test", "gyroscope");
                body = LogGyr;
                break;
            default:
        }

        try {
            ContentValues values = new ContentValues();

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);       //file name
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");        //file extension, will automatically add to file
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/simplelogs/");     //end "/" is not mandatory

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);      //important!

            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            outputStream.write(body.getBytes());

            outputStream.close();

            text_main2.setText("File created successfully");

        } catch (Exception e){
            text_main2.setText("Fail to create file");
            e.printStackTrace();
        }
        WriteOn = false;
    }
}
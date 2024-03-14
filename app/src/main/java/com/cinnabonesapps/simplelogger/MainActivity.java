package com.cinnabonesapps.simplelogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
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
        btnActTwo.setOnClickListener(this);

    }
    //Обработчик нажатий
    public void onClick(View v) {
        //в case помещаем id наших кнопок
        switch (v.getId()) {
            case R.id.button1:
                //-----------------------------------------------
                if(!WriteOn){
                    LogAccel = LogGyr = "time(ms),Fx,Fy,Fz";
                    WriteOn = true;
                    WriteOnAccel = WriteOnGyro = true;

                    time_0 = System.currentTimeMillis();
                    btnActTwo.setText("STOP");
                }else{
                    SaveFile(Accel);
                    SaveFile(Gyro);
                    WriteOnAccel = WriteOnGyro = false;
                    btnActTwo.setText("Start recording");
                }
                break;
                //------------------------------------------------
            //case R.id.
        }
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
    public boolean isExternalStorageWritable()
    {
        //проверяет есть ли доступ к памяти
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }



    public void SaveFile(int NameSensor){
        Date date = new Date();
        String fullpath, foldername, filename;
        foldername = "myFolder";
        switch(NameSensor){
            case Accel:
                filename = "logAccel.csv";
                break;
            case Gyro:
                filename = "logGyro"+ NameSensor + ".csv";
                break;
            default:
                filename = "logGyroDef.csv";

        }
        fullpath = Environment.getExternalStorageDirectory()
                + "/" + foldername
                + "/" + date.toString()
                + "/" + filename;
        if(isExternalStorageWritable()){
            switch(NameSensor){
                case Accel:
                    WriteInStorage(fullpath,LogAccel);
                    break;
                case Gyro:
                    WriteInStorage(fullpath,LogGyr);
                    break;
                default:
            }

        }else text_main2.setText("Ошибка записи");
        WriteOn = false;
    }
    public void WriteInStorage (String filePath, String FileContent)
    {
        //Создание объекта файла.
        File fhandle = new File(filePath);
        try
        {
            //Если нет директорий в пути, то они будут созданы:
            if (!fhandle.getParentFile().exists()) {
                fhandle.getParentFile().mkdirs();
            }
            //Если файл существует, то он будет перезаписан:
            boolean created = fhandle.createNewFile();
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(FileContent);
            myOutWriter.close();
            fOut.close();
            text_main2.setText("Successfully saved");
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            text_main2.setText("Path " + fhandle.getAbsolutePath() + ", " + e.toString());
        }
    }
}
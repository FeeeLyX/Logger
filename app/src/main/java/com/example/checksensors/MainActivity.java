package com.example.checksensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private static final String TAG = "YarikRazrabotchik";
    boolean WriteOn;
    Button btnActTwo;
    SensorManager msensorManager;
    TextView text_main;
    TextView text_main2;
    private float[] accelData;
    String LogAccel;
    long time_0 = 0;
    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //инициализация массива хранения данных с асселерометра
        if (Build.VERSION.SDK_INT >= 23) {
            //динамическое получение прав на INTERNET
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted");

                //делаете что-то с интернетом

            } else {
                Log.d(TAG, "Permission is revoked");
                //запрашиваем разрешение
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {

        }
        accelData = new float[3];
        msensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        WriteOn = false;
        LogAccel = "time(ms),Fx,Fy,Fz";
        setContentView(R.layout.activity_main);

        btnActTwo = (Button) findViewById(R.id.button1);
        text_main = findViewById(R.id.textView_main);
        text_main2 = findViewById(R.id.textView_main2);

        btnActTwo.setOnClickListener(this);
        //writeFile();
        //text_main2.setText(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    public void onClick(View v) {
        Date date = new Date();
        switch (v.getId()) {
            case R.id.button1:
                if(!WriteOn){
                    LogAccel = "time(ms),Fx,Fy,Fz";
                    WriteOn = true;
                    time_0 = System.currentTimeMillis();
                    btnActTwo.setText("Остановить запись");
                }else{
                    String fullpath, foldername, filename;
                    foldername = "myFolder";
                    filename = date.toString() + "log.csv";
                    fullpath = Environment.getExternalStorageDirectory()
                            + "/myFolder"
                            + "/" + filename;
                    if(isExternalStorageWritable()){
                        SaveFile(fullpath,LogAccel);
                    }

                    WriteOn = false;
                    btnActTwo.setText("Запустить запись логов");
                }
                break;
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        msensorManager.registerListener(this,msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause(){
        super.onPause();
        msensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int type = event.sensor.getType(); //Определяем тип датчика
        if (type == Sensor.TYPE_ACCELEROMETER) { //Если акселерометр
            accelData = event.values.clone();
            text_main.setText("X = " + accelData[0] + "\nY = " + accelData[1] + "\nZ = " + accelData[2]);
            if(WriteOn){
                LogAccel += "\n" + (System.currentTimeMillis()-time_0) + "," + accelData[0] + "," + accelData[1] + "," + accelData[2];
            }
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    /*void writeFile() {
        try {
            // отрываем поток для записи

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput("LogAcc" + date.toString(), MODE_PRIVATE)));
            // пишем данные
            bw.write(LogAccel);
            // закрываем поток
            bw.close();

            text_main2.setText("Работает вроде");
            Log.d("Тестим тестим", "Файл записан");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    /* Проверяет, доступно ли external storage для чтения и записи */
    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }

    public void SaveFile (String filePath, String FileContent)
    {
        //Создание объекта файла.
        File fhandle = new File(filePath);
        int hui = 0;
        try
        {
            //Если нет директорий в пути, то они будут созданы:
            if (!fhandle.getParentFile().exists()) {
                hui = 1;
                fhandle.getParentFile().mkdirs();
            }
            //Если файл существует, то он будет перезаписан:
            boolean created = fhandle.createNewFile();
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(FileContent);
            myOutWriter.close();
            fOut.close();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            text_main2.setText("Path " + fhandle.getAbsolutePath() + ", " + e.toString());
        }
    }
}
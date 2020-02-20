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

class CheckSensor extends Thread
{
    @Override
    public void run()
    {
        int temp;


    }
    //Название функции за себя явно говорил,но для одаренных: компанует содержимое в файл и сохраняет его в указанную директорию
    public void SaveFile (String FileContent)
    {
        //Создание объекта файла.
        Date date = new Date();
        String fullpath, foldername, filename;
        foldername = "myFolder";
        filename = date.toString() + "log.csv";
        fullpath = Environment.getExternalStorageDirectory()
                + "/myFolder"
                + "/" + filename;
        File fhandle = new File(fullpath);
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
            Log.d(MainActivity.TAG, "Path " + fhandle.getAbsolutePath() + ", " + e.toString());
        }
    }
}

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    //и вот все ниже мне комментить теперь...
    public static final String TAG = "YarikRazrabotchik";//Метка в логах,как ими пользоваться еще сам +- понял
    boolean WriteOn;//Указывает записываются ли наши логи
    Button btnActTwo;
    SensorManager msensorManager;
    TextView text_main;
    TextView text_main2;
    private float[] accelData;
    String LogAccel;//Содержимое файла
    long time_0 = 0;
    private Activity thisActivity;//так удобнее
//лвлвллвлвв
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
        WriteOn = false;
        LogAccel = "time(ms),Fx,Fy,Fz";
        //запускаем главный слой
        setContentView(R.layout.activity_main);
        //привязываемся к элементам XML файла
        btnActTwo = findViewById(R.id.button1);
        text_main = findViewById(R.id.textView_main);
        text_main2 = findViewById(R.id.textView_main2);
        //привязываем обработчик нажатий к общему интерфейсу onClick
        btnActTwo.setOnClickListener(this);

    }

    public void onClick(View v) {

        //в case помещаем id наших кнопок
        switch (v.getId()) {
            case R.id.button1:
                //-----------------------------------------------
                if(!WriteOn){
                    LogAccel = "time(ms),Fx,Fy,Fz";
                    WriteOn = true;
                    time_0 = System.currentTimeMillis();
                    btnActTwo.setText("Остановить запись");
                }else{

                    if(isExternalStorageWritable()){
                        CheckSensor checkSensor = new CheckSensor();
                        checkSensor.start();
                        WriteOn = false;
                        btnActTwo.setText("Запустить запись логов");
                    }else{

                    }

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
        //просто есть в интерфейсе,без понятий для чего оно,но нужно оставить
    }
    //проверяет есть ли доступ к памяти
    public boolean isExternalStorageWritable()
    {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }

}
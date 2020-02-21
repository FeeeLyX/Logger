//Lysenko Y.A.


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
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import static androidx.core.content.ContextCompat.getSystemService;

//Лятеки-----------------------------------------------------------------------------------------------
class CheckSensor extends Thread
{
    public static final String TAG = "YarikRazrabotchik";
    private float[] accelDataWrite = new float[3];
    private String LogAccelWrite= "time(ms),Fx,Fy,Fz";
    private long time_0 = System.currentTimeMillis();
    private String PathSaveFile;
    private Context mContext;
    @Override
    public void run()
    {
        SensorManager msensorManager;
        String sensorService= Context.SENSOR_SERVICE;
        msensorManager = (SensorManager) mContext.getSystemService(sensorService);
        SensorEventListener selMsensorManager = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                final int type = event.sensor.getType(); //Определяем тип датчика
                if (type == Sensor.TYPE_ACCELEROMETER) { //Если акселерометр
                    accelDataWrite = event.values.clone();
                    LogAccelWrite += "\n" + (System.currentTimeMillis()-time_0) + "," + accelDataWrite[0] + "," + accelDataWrite[1] + "," + accelDataWrite[2];

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        msensorManager.registerListener(selMsensorManager,msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
        while(!interrupted()){
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SaveFile(LogAccelWrite);
        return;
    }
    public void setPathSaveFile(String temp){
        PathSaveFile = temp;
    }
    public void setThis(Context mContext){
        this.mContext = mContext;
    }
    //Название функции за себя явно говорил,но для одаренных: компанует содержимое в файл и сохраняет его в указанную директорию
    private void SaveFile (String FileContent)
    {
        Toast.makeText(mContext,"Сохраняет",Toast.LENGTH_SHORT).show();
        //Создание объекта файла.
        Date date = new Date();
        String fullpath, foldername, filename;
        foldername = PathSaveFile;
        filename = date.toString() + "log.csv";
        fullpath = Environment.getExternalStorageDirectory()
                + "/" + foldername
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
            Toast.makeText(mContext,"Сохранил",Toast.LENGTH_SHORT).show();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            Log.d(MainActivity.TAG, "Path " + fhandle.getAbsolutePath() + ", " + e.toString());
        }
    }
}
//=================================================================================================================
public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    //и вот все ниже мне комментить теперь...
    public static final String TAG = "YarikRazrabotchik";//Метка в логах,как ими пользоваться еще сам +- понял
    boolean WriteOn;//Указывает записываются ли наши логи
    Button btnActTwo;
    SensorManager msensorManager;
    TextView textViewMain;
    TextView textViewMain2;
    public TextView textViewMain3;
    private float[] accelData;
    private Activity thisActivity;//так удобнее
    CheckSensor checkSensor = new CheckSensor();
//лвлвллвлвв
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkSensor.start();
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
        msensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        WriteOn = false;
        //запускаем главный слой
        setContentView(R.layout.activity_main);
        //привязываемся к элементам XML файла
        btnActTwo = findViewById(R.id.button1);
        textViewMain = findViewById(R.id.textView_main);
        textViewMain2 = findViewById(R.id.textView_main2);
        textViewMain3 = findViewById(R.id.textView_main2);
        //привязываем обработчик нажатий к общему интерфейсу onClick
        btnActTwo.setOnClickListener(this);

    }

    public void onClick(View v) {
        Log.d(TAG,"Клик");
        //в case помещаем id наших кнопок
        switch (v.getId()) {
            case R.id.button1:
                //-----------------------------------------------
                if(!WriteOn){
                    if(isExternalStorageWritable()){
                        WriteOn = true;
                        checkSensor.setThis(this);

                        checkSensor.setPathSaveFile("myFolder");
                    }else{

                    }
                    btnActTwo.setText("Остановить запись");
                }else{
                    checkSensor.interrupt();
                    WriteOn = false;
                    btnActTwo.setText("Запустить запись логов");
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
            textViewMain.setText("X = " + accelData[0] + "\nY = " + accelData[1] + "\nZ = " + accelData[2]);
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
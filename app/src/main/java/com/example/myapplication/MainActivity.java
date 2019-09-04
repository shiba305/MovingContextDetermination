package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.sqrt;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;


public class  MainActivity extends AppCompatActivity implements SensorEventListener {
    private Sensor accSensor;
    private TextView mX;
    private TextView mY;
    private TextView mZ;
    private TextView comp;
    private TextView acc;
    private TextView check_Text;
    private ProgressBar bar;
    public double compositeAcc = 0;
    private String temp;
    private SensorManager manager;
    public int button_flag = 0;
    public int check_flag = 0;
    public int progress_flag = 0;
    public int complete_flag = 0;
    public int Progress_Rate = 0;
    public double AccX = 0;
    public double AccY = 0;
    public double AccZ = 0;

    public void onAccuracyChanged(Sensor sensor, int n) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {//savedInstanceState : 画面回転やガベージコレクション等からActivityが死んでも復元を試みる
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //activity_main.xmlに画面レイアウトを設定して読み込ませる





        //フォルダを作成
        String path = Environment.getExternalStorageDirectory().getPath() + "/weka/";
        File root = new File(path);
        if(!root.exists()){
            root.mkdir();
        }
        manager = (SensorManager) this.getSystemService(SENSOR_SERVICE); //SensorManagerを取得
        // SensorManagerはセンサーによって異なる制御方法の違いを吸収してくれるクラス)
        assert this.manager != null;
        accSensor = this.manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Button standing_btn = findViewById(R.id.button4);
        Button walking_btn = findViewById(R.id.button);
        Button running_btn = findViewById(R.id.button5);
        Button stop_btn = findViewById(R.id.button2);
        Button check_btn = findViewById(R.id.button6);
        final ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setProgress(0);

//        schedule(progressBar);



        walking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_flag = 1;
                progress_flag = 2;
                try {
                    schedule(progressBar);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        standing_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_flag = 1;
                progress_flag = 1;
                try {
                    schedule(progressBar);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        running_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_flag = 1;
                progress_flag = 3;
                try {
                    schedule(progressBar);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });





        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setProgress(0);
                button_flag = 0;
                progress_flag = 0;

            }
        });

        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_flag = 1;
                button_flag = 1;
                readArff();

            }
        });

        mX = this.findViewById(R.id.textView);
        mY = this.findViewById(R.id.textView4);
        mZ = this.findViewById(R.id.textView6);
        acc = this.findViewById(R.id.textView5);
        comp = this.findViewById(R.id.textView9);
        check_Text = this.findViewById(R.id.textView10);

    }


    @Override
    protected void onPause() {
        super.onPause();
        this.manager.unregisterListener(this, this.accSensor);
    }

    protected void onResume() {
        super.onResume();
        this.manager.registerListener(this, this.accSensor, 0);
    }

    public void schedule(final ProgressBar progressBar) throws IOException {
        progressBar.setProgress(0);
        if(progress_flag == 1) {
            progressBar.setProgress(0);
            final Timer timer = new Timer(); // 今回追加する処理
            TimerTask task = new TimerTask() {
                int count = 0;
                FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/weka/standing.csv", false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                public void run() {
                    // 定期的に実行したい処理
                count++;
                        progressBar.setProgress(10 * count);
                    pw.print(compositeAcc);
                    pw.print(",standing");
                    pw.println();

                    if (count >= 10 || progress_flag == 0) {
                        pw.close();
                        timer.cancel();
                        complete_flag++;
                        if (complete_flag == 3){
                            comp.setText("全て測定完了");
                            try {
                                createArff();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        button_flag = 0;
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000); // 今回追加する処理
        }
        else{
                progressBar.setProgress(0);
        }

        if(progress_flag == 2) {
            progressBar.setProgress(0);
            final Timer timer = new Timer(); // 今回追加する処理
            TimerTask task = new TimerTask() {
                int count = 0;

                FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/weka/walking.csv", false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                public void run() {
                    // 定期的に実行したい処理
                    count++;
                    progressBar.setProgress(10 * count);
                    pw.print(compositeAcc);
                    pw.print(",walking");
                    pw.println();

                    if (count >= 10 || progress_flag == 0) {
                        pw.close();
                        timer.cancel();
                        complete_flag++;
                        if (complete_flag == 3){
                            comp.setText("全て測定完了");
                            try {
                                createArff();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        button_flag = 0;
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000); // 今回追加する処理
        }
        else{
            progressBar.setProgress(0);
        }

        if(progress_flag == 3) {
            progressBar.setProgress(0);
            final Timer timer = new Timer(); // 今回追加する処理
            TimerTask task = new TimerTask() {
                int count = 0;
                FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/weka/running.csv", false);
                PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                public void run() {
                    // 定期的に実行したい処理
                    count++;
                    progressBar.setProgress(10 * count);
                    pw.print(compositeAcc);
                    pw.print(",running");
                    pw.println();

                    if (count >= 10 || progress_flag == 0) {
                        pw.close();
                        timer.cancel();
                        complete_flag++;
                        if (complete_flag == 3){
                            comp.setText("全て測定完了");
                            try {
                                createArff();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        button_flag = 0;
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000); // 今回追加する処理
        }
        else{
            progressBar.setProgress(0);
        }


    }


    public void createArff()throws IOException {
        String text_data = "@relation file\n\n@attribute acceleration real\n@attribute state\n{standing,walking,running}\n\n@data\n";


        File stand_file = new File(Environment.getExternalStorageDirectory().getPath() + "/weka/standing.csv");
        // 2.ファイルが存在しない場合に例外が発生するので確認する
        if (!stand_file.exists()) {
            System.out.print("ファイルが存在しません");
            return;
        }
        // 3.FileReaderクラスとreadメソッドを使って1文字ずつ読み込み表示する
        FileReader fileReader = new FileReader(stand_file);
        int data;
        while ((data = fileReader.read()) != -1) {
            System.out.print((char) data);
            text_data += (char) data;
        }
        // 4.最後にファイルを閉じてリソースを開放する
        fileReader.close();


        File walk_file = new File(Environment.getExternalStorageDirectory().getPath() + "/weka/walking.csv");
        // 2.ファイルが存在しない場合に例外が発生するので確認する
        if (!stand_file.exists()) {
            System.out.print("ファイルが存在しません");
            return;
        }
        // 3.FileReaderクラスとreadメソッドを使って1文字ずつ読み込み表示する
        FileReader fileReader2 = new FileReader(walk_file);
//        data = 0;
        while ((data = fileReader2.read()) != -1) {
            System.out.print((char) data);
            text_data += (char) data;
        }
        // 4.最後にファイルを閉じてリソースを開放する
        fileReader.close();


        File run_file = new File(Environment.getExternalStorageDirectory().getPath() + "/weka/running.csv");
        // 2.ファイルが存在しない場合に例外が発生するので確認する
        if (!run_file.exists()) {
            System.out.print("ファイルが存在しません");
            return;
        }
        // 3.FileReaderクラスとreadメソッドを使って1文字ずつ読み込み表示する
        FileReader fileReader3 = new FileReader(run_file);
//        data = 0;
        while ((data = fileReader3.read()) != -1) {
            System.out.print((char) data);
            text_data += (char) data;
        }
        // 4.最後にファイルを閉じてリソースを開放する
        fileReader.close();

        FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/weka/weka.arff", false);
        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

        pw.print(text_data);
        pw.close();

    }

    public void readArff(){
        DataSource source = null;
        try {
            source = new DataSource("weka.arff");
            Instances instances = source.getDataSet();
            instances.setClassIndex(1);//setClassIndexは分類したい属性の番号
            Classifier classifier = new SMO();
            classifier.buildClassifier(instances);//buildClassifierを呼び出して分類機を構築する

            //評価、Evalutionオブジェクトを生成してモデルと学習データを入れる。
            Evaluation eval = new Evaluation(instances);
            eval.evaluateModel(classifier, instances);

//            toSummaryStringでモデルに沿った結果が見れる
            System.out.println(eval.toSummaryString());

//            FastVector out = new FastVector(3);

            Attribute acceleraton = new Attribute("acceleraton", 0);
//            out.addElement("standing");
//            out.addElement("walking");
//            out.addElement("running");
//            Attribute state = new Attribute("state",out, 1);

            while (button_flag == 1){
                Instance instance = instances.get(1);//new Instance(1);
                instance.setValue(acceleraton, compositeAcc);
                instance.setDataset(instances);

                double result = classifier.classifyInstance(instance);
                check_Text.setText((int) result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    @SuppressLint("SetTextI18n")//センサーの値が変化すると呼ばれるリスナー、加速度センサ３軸の値をmX,mY,mZに更新
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (button_flag == 1) {

                int[][] arrn = new int[3][100];
                for (int i = 1; i <= arrn.length; i++) {
                    Log.d("SENSOR_DATA", "TYPE_ACCELEROMETER1 = " + sensorEvent.values[0]);
                    Log.d("SENSOR_DATA", "TYPE_ACCELEROMETER2 = " + sensorEvent.values[1]);
                    Log.d("SENSOR_DATA", "TYPE_ACCELEROMETER3 = " + sensorEvent.values[2]);

                    mX.setText("X-axis : " + sensorEvent.values[0]);
                    mY.setText("Y-axis : " + sensorEvent.values[1]);
                    mZ.setText("Z-axis : " + sensorEvent.values[2]);

                    AccX = sensorEvent.values[0];
                    AccY = sensorEvent.values[1];
                    AccZ = sensorEvent.values[2];


                    compositeAcc = Math.pow(AccX,2) + Math.pow(AccY, 2) + Math.pow(AccZ, 2);
                    compositeAcc = sqrt(compositeAcc);
                    acc.setText("acceleration : " + compositeAcc);
                    }
            } else {
                mX.setText("止まっていますX");
                mY.setText("止まっていますY");
                mZ.setText("止まっていますZ");
                acc.setText("止まっています");
            }
        }
    }


}


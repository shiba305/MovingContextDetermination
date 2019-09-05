package com.example.myapplication;

import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import static java.lang.Math.sqrt;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


public class  MainActivity extends AppCompatActivity implements SensorEventListener {
    private Sensor accSensor;
    private TextView mX;
    private TextView mY;
    private TextView mZ;
    private TextView comp;
    private TextView acc;
    private TextView check_Text;
    public double compositeAcc = 0;
    private SensorManager manager;
    public int button_flag = 0;
    public int progress_flag = 0;
    public int complete_flag = 0;
    public double AccX = 0;
    public double AccY = 0;
    public double AccZ = 0;
    public int count = 0;

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
                if (complete_flag >= 3) {
                    button_flag = 1;
                }
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
                        progressBar.setProgress(count);
                    pw.print(compositeAcc);
                    pw.print(", standing");
                    pw.println();

                    if (count >= 400 || progress_flag == 0) {
                        pw.close();
                        timer.cancel();
                        button_flag = 0;
                        complete_flag++;
                        if (complete_flag >= 3){
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
            timer.scheduleAtFixedRate(task, 25, 25); // 今回追加する処理
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
                    progressBar.setProgress(count);
                    pw.print(compositeAcc);
                    pw.print(", walking");
                    pw.println();

                    if (count >= 400 || progress_flag == 0) {
                        pw.close();
                        timer.cancel();
                        button_flag = 0;
                        complete_flag++;
                        if (complete_flag >= 3){
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
            timer.scheduleAtFixedRate(task, 25, 25); // 今回追加する処理
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
                    progressBar.setProgress(count);
                    pw.print(compositeAcc);
                    pw.print(", running");
                    pw.println();

                    if (count >= 400 || progress_flag == 0) {
                        pw.close();
                        timer.cancel();
                        button_flag = 0;
                        complete_flag++;
                        if (complete_flag >= 3){
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
            timer.scheduleAtFixedRate(task,25 , 25); // 今回追加する処理
        }
        else{
            progressBar.setProgress(0);
        }
    }


    public void createArff()throws IOException {
        String text_data = "@relation file\n\n@attribute acceleration real\n@attribute state{standing,walking,running}\n\n@data\n";


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
//            System.out.print((char) data);
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

                if (complete_flag >= 3) {
                    try {
                        /*
                         * 1 : arffのDataSourceインスタンスの生成
                         * 2 : 訓練データのインスタンス生成　.getDataSet();
                         * 3 : 分類したい属性の番号を指定　setClassIndex(1);
                         * 4 : J48の分類器を生成　Classifier classifier = new J48();
                         * 5 : 分類機の構築　classifier.buildClassifier(instances);
                         * */
                        count++;

                        if (count % 100 == 0) {
                            count = 0;

                            String path = Environment.getExternalStorageDirectory().getPath() + "/weka/weka.arff";
                            DataSource source = new DataSource(path);
                            Instances instances = source.getDataSet();
                            instances.setClassIndex(1);//setClassIndexは分類したい属性の番号
                            Classifier classifier = new J48();//分類器の生成
                            classifier.buildClassifier(instances);//buildClassifierを呼び出して分類機を構築する

                            //評価、Evalutionオブジェクトを生成してモデルと学習データを入れる。
                            Evaluation eval = new Evaluation(instances);
                            eval.evaluateModel(classifier, instances);
                            //toSummaryStringでモデルに沿った結果が見れる
                            Attribute acceleraton = new Attribute("acceleraton", 0);

                            Instance instance = new DenseInstance(2);
                            instance.setValue(acceleraton, compositeAcc);
                            instance.setDataset(instances);
                            double result = classifier.classifyInstance(instance);
                            String pattern;
                            int pt = (int) result;
                            switch (pt) {
                                case 0:
                                    pattern = "立ち";
                                    break;
                                case 1:
                                    pattern = "歩き";
                                    break;
                                case 2:
                                    pattern = "走り";
                                    break;
                                default:
                                    pattern = "立ち";
                            }
                            check_Text.setText(pattern);
                        }
//
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                    }
            } else {
                mX.setText("止まっていますX");
                mY.setText("止まっていますY");
                mZ.setText("止まっていますZ");
                acc.setText("止まっています");
            }
        }
    }




package com.keraseye.xkk.keraseye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.*;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.FileInputStream;
import java.io.InputStream;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;
import java.nio.IntBuffer;

public class MainActivity extends AppCompatActivity {
    /*
     * 在需要调用TensoFlow的地方，加载so库“System.loadLibrary("tensorflow_inference");
     * 并”import org.tensorflow.contrib.android.TensorFlowInferenceInterface;就可以使用了
     * */
    //Load the tensorflow inference library
    //static{}(即static块)，会在类被加载的时候执行且仅会被执行一次，一般用来初始化静态变量和调用静态方法。
    static {
        System.loadLibrary("tensorflow_inference");

    }

    //PATH TO OUR MODEL FILE AND NAMES OF THE INPUT AND OUTPUT NODES
    //各节点名称
    private String MODEL_PATH = "file:///android_asset/tensor_model.pb";
    private String INPUT_NAME = "input_10:0";
    private String OUTPUT_NAME = "conv2d_215/Sigmoid:0";
    private TensorFlowInferenceInterface tf;

    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    //保存图片和图片尺寸的
    float[] PREDICTIONS = new float[36864];
    private float[] floatValues;
    private int[] INPUT_SIZE = {224,224,3};

    ImageView imageView;
    TextView resultView;
    Button buttonSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tf = new TensorFlowInferenceInterface(getAssets(),MODEL_PATH);

        imageView=(ImageView)findViewById(R.id.imageView1);
        resultView=(TextView)findViewById(R.id.text_show);
        buttonSub=(Button)findViewById(R.id.button1);

        buttonSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    System.out.println("哈哈哈");
                    InputStream imageStream = getAssets().open("222.jpg");
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(bitmap);

                    predict(bitmap);

                }catch(Exception e){

                }


            }
        });

    }

    //FUNCTION TO COMPUTE THE MAXIMUM PREDICTION AND ITS CONFIDENCE
    public Object[] argmax(float[] array){

        int best = -1;
        float best_confidence = 0.0f;
        for(int i = 0;i < array.length;i++){
            float value = array[i];
            if (value > best_confidence){
                best_confidence = value;
                best = i;
            }
        }
        return new Object[]{best,best_confidence};
    }



    public void predict(final Bitmap bitmap){

        //Runs inference in background thread
        new AsyncTask<Integer,Integer,Integer>(){

            @Override
            protected Integer doInBackground(Integer ...params){
                //Resize the image into 224 x 224
                Bitmap resized_image = ImageUtils.processBitmap(bitmap,256,144);
                final Bitmap a = resized_image;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        imageView.setImageBitmap(a);
                    }
                });
                long startTime=System.currentTimeMillis();   //获取开始时间
//                doSomeThing();  //测试的代码段
                //Normalize the pixels
                floatValues = ImageUtils.normalizeBitmap(resized_image,256, 144,127.5f,1.0f);

                //Pass input into the tensorflow
                tf.feed(INPUT_NAME,floatValues,1,144,256,3);

                //compute predictions
                tf.run(new String[]{OUTPUT_NAME});


                //copy the output into the PREDICTIONS array
                tf.fetch(OUTPUT_NAME,PREDICTIONS);

                long endTime=System.currentTimeMillis(); //获取结束时间
                System.out.println("程序运行时间： "+(endTime-startTime));
                System.out.println(PREDICTIONS);
//                final Bitmap Img = Bitmap.createBitmap(256, 144, Bitmap.Config.RGB_565);
//                Img.copyPixelsFromBuffer(IntBuffer.wrap(ImageUtils.floatToBitmap(PREDICTIONS)));
                final Bitmap bwimg = ImageUtils.floatToBitmap(resized_image,PREDICTIONS);
//                Img.setPixels(ia, 0, 256, 0, 0, 256, 144);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        imageView.setImageBitmap(bwimg);
                    }
                });
                //Obtained highest prediction
//                Object[] results = argmax(PREDICTIONS);
//                System.out.println(ia[2]);
//                int class_index = (Integer) results[0];
//                float confidence = (Float) results[1];

                try{
//                    final String conf = String.valueOf(confidence * 100).substring(0,5);
                    //Convert predicted class index into actual label name
//                    final String label = ImageUtils.getLabel(getAssets().open("labels.json"),class_index);
                    //Display result on UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText(PREDICTIONS[2] + " : " + PREDICTIONS[29232]);
                        }
                    });
                } catch (Exception e){
                }

                return 0;
            }

        }.execute(0);

    }
}

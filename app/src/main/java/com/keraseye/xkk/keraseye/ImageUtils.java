package com.keraseye.xkk.keraseye;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.json.*;

/**
 * Utility class for manipulating images.
 **/
public class ImageUtils {
    /**
     * Returns a transformation matrix from one reference frame into another.
     * Handles cropping (if maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth Width of source frame.
     * @param srcHeight Height of source frame.
     * @param dstWidth Width of destination frame.
     * @param dstHeight Height of destination frame.
     * @param applyRotation Amount of rotation to apply from one frame to another.
     *  Must be a multiple of 90.
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     * cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    public static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation,
            final boolean maintainAspectRatio) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }


    public static Bitmap processBitmap(Bitmap source,int wsize,int hsize){

        int image_height = source.getHeight();
        int image_width = source.getWidth();

        Bitmap croppedBitmap = Bitmap.createBitmap(wsize, hsize, Bitmap.Config.ARGB_8888);

        Matrix frameToCropTransformations = getTransformationMatrix(image_width,image_height,wsize, hsize,0,false);
        Matrix cropToFrameTransformations = new Matrix();
        frameToCropTransformations.invert(cropToFrameTransformations);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(source, frameToCropTransformations, null);

        return croppedBitmap;


    }

    public static float[] normalizeBitmap(Bitmap source,int wsize, int hsize,float mean,float std){

        float[] output = new float[wsize * hsize * 3];

        int[] intValues = new int[source.getHeight() * source.getWidth()];
//        System.out.println("hh"+source.getHeight());
//        System.out.println("ww"+source.getWidth());

        source.getPixels(intValues, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            output[i * 3 + 2] = ((val >> 16) & 0xFF)/255f;
//            System.out.println(output[i * 3] );
            output[i * 3 + 1] = ((val >> 8) & 0xFF)/255f;
            output[i * 3 + 0] = (val & 0xFF)/255f;
        }

        return output;

    }

    public static Object[] argmax(float[] array){


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


    public static String getLabel( InputStream jsonStream,int index){
        String label = "";
        try {

            byte[] jsonData = new byte[jsonStream.available()];
            jsonStream.read(jsonData);
            jsonStream.close();

            String jsonString = new String(jsonData,"utf-8");

            JSONObject object = new JSONObject(jsonString);

            label = object.getString(String.valueOf(index));



        }
        catch (Exception e){


        }
        return label;
    }

    public static Bitmap floatToBitmap(Bitmap bitmap,float[] img_float_array){

//        float[] output = new float[wsize * hsize * 3];

        int[] intValues = new int[bitmap.getHeight() * bitmap.getWidth()];

        int c=0;

        //这样写有很重的问题，因为TF的返回shape不是想象中的那样，需要自己构造
//        int c= 0;
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//        for(int i = 0 ; i < bitmap.getHeight();i++){
//            for(int j =0 ;j<bitmap.getWidth();j++){
////                System.out.println(img_float_array.length);
//                if(img_float_array[j + i*bitmap.getHeight()]>=0.5){
//                    System.out.println(img_float_array[j + i*bitmap.getHeight()]);
//                    bitmap.setPixel(j,i,Color.WHITE);
//                c++;}
//                else{
//                    bitmap.setPixel(j,i,Color.BLACK);
//                }
//            }
//        }
//        System.out.println(c+"个");
        for (int i = 0; i < img_float_array.length; ++i) {

            if(img_float_array[i]>=0.5){c++;bitmap.setPixel(i%bitmap.getWidth(),i/bitmap.getWidth(),Color.WHITE);}
            else{bitmap.setPixel(i%bitmap.getWidth(),i/bitmap.getWidth(),Color.BLACK);}
        }
        System.out.println(c);

        return bitmap;

//
//
//        for (int i = 0; i < intValues.length; ++i) {
//            final int val = intValues[i];
//            output[i * 3] = ((val >> 16) & 0xFF)/255f;
////            System.out.println(output[i * 3] );
//            output[i * 3 + 1] = ((val >> 8) & 0xFF)/255f;
//            output[i * 3 + 2] = (val & 0xFF)/255f;
//        }
//
//        return output;
    }


}

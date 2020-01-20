package com.systematic.netcore.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Dell on 12/11/2016.
 */

public class BitmapUtility {

    private static BitmapUtility bitmapUtil;

    public static BitmapUtility newInstance() {
        return new BitmapUtility();
    }

    public String convetStringFromBitmap(String filePath) {

        Bitmap bitmap = scaleDownBitMap(filePath);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] bytearray = outputStream.toByteArray();
        String imageString = Base64.encodeToString(bytearray, Base64.NO_WRAP);
        return imageString;
    }

    public Bitmap scaleDownBitMap(String filePath) {
        Bitmap reusltbitmap = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            //reusltbitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / 6), (int) (bitmap.getHeight() / 6), false);
            //reusltbitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
            reusltbitmap = bitmap;//no need to scale down bcoz image quality is already low
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reusltbitmap;
    }

    public Bitmap convertBitmapFromString(String imageString) {
        Bitmap bitmap = null;
        try {
            byte[] imageShow = Base64.decode(imageString, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(imageShow, 0, imageShow.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

package geishaproject.demonote.module.picture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import geishaproject.demonote.model.Data;

public class PhotoTool extends Activity{
    private static final String TAG = "PhotoTool";
    /**
     * 照片数组,放置照片的特殊字符（啊斌）,相机url,data数据,长，宽
     */

    private Uri photoUri;
    private Data data;
    /**
     * 构造函数
     */
    public PhotoTool(Data data){ this.data = data; }

    /**
     * 初始化拍照模块
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void initPhoto() {
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    /**
     * 通过地址获取图片
     */
    public Bitmap getBitmapForAdress(String adress){
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        Bitmap bitmap =  BitmapFactory.decodeFile(adress);
        return bitmap;
    }

    /**
     * 将大图片窗口化压缩
     */
    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }

    /**
     * 获取时间，用作命名
     */
    public String GetcurrentTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String currentTime = simpleDateFormat.format(date);
        return currentTime;
    }

    /**
     * 获取文件前缀
     */
    public String getAdress() {
        String name = GetcurrentTime()+".jpg" ;
        File dir = new File(Environment.getExternalStorageDirectory(), "NoteBlocks");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dir1 = new File(dir, "picture");
        if (!dir1.exists()) {
            dir1.mkdirs();
        }
        File pictureFile = new File(dir1, name);
        String adress=pictureFile.toString();
        return adress;
    }
    /**
     *  保存图片
     */
    public void saveImg(Bitmap bitmap,Context context) {
        //打开输入流
        try {
            //图片文件以当前时间命名，路径为pictureFile.getAbsolutePath()
            File pictureFile = new File(getAdress());      //dir1, name
            if (!pictureFile.exists()) {
                try {
                    pictureFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedOutputStream bos = null;
                //拼接的路径重新存入data中
                Log.i("SaveImg", "file had" );
                bos = new BufferedOutputStream(new FileOutputStream(pictureFile));      //BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,bos);           //compress到输出outputStream
                Uri uri = Uri.fromFile(pictureFile);                                    //获得图片的uri
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片
                Log.i("SaveImg", "file ok" );
                bos.flush();
                bos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ;
    }

    /**
     * 获取图片的旋转角度。
     * 只能通过原始文件获取，如果已经进行过bitmap操作无法获取。
     */
    public static int getRotateDegree(String path) {
        int result = 0;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    result = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    result = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    result = 270;
                    break;
            }
        } catch (IOException ignore) {
            return 0;
        }
        return result;
    }

    /**
     * 获取缩放数值。
     * 取值请参考 {@link #evaluateWH(float, float, float, float)}
     */
    private static float getScale(float srcWidth, float srcHeight, float destWidth, float destHeight) {
        int evaluateWH = evaluateWH(srcWidth, srcHeight, destWidth, destHeight);
        switch (evaluateWH) {
            case 0:
                return destWidth / srcWidth;
            case 1:
                return destHeight / srcHeight;
            default:
                return 1f;
        }
    }

    /**
     * 评估使用宽或者高计算缩放比例
     * 以最大缩放比例为准，如宽比例为 0.5， 高比例为0.8，返回宽
     *
     * @return 0：宽， 1：高， -1：不缩放
     */
    private static int evaluateWH(float srcWidth, float srcHeight, float destWidth, float destHeight) {
        if (srcWidth < 1f || srcHeight < 1f || srcWidth < destWidth && srcHeight < destHeight) {
            return -1;
        }
        int result;
        if (destWidth > 0 && destHeight > 0) {
            result = destWidth / srcWidth < destHeight / srcHeight ? 0 : 1;
        } else if (destWidth > 0) {
            result = 0;
        } else if (destHeight > 0) {
            result = 1;
        } else {
            result = -1;
        }
        return result;
    }

    // 参考Hashmap::tableSizeFor，根据传入的缩放比例倍数，获取一个临近的2的次幂的数
    private static int inSampleSizeFor(int n) {
        int maxNum = 1 << 30;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        n = (n < 0) ? 1 : (n >= maxNum) ? maxNum : n + 1;
        return n >>> 1 == 0 ? n : n >>> 1;
    }

    /**
     * 加载缩放bitmap。
     * 根据期望宽高自动获取合适的缩放比例, 具体看{@link #evaluateWH(float, float, float, float)}
     *
     * @param path      图片路径
     * @param maxWidth  期望宽度
     * @param maxHeight 期望高度
     */
    public static Bitmap loadScaledBitmap(String path, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int srcHeight = options.outHeight;
        int srcWidth = options.outWidth;
        // decode失败
        if (srcHeight == -1 || srcWidth == -1) {
            return null;
        }

        // 当比例差距过大时，先通过inSampleSize加载bitmap降低内存消耗
        float scale = getScale(srcWidth, srcHeight, maxWidth, maxHeight);
        int evaluateWH = evaluateWH(srcWidth, srcHeight, maxWidth, maxHeight);
        //options.inSampleSize = inSampleSizeFor((int) (1 / scale));
        options.inJustDecodeBounds = false;
        if (evaluateWH == 0) {
            options.inScaled = true;
            options.inDensity = srcWidth;
            options.inTargetDensity = maxWidth * options.inSampleSize;
        } else if (evaluateWH == 1) {
            options.inScaled = true;
            options.inDensity = srcHeight;
            options.inTargetDensity = maxHeight * options.inSampleSize;
        } else {
            options.inScaled = false;
        }
        return BitmapFactory.decodeFile(path, options);
    }
}

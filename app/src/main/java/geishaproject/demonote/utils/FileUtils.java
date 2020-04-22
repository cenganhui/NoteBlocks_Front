package geishaproject.demonote.utils;

import android.content.Context;
import android.os.Environment;
import java.io.File;


//文件管理类
public class FileUtils {

    //获取文件存放根路径
    public static File getAppDir(Context context) {
        String dirPath = "";
        //SD卡是否存在
        boolean isSdCardExists = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        boolean isRootDirExists = Environment.getExternalStorageDirectory().exists();
        if (isSdCardExists && isRootDirExists) {
            dirPath = String.format("%s/%s/", Environment.getExternalStorageDirectory().getAbsolutePath(), Constant.FilePath.ROOT_PATH);
        } else {
            dirPath = String.format("%s/%s/", context.getApplicationContext().getFilesDir().getAbsolutePath(), Constant.FilePath.ROOT_PATH);
        }

        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        return appDir;
    }

    //获取录音存放路径
    public static File getAppRecordDir(Context context) {
        File appDir = getAppDir(context);
        File recordDir = new File(appDir.getAbsolutePath(), Constant.FilePath.RECORD_DIR);
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir;
    }

    /**
     * 根据文件路径删除对应文件
     * @param filePath 文件路径
     */
    public static void deleteSingleFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }

    public static String checkZipFile(String zipFilePath){
        int length = zipFilePath.length();
        String ZipFileName = zipFilePath.substring(length-4,length);
        return ZipFileName;
    }

}
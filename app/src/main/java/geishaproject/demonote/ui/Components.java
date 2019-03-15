package geishaproject.demonote.ui;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.Random;

import geishaproject.demonote.model.Data;
import geishaproject.demonote.module.audio.Record;
import geishaproject.demonote.module.audio.manager.AudioRecordButton;
import geishaproject.demonote.module.permission.PermissionHelper;
import geishaproject.demonote.module.picture.PhotoTool;

public class Components {
    public static EditText ed_title;    //标题和内容文本框
    public static EditText ed_content;
    public static FloatingActionButton floatingActionButton;  //右下角按钮
    public static Data data;
    public static PhotoTool mPhotoTool;       //图像工具类
    public static Uri photoUri;                   //相机url

    /*  相片部分  */
    public static int REQUSET_CODE = 1;//请求码，判断是哪个回传的请求
    public static int mIndex = 0;

    /*  录音部分  */
    public static  Button StartRecord,StopRecord,PlayRecord;
    public static  MediaRecorder recorder;
    public static MediaPlayer player;
    public static AudioRecordButton mEmTvBtn;
    public static Record mRecords;
    public static PermissionHelper mHelper; //录音权限

    /*  闹钟部分  */
    public static Random rand = new Random();
    public static int times = rand.nextInt(10000);//闹钟唯一标识

}

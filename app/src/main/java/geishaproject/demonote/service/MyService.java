package geishaproject.demonote.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import geishaproject.demonote.R;
import geishaproject.demonote.activity.MainActivity;

import static android.app.PendingIntent.getActivity;

public class MyService extends Service {

    private static final String TAG = "MyService";
    private String mContentTitle;

    public MyService() {
    }



    @Override
    public void onCreate() {
        super.onCreate();

        Notification.Builder builder = new Notification.Builder
                (this.getApplicationContext()); //获取一个Notification构造器
    Intent nfIntent = new Intent(this, MainActivity.class);

        String CHANNEL_ONE_ID = "com.primedu.cn";
        String CHANNEL_ONE_NAME = "Channel One";

    builder.setContentIntent(PendingIntent.
                    getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.icon_alarmclock)) // 设置下拉列表中的图标(大图标)
        .setContentTitle("NoteBlocks") // 设置下拉列表里的标题
        .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
        .setContentText("闹钟") // 设置上下文内容
        .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }

    Notification notification = builder.build(); // 获取构建好的Notification
    notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(1 , notification);
        Log.d(TAG,"已设置service");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("Kathy", "onStartCommand - startId = " + startId + ", Thread ID = " + Thread.currentThread().getId());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
//        Log.i("Kathy", "onDestroy - Thread ID = " + Thread.currentThread().getId());
        stopForeground(true);
        super.onDestroy();
    }

    public void setContentTitle(String contentTitle) {
        mContentTitle = contentTitle;
    }
}

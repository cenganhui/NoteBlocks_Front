
package geishaproject.demonote.activity;



import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;

import android.view.Menu;

import android.view.MenuItem;

import android.view.View;

import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;


import geishaproject.demonote.module.audio.AlarmReceiver;
import geishaproject.demonote.module.audio.Audio;
import geishaproject.demonote.module.richtext.RichText;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.utils.PublicContext;
import geishaproject.demonote.R;
import geishaproject.demonote.module.audio.Record;
import geishaproject.demonote.module.audio.manager.AudioRecordButton;
import geishaproject.demonote.module.audio.manager.MediaManager;
import geishaproject.demonote.model.Data;

import geishaproject.demonote.dao.DataDao;
import geishaproject.demonote.module.picture.PhotoTool;
import geishaproject.demonote.module.permission.PermissionHelper;

import static android.app.AlertDialog.THEME_HOLO_LIGHT;


public class NewNote extends AppCompatActivity implements ViewTreeObserver.OnPreDrawListener {

    private static final String TAG = "NewNote";//Log调试

    /**
     * 活动入口
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        init();              //活动初始化
        addClickListenenr(); //添加活动的所有监听事件
    }

    /**
     * 活动相关初始化，在这里加入模块初始化
     */
    private void init(){
        Components.mHelper = new PermissionHelper(this);//授权处理
        getAllViewById();   //获取控件
        Audio.initAudio(); //初始化录音功能模块
        PhotoTool.initPhoto(); //初始化拍照功能模块
        initDataModel(); //初始化数据模型
        initEditText();  //初始化文本编辑框
        RichText.initifphotohave(); //初始化富文本显示
        Log.d("initFinish","width:"+Components.ed_content.getWidth()+",text: "+Components.ed_content.getText());
    }

    /**
     * 获取控件函数，在此完成所有的findViewById
     */
    private void getAllViewById(){
        Components.floatingActionButton = (FloatingActionButton) findViewById(R.id.finish); //实例化右下角按钮控件

        Components.ed_title = (EditText) findViewById(R.id.title);    //实例化文字编辑框
        Components.ed_content = (EditText) findViewById(R.id.content);

        Components.PlayRecord = (Button) findViewById(R.id.PlayBtn);  //播放录音按钮
        Components.mEmTvBtn = (AudioRecordButton) findViewById(R.id.em_tv_btn);
    }

    /**
     * 添加活动的所有监听事件，在这里绑定点击事件函数
     */
    private void addClickListenenr(){
        Components.floatingActionButton.setOnClickListener(new View.OnClickListener() {  //为悬浮按钮设置监听事件
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Audio.addAudioListener(); //添加录音点击事件
    }

    /**
     * 初始化数据模型
     */
    public void initDataModel(){
        Intent intent = this.getIntent();  //获取上一个活动传来的intent
        int idsFlag = intent.getIntExtra("ids", 0); //根据上一个活动传过来的intent中的数据判断新建与修改
        if (idsFlag != 0) {    //根据data的ids判断是新建还是读写，如果是读写，则显示对应数据
            Components.data = DataDao.GetDataByIds(idsFlag);
            Components.mPhotoTool = new PhotoTool(Components.data);
        }else{                       //如果是新建，则创建一个新的数据模型
            Components.data=new Data(0,"","","","","");
            Components.mPhotoTool = new PhotoTool(Components.data);
        }
    }

    /**
     * 初始化文本编辑框
     */
    private void initEditText(){
        Components.ed_title.setText(Components.data.getTitle());  //获取对应的值
        Components.mPhotoTool.doclear();
        Components.mPhotoTool.readyAdress();
    }

    /**
     * 重写onPause方法，在退出界面时自动终止语音播放
     */
    @Override
    protected void onPause() {
        MediaManager.release();//保证在退出该页面时，终止语音播放
        super.onPause();
    }

    /**
     *  相机拍完照之后的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent photoIntent) {
        super.onActivityResult(requestCode, resultCode, photoIntent);
        if (requestCode == Components.REQUSET_CODE) {
            if (resultCode == Activity.RESULT_OK ) {    //&& data != null 旧版条件，data缩略图
                //说明成功返回
                Uri uri = null;
                if (photoIntent != null && photoIntent.getData() != null) {
                    uri = photoIntent.getData();
                }
                if (uri == null) {
                    if (Components.photoUri != null) {
                        uri = Components.photoUri;
                    }
                }
                //图片的Uri转Bitmap
                Bitmap result = null;
                try {
                    result = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    //将图片一次性切好
                    Bitmap imgBitmap = PhotoTool.imageScale(result,Components.ed_content.getWidth(),Math.round(result.getHeight()*((float) Components.ed_content.getWidth()/result.getWidth())));
                    //保存图片
                    Components.mPhotoTool.saveImg(imgBitmap,PublicContext.getContext());
                    SpannableString spannableString = RichText.GetSpannableString(imgBitmap,Components.mPhotoTool.GetBitmapNmae(Components.mPhotoTool.BitmapAdressSize()-1));
                    Components.ed_content.append(spannableString);
                }
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //说明取消或失败了
                Toast.makeText(PublicContext.getContext(),"您取消了拍照！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 添加闹钟函数
     */
    private void addclock() {
        Components.times++;    //唯一标识改变
        //设置日期数据
        final Calendar calendar = Calendar.getInstance() ;//取得Calender对象
        calendar.setTimeInMillis(System.currentTimeMillis());
        //临时变量
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        //获取现在时间，单位;毫秒
        final long   nowTime  = calendar.getTimeInMillis();//这是当前的时间
        //用户选择的日期数据
        final Calendar hh = Calendar.getInstance();

        //年月日选择工具
        DatePickerDialog datePickerDialog = new DatePickerDialog(PublicContext.getContext(),THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                //将选择数值传给日期数据 hh
                hh.set(Calendar.YEAR,year);
                hh.set(Calendar.MONTH,month);
                hh.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                //小时，分钟选择工具
                TimePickerDialog dialog = new TimePickerDialog(PublicContext.getContext(),THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        hh.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        hh.set(Calendar.MINUTE, minute);

                        String date = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm").format(new java.util.Date(hh.getTimeInMillis()));

                        //↑已获取完用户输入

                        //创建AlarmManager提醒器
                        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);//获取AlarmManager实例
                        //建立广播对象
                        Intent intent = new Intent(PublicContext.getContext(),AlarmReceiver.class);
                        //传送标题，在提醒时显示
                        intent.putExtra("title","标题："+Components.ed_title.getText().toString());
                        //Log.d(TAG,data.getTitle() + ed_title.getText().toString()); 调试
                        //传送事件
                        PendingIntent pi = null;
                        if(Components.data.getIds()==0){
                            pi = PendingIntent.getBroadcast(PublicContext.getContext(), DataDao.GetMaxIds(), intent, 0);
                            System.out.print( DataDao.GetMaxIds());
                        }else {
                            pi = PendingIntent.getBroadcast(PublicContext.getContext(), Components.data.getIds(), intent, 0);
                            Log.d(TAG,""+Components.data.getIds());
                        }

                        //判断时间是否是未来
                        long a = hh.getTimeInMillis()-nowTime;
                        if (a>0){
                            Toast.makeText(getApplicationContext(),"成功设置闹钟提醒",Toast.LENGTH_SHORT).show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+a, pi);
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarm.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+a, pi);//用户设置的时间hh.getTimeInMillis()
                                //Toast.makeText(getApplicationContext(),"测试",Toast.LENGTH_SHORT).show(); 高版本
                            } else
                                alarm.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+a, pi);//开启提醒,立即测试：System.currentTimeMillis()
                        } else
                            Toast.makeText(getApplicationContext(),"失败，提醒时间要在将来哟~",Toast.LENGTH_SHORT).show();

                    }
                },hour,minute,true);
                dialog.show();

            }
        },mYear,mMonth,mDay);
        //选择窗口显示
        datePickerDialog.show();
    }

    /**
     * 重写返回建方法，如果是属于新建则插入数据表并返回主页面，如果是修改，修改表中数据并返回主页面
     */
    @Override
    public void onBackPressed() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd   HH:mm");//编辑便签的时间，格式化
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);

        //先将数据库内setPicturePath置空，在saveimg中才将正确的留下
        Components.data.setPicturePath("");
        Components.data.setAudioPath("");
        Components.data.setTimes(time);    //给当前data更新数据,如果有录音和拍照数据，应该在对应的过程中调用data.setXXX
        Components.data.setTitle(Components.ed_title.getText().toString());
        Components.data.setContent(Components.ed_content.getText().toString());

        //才将图片保存进入数据库
        PhotoTool.check();
        Components.mPhotoTool.showwwww();
        if(Components.data.getIds()!=0){ //根据data修改数据库
            DataDao.ChangeData(Components.data);
            Intent intent=new Intent(NewNote.this,MainActivity.class);
            startActivity(intent);
            NewNote.this.finish();
        } else{  //根据data新建数据
            DataDao.AddNewData(Components.data);
            Intent intent=new Intent(NewNote.this,MainActivity.class);
            startActivity(intent);
            NewNote.this.finish();
        }
    }

    /**
     * 右上角菜单选项创建
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_lo,menu);
        return true;
    }

    /**
     * 响应右上角菜单的点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //点击分享
            case R.id.new_share :
                shareNote();
                break;

            //点击添加图片
            case R.id.add_Photo:
                addPhoto();
                break;

            //点击添加闹钟
            case R.id.add_clock:
                addclock();
                break;

            //默认点击事件
            default:
                break;
        }
        return false;
    }

    /**
     * 点击右上角菜单时调用拍照模块
     */
    private void addPhoto(){
        /*  设置intent  */
        Intent intentAddPhoto =new Intent();
        intentAddPhoto.setAction("android.media.action.IMAGE_CAPTURE");
        intentAddPhoto.addCategory("android.intent.category.DEFAULT");

        /*  拍照文件确认  */
        File pictureFile = new File(Components.mPhotoTool.getAdress());
        if (!pictureFile.exists()) {
            try {
                pictureFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*  设置intent  */
        Intent intents = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Components.photoUri = Uri.fromFile(new File(String.valueOf(pictureFile))); // 包装参数
        intents.putExtra(MediaStore.EXTRA_OUTPUT, Components.photoUri);

        /*  调用活动  */
        startActivityForResult(intents, Components.REQUSET_CODE);
    }

    /**
     * 点击右上角菜单时调用分享模块
     */
    private void shareNote(){
        /*  设置intent  */
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,//分享类型设置为文本型
                "标题："+Components.ed_title.getText().toString()+"    " +
                        "内容："+Components.ed_content.getText().toString());

        /*  调用活动  */
        startActivity(intent);
    }

    @Override
    public boolean onPreDraw() {
        return false;
    }

}

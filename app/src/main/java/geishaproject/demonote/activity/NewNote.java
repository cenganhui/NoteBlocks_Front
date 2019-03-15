
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
        Components.floatingActionButton = (FloatingActionButton) findViewById(R.id.finish); //实例化右下角按钮控件
        initAudio(); //初始化录音功能模块
        initPhoto(); //初始化拍照功能模块
        initDataModel(); //初始化数据模型
        initEditText();  //初始化文本编辑框
        Log.d("initFinish","width:"+Components.ed_content.getWidth()+",text: "+Components.ed_content.getText());
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
        addAudioListener(); //添加录音点击事件

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
        Components.ed_title = (EditText) findViewById(R.id.title);    //实例化文字编辑框
        Components.ed_content = (EditText) findViewById(R.id.content);

        Components.ed_title.setText(Components.data.getTitle());  //获取对应的值
        Components.mPhotoTool.doclear();
        Components.mPhotoTool.readyAdress();
        initifphotohave();
    }

    /**
     * 初始化有图片的文字/没图片处理在最下面的else
     */
    private void initifphotohave() {

        //用下面函数可以将控件宽高提前拿到
        Components.ed_content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                Components.ed_content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Components.mPhotoTool.addsize(Components.ed_content.getWidth(),Components.ed_content.getHeight());
                String world = Components.data.getContent();
                String now = world;         //还有的文本
                //全部文本
                int startindex=0;      //要替换图片的位置
                int endindex=0;        //要替换图片的位置
                int i=0;               //变量
                Bitmap bitmap= null;
                Log.d(TAG,"ed text ;:"+ Components.mPhotoTool.BitmapAdressSize());
                if (Components.mPhotoTool.BitmapAdressSize()>0){
                    for (i=0; i<Components.mPhotoTool.BitmapAdressSize();i++) {
                        //数据定义
                        Log.d(TAG, "要切的文本" + Components.mPhotoTool.GetBitmapNmae(i));
                        String show;        //要放上去的文本
                        //找到要替换的特殊字符位置
                        endindex = now.indexOf(Components.mPhotoTool.GetBitmapNmae(i));
                        //切割子文本
                        show = now.substring(0, endindex);
                        now = now.substring(endindex + Components.mPhotoTool.GetBitmapNmae(i).length());
                        //输出文本
                        Components.ed_content.append(show);
                        //输出图片，GetSpannableString 富文本操作
                        bitmap = Components.mPhotoTool.getBitmapForAdress(Components.mPhotoTool.GetBitmapNmae(i));
                        SpannableString spannableString = GetSpannableString(bitmap, Components.mPhotoTool.GetBitmapNmae(i));
                        Components.ed_content.append(spannableString);
                    }
                    if(i == Components.mPhotoTool.BitmapAdressSize())
                        Components.ed_content.append(now);
                }
                else
                    Components.ed_content.setText(Components.data.getContent());

            }
        });

    }


    /*  录音  */
    /**
     * 初始化录音模块
     */
    private void initAudio() {
        Components.player = new MediaPlayer();   //实例化录音控件
        Components.PlayRecord = (Button) findViewById(R.id.PlayBtn);  //播放录音按钮
        Components.mEmTvBtn = (AudioRecordButton) findViewById(R.id.em_tv_btn);
    }

    /**
     * 添加录音点击事件监听
     */
    private void addAudioListener() {
        Components.PlayRecord.setOnClickListener(new View.OnClickListener() {        //播放录音点击事件
            @Override
            public void onClick(View v) {
                PlayR();
            }
        });

        Components.mEmTvBtn.setHasRecordPromission(false);
        //授权处理
        Components.mHelper = new PermissionHelper(this);

        Components.mHelper.requestPermissions("请授予[录音]、[读写]权限，否则无法录音",
                new PermissionHelper.PermissionListener() {
                    @Override
                    public void doAfterGrand(String... permission) {
                        Components.mEmTvBtn.setHasRecordPromission(true);

                        Components.mEmTvBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {
                            @Override
                            public void onFinished(float seconds, String filePath) {
                                Record recordModel = new Record();
                                recordModel.setSecond((int) seconds <= 0 ? 1 : (int) seconds);
                                recordModel.setPath(filePath);
                                recordModel.setPlayed(false);
                                Components.mRecords = recordModel;
                                String newAudioPath =Components.data.getAudioPath()+Components.mRecords.getPath()+"?";
                                //拼接的路径重新存入data中
                                Components.data.setAudioPath(newAudioPath);
                                Components.data.cutAudioPathArr();
                                Toast.makeText(NewNote.this, newAudioPath, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(NewNote.this, "录音保存成功！时长："+mRecords.getSecond()+"s", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void doAfterDenied(String... permission) {
                        Components.mEmTvBtn.setHasRecordPromission(false);
                        Toast.makeText(NewNote.this, "请授权,否则无法录音", Toast.LENGTH_SHORT).show();
                    }
                }, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    /**
     * 终止语音播放
     */
    @Override
    protected void onPause() {
        MediaManager.release();//保证在退出该页面时，终止语音播放
        super.onPause();
    }

    /**
     * 播放录音
     */
    private void PlayR () {
        if(!Components.data.getAudioPath().equals("")) {
            if (Components.player != null) {
                Components.player.reset();
                try {
                    Components.data.cutAudioPathArr();
                    Toast.makeText(NewNote.this, Components.data.getAudioPath(), Toast.LENGTH_SHORT).show();
                    int i = Components.data.getAudioPathArr().size();
                    Log.d("***size","*/*/*/*size"+i);
                    Components.player.setDataSource(Components.data.getAudioPathArr().get(i-1)); //获取录音文件
                    Components.player.prepare();
                    Components.player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        else{
            Toast.makeText(NewNote.this, "还未曾录音", Toast.LENGTH_SHORT).show();
        }
    }



    /*  拍照部分  */
    /**
     * 初始化拍照模块
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initPhoto() {
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    /**
     *  相机拍完照之后的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Components.REQUSET_CODE) {
            if (resultCode == Activity.RESULT_OK ) {    //&& data != null 旧版条件，data缩略图
                //说明成功返回
                Uri uri = null;
                if (data != null && data.getData() != null) {
                    uri = data.getData();
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
                    Components.mPhotoTool.saveImg(imgBitmap,this);
                    SpannableString spannableString = GetSpannableString(imgBitmap,Components.mPhotoTool.GetBitmapNmae(Components.mPhotoTool.BitmapAdressSize()-1));
                    Components.ed_content.append(spannableString);
                }
            }else if (resultCode == Activity.RESULT_CANCELED) {
                //说明取消或失败了
                Toast.makeText(this,"您取消了拍照！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 富文本自定义函数
     */
    public SpannableString GetSpannableString(Bitmap bitmap,String specialchar){
        SpannableString spannableString = new SpannableString(specialchar);
        Log.d(TAG,"diaonma"+Components.ed_content.getWidth()+"////"+Components.ed_content.getHeight());
       /* BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;*/
        Log.d(TAG,"大小为（m）："+bitmap.getByteCount() / 1024 / 1024+"宽度为" + bitmap.getWidth() + "高度为" + bitmap.getHeight());
        ImageSpan imgSpan = new ImageSpan(PublicContext.getContext(),bitmap, DynamicDrawableSpan.ALIGN_BASELINE);
        spannableString.setSpan(imgSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /*  闹钟部分  */

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
        DatePickerDialog datePickerDialog = new DatePickerDialog(NewNote.this,THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                //将选择数值传给日期数据 hh
                hh.set(Calendar.YEAR,year);
                hh.set(Calendar.MONTH,month);
                hh.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                //小时，分钟选择工具
                TimePickerDialog dialog = new TimePickerDialog(NewNote.this,THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        hh.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        hh.set(Calendar.MINUTE, minute);

                        String date = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm").format(new java.util.Date(hh.getTimeInMillis()));

                        //↑已获取完用户输入

                        //创建AlarmManager提醒器
                        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);//获取AlarmManager实例
                        //建立广播对象
                        Intent intent = new Intent(NewNote.this,AlarmReceiver.class);
                        //传送标题，在提醒时显示
                        intent.putExtra("title","标题："+Components.ed_title.getText().toString());
                        //Log.d(TAG,data.getTitle() + ed_title.getText().toString()); 调试
                        //传送事件
                        PendingIntent pi = null;
                        if(Components.data.getIds()==0){
                            pi = PendingIntent.getBroadcast(PublicContext.getContext(), DataDao.GetMaxIds(), intent, 0);
                            System.out.print( DataDao.GetMaxIds());
                        }else {
                            pi = PendingIntent.getBroadcast(NewNote.this, Components.data.getIds(), intent, 0);
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

    /*  系统界面点击功能等  */
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
        Components.data.setTimes(time);    //给当前data更新数据,如果有录音和拍照数据，应该在对应的过程中调用data.setXXX
        Components.data.setTitle(Components.ed_title.getText().toString());
        Components.data.setContent(Components.ed_content.getText().toString());
        //才将图片保存进入数据库
        check();
        //
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
     * 才将图片保存进入数据库
     */
    private void check() {
        String world = Components.data.getContent();
        String now = world;         //还有的文本
        int startindex=0;      //要替换图片的位置
        int endindex=0;        //要替换图片的位置
        int i=0;               //变量
        int temp = 0;
        Log.d(TAG,""+Components.mPhotoTool.BitmapAdressSize());
        if (Components.mPhotoTool.BitmapAdressSize()>0){
            for (i=0; i<Components.mPhotoTool.BitmapAdressSize();i++) {
                //数据定义
                String show;        //要放上去的文本
                //找到要替换的特殊字符位置
                Log.d(TAG,"dizhi:"+Components.mPhotoTool.GetBitmapNmae(i));
                endindex = now.indexOf(Components.mPhotoTool.GetBitmapNmae(i));
                if(endindex == -1){
                    //删除实际文件
                    Components.mPhotoTool.deleteBitmapForAdress(Components.mPhotoTool.GetBitmapNmae(i-temp));
                    //删除变量内容
                    Components.mPhotoTool.delete(i-temp);
                    temp++;
                }else{
                    //切割子文本
                    show = now.substring(0, endindex);
                    now = now.substring(endindex + Components.mPhotoTool.GetBitmapNmae(i).length());
                    //输出文本
                }
            }
        }
        //当检查完后才真正保存
        Components.mPhotoTool.saveToData();
    }


    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_lo,menu);
        return true;
    }


    @Override
    public boolean onPreDraw() {
        return false;
    }


    /*  右上角菜单点击事件  */
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

    /**
     * 权限请求，直接把参数交给mHelper就行了
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Components.mHelper.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

package geishaproject.demonote.activity;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import geishaproject.demonote.dao.UserDao;
import geishaproject.demonote.module.audio.AlarmReceiver;
import geishaproject.demonote.module.audio.Audio;
import geishaproject.demonote.module.http.HttpSender;
import geishaproject.demonote.module.http.ResultFactory;
import geishaproject.demonote.module.http.UrlFactory;
import geishaproject.demonote.module.richtext.RichText;
import geishaproject.demonote.service.MyService;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.module.http.HttpCallbackListener;
import geishaproject.demonote.utils.PublicContext;
import geishaproject.demonote.R;
import geishaproject.demonote.module.audio.manager.AudioRecordButton;
import geishaproject.demonote.module.audio.manager.MediaManager;
import geishaproject.demonote.model.Data;
import geishaproject.demonote.dao.DataDao;
import geishaproject.demonote.module.picture.PhotoTool;
import geishaproject.demonote.module.permission.PermissionHelper;
import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class NewNote extends AppCompatActivity implements ViewTreeObserver.OnPreDrawListener {

    private static final String TAG = "NewNote";//Log调试
    public static final int UPDATE_TEXT = 1;//NewNote类handler更新数据标识
    public static final int MAKE_TOAST = 2; //NewNote类handler弹窗标识
    public static final int SHARE_NOTE = 103; //分享接口
    /**
     * 新增一个Handler对象处理子线程消息传递
     */
    @SuppressLint("HandlerLeak")
    public  Handler note_handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_TEXT:
                    break;
                case MAKE_TOAST:
                    Toast.makeText(PublicContext.getContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    /**
     * 活动入口
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
        Components.mEmTvBtn = (AudioRecordButton) findViewById(R.id.em_tv_btn);
    }

    /**
     * 添加活动的所有监听事件，在这里绑定点击事件函数
     */
    private void addClickListenenr(){
        Components.floatingActionButton.setOnClickListener(new View.OnClickListener() {  //为悬浮按钮设置监听事件
            @Override
            public void onClick(View v) {
                addPhoto();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode){
            case SHARE_NOTE:
                Log.d("onActivityResult",intent.getStringExtra("data_return"));
                if(intent.getStringExtra("data_return").equals("false")){
                    Toast.makeText(PublicContext.getContext(), "验证失败", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PublicContext.getContext(), "验证成功，开始分享", Toast.LENGTH_SHORT).show();
                    UserDao.setU_user(intent.getStringExtra("data_return"));
                    shareNoteStart();
                }
                break;
            default:
                if (requestCode == Components.REQUSET_CODE) {
                    if (resultCode == Activity.RESULT_OK ) {    //&& data != null 旧版条件，data缩略图
                        //说明成功返回
                        Uri uri = null;
                        if (intent != null && intent.getData() != null) {
                            uri = intent.getData();
                        }
                        if (uri == null) {
                            if (Components.photoUri != null) {
                                uri = Components.photoUri;
                            }
                        }
                        //图片的Uri转Bitmap
                        Bitmap result = null;
                        Bitmap outBitmap = null;
                        try {
                            result = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            // 获取图片旋转角度，旋转图片
                            int degree = Components.mPhotoTool.getRotateDegree(uri.toString().substring(7));//file.getAbsolutePath()
                            Matrix matrix = new Matrix();
                            matrix.postRotate(degree);
                            outBitmap = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, false);
                            //result = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            Log.e(TAG,"Uri:" +uri);
                            Log.e(TAG,"tostring:" + uri.toString());
                            //删除高清版本
                            Components.data.deleteForAdress(uri.toString().substring(7));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (result != null) {
                            DisplayMetrics dm = getResources().getDisplayMetrics();
                            float screenWidth = dm.widthPixels;
                            float screenHeight = dm.heightPixels;
                            Bitmap imgBitmap=null; //将要拿去富文本处理的裁剪图
                            //将图片一次性切好
                            if(Float.toString(getScreenScale()).substring(0,3).equals(Float.toString((float)2/(float)1 ).substring(0,3))||Components.ed_content.getWidth()==1032){
                                imgBitmap = PhotoTool.imageScale(outBitmap,Components.ed_content.getWidth()-40,Math.round(outBitmap.getHeight()*((float) (Components.ed_content.getWidth()-40)/outBitmap.getWidth())));
                            }else if ( Float.toString(getScreenScale()).substring(0,3).equals(Float.toString((float)16/(float)9 ).substring(0,3)))
                                imgBitmap = PhotoTool.imageScale(outBitmap,Components.ed_content.getWidth(),Math.round(outBitmap.getHeight()*((float) (Components.ed_content.getWidth())/outBitmap.getWidth())));
                            if (imgBitmap==null){
                                Toast.makeText(PublicContext.getContext(),"暂不适用于该分辨率的手机",Toast.LENGTH_SHORT);
                                Log.e(TAG,"分辨率比较失败，未能获得图片" );
                                Log.e(TAG,"hehe " + (float)16 /9);
                            }
                            //保存图片
                            Components.mPhotoTool.saveImg(imgBitmap,PublicContext.getContext());
                            int i = Components.data.addPicturePathArr(Components.mPhotoTool.getAdress());
                            //创建富文本，并显示
                            SpannableString spannableString = RichText.GetSpannableString(imgBitmap,Components.data.getPicturePathArr(i));
                            Components.ed_content.append(spannableString);
                        }
                    }else if (resultCode == Activity.RESULT_CANCELED) {
                        //说明取消或失败了
                        Toast.makeText(PublicContext.getContext(),"您取消了拍照！",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

    /**
     * 获取分辨率
     */
    public float getScreenScale(){
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;
        Log.e(TAG,"手机分辨率：:" + screenHeight+" |  "+screenWidth);
        return screenHeight/screenWidth;
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
                            pi = PendingIntent.getBroadcast(NewNote.this, DataDao.GetMaxIds(), intent, 0);
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


                            Intent intentservice = new Intent(PublicContext.getContext() , MyService.class);
                            startService(intentservice);
                            Log.d(TAG,"已设置service");


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
            Log.d("content",Components.ed_content.getText().toString());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//"+"T"+"编辑便签的时间，格式化
            Date date = new Date(System.currentTimeMillis());
            String time = simpleDateFormat.format(date);
            //先将数据库内setPicturePath置空，在saveimg中才将正确的留下
            Components.data.setPicturePath("");
            Components.data.setAudioPath("");
            Components.data.setTimes(time);    //给当前data更新数据,如果有录音和拍照数据，应该在对应的过程中调用data.setXXX
            Components.data.setTitle(Components.ed_title.getText().toString());
            Components.data.setContent(Components.ed_content.getText().toString());
            //才将图片保存进入数据库
            Components.data.check();
            Components.data.showwwww();

            if(Components.data.getIds()!=0){
                //Ids标识为修改数据
                DataDao.ChangeData(Components.data);
                NewNote.this.finish();
            } else{
                //Ids标识为新建数据，根据data新建数据
                if(Components.data.getTitle().equals("")&&Components.data.getContent().equals("")){
                    //空内容时取消创建
                    note_handler.sendMessage(ResultFactory.getResultMessageBySet("您没有填写任何内容，自动取消创建"));
                    NewNote.this.finish();
                }else{
                    DataDao.AddNewData(Components.data);
                    NewNote.this.finish();
                }
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
        intents.putExtra("photoAdress",String.valueOf(pictureFile));
        /*  调用活动  */
        startActivityForResult(intents, Components.REQUSET_CODE);
    }

    /**
     * 点击右上角菜单时调用分享模块
     */
    private  void shareNote(){
        verifyUserAndStart(SHARE_NOTE);
    }

    /**
     * 分享功能开始函数
     */
    private  void shareNoteStart(){
        //向服务器发送分享请求
        Log.d("shareNote",Components.data.toShareJSON());
        //向服务器发送文件
        Data data =  Components.data; //获取data
        data.cutPicturePath();
        data.cutAudioPathArr();
        ArrayList<String> pictureArr = data.getPicturePathArr();
        ArrayList<String> audioArr = data.getAudioPathArr();

        //遍历上传图片
        for(int i=0;i<pictureArr.size();i++){
            File picFile = new File(pictureArr.get(i));
            HttpSender.uploadFile(UrlFactory.post_UploadUrl, picFile, new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                }
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        //遍历上传录音
        for(int j=0;j<audioArr.size();j++){
            File audFile = new File(audioArr.get(j));
            HttpSender.uploadFile(UrlFactory.post_UploadUrl, audFile, new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                }
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        HttpSender.sendPost(Components.data.toShareJSON(), UrlFactory.post_ShareUrl, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                //设置intent
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                //生成外链
                intent.putExtra(Intent.EXTRA_TEXT,//分享类型设置为文本型
                        UrlFactory.addressHead+"/"+"getShareNote?sn_id="+response.substring(response.indexOf(":")+1,response.length()));

                //调用分享接口
                startActivity(intent);

            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void verifyUserAndStart(int functionFlag){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivityForResult(intent,functionFlag);
    }
    @Override
    public boolean onPreDraw() {
        return false;
    }

}

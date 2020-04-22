
package geishaproject.demonote.activity;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import geishaproject.demonote.dao.UserDao;
import geishaproject.demonote.module.audio.AlarmReceiver;
import geishaproject.demonote.module.http.HttpSender;
import geishaproject.demonote.module.http.ResultFactory;
import geishaproject.demonote.module.http.UrlFactory;
import geishaproject.demonote.residemenu.ResideMenu;
import geishaproject.demonote.residemenu.ResideMenuItem;
import geishaproject.demonote.utils.DataJSON;
import geishaproject.demonote.utils.FileUtils;
import geishaproject.demonote.module.http.HttpCallbackListener;
import geishaproject.demonote.utils.PublicContext;
import geishaproject.demonote.R;
import geishaproject.demonote.model.Data;
import geishaproject.demonote.dao.DataDao;
import geishaproject.demonote.module.listview.MyAdapter;
import geishaproject.demonote.utils.Backup;
import geishaproject.demonote.utils.Constant;
import geishaproject.demonote.utils.Zip;
import static geishaproject.demonote.utils.Constant.copyFolder;

//version 1.0
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    ListView listView;
    FloatingActionButton floatingActionButton;
    LayoutInflater layoutInflater;
    ArrayList<Data> arrayList;
    //保存选择文件的路径
    String path;
    private TextView mAmTvBtn;
    private MyAdapter adapter;
    public static final int UPDATE_TEXT = 1;//MainActivity类handler更新数据标识
    public static final int MAKE_TOAST = 2; //MainActivity类handler弹窗标识
    public static final int BACKUP_NOTES = 101;//MainActivity类handler更新数据标识
    public static final int DOWNLOAD_FILE = 102; //MainActivity类handler弹窗标识

    //滑动菜单相关控件
    private ResideMenu resideMenu;      //滑动菜单
    private ResideMenuItem itemExport;  //导出控件
    private ResideMenuItem itemImport;  //导入控件
    private ResideMenuItem itemRegister;//注册控件
    private ResideMenuItem itemBackup;  //云备份控件
    private ResideMenuItem itemRecovery;//云恢复控件

    /**
     * 新增一个Handler对象处理子线程消息传递
     */
    @SuppressLint("HandlerLeak")
    private  Handler handler = new Handler(){
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMenu();
        listView = (ListView)findViewById(R.id.layout_listview);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.add_note);
        layoutInflater = getLayoutInflater();
        arrayList = DataDao.GetAllDatas();
        adapter = new MyAdapter(layoutInflater,arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {   //点击一下跳转到编辑页面（编辑页面与新建页面共用一个布局）

            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),NewNote.class);
                intent.putExtra("ids",arrayList.get(position).getIds());
                startActivity(intent);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {   //长按删除

            @Override

            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this) //弹出一个对话框
                        .setMessage("确定要删除此便签？")
                        .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("确定",new DialogInterface.OnClickListener(){

                            @Override

                            public void onClick(DialogInterface dialog, int which) {
                                //删除广播
                                daleteSever(position);
                                //删除便签对应的图片和音频文件
                                for(int i=0;i<arrayList.get(position).getAudioPathArr().size();i++){
                                    Data.deleteSingleFile(arrayList.get(position).getAudioPathArr().get(i));
                                }
                                for(int i=0;i<arrayList.get(position).getPicturePathArr().size();i++){
                                    Data.deleteSingleFile(arrayList.get(position).getPicturePathArr().get(i));
                                }
                                DataDao.DeleteDataByIds(arrayList.get(position).getIds());
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .create()
                        .show();
                return true;
            }

        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {   //点击悬浮按钮时，跳转到新建页面

            @Override

            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),NewNote.class);
                startActivity(intent);
            }

        });

    }

    private void daleteSever(int position) {
        //删除广播
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //建立广播对象
        Intent intent = new Intent(PublicContext.getContext(),AlarmReceiver.class);
        //传送事件
        PendingIntent pi = PendingIntent.getBroadcast(PublicContext.getContext(), arrayList.get(position).getIds(), intent, 0);
        Log.d(TAG,""+ arrayList.get(position).getIds());
        //删除
        am.cancel(pi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_lo,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_export:
                mExport();
                break;
            case R.id.menu_import:
                mImport();
                break;
            case R.id.menu_register:
                mRegister();
                break;
            case R.id.menu_backup:
                mBackup();
                break;
            case R.id.menu_recovery:
                mRecovery();
                break;
            default:
                break;
        }
        return  true;
    }

    /**
     * 云备份功能入口
     */
    public void mBackup(){
        verifyUserAndStart(BACKUP_NOTES);
    }
    /**
     * 云备份功能开始
     */
    public void mBackupStart(){
        Log.d("MainActivity","CloudBackup"+DataDao.GetAllDatas().toString());
        //获取ArrayList
        ArrayList<Data> backupList = DataDao.GetAllDatas();
        /* 通过ArrayList调用HTTP请求备份图片 */
        //外层循环ArrayList
        for(int i=0;i<backupList.size();i++){
            //内层循环Data图片数组
            backupList.get(i).cutPicturePath();//将存储的String转换为数组便于操作
            ArrayList<String> pictureList = backupList.get(i).getPicturePathArr(); //获取图片数组
            for(int j=0;j<pictureList.size();j++){
                File picFile = new File(pictureList.get(j));     //构建File参数
                HttpSender.uploadFile(UrlFactory.post_UploadUrl, picFile, new HttpCallbackListener() {
                    //回调函数
                    @Override
                    public void onFinish(String response) {
                        Log.d("MainActivity","CloudBackup:picture:"+ResultFactory.getResultStringByResponse(response));
                    }
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        handler.sendMessage(ResultFactory.getResultMessageBySet("成功上传图片"));
        /*通过ArrayList调用HTTP请求备份录音*/
        //外层循环ArrayList
        for(int i=0;i<backupList.size();i++){
            //内层循环Data录音数组
            backupList.get(i).cutAudioPathArr();//将存储的String转换为数组便于操作
            ArrayList<String> audioList = backupList.get(i).getAudioPathArr(); //获取录音数组
            for(int j=0;j<audioList.size();j++){
                File picFile = new File(audioList.get(j));     //构建File参数
                HttpSender.uploadFile(UrlFactory.post_UploadUrl, picFile, new HttpCallbackListener() {
                    //回调函数
                    @Override
                    public void onFinish(String response) {
                        Log.d("MainActivity","CloudBackup:audio:"+ResultFactory.getResultStringByResponse(response));
                    }
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        }
        handler.sendMessage(ResultFactory.getResultMessageBySet("成功上传录音"));
        //通过ArrayList获取JSON
        String datasJSON =  DataJSON.getJSONByArr(backupList);
        //将JSON发送到服务器进行备份
        HttpSender.sendPost(datasJSON, UrlFactory.post_BackupUrl, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                handler.sendMessage(ResultFactory.getResultMessageByResponse(response));
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 云恢复功能入口
     */
    public void mRecovery(){
        verifyUserAndStart(DOWNLOAD_FILE);
    }
    /**
     * 云恢复功能开始
     */
    public void mRecoveryStart(){
        //向服务器发送请求获取JSON
        Map map= new HashMap<>();//构建参数map
        map.put("u_name",UserDao.getU_user());
        String fullUrl = UrlFactory.jointGetUrlAndMap(UrlFactory.get_CloudRecoveryUrl,map);
        HttpSender.sendGet(fullUrl, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.d("MainActivity","mRecoveryStart:start"+response);
                handler.sendMessage(ResultFactory.getResultMessageBySet("成功下载数据"));
                //将JSON构建成ArrayList
                ArrayList<Data> cloudArr= DataJSON.getArrByJSON(response);
                Log.d("MainActivity","mRecoveryStart:"+cloudArr.toString());
                //根据ArrayList更新数据
                for(int i=0;i<cloudArr.size();i++){
                    Log.d("MainActivity","verify:"+cloudArr.get(i).toJSON());
                }
                /*据ArrayList下载图片和音频资源*/
                String filePath=Environment.getExternalStorageDirectory().getPath();
                String picturePath = filePath+"/NoteBlocks/picture/";
                String audioPath = filePath+"/NoteBlocks/record/";
                //通过ArrayList调用HTTP请求下载图片
                //外层循环ArrayList
                for(int i=0;i<cloudArr.size();i++){
                    //内层循环Data图片数组
                    cloudArr.get(i).cutPicturePath();//将存储的String转换为数组便于操作
                    ArrayList<String> pictureList = cloudArr.get(i).getPicturePathArr(); //获取图片数组
                    for(int j=0;j<pictureList.size();j++){
                        String fileName = pictureList.get(j).replace(picturePath,"");
                        Map fileNameMap = new HashMap();
                        fileNameMap.put("filename",fileName);
                        String fullUrl = UrlFactory.jointGetUrlAndMap(UrlFactory.get_RecoveryUrl,fileNameMap);
                        HttpSender.downFile(fullUrl,picturePath,fileName,new HttpCallbackListener() {
                            //回调函数
                            @Override
                            public void onFinish(String response) {
                                Log.d("MainActivity","CloudRecovery:picture:下载成功");
                            }
                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                handler.sendMessage(ResultFactory.getResultMessageBySet("成功下载图片"));
                //通过ArrayList调用HTTP请求下载录音
                //外层循环ArrayList
                for(int i=0;i<cloudArr.size();i++){
                    //内层循环Data录音数组
                    cloudArr.get(i).cutAudioPathArr();//将存储的String转换为数组便于操作
                    ArrayList<String> audioList = cloudArr.get(i).getAudioPathArr(); //获取录音数组
                    for(int j=0;j<audioList.size();j++){
                        Map audioNameMap = new HashMap();
                        String audioName = audioList.get(j).replace(audioPath,"");
                        audioNameMap.put("filename",audioName);
                        String fullUrl = UrlFactory.jointGetUrlAndMap(UrlFactory.get_RecoveryUrl,audioNameMap);
                        HttpSender.downFile(fullUrl, audioPath,audioName, new HttpCallbackListener() {
                            //回调函数
                            @Override
                            public void onFinish(String response) {
                                Log.d("MainActivity","CloudRecovery:audio:成功");
                            }
                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                handler.sendMessage(ResultFactory.getResultMessageBySet("成功下载音频"));
                //根据ArrayList更新数据
                for(int i=0;i<cloudArr.size();i++){
                    Log.d("MainActivity","addNewData:"+cloudArr.get(i).toJSON());
                    DataDao.AddNewData(cloudArr.get(i));
                }
                //更新视图
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(Exception e) {

            }
        });
        //根据ArrayList更新数据

    }
    public void mRecoveryStart1(){
        Map fileNameMap = new HashMap();
        fileNameMap.put("filename",UserDao.getU_user()+".zip");
        handler.sendMessage(ResultFactory.getResultMessageBySet("正在为您下载资源，请稍后"));
        HttpSender.downFile(UrlFactory.jointGetUrlAndMap(UrlFactory.get_RecoveryUrl,fileNameMap), Environment.getExternalStorageDirectory().getPath(), UserDao.getU_user() + ".zip", new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                handler.sendMessage(ResultFactory.getResultMessageBySet("下载资源完成，开始读取"));
                try {
                    //清空本地资源
                    final File oldDir = new File(Environment.getExternalStorageDirectory(), "NoteBlocks");
                    if (!oldDir.exists()) {
                        oldDir.mkdirs();
                    }
                    DataDao.DeleteDataAllData();        //删除所有便签
                    Constant.deleteFile(oldDir);        //删除便签保存的本地文件（图片和音频）
                    //创建NoteBlocks文件夹
                    File dir = new File(Environment.getExternalStorageDirectory(), "NoteBlocks");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    //解压 用户名.zip到NoteBlocks文件夹中
                    Zip.unzip(Environment.getExternalStorageDirectory().getPath()+"/"+ UserDao.getU_user()+".zip", dir.getAbsolutePath());
                    //读取NoteBlocks.json文件中的便签数据
                    Backup.readJsonFile(dir.getAbsolutePath()+"/NoteBlocks.json");
                    handler.sendMessage(ResultFactory.getResultMessageBySet("读取资源成功，为您刷新界面"));
                    //刷新应用界面
                    Intent intent=new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * 跳转注册页面
     */
    public void mRegister(){
        Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }
    /**
     * 导出便签包操作
     */
    public void mExport(){
        //获得文件存放的NoteBlocks文件夹路径
        final File oldDir = new File(Environment.getExternalStorageDirectory(), "NoteBlocks");
        if (!oldDir.exists()) {
            oldDir.mkdirs();
        }
        //创建导出NoteBlocksPack文件夹
        final File newDir = new File(Environment.getExternalStorageDirectory(), "NoteBlocksPack");
        if (!newDir.exists()) {
            newDir.mkdirs();
        }
        //将NoteBlocks下的所有文件复制到NoteBlocksPack中
        copyFolder(oldDir.getAbsolutePath(),newDir.getAbsolutePath());
        //生成NoteBlocks.json文件并保存在NoteBlocksPack文件夹中
        Backup.createJsonFile(newDir);
        handler.sendMessage(ResultFactory.getResultMessageBySet("读取资源成功，正在压缩"));
        //创建线程，对NoteBlocksPack进行压缩
        Thread th  = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    //压缩，生成NoteBlocks.zip
                    Zip.zip(newDir.getAbsolutePath(), Environment.getExternalStorageDirectory().getPath()+"/NoteBlocksPack.zip");
                    //删除NoteBlocksPack文件夹及其里面的所有文件
                    Constant.deleteFile(newDir);
                    //发送toast提示
                    handler.sendMessage(ResultFactory.getResultMessageBySet("压缩成功，文件保存在手机储存根目录："+"NoteBlocksPack.zip"));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        //启动线程
        th.start();

    }


    /**
     * 导入便签包操作
     * 如果导入便签包，则会删除当前所有便签（包括音频图片文件、DataDao及数据库中的所有数据）
     * 再将便签包里的便签添加入数据库
     */
    public void mImport(){
        selectFile();
    }

    /**
     * 调用文件管理器，进行选择文件
     */
    private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);
    }

    /**
     * 获得选择文件的路径
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case BACKUP_NOTES:
                if(data.getStringExtra("data_return").equals("false")) {
                    Toast.makeText(PublicContext.getContext(), "验证失败", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PublicContext.getContext(), "验证成功，开始备份", Toast.LENGTH_SHORT).show();
                    UserDao.setU_user(data.getStringExtra("data_return"));
                    mBackupStart();
                }
                break;
            case DOWNLOAD_FILE:
                if(data.getStringExtra("data_return").equals("false")) {
                    Toast.makeText(PublicContext.getContext(), "验证失败", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PublicContext.getContext(), "验证成功，开始恢复", Toast.LENGTH_SHORT).show();
                    UserDao.setU_user(data.getStringExtra("data_return"));
                    mRecoveryStart();
                }
                break;
            default:
                //是否选择，没选择就不会继续
                if (resultCode == Activity.RESULT_OK) {
                    //得到uri，后面就是将uri转化成file的过程。
                    Uri uri = data.getData();
                    //判断版本获取路径的方式，在拿到uri之后进行版本判断大于等于24（即Android7.0）用最新的获取路径方式，否则用之前的方式
                    if (Build.VERSION.SDK_INT >= 24) {
                        //新的方式
                        path = getFilePathFromURI(this, uri);
                        Log.d("***1",path);
                    } else {
                        //旧的方式
                        //得到uri，后面就是将uri转化成file的过程。
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
                        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        actualimagecursor.moveToFirst();
                        String img_path = actualimagecursor.getString(actual_image_column_index);
                        File file = new File(img_path);
                        path = file.toString();
                        Log.d("***2",path);
                    }
                    String zipName = FileUtils.checkZipFile(path);
                    Log.d("zippp",zipName+"  "+path);
                    if(zipName.equals(".zip")) {
                        final File oldDir = new File(Environment.getExternalStorageDirectory(), "NoteBlocks");
                        if (!oldDir.exists()) {
                            oldDir.mkdirs();
                        }
                        handler.sendMessage(ResultFactory.getResultMessageBySet("读取资源成功，正在刷新列表"));
                        Thread th = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //创建NoteBlocks文件夹
                                    File dir = new File(Environment.getExternalStorageDirectory(), "NoteBlocks");
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    //解压NoteBlocks.zip到NoteBlocks文件夹中
                                    Zip.unzip(path, dir.getAbsolutePath());
                                    //读取NoteBlocks.json文件中的便签数据
                                    Backup.readJsonFile(dir.getAbsolutePath() + "/NoteBlocks.json");
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //启动线程
                        th.start();
                    }
                    else{
                        handler.sendMessage(ResultFactory.getResultMessageBySet("读取资源失败，请选择正确的文件"));
                    }
        }
        }
    }

    /**
     * 新的方式
     * @param context
     * @param contentUri
     * @return
     */
    public String getFilePathFromURI(Context context, Uri contentUri) {
        File rootDataDir = context.getFilesDir();
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(rootDataDir + File.separator + fileName);
            copyFile(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public void copyFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }


    private void verifyUserAndStart(int functionFlag){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivityForResult(intent,functionFlag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    /**
     *  设置滑动菜单
     */

    private void setUpMenu() {

        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.menu_background5);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
        resideMenu.setScaleValue(0.6f);
        // create menu items;
        itemExport     = new ResideMenuItem(this, R.drawable.icon_export,"导出");
        itemImport  = new ResideMenuItem(this, R.drawable.icon_import,"导入");
        itemRegister = new ResideMenuItem(this, R.drawable.icon_register,"注册");
        itemBackup = new ResideMenuItem(this, R.drawable.icon_backup,"云备份");
        itemRecovery = new ResideMenuItem(this, R.drawable.icon_recovery,"云恢复");

        itemExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExport();
            }
        });

        itemImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImport();
            }
        });

        itemRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegister();
            }
        });

        itemBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackup();
            }
        });

        itemRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecovery();
            }
        });


        resideMenu.addMenuItem(itemExport, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemImport, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemRegister, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemBackup, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemRecovery, ResideMenu.DIRECTION_RIGHT);

        // You can disable a direction by setting ->
        // resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        findViewById(R.id.title_bar_right_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {
        resideMenu.closeMenu();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            //Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            //Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu(){
        return resideMenu;
    }

}

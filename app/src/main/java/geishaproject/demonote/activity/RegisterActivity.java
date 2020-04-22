package geishaproject.demonote.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import geishaproject.demonote.R;
import geishaproject.demonote.module.http.HttpSender;
import geishaproject.demonote.module.http.ResultFactory;
import geishaproject.demonote.module.http.UrlFactory;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.module.http.HttpCallbackListener;
import geishaproject.demonote.utils.JSON;
import geishaproject.demonote.utils.PublicContext;

public class RegisterActivity extends AppCompatActivity {

    public static final int UPDATE_TEXT = 1;//RegisterActivity类handler更新数据标识
    public static final int MAKE_TOAST = 2; //RegisterActivity类handler弹窗标识

    /**
     * 新增一个Handler对象处理子线程消息传递
     */
    @SuppressLint("HandlerLeak")
    private Handler register_handler = new Handler(){
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
        setContentView(R.layout.activity_register);
        init();
        addListener();
    }

    /**
     * 初始化方法
     */
    public void init(){
        Components.user_name = findViewById(R.id.ed_register_username);
        Components.user_passwd = findViewById(R.id.ed_register_passwd);
        Components.btn_register = findViewById(R.id.btn_register);
    }

    public void addListener(){
        Components.btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRegisterHttp();
            }
        });
    }

    /**
     * 发送http请求注册账号
     */
    public void sendRegisterHttp(){
        String u_name = Components.user_name.getText().toString(); //从输入框取值
        String u_passwd = Components.user_passwd.getText().toString();

        Map userMap = new HashMap();    //将值构建成键值对
        userMap.put("u_name",u_name);
        userMap.put("u_passwd",u_passwd);

        //调用http服务与服务器交互，并通过register_handler构建消息弹窗联系
        String userJson = JSON.getJsonByMap(userMap);
        HttpSender.sendPost(userJson, UrlFactory.post_RegisterUrl, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.d("sendRegisterHttp",response);
                register_handler.sendMessage( ResultFactory.getResultMessageByResponse(response) );
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 重写返回方法做跳转到主界面
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
        RegisterActivity.this.finish();
    }
}

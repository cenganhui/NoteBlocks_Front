package geishaproject.demonote.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import geishaproject.demonote.R;
import geishaproject.demonote.module.http.HttpSender;
import geishaproject.demonote.module.http.UrlFactory;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.module.http.HttpCallbackListener;
import geishaproject.demonote.utils.JSON;
import geishaproject.demonote.utils.PublicContext;

public class LoginActivity extends AppCompatActivity {
    public static final int UPDATE_TEXT = 1;//RegisterActivity类handler更新数据标识
    public static final int MAKE_TOAST = 2; //RegisterActivity类handler弹窗标识

    //工作标识
    private int methodFlag ;

    //工作URL
    private String methodName = "";
    /**
     * 新增一个Handler对象处理子线程消息传递
     */
    @SuppressLint("HandlerLeak")
    private Handler login_handler = new Handler(){
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
        setContentView(R.layout.activity_login);
        init();
        addListener();
    }

    /**
     * 初始化方法
     */
    public void init(){
        Intent intent = this.getIntent();  //获取上一个活动传来的intent
        methodFlag = intent.getIntExtra("HttpFlag", 0);//根据上一个活动传过来的intent中的数据判断新建与修改
        Components.login_user_name = findViewById(R.id.ed_login_username);
        Components.login_user_passwd = findViewById(R.id.ed_login_passwd);
        Components.btn_login = findViewById(R.id.btn_login);
    }

    public void addListener(){
        Components.btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRegisterHttp();
            }
        });
    }

    /**
     * 发送http请求验证登陆账号
     */
    public void sendRegisterHttp(){
        String u_name = Components.login_user_name.getText().toString(); //从输入框取值
        String u_passwd = Components.login_user_passwd.getText().toString();
        Map userMap = new HashMap();    //将值构建成键值对
        userMap.put("u_name",u_name);
        userMap.put("u_passwd",u_passwd);
        String userJson = JSON.getJsonByMap(userMap);
        HttpSender.sendPost(userJson, UrlFactory.get_LoginUrl, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                returnResultByResponse(response);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 返回信息给上一个活动并结束
     * @param response http请求获取的数据
     */
    private void returnResultByResponse(String response) {
        //根据http请求返回数据，取得需要的值
        String result=response.substring(response.indexOf(":")+1,response.length());//取后端返回的值
        //构建intent
        Intent intent = new Intent();
        intent.putExtra("data_return",result);
        //设置结果
        setResult(RESULT_OK,intent);
        //结束活动
        finish();
    }

    /**
     * 重写返回方法做跳转到主界面
     */
    @Override
    public void onBackPressed() {
        //构建intent
        Intent intent = new Intent();
        intent.putExtra("data_return","false");
        //设置结果
        setResult(RESULT_OK,intent);
        //结束活动
        finish();
    }

}

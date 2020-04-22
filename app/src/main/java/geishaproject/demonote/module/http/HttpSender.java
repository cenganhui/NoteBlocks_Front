package geishaproject.demonote.module.http;

import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HttpSender{
    private static final int TIME_OUT = 10 * 1000;  // 超时时间

    /**
     * 发送Post方法
     * @param Json                  Json格式字符串，存储发送信息
     * @param fullUrl               完整的URL链接如：http://scnu.geishaproject.top:8080/shareNote
     * @param callbackListener      实现接口后传入，起回调作用
     */
    public static void sendPost(final String Json,final String fullUrl,final HttpCallbackListener callbackListener){
        //网络请求无法再主线程进行，因此需要新开线程发送请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //输出日志
                    Log.d("HttpSender", "sendPost:json:" + Json);
                    Log.d("HttpSender", "sendPost:fullUrl:" + fullUrl);

                    //构建需要使用的对象
                    String result = "";             //获取服务器结果
                    BufferedReader reader = null;   //转换流为字符串结果
                    URL url = new URL(fullUrl);

                    //构建HttpURLConnection对象
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");// 设置文件类型:
                    conn.setRequestProperty("accept", "application/json");  // 设置接收类型否则返回415错误



                    // 将请求数据写入连接的流中
                    if (Json != null && !TextUtils.isEmpty(Json)) {
                        byte[] writebytes = Json.getBytes();    //转换为字节数组
                        conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));// 设置文件长度
                        OutputStream outwritestream = conn.getOutputStream();   //获取连接的流
                        outwritestream.write(Json.getBytes());          //写入
                        outwritestream.flush();                         //刷新
                        outwritestream.close();                         //关闭
                    }

                    //输出日志
                    Log.d("HttpSender", "sendPost:conn.getResponseCode:" + conn.getResponseCode());

                    //判断连接状态后获取数据
                    if (conn.getResponseCode() == 200) {
                        reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        result = reader.readLine();
                    }

                    //将请求结果放入回调函数处理
                    callbackListener.onFinish(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * 发送Get方法
     * @param fullUrl              完整的URL链接如：http://scnu.geishaproject.top:8080/getShareNote?sn_id=20
     * @param callbackListener     实现接口后传入，起回调作用
     */
    public static void sendGet(final String fullUrl,final HttpCallbackListener callbackListener){
        //网络请求无法再主线程进行，因此需要新开线程发送请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //输出日志
                    Log.d("HttpSender", "sendGet:fullUrl:" + fullUrl);

                    //构建需要使用的对象
                    URL url = new URL(fullUrl);
                    //构建HttpURLConnection对象
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setReadTimeout(TIME_OUT);

                    //获取返回流
                    InputStream in = conn.getInputStream();

                    //下面对获取到的输入流进行读取
                    StringBuilder respsnse = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while((line = reader.readLine()) != null){
                        respsnse.append(line);
                    }
                    callbackListener.onFinish(respsnse.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     *
     * @param fullUrl               完整的URL链接：http://scnu.geishaproject.top:8080/getShareNote?sn_id=20
     * @param callbackListener      实现接口后传入，起回调作用
     */
    public static void downFile(final String fullUrl,final String savePath,final String saveName,final HttpCallbackListener callbackListener){
        //网络请求无法再主线程进行，因此需要新开线程发送请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //输出日志
                    Log.d("HttpSender", "downFile:fullUrl:" + fullUrl);
                    Log.d("HttpSender", "downFile:savePath:" + savePath);
                    Log.d("HttpSender", "downFile:saveName:" + saveName);

                    //构建需要使用的对象
                    URL url = new URL(fullUrl);
                    FileOutputStream outputStream = new FileOutputStream(savePath+"/"+saveName);
                    //构建HttpURLConnection对象
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setReadTimeout(TIME_OUT);

                    //获取返回流
                    InputStream in = conn.getInputStream();
                    byte[] bytes= new byte[4*1024];//文件缓存区
                    int len=0;
                    while((len=in.read(bytes))!=-1){
                        outputStream.flush();
                        outputStream.write(bytes,0,len);
                    }
                    outputStream.close();//关闭流
                    callbackListener.onFinish("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    /**
     * Http Post传文件
     * 在子线程中调用
     * @param file
     * @return
     */
    public static void uploadFile(final String fullUrl,final File file,final HttpCallbackListener callbackListener) {
        //网络请求无法再主线程进行，因此需要新开线程发送请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; // 内容类型
                String result="";
                try {
                    URL url = new URL(fullUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(TIME_OUT);
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setDoInput(true); // 允许输入流
                    conn.setDoOutput(true); // 允许输出流
                    conn.setUseCaches(false); // 不允许使用缓存
                    conn.setRequestMethod("POST"); // 请求方式
                    conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                            + BOUNDARY);
                    if (file != null) {

                        //当文件不为空，把文件包装并且上传
                        OutputStream outputSteam = conn.getOutputStream();

                        DataOutputStream dos = new DataOutputStream(outputSteam);
                        StringBuffer sb = new StringBuffer();
                        sb.append(PREFIX);
                        sb.append(BOUNDARY);
                        sb.append(LINE_END);

                        //这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                        //filename是文件的名字，包含后缀名的 比如:abc.png
                        sb.append("Content-Disposition: form-data; name=\"multipartFile\"; filename=\""
                                + file.getName() + "\"" + LINE_END);
                        sb.append("Content-Type: application/octet-stream; charset="
                                + "UTF-8" + LINE_END);
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());
                        InputStream is = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                        }
                        is.close();
                        dos.write(LINE_END.getBytes());
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                                .getBytes();
                        dos.write(end_data);
                        dos.flush();
                        // 获取响应码 200=成功 当响应成功，获取响应的流
                        if (conn.getResponseCode() == 200) {
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                            result = reader.readLine();
                        }
                        callbackListener.onFinish(result);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

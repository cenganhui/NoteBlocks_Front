package geishaproject.demonote.module.http;

/**
 * 自定义接口实现回调函数
 */
public interface HttpCallbackListener {
    /**
     * 该方法表示当服务器成功响应我们的请求时调用
     * @param response 表示服务器返回的数据
     */
    void onFinish(String response);

    /**
     * 该方法表示当进行网络操作出现错误的时候调用
     * @param e 错误信息
     */
    void onError(Exception e);
}

package geishaproject.demonote.module.http;

import java.util.Iterator;
import java.util.Map;

public class UrlFactory {
    public static String addressHead = "http://"+ServerInfo.domain+":"+ServerInfo.port;                 //地址头部
    //POST方法
    public static String post_UploadUrl=jointAddressAndMethodName(MethodName.upload);       //上传功能完整URL
    public static String post_ShareUrl=jointAddressAndMethodName(MethodName.share);         //分享功能完整URL
    public static String post_BackupUrl=jointAddressAndMethodName(MethodName.backup);       //备份功能完整URL
    public static String post_RegisterUrl=jointAddressAndMethodName(MethodName.register);   //注册功能完整URL

    //GET方法
    public static String get_RecoveryUrl=jointAddressAndMethodName(MethodName.recovery);   //还原功能完整URL
    public static String get_LoginUrl=jointAddressAndMethodName(MethodName.login);         //登录功能完整URL
    public static String get_CloudRecoveryUrl=jointAddressAndMethodName(MethodName.cloudRecovery);   //云恢复功能完整URL

    /**
     * 拼接地址头部和方法名
     * @param methodName 方法名
     * @return
     */
    public static String jointAddressAndMethodName(String methodName){
        return addressHead+"/"+methodName;
    }

    /**
     * 通过Map将参数拼接进getURl
     * @param getUrl
     * @param parameter
     * @return
     */
    public static String jointGetUrlAndMap(String getUrl,Map parameter){
        String wholeURL=getUrl+"?";
        Iterator iter = parameter.entrySet().iterator();//迭代器循环获取Map里的值

        while (iter.hasNext()) {
            //循环迭代
            Map.Entry entry = (Map.Entry) iter.next();
            //取Map的键值对
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            //拼接URL
            wholeURL +=(key+"="+value);

            if(iter.hasNext()){//判断是否需要给加&继续拼接,
                wholeURL+="&";
            }
        }
        return wholeURL;
    }

}

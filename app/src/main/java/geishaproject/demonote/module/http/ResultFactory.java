package geishaproject.demonote.module.http;

import android.os.Message;
import android.util.Log;
import geishaproject.demonote.ui.Components;

public class ResultFactory {
    /**
     * 通过传入设置的字符串值构建用于Activity弹出消息框的Message信息
     * @param setString 手动设置值
     * @return Message信息
     */
    public static Message getResultMessageBySet(String setString){
        Log.d("ResultFactory","getResultMessageBySet:setString:"+setString);
        Message setMessage = new Message();
        setMessage.what = Components.MAKE_TOAST;
        setMessage.obj = setString;
        return setMessage;
    }

    /**
     * 通过传入服务器返回值构建用于Activity弹出消息框的Message信息
     * @param response 服务器返回值
     * @return Message信息
     */
    public static Message getResultMessageByResponse(String response){
        Log.d("ResultFactory","getResultMessageByResponse:response:"+response);
        Message resultMessage = new Message();
        resultMessage.what = Components.MAKE_TOAST;
        resultMessage.obj = getResultStringByResponse(response);
        return resultMessage;
    }

    /**
     * 通过传入服务器返回值构建结果提示信息
     * @param response  服务器返回值
     * @return String结果提示信息
     */
    public static String getResultStringByResponse(String response){
        String resultString = "";
        String key = response.substring(0,response.indexOf(":"));//取后端返回的键
        String value = response.substring(response.indexOf(":")+1,response.length());//取后端返回的值

        switch(key){
            case "createUser":
                if(value.equals("true")){
                    resultString="创建用户成功";
                }else{
                    resultString="创建用户失败";
                }
                break;
            case "comparPasswd":
                if(value.equals("true")){
                    resultString="验证成功";
                }else{
                    resultString="验证失败";
                }
                break;
            case "uploadSingleFile":
                resultString=value;
                break;
            case "backupNotes":
                if(value.equals("true")){
                    resultString="备份成功";
                }else{
                    resultString="备份失败";
                }
                break;
            case "shareNote":
                if(value.equals("false")){
                    resultString="分享失败";
                }else{
                    resultString="分享成功，编号为:";
                    resultString+=value;
                }
            default:
                resultString="获取接口数据失败";
        }
        return resultString;
    }
}

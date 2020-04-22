package geishaproject.demonote.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSON {
    public static String getJsonByMap(Map jsonMap){
        String resultJsonString="{";
        String key="";
        String value="";

        Iterator iter = jsonMap.entrySet().iterator();//迭代器循环

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            key = entry.getKey().toString();
            value = entry.getValue().toString();

            resultJsonString += getJsonPair(key,value);
            if(iter.hasNext()){//判断是否需要给json加,
                resultJsonString+=",";
            }

        }
        resultJsonString +="}";
        return resultJsonString;
    }

    public static String turnJsonStyle(String string){
        return "\""+string+"\"";
    }

    public static String getJsonPair(String jsonKey,String jsonValue){
        return turnJsonStyle(jsonKey)+":"+turnJsonStyle(jsonValue);
    }


    public static String getJsonByArr(List arr){
        String JsonArr="[";
        for(int i = 0;i<arr.size();i++){
            JsonArr+=arr.get(i);
            if(i+1 < arr.size()){
                JsonArr+=",";
            }
        }
        JsonArr+="]";
        return JsonArr;
    }
}

package geishaproject.demonote.utils;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import geishaproject.demonote.model.Data;
public class DataJSON {

    public static String getJSONByArr(ArrayList<Data> datas){
        ArrayList<String> jsonArr = new ArrayList<String>();
        for(int i=0;i<datas.size();i++){
            String json = datas.get(i).toJSON();
            jsonArr.add(json);
        }
        return JSON.getJsonByArr(jsonArr);
    }

    public static ArrayList<Data> getArrByJSON(String json){
        ArrayList<Data> allDatas = new ArrayList<Data>();
        try {
            JSONArray jsonArray=new JSONArray(json);
            Log.d("DataJSON","getArrByJSON"+jsonArray.toString());
            for(int i=0;i<jsonArray.length();i++){
                Data data = new Data();
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                data.setIds(jsonObject.getInt("n_ids"));
                data.setTitle(jsonObject.getString("n_title"));
                data.setContent(jsonObject.getString("n_content"));
                data.setTimes(jsonObject.getString("n_times").replace("T"," ").substring(0,19));
                data.setAudioPath(jsonObject.getString("n_audioPath"));
                data.setPicturePath(jsonObject.getString("n_picturePath"));
                allDatas.add(data);
                Log.d("DataJSON","getArrByJSON:Data:"+data.toJSON());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return allDatas;
    }
}

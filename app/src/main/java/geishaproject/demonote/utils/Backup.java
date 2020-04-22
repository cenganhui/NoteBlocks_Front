package geishaproject.demonote.utils;

import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import geishaproject.demonote.model.Data;
import geishaproject.demonote.dao.DataDao;

/**
 * 导入和导出相关操作类
 */

public class Backup {
    /**
     * 创建NoteBlocks.json文件
     * @param file
     */
    public static void createJsonFile(File file){
        File JSONFile = new File(file,  "NoteBlocks.json");
        if (!JSONFile.exists()) {
            try {
                JSONFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String filePath = JSONFile.getAbsolutePath();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            //开始写json数据
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(fileOutputStream,"UTF-8"));
            jsonWriter.setIndent("  ");
            jsonWriter.beginArray();
            for(int i = DataDao.GetAllDatas().size()-1; i>=0; i--){
                jsonWriter.beginObject();
                jsonWriter.name("id").value(DataDao.GetAllDatas().get(i).getIds());
                jsonWriter.name("title").value(DataDao.GetAllDatas().get(i).getTitle());
                jsonWriter.name("content").value(DataDao.GetAllDatas().get(i).getContent());
                jsonWriter.name("audioPath").value(DataDao.GetAllDatas().get(i).getAudioPath());
                jsonWriter.name("picturePath").value(DataDao.GetAllDatas().get(i).getPicturePath());
                jsonWriter.name("times").value(DataDao.GetAllDatas().get(i).getTimes());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 读取NoteBlocks.json文件并将数据保存到DataDao和数据库中
     * @param JsonFilePath
     */
    public static void readJsonFile(String JsonFilePath){

        try {
            FileInputStream fileInputStream = new FileInputStream(JsonFilePath);
            JsonReader jsonReader = new JsonReader(new InputStreamReader(fileInputStream,"UTF-8"));
            jsonReader.beginArray();
            Data data = new Data(0,"","","","","");
            while(jsonReader.hasNext()){
                jsonReader.beginObject();
                data = new Data(0,"","","","","");
                while(jsonReader.hasNext()){
                    String s = jsonReader.nextName();
                    if(s.equals("id")){
                        data.setIds(jsonReader.nextInt());
                    }
                    else if(s.equals("title")){
                        data.setTitle(jsonReader.nextString());
                    }
                    else if(s.equals("content")){
                        data.setContent(jsonReader.nextString());
                    }
                    else if(s.equals("audioPath")){
                        data.setAudioPath(jsonReader.nextString());
                    }
                    else if(s.equals("picturePath")){
                        data.setPicturePath(jsonReader.nextString());
                    }
                    else{
                        jsonReader.skipValue();
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//"+"T"+"编辑便签的时间，格式化
                    Date date = new Date(System.currentTimeMillis());
                    String time = simpleDateFormat.format(date);
                    //创建新时间
                    data.setTimes(time);
                }
                jsonReader.endObject();
                data.cutPicturePath();
                data.cutAudioPathArr();
                //若便签不相同，则添加便签
                boolean sign = false;
                for (int i=0;i<DataDao.GetAllDatas().size();i++){
                    if(isTheSameData(data,DataDao.GetAllDatas().get(i))){
                        sign = true;
                        break;
                    }
                }
                if(!sign){
                    boolean flag = DataDao.AddNewData(data);
                }
            }

            jsonReader.endArray();
            jsonReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断两个便签是否相同
     * @param data1
     * @param data2
     * @return
     */
    public static boolean isTheSameData(Data data1, Data data2){
        String titleAndContent1 = data1.getTitle()+data1.getContent();
        String titleAndContent2 = data2.getTitle()+data2.getContent();
        if(titleAndContent1.equals(titleAndContent2)){
            return true;
        }
        else{
            return false;
        }
    }

}

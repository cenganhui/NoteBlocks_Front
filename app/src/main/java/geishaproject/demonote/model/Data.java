
package geishaproject.demonote.model;


import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import geishaproject.demonote.dao.UserDao;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.utils.JSON;

public class Data {
    private static final String TAG = "PhotoTool";
    private int ids;        //编号
    private String title;   //标题
    private String content; //内容
    private String times;   //时间
    private String audioPath;   //音频路径
    private String picturePath; //图片路径
    ArrayList<String> picturePathArr; //存放分割好的图片路径
    ArrayList<String> audioPathArr;  //存放分割好的音频路径

    public Data(){
        this.ids=0;
        this.times="";
        this.title="";
        this.content="";
        this.audioPath = "";
        this.picturePath = "";
        this.picturePathArr = new ArrayList<>();
        this.audioPathArr = new ArrayList<>();
    }

    public Data(int id , String time , String title , String content , String aP , String pP){
        this.ids=id;
        this.times=time;
        this.title=title;
        this.content=content;
        this.audioPath = aP;
        this.picturePath = pP;
        this.picturePathArr = new ArrayList<>();
        this.audioPathArr = new ArrayList<>();
        cutAudioPathArr();
        cutPicturePath();
    }


    public int getIds() {

        return ids;

    }

    public String getTitle() {

        return title;

    }

    public String getContent() {

        return content;

    }

    public String getTimes() {

        return times;

    }

    public String getAudioPath(){

        return audioPath;

    }

    public String getPicturePath() {

        return picturePath;

    }

    public int getAudioPathSize(){
        return audioPathArr.size();
    }

    public int getPicturePathSize() {
        return picturePathArr.size();
    }

    public void deleteAudioPathArr(int i){
        audioPathArr.remove(i);
    }

    public void deletePicturePathArr(int i){
        picturePathArr.remove(i);
    }

    public int addAudioPathArr(String adress){
        audioPathArr.add(adress);
        return audioPathArr.size()-1;
    }

    public int addPicturePathArr(String adress){
        picturePathArr.add(adress);
        return picturePathArr.size()-1;
    }

    public String getAudioPathArr(int i){
        return  audioPathArr.get(i);
    }

    public String getPicturePathArr(int i){
        return picturePathArr.get(i);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public void setIds(int ids){
        this.ids = ids;
    }

    public void clearBitmap() {
        this.picturePathArr = null;
    }


    @Override
    public String toString() {
        return  toJSON();
    }

    public String toJSON(){
        Map dataMap = new HashMap();
        dataMap.put("u_name", UserDao.getU_user());
        dataMap.put("n_ids",getIds());
        dataMap.put("n_title",getTitle());
        dataMap.put("n_content",getContent().replace("\""," \\\" ").replace("\n","\\n"));
        dataMap.put("n_times",getTimes().replace(" ","T"));
        dataMap.put("n_audioPath",getAudioPath());
        dataMap.put("n_picturePath",getPicturePath());
        return  JSON.getJsonByMap(dataMap);
    }

    public String toShareJSON() {
        Map dataMap = new HashMap();
        dataMap.put("u_name", UserDao.getU_user());
        dataMap.put("sn_ids",getIds());
        dataMap.put("sn_title",getTitle());
        dataMap.put("sn_content",getContent().replace("\""," \\\" ").replace("\n","\\n")
                .replace(Environment.getExternalStorageDirectory().getPath()+"/NoteBlocks/picture/","<img>")
                .replace(Environment.getExternalStorageDirectory().getPath()+"/NoteBlocks/record/","<audio>"));
        dataMap.put("sn_times",getTimes().replace(" ","T"));
        dataMap.put("sn_audioPath",getAudioPath().replace(Environment.getExternalStorageDirectory().getPath()+"/NoteBlocks/record/",""));
        dataMap.put("sn_picturePath",getPicturePath().replace(Environment.getExternalStorageDirectory().getPath()+"/NoteBlocks/picture/",""));
        return  JSON.getJsonByMap(dataMap);
    }

    /*
        对音频路径进行分割，分别存入audioPathArr中
     */

    public void cutAudioPathArr(){
        audioPathArr = new ArrayList<>();
        String singleAudioPath = "";
        String oldAudioPath = audioPath;
        //去掉?
        int begin = 0,end = 0;
        boolean flag;
        for(int i=0;i<oldAudioPath.length();i++) {
            flag = true;
            if(oldAudioPath.charAt(i)=='?') {
                end = i;
                singleAudioPath = oldAudioPath.substring(begin, end);
                for(int j=0;j<audioPathArr.size();j++){
                    if(singleAudioPath.equals(audioPathArr.get(j))){
                        flag = false;
                        break;
                    }
                }
                if(flag) {
                    audioPathArr.add(singleAudioPath);
                }
                begin=end+1;
            }
        }
    }


    /*
        对图片路径进行分割，分别存入picturePathArr中
     */
    public void cutPicturePath(){
        //picturePathArr = new ArrayList<>();
        String singlePicturePath = "";
        String oldPicturePath = picturePath;
        //去掉?
        int begin = 0,end = 0;
        boolean flag;
        for(int i=0;i<oldPicturePath.length();i++){
            flag = true;
            if(oldPicturePath.charAt(i)=='?'){
                end = i;
                singlePicturePath = oldPicturePath.substring(begin, end);
                for(int j=0;j<picturePathArr.size();j++){
                    if(singlePicturePath.equals(picturePathArr.get(j))){
                        flag = false;
                        break;
                    }
                }
                if(flag) {
                    picturePathArr.add(singlePicturePath);
                }
                begin=end+1;
            }
        }
    }

    /*
        获取分割好的音频路径数组audioPathArr
     */

    public ArrayList<String> getAudioPathArr() {
        return audioPathArr;
    }


    /*
        获取分割好的图片路径数组picturePathArr
     */

    public ArrayList<String> getPicturePathArr() {
        return picturePathArr;
    }

    /**
     * 删除实际文件的函数
     */
    public void deleteForAdress(String adress){
        deleteSingleFile(adress);
    }
    public static void deleteSingleFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 合适文本与DAta
     */
    public void check() {
        //清除原本的记录，按最后的结果来
        Log.d(TAG, "保存图片时，大小：" + picturePathArr.size());
        Log.d(TAG, "保存音频时，大小：" + audioPathArr.size());
        String world = Components.data.getContent();
        String now = world;         //还有的文本
        int endindex = 0;        //要替换图片的位置
        int i = 0;               //变量
        int temp = 0;
        if (picturePathArr.size() > 0) {
            for (i = 0; i < picturePathArr.size(); i++) {
                //找到要替换的特殊字符位置
                Log.d(TAG, "dizhi:" + picturePathArr.get(i));
                endindex = now.indexOf(picturePathArr.get(i));
                if (endindex == -1) {
                    //删除实际文件
                    deleteForAdress(picturePathArr.get(i - temp));
                    //删除变量内容
                    deletePicturePathArr(i - temp);
                    temp++;
                } else {
                    //切割子文本
                    now = now.substring(endindex + picturePathArr.get(i).length());
                }
            }
        }
        world = Components.data.getContent();
        now = world;         //还有的文本
        i = 0;               //变量
        temp = 0;
        if (audioPathArr.size() > 0) {
            for (i = 0; i < audioPathArr.size(); i++) {
                //数据定义
                String show;        //要放上去的文本
                //找到要替换的特殊字符位置
                endindex = now.indexOf(audioPathArr.get(i));
                if (endindex == -1) {
                    //删除实际文件
                    deleteForAdress(audioPathArr.get(i - temp));
                    //删除变量内容
                    deleteAudioPathArr(i - temp);
                    temp++;
                } else {
                    now = now.substring(endindex + audioPathArr.get(i).length());
                }
            }
        }
        saveData();
    }

    /**
     * 将数组数据存入路径String中
     */
    private void saveData(){
        String newPath = "";
        if (picturePathArr.size() > 0) {
            for (int i = 0; i < picturePathArr.size(); i++) {
                newPath += picturePathArr.get(i)+"?" ;
            }
            setPicturePath(newPath);
            //12313162
        }
        newPath = "";
        if (audioPathArr.size() > 0) {
            for (int i = 0; i < audioPathArr.size(); i++) {
                newPath += audioPathArr.get(i) +  "?" ;
            }
            setAudioPath(newPath);
        }
    }

    /**
     * 输出全部信息,我带你们打
     */
    public void showwwww(){
        Log.e(TAG,"音频总地址daixoa:"+getAudioPathSize());
        Log.e(TAG,"图片总地址daixoa:"+getPicturePathSize());
        Log.e(TAG,"音频总地址"+getAudioPath());
        Log.e(TAG,"图片总地址"+getPicturePath());
    }

}

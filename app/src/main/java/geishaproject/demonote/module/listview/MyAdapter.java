
package geishaproject.demonote.module.listview;



import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import geishaproject.demonote.R;
import java.util.ArrayList;
import geishaproject.demonote.model.Data;

public class MyAdapter extends BaseAdapter {

    LayoutInflater inflater;

    ArrayList<Data> array;

    //ListView主页能显示内容的最大字数
    int maxLength = 60;

    public MyAdapter(LayoutInflater inf,ArrayList<Data> arry){

        this.inflater=inf;

        this.array=arry;

    }

    @Override

    public int getCount() {

        return array.size();

    }

    @Override

    public Object getItem(int position) {

        return array.get(position);

    }

    @Override

    public long getItemId(int position) {

        return position;

    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {  //代码块中包含了对listview的效率优化
        ViewHolder vh;
        if(convertView==null){
            vh=new ViewHolder();
            convertView=inflater.inflate(R.layout.list_item,null);//加载listview子项
            vh.tv1=(TextView) convertView.findViewById(R.id.list_title);
            vh.tv2=(TextView) convertView.findViewById(R.id.list_content);
            vh.tv3=(TextView) convertView.findViewById(R.id.list_time);
            convertView.setTag(vh);
        }
        vh=(ViewHolder) convertView.getTag();
        vh.tv1.setText( array.get(position).getTitle() );
        vh.tv2.setText( dealContent(array.get(position).getContent()) ); //显示内容时做截断优化
        vh.tv3.setText( array.get(position).getTimes() );

        Log.d("setText","MyAdapter: "+array.get(position).getTimes());
        return convertView;
    }

    /**
     * 处理一些内部标识和长度美化后再显示在界面上
     * @param originalContent
     * @return
     */
    public String dealContent(String originalContent){
        return cutLongContent( replaceAddress( originalContent ) );
    }

    /**
     * 过长时添加缩略号
     * @param longContent
     * @return
     */
    public String cutLongContent(String longContent){
        if(longContent.length()>=maxLength) {
            return longContent.substring(0,maxLength)+"...";
        }else{
            return longContent;
        }
    }

    /**
     * 对真实地址进行美化替换处理
     * @param originalContent
     * @return
     */
    public String replaceAddress(String originalContent){
        //使用replaceAll支持正则
        originalContent=originalContent.replaceAll(Environment.getExternalStorageDirectory().getPath()+"/NoteBlocks/record/\\d*.amr","[录音]")
                                        .replaceAll(Environment.getExternalStorageDirectory().getPath()+"/NoteBlocks/picture/\\d*.jpg","[图片]");
        return originalContent;
    }
    class ViewHolder{     //内部类，对控件进行缓存
        TextView tv1,tv2,tv3;
    }

}

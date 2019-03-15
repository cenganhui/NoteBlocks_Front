package geishaproject.demonote.module.richtext;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ViewTreeObserver;

import geishaproject.demonote.ui.Components;
import geishaproject.demonote.utils.PublicContext;

public class RichText {
    private static final String TAG = "RichText";//Log调试
    /**
     * 富文本自定义函数
     */
    public static SpannableString GetSpannableString(Bitmap bitmap, String specialchar){
        SpannableString spannableString = new SpannableString(specialchar);
        Log.d(TAG,"diaonma"+ Components.ed_content.getWidth()+"////"+Components.ed_content.getHeight());
       /* BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;*/
        Log.d(TAG,"大小为（m）："+bitmap.getByteCount() / 1024 / 1024+"宽度为" + bitmap.getWidth() + "高度为" + bitmap.getHeight());
        ImageSpan imgSpan = new ImageSpan(PublicContext.getContext(),bitmap, DynamicDrawableSpan.ALIGN_BASELINE);
        spannableString.setSpan(imgSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * 初始化有图片的文字/没图片处理在最下面的else
     */
    public static void initifphotohave() {

        //用下面函数可以将控件宽高提前拿到
        Components.ed_content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                Components.ed_content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Components.mPhotoTool.addsize(Components.ed_content.getWidth(),Components.ed_content.getHeight());
                String world = Components.data.getContent();
                String now = world;         //还有的文本
                //全部文本
                int startindex=0;      //要替换图片的位置
                int endindex=0;        //要替换图片的位置
                int i=0;               //变量
                Bitmap bitmap= null;
                Log.d(TAG,"ed text ;:"+ Components.mPhotoTool.BitmapAdressSize());
                if (Components.mPhotoTool.BitmapAdressSize()>0){
                    for (i=0; i<Components.mPhotoTool.BitmapAdressSize();i++) {
                        //数据定义
                        Log.d(TAG, "要切的文本" + Components.mPhotoTool.GetBitmapNmae(i));
                        String show;        //要放上去的文本
                        //找到要替换的特殊字符位置
                        endindex = now.indexOf(Components.mPhotoTool.GetBitmapNmae(i));
                        //切割子文本
                        show = now.substring(0, endindex);
                        now = now.substring(endindex + Components.mPhotoTool.GetBitmapNmae(i).length());
                        //输出文本
                        Components.ed_content.append(show);
                        //输出图片，GetSpannableString 富文本操作
                        bitmap = Components.mPhotoTool.getBitmapForAdress(Components.mPhotoTool.GetBitmapNmae(i));
                        SpannableString spannableString = GetSpannableString(bitmap, Components.mPhotoTool.GetBitmapNmae(i));
                        Components.ed_content.append(spannableString);
                    }
                    if(i == Components.mPhotoTool.BitmapAdressSize())
                        Components.ed_content.append(now);
                }
                else
                    Components.ed_content.setText(Components.data.getContent());

            }
        });

    }
}

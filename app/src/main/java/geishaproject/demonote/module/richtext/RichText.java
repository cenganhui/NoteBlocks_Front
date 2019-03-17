package geishaproject.demonote.module.richtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.io.IOException;

import geishaproject.demonote.R;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.utils.PublicContext;

public class RichText {
    private static final String TAG = "RichText";//Log调试
    private static MediaPlayer player = new MediaPlayer();
    /**
     * 图片富文本自定义函数
     */
    public static SpannableString GetSpannableString(Bitmap bitmap, String specialchar){
        SpannableString spannableString = new SpannableString(specialchar);
        Log.d(TAG,"diaonma"+Components.ed_content.getWidth()+"////"+Components.ed_content.getHeight());
       /* BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;*/
        Log.e(TAG,"大小为（m）："+bitmap.getByteCount() / 1024 / 1024+"宽度为" + bitmap.getWidth() + "高度为" + bitmap.getHeight());
        ImageSpan imgSpan = new ImageSpan(PublicContext.getContext(),bitmap, DynamicDrawableSpan.ALIGN_BASELINE);
        spannableString.setSpan(imgSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
    /**
     * 音频富文本自定义函数
     */
    public static SpannableString GetRecordSpannableString(int i, String specialchar){
        SpannableString spannableString = new SpannableString(specialchar);
        /* Bitmap bitmap= BitmapFactory.decodeResource(PublicContext.getContext().getResources(),R.drawable.record);
        bitmap = imageScale(bitmap,110 ,60);
        ImageSpan imgSpan = new ImageSpan(PublicContext.getContext(),bitmap, DynamicDrawableSpan.ALIGN_BASELINE);*/
        MyIm imageSpan=new MyIm(PublicContext.getContext(), R.drawable.record);
        spannableString.setSpan(imageSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Components.ed_content.setMovementMethod(LinkMovementMethod.getInstance());
        spannableString.setSpan(new TextClick(i),0,spannableString.length() , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                int i=0,pi=0,ri=0;               //变量
                Bitmap bitmap= null;

                Log.d(TAG,"photo size ;:"+ Components.mPhotoTool.BitmapAdressSize());
                Log.d(TAG,"record size ;:"+ Components.mPhotoTool.RecordAdressSize());
                Log.d(TAG," size ;:"+ now.indexOf("amr")+"   "+now.indexOf("jpg"));

                Log.d(TAG,"ed text ;:"+ Components.mPhotoTool.BitmapAdressSize());
                //替换掉图片部分
                if (Components.mPhotoTool.BitmapAdressSize() + Components.mPhotoTool.RecordAdressSize()>0){
                    for (i=0; i<Components.mPhotoTool.BitmapAdressSize() + Components.mPhotoTool.RecordAdressSize();i++) {

                        //数据定义
                        //Log.d(TAG, "要切的文本" + mPhotoTool.GetBitmapNmae(i));
                        String show;        //要放上去的文本
                        //找到要替换的特殊字符位置
                        if(now.indexOf("amr") == -1){
                            endindex = now.indexOf(Components.mPhotoTool.GetBitmapNmae(pi));
                            //切割子文本
                            show = now.substring(0, endindex);
                            now = now.substring(endindex + Components.mPhotoTool.GetBitmapNmae(pi).length());
                            //输出文本
                            Components.ed_content.append(show);
                            //输出图片，GetSpannableString 富文本操作
                            bitmap =Components. mPhotoTool.getBitmapForAdress(Components.mPhotoTool.GetBitmapNmae(pi));
                            SpannableString spannableString = GetSpannableString(bitmap, Components.mPhotoTool.GetBitmapNmae(pi));
                            Components.ed_content.append(spannableString);
                            pi++;
                        }else if(now.indexOf("jpg") == -1){
                            endindex = now.indexOf(Components.mPhotoTool.GetRecordNmae(ri));
                            //切割子文本
                            show = now.substring(0, endindex);
                            now = now.substring(endindex + Components.mPhotoTool.GetRecordNmae(ri).length());
                            //输出文本
                            Components.ed_content.append(show);
                            //输出录音小标识到文本***********
                            SpannableString spannableString = GetRecordSpannableString(ri,Components.mPhotoTool.GetRecordNmae(ri));
                            Components.ed_content.append(spannableString);
                            ri++;
                        }else
                        if (now.indexOf("amr")<now.indexOf("jpg")  ){
                            Log.d(TAG," adress ;:"+ Components.mPhotoTool.GetRecordNmae(ri));
                            endindex = now.indexOf(Components.mPhotoTool.GetRecordNmae(ri));
                            show = now.substring(0, endindex);
                            now = now.substring(endindex + Components.mPhotoTool.GetRecordNmae(ri).length());
                            Components.ed_content.append(show);
                            SpannableString spannableString =  GetRecordSpannableString(ri,Components.mPhotoTool.GetRecordNmae(ri));
                            Components.ed_content.append(spannableString);
                            ri++;
                        }else{
                            endindex = now.indexOf(Components.mPhotoTool.GetBitmapNmae(pi));
                            show = now.substring(0, endindex);
                            now = now.substring(endindex + Components.mPhotoTool.GetBitmapNmae(pi).length());
                            Components.ed_content.append(show);
                            bitmap =Components. mPhotoTool.getBitmapForAdress(Components.mPhotoTool.GetBitmapNmae(pi));
                            SpannableString spannableString = GetSpannableString(bitmap, Components.mPhotoTool.GetBitmapNmae(pi));
                            Components.ed_content.append(spannableString);
                            pi++;
                        }

                    }
                    if(i == Components.mPhotoTool.BitmapAdressSize() + Components.mPhotoTool.RecordAdressSize())
                        Components.ed_content.append(now);
                } else
                    Components.ed_content.setText(Components.data.getContent());

            }
        });

    }

    /**
     * 音频点击事件类
     */
    private static class TextClick extends ClickableSpan {
        int i;
        public TextClick(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View widget) {
            //在此处理点击事件
            if (Components.mPhotoTool.RecordAdressSize() != 0) {
                player.reset();
                try {
                    //Toast.makeText(New_note.this, data.getAudioPath(), Toast.LENGTH_SHORT).show();
                    Log.d("拿到音频地址",Components.mPhotoTool.GetRecordNmae(i));
                    player.setDataSource(Components.mPhotoTool.GetRecordNmae(i)); //获取录音文件
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e("------->", "点击了");
        }
        @Override
        public void updateDrawState(TextPaint ds) {
//            ds.setColor(ds.linkColor);
//            ds.setUnderlineText(true);
        }
    }

    /**
     * 缩放音频图片
     */
    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }

    /**
     * 解决图文对齐的类
     */
    public static class MyIm extends ImageSpan
    {
        public MyIm(Context arg0,int arg1) {
            super(arg0, arg1);
        }
        public int getSize(Paint paint, CharSequence text, int start, int end,
                           Paint.FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt fmPaint=paint.getFontMetricsInt();
                int fontHeight = fmPaint.bottom - fmPaint.top;
                int drHeight=rect.bottom-rect.top;

                int top= drHeight/2 - fontHeight/4;
                int bottom=drHeight/2 + fontHeight/4;

                fm.ascent=-bottom;
                fm.top=-bottom;
                fm.bottom=top;
                fm.descent=top;
            }
            return rect.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Drawable b = getDrawable();
            canvas.save();
            int transY = 0;
            transY = ((bottom-top) - b.getBounds().bottom)/2+top;
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

}

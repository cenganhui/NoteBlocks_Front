package geishaproject.demonote.module.richtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
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
import java.util.ArrayList;
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
        Log.d(TAG,"diaonma"+ Components.ed_content.getWidth()+"////"+ Components.ed_content.getHeight());
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

        /**
         * 添加回调函数，在控件完成绘制，确定宽高的时候执行自己的代码
         */
        Components.ed_content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                Components.ed_content.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                SpannableStringBuilder contentSpanBuilder  = new SpannableStringBuilder(Components.data.getContent());
                Components.data.cutAudioPathArr();
                Components.data.cutPicturePath();
                ArrayList<String> audioArr = Components.data.getAudioPathArr();
                ArrayList<String> pictureArr = Components.data.getPicturePathArr();

                //contentSpanBuilder没有index方法定位，而replace需要startIndex和endIndex定位替换，因此使用镜像String定位
                String flagString = Components.data.getContent();

                //音频替换
                for(int i=0;i<audioArr.size();i++){
                    Log.d("audioReplace:",audioArr.get(i));
                    //构建参数
                    int audioPathSize = audioArr.get(i).length();                  //音频地址长度
                    int audioPathStartIndex = flagString.indexOf(audioArr.get(i)); //起始位置
                    SpannableString audioSpan =  GetRecordSpannableString(i,audioArr.get(i));     //构建Span

                    //替换指定位置的文本为Span
                    contentSpanBuilder.replace(audioPathStartIndex,audioPathStartIndex+audioPathSize,audioSpan);   //替换
                }

                //图片替换
                for(int j=0;j<pictureArr.size();j++){
                    Log.d("pictureReplace:",pictureArr.get(j));
                    //构建参数
                    int picturePathSize = pictureArr.get(j).length();                  //图片地址长度
                    int picturePathStartIndex = flagString.indexOf(pictureArr.get(j)); //起始位置
                    Bitmap bitmap =Components.mPhotoTool.getBitmapForAdress(pictureArr.get(j));         //获取位图
                    SpannableString pictureSpan = GetSpannableString(bitmap, pictureArr.get(j));        //构建Span

                    //替换指定位置的文本为Span
                    contentSpanBuilder.replace(picturePathStartIndex,picturePathStartIndex+picturePathSize,pictureSpan);
                }
                //将SpanBuilder放入
                Components.ed_content.setText(contentSpanBuilder);
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
            if (Components.data.getAudioPathSize() != 0) {
                player.reset();
                try {
                    Log.d("拿到音频地址",Components.data.getAudioPathArr(i));
                    player.setDataSource(Components.data.getAudioPathArr(i)); //获取录音文件
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

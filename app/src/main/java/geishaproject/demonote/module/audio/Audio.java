package geishaproject.demonote.module.audio;

import android.Manifest;
import android.media.MediaPlayer;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import geishaproject.demonote.module.audio.manager.AudioRecordButton;
import geishaproject.demonote.module.permission.PermissionHelper;
import geishaproject.demonote.module.richtext.RichText;
import geishaproject.demonote.ui.Components;
import geishaproject.demonote.utils.PublicContext;

public class Audio {

    /**
     * 初始化录音模块
     */
    public static void initAudio() {
        Components.player = new MediaPlayer();   //实例化录音控件
    }
    RichText mRichText;
    /**
     * 播放录音
     */
    public static void PlayR () {
        if(!Components.data.getAudioPath().equals("")) {
            if (Components.player != null) {
                Components.player.reset();
                try {
                    Components.data.cutAudioPathArr();
                    Toast.makeText(PublicContext.getContext(), Components.data.getAudioPath(), Toast.LENGTH_SHORT).show();
                    int i = Components.data.getAudioPathArr().size();
                    Log.d("***size","*/*/*/*size"+i);
                    Components.player.setDataSource(Components.data.getAudioPathArr().get(i-1)); //获取录音文件
                    Components.player.prepare();
                    Components.player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        else{
            Toast.makeText(PublicContext.getContext(), "还未曾录音", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加录音点击事件监听
     */
    public  static  void addAudioListener() {

        Components.mEmTvBtn.setHasRecordPromission(false);
        Components.mHelper.requestPermissions("请授予[录音]、[读写]权限，否则无法录音",
                new PermissionHelper.PermissionListener() {
                    @Override
                    public void doAfterGrand(String... permission) {
                        Components.mEmTvBtn.setHasRecordPromission(true);

                        Components.mEmTvBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {
                            @Override
                            public void onFinished(float seconds, String filePath) {
                                Record recordModel = new Record();
                                recordModel.setSecond((int) seconds <= 0 ? 1 : (int) seconds);
                                recordModel.setPath(filePath);
                                recordModel.setPlayed(false);
                                Components.mRecords = recordModel;
                                String newAudioPath =Components.data.getAudioPath()+Components.mRecords.getPath()+"?";
                                //路径存入data中
                                Components.data.addAudioPathArr(Components.mRecords.getPath());
                                //输出录音小标识到文本***********
                                SpannableString spannableString = RichText.GetRecordSpannableString(Components.data.getAudioPathSize()-1, Components.data.getAudioPathArr(Components.data.getAudioPathSize()-1));
                                Components.ed_content.append(spannableString);
                                //Toast.makeText(PublicContext.getContext(), newAudioPath, Toast.LENGTH_SHORT).show();
                                Toast.makeText(PublicContext.getContext(), "录音保存成功！时长："+Components.mRecords.getSecond()+"s", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void doAfterDenied(String... permission) {
                        Components.mEmTvBtn.setHasRecordPromission(false);
                        Toast.makeText(PublicContext.getContext(), "请授权,否则无法录音", Toast.LENGTH_SHORT).show();
                    }
                }, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

}

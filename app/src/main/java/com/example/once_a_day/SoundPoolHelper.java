package com.example.once_a_day;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

//参考1：SoundPool的使用
public class SoundPoolHelper    //这个工具类实测解决了Android高版本不能正常播放资源文件的问题！并且调用一句代码即可new SoundPoolHelper(context).playSoundWithRedId(R.raw.gun);
{
    private SoundPool mainSoundPool;
    private AudioManager mainAudioManager;
    private float volume;
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 5;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private int soundId;
    private int resId;
    private Context mainContext;
    public SoundPoolHelper(Context context){
        this.mainContext=context;
    }

    //鎾斁
    public void playSoundWithRedId(int resId){
        this.resId=resId;
        init();
    }

    //init settings
    private void init(){
        // AudioManager audio settings for adjusting the volume 用于调整音量的AudioManager音频设置
        mainAudioManager = (AudioManager)this.mainContext. getSystemService(Context.AUDIO_SERVICE);

        // Current volumn Index of particular stream type. 特定流类型的当前体积索引
        float currentVolumeIndex = (float) mainAudioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type. 获取特定流类型的最大卷索引
        float maxVolumeIndex  = (float) mainAudioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1) 音量
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls. 建议使用硬件音量控制更改其音量的音频流。
        ((Activity)this.mainContext).setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.mainSoundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.mainSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When Sound Pool load complete. 当声音池加载完成时
        this.mainSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                playSound();
            }
        });

        //load res 加载资源
        this.soundId=this.mainSoundPool.load(this.mainContext,this.resId,1);
    }

    //play the sound res
    private void playSound(){
        float leftVolumn = volume;
        float rightVolumn = volume;
        // Play sound of gunfire. Returns the ID of the new stream.
        int streamId = this.mainSoundPool.play(this.soundId,leftVolumn, rightVolumn, 1, 0, 1f);
    }
    public void release(){
        resId=0;
    }

}

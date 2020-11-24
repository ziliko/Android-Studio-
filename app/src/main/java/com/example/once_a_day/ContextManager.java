package com.example.once_a_day;

import android.content.Context;

public class ContextManager {//参考2：SoundPool的使用 的辅助类
    private Context context;
    private PigSoundPlayer pigSoundPlayer;

    public ContextManager(Context context){
        this.context = context.getApplicationContext();
    }

    public Context getApplicationContext(){
        return context;
    }

    public void setContext(Context context){
        this.context  = context;
    }
}

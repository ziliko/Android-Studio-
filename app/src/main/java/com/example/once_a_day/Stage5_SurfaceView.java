package com.example.once_a_day;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;

import static android.content.Context.MODE_PRIVATE;

public class Stage5_SurfaceView extends Stage_Father{//父类只能一个,接口可以多个
    //首行，可变数值
    private static final String TAG = "Stage5_SurfaceView";
    private static Stage5_SurfaceView Stage5;

    //TODO(stage5特有)
    private int rotation=0;//控制雪花旋转
    static Bitmap  bbt_kind6,bbt_kind7;  //boss要素
    private static int debuff_ice;//冰冻负面效果  0时无 50时减速一半  100时冰冻  冰冻中被冰冻则破碎

    // 构造函数
    public Stage5_SurfaceView(Context context,AttributeSet attrs){
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg5);
        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss5);

        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1);
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);
        bbt_kind3 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3);
        bbt_kind3L = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3large);
        bbt_kind4=BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet4);
        bbt_kind4L=BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet4large);
        bbt_kind6 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet6);//懒惰就会
        bbt_kind7 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet7);//白给

        //预备动作这里画，伤害动作类里画?
        boss_skill00=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill50);//椭圆冰
        boss_skill01=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill51);//矩形冰

        this.initValue();//TODO 各关卡独有的初始化数值
        this.initSounds();//TODO 各关卡独有的背景音乐
        this.initTimerTask2();//TODO 各关卡独有的BOSS子弹风格
    }

    //所有变量的初始化
    private void initValue(){
        section=2;////各关的阶段数
        mylife=hp;//各关的自机初始血量 9999+ 为上帝模式 hp
        mypower=2;//各关的自机初始蓝量
        bosslife_all=2250*(1+dif*dif);//各关的BOSS初始血量
        BOSS.bosslife=bosslife_all;
        BOSS.control=1+(int)(Math.random()*2);////各关的BOSS初始移动方式   1 或2 的开局移动方式
        BOSS.speed=5;//各关的BOSS初始速度
    }
    //背景音乐定义
    private void initSounds() {
        mediaplayer = MediaPlayer.create(context, R.raw.music5);// *1为当前上下文，*2为音频资源编号
        mediaplayer.setLooping(true);    //设置循环播放
    }

    //定时器2：自动控制BOSS子弹+碰撞  多套逻辑？(移动+碰撞+清理+发射)
    private void initTimerTask2() {
        timer2 = new Timer();
        task2 = new TimerTask() {
            @Override
            public void run() {
                if(stop==0&&!isEnd){
                    if(!isSkill||skill!=3){//受技能3影响：时停
                        if(!isSkill||skill!=4) {//受技能4影响：定身
                            //boss移动
                            //BOSS.control=-1;
                            BOSS.move();
                        }

                        /*特殊情况碰撞检测函数3-玩家撞到BOSS直接判断死亡?
                        if(myY<BOSS.bossY+BOSS.bossHeight) {
                            int dx=(myX+myWidth/2)-(BOSS.bossX+BOSS.bossWidth/2);
                            int dy=(myY+myHeight/2)-(BOSS.bossY+BOSS.bossHeight/2);
                            if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))< BOSS.bossWidth/2) {soundId1=PigSoundPlayer.play_half("die",0);mylife=0;}
                        }
                        */

                        for(int i=0;i<bbtList.size();i++)
                        {
                            bossbullet bbullet=bbtList.get(i);
                            //if(bbullet.getWay()==3||bbullet.getWay()==4)
                            //因为不会重复，所以可以不用分类，直接试三个函数
                            bbullet.Move(time);       //移动
                            //else if(bbullet.getWay()==42||bbullet.getWay()==43)
                            bbullet.Move(myX+myWidth/2,myY+myHeight/2);//方式42 8
                            bbullet.Move();

                            int myarea;
                            if(isSkill) myarea=myWidth/2;//无敌撞弹当然要大范围啦
                            else myarea=(myWidth/4)*dex_area/100;

                            if(mylife>=0&&mylife<9999){                 //碰撞 9999为上帝模式
                                if (bbullet.crash(myX,myY,myWidth,myarea)) {
                                    if(!isSkill&&!isRebirth) {
                                        if(bbullet.is_ice()){
                                            //debuff_ice+=1;//因为碰撞不消失所以持续冰冻 1秒50层
                                            debuff_ice+=20;//改成碰撞会消失，并叠加减速，100则永冻(死亡) 会缓慢恢复
                                            if(debuff_ice>=100) {
                                                debuff_ice=0;
                                                mylife-=1;
                                                soundId1=PigSoundPlayer.play("ice_break",0);//死亡音效：冰破碎 ice_break
                                                //清空屏幕上所有子弹
                                                bbtList.clear();//全置NULL，较为耗时，但不易内存溢出？clear会执行循环将每一个坐标都设置为为null， 并设置数组的size为0。
                                                bbtList.trimToSize();//重置大小
                                                mybtList.clear();
                                                mybtList.trimToSize();
                                                if(mylife>=0) {
                                                    isRebirth=true;//重生入场
                                                    myX=screenWidth/2-myWidth/2;
                                                    myY=screenHeight;
                                                }
                                            }
                                            //else bbullet.jixu();//碰撞不消失，继续
                                        }
                                        //本关特有改动
                                        else{
                                            debuff_ice=0;
                                            mylife-=1;
                                            soundId1=PigSoundPlayer.play_half("die",0);//死亡音效
                                            //new SoundPoolHelper(context).playSoundWithRedId(R.raw.die);//死亡音效
                                            //清空屏幕上所有子弹
                                            bbtList.clear();//全置NULL，较为耗时，但不易内存溢出？clear会执行循环将每一个坐标都设置为为null， 并设置数组的size为0。
                                            bbtList.trimToSize();//重置大小
                                            mybtList.clear();
                                            mybtList.trimToSize();
                                            if(mylife>=0) {
                                                isRebirth=true;//重生入场
                                                myX=screenWidth/2-myWidth/2;
                                                myY=screenHeight;
                                            }
                                        }
                                    }
                                }
                                //擦弹得点(时停期间不得分)
                                else if(delay_garze>=25&&bbullet.garze(myX,myY,myWidth,myarea)){
                                    garze++;//0.5秒触发间隔
                                    delay_garze=0;
                                    soundId2 = PigSoundPlayer.play("garze",0);
                                    //new SoundPoolHelper(context).playSoundWithRedId(R.raw.garze);//擦弹音效
                                    if(garze%10==0&&mypower<8) mypower++;
                                }
                            }
                            if(mylife<0) {isEnd=true;bossdelay=0;}

                            if(bbtList.size()>0&&bbullet.end()) {                         //清理(不能并入碰撞，因为导致end原因有2种)
                                bbtList.remove(i);
                                i--;//删除一个后后面的全往前，size也立即-1
                            }
                        }
                        delay_garze++;
                        bossdelay+=20;
                        //boss自动发子弹   ※※※※此处选子弹※※※※
                        //i=BOSS.bossWidth/2;
                        //j=BOSS.bossHeight;//减少运算量? y=BOSS.bossY+j
                        k=(int)(Math.random()*100);
                        switch (section){
                            case 2://第一阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY 雪花飞舞 小雪花
                                        if (bossdelay>=1200) { //敌机子弹频度改这里
                                            if(k%3==2)         bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,6,bbt_kind3,3));//变子弹只需要改这里
                                            else if(k%3==1)    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,5,bbt_kind3,3));//变子弹只需要改这里
                                            else               bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,4,bbt_kind3,3));//变子弹只需要改这里
                                            bossdelay=0;
                                        }
                                        break;
                                    case 1://NORMAL 千里冰封 冰锥 散-聚 方式0
                                        if(bossdelay%2000==0){
                                            for(int n=0;n<360;n+=30) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,6,boss_skill01,0,n,"rectangle"));    //矩形旋转测试
                                        }
                                        if(bossdelay==8000) soundId6 = PigSoundPlayer.play_half("sword_go",0);//！预告
                                        if(bossdelay>=9000){
                                            for(int n=0;n<360;n+=60) bbtList.add(new bossbullet(myX+myWidth/2-(int)(screenWidth/2*Math.cos(Math.PI*n/180)),myY+myHeight/2-(int)(screenWidth/2*Math.sin(Math.PI*n/180)),screenWidth,screenHeight,4,boss_skill01,0,n,"rectangle"));    //矩形圆形内聚
                                            for(int n=30;n<360;n+=60) bbtList.add(new bossbullet(myX+myWidth/2-(int)(screenWidth*Math.cos(Math.PI*n/180)),myY+myHeight/2-(int)(screenWidth*Math.sin(Math.PI*n/180)),screenWidth,screenHeight,8,boss_skill01,0,n,"rectangle"));    //
                                            //200 100 173
                                            bossdelay =0;
                                        }
                                        break;
                                    case 2://HARD 冰雪封天
                                        if(bossdelay%2000==0){
                                            for(int n=0;n<180;n+=15)  bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,6,bbt_kind3,0,n));    //12方向扩散弹
                                        }
                                        if(bossdelay==9000) soundId6 = PigSoundPlayer.play_half("sword_go",0);//！预告
                                        if(bossdelay>=10000){
                                            for(int n=0;n<360;n+=30) bbtList.add(new bossbullet(myX+myWidth/2-(int)(screenWidth/2*Math.cos(Math.PI*n/180)),myY+myHeight/2-(int)(screenWidth/2*Math.sin(Math.PI*n/180)),screenWidth,screenHeight,4,boss_skill01,0,n,"rectangle"));    //
                                            for(int n=15;n<360;n+=30) bbtList.add(new bossbullet(myX+myWidth/2-(int)(screenWidth*Math.cos(Math.PI*n/180)),myY+myHeight/2-(int)(screenWidth*Math.sin(Math.PI*n/180)),screenWidth,screenHeight,8,boss_skill01,0,n,"rectangle"));    //
                                            //200 100 173
                                            bossdelay =0;
                                        }
                                        break;
                                    default:break;//0
                                }
                                break;

                            case 1://第二阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY 雪花飞舞 大雪花 +飘摇
                                        if (bossdelay>=1800) { //敌机子弹频度改这里
                                            //用了系统随机数  或者用size固定2比1
                                            //if((int)(Math.random()*100)%3==2) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+j,screenWidth,screenHeight,10,bbt_kind2));//变子弹只需要改这里
                                            if(k%3==2)         bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,6,bbt_kind3L,4));//变子弹只需要改这里
                                            else if(k%3==1)    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,5,bbt_kind3L,4));//变子弹只需要改这里
                                            else               bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,4,bbt_kind3L,4));//变子弹只需要改这里
                                            bossdelay=0;
                                            //postInvalidate();
                                        }break;
                                    case 1://NORMAL 千里冰封 冰剑高频高速瞄准追击
                                        if(bossdelay%200==0){
                                            //for(int n=0;n<360;n+=30) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,6,boss_skill04,0,n,"rectangle"));    //矩形旋转测试
                                            bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,0,bbt_kind4L,42,90,"rectangle"));
                                        }
                                        if(bossdelay%2000==0){//偷袭冰刺
                                            soundId6 = PigSoundPlayer.play_half("sword_go",0);
                                            //for(int n=0;n<360;n+=30) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,6,boss_skill04,0,n,"rectangle"));    //矩形旋转测试
                                            bbtList.add(new bossbullet((int)(screenWidth*Math.random()),BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,-20,boss_skill01,42,90,"rectangle"));
                                            bossdelay=0;
                                        }

                                        break;
                                    case 2://HARD 冰雪封天
                                        //全图随机雪花静止 50
                                        if(bossdelay%500==0&&bossdelay<2001) for(i=0;i<10;i++) bbtList.add(new bossbullet((int)(screenWidth*Math.random()),(int)(screenHeight*Math.random()),screenWidth,screenHeight,0,bbt_kind3,50));//
                                        if(bossdelay>6000&&bossdelay%400==0) {
                                            //随机方向剑神 左上 左下 右上 右下 (旋转一秒)  加速  （PS:因为一些原因，Y轴与自机平行时，子弹射速会快到离谱（需降低这种概率?））
                                            soundId6 = PigSoundPlayer.play_half("sword_go",0);//剑来！
                                            if(m>75)        for(i=0;i<5;i++) bbtList.add(new bossbullet((int)(screenWidth*(Math.random()*0.4+0.6)),(int)(screenHeight*(Math.random()*0.4+0.6)),screenWidth,screenHeight,-1,bbt_kind4,8,225,"rectangle"));//右下
                                            else if(m>50)   for(i=0;i<5;i++) bbtList.add(new bossbullet((int)(screenWidth*(Math.random()*0.4+0.6)),(int)(screenHeight*(Math.random()*0.4)),screenWidth,screenHeight,-1,bbt_kind4,8,135,"rectangle"));//右上
                                            else if(m>25)   for(i=0;i<5;i++) bbtList.add(new bossbullet((int)(screenWidth*(Math.random()*0.4)),(int)(screenHeight*(Math.random()*0.4+0.6)),screenWidth,screenHeight,-1,bbt_kind4,8,315,"rectangle"));//左下
                                            else            for(i=0;i<5;i++) bbtList.add(new bossbullet((int)(screenWidth*(Math.random()*0.4)),(int)(screenHeight*(Math.random()*0.4)),screenWidth,screenHeight,-1,bbt_kind4,8,45,"rectangle"));//左上
                                        }
                                        if(bossdelay>=8000){
                                            m=k;//局部随机数
                                            bossdelay =0;
                                        }
                                        break;
                                    default:break;//0
                                }
                                break;

                            case 0://第三阶段
                                switch (dif){
                                    case 0://EASY 雪花飞舞 大小雪花 飘摇+高速
                                        if (bossdelay>=800) { //敌机子弹频度改这里
                                            //用了系统随机数  或者用size固定2比1
                                            //if((int)(Math.random()*100)%3==2) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+j,screenWidth,screenHeight,10,bbt_kind2));//变子弹只需要改这里
                                            if(k%5==4)   bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,6,bbt_kind3L,4));//大雪花
                                            else if(k%5>1){//随机位置飘雪弹 下
                                                bbtList.add(new bossbullet((int)(Math.random()*screenWidth),boss.getHeight(),screenWidth,screenHeight,8,bbt_kind3,4));
                                                bbtList.add(new bossbullet((int)(Math.random()*screenWidth),boss.getHeight(),screenWidth,screenHeight,8,bbt_kind3,4));
                                                bbtList.add(new bossbullet((int)(Math.random()*screenWidth),boss.getHeight(),screenWidth,screenHeight,8,bbt_kind3,4));
                                            }
                                            else {//随机位置飘雪弹 上
                                                bbtList.add(new bossbullet((int)(Math.random()*screenWidth),0,screenWidth,screenHeight,8,bbt_kind3,4));
                                                bbtList.add(new bossbullet((int)(Math.random()*screenWidth),0,screenWidth,screenHeight,8,bbt_kind3,4));
                                                bbtList.add(new bossbullet((int)(Math.random()*screenWidth),0,screenWidth,screenHeight,8,bbt_kind3,4));
                                            }
                                            bossdelay=0;
                                            //postInvalidate();
                                        }break;
                                    case 1://NORMAL 千里冰封 冰锥坠 +冰牢
                                        if(bossdelay%1000==0){
                                            if(time>66&&BOSS.bosslife>bosslife_all*2/3){
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,0,boss_skill01,5,90,"rectangle"));//自由落体运动
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+myWidth/2,0,screenWidth,screenHeight,0,boss_skill01,5,90,"rectangle"));//自由落体运动
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-myWidth/2,0,screenWidth,screenHeight,0,boss_skill01,5,90,"rectangle"));//自由落体运动
                                            }
                                            else if(time>33&&BOSS.bosslife>bosslife_all/3){
                                                bbtList.add(new bossbullet(screenWidth*k/100,0,screenWidth,screenHeight,0,boss_skill01,5,90,"rectangle"));//自由落体运动
                                                bbtList.add(new bossbullet(screenWidth*k/100+myWidth/2,0,screenWidth,screenHeight,0,boss_skill01,5,90,"rectangle"));//自由落体运动
                                                bbtList.add(new bossbullet(screenWidth*k/100-myWidth/2,0,screenWidth,screenHeight,0,boss_skill01,5,90,"rectangle"));//自由落体运动
                                            }
                                            else {
                                                bbtList.add(new bossbullet(screenWidth*k/100,0,screenWidth,screenHeight,8,boss_skill01,0,90,"rectangle"));//自由落体运动
                                                bbtList.add(new bossbullet(screenWidth*k/100+myWidth,0,screenWidth,screenHeight,8,boss_skill01,0,90+k%20,"rectangle"));//自由落体运动
                                                bbtList.add(new bossbullet(screenWidth*k/100-myWidth,0,screenWidth,screenHeight,8,boss_skill01,0,90-k%20,"rectangle"));//自由落体运动
                                            }
                                        }
                                        if(bossdelay>=2000){
                                            i=15-i;
                                            for(int n=i;n<360+i;n+=30) bbtList.add(new bossbullet(myX+myWidth/2-(int)(bbt_kind4.getHeight()*Math.cos(Math.PI*n/180)),myY+myHeight/2-(int)(bbt_kind4.getHeight()*Math.sin(Math.PI*n/180)),screenWidth,screenHeight,-2,bbt_kind4,0,n,"rectangle"));
                                            bossdelay=0;
                                        }

                                        break;
                                    case 2://HARD 冰雪封天 全屏华丽 冰华盛 风雪起
                                        if(bossdelay%1000==0){
                                            for(int n=m;n<360+m;n+=45)  bbtList.add(new bossbullet(screenWidth/2,screenHeight/2-bbt_kind2.getHeight()/2,screenWidth,screenHeight,5,bbt_kind3,0,n));    //12方向扩散弹
                                            m-=5;//此关m用于记录旋转值
                                        }
                                        if(bossdelay%2000==0){
                                            //200 100 173
                                            soundId6 = PigSoundPlayer.play_half("sword_go",0);//剑来！
                                            for(int n=i;n<360+i;n+=10) bbtList.add(new bossbullet(myX+myWidth/2-(int)(bbt_kind4.getHeight()*Math.cos(Math.PI*n/180)),myY+myHeight/2-(int)(bbt_kind4.getHeight()*Math.sin(Math.PI*n/180)),screenWidth,screenHeight,-2,bbt_kind4,0,n,"rectangle"));
                                        }
                                        if(bossdelay%5000==0){
                                            if(time>66&&BOSS.bosslife>bosslife_all*2/3) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,0,screenWidth,screenHeight,0,bbt_kind6,5,90,"rectangle"));//自由落体懒惰就会
                                            else if(time>33&&BOSS.bosslife>bosslife_all/3) {
                                                bbtList.add(new bossbullet((int)(screenWidth*Math.random()),(int)(screenHeight/3*Math.random()),screenWidth,screenHeight,0,bbt_kind6,5,90,"rectangle"));//自由落体懒惰就会
                                                bbtList.add(new bossbullet((int)(screenWidth*Math.random()),(int)(screenHeight/3*Math.random()),screenWidth,screenHeight,0,bbt_kind6,5,90,"rectangle"));//自由落体懒惰就会
                                                bbtList.add(new bossbullet((int)(screenWidth*Math.random()),(int)(screenHeight/3*Math.random()),screenWidth,screenHeight,0,bbt_kind7,5,90,"rectangle"));//随机位置  自由落体懒惰白给
                                            }
                                            else for(int i=0;i<10;i++) bbtList.add(new bossbullet((int)(screenWidth*Math.random()),(int)(screenHeight/3*Math.random()),screenWidth,screenHeight,0,bbt_kind7,5,90,"rectangle"));//随机位置，大量白给
                                        }
                                        if(bossdelay>=10000) {bossdelay=0;debuff_ice+=40;soundId6 = PigSoundPlayer.play("wind",0);}//每10秒强行加40 并随机改变风？
                                        break;
                                    default:break;//0
                                }
                                break;

                            default:break;
                        }

                    }
                    //时停时依然要碰撞检测(手撕静止子弹?)
                    //Collsion_result2();//boss子弹检测
                    //自动解冻，每秒5(时停中也解冻？)
                    if (debuff_ice > 0 && rec_t % 10 == 0) debuff_ice--;
                }

            }
        };
        timer2.schedule(task2, 3000, 20);
    }

    public void mediaplayerDestroyed() {//内存回收涉及知识较多，重难点
        mediaplayer.stop();
        //player.reset();
        //问题 release(会导致page3闪退?)
        //player.release();//防止内存泄漏。用于结束游戏后释放，或者释放后换新的Mediaplayer?
        //player = null;
        //建议一旦不再使用MediaPlayer对象，立即调用release（），以便可以立即释放与MediaPlayer对象关联的内部播放器引擎使用的资源。资源可能包括单一资源（如硬件加速组件），没有调用release()可能导致后续的MediaPlayer实例回退到软件实现或完全失败。一旦MediaPlayer对象处于End状态，就无法再使用它，也无法将其恢复到任何其他状态。

    }

    //主函数
    protected void myDraw() {
        try {
            canvas = sfh.lockCanvas();   // 获取和锁定当前画布
            if (canvas != null) {
                canvas.drawBitmap(bg,null,new Rect(0, 0, screenWidth, screenHeight),paint);
                //背景之上的时停怀表
                if(skillflag==1&&skill==3) canvas.drawBitmap(skillPic, screenWidth/2-skillPic.getWidth()/2, 0, paint);
                if(isEnd) {
                    if(mylife<0) {//失败
                        //player.stop();
                        canvas.drawBitmap(gameover, screenWidth / 2 - gameover.getWidth() / 2, screenHeight / 2 - gameover.getHeight() / 2, paint);
                    }
                    else if(isEnd2) canvas.drawBitmap(tr2, screenWidth / 2 - tr2.getWidth() / 2, screenHeight / 2 - tr2.getHeight() / 2, paint);
                    else canvas.drawBitmap(tr1, screenWidth / 2 - tr1.getWidth() / 2, screenHeight / 2 - tr1.getHeight() / 2, paint);
                    //在按键中加入开箱随机函数并修改数据库+返回
                }
                else {
                    //先画我敌，这样可以判定我的点
                    if(isSkill) canvas.drawBitmap(me_skill, myX, myY, paint);
                    else canvas.drawBitmap(me, myX, myY, paint);
                    canvas.drawBitmap(boss, BOSS.bossX,BOSS.bossY, paint);
                    //if(isDrawboss_bt1) drawRotateBitmap(canvas, paint, bbt_kind3, rotation, boss_bt1X, boss_bt1Y);
                    for(int i=0;i<bbtList.size();i++)
                    {
                        bossbullet bbullet=bbtList.get(i);
                        //canvas.drawBitmap(bbullet.getBitmap(), (int)bbullet.getX(), (int)bbullet.getY(), paint);
                        if(bbullet.getWay()==3||bbullet.getWay()==4) drawRotateBitmap(canvas, paint, bbullet.getBitmap(), bbullet.getRotation_rand(), (int)bbullet.getX(), (int)bbullet.getY());//方式3 4  角度随机
                        else drawRotateBitmap(canvas, paint, bbullet.getBitmap(), bbullet.getRotation(), (int)bbullet.getX(), (int)bbullet.getY());//包括但不限于方式 0 8 42 50

                        //if(bbullet.getWay()==0||bbullet.getWay()==42||bbullet.getWay()==50) drawRotateBitmap(canvas, paint, bbullet.getBitmap(), bbullet.getRotation(), (int)bbullet.getX(), (int)bbullet.getY());//0 8 42 50
                        //else drawRotateBitmap(canvas, paint, bbullet.getBitmap(), bbullet.getRotation_rand(), (int)bbullet.getX(), (int)bbullet.getY());
                    }
                    //rotation++;if(rotation>=360) rotation=0;
                    for(int i=0;i<mybtList.size();i++)
                    {
                        mybullet mbullet=mybtList.get(i);
                        canvas.drawBitmap(mbullet.getBitmap(), mbullet.getX(), mbullet.getY(), paint);
                    }

                    canvas.drawBitmap(bosshp,null,new Rect(10, 10, screenWidth*BOSS.bosslife/bosslife_all-10, 30),paint);
                    for(int i=0;i<section;i++) canvas.drawBitmap(sections,10+sections.getWidth()*i,40,paint);
                    canvas.drawText(String.valueOf(time),screenWidth-100,100,paint);
                    canvas.drawLine(0, downline, screenWidth,downline, paint);//分界线
                    /*
                    canvas.drawText("HP",10,downline+downline_part,paint);//文字或许可以用图片？大小也好设置，位置
                    canvas.drawText("MP",10,downline+downline_part*2,paint);
                    canvas.drawText("graze",10,screenHeight-downline_part,paint);
                    canvas.drawText("score",10,screenHeight,paint);
                     */
                    canvas.drawBitmap(text_HP, 0,downline, paint);
                    canvas.drawBitmap(text_MP, 0,downline+downline_part, paint);
                    canvas.drawBitmap(text_garze, 0,downline+downline_part*2, paint);
                    canvas.drawBitmap(text_score, 0,downline+downline_part*3, paint);
                    for(int i=0;i<8;i++) {
                        if(mylife>i) canvas.drawBitmap(myhp, text_HP.getWidth()+myhp.getWidth()*i,downline+downline_part-myhp.getHeight(), paint);
                        else canvas.drawBitmap(myhp_empty, text_HP.getWidth()+myhp.getWidth()*i,downline+downline_part-myhp.getHeight(), paint);
                    }
                    for(int i=0;i<8;i++) {
                        if(mypower>i) canvas.drawBitmap(mymp, text_HP.getWidth()+myhp.getWidth()*i,downline+downline_part*2-mymp.getWidth(), paint);
                        else canvas.drawBitmap(mymp_empty, text_HP.getWidth()+myhp.getWidth()*i,downline+downline_part*2-mymp.getWidth(), paint);
                    }
                    canvas.drawText(String.valueOf(garze),text_HP.getWidth(),downline+downline_part*3-20,paint);
                    canvas.drawText(String.valueOf("ice:"+debuff_ice),screenWidth/2,downline+downline_part*3-20,paint);//特有，显示冰冻Debuff
                    //canvas.drawBitmap(pic1, 0, downline, paint);
                    //canvas.drawBitmap(pic2, screenWidth - pic2.getWidth()-50, screenHeight - pic2.getHeight()-50, paint);
                    if(skillflag==1) {
                        if (skill < 3) canvas.drawBitmap(skillPic, myX + myWidth / 2 - skillPic.getWidth() / 2, myY + me.getHeight() / 2 - skillPic.getHeight(), paint);
                        else if (skill == 4) canvas.drawBitmap(skillPic,null,new Rect(0,0,screenWidth,screenWidth) , paint);
                    }
                    //自旋解决：使用Canvas的drawBitmap(Bitmap bitmap,Matrix matrix,Paint paint)方法，最重要的就是定制Matrix。
                    //canvas.rotate(30);//旋转功能以画布旋转，较复杂
                    //canvas.drawBitmap(me, myX, myY, paint);
                    //canvas.rotate(-90);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (canvas != null) {
                sfh.unlockCanvasAndPost(canvas);    // 解锁当前画布
            }
        }
    }

    //按键检测函数 TODO 重写 （此关独有）
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int)event.getX();
        int touchY = (int)event.getY();
        //System.out.println(touchX + ", " + touchY);
        if(isEnd) {
            if(mylife>=0&&delay_clock>=50){//延时1秒防止秒开箱
                isEnd2= true;
                //mediaplayerDestroyed();
                if(mylife>=9999) rec=0;//上帝模式无奖励
                else rec=rec_t%20+1;//开箱并随机抽奖+修改数据库 加一用于区别退出和无奖
            }
        }
        else if(stop==0&&!isRebirth&&debuff_ice<100){//重生动画中不可控制
            // 被冰冻debuff不可控制（此关独有）
            switch (event.getAction() & MotionEvent.ACTION_MASK){// 进行与操作是为了判断多点触摸
                // 第一个手指按下事件
                case MotionEvent.ACTION_DOWN:
                    touchstartX=touchX;
                    touchstartY=touchY;
                    touchmoveX=touchX-myX;
                    touchmoveY=touchY-myY;
                    break;
                // 第二个手指按下事件
                case MotionEvent.ACTION_POINTER_DOWN:
                    touchmoveX=touchX-myX;//加入这两句可以削弱多指时闪现，不能连续发动?
                    touchmoveY=touchY-myY;
                    //touchstartX=touchX-myX;//加入这两句可以削弱多指时闪现，不能连续发动?
                    //touchstartY=touchY-myY;
                    if(!isSkill&&mypower>0&&delay_skill>0){//延时
                        mypower-=1;
                        isSkill=true;//标志技能开始
                        delay_skill=0;//重置技能定时器计数值
                        //在定时器中关闭技能
                        //postInvalidate();
                    }
                    break;
                // 手指滑动事件
                case MotionEvent.ACTION_MOVE:
                    //受冰冻debuff控制（此关独有）
                    myX=touchstartX+(touchX-touchstartX)*sensitivity/100*(100-debuff_ice)/100-touchmoveX;
                    myY=touchstartY+(touchY-touchstartY)*sensitivity/100*(100-debuff_ice)/100-touchmoveY;
                    //myX=(touchX-touchstartX)*sensitivity/100;
                    //myY=(touchY-touchstartY)*sensitivity/100;
                    if (myX < 0) myX = 0;
                    else if (myX > screenWidth - myWidth) myX = screenWidth - myWidth;//边界处理
                    if (myY < 0) myY = 0;
                    else if (myY > downline - myHeight/2) myY = downline - myHeight/2;//边界处理
                    break;
                // 手指放开事件
                case MotionEvent.ACTION_UP:
                //多点按住，松开其中一个点时触发（即非最后一个点被放开时）
                case MotionEvent.ACTION_POINTER_UP:

                    break;
            }
        }
        //return super.onTouchEvent(event);
        return true;
    }


    //转阶段函数 各关卡不同
    protected void section_change(){
        m=0;
        bossdelay=0;
        section--;
        soundId4=PigSoundPlayer.play("section_change",0);
        BOSS.bosslife=bosslife_all;
        if(section==0){
            if(dif==2) BOSS.speed=1;//终末阶段减速
            time=99;
        }
        else time=45+15*dif;
        //清空屏幕上所有子弹
        bbtList.clear();//可设置为一个得分点，根据清除的数量
        bbtList.trimToSize();//
        //mybtList.clear();//会导致下方end函数闪退
        //mybtList.trimToSize();
    }
    /*
            else if (upArea.contains(touchX, touchY)) {//没有条件相当于包含了dowm和move和up?   此时else只剩下dowm了?
                if (Area_half.contains(touchX, touchY)) myY -= stepSize;
                else myY -= stepSize*2;
                if (myY < 0) {
                    myY = 0;
                }
                //postInvalidate();
            }
            else if (leftArea.contains(touchX, touchY)) {
                if (Area_half.contains(touchX, touchY)) myX -= stepSize;
                else myX -= stepSize*2;
                if (myX < 0) {
                    myX = 0;
                }
                //postInvalidate();
            }
            else if (rightArea.contains(touchX, touchY)) {
                if (Area_half.contains(touchX, touchY)) myX += stepSize;
                else myX += stepSize*2;
                if (myX + myWidth > screenWidth) {
                    myX = screenWidth - myWidth;
                }
                //postInvalidate();
            }
            else if (downArea.contains(touchX, touchY)) {
                if (Area_half.contains(touchX, touchY)) myY += stepSize;
                else myY += stepSize*2;
                if (myY + myHeight > downline) {
                    myY = downline - myHeight;
                }
                //postInvalidate();
            }
            */
            /*手动开火，已改成手动开技能
            if (buttonArea.contains(touchX, touchY)&&!isSkill) { //已经在施展一个技能的时候不能重叠使用
                if(mypower>0&&delay_skill>0){//延时
                    mypower-=1;
                    isSkill=true;//标志技能开始
                    delay_skill=0;//重置技能定时器计数值
                    //在定时器中关闭技能
                    //postInvalidate();
                }
            }*/

    /*
    //碰撞结算函数
    private void Collsion_result1(){
        if(BOSS.bosslife>0){
            if (isDrawBullet1&&isCollsion1(bt1X,bt1Y)) {
                isDrawBullet1=false;//子弹提前消失
                if(isSkill&&skill==3) timestopkill+=str;
                else BOSS.bosslife-=str;
            }
            if (isDrawBullet2&&isCollsion1(bt2X,bt2Y)) {
                isDrawBullet2=false;//子弹提前消失
                if(isSkill&&skill==3) timestopkill+=str;
                else BOSS.bosslife-=str;
            }
            if (isDrawBullet3&&isCollsion1(bt3X,bt3Y)) {
                isDrawBullet3=false;//子弹提前消失
                if(isSkill&&skill==3) timestopkill+=str;
                else BOSS.bosslife-=str;
            }
            if (isDrawBullet4&&isCollsion1(bt4X,bt4Y)) {
                isDrawBullet4=false;//子弹提前消失
                if(isSkill&&skill==3) timestopkill+=str;
                else BOSS.bosslife-=str;
            }
            if (isDrawBullet5&&isCollsion1(bt5X,bt5Y)) {
                isDrawBullet5=false;//子弹提前消失
                if(isSkill&&skill==3) timestopkill+=str;
                else BOSS.bosslife-=str;
            }
            if(BOSS.bosslife<=0) {isEnd=true;mydelay=0;delay_clock=0;}
        }
        else {isEnd=true;mydelay=0;delay_clock=0;}
    }
    private void Collsion_result2(){
        if(mylife>=0){
            if(myY<BOSS.bossY+BOSS.bossHeight) {//特殊情况碰撞检测函数3-玩家撞到BOSS直接判断死亡
                int dx=(myX+myWidth/2)-(BOSS.bossX+BOSS.bossWidth/2);
                int dy=(myY+myHeight/2)-(BOSS.bossY+BOSS.bossHeight/2);
                if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))< BOSS.bossWidth/2) mylife=0;
            }
            else{
                if (isDrawboss_bt1&&isCollsion2(boss_bt1X,boss_bt1Y,bbt_kind3)) {
                    isDrawboss_bt1=false;//子弹提前消失
                    if(!isSkill) mylife-=1;
                }
                if (isDrawboss_bt2&&isCollsion2(boss_bt2X,boss_bt2Y,bbt_kind3)) {
                    isDrawboss_bt2=false;//子弹提前消失
                    if(!isSkill) mylife-=1;
                }
                if (isDrawboss_bt3&&isCollsion2(boss_bt3X,boss_bt3Y,bbt_kind3)) {
                    isDrawboss_bt3=false;//子弹提前消失
                    if(!isSkill) mylife-=1;
                }
                if (isDrawboss_bt4&&isCollsion2(boss_bt4X,boss_bt4Y,bbt_kind3)) {
                    isDrawboss_bt4=false;//子弹提前消失
                    if(!isSkill) mylife-=1;
                }
                if (isDrawboss_bt5&&isCollsion2(boss_bt5X,boss_bt5Y,bbt_kind3)) {
                    isDrawboss_bt5=false;//子弹提前消失
                    if(!isSkill) mylife-=1;
                }
            }
            if(mylife<0) {isEnd=true;bossdelay=0;}
        }
    }
    */

    /*
    //碰撞检测函数-命中BOSS-同时作为单发游戏结束变量-或者另设10血判断变量
    private boolean isCollsion1(int btX,int btY) {
        int dx,dy;
        dx=(BOSS.bossX+BOSS.bossWidth/2)-(btX+mybt.getWidth()/2);
        dy=(BOSS.bossY+BOSS.bossHeight/2)-(btY+mybt.getHeight()/2);
        //变量1：BOSS.bossX+BOSS.bossWidth/2; BOSS.bossY+BOSS.bossHeight/2;
        //变量2：mybtX+mybt.getWidth()/2; mybtY+mybt.getHeight()/2;
        if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))<= BOSS.bossHeight/2) {
            btX=0;//不重置则一发毙命
            btY=0;
            return true;
        }
        else return false;
    }
    //碰撞检测函数2-命中玩家-同时作为单发游戏结束变量-或者另设10血判断变量
    private boolean isCollsion2(int btX,int btY,Bitmap kind) {
        int dx,dy,myarea;
        dx=(myX+myWidth/2)-(btX+kind.getWidth()/2);
        dy=(myY+myHeight/2)-(btY+kind.getHeight()/2);
        if(isSkill) myarea=myWidth/2;//无敌撞弹当然要大范围啦
        else myarea=(myWidth/4)*dex_area/100;
        //为削弱雪花难度将人物判定点改成最中心即去掉myWidth/2
        if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))<= myarea+kind.getWidth()/2) {//涉及玩家判定点
            btX=0;//不重置则一发毙命
            btY=0;
            return true;
        }
        else return false;
    }
     */

}

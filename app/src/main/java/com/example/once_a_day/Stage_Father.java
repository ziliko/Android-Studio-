package com.example.once_a_day;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

public class Stage_Father extends SurfaceView implements SurfaceHolder.Callback, Runnable{//父类只能一个,接口可以多个
    private static final String TAG = "Stage_Father";
    //BOSS血量改动暂时无效，要在后面初始化里改
    int section;//一关分成3部分
    int bosslife_all;//
    BOSS BOSS;
    //int bosslife;//
    int mylife;//
    int mypower;//
    int stepSize;//方向盘控制时期，用于控制每按一次移动的距离，已废弃
    //int speed;//boss移动速度

    //受装备加成
    int player;  //自机角色 0 1 2 3
    int dif;  //难度等级 0 1 2
    int str;       //力量，已加入函数
    int hp;          //(和hp已有冲突)
    int dex;         //灵巧，已加入函数
    int dex_area;  //灵巧，已加入函数
    int skill;        //
    int sensitivity;//触屏灵敏度

    int bggundong;//控制背景滚动
    double Alpha=0; //设置透明程度，开始游戏时渐变至不透明 0~255
    public static int rec;//返回数据
    public static int stop;//控制暂停
    int rec_t;
    public static boolean flag;//或者用flag控制暂停？
    public static boolean isEnd;
    public static boolean isEnd2;//检测是否已经开箱领取奖励
    Thread th;//单独刷屏线程
    SurfaceHolder sfh;
    Canvas canvas;
    Paint paint;
    static final long REFRESH_INTERVAL = 20;//刷新率：如果要60FPS则应设为17s

    static Context context;
    static MediaPlayer mediaplayer;     //背景音乐的类
    //private SoundPool pool;         //游戏音效的类
    //private Rect region4, region5;//安卓5.0以上禁用，另寻出路
    int soundId1, soundId2, soundId3, soundId4, soundId5, soundId6;

    static Bitmap pic1, pic2,bg,tr1,tr2,gameover,sections;             //场景要素
    static Bitmap text_HP,text_MP,text_garze,text_score;      //场景文字
    static Bitmap me,me_skill,mybt,myhp,myhp_empty,mymp,mymp_empty;                 //玩家要素
    static Bitmap boss,bosshp,
            bbt_kind1,bbt_kind2,bbt_kind3,bbt_kind4,bbt_kind5,
            bbt_kind1L,bbt_kind2L,bbt_kind3L,bbt_kind4L,bbt_kind5L;  //boss要素

    //对应技能下不同刀光，备用30张图
    static Bitmap skill_00,skill_01,skill_02,skill_03,skill_04,skill_05,skill_06,skill_07,skill_08,skill_09,skill_10,
            skill_11,skill_12,skill_13,skill_14,skill_15,skill_16,skill_17,skill_18,skill_19,skill_20,
            skill_21,skill_22,skill_23,skill_24,skill_25,skill_26,skill_27,skill_28,skill_29,skill_30;
    static Bitmap boss_skill00,boss_skill01,boss_skill02,boss_skill03,boss_skill04,
            boss_skill00L,boss_skill01L,boss_skill02L,boss_skill03L,boss_skill04L;

    int screenWidth = 0;
    int screenHeight = 0;
    int myWidth = 0;
    int myHeight = 0;
    //int BOSS.bossWidth = 0;
    //int BOSS.bossHeight = 0;
    int downline = 0;//下界分界线Y轴坐标
    int downline_part = 0;//将线下面区域四等分

    //int BOSS.bossX = -1;//控制boss -1用于仅一次的开场初始化部分
    //int BOSS.bossY = -1;//
    //int BOSS.control = 0;//控制BOSS移动模式 0静止 1左 2右 3随机闪现？(超出范围默认静止)

    int touchstartX = 0;//触屏控制起始位置
    int touchstartY = 0;//
    int touchmoveX = 0;//触屏控制相对起始位置变化量
    int touchmoveY = 0;//
    int myX = 0;//控制玩家
    int myY = 0;//

    Rect upArea = null;
    Rect leftArea = null;
    Rect rightArea = null;
    Rect downArea = null;
    Rect Area_half = null;//用来切割区域变成8种操控
    Rect buttonArea=null;

    boolean isSkill=false;//代表当前是否使用了技能
    boolean isRebirth=false;//代表当前是否死亡重生
    static Timer timer;//用于控制自机
    static TimerTask task;
    static Timer timer2;//用于控制敌机
    static TimerTask task2;
    static Timer timer3;//用于释放技能
    static TimerTask task3;

    static List<Bitmap> pics;//※※※※集合※※※※
    static ArrayList<mybullet> mybtList;//※※※※动态数组与泛型？※※※※
    static ArrayList<bossbullet> bbtList;
    static int curIndex;       //实现分帧动画
    static Bitmap skillPic;    //动画对象
    static int skillflag;      //动画变量
    static int timestopkill;   //结算时停后的boss血量

    int mydelay;
    int bossdelay;
    int delay_skill;//游戏中用于使用技能如时停n秒，无敌n秒
    int delay_damage;//技能中独立延时函数，用于计算伤害
    int delay_clock;//游戏中用于倒计时-1；游戏后用于延迟1秒开箱
    int delay_rebirth;//掉残重生2秒延时
    int delay_garze;//擦弹间隔延时
    int i,j,k,k2,k3,m;//i,j变量 k作为上层随机数 m作为下层局部随机数
    int time;//阶段倒计时
    int garze;//擦弹得点

    /*———————————————————————————————————————————————————分割线———————————————————————————————————————————————————*/
    //初始化部分

    public Stage_Father(Context context, AttributeSet attrs) {
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;
        sfh = getHolder();      // 获取SurfaceHolder对象
        sfh.addCallback(this);  // 绑定Callback监听器
        paint = new Paint();
        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg1);//TODO
        pic1 = BitmapFactory.decodeResource(getResources(), R.drawable.controller);
        pic2 = BitmapFactory.decodeResource(getResources(), R.drawable.button);

        mybt = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
        myhp = BitmapFactory.decodeResource(getResources(), R.drawable.myhp);
        myhp_empty = BitmapFactory.decodeResource(getResources(), R.drawable.myhp_empty);
        mymp = BitmapFactory.decodeResource(getResources(), R.drawable.mymp);
        mymp_empty = BitmapFactory.decodeResource(getResources(), R.drawable.mymp_empty);
        text_HP=BitmapFactory.decodeResource(getResources(), R.drawable.text_hp);
        text_MP=BitmapFactory.decodeResource(getResources(), R.drawable.text_mp);
        text_garze=BitmapFactory.decodeResource(getResources(), R.drawable.text_graze);
        text_score=BitmapFactory.decodeResource(getResources(), R.drawable.text_score);

        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss1);//TODO
        bosshp = BitmapFactory.decodeResource(getResources(), R.drawable.bosshp);
        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1_yellow);//TODO
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);//TODO
        bbt_kind3 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3);//TODO

        tr1 = BitmapFactory.decodeResource(getResources(), R.drawable.treasure1);
        tr2 = BitmapFactory.decodeResource(getResources(), R.drawable.treasure2);
        sections=BitmapFactory.decodeResource(getResources(), R.drawable.section);
        gameover= BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
        setFocusable(true);//控制键盘是否可以获得这个按钮的焦点?

        initValue();//所有变量的初始化

        if(player==3) me = BitmapFactory.decodeResource(getResources(), R.drawable.player3);
        else if(player==2) me = BitmapFactory.decodeResource(getResources(), R.drawable.player2);
        else if(player==1) me = BitmapFactory.decodeResource(getResources(), R.drawable.player1);
        else me = BitmapFactory.decodeResource(getResources(), R.drawable.player0);

        if(skill==0) me_skill = BitmapFactory.decodeResource(getResources(), R.drawable.me_skill0);
        else if(skill==1){
            me_skill = BitmapFactory.decodeResource(getResources(), R.drawable.me_skill1);
            skill_00 = BitmapFactory.decodeResource(getResources(), R.drawable.skill1_00);
            skill_01 = BitmapFactory.decodeResource(getResources(), R.drawable.skill1_01);
            skill_02 = BitmapFactory.decodeResource(getResources(), R.drawable.skill1_02);
            skill_03 = BitmapFactory.decodeResource(getResources(), R.drawable.skill1_03);
            skill_04 = BitmapFactory.decodeResource(getResources(), R.drawable.skill1_04);
            pics.add(skill_00);pics.add(skill_01);pics.add(skill_02);pics.add(skill_03);pics.add(skill_04);
        }
        else if(skill==2){
            me_skill = BitmapFactory.decodeResource(getResources(), R.drawable.me_skill2);
            skill_00 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_00);
            skill_01 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_01);
            skill_02 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_02);
            skill_03 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_03);
            skill_04 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_04);
            skill_05 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_05);
            skill_06 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_06);
            skill_07 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_07);
            skill_08 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_08);
            skill_09 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_09);
            skill_10 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_10);
            skill_11 = BitmapFactory.decodeResource(getResources(), R.drawable.skill2_11);
            pics.add(skill_00);pics.add(skill_01);pics.add(skill_02);pics.add(skill_03);pics.add(skill_04);pics.add(skill_05);
            pics.add(skill_06);pics.add(skill_07);pics.add(skill_08);pics.add(skill_09);pics.add(skill_10);pics.add(skill_11);
        }
        else if(skill==3){
            me_skill = BitmapFactory.decodeResource(getResources(), R.drawable.me_skill3);
            skill_00 = BitmapFactory.decodeResource(getResources(), R.drawable.skill3_00);
            skill_01 = BitmapFactory.decodeResource(getResources(), R.drawable.skill3_01);
            skill_02 = BitmapFactory.decodeResource(getResources(), R.drawable.skill3_02);
            skill_03 = BitmapFactory.decodeResource(getResources(), R.drawable.skill3_03);
            skill_04 = BitmapFactory.decodeResource(getResources(), R.drawable.skill3_04);
            skill_05 = BitmapFactory.decodeResource(getResources(), R.drawable.skill3_05);
            pics.add(skill_00);pics.add(skill_01);pics.add(skill_02);pics.add(skill_03);pics.add(skill_04);pics.add(skill_05);
        }
        else if(skill==4){
            me_skill = BitmapFactory.decodeResource(getResources(), R.drawable.me_skill4);
            skill_00 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_00);
            skill_01 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_01);
            skill_02 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_02);
            skill_03 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_03);
            skill_04 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_04);
            skill_05 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_05);
            skill_06 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_06);
            skill_07 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_07);
            skill_08 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_08);
            skill_09 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_09);
            skill_10 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_10);
            skill_11 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_11);
            skill_12 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_12);
            skill_13 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_13);
            skill_14 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_14);
            skill_15 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_15);
            skill_16 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_16);
            skill_17 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_17);
            skill_18 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_18);
            skill_19 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_19);
            skill_20 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_20);
            skill_21 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_21);
            skill_22 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_22);
            skill_23 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_23);
            skill_24 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_24);
            skill_25 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_25);
            skill_26 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_26);
            skill_27 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_27);
            skill_28 = BitmapFactory.decodeResource(getResources(), R.drawable.skill4_28);
            pics.add(skill_00);pics.add(skill_01);pics.add(skill_02);pics.add(skill_03);pics.add(skill_04);
            pics.add(skill_05);pics.add(skill_06);pics.add(skill_07);pics.add(skill_08);pics.add(skill_09);
            pics.add(skill_10);pics.add(skill_11);pics.add(skill_12);pics.add(skill_13);pics.add(skill_14);
            pics.add(skill_15);pics.add(skill_16);pics.add(skill_17);pics.add(skill_18);pics.add(skill_19);
            pics.add(skill_20);pics.add(skill_21);pics.add(skill_22);pics.add(skill_23);pics.add(skill_24);
            pics.add(skill_25);pics.add(skill_26);pics.add(skill_27);pics.add(skill_28);
        }
        //要减少加载压力，根据skill加载

        this.initTimerTask1();
        this.initTimerTask3();
    }

    /*———————————————————————————————————————————————————分割线———————————————————————————————————————————————————*/
    //一些初始化函数

    //所有变量的初始化
    private void initValue(){
        SharedPreferences sp=context.getSharedPreferences("zks", MODE_PRIVATE);

        player=sp.getInt("player", 0);
        dif=sp.getInt("dif", 0);
        str=sp.getInt("str", 10);
        dex=sp.getInt("dex", 6);
        dex_area=sp.getInt("dex_area", 100);
        hp=sp.getInt("hp", 2);
        skill=sp.getInt("skill", 0);
        sensitivity=sp.getInt("sensitivity", 100);

        section=2;
        mylife=hp;//9999+ 为上帝模式 hp
        mypower=2;
        bosslife_all=2400*(1+dif*2);
        BOSS=new BOSS();
        BOSS.bosslife=bosslife_all;
        BOSS.control=1+(int)(Math.random()*2);//1 或2 的开局移动方式

        bggundong=0;//控制背景滚动
        rec=0;//返回数据
        stop=0;//控制暂停
        rec_t=0;
        flag=false;//或者用flag控制暂停？
        isEnd=false;
        isEnd2=false;

        screenWidth = 0;
        screenHeight = 0;
        myWidth = 0;
        myHeight = 0;
        BOSS.bossWidth = 0;
        BOSS.bossHeight = 0;
        downline = 0;//下界分界线Y轴坐标
        downline_part = 0;//将线下面区域四等分
        BOSS.bossX = -1;//控制boss -1用于仅一次的开场初始化部分
        BOSS.bossY = -1;//
        touchstartX = 0;//触屏控制起始位置
        touchstartY = 0;//
        myX = 0;//控制玩家
        myY = 0;//

        timer=null;//
        task=null;
        timer2=null;//
        task2=null;
        timer3=null;//
        task3=null;
        isSkill=false;//代表当前是否使用了技能
        isRebirth=false;//代表当前是否死亡重生

        pics = new ArrayList<Bitmap>();//※※※※集合※※※※
        mybtList = new ArrayList<mybullet>();//※※※※动态数组与泛型？※※※※
        bbtList = new ArrayList<bossbullet>();

        mydelay=0;
        bossdelay=0;
        delay_skill=0;//游戏中用于使用技能如时停n秒，无敌n秒
        delay_damage=0;//技能中独立延时函数，用于计算伤害
        delay_clock=0;//游戏中用于倒计时-1；游戏后用于延迟1秒开箱
        delay_rebirth=0;//掉残重生2秒延时
        delay_garze=0;//擦弹间隔延时
        time=45+15*dif;;//阶段倒计时
        garze=0;//擦弹得点
        i=0;j=0;k=0;m=0;//全局变量初始化
    }

    //定时器1：控制自机子弹+碰撞计算 (移动+碰撞+清理+发射)
    protected void initTimerTask1() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                if(stop==0&&!isEnd){
                    if(isRebirth){
                        delay_rebirth++;
                        if(delay_rebirth>100) {isRebirth=false;delay_rebirth=0;} //2秒
                        else {
                            if(delay_rebirth%10==0) {bbtList.clear();bbtList.trimToSize();}//清空敌弹
                            if(delay_rebirth>=50) myY-=(screenHeight-downline+myHeight)/40;//自机向上move入场 总Y路程=screemH-dielineH+myH
                        }
                    }
                    else{
                        for(int i=0;i<mybtList.size();i++)
                        {
                            mybullet mbullet=mybtList.get(i);
                            mbullet.Move();                             //移动

                            if(BOSS.bosslife>0){                             //碰撞
                                if(mbullet.crash(BOSS.bossX,BOSS.bossY,BOSS.bossWidth)){
                                    if(isSkill&&skill==3) timestopkill+=str;
                                    else BOSS.bosslife-=str;
                                }
                            }
                            if(BOSS.bosslife<=0) {   //转阶段判断处1
                                if(section>0) section_change();
                                else if(!isEnd){           //游戏结束判定处
                                    soundId3 = PigSoundPlayer.play("win",0.3f,0.3f,1,0,1.0f);
                                    isEnd=true;
                                    mydelay=0;
                                    delay_clock=0;
                                }
                            }

                            if(mbullet.end()) {                         //清理(不能并入碰撞，因为导致end原因有2种)
                                mybtList.remove(i);
                                i--;//删除一个后后面的全往前，size也立即-1
                            }
                        }
                        mydelay+=20;
                        //玩家自动发子弹
                        if (mydelay>=1200/dex) { //子弹最短400ms间隔 400(3颗)->240(5颗)(现在已经没有这个限制)
                            switch (player){
                                case 3://锯白毛   画笔模式(3秒后开始移动) 三倍ice crean!（反正你打不着）
                                    mybtList.add(new mybullet(myX + myWidth/2,myY,dex,mybt,270,1));
                                    mybtList.add(new mybullet(myX + myWidth/2,myY,dex,mybt,270,1));
                                    mybtList.add(new mybullet(myX + myWidth/2,myY,dex,mybt,270,1));
                                    break;
                                case 2://紫毛  分叉--(进化！爱心型) 左-1 右1
                                    mybtList.add(new mybullet(myX + myWidth/2,myY,dex,screenWidth,screenHeight,mybt,-1));
                                    mybtList.add(new mybullet(myX + myWidth/2,myY,dex,screenWidth,screenHeight,mybt,1));
                                    break;
                                case 1://蓝毛  扩散3排
                                    mybtList.add(new mybullet(myX ,myY,dex,mybt,260));
                                    mybtList.add(new mybullet(myX + myWidth/2,myY,dex,mybt,270));
                                    mybtList.add(new mybullet(myX + myWidth,myY,dex,mybt,280));
                                    break;
                                case 0://白毛  直线2排
                                    mybtList.add(new mybullet(myX + myWidth/3,myY,dex,mybt));
                                    mybtList.add(new mybullet(myX + myWidth*2/3,myY,dex,mybt));
                                    break;
                                default:break;
                            }
                            mydelay = 0;
                            //postInvalidate();
                        }
                    }

                }
                //隔20秒重置的随机数
                rec_t++;//随机数独立x
                if(rec_t>=1000) rec_t=0;
                //delay_skill受暂停影响
                if(stop==0) delay_skill++;//随机数独立x
                if(isSkill&&delay_skill>=50) {//关闭技能
                    switch (skill){
                        case 0:case 1:  {if(delay_skill>=50) isSkill=false;break;}
                        case 2:         {if(delay_skill>=100) isSkill=false;break;}
                        case 3:case 4:  {if(delay_skill>=150) isSkill=false;break;}
                    }
                }
                if(delay_skill>=1000) delay_skill=0;
                //isEnd;stop;delay>=50  结束前受暂停影响，结束后不受暂停影响
                if(!isEnd){
                    if(stop==0) {
                        if(isSkill&&skill==3) ;//受时停影响
                        else delay_clock++;// 独立延时数：游戏结束前用于1秒倒计时 50=1s
                        if (!isEnd && delay_clock >= 50) {
                            delay_clock = 0;
                            if(time>0) time--;
                            else {//转阶段判断处2
                                //BOSS.bosslife=0;//没必要，且最后阶段无计时(或者99)
                                if(section>0) section_change();
                            }

                        }
                    }
                }
                else delay_clock++;//独立延时数：游戏结束后用于延迟1秒开箱 50=1s
            }
        };
        timer.schedule(task, 3000, 20);
    }

    //定时器3：控制自机技能动画效果和伤害效果 以及一些其他的变量
    private void initTimerTask3() {
        timer3 = new Timer();
        task3 = new TimerTask() {
            @Override
            public void run() {
                //控制音乐暂停/继续
                if(stop==1&&mediaplayer.isPlaying()) mediaplayer.pause();
                if(stop==0&&!mediaplayer.isPlaying()) mediaplayer.start();
                //控制技能
                if (stop == 0&&isSkill) {
                    if(mylife>9999) {BOSS.bosslife=0;isSkill = false;}//上帝模式开技能则秒杀
                    else{
                        delay_damage++;
                        //if(skillflag>1) {skillflag=0;curIndex++;}//curIndex++需要注意溢出
                        switch(skill) {
                            case 0:break;
                            case 1:{
                                //delay_damage 10 20 30 40 50;55 60 65 70
                                if(delay_damage==1) soundId5=PigSoundPlayer.play_half("skill1",0);
                                else if(delay_damage>=30&&skillflag==0) {skillflag = 1;curIndex = 0;skillPic=pics.get(curIndex);}
                                else if (skillflag == 1) {
                                    //拉刀光0-4 0-50
                                    if(delay_damage==44) {curIndex = 4;/*if(进入范围)*/ BOSS.bosslife-=str*5;}//伤害判定(仅一次)
                                    else if(delay_damage==39) curIndex = 3;//
                                    else if(delay_damage==35) curIndex = 2;//
                                    else if(delay_damage==32) curIndex = 1;//
                                    skillPic=pics.get(curIndex);
                                }
                                break;
                            }
                            case 2:{
                                if(delay_damage>=50&&skillflag==0) {skillflag = 1;curIndex = 0;skillPic=pics.get(curIndex);}
                                else if (skillflag == 1) {
                                    //拉刀光0-11 0-100
                                    if(delay_damage<96) curIndex=(delay_damage-50)/4;
                                    switch(delay_damage){//伤害判定(三次)
                                        case 54:case 66:case 78:{/*if(进入范围)*/ soundId5=PigSoundPlayer.play_half("skill2",0);BOSS.bosslife-=str*2;break;}
                                        default:break;
                                    }
                                    skillPic=pics.get(curIndex);
                                }
                                break;
                            }
                            case 3:{
                                if(skillflag==0) { skillflag = 1;curIndex = 0;skillPic=pics.get(curIndex); soundId5=PigSoundPlayer.play_half("skill3",0);}
                                else if (skillflag == 1) {
                                    //拉刀光0-5 0-150
                                    //time stop!!!
                                    if(delay_damage==149) {BOSS.bosslife-=timestopkill;timestopkill=0;}//时停结算
                                    else if(delay_damage>=140) curIndex = 0 ;//
                                    else if(delay_damage>=130) curIndex = 1;//
                                    else if(delay_damage>=120) curIndex = 2;//
                                    else if(delay_damage>=110) curIndex = 3;//
                                    else if(delay_damage>=100) curIndex = 4;//
                                    else if(delay_damage>=50) curIndex = 5;//
                                    else if(delay_damage>=40) curIndex = 4;//
                                    else if(delay_damage>=30) curIndex = 3;//
                                    else if(delay_damage>=20) curIndex = 2;//
                                    else if(delay_damage>=10) curIndex = 1;//
                                    skillPic=pics.get(curIndex);
                                }
                                break;
                            }
                            case 4:{
                                if(skillflag==0) {skillflag = 1;curIndex = 0;skillPic=pics.get(curIndex);}
                                else if (skillflag == 1) {
                                    //拉刀光0-28 0-150
                                    if(60<delay_damage&&delay_damage<145) curIndex=(delay_damage-60)/3;
                                    switch(delay_damage){//伤害判定(七次)
                                        case 69:case 81:case 93:case 105:case 117:case 129:case 141:{/*if(进入范围)*/ BOSS.bosslife-=str*2;soundId5=PigSoundPlayer.play_half("skill4",0);break;}
                                        default:break;
                                    }
                                    skillPic=pics.get(curIndex);
                                }
                                break;
                            }
                            default:break;
                        }

                        //skillPic=pics.get(curIndex);
                    }
                }
                else if(!isSkill&&skillflag==1) {skillflag = 0;delay_damage=0;}//时停结束后的清洗？
            }
        };
        timer3.schedule(task3, 1000, 20);
    }


    /*———————————————————————————————————————————————————分割线———————————————————————————————————————————————————*/
    //继承接口后需要实现的函数

    @Override
    //用于刷新画布
    public void run() {
        while (flag) {
            //if(stop==0){
            long start = System.currentTimeMillis();
            //渐变不透明程度 2.04秒
            if(Alpha<255&&stop==0) {
                Alpha+=2.5;
                paint.setAlpha((int)Alpha);
            }
            myDraw();
            long end = System.currentTimeMillis();
            long interval = end - start;
            try {
                if (interval < REFRESH_INTERVAL) {
                    Thread.sleep(REFRESH_INTERVAL - interval);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            //}
        }
    }

    // SurfaceView被创建时的回调函数 切屏再打开也会调用（因为切屏后surfaceview直接销毁+新建？）
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //myDraw();
        //rec=0;stop=0;
        //Toast.makeText(context, "surfaceCreated调用", Toast.LENGTH_SHORT).show();
        screenWidth = getWidth();
        screenHeight = getHeight();
        myWidth = me.getWidth();
        myHeight = me.getHeight();
        BOSS.bossWidth = boss.getWidth();
        BOSS.bossHeight = boss.getHeight();
        BOSS.screenWidth = screenWidth;
        BOSS.screenHeight = screenHeight;
        downline=screenHeight-pic1.getHeight();
        downline_part=(screenHeight-downline)/4;
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);paint.setStrokeWidth(5);

        //敌我初始位置+方向键判定区域 备注：若定时器不延时则到执行这条函数时不等于0，0？
        //这部分只开场初始化一次，切屏不重置
        if(BOSS.bossX==-1&&BOSS.bossY==-1) {
            BOSS.bossX=screenWidth/2-BOSS.bossWidth/2;
            BOSS.bossY=50;
            myX=screenWidth/2-myWidth/2;
            myY=downline-myHeight;
        }
        //int x=pic1.getWidth()/2;//方向键中心x坐标
        //int y=screenHeight-pic1.getHeight()/2;//方向键中心y坐标
        //int z=pic1.getWidth()/7;
        //upArea = new Rect(x-z,screenHeight-pic1.getHeight(),x+z,y-z);
        //leftArea = new Rect(0,y-z,x-z,y+z);
        //rightArea = new Rect(x+z,y-z,pic1.getWidth(),y+z);
        //downArea = new Rect(x-z,y+z,x+z,screenHeight);
        //Area_half = new Rect(x-z*9/4,y-z*9/4,x+z*9/4,y+z*9/4);
        //buttonArea=new Rect(screenWidth-pic2.getWidth()-50,screenHeight- pic2.getHeight()-50,screenWidth-50,screenHeight-50);

        flag = true;
        th = new Thread(this);
        th.start();
        //mediaplayer.start();//改成定时器根据stop控制了，现在音乐也受stop影响了
    }

    // SurfaceView状态改变时的回调函数
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //??
    }

    // SurfaceView被销毁时的回调函数
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
        stop=1;
        if(mediaplayer.isPlaying()) mediaplayer.pause();//用于切屏回来后继续播放音乐
    }

    protected void myDraw() {
        try {
            canvas = sfh.lockCanvas();   // 获取和锁定当前画布
            if (canvas != null) {
                //滚动背景，仅关卡1实装，其他图不衔接
                canvas.drawBitmap(bg,null,new Rect(0, bggundong, screenWidth, bggundong+screenHeight),paint);
                canvas.drawBitmap(bg,null,new Rect(0, bggundong-screenHeight, screenWidth, bggundong),paint);
                if(stop==0) {bggundong+=5;if(bggundong>=screenHeight) bggundong=0;}
                //背景之上的时停怀表
                if(skillflag==1&&skill==3) canvas.drawBitmap(skillPic, screenWidth/2-skillPic.getWidth()/2, 0, paint);
                if(isEnd) {
                    if(mylife<0)canvas.drawBitmap(gameover, screenWidth / 2 - gameover.getWidth() / 2, screenHeight / 2 - gameover.getHeight() / 2, paint);
                    else if(isEnd2) canvas.drawBitmap(tr2, screenWidth / 2 - tr2.getWidth() / 2, screenHeight / 2 - tr2.getHeight() / 2, paint);
                    else canvas.drawBitmap(tr1, screenWidth / 2 - tr1.getWidth() / 2, screenHeight / 2 - tr1.getHeight() / 2, paint);
                    //在按键中加入开箱随机函数并修改数据库+返回
                }
                else {
                    //先画我敌-子弹-其他物品
                    if(isSkill) canvas.drawBitmap(me_skill, myX, myY, paint);
                    else canvas.drawBitmap(me, myX, myY, paint);
                    canvas.drawBitmap(boss, BOSS.bossX,BOSS.bossY, paint);

                    for(int i=0;i<bbtList.size();i++)
                    {
                        bossbullet bbullet=bbtList.get(i);
                        canvas.drawBitmap(bbullet.getBitmap(), (int)bbullet.getX(), (int)bbullet.getY(), paint);
                    }
                    for(int i=0;i<mybtList.size();i++)
                    {
                        mybullet mbullet=mybtList.get(i);
                        canvas.drawBitmap(mbullet.getBitmap(), mbullet.getX(), mbullet.getY(), paint);
                    }


                    canvas.drawBitmap(bosshp,null,new Rect(10, 10, screenWidth*BOSS.bosslife/bosslife_all-10, 30),paint);
                    for(int i=0;i<section;i++) canvas.drawBitmap(sections,10+sections.getWidth()*i,40,paint);
                    canvas.drawText(String.valueOf(time),screenWidth-100,100,paint);
                    canvas.drawLine(0, downline, screenWidth,downline, paint);//分界线
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
                    //canvas.drawBitmap(pic1, 0, downline, paint);
                    //canvas.drawBitmap(pic2, screenWidth - pic2.getWidth()-50, screenHeight - pic2.getHeight()-50, paint);
                    if(skillflag==1) {
                        if (skill < 3) canvas.drawBitmap(skillPic, myX + myWidth / 2 - skillPic.getWidth() / 2, myY + me.getHeight() / 2 - skillPic.getHeight(), paint);
                        else if (skill == 4) canvas.drawBitmap(skillPic,null,new Rect(0,0,screenWidth,screenWidth) , paint);
                    }
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

    /*———————————————————————————————————————————————————分割线———————————————————————————————————————————————————*/
    //其他函数部分

    //自旋函数：参考https://blog.csdn.net/nupt123456789/article/details/44079741
    protected void drawRotateBitmap(Canvas canvas, Paint paint, Bitmap bitmap,
                                  float rotation, float posX, float posY) {
        Matrix matrix = new Matrix();
        int offsetX = bitmap.getWidth() / 2;
        int offsetY = bitmap.getHeight() / 2;
        matrix.postTranslate(-offsetX, -offsetY);
        matrix.postRotate(rotation);
        matrix.postTranslate(posX + offsetX, posY + offsetY);
        canvas.drawBitmap(bitmap, matrix, paint);
    }

    //按键检测函数
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int)event.getX();
        int touchY = (int)event.getY();
        //System.out.println(touchX + ", " + touchY);
        if(isEnd) {
            if(mylife>=0&&delay_clock>=50){//延时1秒防止秒开箱
                isEnd2= true;
                if(mylife>100) rec=0;//上帝模式9999命无奖励
                else rec=rec_t%20+1;//开箱并随机抽奖+修改数据库 加一用于区别退出和无奖
            }
        }
        else if(stop==0&&!isRebirth){//重生动画中不可控制

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
                    myX=touchstartX+(touchX-touchstartX)*sensitivity/100-touchmoveX;
                    myY=touchstartY+(touchY-touchstartY)*sensitivity/100-touchmoveY;
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
    protected static void end(){//结束时外部调用用于关闭定时器线程
        /*TimerTask.cancel() 只是将当前任务终止，也可以理解为从定时任务（任务队列）中清除掉，但实际上并没有对Timer进行任何操作，
        所以在调用 TimerTask.cancel() 时，Timer定时任务（任务队列）中的其它任务还是正常执行，不会影响其它任务的正常执行，
        Timer.cancel() 是将整个定时任务（任务队列）的中的所有任务全部清除。*/
        timer.cancel();
        timer2.cancel();
        timer3.cancel();
    }
    //interface stage3_return{//接口
    //    int value=3;
    //}

    //转阶段函数 各关卡不同
    protected void section_change(){ }

}

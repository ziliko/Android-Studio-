package com.example.once_a_day;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;

import static android.content.Context.MODE_PRIVATE;


public class Stage4_SurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{//父类只能一个,接口可以多个
    //首行，可变数值
    //BOSS血量改动暂时无效，要在后面初始化里改
    private static int section;//一关分成3部分
    private static int bosslife_all = 700;//
    private static int bosslife = 700;//
    private static int mylife = 2;//
    private static int mypower = 2;//
    private static int stepSize = 20;
    private static int boss_speed = 4;//boss移动速度
    //受装备加成
    public static int player=0;  //自机角色 0 1 2 3
    public static int dif=0;  //难度等级 0 1 2
    public static int str = 10;       //力量，已加入函数
    public static int hp= 2;          //(和hp已有冲突)
    public static int dex= 6;         //灵巧，已加入函数
    public static int dex_area = 100;  //灵巧，已加入函数
    public static int skill=0;        //
    public static int sensitivity=100;//触屏灵敏度

    private static Stage4_SurfaceView Stage4;

    private static int bggundong=0;//控制背景滚动
    public static int rec;//返回数据
    public static int stop;//控制暂停
    private static int rec_t;
    public static boolean flag;//或者用flag控制暂停？
    public static boolean isEnd;
    boolean isEnd2;
    Thread th;//单独刷屏线程
    SurfaceHolder sfh;
    Canvas canvas;
    Paint paint;
    static final long REFRESH_INTERVAL = 20;//刷新率：如果要60FPS则应设为17s

    static Context context;
    static MediaPlayer mediaplayer;     //背景音乐的类
    //private SoundPool pool;         //游戏音效的类
    //private Rect region4, region5;//安卓5.0以上禁用，另寻出路
    private static int soundId1, soundId2, soundId3, soundId4, soundId5, soundId6;

    static Bitmap pic1, pic2,bg,tr1,tr2,gameover,sections;             //场景要素
    static Bitmap text_HP,text_MP,text_garze,text_score;      //场景文字
    static Bitmap me,me_skill,mybt,myhp,myhp_empty,mymp,mymp_empty;                 //玩家要素
    static Bitmap  boss,bosshp,bbt_kind1,bbt_kind2,bbt_kind3;  //boss要素
    //对应技能下不同刀光，备用30张图
    static  Bitmap skill_00,skill_01,skill_02,skill_03,skill_04,skill_05,skill_06,skill_07,skill_08,skill_09,skill_10,
            skill_11,skill_12,skill_13,skill_14,skill_15,skill_16,skill_17,skill_18,skill_19,skill_20,
            skill_21,skill_22,skill_23,skill_24,skill_25,skill_26,skill_27,skill_28,skill_29,skill_30;
    static  Bitmap boss_skill00,boss_skill01,boss_skill02,boss_skill03,boss_skill04;
    static  Bitmap boss_skill01L,boss_skill02L,boss_skill03L,boss_skill04L;

    private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static int myWidth = 0;
    private static int myHeight = 0;
    private static int bossWidth = 0;
    private static int bossHeight = 0;
    private static int downline = 0;//下界分界线Y轴坐标
    private static int downline_part = 0;//将线下面区域四等分

    private static int bossX = -1;//控制boss -1用于仅一次的开场初始化部分
    private static int bossY = -1;//
    private static int bossX_control = 0;//控制BOSS移动模式 0静止 1左 2右 3随机闪现？(超出范围默认静止)

    private static int touchstartX = 0;//触屏控制起始位置
    private static int touchstartY = 0;//
    private static int touchmoveX = 0;//触屏控制相对起始位置变化量
    private static int touchmoveY = 0;//
    private static int myX = 0;//控制玩家
    private static int myY = 0;//

    private Rect upArea = null;
    private Rect leftArea = null;
    private Rect rightArea = null;
    private Rect downArea = null;
    private Rect Area_half = null;//用来切割区域变成8种操控
    private Rect buttonArea=null;

    private boolean isSkill=false;//代表当前是否使用了技能
    private boolean isRebirth=false;//代表当前是否死亡重生
    private static Timer timer;//用于控制自机
    private static TimerTask task;
    private static Timer timer2;//用于控制敌机
    private static TimerTask task2;
    private static Timer timer3;//用于释放技能
    private static TimerTask task3;

    static List<Bitmap> pics;//※※※※集合※※※※
    static ArrayList<mybullet>mybtList;//※※※※动态数组与泛型？※※※※
    static ArrayList<bossbullet> bbtList;
    static int curIndex;       //实现分帧动画
    static Bitmap skillPic;    //动画对象
    static int skillflag;      //动画变量
    static int timestopkill;   //结算时停后的boss血量

    private static int mydelay;
    private static int bossdelay;
    private static int delay_skill;//游戏中用于使用技能如时停n秒，无敌n秒
    private static int delay_damage;//技能中独立延时函数，用于计算伤害
    private static int delay_clock;//游戏中用于倒计时-1；游戏后用于延迟1秒开箱
    private static int delay_rebirth;//掉残重生2秒延时
    private static int delay_garze;//擦弹间隔延时
    private static int i,j,k,m;//i,j变量 k作为上层随机数 m作为下层局部随机数
    private static int time;//阶段倒计时
    private static int garze;//擦弹得点

    // 构造函数
    private Stage4_SurfaceView(Context context){
        super(context);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;
        sfh = getHolder();      // 获取SurfaceHolder对象
        sfh.addCallback(this);  // 绑定Callback监听器
        paint = new Paint();
        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg4);
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

        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss4);
        bosshp = BitmapFactory.decodeResource(getResources(), R.drawable.bosshp);
        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1_blue);
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);
        bbt_kind3 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3);

        //预备动作这里画，伤害动作类里画?
        boss_skill01=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill41);
        boss_skill01L=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill41_large);
        boss_skill02=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill42);
        boss_skill02L=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill42_large);
        boss_skill03=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill43);
        boss_skill03L=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill43_large);
        boss_skill04=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill51);//测试用

        tr1 = BitmapFactory.decodeResource(getResources(), R.drawable.treasure1);
        tr2 = BitmapFactory.decodeResource(getResources(), R.drawable.treasure2);
        sections=BitmapFactory.decodeResource(getResources(), R.drawable.section);
        gameover= BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
        setFocusable(true);//控制键盘是否可以获得这个按钮的焦点?
        this.initValue();//所有变量的初始化

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

        this.initSounds();
        this.initTimerTask();
        this.initTimerTask2();
        this.initTimerTask3();
    }

    /**/
    public static Stage4_SurfaceView Init(Context context){
        if(Stage4!=null){
            Stage4.setVisibility(GONE);
            Stage4 = new Stage4_SurfaceView(context);
            Stage4.setVisibility(VISIBLE);
        }
        else Stage4 = new Stage4_SurfaceView(context);
        return Stage4;
    }

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

        stop=0;
        rec=0;
        section=2;
        mylife=hp;//9999+ 为上帝模式 hp
        mypower=2;
        bosslife_all=2250*(1+dif*2);
        bosslife=bosslife_all;
        bossX_control=1+(int)(Math.random()*2);//1 或2 的开局移动方式

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
        bossWidth = 0;
        bossHeight = 0;
        downline = 0;//下界分界线Y轴坐标
        downline_part = 0;//将线下面区域四等分
        bossX = -1;//控制boss -1用于仅一次的开场初始化部分
        bossY = -1;//
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
    //背景音乐定义
    private void initSounds() {
        /*
        PigSoundPlayer.initSoundPlayer(5,5);
        PigSoundPlayer.getLoader(context).load("die",R.raw.die,1);
        PigSoundPlayer.getLoader(context).load("garze",R.raw.garze,1);
        PigSoundPlayer.getLoader(context).load("win",R.raw.win,1);
        switch(skill){
            case 4:PigSoundPlayer.getLoader(context).load("skill",R.raw.skill4,1);break;
            case 3:PigSoundPlayer.getLoader(context).load("skill",R.raw.skill3,1);break;
            case 2:PigSoundPlayer.getLoader(context).load("skill",R.raw.skill2,1);break;
            case 1:PigSoundPlayer.getLoader(context).load("skill",R.raw.skill1,1);break;
        }
         */
        //soundId1 = PigSoundPlayer.play(R.raw.die,0,0,1,0,0);
        //soundId1 = pool.load(context, R.raw.sound1, 1);//*3为优先级（整型）
        //soundId2 = pool.load(context, R.raw.sound2, 1);
        mediaplayer = MediaPlayer.create(context, R.raw.music4);// *1为当前上下文，*2为音频资源编号
        mediaplayer.setLooping(true);    //设置循环播放
    }

    //定时器函数：控制自机子弹+碰撞计算 (移动+碰撞+清理+发射)
    private void initTimerTask() {
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

                            if(bosslife>0){                             //碰撞
                                if(mbullet.crash(bossX,bossY,bossWidth)){
                                    if(isSkill&&skill==3) timestopkill+=str;
                                    else bosslife-=str;
                                }
                            }
                            if(bosslife<=0) {   //转阶段判断处1
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
                                case 0://白毛  直线2排  概率切割
                                    mybtList.add(new mybullet(myX,myY,dex,mybt));
                                    mybtList.add(new mybullet(myX + myWidth,myY,dex,mybt));
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
                            time--;
                            if (time <= 0) {//转阶段判断处2
                                bosslife = 0;//没必要，且最后阶段无计时(或者99)
                                if(section>0) section_change();
                            }
                        }
                    }
                }
                else delay_clock++;//独立延时数：游戏结束后用于延迟1秒开箱 50=1s
            }
        };
        timer.schedule(task, 1000, 20);
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
                            //bossX_control=-1;
                            if(bossX_control==1){
                                bossX-=boss_speed;
                                if(bossX<=0) {bossX=0;bossX_control=2;}
                            }
                            if(bossX_control==2){
                                bossX+=boss_speed;
                                if(bossX>=screenWidth-bossWidth) {bossX=screenWidth-bossWidth;bossX_control=1;}
                            }
                        }

                        if(myY<bossY+bossHeight) {//特殊情况碰撞检测函数3-玩家撞到BOSS直接判断死亡?
                            int dx=(myX+myWidth/2)-(bossX+bossWidth/2);
                            int dy=(myY+myHeight/2)-(bossY+bossHeight/2);
                            if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))< bossWidth/2) {soundId1=PigSoundPlayer.play_half("die",0);mylife=0;}
                        }

                        for(int i=0;i<bbtList.size();i++)
                        {
                            bossbullet bbullet=bbtList.get(i);
                            //(若用到旋转等其他带参方式冲突则需要区分)
                            bbullet.Move();
                            bbullet.Move(myX+myWidth/2,myY+myHeight/2);//方式42
                            bbullet.Move(myX+myWidth/2);         //移动：方式2、41，方式0不管这个参数，带参数->实现追踪

                            int myarea;
                            if(isSkill) myarea=myWidth/2;//无敌撞弹当然要大范围啦
                            else myarea=(myWidth/4)*dex_area/100;

                            if(mylife>0&&mylife<9999){                 //碰撞 9999为上帝模式
                                if (bbullet.crash(myX,myY,myWidth,myarea)) {
                                    if(!isSkill&&!isRebirth) {
                                        mylife-=1;
                                        soundId1=PigSoundPlayer.play_half("die",0);//死亡音效
                                        //清空屏幕上所有子弹
                                        bbtList.clear();//全置NULL，较为耗时，但不易内存溢出？clear会执行循环将每一个坐标都设置为为null， 并设置数组的size为0。
                                        bbtList.trimToSize();//重置大小
                                        mybtList.clear();
                                        mybtList.trimToSize();
                                        if(mylife>0) {
                                            isRebirth=true;//重生入场
                                            myX=screenWidth/2-myWidth/2;
                                            myY=screenHeight;
                                        }
                                    }
                                }
                                //擦弹得点(时停期间不得分)
                                else if(delay_garze>=25&&bbullet.garze(myX,myY,myWidth,myarea)){
                                    garze++;//0.5秒触发间隔
                                    delay_garze=0;
                                    soundId2 = PigSoundPlayer.play("garze",0);//擦弹音效
                                    if(garze%10==0&&mypower<8) mypower++;
                                }
                            }
                            if(mylife<=0) {isEnd=true;bossdelay=0;}

                            if(bbtList.size()>0&&bbullet.end()) {                         //清理(不能并入碰撞，因为导致end原因有2种)
                                bbtList.remove(i);
                                i--;//删除一个后后面的全往前，size也立即-1
                            }
                        }
                        delay_garze++;
                        bossdelay+=20;
                        //boss自动发子弹   ※※※※此处选子弹※※※※
                        k=(int)(Math.random()*100);
                        switch (section){
                            case 2://第一阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL 横排瞄准弹
                                        if (bossdelay>=500+10*time) { //敌机子弹频度改这里
                                            for(int n=screenWidth/16-bbt_kind1.getWidth()/2;n<screenWidth;n+=screenWidth/8) bbtList.add(new bossbullet(n,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind1,42));//自由落体追踪运动
                                            bossdelay=0;
                                        }
                                        break;
                                    case 2://HARD 旋转扩散弹?
                                        /*DNA形状测试
                                        if (bossdelay%100==0) bbtList.add(new bossbullet(screenWidth/16,bossY+bossHeight,screenWidth,screenHeight,2,bbt_kind1,33));//自由落体追踪运动
                                        if (bossdelay>=500) { //敌机子弹频度改这里
                                            for(int n=screenWidth/16;n<screenWidth*15/16;n+=50) bbtList.add(new bossbullet(n,bossY+bossHeight,screenWidth,screenHeight,2,bbt_kind1,33));//自由落体追踪运动
                                            bossdelay=0;
                                        }
                                        if (bossdelay%100==0) bbtList.add(new bossbullet(screenWidth*15/16,bossY+bossHeight,screenWidth,screenHeight,2,bbt_kind1,33));//自由落体追踪运动
                                        break;*/
                                        /**/
                                        if (bossdelay>=1000) { //单发弹(此处不改成%有奇效)
                                            //共75秒
                                            if(time>60) for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2,screenWidth,screenHeight,6,bbt_kind1,7,n));    //12方向下落旋转弹
                                            else if(time>20) for(int n=0;n<360;n+=15) bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2,screenWidth,screenHeight,5,bbt_kind1,6,n));    //12方向扩散旋转弹
                                            else if(time<15) for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2,screenWidth,screenHeight,6,bbt_kind1,7,n));    //12方向下落旋转弹
                                            bossdelay=0;
                                        }break;


                                    default:break;//0
                                }
                                break;
                            case 1://第二阶段 圆周弹+追逐弹(单发加速?)
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=600) { //敌机子弹频度改这里
                                            if(k%4==3) {//if(bbtList.size()%3==2)
                                                //bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+140,bossY+bossHeight-60,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+200,bossY+bossHeight-200,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+140,bossY+bossHeight-340,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight-400,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-140,bossY+bossHeight-340,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-200,bossY+bossHeight-200,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-140,bossY+bossHeight-60,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                            }
                                            else bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL 梦想妙珠x3
                                        if (bossdelay%600==0) { //四方弹
                                            for (int n = 0; n < 360; n += 15) bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 6, bbt_kind1, 0, n, 360 + n));    //正方形扩散弹 新增构造，使用方式0 新输入角变量 变动speed

                                        }
                                        //随时间频率暴增 60时5.4秒一波;30时3.6秒一波;1时1.8秒一波
                                        if(m==0&&bossdelay>=1800+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%500==0){
                                            switch(m){
                                                case 3:m=0;bbtList.add(new bossbullet(bossX+bossWidth/2+boss_skill01L.getWidth(),bossY+bossHeight/2,screenWidth,screenHeight,0,boss_skill03,42));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(bossX+bossWidth/2-boss_skill01L.getWidth(),bossY+bossHeight/2,screenWidth,screenHeight,2,boss_skill01,42));break;
                                                case 1:m++;bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,4,boss_skill02,42));break;
                                                default:break;
                                            }
                                        }
                                        break;
                                    case 2://HARD 梦想妙珠x8
                                        if (bossdelay%1200==0) { //二重四方
                                            i=1-i;
                                            for(int n=0;n<360;n+=5) {//加几度偏移
                                                bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 + n+i*45 ));    //正方形扩散弹
                                                bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 5, bbt_kind1, 0, n, 360 + n + 45+i*45 ));    //正方形扩散弹
                                                //m冲突，改成n后发生了可怕的事情（圆+方+弹射.....）
                                            }

                                        }
                                        //随时间频率暴增 75时7.8秒一波;50时6.6秒一波;1时3.6秒一波
                                        if(m==0&&bossdelay>=3600+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%300==0){
                                            switch(m){
                                                case 8:m=0;bbtList.add(new bossbullet(bossX+bossWidth/2-280,bossY+bossHeight/2+280,screenWidth,screenHeight,0,boss_skill02,42));break;
                                                case 7:m++;bbtList.add(new bossbullet(bossX+bossWidth/2-400,bossY+bossHeight/2,screenWidth,screenHeight,1,boss_skill01,42));break;
                                                case 6:m++;bbtList.add(new bossbullet(bossX+bossWidth/2-280,bossY+bossHeight/2-280,screenWidth,screenHeight,2,boss_skill03,42));break;//自由落体追踪运动
                                                case 5:m++;bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2-400,screenWidth,screenHeight,3,boss_skill02,42));break;
                                                case 4:m++;bbtList.add(new bossbullet(bossX+bossWidth/2+280,bossY+bossHeight/2-280,screenWidth,screenHeight,4,boss_skill01,42));break;
                                                case 3:m++;bbtList.add(new bossbullet(bossX+bossWidth/2+400,bossY+bossHeight/2,screenWidth,screenHeight,5,boss_skill03,42));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(bossX+bossWidth/2+280,bossY+bossHeight/2+280,screenWidth,screenHeight,6,boss_skill02,42));break;
                                                case 1:m++;bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2+400,screenWidth,screenHeight,7,boss_skill01,42));break;
                                                default:break;
                                            }
                                        }
                                        break;
                                    default:break;//0
                                }
                                break;
                            case 0://第三阶段 矩 阵 弹+梦想封印
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=600) { //敌机子弹频度改这里

                                            if(k%5==4) {//if(bbtList.size()%3==2)
                                                //bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+140,bossY+bossHeight-60,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+200,bossY+bossHeight-200,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+140,bossY+bossHeight-340,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight-400,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-140,bossY+bossHeight-340,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-200,bossY+bossHeight-200,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-140,bossY+bossHeight-60,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                            }
                                            //else if(k%5>1)
                                            for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,n));    //12方向扩散弹
                                            //else bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//偷袭弹?
                                            bossdelay=0;
                                            //postInvalidate();
                                        }break;
                                    case 1://NORMAL 八方鬼缚+梦想封印 三连(大) 追逐随重力加速加速
                                        if (bossdelay%600==0) { //四方弹
                                            for (int n = 0; n < 360; n += 15) {
                                                bbtList.add(new bossbullet(bossX + bossWidth / 2, bossY + bossHeight / 2, screenWidth, screenHeight, 6, bbt_kind1, 0, n, 360 + n));    //正方形扩散弹 新增构造，使用方式0 新输入角变量 变动speed
                                                bbtList.add(new bossbullet(bossX + bossWidth / 2, bossY + bossHeight / 2, screenWidth, screenHeight, 6, bbt_kind1, 0, n, 360 + n+45));    //正方形扩散弹 新增构造，使用方式0 新输入角变量 变动speed
                                            }
                                        }
                                        //随时间频率暴增 99时7.2秒一波;50时4.8秒一波;10时2.4秒一波
                                        if(m==0&&bossdelay>=1800+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%500==0){
                                            switch(m){
                                                case 3:m=0;bbtList.add(new bossbullet(bossX+bossWidth/2+boss_skill01L.getWidth(),bossY+bossHeight/2,screenWidth,screenHeight,0,boss_skill03L,41));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(bossX+bossWidth/2-boss_skill01L.getWidth(),bossY+bossHeight/2,screenWidth,screenHeight,2,boss_skill01L,41));break;
                                                case 1:m++;bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,4,boss_skill02L,41));break;
                                                default:break;
                                            }
                                        }
                                        break;
                                    case 2://HARD 八方龙杀+梦想封印 八连(大) 追逐随重力加速加速
                                        if (bossdelay%1200==0) {
                                            //   for(int n=(int)(Math.random()*4)-2;n<360;n+=5)    欲加几度偏移，m冲突，改成n后发生了可怕的事情（圆+方+弹射.....）  0正常方 1偏移45度方 -1  -2  当jumptime<=0 时，退化成圆形扩散
                                            //bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 + n + n*45));    //正方形扩散弹
                                            //bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 5, bbt_kind1, 0, n, 360 + n + 45 - n*45));    //正方形扩散弹
                                            if(time>66&&bosslife>bosslife_all*2/3){
                                                i=1-i;//用到了全局变量i
                                                for(int n=0;n<360;n+=5) {//四方+圆\四方45+圆
                                                    bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 0));    //圆形扩散弹
                                                    bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360+i*45 + n));    //正方形扩散弹
                                                }
                                            }
                                            else if(time>33&&bosslife>bosslife_all/3){
                                                i-=15;
                                                if(i<15) i=360;
                                                for(int n=0;n<360;n+=5) {//动四方+圆
                                                    bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 0));    //圆形扩散弹
                                                    bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 +i + n));    //正方形扩散弹
                                                }
                                            }
                                            else for(int n=0;n<360;n+=5) {//四方+四方45+圆
                                                bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 0));    //圆形扩散弹
                                                bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 + n));    //正方形扩散弹
                                                bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 405 + n));    //正方形扩散弹
                                            }

                                        }
                                        //随时间频率暴增 99时9秒一波;50时7.6秒一波;10时4.2秒一波
                                        if(m==0&&bossdelay>=3600+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%300==0){
                                            switch(m){
                                                case 8:m=0;bbtList.add(new bossbullet(bossX+bossWidth/2-280,bossY+bossHeight/2+280,screenWidth,screenHeight,0,boss_skill02L,41));break;
                                                case 7:m++;bbtList.add(new bossbullet(bossX+bossWidth/2-400,bossY+bossHeight/2,screenWidth,screenHeight,1,boss_skill01L,41));break;
                                                case 6:m++;bbtList.add(new bossbullet(bossX+bossWidth/2-280,bossY+bossHeight/2-280,screenWidth,screenHeight,2,boss_skill03L,41));break;//自由落体追踪运动
                                                case 5:m++;bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2-400,screenWidth,screenHeight,3,boss_skill02L,41));break;
                                                case 4:m++;bbtList.add(new bossbullet(bossX+bossWidth/2+280,bossY+bossHeight/2-280,screenWidth,screenHeight,4,boss_skill01L,41));break;
                                                case 3:m++;bbtList.add(new bossbullet(bossX+bossWidth/2+400,bossY+bossHeight/2,screenWidth,screenHeight,5,boss_skill03L,41));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(bossX+bossWidth/2+280,bossY+bossHeight/2+280,screenWidth,screenHeight,6,boss_skill02L,41));break;
                                                case 1:m++;bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2+400,screenWidth,screenHeight,7,boss_skill01L,41));break;
                                                default:break;
                                            }
                                        }
                                        break;
                                    default:break;//0
                                }
                                break;
                            default:break;
                        }
                    }
                    //时停时依然要碰撞检测(手撕静止子弹?)
                    //Collsion_result2();//boss子弹检测
                }

            }
        };
        timer2.schedule(task2, 1000, 20);
    }
    //定时器3：控制技能动画效果和伤害效果
    private void initTimerTask3() {
        timer3 = new Timer();
        task3 = new TimerTask() {
            @Override
            public void run() {
                if (stop == 0&&isSkill) {
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
                                if(delay_damage==44) {curIndex = 4;/*if(进入范围)*/ bosslife-=str*5;}//伤害判定(仅一次)
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
                                    case 54:case 66:case 78:{/*if(进入范围)*/ soundId5=PigSoundPlayer.play_half("skill2",0);bosslife-=str*2;break;}
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
                                if(delay_damage==149) {bosslife-=timestopkill;timestopkill=0;}//时停结算
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
                                    case 69:case 81:case 93:case 105:case 117:case 129:case 141:{/*if(进入范围)*/ bosslife-=str*2;soundId5=PigSoundPlayer.play_half("skill4",0);break;}
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
                else if(!isSkill&&skillflag==1) {skillflag = 0;delay_damage=0;}//结束后的清洗
            }
        };
        timer3.schedule(task3, 0, 20);
    }
    @Override
    //用于刷新画布
    public void run() {
        while (flag) {
            //if(stop==0){
                long start = System.currentTimeMillis();
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

    // SurfaceView被创建时的回调函数
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //myDraw();
        screenWidth = getWidth();
        screenHeight = getHeight();
        myWidth = me.getWidth();
        myHeight = me.getHeight();
        bossWidth = boss.getWidth();
        bossHeight = boss.getHeight();
        downline=screenHeight-pic1.getHeight();
        downline_part=(screenHeight-downline)/4;
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);paint.setStrokeWidth(5);

        //敌我初始位置+方向键判定区域 备注：若定时器不延时则到执行这条函数时不等于0，0？
        //这部分只开场初始化一次，切屏不重置
        if(bossX==-1&&bossY==-1) {
            bossX=screenWidth/2-bossWidth/2;
            bossY=50;
            myX=screenWidth/2-myWidth/2;
            myY=downline-myHeight;
        }
        int x=pic1.getWidth()/2;//方向键中心x坐标
        int y=screenHeight-pic1.getHeight()/2;//方向键中心y坐标
        int z=pic1.getWidth()/7;
        upArea = new Rect(x-z,screenHeight-pic1.getHeight(),x+z,y-z);
        leftArea = new Rect(0,y-z,x-z,y+z);
        rightArea = new Rect(x+z,y-z,pic1.getWidth(),y+z);
        downArea = new Rect(x-z,y+z,x+z,screenHeight);
        Area_half = new Rect(x-z*9/4,y-z*9/4,x+z*9/4,y+z*9/4);
        buttonArea=new Rect(screenWidth-pic2.getWidth()-50,screenHeight- pic2.getHeight()-50,screenWidth-50,screenHeight-50);

        flag = true;
        th = new Thread(this);
        th.start();
        mediaplayer.start();
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
        mediaplayer.pause();//用于切屏回来后继续播放音乐

    }
    public static void end(){//结束时外部调用用于关闭定时器线程
        timer.cancel();
        timer2.cancel();
        timer3.cancel();
    }
    //interface stage3_return{//接口
    //    int value=3;
    //}

    //主函数
    private void myDraw() {
        try {
            canvas = sfh.lockCanvas();   // 获取和锁定当前画布
            if (canvas != null) {
                canvas.drawBitmap(bg,null,new Rect(0, 0, screenWidth, screenHeight),paint);
                //背景之上的时停怀表
                if(skillflag==1&&skill==3) canvas.drawBitmap(skillPic, screenWidth/2-skillPic.getWidth()/2, 0, paint);
                if(isEnd) {
                    if(mylife<=0)canvas.drawBitmap(gameover, screenWidth / 2 - gameover.getWidth() / 2, screenHeight / 2 - gameover.getHeight() / 2, paint);
                    else if(isEnd2) canvas.drawBitmap(tr2, screenWidth / 2 - tr2.getWidth() / 2, screenHeight / 2 - tr2.getHeight() / 2, paint);
                    else canvas.drawBitmap(tr1, screenWidth / 2 - tr1.getWidth() / 2, screenHeight / 2 - tr1.getHeight() / 2, paint);
                    //在按键中加入开箱随机函数并修改数据库+返回
                }
                else {
                    //先画我敌-子弹-其他物品
                    if(isSkill) canvas.drawBitmap(me_skill, myX, myY, paint);
                    else canvas.drawBitmap(me, myX, myY, paint);
                    canvas.drawBitmap(boss, bossX,bossY, paint);

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


                    canvas.drawBitmap(bosshp,null,new Rect(10, 10, screenWidth*bosslife/bosslife_all-10, 30),paint);
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
    //自旋函数：参考https://blog.csdn.net/nupt123456789/article/details/44079741
    private void drawRotateBitmap(Canvas canvas, Paint paint, Bitmap bitmap,
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
            if(mylife>0&&delay_clock>=50){//延时1秒防止秒开箱
                isEnd2= true;
                if(mylife>=9999) rec=0;//上帝模式无奖励
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

    //转阶段函数 各关卡不同
    private void section_change(){
        bossdelay=0;
        section--;
        soundId4=PigSoundPlayer.play("section_change",0);
        //此关m用于旋转
        m=0;
        bosslife=bosslife_all;
        //N H 二三阶段，BOSS上居中不动
        if(dif>0){
            bossX=screenWidth/2-bossWidth/2;
            bossX_control=0;
        }
        if(section==0) {
            time=99;
        }
        else time=45+15*dif;
        //清空屏幕上所有子弹
        bbtList.clear();//可设置为一个得分点，根据清除的数量
        bbtList.trimToSize();//
        //mybtList.clear();//会导致下方end函数闪退
        //mybtList.trimToSize();
    }

}

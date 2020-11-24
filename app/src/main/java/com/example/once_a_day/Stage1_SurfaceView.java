package com.example.once_a_day;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import java.util.Timer;
import java.util.TimerTask;

public class Stage1_SurfaceView extends Stage_Father{
    //首行，可变数值
    private static final String TAG = "Stage1_SurfaceView";
    private static Stage1_SurfaceView Stage1;

    // 构造函数
    public Stage1_SurfaceView(Context context,AttributeSet attrs){
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg1);//TODO
        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss1);//TODO
        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1_yellow);//TODO
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);//TODO
        bbt_kind3 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3);//TODO

        this.initValue();//TODO 各关卡独有的初始化数值
        this.initSounds();//TODO 各关卡独有的背景音乐
        this.initTimerTask2();//TODO 各关卡独有的BOSS子弹风格
    }

    /*单例模式下 类内部声明自己唯一的对象 但这里并不需要用到
    public static Stage1_SurfaceView Init(Context context){
        if(Stage1!=null){
            Stage1.setVisibility(GONE);
            Stage1 = new Stage1_SurfaceView(context);
            Stage1.setVisibility(VISIBLE);
        }
        else Stage1 = new Stage1_SurfaceView(context);
        return Stage1;
    }*/

    //所有变量的初始化 TODO 各关卡不同的变量 如：BOSS血量、速度
    private void initValue(){
        section=2;////各关的阶段数
        mylife=hp;//各关的自机初始血量 9999+ 为上帝模式 hp
        mypower=2;//各关的自机初始蓝量
        bosslife_all=2400*(1+dif*2);//各关的BOSS初始血量
        bosslife=bosslife_all;
        bossX_control=1+(int)(Math.random()*2);////各关的BOSS初始移动方式   1 或2 的开局移动方式
        boss_speed=3;//各关的BOSS初始速度
    }

    //背景音乐定义 TODO 各关卡不同
    private void initSounds() {
        mediaplayer = MediaPlayer.create(context, R.raw.music1);// *1为当前上下文，*2为音频资源编号
        mediaplayer.setLooping(true);    //设置循环播放
    }

    //定时器2：自动控制BOSS子弹+碰撞  多套逻辑？(移动+碰撞+清理+发射)  TODO 各关卡不同
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
                            bbullet.Move();                             //移动

                            int myarea;
                            if(isSkill) myarea=myWidth/2;//无敌撞弹当然要大范围啦
                            else myarea=(myWidth/4)*dex_area/100;

                            if(mylife>0&&mylife<9999){                 //碰撞 9999为上帝模式
                                if (bbullet.crash(myX,myY,myWidth,myarea)) {
                                    if(!isSkill&&!isRebirth) {
                                        mylife-=1;
                                        soundId1=PigSoundPlayer.play_half("die",0);//死亡音效
                                        System.out.println("播放了die,id为:" + soundId1+"my坐标:"+myX+","+myY);
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
                        /*※※※※※关卡设计在此※※※※※
                        switch (dif){
                                    case 0://EASY

                                        break;
                                    case 1://NORMAL

                                        break;
                                    case 2://HARD

                                        break;

                                    default:break;//0
                                }
                         */
                        switch (section){
                            case 2://第一阶段
                                //bosslife=20;//测试用例
                                if (bossdelay>=1200) { //敌机子弹频度改这里
                                    //i=bossWidth/2;
                                    //j=bossHeight;//减少运算量?
                                    /*正常弹！上方安全*/
                                    //bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2));//变子弹只需要改这里
                                    bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2));//变子弹只需要改这里
                                    bbtList.add(new bossbullet(bossX+bossWidth/2+50,bossY+bossHeight,screenWidth,screenHeight,9,bbt_kind2));//变子弹只需要改这里
                                    bbtList.add(new bossbullet(bossX+bossWidth/2-50,bossY+bossHeight,screenWidth,screenHeight,9,bbt_kind2));//变子弹只需要改这里
                                    bbtList.add(new bossbullet(bossX+bossWidth/2+100,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//变子弹只需要改这里
                                    bbtList.add(new bossbullet(bossX+bossWidth/2-100,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//变子弹只需要改这里
                                    if(dif>1){//追加
                                        bbtList.add(new bossbullet(bossX+bossWidth/2-200,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind2));//左+
                                        bbtList.add(new bossbullet(bossX+bossWidth/2+200,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind2));//右+
                                        bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind2));//上+
                                        bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,2,bbt_kind2));//上+
                                    }
                                    if(dif>0){//追加
                                        bbtList.add(new bossbullet(bossX+bossWidth/2-150,bossY+bossHeight,screenWidth,screenHeight,7,bbt_kind2));//左+
                                        bbtList.add(new bossbullet(bossX+bossWidth/2+150,bossY+bossHeight,screenWidth,screenHeight,7,bbt_kind2));//右+
                                        bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//上+
                                        bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,4,bbt_kind2));//上+
                                    }
                                    bossdelay=0;
                                }break;
                            case 1://第二阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://难度EASY
                                        if (bossdelay>=1200) { //敌机子弹频度改这里
                                            /*菊爆！但是躲下方反而安全？*/
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,screenHeight,screenWidth,screenHeight,10,bbt_kind2,1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+50,screenHeight,screenWidth,screenHeight,9,bbt_kind2,1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+100,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-50,screenHeight,screenWidth,screenHeight,9,bbt_kind2,1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-100,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://难度Normal 交替正反弹（k）
                                        if (bossdelay>=1200) { //敌机子弹频度改这里
                                            if(k==0) {j=screenHeight;k=1;}//反弹
                                            else  {j=bossY+bossHeight;k=0;}//正弹
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,10,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+50,j,screenWidth,screenHeight,9,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-50,j,screenWidth,screenHeight,9,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+100,j,screenWidth,screenHeight,8,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-100,j,screenWidth,screenHeight,8,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+150,j,screenWidth,screenHeight,7,bbt_kind2,k));//右+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-150,j,screenWidth,screenHeight,7,bbt_kind2,k));//左+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,8,bbt_kind2,k));//上+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,4,bbt_kind2,k));//上+
                                            bossdelay=0;
                                        }break;
                                    case 2://难度HARD
                                        if (bossdelay>=1200) { //敌机子弹频度改这里
                                            if(k==0) {j=screenHeight;k=1;}//反弹
                                            else     {j=bossY+bossHeight;k=0;}//正弹
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,10,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+50,j,screenWidth,screenHeight,9,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-50,j,screenWidth,screenHeight,9,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+100,j,screenWidth,screenHeight,8,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-100,j,screenWidth,screenHeight,8,bbt_kind2,k));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+150,j,screenWidth,screenHeight,7,bbt_kind2,k));//右+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-150,j,screenWidth,screenHeight,7,bbt_kind2,k));//左+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+200,j,screenWidth,screenHeight,6,bbt_kind2,k));//右+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-200,j,screenWidth,screenHeight,6,bbt_kind2,k));//左+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,8,bbt_kind2,k));//上+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,4,bbt_kind2,k));//上+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,6,bbt_kind2,k));//上+
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,j,screenWidth,screenHeight,2,bbt_kind2,k));//上+
                                            bossdelay=0;
                                        }break;
                                    default:break;//0
                                }
                                break;
                            case 0://第三阶段
                                switch (dif){
                                    case 0://EASY  上下夹击
                                        if (bossdelay>=1200) { //敌机子弹频度改这里
                                            /*高难度构思 菊爆！但是躲下方反而安全？*/
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+50,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+100,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-50,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-100,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+50,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+100,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-50,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-100,screenHeight,screenWidth,screenHeight,8,bbt_kind2,1));//
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL  上下夹击 + 十字 (+ 炮?)
                                        if (bossdelay%1200==0) { //敌机子弹频度改这里
                                            //一横排子弹
                                            for(k=-time;k<screenWidth+100;k+=bbt_kind2.getWidth()*2) {
                                                bbtList.add(new bossbullet(k,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));
                                                bbtList.add(new bossbullet(k,screenHeight,screenWidth,screenHeight,8,bbt_kind2));
                                            }
                                        }
                                        if (bossdelay%3600==0) { //敌机子弹频度改这里
                                            //一竖排子弹
                                            for(k=3;k<20;k+=2) {
                                                bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,k,bbt_kind2));
                                            }
                                        }
                                        if (bossdelay>7200) { //敌机子弹频度改这里
                                            //仿魔炮
                                            if (bossX_control<3) {bossX_control=10-bossX_control;soundId6=PigSoundPlayer.play("bossskill1",0);}//暂停移动+音效(仅一次)
                                            if(bossdelay%100==0) for(int n=0;n<181;n+=10)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,3,bbt_kind1,12,n));    //方向扩散弹
                                            if(bossdelay>9200) {bossdelay=0;bossX_control=10-bossX_control;}
                                        }
                                        break;
                                    case 2://HARD  九宫格夹击切割+魔炮 20秒一轮
                                        if (bossdelay%500==0) { //敌机子弹频度改这里
                                            //一横排子弹
                                            for(k=-time%10*20;k<screenWidth+100;k+=bbt_kind2.getWidth()*2) {
                                                bbtList.add(new bossbullet(k,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind2));
                                                bbtList.add(new bossbullet(k,screenHeight,screenWidth,screenHeight,8,bbt_kind2));
                                            }
                                        }
                                        if (bossdelay%5000==0) { //敌机子弹频度改这里
                                            //三竖排子弹
                                            for(k=1;k<20;k+=2) {
                                                bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,k,bbt_kind2));
                                                bbtList.add(new bossbullet(bossX+bossWidth/2-screenWidth/3,bossY+bossHeight,screenWidth,screenHeight,k,bbt_kind2));
                                                bbtList.add(new bossbullet(bossX+bossWidth/2+screenWidth/3,bossY+bossHeight,screenWidth,screenHeight,k,bbt_kind2));
                                            }
                                        }
                                        if(time>50&&bossdelay>13000) { //敌机子弹频度改这里 50秒3波
                                            //仿魔炮
                                            if (boss_speed==3) {boss_speed=1;soundId6=PigSoundPlayer.play("bossskill1",0);}//暂停移动+音效(仅一次)
                                            if(bossdelay%100==0) for(int n=0;n<181;n+=10)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,12,n));    //方向扩散弹
                                            if(bossdelay>16000) {bossdelay=0;boss_speed=3;}//注！这个初始化不一定实现，因为是最好阶段所以问题不大，后续拓展要注意修改
                                        }
                                        else if(time>20&&bossdelay>8000) {//30秒3波
                                            if (boss_speed==3) {boss_speed=1;soundId6=PigSoundPlayer.play("bossskill1",0);}//暂停移动+音效(仅一次)
                                            if(bossdelay%100==0) for(int n=0;n<181;n+=10)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,8,bbt_kind1,12,n));    //方向扩散弹
                                            if(bossdelay>9900) {bossdelay=0;boss_speed=3;}
                                        }
                                        else if(time<=20&&bossdelay>=1500) {//狂暴吧！
                                            if(boss_speed!=3) boss_speed=3;
                                            if(bossdelay==1500) soundId6=PigSoundPlayer.play("bossskill1",0);//音效(仅一次)
                                            if(bossdelay%100==0) for(int n=0;n<181;n+=10)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind1,12,n));    //方向扩散弹
                                            if(bossdelay>4000) bossdelay=0;
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

    //主函数 (覆盖父类方法 且为final) TODO 各关卡不同(大概)
    protected final void myDraw() {
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
                    if(mylife<=0)canvas.drawBitmap(gameover, screenWidth / 2 - gameover.getWidth() / 2, screenHeight / 2 - gameover.getHeight() / 2, paint);
                    else if(isEnd2) canvas.drawBitmap(tr2, screenWidth / 2 - tr2.getWidth() / 2, screenHeight / 2 - tr2.getHeight() / 2, paint);
                    else canvas.drawBitmap(tr1, screenWidth / 2 - tr1.getWidth() / 2, screenHeight / 2 - tr1.getHeight() / 2, paint);
                    //在按键中加入开箱随机函数并修改数据库+返回
                }
                else {
                    //先画我+敌，我+敌子弹
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
                    //再画其他物品
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

    //转阶段函数 TODO 各关卡不同
    public void section_change(){
        bossdelay=0;
        section--;
        soundId4=PigSoundPlayer.play("section_change",0);
        bosslife=bosslife_all;
        if(section==0){
            time=99;
            //已经废弃，因为其他地方有调用i 频繁置为0了
            //i=bossWidth/2;//因为关卡1第二阶段有局部随机改动，因此第三阶段初始化一次
            //j=bossHeight;
        }
        else time=45+15*dif;
        //清空屏幕上所有子弹
        bbtList.clear();//可设置为一个得分点，根据清除的数量
        bbtList.trimToSize();//
        //mybtList.clear();//会导致下方end函数闪退
        //mybtList.trimToSize();
    }
}
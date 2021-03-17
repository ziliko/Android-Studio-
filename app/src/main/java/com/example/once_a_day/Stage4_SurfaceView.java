package com.example.once_a_day;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import java.util.Timer;
import java.util.TimerTask;

public class Stage4_SurfaceView extends Stage_Father{//父类只能一个,接口可以多个
    private static final String TAG = "Stage4_SurfaceView";
    private static Stage4_SurfaceView Stage4;

    // 构造函数
    public Stage4_SurfaceView(Context context,AttributeSet attrs){
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg4);
        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss4);
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

        this.initValue();//TODO 各关卡独有的初始化数值
        this.initSounds();//TODO 各关卡独有的背景音乐
        this.initTimerTask2();//TODO 各关卡独有的BOSS子弹风格
    }

    //所有变量的初始化
    private void initValue(){
        section=2;////各关的阶段数
        mylife=hp;//各关的自机初始血量 9999+ 为上帝模式 hp
        mypower=2;//各关的自机初始蓝量
        bosslife_all=1875*(1+dif*dif);//各关的BOSS初始血量
        BOSS.bosslife=bosslife_all;
        BOSS.control=1+(int)(Math.random()*2);////各关的BOSS初始移动方式   1 或2 的开局移动方式
        BOSS.speed=4;//各关的BOSS初始速度
    }

    //背景音乐定义
    private void initSounds() {
        mediaplayer = MediaPlayer.create(context, R.raw.music4);// *1为当前上下文，*2为音频资源编号
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
                            //(若用到旋转等其他带参方式冲突则需要区分)
                            bbullet.Move();
                            bbullet.Move(myX+myWidth/2,myY+myHeight/2);//方式42
                            bbullet.Move(myX+myWidth/2);         //移动：方式2、41，方式0不管这个参数，带参数->实现追踪

                            int myarea;
                            if(isSkill) myarea=myWidth/2;//无敌撞弹当然要大范围啦
                            else myarea=(myWidth/4)*dex_area/100;

                            if(mylife>=0&&mylife<9999){                 //碰撞 9999为上帝模式
                                if (bbullet.crash(myX,myY,myWidth,myarea)) {
                                    if(!isSkill&&!isRebirth) {
                                        mylife-=1;
                                        soundId1=PigSoundPlayer.play_half("die",0);//死亡音效
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
                                //擦弹得点(时停期间不得分)
                                else if(delay_garze>=25&&bbullet.garze(myX,myY,myWidth,myarea)){
                                    garze++;//0.5秒触发间隔
                                    delay_garze=0;
                                    soundId2 = PigSoundPlayer.play("garze",0);//擦弹音效
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
                        k=(int)(Math.random()*100);
                        switch (section){
                            case 2://第一阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL 横排瞄准弹
                                        if (bossdelay>=500+10*time) { //敌机子弹频度改这里
                                            for(int n=screenWidth/16-bbt_kind1.getWidth()/2;n<screenWidth;n+=screenWidth/8) bbtList.add(new bossbullet(n,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,0,bbt_kind1,42));//自由落体追踪运动
                                            bossdelay=0;
                                        }
                                        break;
                                    case 2://HARD 旋转扩散弹?
                                        /*DNA形状测试
                                        if (bossdelay%100==0) bbtList.add(new bossbullet(screenWidth/16,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,2,bbt_kind1,33));//自由落体追踪运动
                                        if (bossdelay>=500) { //敌机子弹频度改这里
                                            for(int n=screenWidth/16;n<screenWidth*15/16;n+=50) bbtList.add(new bossbullet(n,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,2,bbt_kind1,33));//自由落体追踪运动
                                            bossdelay=0;
                                        }
                                        if (bossdelay%100==0) bbtList.add(new bossbullet(screenWidth*15/16,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,2,bbt_kind1,33));//自由落体追踪运动
                                        break;*/
                                        /**/
                                        if (bossdelay>=1000) { //单发弹(此处不改成%有奇效)
                                            //共75秒
                                            if(time>60) for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,6,bbt_kind1,7,n));    //12方向下落旋转弹
                                            else if(time>20) for(int n=0;n<360;n+=15) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,5,bbt_kind1,6,n));    //12方向扩散旋转弹
                                            else if(time<15) for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,6,bbt_kind1,7,n));    //12方向下落旋转弹
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
                                                //bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+140,BOSS.bossY+BOSS.bossHeight-60,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+200,BOSS.bossY+BOSS.bossHeight-200,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+140,BOSS.bossY+BOSS.bossHeight-340,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight-400,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-140,BOSS.bossY+BOSS.bossHeight-340,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-200,BOSS.bossY+BOSS.bossHeight-200,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-140,BOSS.bossY+BOSS.bossHeight-60,screenWidth,screenHeight,5,bbt_kind2,2));//变子弹只需要改这里
                                            }
                                            else bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL 梦想妙珠x3
                                        if (bossdelay%600==0) { //四方弹
                                            for (int n = 0; n < 360; n += 15) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 6, bbt_kind1, 0, n, 360 + n));    //正方形扩散弹 新增构造，使用方式0 新输入角变量 变动speed

                                        }
                                        //随时间频率暴增 60时5.4秒一波;30时3.6秒一波;1时1.8秒一波
                                        if(m==0&&bossdelay>=1800+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%500==0){
                                            switch(m){
                                                case 3:m=0;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+boss_skill01L.getWidth(),BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,0,boss_skill03,42));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-boss_skill01L.getWidth(),BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,2,boss_skill01,42));break;
                                                case 1:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,4,boss_skill02,42));break;
                                                default:break;
                                            }
                                        }
                                        break;
                                    case 2://HARD 梦想妙珠x8
                                        if (bossdelay%1200==0) { //二重四方
                                            i=1-i;
                                            for(int n=0;n<360;n+=5) {//加几度偏移
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 + n+i*45 ));    //正方形扩散弹
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 5, bbt_kind1, 0, n, 360 + n + 45+i*45 ));    //正方形扩散弹
                                                //m冲突，改成n后发生了可怕的事情（圆+方+弹射.....）
                                            }

                                        }
                                        //随时间频率暴增 75时7.8秒一波;50时6.6秒一波;1时3.6秒一波
                                        if(m==0&&bossdelay>=3600+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%300==0){
                                            switch(m){
                                                case 8:m=0;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-280,BOSS.bossY+BOSS.bossHeight/2+280,screenWidth,screenHeight,0,boss_skill02,42));break;
                                                case 7:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-400,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,1,boss_skill01,42));break;
                                                case 6:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-280,BOSS.bossY+BOSS.bossHeight/2-280,screenWidth,screenHeight,2,boss_skill03,42));break;//自由落体追踪运动
                                                case 5:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2-400,screenWidth,screenHeight,3,boss_skill02,42));break;
                                                case 4:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+280,BOSS.bossY+BOSS.bossHeight/2-280,screenWidth,screenHeight,4,boss_skill01,42));break;
                                                case 3:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+400,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,5,boss_skill03,42));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+280,BOSS.bossY+BOSS.bossHeight/2+280,screenWidth,screenHeight,6,boss_skill02,42));break;
                                                case 1:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2+400,screenWidth,screenHeight,7,boss_skill01,42));break;
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
                                                //bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+140,BOSS.bossY+BOSS.bossHeight-60,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+200,BOSS.bossY+BOSS.bossHeight-200,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+140,BOSS.bossY+BOSS.bossHeight-340,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight-400,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-140,BOSS.bossY+BOSS.bossHeight-340,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-200,BOSS.bossY+BOSS.bossHeight-200,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-140,BOSS.bossY+BOSS.bossHeight-60,screenWidth,screenHeight,10,bbt_kind2,2));//变子弹只需要改这里
                                            }
                                            //else if(k%5>1)
                                            for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,n));    //12方向扩散弹
                                            //else bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,20,bbt_kind1));//偷袭弹?
                                            bossdelay=0;
                                            //postInvalidate();
                                        }break;
                                    case 1://NORMAL 八方鬼缚+梦想封印 三连(大) 追逐随重力加速加速
                                        if (bossdelay%600==0) { //四方弹
                                            for (int n = 0; n < 360; n += 15) {
                                                bbtList.add(new bossbullet(BOSS.bossX + BOSS.bossWidth / 2, BOSS.bossY + BOSS.bossHeight / 2, screenWidth, screenHeight, 6, bbt_kind1, 0, n, 360 + n));    //正方形扩散弹 新增构造，使用方式0 新输入角变量 变动speed
                                                bbtList.add(new bossbullet(BOSS.bossX + BOSS.bossWidth / 2, BOSS.bossY + BOSS.bossHeight / 2, screenWidth, screenHeight, 6, bbt_kind1, 0, n, 360 + n+45));    //正方形扩散弹 新增构造，使用方式0 新输入角变量 变动speed
                                            }
                                        }
                                        //随时间频率暴增 99时7.2秒一波;50时4.8秒一波;10时2.4秒一波
                                        if(m==0&&bossdelay>=1800+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%500==0){
                                            switch(m){
                                                case 3:m=0;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+boss_skill01L.getWidth(),BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,0,boss_skill03L,41));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-boss_skill01L.getWidth(),BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,2,boss_skill01L,41));break;
                                                case 1:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight,screenWidth,screenHeight,4,boss_skill02L,41));break;
                                                default:break;
                                            }
                                        }
                                        break;
                                    case 2://HARD 八方龙杀+梦想封印 八连(大) 追逐随重力加速加速
                                        if (bossdelay%1200==0) {
                                            //   for(int n=(int)(Math.random()*4)-2;n<360;n+=5)    欲加几度偏移，m冲突，改成n后发生了可怕的事情（圆+方+弹射.....）  0正常方 1偏移45度方 -1  -2  当jumptime<=0 时，退化成圆形扩散
                                            //bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 + n + n*45));    //正方形扩散弹
                                            //bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 5, bbt_kind1, 0, n, 360 + n + 45 - n*45));    //正方形扩散弹
                                            if(time>66&&BOSS.bosslife>bosslife_all*2/3){
                                                i=1-i;//用到了全局变量i
                                                for(int n=0;n<360;n+=5) {//四方+圆\四方45+圆
                                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 0));    //圆形扩散弹
                                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360+i*45 + n));    //正方形扩散弹
                                                }
                                            }
                                            else if(time>33&&BOSS.bosslife>bosslife_all/3){
                                                i-=15;
                                                if(i<15) i=360;
                                                for(int n=0;n<360;n+=5) {//动四方+圆
                                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 0));    //圆形扩散弹
                                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 +i + n));    //正方形扩散弹
                                                }
                                            }
                                            else for(int n=0;n<360;n+=5) {//四方+四方45+圆
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 0));    //圆形扩散弹
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 360 + n));    //正方形扩散弹
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2, BOSS.bossY+BOSS.bossHeight/2, screenWidth, screenHeight, 7, bbt_kind1, 0, n, 405 + n));    //正方形扩散弹
                                            }

                                        }
                                        //随时间频率暴增 99时9秒一波;50时7.6秒一波;10时4.2秒一波
                                        if(m==0&&bossdelay>=3600+time/10*600) {m=1;bossdelay=0;}
                                        else if(m>0&&bossdelay%300==0){
                                            switch(m){
                                                case 8:m=0;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-280,BOSS.bossY+BOSS.bossHeight/2+280,screenWidth,screenHeight,0,boss_skill02L,41));break;
                                                case 7:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-400,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,1,boss_skill01L,41));break;
                                                case 6:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-280,BOSS.bossY+BOSS.bossHeight/2-280,screenWidth,screenHeight,2,boss_skill03L,41));break;//自由落体追踪运动
                                                case 5:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2-400,screenWidth,screenHeight,3,boss_skill02L,41));break;
                                                case 4:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+280,BOSS.bossY+BOSS.bossHeight/2-280,screenWidth,screenHeight,4,boss_skill01L,41));break;
                                                case 3:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+400,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,5,boss_skill03L,41));break;//自由落体追踪运动
                                                case 2:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+280,BOSS.bossY+BOSS.bossHeight/2+280,screenWidth,screenHeight,6,boss_skill02L,41));break;
                                                case 1:m++;bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2+400,screenWidth,screenHeight,7,boss_skill01L,41));break;
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
        timer2.schedule(task2, 3000, 20);
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

    //转阶段函数 各关卡不同
    protected void section_change(){
        bossdelay=0;
        section--;
        soundId4=PigSoundPlayer.play("section_change",0);
        //此关m用于旋转
        m=0;
        BOSS.bosslife=bosslife_all;
        //N H 二三阶段，BOSS上居中不动
        if(dif>0){
            BOSS.bossX=screenWidth/2-BOSS.bossWidth/2;
            BOSS.control=0;
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

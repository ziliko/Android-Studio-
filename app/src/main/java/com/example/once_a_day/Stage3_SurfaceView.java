package com.example.once_a_day;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import java.util.Timer;
import java.util.TimerTask;

public class Stage3_SurfaceView extends Stage_Father{//父类只能一个,接口可以多个
    //首行，可变数值
    private static final String TAG = "Stage3_SurfaceView";
    private static Stage3_SurfaceView Stage3;

    // 构造函数
    public Stage3_SurfaceView(Context context,AttributeSet attrs){
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg3);
        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss3);
        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1_green);
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);
        bbt_kind2L = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2large);
        bbt_kind3 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3);

        this.initValue();//TODO 各关卡独有的初始化数值
        this.initSounds();//TODO 各关卡独有的背景音乐
        this.initTimerTask2();//TODO 各关卡独有的BOSS子弹风格
    }

    //所有变量的初始化
    private void initValue(){
        section=2;////各关的阶段数
        mylife=hp;//各关的自机初始血量 9999+ 为上帝模式 hp
        mypower=2;//各关的自机初始蓝量
        bosslife_all=3000*(1+dif*2);//各关的BOSS初始血量
        bosslife=bosslife_all;
        bossX_control=1+(int)(Math.random()*2);////各关的BOSS初始移动方式   1 或2 的开局移动方式
        boss_speed=3;//各关的BOSS初始速度
    }

    //背景音乐定义
    private void initSounds() {
        mediaplayer = MediaPlayer.create(context, R.raw.music3);// *1为当前上下文，*2为音频资源编号
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
                            //bossX_control=-1;
                            if(bossX_control==1){
                                bossX-=boss_speed;
                                if(bossX<=0) {bossX=0;bossX_control=2;}
                            }
                            if(bossX_control==2){
                                bossX+=boss_speed;
                                if(bossX>=screenWidth-bossWidth) {bossX=screenWidth-bossWidth;bossX_control=1;}
                            }
                            //N H 第二三阶段BOSS居中不动
                        }

                        if(myY<bossY+bossHeight) {//特殊情况碰撞检测函数3-玩家撞到BOSS直接判断死亡?
                            int dx=(myX+myWidth/2)-(bossX+bossWidth/2);
                            int dy=(myY+myHeight/2)-(bossY+bossHeight/2);
                            if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))< bossWidth/2) {soundId1=PigSoundPlayer.play_half("die",0);mylife=0;}
                        }

                        for(int i=0;i<bbtList.size();i++)
                        {
                            bossbullet bbullet=bbtList.get(i);
                            if(bbullet.getWay()==31) {//吸附效果
                                touchstartX=myX;//吸附需重置触屏初始点，否则吸不过去(还是有问题，待改)
                                touchstartY=myY;
                                bbullet.Move(1);
                                if(time>20){
                                    int a=1+(100-time)/20;
                                    if(myX+myWidth/2<bbullet.getX_mid()) myX+=a;else myX-=a;
                                    if(myY+myHeight/2<bbullet.getY_mid()) myY+=a;else myY-=a;
                                }
                                else{
                                    int a=bbullet.getMove_waytime()/50;//每一秒加1
                                    if(myX+myWidth/2<bbullet.getX_mid()) myX+=a;else myX-=a;
                                    if(myY+myHeight/2<bbullet.getY_mid()) myY+=a;else myY-=a;
                                }
                            }
                            else bbullet.Move();                             //移动

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
                                        if (bossdelay>=900) {
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2));
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL
                                        if (bossdelay>=900) { //单发弹(此处不改成%有奇效)
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,5,bbt_kind2));
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2));
                                            bossdelay=0;
                                        }break;
                                    case 2://HARD
                                        if (bossdelay>=600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2-bbt_kind2.getWidth()*2,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind2,5));//自由落体运动
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind2,5));//自由落体运动
                                            bbtList.add(new bossbullet(bossX+bossWidth/2+bbt_kind2.getWidth()*2,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind2,5));//自由落体运动
                                            bossdelay=0;
                                        }break;
                                    default:break;//0
                                }
                                break;
                            case 1://第二阶段 N H BOSS移到正中间，旋转 放弹
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>3600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,5,bbt_kind2L));//固定位置
                                            bbtList.add(new bossbullet(screenWidth*k/100,bossY+bossHeight,screenWidth,screenHeight,5,bbt_kind2L));//随机
                                            bossdelay=0;
                                        }
                                        else if (bossdelay%900==0) {
                                            bbtList.add(new bossbullet(bossX+bossWidth/2, bossY+bossHeight, screenWidth, screenHeight, 10, bbt_kind2));//变子弹只需要改这里
                                        }break;
                                    case 1://NORMAL 4方向
                                        if (bossdelay<6000&&bossdelay>1500&&bossdelay%600==0) { //
                                            for(int n=m;n<360+m;n+=90)  bbtList.add(new bossbullet(screenWidth/2,screenHeight/2-bbt_kind2.getHeight()/2,screenWidth,screenHeight,5,bbt_kind2L,0,n));    //12方向扩散弹
                                            m-=10;//此关m用于记录旋转值
                                        }
                                        if (bossdelay>6000&&bossdelay%300==0) { //
                                            for(int n=m;n<360+m;n+=90)  bbtList.add(new bossbullet(screenWidth/2,screenHeight/2-bbt_kind2.getHeight()/2,screenWidth,screenHeight,5,bbt_kind2,0,n));    //12方向扩散弹
                                            m+=20;//此关m用于记录旋转值
                                        }
                                        if(bossdelay>12000) bossdelay=0;
                                        break;
                                    case 2://HARD 8方向
                                        if (bossdelay%1200==0) { //
                                            for(int n=m;n<360+m;n+=45)  bbtList.add(new bossbullet(screenWidth/2,screenHeight/2-bbt_kind2L.getHeight()/2,screenWidth,screenHeight,6,bbt_kind2L,0,n));    //12方向扩散弹
                                            m+=10;
                                        }
                                        if ((bossdelay-600)%1200==0) { //
                                            for(int n=m/10;n<360+m;n+=15)  bbtList.add(new bossbullet(screenWidth/2,screenHeight/2-bbt_kind2.getHeight()/2,screenWidth,screenHeight,6,bbt_kind2,0,n));    //12方向扩散弹
                                            m+=10;
                                        }
                                        if(bossdelay>6000) bossdelay=0;
                                        break;
                                    default:break;//0
                                }
                                break;
                            case 0://第三阶段 BOSS移到正中间，放阴阳鬼神玉/移动放阴阳玉
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=3600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind2L,5));//自由落体运动
                                            bbtList.add(new bossbullet(bossX-bossWidth*3/2,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind2L,5));//自由落体运动
                                            bbtList.add(new bossbullet(bossX+bossWidth*5/2,bossY+bossHeight,screenWidth,screenHeight,0,bbt_kind2L,5));//自由落体运动
                                            bbtList.add(new bossbullet(screenWidth*k/100,bossY+bossHeight,screenWidth,screenHeight,5,bbt_kind2L));//随机
                                            bossdelay=0;
                                        }
                                        else if (bossdelay%600==0) bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind2));//变子弹只需要改这里
                                        break;
                                    case 1://NORMAL 加入阴阳玉 吸附效果  因为较小可以用正常大小         x方式31 (基于雪花方式3)
                                        if (bossdelay>=6000) { //
                                            soundId6=PigSoundPlayer.play("bossskill3",0);
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2,screenWidth,screenHeight,screenHeight/300,boss_skill00,31));//变子弹只需要改这里
                                            bossdelay=0;
                                        }
                                        else if (bossdelay%3000==0) { //重力弹射弹
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,me_skill,30,360*k/100,4));    //随机方向重力 + 5次弹射
                                            if(time<50||bosslife<bosslife_all/2)bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,me_skill,30,360*k/100+180,2));    //随机方向重力弹射 + 2次弹射

                                        }
                                        break;
                                    case 2://HARD 加入阴阳鬼神玉  因为较大几乎全屏，所以要用Rect绘制   x方式32 (基于雪花方式3)
                                        if (bossdelay>=12000) { //敌机子弹频度改这里
                                            soundId6=PigSoundPlayer.play("bossskill3",0);
                                            //bbtList.add(new bossbullet(bossX+bossWidth/2,-boss_skill01.getHeight(),screenWidth,screenHeight,screenHeight/500,boss_skill03,31,90,"rectangle"));//测试矩形：结果碰撞范围未旋转
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight/2,screenWidth,screenHeight,screenHeight/500,boss_skill01,31));//变子弹只需要改这里
                                            bossdelay=0;
                                        }
                                        else if (bossdelay==3000) { //重力弹射弹
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,me_skill,30,30,3));    //3次弹射
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,me_skill,30,150,3));    //3次弹射
                                            if(time<66||bosslife<bosslife_all*2/3)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,me_skill,30,360*k/100,2));    //随机方向重力弹射 + 2次弹射
                                            if(time<33||bosslife<bosslife_all/3)    bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,me_skill,30,360*k/100+180,1));    //随机方向重力弹射 + 2次弹射

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

    //主函数
    protected void myDraw() {
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
                        if(bbullet.getWay()==31) drawRotateBitmap(canvas, paint, bbullet.getBitmap(), bbullet.getRotation(), (int)bbullet.getX(), (int)bbullet.getY());
                        else canvas.drawBitmap(bbullet.getBitmap(), (int)bbullet.getX(), (int)bbullet.getY(), paint);
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
        bosslife=bosslife_all;
        //N H 第二阶段屏幕正中不动、第三阶段BOSS移至上居中不动
        if(dif>0){
            if(section==1){
                bossX=screenWidth/2-bossWidth/2;
                bossY=screenHeight/2-bossHeight/2;
                bossX_control=0;
            }
            else if(section==0){
                bossX=screenWidth/2-bossWidth/2;
                bossY=50;
                bossX_control=0;
            }
        }
        if(section==0) time=99;
        else time=45+15*dif;
        //清空屏幕上所有子弹
        bbtList.clear();//可设置为一个得分点，根据清除的数量
        bbtList.trimToSize();//
        //mybtList.clear();//会导致下方end函数闪退
        //mybtList.trimToSize();
    }

}

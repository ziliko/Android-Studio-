package com.example.once_a_day;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Timer;
import java.util.TimerTask;

public class StageA2_SV extends Stage_Father{//父类只能一个,接口可以多个
    //首行，可变数值
    private static final String TAG = "Stage2_SurfaceView";
    private static Stage2_SurfaceView Stage2;
    private int[] bosslife_all;//覆盖父类的bosslife_all

    // 构造函数
    public StageA2_SV(Context context,AttributeSet attrs){
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg_1p_2);
        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss2);
        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1_yellow);
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);
        bbt_kind3 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet3);

        boss_skill01=BitmapFactory.decodeResource(getResources(), R.drawable.bossskill21);

        this.initValue();//TODO 各关卡独有的初始化数值
        this.initSounds();//TODO 各关卡独有的背景音乐
        this.initTimerTask2();//TODO 各关卡独有的BOSS子弹风格
    }

    //所有变量的初始化
    private void initValue(){
        time=60;
        section=3;////各关的阶段数
        mylife=hp;//各关的自机初始血量 9999+ 为上帝模式 hp
        mypower=2;//各关的自机初始蓝量
        bosslife_all=new int[]{15000,10000,10000,10000};//各阶段的BOSS初始血量 TODO 与父类不同
        BOSS.bosslife=bosslife_all[section];
        BOSS.control=3;////各关的BOSS初始移动方式
        BOSS.speed=10;//各关的BOSS初始速度

    }
    //背景音乐定义
    private void initSounds() {
        mediaplayer = MediaPlayer.create(context, R.raw.nosound);// *1为当前上下文，*2为音频资源编号
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
                            bbullet.Move();                             //移动

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

                        //int k=(int)(Math.random()*100);

                        //boss自动发子弹   ※※※※此处选子弹※※※※
                        switch (section){
                            case 3://第一阶段 ☆崩岳☆ 跳起-下落- 一向，三才，四方，八荒

                                if(bossdelay>=1000){
                                    k=(int)(Math.random()*360);
                                    //40的概率，BOSS乱动
                                    if(Math.random()>0.6) {
                                        BOSS.toX=(int)((screenWidth-BOSS.bossWidth)*Math.random());
                                        BOSS.toY=(int)(screenHeight*2/3*Math.random());
                                    }
                                    k2=(int)(100*Math.cos((k+90)*Math.PI/180));
                                    k3=(int)(100*Math.sin((k+90)*Math.PI/180));
                                    bossdelay=0;
                                }
                                if (bossdelay>300&&bossdelay%200==0) {
                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2+k2,BOSS.bossY+BOSS.bossHeight/2+k3,screenWidth,screenHeight,10,bbt_kind2,121,k));    //
                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,10,bbt_kind2,121,k));    //
                                    bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2-k2,BOSS.bossY+BOSS.bossHeight/2-k3,screenWidth,screenHeight,10,bbt_kind2,121,k));    //
                                }

                                break;
                            case 2://第二阶段 ☆荡千军☆ 以自身为中心的旋转横扫，90°-180-270
                                //内环，外环，以及逆时针设计  关于子弹：TODO 不同速度(可double)，保持同步划过....
                                //满血~2/3血 且 时间未过1/3 TODO 一马当先(剪刀)
                                if(time>40&&BOSS.bosslife>bosslife_all[section]*2/3){
                                    if(bossdelay>=4000){
                                        k=(int)(Math.random()*360);
                                        k2=(int)(Math.random()*40);
                                        k3=(int)(Math.random()*40);
                                        bossdelay=0;
                                    }
                                    if (bossdelay>=3940) {
                                        for(double speed=0;speed<15;speed+=0.2)  if(speed*10<k2+10||speed*10>k2+25) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,speed,bbt_kind1,123,k));    //
                                        for(double speed=0;speed<15;speed+=0.2)  if(speed*10<k3+10||speed*10>k3+25) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,speed,bbt_kind1,123,k,-1));//逆时针
                                    }
                                }
                                //2/3血~1/3血 且 时间未过2/3 TODO
                                else if(time>20&&BOSS.bosslife>bosslife_all[section]/3){
                                    if(bossdelay>=1000){
                                        k=k-45-(int)(Math.random()*60);
                                        if(k<0) k+=360;
                                        k2=(int)(Math.random()*40);
                                        k3=(int)(Math.random()*40);
                                        bossdelay=0;
                                    }
                                    if (bossdelay>=940) {
                                        for(double speed=0;speed<15;speed+=0.2)
                                            if((speed*10<k2+20||speed*10>k2+35)&&(speed*10<k3+20||speed*10>k3+35)) {
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,speed,bbt_kind1,123,k,"123-1"));    //
                                                bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,speed,bbt_kind1,123,k+180,"123-1"));
                                            }
                                    }
                                }
                                //1/3血~0血 or 时间只剩下1/3 TODO 万夫莫开(大回旋)
                                else {
                                    if(bossdelay%100==0){
                                        if(Math.random()>0.5) {if(k2>10) k2-=3;}
                                        else if(k2<50) k2+=3;
                                        if(k2>55) k2=30;
                                        k3-=2;
                                        if(k3<=360) k3=720;
                                    }
                                    if (bossdelay>=3000&&bossdelay%100==0) { //speed空隙从1到6到1到6循环(伪心电图)
                                        for(double speed=0;speed<15;speed+=0.4)  if(speed*10<k2||speed*10>k2+20) bbtList.add(new bossbullet(BOSS.bossX+BOSS.bossWidth/2,BOSS.bossY+BOSS.bossHeight/2,screenWidth,screenHeight,speed,bbt_kind1,123,k3,"123-3"));    //

                                    }
                                }
                                break;
                            case 1://第三阶段 ☆云破天开☆ 斩裂一条线，然后线上布满弹幕，随后弹幕向中心/向周边减速扩散  然后以一线裂开消灭弹幕

                                break;
                            case 0://最终阶段 ★狂沙百战★ 旋转弹幕，时不时刮一道圆风刃 or 关卡1的炮  疏密变化：疏可以躲+输出+有风刃，密必须跟着转圈

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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        BOSS.toX=screenWidth/2-BOSS.bossWidth/2;
        BOSS.toY=50;
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

                    canvas.drawBitmap(bosshp,null,new Rect(10, 10, screenWidth*BOSS.bosslife/bosslife_all[section]-10, 30),paint);
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

    protected void section_change(){
        bossdelay=0;
        k=0;k2=0;k3=0;
        section--;
        soundId4=PigSoundPlayer.play("section_change",0);
        BOSS.bosslife=bosslife_all[section];
        if(section==2){
            BOSS.toX=screenWidth/2-BOSS.bossWidth/2;
            BOSS.toY=screenHeight/3-BOSS.bossHeight/2;
        }
        if(section==0){
            time=99;
            BOSS.bossX=screenWidth/2-BOSS.bossWidth/2;//ALL第三阶段重置到X轴中心位置（此关独有）
        }
        else time=60;
        //清空屏幕上所有子弹
        bbtList.clear();//可设置为一个得分点，根据清除的数量?
        bbtList.trimToSize();//
        //mybtList.clear();//会导致下方end函数闪退
        //mybtList.trimToSize();
    }

}
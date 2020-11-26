package com.example.once_a_day;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import java.util.Timer;
import java.util.TimerTask;

public class Stage2_SurfaceView extends Stage_Father{//父类只能一个,接口可以多个
    //首行，可变数值
    private static final String TAG = "Stage2_SurfaceView";
    private static Stage2_SurfaceView Stage2;

    // 构造函数
    public Stage2_SurfaceView(Context context,AttributeSet attrs){
        super(context,attrs);//添加控件需将SurfaceView的构造函数修改为两个参数的
        this.context = context;

        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);
        boss = BitmapFactory.decodeResource(getResources(), R.drawable.boss2);
        bbt_kind1= BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet1);
        bbt_kind2 = BitmapFactory.decodeResource(getResources(), R.drawable.boss_bullet2);
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
        bosslife_all=1800*(1+dif*2);//各关的BOSS初始血量
        bosslife=bosslife_all;
        bossX_control=1+(int)(Math.random()*2);////各关的BOSS初始移动方式   1 或2 的开局移动方式
        boss_speed=5;//各关的BOSS初始速度
    }
    //背景音乐定义
    private void initSounds() {
        mediaplayer = MediaPlayer.create(context, R.raw.music2);// *1为当前上下文，*2为音频资源编号
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

                        int k=(int)(Math.random()*100);
                        //boss自动发子弹   ※※※※此处选子弹※※※※
                        switch (section){
                            case 2://第一阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL
                                        if (bossdelay%600==0) { //单发弹(此处不改成%有奇效)
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//
                                        }
                                        if (bossdelay>=1800) { //弹射弹
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,45,5));//2方向扩散弹 + 5次弹射
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,135,5));
                                            bossdelay=0;
                                        }break;
                                    case 2://HARD
                                        if (bossdelay%300==0) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                        }
                                        if (bossdelay>=3600) { //弹射弹
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,30,5));//2方向扩散弹 + 2次弹射
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,60,5));
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,120,5));//2方向扩散弹 + 2次弹射
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,150,5));
                                            bossdelay=0;
                                        }break;

                                    default:break;//0
                                }
                                break;
                            case 1://第二阶段
                                //bosslife=20;//测试用例
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay>=600) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind1));//变子弹只需要改这里
                                            bossdelay=0;
                                        }break;
                                    case 1://NORMAL
                                        if (bossdelay>=900&&bossdelay%40==0) { //单发弹(此处不改成%有奇效)
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//
                                        }
                                        if (bossdelay>=1800) { //弹射弹
                                            for(int n=45;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,n,5));    //4方向扩散弹 + 5次弹射
                                            bossdelay=0;
                                        }break;
                                    case 2://HARD
                                        if (bossdelay>=1200&&bossdelay%40==0) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1));//变子弹只需要改这里
                                        }
                                        if (bossdelay>=3600) { //弹射弹
                                            for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,6,bbt_kind1,0,n,5));    //12方向扩散弹 + 5次弹射
                                            bossdelay=0;
                                        }break;
                                    default:break;//0
                                }
                                break;
                            case 0://第三阶段
                                switch (dif){
                                    case 0://EASY
                                        if (bossdelay%600==0) { //敌机子弹频度改这里
                                            bbtList.add(new bossbullet(bossX + bossWidth / 2, bossY + bossHeight, screenWidth, screenHeight, 20, bbt_kind1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX + bossWidth / 2, bossY + bossHeight, screenWidth, screenHeight, 10, bbt_kind1));//变子弹只需要改这里
                                            bbtList.add(new bossbullet(bossX + bossWidth / 2, bossY + bossHeight, screenWidth, screenHeight, 6, bbt_kind1));//变子弹只需要改这里
                                            //postInvalidate();
                                        }
                                        if(bossdelay>=3000) {
                                            bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20+k/10,bbt_kind1,0,2*k,8));    //随机速度方向弹+5次弹射
                                            bossdelay=0;
                                        }
                                        break;
                                    case 1://NORMAL
                                        //if(bossX!=screenWidth/2-bossWidth/2) bossX=screenWidth/2-bossWidth/2;//BOSS位置保持重置到屏幕中间() 或者变动?(图形无美感...?)
                                        if(bossdelay==3000) m=(int)(Math.random()*100);//每轮生成一次
                                        if (bossdelay>=3000&&bossdelay%200==0) { //连续弹射弹
                                            //随机30 45 60°起始
                                            if(time>66&&bosslife<bosslife_all*2/3)
                                                for(int n=45;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,10,bbt_kind1,0,n,3));    //4方向扩散弹 + 5次弹射
                                            //强化阶段
                                            else if(time>33&&bosslife<bosslife_all/3){
                                                if(m>50) for(int n=30;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,12,bbt_kind1,0,n,4));    //4方向扩散弹 + 5次弹射
                                                else for(int n=60;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,12,bbt_kind1,0,n,4));    //4方向扩散弹 + 5次弹射
                                            }
                                            //疯狂阶段
                                            else{
                                                if(m>66) for(int n=45;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,15,bbt_kind1,0,n,5));    //4方向扩散弹 + 5次弹射
                                                else if(m>33) for(int n=30;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,15,bbt_kind1,0,n,5));    //4方向扩散弹 + 5次弹射
                                                else for(int n=75;n<360;n+=90)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,15,bbt_kind1,0,n,5));    //4方向扩散弹 + 5次弹射
                                            }
                                        }
                                        if (bossdelay==4000) soundId6=PigSoundPlayer.play("bossskill2",0);//音效提前
                                        if (bossdelay>=5000) { //冈格尼尔
                                            //单发冈格尼尔动画 发射时前面的子弹暂停 射完清空全部/部分
                                            soundId6=PigSoundPlayer.play("bossskill2",0);//音效
                                            //canvas.drawLine(myX+myWidth/2, 0, myX+myWidth/2,screenHeight, paint);//预告线
                                            //canvas.drawLine(screenWidth*k/100, 0, screenWidth*k/100,screenHeight, paint);//预告线
                                            bbtList.add(new bossbullet(myX+myWidth/2,-boss_skill01.getHeight(),screenWidth,screenHeight,20,boss_skill01,0,90,"rectangle"));
                                            bbtList.add(new bossbullet(screenWidth*k/100,-boss_skill01.getHeight(),screenWidth,screenHeight,50,boss_skill01,0,90,"rectangle"));
                                            bossdelay=0;
                                        }break;
                                    case 2://HARD
                                        if (bossdelay==4000) soundId6=PigSoundPlayer.play("bossskill2",0);//音效提前
                                        if(bossdelay==5000) {
                                            m=(int)(Math.random()*100);
                                            //soundId6=PigSoundPlayer.play("bossskill2",0);//音效
                                            //canvas.drawLine(myX+myWidth/2, 0, myX+myWidth/2,screenHeight, paint);//预告线
                                            bbtList.add(new bossbullet(myX+myWidth/2,-boss_skill01.getHeight(),screenWidth,screenHeight,50,boss_skill01,0,90,"rectangle"));
                                        }//每轮生成一次
                                        if (bossdelay>=5000&&bossdelay%200==0) { //弹射弹
                                            //0°起始
                                            if(time>66&&bosslife>bosslife_all*2/3)
                                                for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1,0,n,3));    //12方向扩散弹 + 5次弹射
                                            // 强化阶段 45°起始
                                            else if(time>33&&bosslife>bosslife_all/3){
                                                for(int n=45;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1,0,n,3));    //12方向扩散弹 + 5次弹射
                                            }
                                            //疯狂阶段 随机0° 45°起始
                                            else{
                                                if(m>50) for(int n=0;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1,0,n,4));    //12方向扩散弹 + 5次弹射
                                                else for(int n=45;n<360;n+=30)  bbtList.add(new bossbullet(bossX+bossWidth/2,bossY+bossHeight,screenWidth,screenHeight,20,bbt_kind1,0,n,4));    //12方向扩散弹 + 5次弹射
                                            }
                                        }
                                        if (bossdelay==7000) soundId6=PigSoundPlayer.play("bossskill2",0);//音效提前
                                        if (bossdelay>=8000) { //连续随机冈格尼尔
                                            //多发冈格尼尔动画 发射时前面的子弹暂停/减速 射完清空全部/部分
                                            //soundId6=PigSoundPlayer.play("bossskill2",0);//音效
                                            bbtList.add(new bossbullet(myX+myWidth/2,-boss_skill01.getHeight(),screenWidth,screenHeight,50,boss_skill01,0,90,"rectangle"));
                                            bbtList.add(new bossbullet(screenWidth*k/100,-boss_skill01.getHeight(),screenWidth,screenHeight,50,boss_skill01,0,90,"rectangle"));
                                            bbtList.add(new bossbullet(screenWidth*(100-k)/100,-boss_skill01.getHeight(),screenWidth,screenHeight,50,boss_skill01,0,90,"rectangle"));
                                            if(time<70) {
                                                bbtList.add(new bossbullet(screenWidth * k / 50, -boss_skill01.getHeight() * 2, screenWidth, screenHeight, 50, boss_skill01, 0, 90, "rectangle"));
                                                bbtList.add(new bossbullet(screenWidth * k / 200, -boss_skill01.getHeight() * 2, screenWidth, screenHeight, 50, boss_skill01, 0, 90, "rectangle"));
                                            }
                                            if(time<40) {
                                                bbtList.add(new bossbullet(screenWidth * k / 30, -boss_skill01.getHeight() * 2, screenWidth, screenHeight, 50, boss_skill01, 0, 90, "rectangle"));
                                                bbtList.add(new bossbullet(screenWidth * k / 330, -boss_skill01.getHeight() * 2, screenWidth, screenHeight, 50, boss_skill01, 0, 90, "rectangle"));
                                            }
                                            //bbtList.add(new bossbullet(-boss_skill01.getWidth(),myY+myHeight/2,screenWidth,screenHeight,20,boss_skill01,0,0,"rectangle"));
                                            bossdelay=0;
                                        }break;
                                    default:break;//0
                                }
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

    protected void section_change(){
        bossdelay=0;
        section--;
        soundId4=PigSoundPlayer.play("section_change",0);
        bosslife=bosslife_all;
        if(section==0){
            time=99;
            bossX=screenWidth/2-bossWidth/2;//ALL第三阶段重置到X轴中心位置（此关独有）
            if(dif>0) bossX_control=0;//N H 第三阶段静止
        }
        else time=45+15*dif;
        //清空屏幕上所有子弹
        bbtList.clear();//可设置为一个得分点，根据清除的数量
        bbtList.trimToSize();//
        //mybtList.clear();//会导致下方end函数闪退
        //mybtList.trimToSize();
    }

}

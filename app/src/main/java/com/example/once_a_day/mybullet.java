package com.example.once_a_day;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class mybullet {
    Bitmap mybt;
    double x;
    double y;
    double start_x;
    double start_y;
    double center_x;//作为运动圆心 会变动
    double center_y;
    int w;//weith
    int h;//height
    boolean isBullet;//子弹存活标志
    double speed;
    double speed_x;
    double speed_y;
    int moveway;//方式 默认0
    int angle;//角度 默认270
    int screenw;//屏幕大小
    int screenh;
    int leftright;//单独用于分叉
    int movetime=0;//记录时间 50=1秒
    int move_section=0;//单子弹有不同阶段动作时用
    //构造函数
    public mybullet() {
    }
    //通用初始化部分
    public void mybullet_basic(int x, int y, int speed,Bitmap bitmap){
        this.x=x-bitmap.getWidth()/2;
        this.y=y-bitmap.getHeight();
        this.w=bitmap.getWidth();
        this.h=bitmap.getHeight();
        start_x=this.x+w/2;//记录圆心位置
        start_y=this.y+h/2;
        this.speed=speed;
        moveway=0;
        angle=270;
        this.mybt=bitmap;

    }
    //初始化构造函数  得到自机中上点坐标和子弹图形，数据需要处理 基本类型 直射 0
    public mybullet(int x, int y, int speed, Bitmap bitmap) {
        mybullet_basic(x,y,speed,bitmap);
        speed_y=-speed;
        isBullet=true;
    }
    //初始化构造函数  得到自机中上点坐标和子弹图形，数据需要处理 基本类型 散射 2 3
    public mybullet(int x, int y, int speed,Bitmap bitmap,int angle) {
        mybullet_basic(x,y,speed,bitmap);
        //this.moveway=moveway;
        this.angle=angle;
        speed_x=speed*Math.cos(Math.PI*angle/180);
        speed_y=speed*Math.sin(Math.PI*angle/180);
        isBullet=true;
    }
    //初始化构造函数  得到自机中上点坐标和子弹图形，数据需要处理 基本类型 加上基本方式
    public mybullet(int x, int y, int speed,Bitmap bitmap,int angle,int moveway) {
        mybullet_basic(x,y,speed,bitmap);
        //this.moveway=moveway;
        this.angle=angle;
        this.moveway=moveway;
        speed_x=speed*Math.cos(Math.PI*angle/180);
        speed_y=speed*Math.sin(Math.PI*angle/180);
        isBullet=true;
    }
    //初始化构造函数  得到自机中上点坐标和子弹图形，数据需要处理 基本类型 分叉射 1
    public mybullet(int x, int y, int speed, int screenw,int screenh,Bitmap bitmap,int leftright) {  //这个moveway暂时只用来区分左右分叉
        mybullet_basic(x,y,speed,bitmap);
        this.screenw=screenw;
        this.screenh=screenh;
        //this.moveway=moveway;
        this.leftright=leftright;
        speed_y=-speed;
        isBullet=true;
    }

    //返回Bitmap对象，用于绘制不同种类子弹
    public Bitmap getBitmap() {
        return mybt;
    }
    //子弹生命结束判断函数
    public boolean end()
    {
        if(isBullet) return false;
        else return true;//该子弹对象生命周期结束，清除
    }
    //
    public void Draw_Bullet(Canvas canvas) {
        //
    }
    //子弹移动
    public void Move() {
        if(isBullet) {
            movetime++;
            if(moveway==1) {
                if (movetime > 150) {
                    y += speed_y * 8;
                    x += speed_x * 8;
                }
            }
            else{
                if(screenw>0) {//仅用于爱心触发
                    //每次重置方向   根据中心--角度--角度+90--得到新的xy速度
                    //左-1右1
                    if(move_section<3){
                        double x=this.x+w/2;//局部变量，用于更精确计算
                        double y=this.y+h/2;
                        if(move_section==0){
                            center_x=start_x+leftright*screenw/12;
                            center_y=start_y;
                            if(y>start_y) move_section++;
                        }
                        else if(move_section==1){
                            center_x=start_x-leftright*screenw/6;
                            center_y=start_y;
                            if(y<start_y) move_section++;
                        }
                        else if(move_section==2){
                            center_x=start_x+leftright*screenw*5/6;
                            center_y=start_y;
                            if(leftright>0) {if(x>start_x) move_section++;}
                            else            {if(x<start_x) move_section++;}
                        }
                        if(leftright>0){
                            if(x>=center_x) angle=(int)Math.toDegrees(Math.atan((y-center_y)/(x-center_x)))+90;//注！这两句只能处理顺时针的情况
                            else angle=(int)Math.toDegrees(Math.atan((y-center_y)/(x-center_x)))+270;
                        }
                        else{
                            if(x>=center_x) angle=(int)Math.toDegrees(Math.atan((y-center_y)/(x-center_x)))-90;//注！这两句只能处理逆时针的情况
                            else angle=(int)Math.toDegrees(Math.atan((y-center_y)/(x-center_x)))-270;
                        }
                        speed_x=speed*Math.cos(Math.PI*angle/180);
                        speed_y=speed*Math.sin(Math.PI*angle/180);
                        this.y+=speed_y*(1+move_section);
                        this.x+=speed_x*(1+move_section);
                        //else 即没有动作
                    }
                    else{
                        y+=speed_y*8;
                        x+=speed_x*8;
                    }
                }
                else{
                    y+=speed_y*8;
                    x+=speed_x*8;
                }
            }

            if(y<=0) isBullet=false;//若是敌机子弹则需上下左右4个方向/下1个方向判定，需要screenw、h
        }
    }

    //子弹碰撞处理+ 提前结束生命
    public boolean crash(int bossX,int bossY,int bossW) {//加入中心位置的xy参数，判定半径参数
        //3参数前提-BOSS长宽一样，否则需要4参数
        if(isBullet){
            int dx=(bossX+bossW/2)-(int)(x+w/2);
            int dy=(bossY+bossW/2)-(int)(y+h/2);
            if (Math.sqrt(Math.pow(dx, 2)+ Math.pow(dy,2))<= bossW/2) {
                //if(Math.abs(centre_BOSS.bossX-centre_x)<r && Math.abs(centre_BOSS.bossY-centre_y)<r)
                isBullet=false;//提前结束生命
                return true;//调用敌机hp--的函数?
            }
        }
        return false;
    }
    public int getX() {
        return (int)x;
    }
    public int getY() {
        return (int)y;
    }
}

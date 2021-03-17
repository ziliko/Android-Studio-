package com.example.once_a_day;

//BOSS 对象
public class BOSS {
    //基本属性
    int bosslife_all;
    int bosslife;
    int speed;//boss移动速度
    int bossX;//当前坐标
    int bossY;
    int bossWidth = 0;
    int bossHeight = 0;
    //其他属性
    int screenWidth = 0;
    int screenHeight = 0;
    //要到达的目标坐标(仅当使用时)
    int toX;
    int toY;
    //运动模式
    int control;
    //特殊状态-如技能
    int buff_skill;
    int act_section;//动作阶段，某个动作阶段中不能被其他？？冲突
    int skill_time;
    //函数
    public void move(){
        if(control==1){
            bossX-=speed;
            if(bossX<=0) {bossX=0;control=2;}
        }
        else if(control==2){
            bossX+=speed;
            if(bossX>=screenWidth-bossWidth) {bossX=screenWidth-bossWidth;control=1;}
        }
        else if(control==3){//向目标点位移动
            //计算当前坐标和目标坐标的角度
            //根据角度把速度分配给X、Y速度
            //但是我选择简单点!
            if(bossX>toX) {
                bossX-=speed;
                if(bossX<toX) bossX=toX;
            }
            else if(bossX<toX) {
                bossX+=speed;
                if(bossX>toX) bossX=toX;
            }
            if(bossY>toY) {
                bossY-=speed;
                if(bossY<toY) bossY=toY;
            }
            else if(bossY<toY) {
                bossY+=speed;
                if(bossY>toY) bossY=toY;
            }

        }
    }


}
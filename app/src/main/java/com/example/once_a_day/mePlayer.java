package com.example.once_a_day;

import android.graphics.Bitmap;

//自机 对象
public class mePlayer {
    Bitmap mybt;
    double x;
    double y;

    int w;//weith  用于碰撞判定
    int h;//height

    int screenw;//屏幕大小
    int screenh;

    int player=0;  //自机角色 0 1 2 3
    int dif=0;  //难度等级 0 1 2
    int str = 10;       //力量，已加入函数
    int hp= 2;          //(和hp已有冲突)
    int dex= 6;         //灵巧，已加入函数
    int dex_area = 100;  //灵巧，已加入函数
    int skill=0;        //
}

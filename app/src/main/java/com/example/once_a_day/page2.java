package com.example.once_a_day;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
 **    该界面为游戏主界面，展示了除游戏内容以外的所有信息，包括公共，物品，整备，说明等
 *     主要功能：选择关卡进行游戏，改变装备、人物、难度等设置，修改账号密码、灵敏度等设置
 **    界面跳转：选择关卡--> page3界面
 **
 */
public class page2 extends AppCompatActivity {//implements Stage3_SurfaceView.stage3_return
    //系统公告
    private String notice="\n※※版本更新?.?.?※※\n咕咕 咕咕咕咕咕咕" +
            "\n※※版本更新1.1.5※※\n1.关卡初步完成\n2.音效BUG修复\n3.加入4角色,子弹各不同\n4.加入灵敏度设置" +
            "\n※※版本更新1.1.2※※\n1.三种难度设定\n2.高难度目前较粗糙" +
            "\n※※版本更新1.1.1※※\n1.界面优化+三阶段弹幕设计\n2.音效有BUG,重开app即可\n3.初始判断范围缩小一半"+
            "\n※※版本更新1.1.0※※\n1.界面重置+弹幕+擦弹机制+音效"+
            "\n※※版本更新1.0.8※※\n1.加入触屏控制"+
            "\n※※版本更新1.0.7※※\n1.技能系统已实装"+
            "\n※※版本更新1.0.5※※\n1.装备系统已实装\n2.云档功能不可用(需要服务器)\n3.装备合成实装需讨论\n4.新手上路，请多指BUG\n";
            ;

    private int player,difficuliy;
    private Spinner spinner1, spinner2;
    private static final String[] PLAYERS = {"白毛", "蓝毛","紫毛","画家?"};
    private static final String[] DIFFICULTY = {"EASY", "NORMAL", "HARD"};
    private ArrayAdapter<String> adapter1, adapter2;

    private MySQLite helper;
    private SQLiteDatabase mydb;
    private EditText edit1;
    private TextView text12,text52;//不同难度不同爆率
    private Button button001,button002,//云存档/读档
            button0,button1,button2,button3,button4,button5,//关卡按钮
            button61,button62,button63,button64,//功能按钮
            button71,button72,button73,button74;//功能按钮
    private TextView tv10;
    private SharedPreferences sp;//存放3个装备，以及5个判定？
    private static final String PREFERENCE_NAME="zks";
    int ide = 0;  //全局变量?
    int str,dex,dex_area,hp,skill;//全局变量：属性

    private Handler handler1, handler2;//网络编程
    private static final String IP = "192.168.43.164";
    private static final String LIST_URL = "http://" + IP + ":8081/ProductService/list.do";
    private static final String LIST_MESSAGE = "result1";
    private static final String VIEW_URL = "http://" + IP + ":8081/ProductService/view.do";//?id=5
    private static final String VIEW_MESSAGE = "result2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_page2);
        player=0;difficuliy=0;//默认
        this.initPigSoundPool();
        this.initDB();          //数据库初始化
        this.initViews();       //控件初始化
        this.initProperty();    //属性初始化
        this.initHandler();
        this.initListeners();
        this.initSpinner();
    }

    private void initPigSoundPool(){
        //PigSoundPlayer mySoundPlayer;
        //每关固定有6个ID，surfaceview destroy时需要根据ID清空当前soundpook
        PigSoundPlayer.initSoundPlayer(10,5);
        PigSoundPlayer.getLoader(page2.this).load("die",R.raw.die,2);                       //1
        PigSoundPlayer.getLoader(page2.this).load("garze",R.raw.garze,1);                   //2
        PigSoundPlayer.getLoader(page2.this).load("win",R.raw.win,3);                       //3
        PigSoundPlayer.getLoader(page2.this).load("section_change",R.raw.section_change,1); //4
        PigSoundPlayer.getLoader(page2.this).load("skill4",R.raw.skill4,1);                 //5
        PigSoundPlayer.getLoader(page2.this).load("skill3",R.raw.skill3,1);
        PigSoundPlayer.getLoader(page2.this).load("skill2",R.raw.skill2,1);
        PigSoundPlayer.getLoader(page2.this).load("skill1",R.raw.skill1,1);
        PigSoundPlayer.getLoader(page2.this).load("bossskill1",R.raw.bossskill1,1);         //6
        PigSoundPlayer.getLoader(page2.this).load("bossskill2",R.raw.bossskill2,1);
        PigSoundPlayer.getLoader(page2.this).load("bossskill3",R.raw.bossskill3,1);
        PigSoundPlayer.getLoader(page2.this).load("ice_break",R.raw.ice_break,1);
        PigSoundPlayer.getLoader(page2.this).load("sword_go",R.raw.sword_go,1);
        PigSoundPlayer.getLoader(page2.this).load("sword_crash",R.raw.sword_crash,1);
        PigSoundPlayer.getLoader(page2.this).load("wind",R.raw.wind,1);
    }

    private void initDB() {
        helper = new MySQLite(page2.this);
        mydb = helper.getWritableDatabase();

        Cursor cursor = mydb.rawQuery("SELECT * FROM " + helper.ITEM_NAME, null);   //cuisor 光标，也称游标 https://blog.csdn.net/android_zyf/article/details/53420267
        if (cursor.getCount() == 0) {//无数据则导入初始数据(存在问题第一次运行直接点抽卡会出错)
            mydb.execSQL("INSERT INTO item " +                  //后续加入合成：5个低级换1个高级
                    "select'1','A型源码','0','攻击+2'\n" +              //(10-max)攻击+2|4|6|10
                    "union all select'2','B型源码','0','判定-15|攻速+1'\n" +    //(100-1)(自机判定范围)-10%|20|30|49 && (基础6)攻速+1234
                    "union all select'3','C型源码','0','体力+1'\n" +    //(2-8)生命+1|2|3|4
                    "union all select'4','D型源码','0','skill1:滑稽一闪'\n" +    //获得技能，1234?
                    "union all select'5','E型源码','0','攻击+4'\n" +
                    "union all select'6','F型源码','0','判定-30|攻速+2'\n" +
                    "union all select'7','G型源码','0','体力+2'\n" +
                    "union all select'8','H型源码','0','skill2:怒稽连斩'\n" +
                    "union all select'9','I型源码','0','攻击+6'\n" +
                    "union all select'10','J型源码','0','判定-45|攻速+3'\n" +
                    "union all select'11','K型源码','0','体力+3'\n" +
                    "union all select'12','L型源码','0','skill3:时稽结界'\n" +
                    "union all select'13','M型源码','0','攻击+3|判定-20|攻速+1|体力+1'\n" +
                    "union all select'14','N型源码','0','攻击+10'\n" +
                    "union all select'15','O型源码','0','判定-60|攻速+4'\n" +
                    "union all select'16','P型源码','0','体力+4'\n" +
                    "union all select'17','Q型源码','0','skill4:诛稽剑阵'\n" +
                    "union all select'18','R型源码','0','攻击+5|判定-39|攻速+2|体力+2'\n"+
                    "union all select'19','上帝之眼','0','无敌,没有结算,用于鉴赏并查漏补缺'");
        }
    }

    private void initViews() {
        edit1 = (EditText) findViewById(R.id.edit01);
        text12=(TextView) findViewById(R.id.textView12);
        text52=(TextView) findViewById(R.id.textView52);
        button001 = (Button) findViewById(R.id.button001);
        button002 = (Button) findViewById(R.id.button002);
        button0 = (Button) findViewById(R.id.button00);
        button1 = (Button) findViewById(R.id.button01);
        button2 = (Button) findViewById(R.id.button02);
        button3 = (Button) findViewById(R.id.button03);
        button4 = (Button) findViewById(R.id.button04);
        button5 = (Button) findViewById(R.id.button05);
        button61 = (Button) findViewById(R.id.button061);
        button62 = (Button) findViewById(R.id.button062);
        button63 = (Button) findViewById(R.id.button063);
        button64 = (Button) findViewById(R.id.button064);
        button71 = (Button) findViewById(R.id.button071);
        button72 = (Button) findViewById(R.id.button072);
        button73 = (Button) findViewById(R.id.button073);
        button74 = (Button) findViewById(R.id.button074);
        tv10 = (TextView) findViewById(R.id.text10);
        tv10.setText(notice);//进入游戏自动显示公告
        sp = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
    }

    private void initProperty() {
        str=10;dex=6;dex_area=100;hp=2;skill=0;
        String equip1=sp.getString("equipment1","无");//A型源码
        String equip2=sp.getString("equipment2","无");
        String equip3=sp.getString("equipment3","无");
        if(!equip1.equals("无")) property_change(equip1);
        if(!equip2.equals("无")) property_change(equip2);
        if(!equip3.equals("无")) property_change(equip3);
    }

    private void initHandler() {
        handler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.getData().getString(LIST_MESSAGE);
                String right=result.substring(0,1);//判断读取成功时的返回首字符,substring 用于截取字符
                //tv2.setText(result);
                if(right.equals("1")){
                    String[] all=result.split("\n");//拆出18个数组
                    String[] all2={"0","0","0","0"};
                    String sql="";
                    int x,y;
                    for(int i=0;i<18;i++) {//循环拆出各数组4个内容，逐条修改数据库
                        all2=all[i].split(",");
                        try{//捕捉字符串非数字的异常
                            x=Integer.parseInt(all2[0]);
                            y=(int)Double.parseDouble(all2[2]);//字符串浮点数"98.0"是不能作为 int 类型解析的，所以抛出了异常。
                            sql = "UPDATE " + helper.ITEM_NAME + " SET number="+y+" WHERE id="+x;
                            mydb.execSQL(sql);
                            sql="";
                        }catch(NumberFormatException e){e.printStackTrace();}

                    };
                    new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("云读档成功！").show();
                    //Toast.makeText(page2.this, "云读档成功！", Toast.LENGTH_SHORT).show();
                }
                else new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("云读档失败！").show();
                //Toast.makeText(page2.this, "云读档失败！", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void initListeners() {
        /*
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.parseInt(edit1.getText().toString());
                String name = edit2.getText().toString();
                String major = edit3.getText().toString();
                //String major = Double.parseDouble(edit3.getText().toString());
                insertData(id, name, major);   //插入数据
                edit1.setText("");
                edit2.setText("");
                edit3.setText("");
                Toast.makeText(page2.this, "插入数据成功", Toast.LENGTH_SHORT).show();
            }
        });
         */
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(value==3)
                if(edit1.getText().toString().equals(""))//处理了输入为空时的闪退
                    new AlertDialog.Builder(page2.this).setTitle("警告").setMessage("请输入物品序号!").show();
                else{
                    int id = Integer.parseInt(edit1.getText().toString());
                    if(id==19) {updateData(id);Toast.makeText(page2.this, "已获得上帝之眼", Toast.LENGTH_SHORT).show();}
                    else Toast.makeText(page2.this, "你只能获得序号为19的物品", Toast.LENGTH_SHORT).show();
                    if(id>2020) updateData(id-2020);
                    //else Toast.makeText(page2.this, "修改数据成功", Toast.LENGTH_SHORT).show();
                }

            }
        });
        button001.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//云存档功能未实现,暂改成重置存档(危)
                AlertDialog.Builder dialog = new AlertDialog.Builder (page2.this);
                dialog.setTitle ("警告").setMessage ("该功能实际效果为清空存档，确定？");
                //点击确定就退出程序
                dialog.setPositiveButton ("确定", new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sql = "UPDATE " + helper.ITEM_NAME + " SET number=0";
                        mydb.execSQL(sql);
                        new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("你存档没了！").show();
                    }
                });
                //如果取消，就什么都不做，关闭对话框
                dialog.setNegativeButton ("取消",null);
                dialog.show ();
                //Toast.makeText(page2.this, "你存档没了！", Toast.LENGTH_SHORT).show();
                /*edit1.setText("");edit2.setText("");edit3.setText("");*/
            }
        });
        button002.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //按键获取信息，在Handle里修改数据库
                //new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("云读档中，请不要乱动......").show();

                //if() progressDialog.dismiss();
                Toast.makeText(page2.this, "云读档中，请稍候......", Toast.LENGTH_SHORT).show();
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url( LIST_URL)
                                    .build();
                            Response response = client.newCall( request). execute();
                            //String responseData = response.body().string();
                            //String result = parseJsonObject(responseData);//中转处理JSON格式
                            String result =response.body().string();

                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString(LIST_MESSAGE, result);
                            msg.setData(bundle);
                            handler1.sendMessage(msg);
                            /*
                                String jsonResult = EntityUtils.toString(response.getEntity(), "UTF-8");
                                String result = parseJsonObject(jsonResult);
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString(JSON_MESSAGE, jsonResult);
                                bundle.putString(FINAL_MESSAGE, result);
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                             */
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            String result = ex.toString();
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString(LIST_MESSAGE, result);
                            msg.setData(bundle);
                            handler1.sendMessage(msg);
                        }
                    }
                };
                thread.start();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(new Stage1_SurfaceView(page2.this));
                //方案一如上；方案二再创一个activity，按钮传参数Conteng5个关卡
                Update_SharedPreference();
                Intent intent1 = new Intent(page2.this, page3.class);
                intent1.putExtra("stage", "1");
                /*
                intent1.putExtra("player", player);
                intent1.putExtra("dif", difficuliy);
                intent1.putExtra("str", str);
                intent1.putExtra("dex", dex);
                intent1.putExtra("dex_area", dex_area);
                intent1.putExtra("hp", hp);
                intent1.putExtra("skill", skill);
                 */
                startActivity(intent1);
                //不关闭当前page2
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(new Stage1_SurfaceView(page2.this));
                //方案一如上；方案二再创一个activity，按钮传参数Conteng5个关卡
                Update_SharedPreference();
                Intent intent1 = new Intent(page2.this, page3.class);
                intent1.putExtra("stage", "2");

                startActivity(intent1);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(new Stage1_SurfaceView(page2.this));
                //方案一如上；方案二再创一个activity，按钮传参数Conteng5个关卡
                Update_SharedPreference();
                Intent intent1 = new Intent(page2.this, page3.class);
                intent1.putExtra("stage", "3");

                startActivity(intent1);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(new Stage1_SurfaceView(page2.this));
                //方案一如上；方案二再创一个activity，按钮传参数Conteng5个关卡
                Update_SharedPreference();
                Intent intent1 = new Intent(page2.this, page3.class);
                intent1.putExtra("stage", "4");

                startActivity(intent1);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setContentView(new Stage1_SurfaceView(page2.this));
                //方案一如上；方案二再创一个activity，按钮传参数Conteng5个关卡
                Update_SharedPreference();
                Intent intent1 = new Intent(page2.this, page3.class);
                intent1.putExtra("stage", "5");

                startActivity(intent1);
            }
        });
        /*
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.parseInt(edit1.getText().toString());
                removeData(id);
                edit1.setText("");
                edit2.setText("");
                edit3.setText("");
                Toast.makeText(page2.this, "删除数据成功", Toast.LENGTH_SHORT).show();
            }
        });
         */
        button61.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(page2.this, register.class);//registerActivity
                startActivity(intent1);//跳转到修改账号密码界面
            }
        });
        button62.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//改灵敏度   //(待实现)装备合成 --涉及概率调整，兑换数目策划，平衡性等
                Intent intent1 = new Intent(page2.this, setting.class);//registerActivity
                startActivity(intent1);//跳转到修改设置界面
                /*edit1.setText("");edit2.setText("");edit3.setText("");*/
            }
        });
        button63.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//进行装备
                final String[] items = {"无","A" ,"B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","上帝之眼","清空装备"};
                final AlertDialog.Builder dialog4 = new AlertDialog.Builder (page2.this)
                        .setTitle ("选择你要装备的源码:")
                        //参数1：选项。参数2：默认选项。参数3：选中时的事件
                        .setSingleChoiceItems (items, 0, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText (MainActivity.this,"你选择了："+stars[which],Toast.LENGTH_LONG).show ();
                                ide = which;
                            }
                        }).setPositiveButton ("确定", new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String equip1=sp.getString("equipment1","无");//A型源码
                                String equip2=sp.getString("equipment2","无");
                                String equip3=sp.getString("equipment3","无");
                                if(ide==20) {//清空背包(清空同时把物品数量归还到背包数据库)
                                    //if(S.1非空) sql;并清零
                                    if(!equip1.equals("无")){
                                        String sql = "UPDATE " + helper.ITEM_NAME + " SET number=number+1 WHERE name='" + equip1+"'";//字符串需加单引号?
                                        mydb.execSQL(sql);
                                    }
                                    if(!equip2.equals("无")){
                                        String sql = "UPDATE " + helper.ITEM_NAME + " SET number=number+1 WHERE name='" + equip2+"'";//字符串需加单引号
                                        mydb.execSQL(sql);
                                    }
                                    if(!equip3.equals("无")){
                                        String sql = "UPDATE " + helper.ITEM_NAME + " SET number=number+1 WHERE name='" + equip3+"'";//字符串需加单引号
                                        mydb.execSQL(sql);
                                    }
                                    SharedPreferences.Editor editor=sp.edit();
                                    editor.remove("equipment1");editor.remove("equipment2");editor.remove("equipment3");
                                    editor.remove("kind0");editor.remove("kind1");editor.remove("kind2");editor.remove("kind3");editor.remove("kind4");editor.remove("kind5");editor.remove("kind6");
                                    str=10;dex=6;dex_area=100;hp=2;skill=0;     //editor.putInt("str",10);editor.putInt("dex",6);editor.putInt("dex_area",100);editor.putInt("hp",2);editor.putInt("skill",0);//属性初始化
                                    editor.commit();
                                    new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("已清空装备并归还至背包!").show();
                                    //Toast.makeText (page2.this,"已清空装备并归还至背包!",Toast.LENGTH_LONG).show ();
                                }
                                else if(0<ide&&ide<20) {
                                    /**/
                                    String sql= "SELECT * FROM " + helper.ITEM_NAME+" WHERE id="+ide;
                                    Cursor cursor = mydb.rawQuery(sql, null);   //cuisor 光标，也称游标 https://blog.csdn.net/android_zyf/article/details/53420267
                                    String result = "kind"+kind_judge(ide);
                                    int kind=sp.getInt(result,0);
                                    while (cursor.moveToNext()) {
                                        String name = cursor.getString(1);
                                        int number = cursor.getInt(2);
                                        if(!equip3.equals("无"))Toast.makeText (page2.this,"装备栏已满，请先清空(在选项最下面)!",Toast.LENGTH_LONG).show ();
                                        else if(number==0) Toast.makeText (page2.this,"该物品数量为0，无法装备!",Toast.LENGTH_LONG).show ();
                                        else if(kind!=0) Toast.makeText (page2.this,"同类型装备只能穿一个!",Toast.LENGTH_LONG).show ();
                                        else {
                                            SharedPreferences.Editor editor=sp.edit();
                                            editor.putInt(result,1);    //将该种类置为1，防止重复装备
                                            String sql2 = "UPDATE " + helper.ITEM_NAME + " SET number=number-1 WHERE id=" + ide;
                                            mydb.execSQL(sql2);         //数据库减一
                                            property_change(ide);       //更新属性
                                            if(equip1.equals("无")) editor.putString("equipment1",name);
                                            else if(equip2.equals("无")) editor.putString("equipment2",name);
                                            else editor.putString("equipment3",name);
                                            editor.commit();
                                            if(ide==19) new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("成功装备上帝之眼").show();
                                            else new AlertDialog.Builder(page2.this).setTitle("提示").setMessage("成功装备"+items[ide]+"型源码!").show();
                                            //Toast.makeText (page2.this,"成功装备"+items[ide]+"型源码!",Toast.LENGTH_LONG).show ();
                                        }
                                    }
                                }
                                ide=0;//重置
                            }
                        });   //点击确定后对话框消失，并且打印所选内容
                dialog4.show ();
                //Intent intent1 = new Intent(page2.this, Dialog.class);//registerActivity
                //startActivity(intent1);//跳转到
            }
        });
        button64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder (page2.this);
                dialog.setTitle ("温馨提示").setMessage ("你确定要退出游戏嘛？");
                //点击确定就退出程序
                dialog.setPositiveButton ("确定", new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                //如果取消，就什么都不做，关闭对话框
                dialog.setNegativeButton ("取消",null);
                dialog.show ();
                //System.exit(0);
                //finish();//结束游戏
            }
        });
        button71.setOnClickListener(new View.OnClickListener() {//显示：系统公告
            @Override
            public void onClick(View v) {
                if(tv10.getText().toString().equals("")) tv10.setText(notice);
                else tv10.setText(null);//关闭背包(清空文本框)
            }
        });
        button72.setOnClickListener(new View.OnClickListener() {//显示：属性面板
            @Override
            public void onClick(View v) {//显示：属性面板
                String desc="";
                if(skill==1) desc+="\n技能效果:无敌1秒并造成攻击力x5的一段伤害";
                else if(skill==2) desc+="\n技能效果:无敌2秒并造成攻击力x2的三段伤害";
                else if(skill==3) desc+="\n技能效果:无敌3秒并暂停时间,只有你能行动,3秒后开始结算";
                else if(skill==4) desc+="\n技能效果:无敌3秒并控制BOSS,造成攻击力x2的七段伤害";
                desc+="\n";
                String property="\n攻击力："+str+"\n攻速："+dex+"\n判定范围："+dex_area+"\n体力："+hp+"\nSKILL："+skill+desc;
                if(tv10.getText().toString().equals("")) tv10.setText(property);//关闭背包(清空文本框)
                else tv10.setText(null);//关闭背包(清空文本框)
            }
        });
        button73.setOnClickListener(new View.OnClickListener() {//显示：已穿装备
            @Override
            public void onClick(View v) {
                String equip1=sp.getString("equipment1","无");//A型源码
                String equip2=sp.getString("equipment2","无");
                String equip3=sp.getString("equipment3","无");
                String equipment="\n装备一："+equip1+"\n装备二："+equip2+"\n装备三："+equip3;
                if(tv10.getText().toString().equals("")) tv10.setText(equipment);//关闭背包(清空文本框)
                else tv10.setText(null);//关闭背包(清空文本框)
            }
        });
        button74.setOnClickListener(new View.OnClickListener() {//显示：查看背包
            @Override
            public void onClick(View v) {
                if(tv10.getText().toString().equals("")) queryData();//查看背包(将数据库数据导入文本框)
                else tv10.setText(null);//关闭背包(清空文本框)
            }
        });
    }

    private void initSpinner(){
        spinner1 = (Spinner) findViewById(R.id.spinner01);
        spinner2 = (Spinner) findViewById(R.id.spinner02);
        adapter1 = new ArrayAdapter<String>(page2.this, android.R.layout.simple_spinner_item, PLAYERS);
        adapter2 = new ArrayAdapter<String>(page2.this, android.R.layout.simple_spinner_item, DIFFICULTY);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (player!=0&&position==0) {
                    player=position;
                    Toast.makeText(page2.this, "白毛控!", Toast.LENGTH_SHORT).show();
                }
                else if (position==1) {
                    player=position;
                    Toast.makeText(page2.this, "稳中求胜", Toast.LENGTH_SHORT).show();
                }
                else if (position==2) {
                    player=position;
                    Toast.makeText(page2.this, "给我你的心作纪念", Toast.LENGTH_SHORT).show();
                }
                else if (position==3) {
                    player=position;
                    Toast.makeText(page2.this, "一个病娇的画家?", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}//text4.setText("任何选项都没被选！");
        });
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //原版：20 15 10 5
                if (position == 0&&difficuliy!=0) {
                    difficuliy=0;
                    text12.setText("40 10 0  0");text52.setText("10 0");
                    Toast.makeText(page2.this, "无装备也可上手的程度", Toast.LENGTH_SHORT).show();
                }
                else if (position == 1) {
                    difficuliy=1;
                    text12.setText("0  39 10 1");text52.setText("19 1");
                    Toast.makeText(page2.this, "正常游戏的程度", Toast.LENGTH_SHORT).show();
                }
                else if (position == 2) {
                    difficuliy=2;
                    text12.setText("0  0  40 10");text52.setText("40 10");
                    Toast.makeText(page2.this, "乱七八糟的程度", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}//text4.setText("任何选项都没被选！");
        });
    }

    private void Update_SharedPreference(){
        SharedPreferences.Editor editor=sp.edit();

        editor.putInt("player", player);
        editor.putInt("dif", difficuliy);
        editor.putInt("str", str);
        editor.putInt("dex", dex);
        editor.putInt("dex_area", dex_area);
        editor.putInt("hp", hp);
        editor.putInt("skill", skill);
        //editor.putInt("sensitivity", 100);

        editor.commit();
    }

    //字符串需加单引号!!!!!!!!!!!!!!!!!!!!!!!!VALUES (1,'Tom' ,90.0)|
    private void insertData(int id, String name, String major) {
        String sql = "INSERT INTO " + helper.ITEM_NAME + " (id,name,major) VALUES (" + id + ",'" + name + "','" + major + "')";//字符串需加单引号
        mydb.execSQL(sql);
    }//INSERT INTO student(1,zks,100)

    private void updateData(int id) {//更新-即抽到的物品数量+1
        //String sql = "UPDATE " + helper.TABLE_NAME + " SET major=" + major + " WHERE id=" + id;
        String sql = "UPDATE " + helper.ITEM_NAME + " SET number=number+1 WHERE id=" + id;
        mydb.execSQL(sql);
    }/*UPDATE student
    SET credit=100 WHERE id=1*/

    private void removeData(int id) {
        String sql = "DELETE FROM " + helper.ITEM_NAME + " WHERE id=" + id;
        mydb.execSQL(sql);
    }//DELETE FROM student WHERE id=1

    private void queryData() {  //SELECT * FROM student
        //String sql= "SELECT * FROM " + helper.TABLE_NAME;
        String result = null;
        String sql= "SELECT description FROM "+ helper.ITEM_NAME + " WHERE id=2";//版本更新，数据库描述变动时用来更新数据？
        Cursor cursor = mydb.rawQuery(sql, null);   //cuisor 光标，也称游标 https://blog.csdn.net/android_zyf/article/details/53420267
        while (cursor.moveToNext()) {//需要改成表格输出？
            String des = cursor.getString(0);
            //要更新的数据写在这里
            if (des.equals("判定-10|攻速+1")) {
                mydb.execSQL("INSERT INTO item select'19','上帝之眼','0','无敌,没有结算,用于鉴赏并查漏补缺'");
                mydb.execSQL("UPDATE item SET description='判定-15|攻速+1' WHERE id=2");
                mydb.execSQL("UPDATE item SET description='判定-30|攻速+2' WHERE id=6");
                mydb.execSQL("UPDATE item SET description='判定-45|攻速+3' WHERE id=10");
                mydb.execSQL("UPDATE item SET description='攻击+3|判定-20|攻速+1|体力+1' WHERE id=13");
                mydb.execSQL("UPDATE item SET description='判定-60|攻速+4' WHERE id=15");
                mydb.execSQL("UPDATE item SET description='攻击+5|判定-39|攻速+2|体力+2' WHERE id=18");
            }
        }
        sql= "SELECT * FROM " + helper.ITEM_NAME;
        cursor = mydb.rawQuery(sql, null);   //cuisor 光标，也称游标 https://blog.csdn.net/android_zyf/article/details/53420267
        result = "\n";
        while (cursor.moveToNext()) {//需要改成表格输出？
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            int number = cursor.getInt(2);
            String des = cursor.getString(3);
            //String temp = "序号:"+id + ",物品名:" + name + ",数量:" + number + "\n";
            String temp = id + "," + name + "x" + number + ":"+des+"\n";
            result += temp;
        }
        tv10.setText(result);
    }

    private int kind_judge(int id) {//源码5种类判别函数
        if(id==1||id==5||id==9||id==14)         return 1;
        else if(id==2||id==6||id==10||id==15)   return 2;
        else if(id==3||id==7||id==11||id==16)   return 3;
        else if(id==4||id==8||id==12||id==17)   return 4;
        else if(id==13||id==18)                 return 5;
        else if(id==19)                         return 6;
        else return 0;
    }//DELETE FROM student WHERE id=1

    private void property_change(String equip){//源码效果实装函数
        switch (equip){
            case "A型源码":{str+=2;break;}
            case "B型源码":{dex+=1;dex_area-=15;break;}
            case "C型源码":{hp+=1;break;}
            case "D型源码":{skill=1;break;}
            case "E型源码":{str+=4;break;}
            case "F型源码":{dex+=2;dex_area-=30;break;}
            case "G型源码":{hp+=2;break;}
            case "H型源码":{skill=2;break;}
            case "I型源码":{str+=6;break;}
            case "J型源码":{dex+=3;dex_area-=45;break;}
            case "K型源码":{hp+=3;break;}
            case "L型源码":{skill=3;break;}
            case "M型源码":{str+=3;dex+=1;dex_area-=20;hp+=1;break;}
            case "N型源码":{str+=10;break;}
            case "O型源码":{dex+=4;dex_area-=60;break;}
            case "P型源码":{hp+=4;break;}
            case "Q型源码":{skill=4;break;}
            case "R型源码":{str+=5;dex+=2;dex_area-=39;hp+=2;break;}
            case"上帝之眼":{hp+=9999;break;}
            default:break;
        }
    }
    private void property_change(int id){//源码效果实装函数
        switch (id){
            case 1:{str+=2;break;}
            case 2:{dex+=1;dex_area-=15;break;}
            case 3:{hp+=1;break;}
            case 4:{skill=1;break;}
            case 5:{str+=4;break;}
            case 6:{dex+=2;dex_area-=30;break;}
            case 7:{hp+=2;break;}
            case 8:{skill=2;break;}
            case 9:{str+=6;break;}
            case 10:{dex+=3;dex_area-=45;break;}
            case 11:{hp+=3;break;}
            case 12:{skill=3;break;}
            case 13:{str+=3;dex+=1;dex_area-=20;hp+=1;break;}
            case 14:{str+=10;break;}
            case 15:{dex+=4;dex_area-=60;break;}
            case 16:{hp+=4;break;}
            case 17:{skill=4;break;}
            case 18:{str+=5;dex+=2;dex_area-=39;hp+=2;break;}
            case 19:{hp+=9999;break;}
            default:break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder dialog = new AlertDialog.Builder (page2.this);
            dialog.setTitle ("温馨提示").setMessage ("你确定要退出游戏嘛？");
            //点击确定就退出程序
            dialog.setPositiveButton ("确定", new DialogInterface.OnClickListener () {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            //如果取消，就什么都不做，关闭对话框
            dialog.setNegativeButton ("取消",null);
            dialog.show ();
        }
        return false;
    }
    /*
    public void onRestart(){//每次结束游戏返回该界面时
        super.onRestart();
        PigSoundPlayer.release(5,null,5);//会失去所有音效,
        //initPigSoundPool();
        Toast.makeText(page2.this, "测试重置", Toast.LENGTH_SHORT).show();
    }
     */
}

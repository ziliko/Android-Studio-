package com.example.once_a_day;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/*
**    该界面为登录界面，是整个APP的入口
**    主要功能为：登录--> 进入page2界面
**    输入账号、密码。可在登录成功以后修改。
*/
public class MainActivity extends AppCompatActivity {

    private Button button1,button2;
    private EditText edit1,edit2;
    private SharedPreferences sp;
    private static final String PREFERENCE_NAME="zks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit1 = (EditText) findViewById(R.id.edit01);
        edit2 = (EditText) findViewById(R.id.edit02);
        sp = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        //加密
        String login =sp.getString("login","");
        if(login.equals("")) {
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("login","zi");
            editor.putString("password","");
            editor.commit();
        }
        /*此处手动记录zks(SharePreferences)里的所有内容(共计10个，一般同时存在0-8个)
            账号login 密码password                    (未修改时其实是无的)
            装备equipment1、equipment2、equipment3             (可无/可有)
            装备种类判别kind1、kind2、kind3、kind4、kind5     (平时可无)
            //(不需要，直接page2全局变量然后传过去)  五个属性 str、dex、dex_area、hp、skill          (需要初始化)
            ++增加8个 str、dex、dex_area、hp、skill      player dif      sensitivity   在加载关卡时初始化  关卡内读取
         */

        button1 = (Button) findViewById(R.id.button01);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login =sp.getString("login","");

                String zks1=edit1.getText().toString();//trim()方法去除字符串的头尾空格:
                String zks2=edit2.getText().toString();
                if(login!=null)
                {
                    if(zks1.equals(login.trim()))
                    {
                        String password =sp.getString("password","");
                        if(zks2.equals(password.trim()))
                        {
                            Intent intent1 = new Intent(MainActivity.this, page2.class);
                            startActivity(intent1);
                            finish();//跳转后关闭登录界面
                        }
                        else new AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("密码错误").show();
                    }
                    else new AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("该用户不存在").show();
                }
                else new AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("还未注册").show();

            }
        });
        button2 = (Button) findViewById(R.id.button02);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

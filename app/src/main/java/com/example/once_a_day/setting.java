package com.example.once_a_day;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class setting extends AppCompatActivity {

    private Button button1,button2;
    private TextView tv1,tv2,tv3;
    private SeekBar SB1,SB2,SB3;

    private int sensitivity=0;
    private SharedPreferences sp;
    private static final String PREFERENCE_NAME="zks";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_setting);

        SB1= (SeekBar) findViewById(R.id.seekBar1);
        SB2= (SeekBar) findViewById(R.id.seekBar2);
        SB3= (SeekBar) findViewById(R.id.seekBar3);
        tv1 = (TextView) findViewById(R.id.text12);
        tv2 = (TextView) findViewById(R.id.text22);
        tv3 = (TextView) findViewById(R.id.text32);
        button1 = (Button) findViewById(R.id.button01);
        button2 = (Button) findViewById(R.id.button02);
        sp = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        sensitivity=sp.getInt("sensitivity",100);
        SB1.setProgress(sensitivity);
        tv1.setText(""+sensitivity);//要String类型？

        SeekBarListener_init();
        ButtonListener_init();
    }

    private void SeekBarListener_init(){
        SB1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度发生改变时会触发 以10为单位
                sensitivity=progress/10*10;
                tv1.setText(""+sensitivity);//要String类型？
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //按住SeekBar时会触发
                Toast.makeText(setting.this, "ha na si te！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //放开SeekBar时触发
                Toast.makeText(setting.this, "Hentai！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ButtonListener_init(){
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //确认修改
                SharedPreferences.Editor editor=sp.edit();
                editor.putInt("sensitivity", sensitivity);
                editor.commit();
                new AlertDialog.Builder(setting.this).setTitle("提示").setMessage("修改成功!").show();

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent1 = new Intent(register.this, MainActivity.class);
                //startActivity(intent1);
                Intent intent1 = new Intent(setting.this, page2.class);
                startActivity(intent1);
                //finish();//因为page2是single模式，所以跳转后自动关闭这个
                //问题：若之前未关闭登录界面则此时又创造一个登陆界面，即有2个
            }
        });
    }

}

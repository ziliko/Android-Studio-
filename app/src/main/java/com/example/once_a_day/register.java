package com.example.once_a_day;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class register extends AppCompatActivity {

    private Button button1,button2;
    private EditText edit1,edit2;
    private SharedPreferences sp;
    private static final String PREFERENCE_NAME="zks";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edit1 = (EditText) findViewById(R.id.edit01);
        edit2 = (EditText) findViewById(R.id.edit02);
        sp = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        button1 = (Button) findViewById(R.id.button01);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=sp.edit();
                String zks3=edit1.toString().trim();
                String zks4=edit2.toString().trim();
                //editor.putString("login","zks");
                //editor.putString("password","123");
                editor.putString("login",edit1.getText().toString());
                editor.putString("password",edit2.getText().toString());
                editor.commit();

                Toast.makeText(register.this, "修改成功！", Toast.LENGTH_SHORT).show();
            }
        });
        button2 = (Button) findViewById(R.id.button02);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent1 = new Intent(register.this, MainActivity.class);
                //startActivity(intent1);
                finish();//跳转后关闭本界面
                //问题：若之前未关闭登录界面则此时又创造一个登陆界面，即有2个
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
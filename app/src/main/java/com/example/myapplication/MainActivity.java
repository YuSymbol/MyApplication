package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    //  输入logt，利用tab会自动补全当前的类名。更新
    private static final String TAG = "MainActivity";
    //  main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        //  打印日志的功能，必须学会习惯使用
        //  在Logcat栏显示，可以通过筛选重要级别和关键字（例如类名以分类）来查看
        Log.d(TAG,"onCreate execute");

    }
}

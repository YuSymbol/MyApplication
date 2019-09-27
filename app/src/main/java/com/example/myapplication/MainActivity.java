package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView dateTv;
    private TextView locationTv;
    private TextView weekdayTv;

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

        dateTv = (TextView)findViewById(R.id.tv_demo_date);
        locationTv = (TextView)findViewById(R.id.tv_demo_locaiton);
        weekdayTv = (TextView)findViewById(R.id.tv_demo_weekday);

        dateTv.setText(getFormatDate());
        weekdayTv.setText(getWeekday());



    }

    private String getFormatDate(){
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("MM/dd\nyyyy");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    private String getWeekday(){
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        return convertNumToWeekday(week);

    }

    private String convertNumToWeekday(int week){
        switch (week){
            case 1:
                return "SUN";
            case 2:
                return "MON";
            case 3:
                return "TUES";
            case 4:
                return "WED";
            case 5:
                return "THUS";
            case 6:
                return "FRI";
            case 7:
                return "SAT";
            default:
                Log.d(TAG, "convertNumToWeekday:week input error!");
                return null;
        }
        
    }



}

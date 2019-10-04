package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.util.HttpUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import interfaces.heweather.com.interfacesmodule.*;

public class MainActivity extends AppCompatActivity {

    private TextView dateTv;
    private TextView locationTv;
    private TextView weekdayTv;
    private TextView testTv;
    private final String KEY = "69f2cb642a1646379bdb680390c377c7";  //  访问天气API的key
    private final String DAXING_ID = "CN101011100"; //  大兴区的id

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

        //  和风天气api初始化
        HeConfig.init("HE1909200100361711", "69f2cb642a1646379bdb680390c377c7");
        //  免费接口要转换成免费节点
        HeConfig.switchToFreeServerNode();


        dateTv = (TextView)findViewById(R.id.tv_demo_date);
        locationTv = (TextView)findViewById(R.id.tv_demo_locaiton);
        weekdayTv = (TextView)findViewById(R.id.tv_demo_weekday);
        testTv = (TextView)findViewById(R.id.tv_demo_test);

        dateTv.setText(getFormatDate());
        weekdayTv.setText(getWeekday());



        //  使用guolin提供的域名访问数据
//        requestWeatherInfo(DAXING_ID);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //  将SDK的请求天气方法放在start环节调用
        requestWeatherInfoBySDK(DAXING_ID);
    }

    private void requestWeatherInfoBySDK(String cityId){

        /**
         * 实况天气
         * 实况天气即为当前时间点的天气状况以及温湿风压等气象指数，具体包含的数据：体感温度、
         * 实测温度、天气状况、风力、风速、风向、相对湿度、大气压强、降水量、能见度等。
         * @param context  上下文
         * @param location 地址详解
         * @param lang       多语言，默认为简体中文
         * @param unit        单位选择，公制（m）或英制（i），默认为公制单位
         * @param listener  网络访问回调接口
         */
        HeWeather.getWeatherNow(MainActivity.this, cityId, Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC, new HeWeather.OnResultWeatherNowBeanListener() {

                    public static final String TAG = "WeatherNow";
                    @Override
                    public void onError(Throwable throwable) {
                        Log.i(TAG, "on Error: ", throwable);
                        System.out.println("Weather now error: "+new Gson());
                    }

                    @Override
                    public void onSuccess(Now dataObject) {
                        Log.i(TAG, "Weather Now Success: "+new Gson().toJson(dataObject));

                        String jsonData = new Gson().toJson(dataObject);
                        String weather = null;
                        String temp = null;
                        String weatherCode = null;
                        if(dataObject.getStatus().equals("ok")){
                            String jsonNow = new Gson().toJson(dataObject.getNow());
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(jsonNow);
                                weather = jsonObject.getString("cond_txt");
                                temp = jsonObject.getString("tmp");
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "读取天气数据不存在", Toast.LENGTH_LONG).show();
                            return;
                        }
                        testTv.setText(weather+"\t"+temp);

                    }
                });

    }

    //  请求天气
    public void requestWeatherInfo(String cityId){

        //  拼接url地址
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+cityId+"&key="+KEY;

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "天气信息读取失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                //  读取访问页面主体中的字符串部分
                final String responseText = response.body().string();
                Log.d(TAG, "onResponse: "+responseText);
            }
        });

    }


    //  设置日期
    private String getFormatDate(){
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("MM/dd\nyyyy");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    //  设置星期
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

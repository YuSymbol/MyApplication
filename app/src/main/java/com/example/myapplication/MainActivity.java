package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.service.UpdateWeatherService;
import com.example.myapplication.util.HttpUtil;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
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

    private TextView tempNowTv;
    private TextView weatherNowTv;

    private TextView windTv;
    private TextView tempTv;
    private TextView weatherTv;

    private ImageView weatherIv;

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

        //  和风天气初始化
        heWeatherInit();
        //  各种控件初始化
        viewInit();

        requestWeatherNowBySDK(DAXING_ID);
        requestWeatherDailyBySDK(DAXING_ID);
        //  放在onCreate阶段调用以确保不会看到离线内容
        dateTv.setText(getFormatDate());
        weekdayTv.setText(getWeekday());
        //  将SDK的请求天气方法放在start环节调用

        Intent intent = new Intent(this, UpdateWeatherService.class);
        startService(intent);

    }

    private void heWeatherInit(){
        //  和风天气api初始化
        HeConfig.init("HE1909200100361711", "69f2cb642a1646379bdb680390c377c7");
        //  免费接口要转换成免费节点
        HeConfig.switchToFreeServerNode();
    }

    private void viewInit(){
        dateTv = (TextView)findViewById(R.id.tv_demo_date);
        locationTv = (TextView)findViewById(R.id.tv_demo_locaiton);
        weekdayTv = (TextView)findViewById(R.id.tv_demo_weekday);
        testTv = (TextView)findViewById(R.id.tv_demo_test);

        tempNowTv = (TextView)findViewById(R.id.tv_demo_temperature_realtime);
        weatherNowTv = (TextView)findViewById(R.id.tv_demo_weather_realtime);

        tempTv = (TextView)findViewById(R.id.tv_demo_temperature);
        weatherTv = (TextView)findViewById(R.id.tv_demo_weather);
        windTv = (TextView)findViewById(R.id.tv_demo_wind);

        weatherIv = (ImageView)findViewById(R.id.iv_demo_weather);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //  通过SDK读取即时天气
    private void requestWeatherNowBySDK(String cityId){

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
                        System.out.println("Weather Now Error: "+new Gson());
                    }

                    @Override
                    public void onSuccess(Now dataObject) {
                        Log.i(TAG, "Weather Now Success: "+new Gson().toJson(dataObject));

                        String jsonData = new Gson().toJson(dataObject);
                        String weather = null;
                        String temp = null;
                        String weatherCode = null;
                        int resId;
                        if(dataObject.getStatus().equals("ok")){
                            NowBase now = dataObject.getNow();
                            weatherCode = now.getCond_code();
                            weather = now.getCond_txt();
                            temp = now.getTmp();

                            //  通过天气代码找到资源文件
                            String imgName = "ic_weather_"+weatherCode;
                            resId = getResIdByWeatherCode(imgName);
                            Log.d(TAG, "onSuccess :"+resId);

//                            String jsonNow = new Gson().toJson(dataObject.getNow());
//                            JSONObject jsonObject = null;
//                            try {
//                                jsonObject = new JSONObject(jsonNow);
//
//                                //  读取即时天气
//                                weather = jsonObject.getString("cond_txt");
//                                //  读取即时温度
//                                temp = jsonObject.getString("tmp");
//                            } catch (JSONException e){
//                                e.printStackTrace();
//                            }
                        } else {
                            Toast.makeText(MainActivity.this, "读取天气数据不存在", Toast.LENGTH_LONG).show();
                            return;
                        }
                        tempNowTv.setText(temp+"°");
                        weatherNowTv.setText(weather);
                        weatherIv.setImageResource(resId);
                    }
                });

    }

    //  通过SDK读取近日天气
    private void requestWeatherDailyBySDK(String cityId){


        HeWeather.getWeatherForecast(MainActivity.this, cityId, Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC, new HeWeather.OnResultWeatherForecastBeanListener() {

            public static final String TAG = "WeatherDaily";
                    @Override
                    public void onError(Throwable throwable) {
                        Log.i(TAG, "on Error: ", throwable);
                        System.out.println("Weather Daily Error: "+new Gson());
                    }

                    @Override
                    public void onSuccess(Forecast dataObject) {
                        Log.i(TAG, "Weather Now Success: "+new Gson().toJson(dataObject));
                        String weather, temp, wind = null;
                        if(dataObject.getStatus().equals("ok")){
                            //  mini版本，只读取当日天气，近一周的天气信息后续再补充
                            ForecastBase today = dataObject.getDaily_forecast().get(0);
                            weather = today.getCond_txt_d();

                            String max = today.getTmp_max();
                            String min = today.getTmp_min();
                            temp = min+"~"+max+"℃";

                            String windDir = today.getWind_dir();
                            String windSc = today.getWind_sc();
                            wind = windDir+""+windSc+"级";
                        } else {
                            Toast.makeText(MainActivity.this, "读取天气数据不存在", Toast.LENGTH_LONG).show();
                            return;
                        }
                        tempTv.setText(temp);
                        weatherTv.setText(weather);
                        windTv.setText(wind);

                    }
                });
    }


    //  设置天气图标
    private int getResIdByWeatherCode(String imgName){
        int resId = getResources().getIdentifier(imgName, "drawable", "com.example.myapplication");
        return resId;
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
                return "Sun.";
            case 2:
                return "Mon.";
            case 3:
                return "Tues.";
            case 4:
                return "Wed.";
            case 5:
                return "Thus.";
            case 6:
                return "Fri.";
            case 7:
                return "Sat.";
            default:
                Log.d(TAG, "convertNumToWeekday:week input error!");
                return null;
        }
        
    }



}

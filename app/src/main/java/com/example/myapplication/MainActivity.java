package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.example.myapplication.service.UpdateTimeService;
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

    MyTimeBroadCast myTimeBroadCast;

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

    //  baiduAPI的内容
    public LocationClient mLocationClient;



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
        //  获取并设置天气信息
        updateWeather(DAXING_ID);


        //  启动自动更新服务
//        Intent intent = new Intent(this, UpdateWeatherService.class);
//        startService(intent);
        //  启动更新时间服务，作为例子学习
        //  暂时设置成了每隔30min更新一次当前天气内容
        Intent timeService = new Intent(this, UpdateTimeService.class);
        startService(timeService);

        //  baiduAPI
//        mLocationClient = new LocationClient(getApplicationContext());
//        mLocationClient.registerLocationListener(new MyLocationListener());
//        requestLocation();



    }

    private void requestLocation(){
        mLocationClient.start();
    }

    //  baiduAPI
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("定位方式：");
            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                //  GPS定位
                currentPosition.append("GPS");
            } else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                //  网络定位
                currentPosition.append("网络");
            }
            testTv.setText(currentPosition);
        }
    }

//    public static int count = 0;
    public class MyTimeBroadCast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            requestWeatherNowBySDK(DAXING_ID);
//            weatherNowTv.setText(Integer.toString(count));
//            count++;
//            updateTime();
        }
    }

    private void updateTime() {
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String dateStr = dateFormat.format(date);
        String timeStr = timeFormat.format(date);

//        testTv.setText(timeStr);//显示出时间
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

    private void updateWeather(String cityId){
        //  获取即时天气
        requestWeatherNowBySDK(cityId);
        //  获取近期天气
        requestWeatherDailyBySDK(cityId);
        //  设置日期和星期
        dateTv.setText(getFormatDate());
        weekdayTv.setText(getWeekday());
    }

    @Override
    protected void onStart() {
        myTimeBroadCast = new MyTimeBroadCast();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("TIME_CHANGED_ACTION");
        registerReceiver(myTimeBroadCast, filter1);
//        updateTime();
        super.onStart();

    }

    //  停止自动更新服务
    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, UpdateWeatherService.class));
        super.onDestroy();
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

//                        String jsonData = new Gson().toJson(dataObject);
                        String weather = null;
                        String temp = null;
                        String weatherCode = null;
                        String loc = dataObject.getUpdate().getLoc();
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
                        tempNowTv.setText(temp+"°C");
                        weatherNowTv.setText(weather);
                        weatherIv.setImageResource(resId);
//                        testTv.setText(loc);
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
                        String date, weather, temp, wind = null;
                        if(dataObject.getStatus().equals("ok")){
                            //  mini版本，只读取当日天气，近一周的天气信息后续再补充
                            ForecastBase today = dataObject.getDaily_forecast().get(0);

                            //  查看一下更新时间
                            String loc = dataObject.getUpdate().getLoc();
                            testTv.setText(loc);

                            //  后续应该补充白天和晚上的区别
                            weather = today.getCond_txt_d();

                            date = today.getDate();
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
                        tempTv.setTextColor(Color.WHITE);
                        weatherTv.setText(weather);
                        weatherTv.setTextColor(Color.WHITE);
                        windTv.setText(wind);
                        windTv.setTextColor(Color.WHITE);
//                        dateTv.setText(date);

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

package com.example.myapplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAPI {

    static final String CID = "CN101011100";   //  大兴的城市代码
    static final String WEATHER_KEY = "69f2cb642a1646379bdb680390c377c7";  //  调用天气API的代码
    static String parameters = "location="+CID+"key="+WEATHER_KEY;

    static StringBuilder sb = new StringBuilder();
    static InputStream is = null;
    static BufferedReader br = null;
    static PrintWriter out = null;

    public static void getWeather(){
        try{
            //  接口地址
            String url = "https://api.heweather.net/s6/weather";
            URL uri = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();

            //  配置连接
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");

            //  发送参数
            connection.setDoOutput(true);
            out = new PrintWriter(connection.getOutputStream());
            out.print(parameters);
            out.flush();

            //  接收结果
            is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            //  缓冲逐行读取
            while((line = br.readLine())!=null){
                sb.append(line);
            }
            System.out.println(sb.toString());

        } catch(Exception exception){
            exception.printStackTrace();
        } finally {
            try{
                if(is!=null){
                    is.close();
                }
                if(br!=null){
                    br.close();
                }
                if(out!=null){
                    out.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){

        getWeather();
    }

}

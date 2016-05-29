package com.xyh.simpleweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xyh.simpleweather.R;
import com.xyh.simpleweather.service.AutoUpdateService;
import com.xyh.simpleweather.util.HttpCallbackListener;
import com.xyh.simpleweather.util.HttpUtil;
import com.xyh.simpleweather.util.Utility;

/**
 * Created by 向阳湖 on 2016/3/11.
 */
public class SimpleWeatherActivity extends Activity implements View.OnClickListener {
    private TextView tv_placeName, tv_ptime, tv_currentDate, tv_weatherDesp, tv_temp1, tv_temp2;
    private Button btn_switchPlace, btn_refreshWeather;
    private LinearLayout layout_weatherInfo;
    private static final String TAG = "SimpleWeatherActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_weather_layout);
        initView();
        String countyCode = getIntent().getStringExtra("county_code");
        //可能就是这里导致了  切换城市后还是显示以前信息
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号就去查天气,县级代号显示正确但显示的还是以前的天气信息
            tv_ptime.setText("同步中...");
            layout_weatherInfo.setVisibility(View.INVISIBLE);
            tv_placeName.setVisibility(View.INVISIBLE);
            //上面没问题,可能是这里导致的
            queryWeatherCode(countyCode);
            Log.d(TAG, "-----onCreate: 有县级代号countyCode = "+countyCode);
        } else {
            Log.d(TAG, "-----onCreate: 无县级代号countyCode = "+countyCode);
            //没有县级代码直接显示本地天气
            showWeather();
        }
        btn_switchPlace.setOnClickListener(this);
        btn_refreshWeather.setOnClickListener(this);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void initView() {
        tv_placeName = (TextView) findViewById(R.id.tv_placeName);
        tv_ptime = (TextView) findViewById(R.id.tv_ptime);
        tv_currentDate = (TextView) findViewById(R.id.tv_currentDate);
        tv_temp1 = (TextView) findViewById(R.id.tv_temp1);
        tv_temp2 = (TextView) findViewById(R.id.tv_temp2);
        tv_weatherDesp = (TextView) findViewById(R.id.tv_weather_desp);
        layout_weatherInfo = (LinearLayout) findViewById(R.id.weather_info_layout);
        btn_switchPlace = (Button) findViewById(R.id.btn_switchPlace);
        btn_refreshWeather = (Button) findViewById(R.id.btn_refreshWeather);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switchPlace:
                Intent i = new Intent(SimpleWeatherActivity.this, ChooseAreaActivity.class);
                i.putExtra("from_weather_activity", true);
                Log.d("SimpleWeatherActivity", "------选择城市跳转chooseAreaActivity");
                startActivity(i);
                finish();
                break;
            case R.id.btn_refreshWeather:
                tv_ptime.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code","");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromSever(address,"countyCode");
    }

    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromSever(address,"weatherCode");
    }

    //reponse有问题,可能是api有问题,导致数据解析有问题.之前网上也说了这api有问题.完毕!
    private void queryFromSever(final String address, final String typeCode) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if (typeCode.equals("countyCode")) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if (typeCode.equals("weatherCode")) {
                    //处理该服务器返回的数据
                    Log.d("SimpleWeather", "/-----1 typeCode.equals(weatherCode)相等");
                    Utility.handleWeatherResponse(SimpleWeatherActivity.this, response);
                    Log.d("SimpleWeather", "/-----2 typeCode.equals(weatherCode)相等");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_ptime.setText("同步失败");
                    }
                });
            }
        });
    }


//    editor.putBoolean("city_selected", true);
//    editor.putString("city_name", cityName);
//    editor.putString("weather_code", weatherCode);
//    editor.putString("ptime", ptime);
//    editor.putString("temp1", temp1);
//    editor.putString("temp2", temp2);
//    editor.putString("weather_desp", weatherDesp);
//    editor.putString("current_date", sdf.format(new Date()));

    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SimpleWeatherActivity.this);
        tv_placeName.setText(prefs.getString("city_name","error"));
        tv_ptime.setText("今天"+prefs.getString("ptime", "error")+"发布");
        tv_currentDate.setText(prefs.getString("current_date","error"));
        tv_weatherDesp.setText(prefs.getString("weather_desp", "error"));
        tv_temp1.setText(prefs.getString("temp1", "error"));
        tv_temp2.setText(prefs.getString("temp2","error"));
        tv_placeName.setVisibility(View.VISIBLE);
        layout_weatherInfo.setVisibility(View.VISIBLE);
    }
}

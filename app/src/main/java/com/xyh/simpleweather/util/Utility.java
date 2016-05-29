package com.xyh.simpleweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.xyh.simpleweather.database.SimpleWeatherDB;
import com.xyh.simpleweather.model.City;
import com.xyh.simpleweather.model.County;
import com.xyh.simpleweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 向阳湖 on 2016/3/10.
 */
public class Utility {
    private static final String TAG = "Utility";
    //解析从网上获取的数据,设置省数据
    public synchronized static boolean handleProvinceResponse(SimpleWeatherDB weatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            Log.d("Utility", "--------handleProvinceResponse(weatherDB,response)执行");
            String[] provinces = response.split(",");
            if (provinces != null && provinces.length > 0) {
                for (String p : provinces) {
                    Province province = new Province();
                    //分割符别错了!!!!
                    String[] array = p.split("\\|");
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    weatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    //解析从网上获取的数据,设置城市数据
    public static boolean handleCityResponse(SimpleWeatherDB weatherDB, String response, int provinceId) {
        Log.d("utility", "-------response不为空");
        if (!TextUtils.isEmpty(response)) {
            String[] cites = response.split(",");
            if (cites != null && cites.length > 0) {
                for (String c : cites) {
                    City city = new City();
                    String[] array = c.split("\\|");
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    weatherDB.saveCity(city);
                    Log.d("utility", "public synchronized static boolean handleCityResponse为真");
                }
                return true;
            }
        }
        return false;
    }

    //解析从网上获取的数据,设置县数据
    public static boolean handleCountyResponse(SimpleWeatherDB weatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] counties = response.split(",");
            if (counties != null && counties.length > 0) {
                for (String c : counties) {
                    County county = new County();
                    String[] array = c.split("\\|");
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    weatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    //这里有问题 切换城市后天气显示的还是之前城市名字,天气代号没问题.排除这步问题是解析过来的reponse有问题
    //天气代号正常,但是解析的数据换不过来,或者换过来后又瞬间跳回之前的城市信息
    //例如襄阳->武汉 假如错误:数据是以下情况:襄阳->襄阳  襄阳->武汉->襄阳
    public static void handleWeatherResponse(Context context, String response) {
        try {
            Log.d(TAG, "uu-----handleWeatherResponse: reponse = "+response);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
//            Log.d("Utility","/------2handleWeatherResponse()运行");
            String weatherCode = weatherInfo.getString("cityid");
            String cityName = weatherInfo.getString("city");
            String ptime = weatherInfo.getString("ptime");
            String weatherDesp = weatherInfo.getString("weather");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
//            Log.d("Utility","/------3handleWeatherResponse()运行");
            saveWeatherInfo(context, weatherCode, cityName, ptime, weatherDesp, temp1, temp2);
        } catch (JSONException e) {
            Log.d("Utility","uu------JSONException异常");
            e.printStackTrace();
        }
    }

    //可能 SharedPreferences缓存问题导致更新城市后天气信息没换成功.排除这不问题是上面问题
    public static void saveWeatherInfo(Context context, String weatherCode, String cityName, String ptime, String weatherDesp, String temp1, String temp2) {
        Log.d("Utility","/------saveWeatherInfo()运行");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("ptime", ptime);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
        Log.d(TAG, " uu-----saveWeatherInfo:cityName = "+cityName);

        Log.d("Utility", "----------天气存储提交了吗" + editor.commit());
    }
}

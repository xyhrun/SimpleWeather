package com.xyh.simpleweather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xyh.simpleweather.model.City;
import com.xyh.simpleweather.model.County;
import com.xyh.simpleweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 向阳湖 on 2016/3/10.
 */
public class SimpleWeatherDB {

    private static SimpleWeatherDB weatherDB;
    private static final String DB_NAME = "simple_weather";
    private static final int VERSION = 1;
    private SQLiteDatabase db;

    private SimpleWeatherDB(Context context) {
        SimpleWeatherHelper helper = new SimpleWeatherHelper(context, DB_NAME, null, VERSION);
        db = helper.getWritableDatabase();
    }

    public synchronized static SimpleWeatherDB getInstance(Context context) {
        if (weatherDB == null) {
            weatherDB = new SimpleWeatherDB(context);
        }
        return weatherDB;
    }

    //保存数据
    public void saveProvince(Province province) {
        if (province != null) {
            Log.d("SimpleWeatherDB", "--------saveProvince(Province province)执行");
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    //查询数据并加载
    public List<Province> loadProvinces() {
        Log.d("SimpleWeatherDB", "--------loadProvinces()执行");
        List<Province> provinceList = new ArrayList<Province>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                provinceList.add(province);
            } while (cursor.moveToNext());
        }
        return provinceList;
    }

    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
            Log.d("SimpleWeatherDB", "---------把city数据保存到数据库表City");
        }
    }

    public List<City> loadCities(int provinceId) {
        List<City> cityList = new ArrayList<City>();
        Cursor cursor = db.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                cityList.add(city);
//                Log.d("SimpleWeatherDB", "--------把city数据保存到数组CityList中");
            } while (cursor.moveToNext());
        }
        return cityList;
    }

    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());
            db.insert("County", null, values);
        }
    }

    public List<County> loadCounties(int cityId) {
        List<County> countyList = new ArrayList<County>();
        Cursor cursor = db.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                countyList.add(county);
            } while (cursor.moveToNext());
        }
        return countyList;
    }
}

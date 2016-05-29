package com.xyh.simpleweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xyh.simpleweather.R;
import com.xyh.simpleweather.database.SimpleWeatherDB;
import com.xyh.simpleweather.model.City;
import com.xyh.simpleweather.model.County;
import com.xyh.simpleweather.model.Province;
import com.xyh.simpleweather.util.HttpCallbackListener;
import com.xyh.simpleweather.util.HttpUtil;
import com.xyh.simpleweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    private static final int PROVINCE_LEVEL = 0;
    private static final int CITY_LEVEL = 1;
    private static final int COUNTY_LEVEL = 2;
    private int currentLevel;

    private ProgressDialog dialog;
    private SimpleWeatherDB weatherDB;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
//    private County selectedCounty;

    private TextView tv_title;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList;
    private boolean isFromSimpleWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isFromSimpleWeather = getIntent().getBooleanExtra("from_weather_activity", false);
        Log.d("ChooesAreaActivity", "--------isFromSimpleWeather是"+isFromSimpleWeather);
        Log.d("ChooesAreaActivity", "--------city_selected是"+ prefs.getBoolean("city_selected", false));
//        editor.putBoolean("city_selected", true);
        if (prefs.getBoolean("city_selected", false) && !isFromSimpleWeather) {
            Intent i = new Intent(this, SimpleWeatherActivity.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.choose_area_layout);
        initView();
        weatherDB = SimpleWeatherDB.getInstance(this);
        dialog = new ProgressDialog(this);
        dataList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == PROVINCE_LEVEL) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == CITY_LEVEL) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == COUNTY_LEVEL) {
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent i = new Intent(ChooseAreaActivity.this, SimpleWeatherActivity.class);
                    i.putExtra("county_code", countyCode);
                    startActivity(i);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.lv_view);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    private void queryProvinces() {
//        Log.d("ChooesAreaActivity", "--------queryProvinces()运行");
        provinceList = weatherDB.loadProvinces();
//        provinceList.clear();
        if (provinceList.size() > 0) {
//            Log.d("ChooesAreaActivity", "--------从weatherDB.loadProvinces()返回的provinceList.size()>0");
            dataList.clear();
            for (Province p : provinceList) {
                String provinceName = p.getProvinceName();
                dataList.add(provinceName);
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            tv_title.setText("中国");
            currentLevel = PROVINCE_LEVEL;
        } else {
            Log.d("ChooesAreaActivity", "--------queryProvinces()运行");
            queryFromSever("province", null);
        }
    }

    private void queryCities() {
        cityList = weatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
//            Log.d("ChooesAreaActivity", "--------从weatherDB.loadCities()返回的citiesList.size()>0");
//            for (City city : cityList) {
//                Log.d("ChooesAreaActivity", "--------城市名字"+city.getCityName());
//                Log.d("ChooesAreaActivity", "--------城市代号"+city.getCityCode());
//                Log.d("ChooesAreaActivity", "--------城市所属省代号"+city.getProvinceId());
//                Log.d("ChooesAreaActivity", "--------城市id"+city.getId());
//            }
            dataList.clear();
            for (City c : cityList) {
                String cityName = c.getCityName();
                dataList.add(cityName);
//                Log.d("ChooesAreaActivity", "--------添加城市名字");
            }
//            for (String city : dataList) {
////                Log.d("ChooesAreaActivity", "--------添加城市名字"+city);
//            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            tv_title.setText(selectedProvince.getProvinceName());
            currentLevel = CITY_LEVEL;
        } else {
            queryFromSever("city", selectedProvince.getProvinceCode());
        }


    }

    private void queryCounties() {
        countyList = weatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County c : countyList) {
                String countyName = c.getCountyName();
                dataList.add(countyName);
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            tv_title.setText(selectedCity.getCityName());
            currentLevel = COUNTY_LEVEL;
        } else {
            queryFromSever("county", selectedCity.getCityCode());
        }
    }

    private void queryFromSever(final String type, String typeCode) {
        Log.d("ChooesAreaActivity", "--------queryFromSever()运行");
        String address;
        if (!TextUtils.isEmpty(typeCode)) {
            address = "http://www.weather.com.cn/data/list3/city" + typeCode + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();
        Log.d("ChooesAreaActivity", "--------httpUtil.sendHttpRequest()运行");
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            public void onFinish(String response) {
                Log.d("ChooseAreaActivity", "--------onFinish()运行");
                boolean result = false;
                if ("province".equals(type) ) {
//                    Log.d("ChooseAreaActivity", "--------调用handleProvinceResponse(weatherDB,response)方法");
                    result = Utility.handleProvinceResponse(weatherDB, response);
                } else if (type .equals("city")) {
                    Log.d("ChooseAreaActivity", "--------type .equals(city)为真");
                    result = Utility.handleCityResponse(weatherDB, response, selectedProvince.getId());
                    Log.d("ChooseAreaActivity", "--------解析出City数据"+result);
                } else if (type.equals("county")) {
                    result = Utility.handleCountyResponse(weatherDB, response,selectedCity.getId());
                }

                if (result) {
                    //通过runOnUiThread子线程回到主线程处理逻辑问题
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals("province") ) {
                                queryProvinces();
                            } else if (type.equals("county")) {
                                queryCounties();
                            } else if (type.equals("city") ) {
                                Log.d("ChooseAreaActivity", "--------从网上搜索跳到queryCities()");
                                queryCities();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Log.d("ChooseAreaActivity", "--------onError()运行");
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (dialog != null) {
            dialog.setMessage("正在加载");
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    private void closeProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        //不能要父类构造方法,否则返回就退出
        if (currentLevel == COUNTY_LEVEL) {
            queryCities();
        } else if (currentLevel == CITY_LEVEL) {
            queryProvinces();
        } else {
            if (isFromSimpleWeather) {
                Intent intent = new Intent(ChooseAreaActivity.this, SimpleWeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}

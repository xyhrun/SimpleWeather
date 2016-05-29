package com.xyh.simpleweather.util;

/**
 * Created by 向阳湖 on 2016/3/10.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);

}

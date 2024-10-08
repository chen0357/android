package com.example.ychen18y_epsg.upv.es.myapplication9_30;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // POST 请求，发送测量数据
    @POST("insert_measurement.php")  // 替换为你的服务器端点路径
    Call<JSONObject> insertMeasurement(@Body JSONObject postData);

    // GET 请求，用于检查测量值
    @GET("insert_measurement.php")  // 替换为你的服务器端点路径
    Call<JSONObject> checkMeasurement(
            @Query("temperature") String temperature,
            @Query("co") String co
    );
}


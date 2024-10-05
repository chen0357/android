package com.example.ychen18y_epsg.upv.es.myapplication9_30;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    // GET request to check the measurement
    @GET("logica/comprobarenviomedicion.php")
    Call<JSONObject> checkMeasurement(@Query("temperatura") String temperatura, @Query("co2") String co2);

    // POST request to insert the measurement

    @POST("/insert_measurement.php") // 替换为你的PHP脚本路径
    Call<JSONObject> insertMeasurement(@Body JSONObject postData);
}


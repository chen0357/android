package com.example.ychen18y_epsg.upv.es.myapplication9_30;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @POST("insert_measurement.php")
    Call<ResponseBody> insertMeasurement(@Body RequestBody postData);

    @GET("insert_measurement.php")
    Call<ResponseBody> checkMeasurement(
            @Query("temperature") String temperature,
            @Query("co") String co
    );
}

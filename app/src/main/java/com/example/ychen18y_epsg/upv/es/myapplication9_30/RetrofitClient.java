package com.example.ychen18y_epsg.upv.es.myapplication9_30;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2/"; // 在模拟器上，10.0.2.2指向localhost

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // 设置base URL，确保它指向正确的服务器地址
                    .addConverterFactory(GsonConverterFactory.create()) // 使用GSON转换器
                    .build();
        }
        return retrofit;
    }
}




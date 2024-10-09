package com.example.ychen18y_epsg.upv.es.myapplication9_30;

        import retrofit2.Retrofit;
        import retrofit2.converter.gson.GsonConverterFactory;
        import com.google.gson.Gson;
        import com.google.gson.GsonBuilder;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()  // 启用宽松模式，允许处理格式不严格的JSON
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))  // 使用自定义Gson转换器
                    .build();
        }
        return retrofit;
    }
}

//.baseUrl("https://insert_measurement.php/")

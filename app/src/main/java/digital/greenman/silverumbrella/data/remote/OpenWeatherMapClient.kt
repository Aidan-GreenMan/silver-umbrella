package digital.greenman.silverumbrella.data.remote

import digital.greenman.silverumbrella.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenWeatherMapClient(private val apiKey: String?) {
    /**
     * Interceptor to add the API key as a query parameter to each request
     * if it isn't present.
     */
    private val apiKeyInjector = Interceptor { chain ->
        val originalRequest = chain.request()

        if (apiKey.isNullOrBlank() ||
            originalRequest.url.queryParameterNames.contains("appid")
        ) {
            chain.proceed(originalRequest)
        } else {
            val newUrl = originalRequest.url.newBuilder()
                .addQueryParameter("appid", apiKey)
                .build()

            val request = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(request)
        }
    }

    /**
     * Only log the HTTP requests in a debug build.
     */
    private val debugLogger = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(debugLogger)
        .addInterceptor(apiKeyInjector)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherApi: WeatherApi by lazy {
        retrofit.create(WeatherApi::class.java)
    }

    val geoApi: GeoApi by lazy {
        retrofit.create(GeoApi::class.java)
    }
}

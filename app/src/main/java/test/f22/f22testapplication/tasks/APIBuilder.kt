package test.f22.f22testapplication.tasks

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Retrofit
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import test.f22.f22testapplication.models.FoodItem

open class APIBuilder {
    private constructor()

    var apiService : APIServices? = null
    private val BASE_URL = "https://android-full-time-task.firebaseio.com/"

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val gSon = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gSon))
            .build()

        apiService = retrofit.create(APIServices::class.java)
    }

    companion object {
        var builder : APIBuilder? = null
        fun getInstance() : APIBuilder {
            if (builder == null) {
                builder = APIBuilder()
            }
            return builder!!
        }
    }
}

interface APIServices {

    @GET("/data.json")
    fun getFoodItems(): Observable<List<FoodItem>>
}
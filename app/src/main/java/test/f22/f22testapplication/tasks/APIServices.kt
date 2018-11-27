//package test.f22.f22testapplication.tasks
//
//import io.reactivex.Observable
//import retrofit2.http.GET
//import test.f22.f22testapplication.models.FoodItem
//
//
//interface APIServices {
//    val BASE_URL : String
//    get() = "https://android-full-time-task.firebaseio.com/"
//
//    @GET("/data.json")
//    fun getFoodItems(): Observable<List<FoodItem>>
//}
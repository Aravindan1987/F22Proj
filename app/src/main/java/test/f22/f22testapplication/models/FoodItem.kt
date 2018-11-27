package test.f22.f22testapplication.models

import com.google.gson.annotations.SerializedName
import com.orm.SugarRecord

data class FoodItem(@SerializedName("average_rating") var averageRating : Double? = null,
                    @SerializedName("image_url") var imageUrl : String? = null,
                    @SerializedName("item_name") var itemName : String? = null,
                    @SerializedName("item_price") var itemPrice : Double? = null,
                    var itemCount : Int = 0) :  SugarRecord()
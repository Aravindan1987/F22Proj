package test.f22.f22testapplication.activity

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.os.Bundle
import android.widget.ImageView
import com.google.gson.Gson
import test.f22.f22testapplication.R
import test.f22.f22testapplication.base.BaseActivity
import test.f22.f22testapplication.constants.IntentConstants
import test.f22.f22testapplication.databinding.ActivityFoodDetailBinding
import test.f22.f22testapplication.models.FoodItem
import test.f22.f22testapplication.tasks.DBExecutor
import test.f22.f22testapplication.tasks.ImageDownloadUtils
import test.f22.f22testapplication.utils.FormatUtils
import test.f22.f22testapplication.utils.ResourceUtils
import test.f22.f22testapplication.viewmodels.BaseViewModel

open class FoodItemDetailActivity : BaseActivity() {
    private lateinit var foodItem : FoodItem
    private lateinit var binding : ActivityFoodDetailBinding
    private lateinit var viewModel : FoodItemDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            foodItem = Gson().fromJson(savedInstanceState.getString(IntentConstants.FOOD_ITEM), FoodItem::class.java)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_detail)
        viewModel = ViewModelProviders.of(this, CustomViewModelFactory(foodItem)).get(FoodItemDetailViewModel::class.java)
        viewModel.registerWithActivity(this)
        binding.viewModel = viewModel
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun getScreenTitle(): String? = ResourceUtils.getString(R.string.food_detail_title)

    override fun parseBundleData(bundle: Bundle) {
        foodItem = Gson().fromJson(bundle.getString(IntentConstants.FOOD_ITEM), FoodItem::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(IntentConstants.FOOD_ITEM, Gson().toJson(foodItem))
    }
}

class FoodItemDetailViewModel(private val foodItem: FoodItem) : BaseViewModel<FoodItemDetailActivity>() {

    init {
        foodItem.itemCount =
                DBExecutor.getInstance().getFoodItemFromDB(foodItem)?.itemCount ?: 0
    }
    companion object {
        @BindingAdapter("app:image_url") @JvmStatic
        fun ImageView.loadImage(url:String) {
            ImageDownloadUtils.downloadImage(R.dimen.detail_image_size, url, this)
        }
    }

    val itemName = foodItem.itemName
    val fare = "${ResourceUtils.getString(R.string.rupee)}${foodItem.itemPrice.toString()}"
    val rating : String? = FormatUtils.singleDigitFormat.format(foodItem.averageRating)
    val selectedItemCount : ObservableField<String> = ObservableField(foodItem.itemCount.toString())
    val imageUrl : String? = foodItem.imageUrl

    fun onItemCountChanged(isIncrement : Boolean) {
        DBExecutor.getInstance().updateFoodItemCount(foodItem, isIncrement)
        if (isIncrement) {
            foodItem.itemCount = foodItem.itemCount + 1
        } else {
            if (foodItem.itemCount > 0) {
                foodItem.itemCount = foodItem.itemCount - 1
            } else return
        }
        selectedItemCount.set(foodItem.itemCount.toString())
    }
}

class CustomViewModelFactory(private val foodItem: FoodItem) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FoodItemDetailViewModel(foodItem) as T
    }
}
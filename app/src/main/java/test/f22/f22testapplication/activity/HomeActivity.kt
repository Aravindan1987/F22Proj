package test.f22.f22testapplication.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import test.f22.f22testapplication.R
import test.f22.f22testapplication.base.BaseActivity
import test.f22.f22testapplication.base.Event
import test.f22.f22testapplication.constants.IntentConstants
import test.f22.f22testapplication.databinding.ActivityHomeBinding
import test.f22.f22testapplication.databinding.FoodItemBinding
import test.f22.f22testapplication.dialogs.FilterAppliedEvent
import test.f22.f22testapplication.dialogs.FilterDialog
import test.f22.f22testapplication.models.FoodItem
import test.f22.f22testapplication.tasks.APIBuilder
import test.f22.f22testapplication.tasks.DBExecutor
import test.f22.f22testapplication.tasks.ImageDownloadUtils
import test.f22.f22testapplication.utils.FormatUtils
import test.f22.f22testapplication.utils.ResourceUtils
import test.f22.f22testapplication.viewmodels.BaseViewModel


class HomeActivity : BaseActivity() {

    private lateinit var binding : ActivityHomeBinding
    private lateinit var viewModel : HomeViewModel

    private var foodListAdapter : FoodListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.registerWithActivity(this)
        binding.viewModel = viewModel
        updateView()
    }

    override fun getScreenTitle() = ResourceUtils.getString(R.string.home_title)

    private fun updateView() {
        initializeListView(binding.foodListView)
    }

    fun updateFoodList(foodList : List<FoodItem>) {
        if (binding.foodListView.adapter == null) {
            foodListAdapter = FoodListAdapter(foodList)
            binding.foodListView.adapter = foodListAdapter
        } else {
            foodListAdapter?.foodList = foodList
            foodListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DBExecutor.getInstance().shutdown()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_filter -> {
                val filterDialog = FilterDialog()
                filterDialog.show(supportFragmentManager.beginTransaction(),"")
            }
            R.id.action_cart -> {startCartActivity()}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startCartActivity () {
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }

    override fun onEventReceived(event: Event) {
        super.onEventReceived(event)
        viewModel.onEventReceived(event)
    }
}

class HomeViewModel : BaseViewModel<HomeActivity>() {
    private var foodList : List<FoodItem>? = null
    init {
        getFoodProducts()
    }

    fun onEventReceived(event: Event) {
        when(event) {
            is FilterAppliedEvent -> { onFilterApplied(event.item) }
            is FoodItemSelectedEvent -> { onFoodItemSelected(event.foodItem) }
        }
    }

    private fun onFoodItemSelected(foodItem: FoodItem) {
        val intent = Intent(activityReference?.get(),FoodItemDetailActivity::class.java)
        intent.putExtra(IntentConstants.FOOD_ITEM, Gson().toJson(foodItem))
        activityReference?.get()?.startActivity(intent)
    }

    private fun onFilterApplied(item : String) {
        foodList = when (item) {
            ResourceUtils.getString(R.string.filter_list_rating) -> foodList?.sortedByDescending { it -> it.averageRating  }
            ResourceUtils.getString(R.string.filter_list_fare) -> foodList?.sortedBy { it -> it.itemPrice  }
            else -> foodList
        }
        activityReference?.get()?.updateFoodList(foodList!!)
    }

    private fun getFoodProducts() {
       val foodListObservable = APIBuilder.getInstance().apiService?.getFoodItems()

        foodListObservable?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(this::handleFoodProductResult)
    }

    private fun handleFoodProductResult(foodList : List<FoodItem>) {
        this.foodList = foodList
        activityReference?.get()?.updateFoodList(foodList)
    }
}

class FoodListAdapter(var foodList : List<FoodItem>) : RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder> () {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FoodListViewHolder {
        val itemBinding = FoodItemBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return FoodListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(p0: FoodListViewHolder, p1: Int) {
        p0.itemView.setOnClickListener( { EventBus.getDefault().post(FoodItemSelectedEvent(foodList[p1])) } )
        p0.bind(foodList[p1])
    }

    override fun getItemCount(): Int  = foodList.size

    class FoodListViewHolder(private val binding : FoodItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(foodItem : FoodItem) {
            binding.foodItemViewModel = FoodItemViewModel(foodItem)
            binding.executePendingBindings()
        }
    }

    class FoodItemViewModel(private val foodItem : FoodItem) {

        init {
            foodItem.itemCount =
                    DBExecutor.getInstance().getFoodItemFromDB(foodItem)?.itemCount ?: 0
        }

        companion object {
            @BindingAdapter("app:image_url") @JvmStatic
            fun ImageView.loadImage(url:String) {
                ImageDownloadUtils.downloadImage(R.dimen.list_image_size, url, this)
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
}

class FoodItemSelectedEvent(val foodItem: FoodItem) : Event()
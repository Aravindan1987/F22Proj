package test.f22.f22testapplication.activity

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.orm.SugarRecord
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import test.f22.f22testapplication.R
import test.f22.f22testapplication.base.BaseActivity
import test.f22.f22testapplication.base.ShowToastEvent
import test.f22.f22testapplication.constants.FareConstants
import test.f22.f22testapplication.databinding.ActivityCartBinding
import test.f22.f22testapplication.databinding.CartFareItemBinding
import test.f22.f22testapplication.databinding.FoodItemCartBinding
import test.f22.f22testapplication.models.FoodItem
import test.f22.f22testapplication.utils.FormatUtils
import test.f22.f22testapplication.utils.ResourceUtils
import test.f22.f22testapplication.viewmodels.BaseViewModel


class CartActivity : BaseActivity(){

    private lateinit var binding : ActivityCartBinding
    private lateinit var viewModel : CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        viewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        updateFareListObserver()
        viewModel.registerWithActivity(this)
        binding.cartViewModel = viewModel
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.updateView()
    }

    private fun updateFareListObserver() {
        viewModel.getFareObserver()?.observe(this, Observer { list ->
            list?.let { updateFareList(it) }
        })
    }

    fun updateListView(list : List<FoodItem>) {
        initializeListView(binding.itemsList)
        initializeListView(binding.fareList)
        binding.itemsList.adapter = CartListAdapter(list)
    }

    private fun updateFareList(list : List<Pair<String, String>>) {
        binding.fareList.adapter = CartFareAdapter(list)
    }

    override fun getScreenTitle(): String?  = ResourceUtils.getString(R.string.cart_title)
}

class CartViewModel : BaseViewModel<CartActivity> () {

    var isCartEmpty : ObservableField<Boolean> = ObservableField(false)
    val promoApplied : ObservableField<String> = ObservableField()
    private var fareListObserver : MutableLiveData<List<Pair<String, String>>>? = null
    private var appliedPromo : PublishSubject<String>? = PublishSubject.create()
    private lateinit var foodList : List<FoodItem>
    private var totalFare : Double = 0.0

    fun updateView() {
        val thread = Thread(runnable)
        thread.start()
        updatePromoObserver()
    }

    private val runnable = Runnable {
        foodList = SugarRecord.listAll(FoodItem::class.java)
        totalFare = foodList.sumByDouble { foodItem -> foodItem.itemCount.times(foodItem.itemPrice ?: 0.0) }

        activityReference?.get()?.runOnUiThread {
            if (foodList.isEmpty()) {
                displayEmptyListView()
            } else {
                onSelectedItemsAvailable()
            }
        }
    }

    private fun onSelectedItemsAvailable() {
        isCartEmpty.set(false)
        activityReference?.get()?.updateListView(foodList)
        updateFareList(null)
    }

    private fun updatePromoObserver() {
        appliedPromo?.subscribe(this::onPromoApplied)
    }

    private fun onPromoApplied(promo :String) {
        updateFareList(promo)
    }

    fun getFareObserver() : MutableLiveData<List<Pair<String, String>>>? {
        synchronized(this) {
            if (fareListObserver == null) {
                fareListObserver = MutableLiveData()
            }
        }
        return fareListObserver
    }

    private fun updateFareList(promo : String?) {
        var payableFare = totalFare
        val fareList = ArrayList<Pair<String, String>>()
        fareList.add(Pair(ResourceUtils.getString(R.string.total_selected),
            "${ResourceUtils.getString(R.string.rupee)}$totalFare"))

        promo?.let {
            when (it) {
                FareConstants.PROMO_F22 -> {
                    val discountedAmount = getDiscountedAmount(it, totalFare).toInt()
                    fareList.add(Pair("$it ${ResourceUtils.getString(R.string.applied)}",
                        "- ${ResourceUtils.getString(R.string.rupee)}${FormatUtils.singleDigitFormat.format(discountedAmount)}"))
                    payableFare = payableFare.minus(discountedAmount)
                }
            }
        }

        if (!(promo != null && promo == FareConstants.PROMO_FREE)){
            fareList.add(Pair(ResourceUtils.getString(R.string.delivery_charges),
                "${ResourceUtils.getString(R.string.rupee)}${FareConstants.DELIVERY_CHARGE}"))
            payableFare = payableFare.plus(FareConstants.DELIVERY_CHARGE)
        }
        fareList.add(Pair(ResourceUtils.getString(R.string.total_payable),
            "${ResourceUtils.getString(R.string.rupee)}$payableFare"))

        fareListObserver?.postValue(fareList)
    }

    private fun getDiscountedAmount(promo :String, fare : Double) : Double =
        when (promo) {
            FareConstants.PROMO_F22 -> (fare.div(100).times(20))
            else -> 0.0
        }

    fun applyPromo() {
        promoApplied.get()?.let {
            when (it) {
                FareConstants.PROMO_F22 -> {
                    if (totalFare > FareConstants.PROMO_F22_VALUE)
                        appliedPromo?.onNext(it)
                    else
                        EventBus.getDefault().post(ShowToastEvent("${ResourceUtils.getString(R.string.promo_error_no_min_total)}${FareConstants.PROMO_F22_VALUE}"))
                }
                FareConstants.PROMO_FREE -> {
                    if (totalFare > FareConstants.PROMO_FREE_VALUE)
                        appliedPromo?.onNext(it)
                    else
                        EventBus.getDefault().post(ShowToastEvent("${ResourceUtils.getString(R.string.promo_error_no_min_total)}${FareConstants.PROMO_FREE_VALUE}"))
                }
                else -> {}
            }
        }
    }

    private fun displayEmptyListView() {
        isCartEmpty.set(true)
    }
}

class CartListAdapter(private var foodList : List<FoodItem>) : RecyclerView.Adapter<CartListAdapter.CartListViewHolder> () {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CartListViewHolder {
        val itemBinding = FoodItemCartBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return CartListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(p0: CartListViewHolder, p1: Int) = p0.bind(foodList[p1])

    override fun getItemCount(): Int  = foodList.size

    class CartListViewHolder(private val binding : FoodItemCartBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(foodItem : FoodItem) {
            binding.viewModel = CartItemViewModel(foodItem)
            binding.executePendingBindings()
        }
    }

    class CartItemViewModel(foodItem : FoodItem) {
        val name = foodItem.itemName
        val fare = "${ResourceUtils.getString(R.string.rupee)}${foodItem.itemPrice.toString()}"
        val totalFareText = "${ResourceUtils.getString(R.string.cart_total_item_fare)} (${foodItem.itemCount})"
        val totalFareValue = "${ResourceUtils.getString(R.string.rupee)}${foodItem.itemPrice?.times(foodItem.itemCount).toString()}"
    }
}

class CartFareAdapter(private var fareList : List<Pair<String, String>>) : RecyclerView.Adapter<CartFareAdapter.CartFareListViewHolder> () {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CartFareListViewHolder {
        val itemBinding = CartFareItemBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return CartFareListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(p0: CartFareListViewHolder, p1: Int) = p0.bind(fareList[p1])

    override fun getItemCount(): Int  = fareList.size

    class CartFareListViewHolder(private val binding : CartFareItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item : Pair<String, String>) {
            binding.viewModel = CartFareItemViewModel(item)
            binding.executePendingBindings()
        }
    }

    class CartFareItemViewModel(item : Pair<String, String>) {
        val name = item.first
        val fare = item.second
    }
}
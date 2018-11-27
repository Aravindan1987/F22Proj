package test.f22.f22testapplication.tasks

import com.orm.SugarRecord
import test.f22.f22testapplication.models.FoodItem
import java.util.concurrent.Executors

class DBExecutor {

    private var executorService = Executors.newCachedThreadPool()

    fun updateFoodItemCount(foodItem : FoodItem, isCountIncrement : Boolean){
        val runnable = FoodItemRunnable(foodItem, isCountIncrement)
        executorService.submit(runnable)
    }

    fun getFoodItemFromDB(foodItem: FoodItem) : FoodItem? =
        SugarRecord.find(FoodItem::class.java, "item_name = ?", foodItem.itemName).firstOrNull()

    fun shutdown() {
        executorService.shutdown()
        dbExecutor = null
    }

    private constructor()

    companion object {
        private var dbExecutor : DBExecutor? = null
        fun  getInstance() : DBExecutor {
            synchronized(this) {
                if (dbExecutor == null) {
                    dbExecutor = DBExecutor()
                }
            }
            return dbExecutor!!
        }
    }
}

class FoodItemRunnable(private val foodItem : FoodItem, private val isCountIncrement : Boolean) : Runnable {

    override fun run() {
        val foodList = SugarRecord.find(FoodItem::class.java, "item_name = ?", foodItem.itemName)
        val food = foodList.firstOrNull()
        food?.let { rowPresent(it) } ?: rowNotPresent()

    }

    private fun rowPresent(foodItem : FoodItem) {
        if (isCountIncrement)
            foodItem.itemCount = foodItem.itemCount + 1
        else
            foodItem.itemCount = foodItem.itemCount - 1

        if (foodItem.itemCount == 0)
            SugarRecord.delete(foodItem)
        else
            SugarRecord.save(foodItem)
    }

    private fun rowNotPresent() {
        if (isCountIncrement) {
//            foodItem.itemCount = foodItem.itemCount + 1
            SugarRecord.save(foodItem)
        }
    }
}
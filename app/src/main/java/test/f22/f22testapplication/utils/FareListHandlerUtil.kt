//package test.f22.f22testapplication.utils
//
//import io.reactivex.Observable
//import test.f22.f22testapplication.R
//import test.f22.f22testapplication.constants.FareConstants
//import test.f22.f22testapplication.models.FoodItem
//
//class FareListHandlerUtil {
//
//    companion object {
//        val fareList = ArrayList<Pair<String, String>>()
//
//        fun createFareList(list: List<FoodItem>) : List<Pair<String, String>> {
//            val totalFare = list.sumByDouble { foodItem -> foodItem.itemCount.times(foodItem.itemPrice ?: 0.0) }
//            fareList.add(Pair(ResourceUtils.getString(R.string.total_selected),
//                "${ResourceUtils.getString(R.string.rupee)}$totalFare"))
//            fareList.add(Pair(ResourceUtils.getString(R.string.delivery_charges),
//                "${ResourceUtils.getString(R.string.rupee)}${FareConstants.DELIVERY_CHARGE}"))
//            fareList.add(Pair(ResourceUtils.getString(R.string.total_payable),
//                "${ResourceUtils.getString(R.string.rupee)}${totalFare.plus(FareConstants.DELIVERY_CHARGE)}"))
//
//            val observer = Observable.create<ArrayList<Pair<String, String>>>(Observable<>)
//
//            return fareList
//        }
//    }
//}
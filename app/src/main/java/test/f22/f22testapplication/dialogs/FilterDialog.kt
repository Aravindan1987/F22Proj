package test.f22.f22testapplication.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import test.f22.f22testapplication.R
import test.f22.f22testapplication.base.BaseDialogFragment
import test.f22.f22testapplication.base.Event
import test.f22.f22testapplication.base.F22Application
import test.f22.f22testapplication.databinding.DialogFilterBinding
import test.f22.f22testapplication.databinding.FilterItemBinding
import test.f22.f22testapplication.utils.ResourceUtils

class FilterDialog : BaseDialogFragment() {

    private lateinit var binding : DialogFilterBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_filter, null, false)
        updateView()
        return AlertDialog.Builder(activity).setView(binding.root).create()
    }

    private fun updateView() {
        binding.filterList.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        binding.filterList.layoutManager = layoutManager
        updateList()
    }

    private fun updateList() {
        binding.filterList.adapter = FilterItemsAdapter()
    }

    override fun onEventReceived(event: Event) {
        super.onEventReceived(event)
        when (event) {
            is FilterAppliedEvent -> dismiss()
        }
    }
}

class FilterItemsAdapter : RecyclerView.Adapter<FilterItemsAdapter.FilterViewHolder>() {

    private val filterItemList = listOf(ResourceUtils.getString(R.string.filter_list_fare),
        ResourceUtils.getString(R.string.filter_list_rating))

    override fun onBindViewHolder(p0: FilterViewHolder, p1: Int) {
        p0.itemView.setOnClickListener( {
            EventBus.getDefault().post(FilterAppliedEvent(filterItemList[p1]))
        })
        p0.bind(filterItemList[p1])
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FilterViewHolder {
        val binding = FilterItemBinding.inflate(LayoutInflater.from(F22Application.context), p0, false)
        return FilterViewHolder(binding)
    }

    override fun getItemCount(): Int = filterItemList.size

    class FilterViewHolder(private val binding :FilterItemBinding):RecyclerView.ViewHolder(binding.root) {

        fun bind(item : String) {
            binding.string = item
            binding.executePendingBindings()
        }
    }
}

class FilterAppliedEvent(val item : String) : Event()
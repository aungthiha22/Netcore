package com.systematic.netcore.adapter

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.systematic.netcore.R
import com.systematic.netcore.objects.ScoutObject
import kotlinx.android.synthetic.main.scout_card.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScoutListAdapter(val items: ArrayList<ScoutObject>, val listener: (Int) -> Unit): RecyclerView.Adapter<ScoutListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
        R.layout.scout_card, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], position, listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: ScoutObject, pos: Int, listener: (Int) -> Unit) = with(itemView) {

            var defaultDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

            val tvCompanyName = itemView.tvCompanyName
            val tvScoutType = itemView.tvScoutType
            val tvAddress = itemView.tvAddress
            val tvTownship = itemView.tvTownship
            val tvTimeAgo = itemView.tvTimeAgo
            val imageViewUpload = itemView.imageViewUpload
            val cardView = itemView.cardView

            tvCompanyName?.text = item.companyName
            //tvScoutType?.text = defaultDateFormat.format(item.scoutOn.time)
            tvScoutType?.text = item.scoutType
            tvAddress?.text = item.contactPerson
            tvTownship?.text = item.townshipName

            Log.i("modifiedOn", item.modifiedOn)
            var day = item.modifiedOn.split("\\s".toRegex())[0]
            var time = (item.modifiedOn.split("\\s".toRegex())[1]).split("\\.".toRegex())[0]
            var date = "$day $time"
            var timeAgo = item.timeAgo
            tvTimeAgo?.text = "$date\n$timeAgo"
            //tvTimeAgo?.visibility = View.GONE
            if (!item.uploadOn.equals("")) {
                imageViewUpload.setBackgroundResource(R.drawable.upload_finish)
            }

            val cvItem = findViewById(R.id.forOnClick) as LinearLayout
            cvItem.setOnClickListener {
                listener(pos)
            }

            cardView.setOnClickListener {
                listener(pos)
            }
        }

        protected fun getPixelsFromDPs(dps: Int, context: Context): Int {
            val r = context.getResources()
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), r.getDisplayMetrics()).toInt()
        }
    }
}
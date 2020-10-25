package com.fantasmaplasma.bigonotation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class ComplexitySpinnerAdapter(context: Context, private val items: Array<String>) :
    ArrayAdapter<String>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView ?: initView(position, parent)
    }

    private fun initView(position: Int, parent: ViewGroup) =
        LayoutInflater.from(context).inflate(
            R.layout.spinner_item, parent, false
        ).also {
            it.findViewById<TextView>(R.id.tv_complexity_spinner)
                .text = items[position]
        }
}

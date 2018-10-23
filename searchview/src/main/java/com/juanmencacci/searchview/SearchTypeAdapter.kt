/*
 * Created by Juan Cruz Mencacci on 17/10/18 21:01
 * Copyright (c) 2018 . All rights reserved.
 */

package com.juanmencacci.searchview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * @author Juan Cruz Mencacci
 */


class SearchTypeAdapter(context: Context) : BaseAdapter() {

    private val mSearchTypeList: ArrayList<SearchType> = arrayListOf()
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mTextColor: Int = ContextCompat.getColor(context, R.color.textMediumEmphasis)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if(convertView == null) {
            view = this.mInflater.inflate(R.layout.item_search, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = mSearchTypeList[position]
        viewHolder.textItem.text = item.type
        viewHolder.textItem.setTextColor(mTextColor)
        viewHolder.imageIcon.setImageResource(item.iconDrawable)

        return view
    }

    override fun getItem(position: Int): Any {
        return mSearchTypeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
       return mSearchTypeList.size
    }

    class ViewHolder (convertView: View) {
        var textItem: TextView = convertView.findViewById<View>(R.id.text_item) as TextView
        var imageIcon: ImageView =  convertView.findViewById<View>(R.id.image_icon) as ImageView
    }

    fun addType(type: SearchType){
        mSearchTypeList.add(type)
    }

    fun addTypes(typeList: ArrayList<SearchType>){
        mSearchTypeList.addAll(typeList)
    }

    fun setTypeList(typeList: ArrayList<SearchType>){
        mSearchTypeList.clear()
        addTypes(typeList)
    }

    fun setTextColor(newColor: Int){
        mTextColor = newColor
    }

}
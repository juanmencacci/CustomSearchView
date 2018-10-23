/*
 * Created by Juan Cruz Mencacci on 17/10/18 21:02
 * Copyright (c) 2018 . All rights reserved.
 */

package com.juanmencacci.searchview

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.widget.*
import java.text.Normalizer


/**
 * @author Juan Cruz Mencacci
 */


class SearchSuggestAdapter(context: Context) : BaseAdapter(), Filterable {

    private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    private var mSearchSuggestList: ArrayList<String> = arrayListOf()
    private var mSearchSuggestFiltered: ArrayList<String> = arrayListOf()

    @DrawableRes
    private var mSuggestDrawable: Drawable? = ContextCompat.getDrawable(context,R.drawable.ic_round_info)
    private var mTextColor: Int = ContextCompat.getColor(context, R.color.textMediumEmphasis)

    private var mInflater: LayoutInflater = LayoutInflater.from(context)

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

        val suggest = mSearchSuggestFiltered[position]
        viewHolder.textItem.text = suggest
        viewHolder.textItem.setTextColor(mTextColor)

        mSuggestDrawable?.let { viewHolder.imageIcon.setImageDrawable(it) }

        return view
    }

    override fun getItem(position: Int): Any {
        return mSearchSuggestFiltered[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mSearchSuggestFiltered.size
    }

    class ViewHolder (convertView: View) {
        var textItem: TextView = convertView.findViewById<View>(R.id.text_item) as TextView
        var imageIcon: ImageView =  convertView.findViewById<View>(R.id.image_icon) as ImageView
    }

    fun addSuggest(suggest: String){
        mSearchSuggestList.add(suggest)
    }

    fun addSuggests(suggestList: ArrayList<String>){
        mSearchSuggestList.addAll(suggestList)
    }

    fun setSuggestList(suggestList: ArrayList<String>){
        mSearchSuggestList.clear()
        addSuggests(suggestList)
    }

    fun setCustomDrawable(drawable: Drawable){
        mSuggestDrawable = drawable
    }

    fun setTextColor(newColor: Int){
        mTextColor = newColor
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()

                if(constraint.isNotEmpty() && constraint.isNotBlank()){
                    val searchData: ArrayList<String> = arrayListOf()

                    for (string in mSearchSuggestList) {
                        if (string.unaccent().replace(" ","").startsWith(constraint.unaccent().replace(" ",""), true)) {
                            searchData.add(string)
                        }
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = searchData
                    filterResults.count = searchData.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.values != null) {
                    mSearchSuggestFiltered = (results.values as ArrayList<String>)
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun String.unaccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")
    }

    fun CharSequence.unaccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")
    }

}
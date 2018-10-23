package com.juanmencacci.searchviewexample

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import com.juanmencacci.searchview.SearchType
import com.juanmencacci.searchview.SearchView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.text.TextUtils
import android.speech.RecognizerIntent
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        searchView = findViewById(R.id.search_view)

        val arrayList = ArrayList<SearchType>()
        arrayList.add(SearchType("Search by location", R.drawable.ic_round_location_on))
        arrayList.add(SearchType("Search by name", R.drawable.ic_round_import_contacts))

        searchView.setSearchTypes(arrayList)

        val suggestionsList = ArrayList<String>()
        suggestionsList.add("Argentina")
        suggestionsList.add("Estados Unidos")
        suggestionsList.add("River Plate")
        suggestionsList.add("Boca")
        suggestionsList.add("España")
        suggestionsList.add("Real Madrid")
        suggestionsList.add("Barcelona")
        searchView.setSearchSuggestions(suggestionsList)

        searchView.enableVoiceSearch(true)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String, typePosition: Int): Boolean {
                textview.text = query
                return true
            }

            override fun onQueryTextChange(newText: String, typePosition: Int): Boolean {
                //textview.type = newText
                return true
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val searchViewMenu = menu.findItem(R.id.action_search)
        searchView.setMenuItem(searchViewMenu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (search_view.isSearchOpen()){
            search_view.closeSearchView()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SearchView.REQUEST_VOICE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.size > 0) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQueryText(searchWrd)
                }
            }

            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}

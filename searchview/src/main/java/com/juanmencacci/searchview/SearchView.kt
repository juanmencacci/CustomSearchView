/*
 * Created by Juan Cruz Mencacci on 17/10/18 21:01
 * Copyright (c) 2018 . All rights reserved.
 */

package com.juanmencacci.searchview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.view.inputmethod.InputMethodManager
import android.speech.RecognizerIntent
import android.content.Intent
import android.view.MenuItem
import android.app.Activity
import android.os.Build
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.TextView
import android.content.ActivityNotFoundException
import android.content.res.TypedArray
import android.text.InputType
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * @author Juan Cruz Mencacci
 */

class SearchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener, Filter.FilterListener{

    companion object {
        const val REQUEST_VOICE = 1111
    }

    private var mMenuItem: MenuItem? = null

    private var mIsSearchViewVisible: Boolean = false
    private var mAllowVoiceSearch: Boolean = false
    private var mShowOnlyIconType: Boolean = false
    private var mSubmitOnClick = false

    /**
     * Current data
     */

    private var mCurrentSearchType: SearchType? = null
    private var mCurrentSearchTypePosition: Int = 0

    /**
     * Interfaces variables.
     */

    private var mOnQueryChangeListener: OnQueryTextListener? = null
    private var mSearchViewListener: SearchViewListener? = null

    /**
     * List Adapter
     */

    private lateinit var mSearchTypeTypeAdapter: SearchTypeAdapter
    private lateinit var mSearchSuggestAdapter: SearchSuggestAdapter


    /**
     * View components.
     */

    private lateinit var mLayoutSearchView: ConstraintLayout
    private lateinit var mLayoutClearView: View
    private lateinit var mLayoutSearchBar: View

    private lateinit var mSearchTypeText: TextView
    private lateinit var mInputText: EditText

    private lateinit var mBackButton: ImageButton
    private lateinit var mVoiceButton: ImageButton
    private lateinit var mClearButton: ImageButton

    private lateinit var mSearchTypeIcon: ImageView

    private lateinit var mSearchTypesListView: ListView
    private lateinit var mSearchSuggestListView: ListView


    init {
        initView()
        initStyleAttributes(attrs)
    }

    /**
     * Private methods.
     */

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_search, this, true)

        //Initialize view components.
        mLayoutSearchView = findViewById(R.id.layout_search_view)
        mLayoutClearView = mLayoutSearchView.findViewById(R.id.layout_clear_view) as View
        mLayoutSearchBar = mLayoutSearchView.findViewById(R.id.layout_search_bar) as View

        mSearchTypeText = mLayoutSearchView.findViewById(R.id.search_type_text) as TextView
        mInputText = mLayoutSearchView.findViewById(R.id.search_input_text) as EditText

        mBackButton = mLayoutSearchView.findViewById(R.id.action_back_btn) as ImageButton
        mVoiceButton = mLayoutSearchView.findViewById(R.id.action_voice_btn) as ImageButton
        mClearButton = mLayoutSearchView.findViewById(R.id.action_clear_btn) as ImageButton

        mSearchTypeIcon = mLayoutSearchView.findViewById(R.id.search_type_image) as ImageView

        mSearchTypesListView = mLayoutSearchView.findViewById(R.id.list_search_types) as ListView
        mSearchSuggestListView = mLayoutSearchView.findViewById(R.id.list_search_suggest) as ListView

        initOnClickListener()
        initAdapters()
        initListViews()
        initSearchText()

        adjustActivitySoftInputMode()

        if (!isInEditMode) {
            this.visibility = View.GONE
        }
    }

    private fun initStyleAttributes(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomSearchView)

        getDrawableAttribute(attributes, R.styleable.CustomSearchView_searchBackground)?.let { setBackground(it) }
        getDrawableAttribute(attributes, R.styleable.CustomSearchView_searchVoiceIcon)?.let { setVoiceIcon(it) }
        getDrawableAttribute(attributes, R.styleable.CustomSearchView_searchClearIcon)?.let { setClearIcon(it) }
        getDrawableAttribute(attributes, R.styleable.CustomSearchView_searchBackIcon)?.let { setBackIcon(it) }
        getDrawableAttribute(attributes, R.styleable.CustomSearchView_searchSuggestionIcon)?.let { setSuggestionDrawable(it) }
        getDrawableAttribute(attributes, R.styleable.CustomSearchView_listViewBackgroundDrawable)?.let { setListViewBackgroundDrawable(it) }

        getStringAttribute(attributes, R.styleable.CustomSearchView_android_hint)?.let { setHint(it) }

        if (attributes.hasValue(R.styleable.CustomSearchView_listViewBackgroundColor)) {
            setListViewBackgroundColor(attributes.getColor(R.styleable.CustomSearchView_listViewBackgroundColor, 0))
        }

        if (attributes.hasValue(R.styleable.CustomSearchView_listViewTextColor)) {
            setListViewTextColor(attributes.getColor(R.styleable.CustomSearchView_listViewTextColor, 0))
        }

        if (attributes.hasValue(R.styleable.CustomSearchView_android_textColor)) {
            setTextColor(attributes.getColor(R.styleable.CustomSearchView_android_textColor, 0))
        }

        if (attributes.hasValue(R.styleable.CustomSearchView_android_textColorHint)) {
            setHintTextColor(attributes.getColor(R.styleable.CustomSearchView_android_textColorHint, 0))
        }

        if (attributes.hasValue(R.styleable.CustomSearchView_android_inputType)) {
            setInputType(attributes.getInt(R.styleable.CustomSearchView_android_inputType, InputType.TYPE_TEXT_FLAG_CAP_WORDS))
        }

        if (attributes.hasValue(R.styleable.CustomSearchView_enableVoiceSearch)) {
            enableVoiceSearch(attributes.getBoolean(R.styleable.CustomSearchView_enableVoiceSearch, false))
        }

        if (attributes.hasValue(R.styleable.CustomSearchView_showOnlyIconType)) {
            showOnlyIconType(attributes.getBoolean(R.styleable.CustomSearchView_showOnlyIconType, false))
        }

        attributes.recycle()
    }

    private fun getDrawableAttribute(attrs: TypedArray, index: Int ) : Drawable? {
        if(attrs.hasValue(index)){
            return attrs.getDrawable(index)
        }
        return null
    }
    private fun getStringAttribute(attrs: TypedArray, index: Int ) : String? {
        if(attrs.hasValue(index)){
            return attrs.getString(index)
        }
        return null
    }

    private fun adjustActivitySoftInputMode(){
        if(context is Activity){
            (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    private fun initOnClickListener() {
        mInputText.setOnClickListener(this)
        mBackButton.setOnClickListener(this)
        mVoiceButton.setOnClickListener(this)
        mClearButton.setOnClickListener(this)
        mLayoutClearView.setOnClickListener(this)
    }

    private fun initAdapters() {
        mSearchTypeTypeAdapter = SearchTypeAdapter(context)
        mSearchSuggestAdapter = SearchSuggestAdapter(context)
        startFilter(mInputText.text)
    }

    private fun initListViews() {
        mSearchTypesListView.adapter = mSearchTypeTypeAdapter
        mSearchTypesListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            onSearchTypeItemClick(mSearchTypeTypeAdapter.getItem(position) as SearchType, position)
        }

        mSearchSuggestListView.adapter = mSearchSuggestAdapter
        mSearchSuggestListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            onSearchSuggestItemClick(mSearchSuggestAdapter.getItem(position) as String)
        }
        hideListViews()
    }

    private fun initSearchText() {

        mInputText.setOnEditorActionListener {
            v, actionId, event ->
            onSubmitText(mInputText.text)
            true
        }

        mInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                this@SearchView.onTextChanged(s)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        mInputText.setOnFocusChangeListener { v, hasFocus ->
            handleOnInputTextFocus(hasFocus)
        }

    }

    /**
     * Implement on click listener
     */

    override fun onClick(v: View?) {
        when (v) {
            mBackButton -> closeSearch()
            mLayoutClearView -> closeSearch()
            mVoiceButton -> onVoiceAction()
            mClearButton -> clearInput()
            mInputText -> showListViews()
        }
    }

    private fun onSearchTypeItemClick(searchType: SearchType, position: Int){
        setCurrentSearchType(searchType, position)
        if(mSubmitOnClick){
            onSubmitText(mInputText.text)
        }
    }

    private fun onSearchSuggestItemClick(query: String){
        setQueryText(query)
        if(mSubmitOnClick){
            onSubmitText(mInputText.text)
        }
    }

    //Handle Show and Close SearchView.
    private fun showSearch() {
        if(!isSearchOpen()) {
            clearInput()
            requestInputTextFocus()
            showSearchViewLayout()
            mSearchViewListener?.onSearchViewShown()
            mIsSearchViewVisible = true
            this.visibility = View.VISIBLE
        }
    }
    private fun closeSearch() {
        if(isSearchOpen()){
            clearInput()
            handleOnInputTextFocus(false)
            hideSearchViewLayout()
            mSearchViewListener?.onSearchViewClosed()
            mIsSearchViewVisible = false
            this.visibility = View.GONE
        }
    }

    //Handle on input type clear button was pressed.
    private fun clearInput() {
        mInputText.text = null
        handleVoiceAndClearVisibility()
    }

    //Handle on voice click.
    private fun onVoiceAction() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak an item name or number")    // user hint
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)    // setting recognition model, optimized for short phrases – search queries
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)    // quantity of results we want to receive
            if (context is Activity) {
                (context as Activity).startActivityForResult(intent, REQUEST_VOICE)
            }
        } catch (e: ActivityNotFoundException) {
            Log.d("SearchView", e.message)
            Toast.makeText(context,"Sorry! Your device doesn't support speech Language",Toast.LENGTH_LONG).show()
            mAllowVoiceSearch = false
        }
    }

    //Handle on type change.
    private fun onTextChanged(newText: CharSequence) {
        handleVoiceAndClearVisibility()
        startFilter(newText)
        if(newText.isNotEmpty() && newText.isNotBlank()){
            mOnQueryChangeListener?.onQueryTextChange(newText.toString(), mCurrentSearchTypePosition)
        } else {
            mOnQueryChangeListener?.onQueryTextChange("", mCurrentSearchTypePosition)
        }
    }

    //Handle on type mSubmitOnClick.
    private fun onSubmitText(queryText: CharSequence){
        if(queryText.isNotEmpty() && queryText.isNotBlank()){
            closeSearch()
            mOnQueryChangeListener?.onQueryTextSubmit(queryText.toString(), mCurrentSearchTypePosition)
        }
    }

    //Update SearchTypeText type.
    private fun setCurrentSearchType(searchType: SearchType, adapterPosition: Int){
        mCurrentSearchType = searchType
        mCurrentSearchTypePosition = adapterPosition
        handleSearchType()
    }

    private fun handleSearchType(){
        if(mShowOnlyIconType){
            mCurrentSearchType?.iconDrawable?.let { mSearchTypeIcon.setImageResource(it) }
            showSearchTypeIcon()
            hideSearchTypeText()
        } else {
            mSearchTypeText.text = "${mCurrentSearchType?.type}: "
            hideSearchTypeIcon()
            showSearchTypeText()
        }
    }

    private fun startFilter(s: CharSequence) {
        mSearchSuggestAdapter.filter.filter(s, this@SearchView)
    }


    //Show and Hide SearchTypeText
    private fun showSearchTypeText() {
        mSearchTypeText.visibility = View.VISIBLE
    }
    private fun hideSearchTypeText() {
        mSearchTypeText.visibility = View.GONE
    }

    private fun showSearchTypeIcon() {

    }
    private fun hideSearchTypeIcon() {

    }

    //Show and Hide ListView.
    private fun showListViews() {
        if (mSearchTypeTypeAdapter.count >= 1) {
            mSearchTypesListView.visibility = View.VISIBLE
            showBackView()
        }

        /*
        if (mSearchSuggestAdapter.count >= 1) {
            mSearchSuggestListView.visibility = View.VISIBLE
            showBackView()
        }*/

    }
    private fun hideListViews() {
        mSearchTypesListView.visibility = View.GONE
        //mSearchSuggestListView.visibility = View.GONE
        hideBackView()
    }

    //Show and Hide Suggestion ListView.
    private fun showSuggestionListView(){
        mSearchSuggestListView.visibility = View.VISIBLE
    }
    private fun hideSuggestionListView(){
        mSearchSuggestListView.visibility = View.GONE
    }

    //Show and Hide Clear View.
    private fun showBackView(){
        mLayoutClearView.visibility = View.VISIBLE
    }
    private fun hideBackView(){
        mLayoutClearView.visibility = View.GONE
    }

    //Show and Hide Clear Button.
    private fun showClearButton(){
        mClearButton.visibility = View.VISIBLE
    }
    private fun hideClearButton(){
        mClearButton.visibility = View.GONE
    }

    //Check if voice is available and show action button.
    private fun showVoice(show: Boolean) {
        if (show && isVoiceAvailable() && mAllowVoiceSearch) {
            showVoiceButton()
        } else {
            hideVoiceButton()
        }
    }

    //Show and Hide Voice Button.
    private fun showVoiceButton(){
        mVoiceButton.visibility = View.VISIBLE
    }
    private fun hideVoiceButton(){
        mVoiceButton.visibility = View.GONE
    }

    //Show and Hide Keybord with view focused.
    private fun hideKeyboard(withView: View) {
        val imm = withView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(withView.windowToken, 0)
    }
    private fun showKeyboard(withView: View) {
        withView.requestFocus()
        val imm = withView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(withView, 0)
    }

    //Show and Hide SearchView layout.
    private fun showSearchViewLayout(){
        mLayoutSearchView.visibility = View.VISIBLE
    }
    private fun hideSearchViewLayout(){
        mLayoutSearchView.visibility = View.GONE
    }

    //Handle Voice and Clear buttons visibility.
    private fun handleVoiceAndClearVisibility(){
        if(mInputText.text.isNotEmpty()){
            showClearButton()
            showVoice(false)
        } else {
            hideClearButton()
            showVoice(true)
        }
    }
    //Handle Visibilities on InputText Focus.
    private fun handleOnInputTextFocus(focused: Boolean){
        if(focused){
            showListViews()
            showKeyboard(mInputText)
        } else {
            hideKeyboard(mInputText)
        }
    }
    //Request InputText focus.
    private fun requestInputTextFocus(){
        mInputText.requestFocus()
    }

    //Check if voice is available
    private fun isVoiceAvailable(): Boolean {
        if (isInEditMode) {
            return true
        }
        val pm = context.packageManager
        val activities = pm.queryIntentActivities(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
        return activities.size != 0
    }

    /**
     * Public access methods.
     */

    //Check if SearchView is opened or closed (Visibly or Gone).
    fun isSearchOpen(): Boolean {
        return mIsSearchViewVisible
    }

    fun closeSearchView(){
        closeSearch()
    }
    fun showSearchView(){
        showSearch()
    }

    fun showSearchViewList(){
        showListViews()
    }
    fun hideSearchViewList(){
        hideListViews()
    }

    //Set query/text to EditText (mInputText).
    fun setQueryText(newText: String){
        mInputText.setText(newText)
        mInputText.setSelection(mInputText.length())
    }

    /**
     * Set Adapter for searchTypes list with the given searchTypes array
     *
     * @param searchTypes array of types
     */
    fun setSearchTypes(searchTypes: ArrayList<SearchType>) {
        if(searchTypes.isNotEmpty()){
            mSearchTypeTypeAdapter.setTypeList(searchTypes)
            setCurrentSearchType(searchTypes[0], 0)
        }
    }

    /**
     * Set Adapter for suggestions list with the given suggestion array
     *
     * @param suggestions array of suggestions
     */
    fun setSearchSuggestions(suggestions: ArrayList<String>) {
        if(suggestions.isNotEmpty()){
            mSearchSuggestAdapter.setSuggestList(suggestions)
        }
    }


    /**
     * Submit the textQuery as soon as the user clicks the item.
     *
     * @param submit mSubmitOnClick state
     */
    fun setSubmitOnClick(submit: Boolean) {
        this.mSubmitOnClick = submit
    }

    /**
     * Set this listener to listen to Query Change events.
     *
     * @param listener
     */
    fun setOnQueryTextListener(listener: OnQueryTextListener) {
        mOnQueryChangeListener = listener
    }

    /**
     * Set this listener to listen to Search View open and close events
     *
     * @param listener
     */
    fun setOnSearchViewListener(listener: SearchViewListener) {
        mSearchViewListener = listener
    }

    /**
     * Call this method and pass the menu item so this class can handle click events for the Menu Item.
     *
     * @param menuItem
     */
    fun setMenuItem(menuItem: MenuItem) {
        this.mMenuItem = menuItem
        mMenuItem?.setOnMenuItemClickListener {
            showSearch()
            true
        }
    }


    /**
     * Public access Attributes
     */

    override fun setBackground(background: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mLayoutSearchBar.background = background
        } else {
            mLayoutSearchBar.setBackgroundDrawable(background)
        }
    }

    override fun setBackgroundColor(color: Int) {
        mLayoutSearchBar.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        mInputText.setTextColor(color)
    }

    fun setHintTextColor(color: Int) {
        mInputText.setHintTextColor(color)
    }

    fun setHint(hint: CharSequence) {
        mInputText.hint = hint
    }

    fun setInputType(inputType: Int) {
        mInputText.inputType = inputType
    }

    fun setCursorDrawable(drawable: Drawable) {
        try {
            val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            f.isAccessible = true
            f.set(mInputText, drawable)
        } catch (e: Exception) {
            Log.e("SearchView", e.toString())
        }

    }

    fun setVoiceIcon(drawable: Drawable) {
        mVoiceButton.setImageDrawable(drawable)
    }

    fun setClearIcon(drawable: Drawable) {
        mClearButton.setImageDrawable(drawable)
    }

    fun setBackIcon(drawable: Drawable) {
        mBackButton.setImageDrawable(drawable)
    }

    /**
     * Set custromDrawable for suggestion items.
     *
     * @param customDrawable DrawableRes
     */
    fun setSuggestionDrawable(drawable: Drawable){
        mSearchSuggestAdapter.setCustomDrawable(drawable)
    }

    fun enableVoiceSearch(enable: Boolean) {
        mAllowVoiceSearch = enable
    }

    fun setListViewBackgroundDrawable(backgroundDrawable: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSearchTypesListView.background = backgroundDrawable
            mSearchSuggestListView.background = backgroundDrawable
        } else {
            mSearchTypesListView.setBackgroundDrawable(backgroundDrawable)
            mSearchSuggestListView.setBackgroundDrawable(backgroundDrawable)
        }
    }

    fun setListViewBackgroundColor(backgroundColor: Int) {
        mSearchTypesListView.setBackgroundColor(backgroundColor)
        mSearchSuggestListView.setBackgroundColor(backgroundColor)

    }

    fun setListViewTextColor(newColorInt: Int){
        mSearchTypeTypeAdapter.setTextColor(newColorInt)
        mSearchSuggestAdapter.setTextColor(newColorInt)
    }

    fun showOnlyIconType(enable: Boolean) {
        mShowOnlyIconType = enable
    }

    /**
     * SearchView Interfaces
     */

    interface OnQueryTextListener {

        /**
         * Called when the user submits the textQuery. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the textQuery type that is to be submitted
         * @return true if the textQuery has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        fun onQueryTextSubmit(query: String, typePosition: Int): Boolean

        /**
         * Called when the textQuery type is changed by the user.
         *
         * @param newText the new content of the textQuery type field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        fun onQueryTextChange(newText: String, typePosition: Int): Boolean
    }

    interface SearchViewListener {
        fun onSearchViewShown()

        fun onSearchViewClosed()
    }


    override fun onFilterComplete(count: Int) {
        if (count > 0) {
            showSuggestionListView()
        } else {
            hideSuggestionListView()
        }
    }


}
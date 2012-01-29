package com.icsd.freebooks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.icsd.adapters.CategoriesAdapter;
import com.icsd.adapters.FeaturedAdapter;
import com.icsd.adapters.LibraryAdapter;
import com.icsd.animations.Flip3dDoAnimation;
import com.icsd.constants.ErrorCodes;
import com.icsd.structs.Book;
import com.icsd.structs.CategoriesResult;
import com.icsd.structs.FeaturedResult;
import com.icsd.threads.CheckLibraryThread;
import com.icsd.threads.DownloadBookThread;
import com.icsd.threads.OpenBookThread;
import com.icsd.threads.PopulateCategoriesThread;
import com.icsd.threads.PopulateFeaturedThread;
import com.icsd.sql.DataBaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FreeBooksActivity extends Activity implements OnClickListener, OnItemSelectedListener, AnimationListener, ErrorCodes
{
	private static final String SEARCH_URL = "http://www.feedbooks.com/books/search.atom?";
	private static final String CATEGORIES_URL = "http://www.feedbooks.com/books/categories.atom";
	private static final String FEATURED_BOOKS_URL = "http://www.feedbooks.com/featured/books";
	
	private static final String TAG = "FREEBOOKS_ACTIVITY";
	
	private Resources res;
	private short page;
	
	private ToggleButton libraryButton;
	private ToggleButton categoriesButton;
	private ToggleButton searchButton;
	private ToggleButton featuredButton;
	private TextView tvHeader;
	
	private Flip3dDoAnimation flipAnimation;
	
	private View libraryPage;
	private View categoriesPage;
	private View searchPage;
	private View featuredPage;

	private ListView categoriesList;
	private CategoriesAdapter categoriesAdapter;
	private ArrayList<CategoriesResult> categoriesResults;
	
	private GridView libraryList;
	private LibraryAdapter libraryAdapter;
	
	private ListView featuredList;
	private FeaturedAdapter featuredAdapter;
	private ArrayList<FeaturedResult> featuredResults;
	private Book book;
	private AlertDialog.Builder inLibraryDialog;
	
	private DataBaseHelper databaseHelper;
	
	private boolean simpleSearchView;
	private boolean isFeaturedLoading;
	private boolean isCategoriesLoading;
	private boolean isDeleteVisible;
	
	private short language;
	
	private PopulateCategoriesThread populateCategories;
	private PopulateFeaturedThread populateFeatured;
	private CheckLibraryThread checkLibrary;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        res = getResources();
        
        libraryButton = (ToggleButton) this.findViewById(R.id.toggleLibrary);
        categoriesButton = (ToggleButton) this.findViewById(R.id.toggleCategories);
        searchButton = (ToggleButton) this.findViewById(R.id.toggleSearch);
        featuredButton = (ToggleButton) this.findViewById(R.id.toggleFeatured);
        
        final Button deleteButton = (Button) this.findViewById(R.id.deleteButton);
        final Button okButton = (Button) this.findViewById(R.id.okButton);
        final Button searchTopButton = (Button) this.findViewById(R.id.searchButton);
        final Button reloadTopButton = (Button) this.findViewById(R.id.reloadButton);
        final Button stopTopButton = (Button) this.findViewById(R.id.stopButton);
                
        libraryButton.setOnClickListener(this);
        categoriesButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        featuredButton.setOnClickListener(this);
        searchTopButton.setOnClickListener(this);
        reloadTopButton.setOnClickListener(this);
        stopTopButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        
        tvHeader = (TextView) this.findViewById(R.id.textViewHeader);
        
        libraryPage = this.findViewById(R.id.libraryPage);
        categoriesPage = this.findViewById(R.id.categoriesPage);
        searchPage = this.findViewById(R.id.searchPage);
        featuredPage = this.findViewById(R.id.featuredPage);
        
        flipAnimation = new Flip3dDoAnimation();
        
        databaseHelper = DataBaseHelper.getInstance(this);
        databaseHelper.open();
        
        libraryList = (GridView) this.findViewById(R.id.libraryPage);
        
        //libraryAdapter.getCursor().requery();
        
        categoriesList = (ListView) this.findViewById(R.id.categoriesList);
        categoriesResults = new ArrayList<CategoriesResult>();
        categoriesAdapter = new CategoriesAdapter(this, R.xml.categories_row, categoriesResults);
        categoriesList.setAdapter(categoriesAdapter);
        categoriesList.setOnItemClickListener(new OnItemClickListener()
        	{

        		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
        		{
        			goToSubcategories(position);
        		}
        	});

        featuredList = (ListView) this.findViewById(R.id.featuredList);
        featuredResults = new ArrayList<FeaturedResult>();
        featuredAdapter = new FeaturedAdapter(this, R.xml.featured_row, featuredResults);
        featuredList.setAdapter(featuredAdapter);
        featuredList.setOnItemClickListener(new OnItemClickListener()
    	{

    		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
    		{
    			Intent goToBook = new Intent(getBaseContext(), BookPageActivity.class);
    			goToBook.putExtra("BOOK_URL", featuredResults.get(position).getBookUrl());
    			goToBook.putExtra("FROM_FEATURED", true);
    			startActivityForResult(goToBook, 5);
    		}
    	});
        book = new Book();
        
        inLibraryDialog = new AlertDialog.Builder(this);
		inLibraryDialog.setTitle(res.getString(R.string.book_in_library));
		inLibraryDialog.setMessage(res.getString(R.string.overwrite));
		inLibraryDialog.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener()
			{
			 	public void onClick(DialogInterface dialog, int id)
			 	{
			 		downloadBook();
			 	}
		 	});
		 
		 inLibraryDialog.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener()
		 	{
			 	public void onClick(DialogInterface dialog, int id)
			 	{
			 		dialog.cancel();
               }
		 	});
        //featuredList.setSelector(res.getDrawable(@null));
        
        //ColorDrawable divcolor = new ColorDrawable(Color.argb(25, 0, 0, 0));
        //featuredList.setDivider(divcolor);
        //featuredList.setDividerHeight(2);
        
        //Initial state
        categoriesPage.setVisibility(View.GONE);
        searchPage.setVisibility(View.GONE);
        featuredPage.setVisibility(View.GONE);
        libraryButton.setChecked(true);
        libraryButton.setClickable(false);
        
        this.findViewById(R.id.okButton).setVisibility(View.GONE);
        this.findViewById(R.id.placeholderRight).setVisibility(View.GONE);
        this.findViewById(R.id.imageLoading).setVisibility(View.GONE);
        this.findViewById(R.id.searchButton).setVisibility(View.GONE);
        this.findViewById(R.id.stopButton).setVisibility(View.GONE);
        this.findViewById(R.id.reloadButton).setVisibility(View.GONE);

        isFeaturedLoading = false;
    	isCategoriesLoading = false;
    	isDeleteVisible = true;
    	
        page = 1;
        language = 0;
        simpleSearchView = true;
        
        checkLibrary = new CheckLibraryThread(new WeakReference<FreeBooksActivity>(this));
        
        searchPage();
        categoriesPage();
        featuredPage();
        checkLibrary.execute();
    }
    
    
    /////////////////////////////////////////////////////////
    // Animations
    /////////////////////////////////////////////////////////
    
    public void startAnimation(View view)
    {
    	Animation loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_animation);
    	loadingAnimation.setAnimationListener(this);
    	view.startAnimation(loadingAnimation);
	}
    
	public void onAnimationEnd(Animation animation)
	{
		if(isLoading())
			startAnimation(this.findViewById(R.id.imageLoading));
		
	}
	
	public void onAnimationRepeat(Animation animation)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void onAnimationStart(Animation animation)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void stopAnimation()
	{
		if(!isLoading())
		{
			this.findViewById(R.id.placeholderRight).setVisibility(View.GONE);
			this.findViewById(R.id.imageLoading).setVisibility(View.GONE);
		}
	}
	
	//////////////////////////////////////////////////////////
	//Library Page
	//////////////////////////////////////////////////////////
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(databaseHelper.isAddBook())
			libraryAdapter.getCursor().requery();
		
		Log.d(TAG, "onActivityResult()");		
	}
	
	public void populateLibrary()
	{
		libraryAdapter = new LibraryAdapter(this, databaseHelper.fetchBooks(),true);
        libraryList.setAdapter(libraryAdapter);
	}
	
	private void showDeleteButton()
	{
		this.findViewById(R.id.deleteButton).setVisibility(View.GONE);
		this.findViewById(R.id.okButton).setVisibility(View.VISIBLE);
		isDeleteVisible = false;
		libraryAdapter.setDelVisibility(true);
		for(int i=0; i < libraryAdapter.getCount(); i++)
		{
			final RelativeLayout itemLayout = (RelativeLayout)libraryList.getChildAt(i);
			if(itemLayout != null)
			{
				final Button button = (Button) itemLayout.getChildAt(2);
				button.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void hideDeleteButton()
	{
		this.findViewById(R.id.okButton).setVisibility(View.GONE);
		this.findViewById(R.id.deleteButton).setVisibility(View.VISIBLE);
		isDeleteVisible = true;
		libraryAdapter.setDelVisibility(false);
		for(int i=0; i < libraryAdapter.getCount(); i++)
		{
			final RelativeLayout itemLayout = (RelativeLayout)libraryList.getChildAt(i);
			if(itemLayout != null)
			{
				final Button button = (Button) itemLayout.getChildAt(2);
				button.setVisibility(View.GONE);
			}
		}
	}
	
	public void onClickDelete(View view)
	{
		if(databaseHelper.removeBook(view.getId() - 100, true))
		{
			libraryAdapter.removeCover(view.getId() - 100);
			libraryAdapter.getCursor().requery();
		}
	}
	
	public void onClickOpenBook(View view)
	{
		if(isDeleteVisible)
		{
			final OpenBookThread openBook = new OpenBookThread(new WeakReference<FreeBooksActivity>(this), view.getId() - 200);
			openBook.execute();
		}
	}
	
	public void goToReaderOrPostError(short ERROR, int bookId, String author, String title, String baseUrl, String epubPath)
	{
		if(ERROR == NO_ERROR)
		{
			Intent goToReader = new Intent(getBaseContext(), EpubReaderActivity.class);
			goToReader.putExtra("BOOK_ID", bookId);
			goToReader.putExtra("AUTHOR", author);
			goToReader.putExtra("TITLE", title);
			goToReader.putExtra("BASE_URL", baseUrl);
			goToReader.putExtra("EPUB_PATH", epubPath);
			startActivity(goToReader);
		}
		else
		{
			libraryAdapter.getCursor().requery();
			postError(ERROR);
		}
	}
	
	//////////////////////////////////////////////////////////
	//Categories Page
	//////////////////////////////////////////////////////////
	
	private void categoriesPage()
	{
		if(!isOnline())
			return;
		
		this.findViewById(R.id.placeholderRight).setVisibility(View.VISIBLE);
		this.findViewById(R.id.imageLoading).setVisibility(View.VISIBLE);
		if(!isLoading())
        	startAnimation(this.findViewById(R.id.imageLoading));
		
		isCategoriesLoading = true;		
		
		if(page == 2)
		{
			this.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
			this.findViewById(R.id.reloadButton).setVisibility(View.GONE);
		}
        
        categoriesResults.clear();
		categoriesAdapter.notifyDataSetChanged();
		
		populateCategories = null;
		populateCategories = new PopulateCategoriesThread(new WeakReference<FreeBooksActivity>(this), CATEGORIES_URL);
        populateCategories.execute();
	}
	
	public void updateCategories(CategoriesResult categoriesResult)
	{
		categoriesResults.add(categoriesResult);
		categoriesAdapter.notifyDataSetChanged();
	}
	
	public void categoriesThreadFinished(boolean isCancelled, short ERROR)
	{
		isCategoriesLoading = false;
		stopAnimation();
		
		if(page == 2)
        {
        	this.findViewById(R.id.stopButton).setVisibility(View.GONE);
    		this.findViewById(R.id.reloadButton).setVisibility(View.VISIBLE);
        }
		
		if(ERROR != NO_ERROR)
        	postError(ERROR);
        
        if(isCancelled)
        	Log.w(TAG, "categoriesThreadFinished()");
	}
	
	//////////////////////////////////////////////////////////
	// Search Page
	//////////////////////////////////////////////////////////
	
	private void searchPage()
    {
		final RadioButton radioSimpleSearch = (RadioButton)this.findViewById(R.id.radioSearchSimple);
		radioSimpleSearch.setOnClickListener(this);
		
		final RadioButton radioAdvancedSearch = (RadioButton)this.findViewById(R.id.radioSearchAdvanced);
		radioAdvancedSearch.setOnClickListener(this);
		
		final Spinner languageSpinner = (Spinner)this.findViewById(R.id.spinnerLanguage);
		languageSpinner.setOnItemSelectedListener(this);
		
		final String[] languages = { res.getString(R.string.all), res.getString(R.string.english), 
									res.getString(R.string.french), res.getString(R.string.german), res.getString(R.string.spanish)};
		
		ArrayAdapter<String> languagesAdapter = new ArrayAdapter<String>(this, R.xml.spinner_item, languages);
		languageSpinner.setAdapter(languagesAdapter);
		languageSpinner.setSelection(0);
		
		setSearchSimpleView();
    }
    
	private void setSearchSimpleView()
	{
		((TextView)findViewById(R.id.textQuery)).setText(res.getString(R.string.search_));

		this.findViewById(R.id.textTitle).setVisibility(View.GONE);
		this.findViewById(R.id.textAuthor).setVisibility(View.GONE);
		this.findViewById(R.id.textLanguage).setVisibility(View.GONE);
		this.findViewById(R.id.inputTitle).setVisibility(View.GONE);
		this.findViewById(R.id.inputAuthor).setVisibility(View.GONE);
		this.findViewById(R.id.spinnerLanguage).setVisibility(View.GONE);
		simpleSearchView = true;
	}
	
	private void setSearchAdvancedView()
	{
		((TextView)findViewById(R.id.textQuery)).setText(res.getString(R.string.query_));
		
		this.findViewById(R.id.textTitle).setVisibility(View.VISIBLE);
		this.findViewById(R.id.textAuthor).setVisibility(View.VISIBLE);
		this.findViewById(R.id.textLanguage).setVisibility(View.VISIBLE);
		this.findViewById(R.id.inputTitle).setVisibility(View.VISIBLE);
		this.findViewById(R.id.inputAuthor).setVisibility(View.VISIBLE);
		this.findViewById(R.id.spinnerLanguage).setVisibility(View.VISIBLE);
		simpleSearchView = false;
	}
	
	private String getSearchUrl(String query)
	{
		//remove spaces from the query
		query = query.trim();
		query = query.replace(' ', '+');
		String url = SEARCH_URL;
		
		if(!query.equals(""))
			url = SEARCH_URL + "lang=all&query="+query;
		else
			url = SEARCH_URL + "lang=all&query=";
		
		Log.i(TAG, "getSearchUrl():\nurl: "+url);
		return url;
	}
	
	private String getSearchUrl(String query, String title, String author, int lang)
	{
		String url = SEARCH_URL;

		boolean termAdded = false;

		String language = "lang=all&query=";
		
		switch(lang)
		{
			case 0 :
				break;
			case 1 :
				language = "lang=en&query=";
			case 2 :
				language = "lang=fr&query=";
			case 3 :
				language = "lang=de&query=";
			case 4 :
				language = "lang=es&query=";
		}
		
		url = url + language;

		query = query.trim();
		query = query.replace(' ', '+');

		
		if(!query.equals(""))
		{
			url = url + query;
			termAdded = true;
		}
		
		title = title.trim();
		title = title.replace(' ', '+');
		
					
		if(!title.equals(""))
		{
			if(termAdded)
				url = url + "+";
			
			url = url + "title%3A" + "\""+ title + "\"";
			termAdded = true;
		}

		author = author.trim();
		author = author.replace(' ', '+');

		if(!author.equals(""))
		{
			if(termAdded)
				url = url + "+";
			
			url = url + "author%3A" + "\"" + author + "\"";
		}

		return url;
	}
	
	////////////////////////////////////////////////////////////////
	// Featured Paged
	////////////////////////////////////////////////////////////////
	
	private void featuredPage()
	{
		if(!isOnline())
			return;
		
		this.findViewById(R.id.placeholderRight).setVisibility(View.VISIBLE);
		this.findViewById(R.id.imageLoading).setVisibility(View.VISIBLE);
		
		if(page == 4)
		{
			this.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
			this.findViewById(R.id.reloadButton).setVisibility(View.GONE);
		}
        
        if(!isLoading())
        	startAnimation(this.findViewById(R.id.imageLoading));
        
        isFeaturedLoading = true;
        
		featuredResults.clear();
		featuredAdapter.notifyDataSetChanged();
		populateFeatured = null;
		populateFeatured = new PopulateFeaturedThread(new WeakReference<FreeBooksActivity>(this), FEATURED_BOOKS_URL);
        populateFeatured.execute();
	}
	
	public void updateFeatured(FeaturedResult result)
    {
		if(result.getCover() == null)
		{
			final Drawable nocover = res.getDrawable(R.drawable.book_cover);
			result.setCover(nocover); 
		}
		
    	featuredResults.add(result);
    	featuredAdapter.notifyDataSetChanged();
    }
	
	public void featuredThreadFinished(boolean isCancelled, short ERROR)
	{
		isFeaturedLoading = false;
		stopAnimation();
        
        if(page == 4)
        {
        	this.findViewById(R.id.stopButton).setVisibility(View.GONE);
    		this.findViewById(R.id.reloadButton).setVisibility(View.VISIBLE);
        }
        
        if(ERROR != NO_ERROR)
        	postError(ERROR);
        
        if(isCancelled)
        	Log.w(TAG, "featuredThreadFinished()");
	}
	
	private void downloadBook()
	{
		final DownloadBookThread downloadBook = new DownloadBookThread(new WeakReference<FreeBooksActivity>(this),
				new WeakReference<Book>(book), true, true);
		downloadBook.execute();
	}
	
	public void onClickFeatured(View view)
	{
		final int position = view.getId() -300;
		book.clear();
		book.setTitle(featuredResults.get(position).getTitle());
		book.setAuthor(featuredResults.get(position).getAuthor());
		book.setCoverUrl(featuredResults.get(position).getCoverUrl());
		book.setLanguage("en");
		book.setEpubUrl(featuredResults.get(position).getEpubUrl());
				
		if(databaseHelper.isInLibrary(book.getTitle(), book.getAuthor(), "en"))
			inLibraryDialog.show();
		else
		{
			final DownloadBookThread downloadBook = new DownloadBookThread(new WeakReference<FreeBooksActivity>(this),
					new WeakReference<Book>(book), false, true);
			downloadBook.execute();
		}
	}
	
	public void updateLibrary()
	{
		if(databaseHelper.isAddBook())
			libraryAdapter.getCursor().requery();
	}
	
	///////////////////////////////////////////////////////////////////////
	//Buttons
	///////////////////////////////////////////////////////////////////////
  
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.toggleLibrary:
			goToLibrary();
			break;
		case R.id.toggleCategories:
			goToCategories();
			break;
		case R.id.toggleSearch:
			goToSearch();
			break;
		case R.id.toggleFeatured:
			goToFeatured();
			break;
		case R.id.radioSearchSimple:
			setSearchSimpleView();
			simpleSearchView = true;
			break;
		case R.id.radioSearchAdvanced:
			setSearchAdvancedView();
			simpleSearchView = false;
			break;
		case R.id.deleteButton:
			showDeleteButton();
			break;
		case R.id.okButton:
			hideDeleteButton();
			break;
		case R.id.searchButton:
			goToResults();
			break;
		case R.id.reloadButton:
			switch(page)
			{
			case 2:
				if(!isCategoriesLoading)
					categoriesPage();
				break;
			case 4:
				if(!isFeaturedLoading)
					featuredPage();
				break;
			}
			break;
		case R.id.stopButton:
			switch(page)
			{
			case 2:
				if(isCategoriesLoading)
				{
					if((populateCategories != null) && (populateCategories.getStatus() != AsyncTask.Status.FINISHED))
						populateCategories.cancel(true);
				}
				break;
			case 4:
				if(isFeaturedLoading)
				{
					if((populateFeatured != null) && (populateFeatured.getStatus() != AsyncTask.Status.FINISHED))
						populateFeatured.cancel(true);
				}

				break;
			}
			break;
		}
	}
	
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3)
	{
		language = (short) pos;		
	}

	public void onNothingSelected(AdapterView<?> arg0)
	{
		// TODO Auto-generated method stub
		
	}

	private void goToLibrary()
	{
		setButtons(true, false, false, false);
		flipAnimation.animate(getPage(), libraryPage);
		page = 1;
		setTopButtons();
		tvHeader.setText(R.string.library);
	}
	
	private void goToCategories()
	{
		setButtons(false, true, false, false);
		flipAnimation.animate(getPage(), categoriesPage);
		page = 2;
		setTopButtons();
		tvHeader.setText(R.string.categories);
	}
	
	private void goToSearch()
	{
		setButtons(false, false, true, false);
		flipAnimation.animate(getPage(), searchPage);
		page = 3;
		setTopButtons();
		tvHeader.setText(R.string.search);
	}
	
	private void goToFeatured()
	{
		setButtons(false, false, false, true);
		flipAnimation.animate(getPage(), featuredPage);
		page = 4;
		setTopButtons();
		tvHeader.setText(R.string.featured);
	}
	
	private void goToResults()
	{
		final String query = ((TextView)findViewById(R.id.inputQuery)).getText().toString();
		final String title = ((TextView)findViewById(R.id.inputTitle)).getText().toString();
		final String author = ((TextView)findViewById(R.id.inputAuthor)).getText().toString();
		
		final String url;
		
		if(simpleSearchView)
			url = getSearchUrl(query);
		else
			url = getSearchUrl(query, title, author, language);
		
		Intent goToResults = new Intent(getBaseContext(), ResultsPageActivity.class);
		goToResults.putExtra("SEARCH_URL", url);
		goToResults.putExtra("FROM_BOOK", false);
		startActivityForResult(goToResults,3);
	}
	
	private void goToSubcategories(int position)
	{
		final String url = categoriesResults.get(position).getUrl();
		Intent goToSubcategories = new Intent(getBaseContext(), SubcategoriesPageActivity.class);
		goToSubcategories.putExtra("URL", url);
		startActivityForResult(goToSubcategories,4);
	}

	private void setButtons(boolean lib, boolean cat, boolean srch, boolean feat)
	{
		libraryButton.setChecked(lib);
		categoriesButton.setChecked(cat);
		searchButton.setChecked(srch);
		featuredButton.setChecked(feat);
		
		libraryButton.setClickable(!lib);
		categoriesButton.setClickable(!cat);
		searchButton.setClickable(!srch);
		featuredButton.setClickable(!feat);
		
	}
	
	private void setTopButtons()
	{
		switch(page)
		{
		case 1:
			this.findViewById(R.id.searchButton).setVisibility(View.GONE);
			this.findViewById(R.id.reloadButton).setVisibility(View.GONE);
			this.findViewById(R.id.stopButton).setVisibility(View.GONE);
			
			if(isDeleteVisible)
			{
				this.findViewById(R.id.okButton).setVisibility(View.GONE);
				this.findViewById(R.id.deleteButton).setVisibility(View.VISIBLE);
			}
			else
			{
				this.findViewById(R.id.deleteButton).setVisibility(View.GONE);
				this.findViewById(R.id.okButton).setVisibility(View.VISIBLE);
			}
			break;
		case 2:
			this.findViewById(R.id.searchButton).setVisibility(View.GONE);
			this.findViewById(R.id.okButton).setVisibility(View.GONE);
			this.findViewById(R.id.deleteButton).setVisibility(View.GONE);
			
			if(isCategoriesLoading)
			{
				this.findViewById(R.id.reloadButton).setVisibility(View.GONE);
				this.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
			}
			else
			{
				this.findViewById(R.id.stopButton).setVisibility(View.GONE);
				this.findViewById(R.id.reloadButton).setVisibility(View.VISIBLE);
			}
			
			break;
		case 3:
			this.findViewById(R.id.reloadButton).setVisibility(View.GONE);
			this.findViewById(R.id.stopButton).setVisibility(View.GONE);
			this.findViewById(R.id.okButton).setVisibility(View.GONE);
			this.findViewById(R.id.deleteButton).setVisibility(View.GONE);
			this.findViewById(R.id.searchButton).setVisibility(View.VISIBLE);
			break;
		case 4:
			this.findViewById(R.id.searchButton).setVisibility(View.GONE);
			this.findViewById(R.id.okButton).setVisibility(View.GONE);
			this.findViewById(R.id.deleteButton).setVisibility(View.GONE);
			
			if(isFeaturedLoading)
			{
				this.findViewById(R.id.reloadButton).setVisibility(View.GONE);
				this.findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
			}
			else
			{
				this.findViewById(R.id.stopButton).setVisibility(View.GONE);
				this.findViewById(R.id.reloadButton).setVisibility(View.VISIBLE);
			}
			break;
		}
	}
	
	////////////////////////////////////////////////////////////////////////
	//Utility
	//////////////////////////////////////////////////////////////////////////
	
	
	
	private boolean isLoading()
	{
		if(isFeaturedLoading || isCategoriesLoading)
			return true;
		else
			return false;
	}
	
	public boolean isOnline()
	{
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    
	    if (netInfo != null && netInfo.isConnected())
	    	return true;

	    return false;
	}
	
	private void postError(short ERROR)
	{
		final String message;
			
		switch(ERROR)
		{
			case MALFORMED_URL:
				message = res.getString(R.string.malformed_url_exception);
				break;
			case PARSER_CONFIG_ERROR:
				message = res.getString(R.string.parser_config_exception);
				break;
			case SAX_ERROR:
				message = res.getString(R.string.parser_exception);
				break;
			case IO_ERROR:
				message = res.getString(R.string.categories_no_connection);
				break;
			case BOOK_DOESNT_EXIST:
				message = res.getString(R.string.book_not_exist);
				break;
			case IO_ERROR + 10:
				message = res.getString(R.string.featured_no_connection);
				break;
			default:
				message = res.getString(R.string.error);
				break;
		}
		
		final Toast msg = Toast.makeText(this, message, Toast.LENGTH_LONG);
		msg.show();
	}
	
	private View getPage()
	{
		switch(page)
		{
		case 1:
			return libraryPage;
		case 2:
			return categoriesPage;
		case 3:
			return searchPage;
		case 4:
			return featuredPage;
		default:
			return searchPage;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		Log.i(TAG, "[CALLBACK] onDestroy()");
		if ((populateCategories != null) && (populateCategories.getStatus() != AsyncTask.Status.FINISHED))
			populateCategories.cancel(true);
		
		if ((populateFeatured != null) && (populateFeatured.getStatus() != AsyncTask.Status.FINISHED))
			populateFeatured.cancel(true);
		
		if ((checkLibrary != null) && (checkLibrary.getStatus() != AsyncTask.Status.FINISHED))
			checkLibrary.cancel(true);
		
		databaseHelper.close();
		super.onDestroy();
	}
}
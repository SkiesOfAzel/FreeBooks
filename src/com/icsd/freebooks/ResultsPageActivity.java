package com.icsd.freebooks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.icsd.adapters.ResultsAdapter;
import com.icsd.constants.ErrorCodes;
import com.icsd.structs.SearchResult;
import com.icsd.threads.PopulateResultsThread;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ResultsPageActivity extends Activity implements OnClickListener, OnItemClickListener, AnimationListener, ErrorCodes
{
	private String url;
	private ListView listView;
	private ResultsAdapter adapter;
	private ArrayList<SearchResult> results;
	private boolean isCancelled;
	private boolean isLoading;
	private boolean isFromBook;
	private boolean isSearchByAuthor;
	private PopulateResultsThread populateList;
	private Resources res;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_page);
        Bundle extras = getIntent().getExtras();
        
        if(extras!=null)
        {
        	url = extras.getString("SEARCH_URL");
        	isFromBook = extras.getBoolean("FROM_BOOK", false);
        	isSearchByAuthor = extras.getBoolean("URL_TYPE", false);
        }
        else
        {
        	url = "";
        	isFromBook = false;
        	isSearchByAuthor= false;
        }
        
        isCancelled = false;
        isLoading = false;
                        
        res = getResources();
        
        listView = (ListView) this.findViewById(R.id.resultsList);
        results = new ArrayList<SearchResult>();
        adapter = new ResultsAdapter(this, R.xml.results_row, results);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(new OnItemClickListener()
    	{

    		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
    		{
    			if(isFromBook)
    				returnToBook(position);
    			else
    				goToBook(position);
    		}
    	});
        
        final Button resultSearchButton = (Button) this.findViewById(R.id.resultSearchButton);
        resultSearchButton.setOnClickListener(this);
        
        final Button resultStopButton = (Button) this.findViewById(R.id.resultStopButton);
        resultStopButton.setOnClickListener(this);
        
        getResults();
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
		if(isLoading)
			startAnimation(this.findViewById(R.id.resImageLoading));

	}

	public void onAnimationRepeat(Animation animation)
	{
		// TODO Auto-generated method stub

	}

	public void onAnimationStart(Animation animation)
	{
		// TODO Auto-generated method stub

	}
	
	public void stopAnimation()
	{
		isLoading = false;
		this.findViewById(R.id.resPlaceholderRight).setVisibility(View.GONE);
        this.findViewById(R.id.resImageLoading).setVisibility(View.GONE);
	}
	
	////////////////////////////////////////////////////
	//Results
	////////////////////////////////////////////////////
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK && requestCode==1)
		{
			url = data.getStringExtra("RETURNED_URL");
			isSearchByAuthor = data.getBooleanExtra("URL_TYPE", false);
			getResults();
		}
	}
	  
	private void getResults()
	{
		isLoading = true;
		
		startAnimation(this.findViewById(R.id.resImageLoading));
		
		this.findViewById(R.id.resultSearchButton).setVisibility(View.GONE);
		this.findViewById(R.id.resultStopButton).setVisibility(View.VISIBLE);
		
		results.clear();
		adapter.notifyDataSetChanged();
        
		populateList = null;
		populateList = new PopulateResultsThread(new WeakReference<ResultsPageActivity>(this), url, isSearchByAuthor);
        populateList.execute();
	}
    
    public void updateList(SearchResult result)
    {
    	results.add(result);
    	adapter.notifyDataSetChanged();
    }
    
    public void threadFinished(short ERROR)
    {
    	stopAnimation();
    	this.findViewById(R.id.resultStopButton).setVisibility(View.GONE);
    	this.findViewById(R.id.resultSearchButton).setVisibility(View.VISIBLE);
		
    	if(results.size() == 0)
    		postError(NO_RESULTS);

    	else if (ERROR == THREAD_CANCELLED)
    	{
    		if(isCancelled)
    		{
    			postError(ERROR);
    			isCancelled = false;
    		}
    	}
    	else
    		postError(ERROR);
    }

    public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.resultSearchButton:
			getResults();
			break;
			
		case R.id.resultStopButton:
			if((populateList != null) && (populateList.getStatus() != AsyncTask.Status.FINISHED))
				populateList.cancel(true);
			isCancelled = true;
			break;
		}	
	}
	
	private void postError(short ERROR)
	{
		if(ERROR != NO_ERROR)
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
					message = res.getString(R.string.results_no_connection);
					break;
				case THREAD_CANCELLED:
					message = res.getString(R.string.search_canceled);
					break;
				case NO_RESULTS:
					message = res.getString(R.string.no_results);
					break;
				default:
					message = res.getString(R.string.search_canceled);
					break;
			}
			
			final Toast msg = Toast.makeText(this, message, Toast.LENGTH_LONG);
			msg.show();
		}
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void goToBook(int position)
	{
		if (populateList != null && populateList.getStatus() != AsyncTask.Status.FINISHED)
			populateList.cancel(true);
		
		isLoading = false;
		
		Intent goToBook = new Intent(getBaseContext(), BookPageActivity.class);
		goToBook.putExtra("BOOK_URL", results.get(position).getBookUrl());
		goToBook.putExtra("FROM_FEATURED", false);
		startActivityForResult(goToBook, 1);
	}
	private void returnToBook(int position)
	{
		final Intent intent= getIntent();
		intent.putExtra("RETURNED_URL", results.get(position).getBookUrl());
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	protected void onDestroy()
	{
		if (populateList != null && populateList.getStatus() != AsyncTask.Status.FINISHED)
			populateList.cancel(true);
		
		super.onDestroy();
	}
}

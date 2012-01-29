package com.icsd.freebooks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.icsd.adapters.SubcategoriesAdapter;
import com.icsd.constants.ErrorCodes;
import com.icsd.structs.SubcategoriesResult;
import com.icsd.threads.PopulateSubcategoriesThread;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SubcategoriesPageActivity extends Activity implements OnClickListener, AnimationListener, ErrorCodes
{
	//private final static String TAG = "SUBCATEGORIES_ACTIVITY";
	
	private SubcategoriesAdapter adapter;
	private ArrayList<SubcategoriesResult> results;
	private Resources res;
	private PopulateSubcategoriesThread populateSubcategories;
	
	private String url;
	private boolean isLoading;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.subcategories_page);

        Bundle extras = getIntent().getExtras();
        
        if(extras!=null)
        	url = extras.getString("URL");
        else
        	url = null;
        
        res = getResources();
        
        final Button reloadButton = (Button) this.findViewById(R.id.subReloadButton);
        reloadButton.setOnClickListener(this);
        
        final Button stopButton = (Button) this.findViewById(R.id.subStopButton);
        stopButton.setOnClickListener(this);
        
        final ListView listView = (ListView) this.findViewById(R.id.subcategoriesList);
        results = new ArrayList<SubcategoriesResult>();
        adapter = new SubcategoriesAdapter(this, R.xml.subcategories_row, results);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener()
        	{

        		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
        		{
        			goToLanguages(position);
        		}
        	});
        
        subcategoriesPage();       
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
			startAnimation(this.findViewById(R.id.subImageLoading));

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
		isLoading = false;
		this.findViewById(R.id.subPlaceholderRight).setVisibility(View.GONE);
        this.findViewById(R.id.subImageLoading).setVisibility(View.GONE);
	}
	
	//////////////////////////////////////////////////////
	//Results
	//////////////////////////////////////////////////////
	
	private void subcategoriesPage()
	{
		this.findViewById(R.id.subReloadButton).setVisibility(View.GONE);
		this.findViewById(R.id.subStopButton).setVisibility(View.VISIBLE);
		
		isLoading = true;
		this.findViewById(R.id.subPlaceholderRight).setVisibility(View.VISIBLE);
        this.findViewById(R.id.subImageLoading).setVisibility(View.VISIBLE);
        startAnimation(this.findViewById(R.id.subImageLoading));
        
        results.clear();
		adapter.notifyDataSetChanged();
		
		populateSubcategories = null;
		populateSubcategories = new PopulateSubcategoriesThread(new WeakReference<SubcategoriesPageActivity>(this), url);
        populateSubcategories.execute();
	}
	
	public void updateSubcategories(SubcategoriesResult result)
	{
		results.add(result);
		adapter.notifyDataSetChanged();
	}
	
	public void threadFinished(short error)
	{
		stopAnimation();
		this.findViewById(R.id.subStopButton).setVisibility(View.GONE);
		this.findViewById(R.id.subReloadButton).setVisibility(View.VISIBLE);
		postError(error);
	}

	////////////////////////////////////////////////////
	//Buttons
	////////////////////////////////////////////////////
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.subReloadButton:
			subcategoriesPage();
			break;
		case R.id.subStopButton:
			if((populateSubcategories != null) && (populateSubcategories.getStatus() != AsyncTask.Status.FINISHED))
				populateSubcategories.cancel(true);
			break;
		}
	}
	
	///////////////////////////////////////////////////
	// Utility
	///////////////////////////////////////////////////
	
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
					message = res.getString(R.string.categories_no_connection);
					break;
				default:
					message = res.getString(R.string.error);
					break;
			}
			
			final Toast msg = Toast.makeText(this, message, Toast.LENGTH_LONG);
			msg.show();
		}
	}
	
	private void goToLanguages(int position)
	{
		final String url = results.get(position).getUrl();
		Intent goToLanguage = new Intent(getBaseContext(), LanguagePageActivity.class);
		goToLanguage.putExtra("URL", url);
		startActivity(goToLanguage);
	}
	
	@Override
	protected void onDestroy()
	{
		if ((populateSubcategories != null) && (populateSubcategories.getStatus() != AsyncTask.Status.FINISHED))
			populateSubcategories.cancel(true);
		
		super.onDestroy();
	}
}

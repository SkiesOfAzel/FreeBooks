package com.icsd.freebooks;

import com.icsd.adapters.LanguagesAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class LanguagePageActivity extends Activity
{

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_page);
        
        final String url;
        
        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        	url = extras.getString("URL");	
        else
        	url = "";

        final Resources res = getResources();
        
        final ListView listView = (ListView) this.findViewById(R.id.languageList);
        final String[] languages = new String[] {res.getString(R.string.english), res.getString(R.string.french), res.getString(R.string.german), res.getString(R.string.spanish)};
        final LanguagesAdapter adapter = new LanguagesAdapter(this, R.xml.categories_row, languages);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        
        listView.setOnItemClickListener(new OnItemClickListener()
    	{

    		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
    		{
    			goToResults(url, position);
    		}
    	});
    }
	
	private void goToResults(String url, int position)
	{
		final String myUrl;
		
		switch(position)
		{
		case 1:
			myUrl = url.replace("lang=en", "lang=fr");
			break;
		
		case 2:
			myUrl = url.replace("lang=en", "lang=de");
			break;
			
		case 3:
			myUrl = url.replace("lang=en", "lang=es");
			break;
			
		default:
			myUrl = url;
			break;
		}
		
		Intent goToResults = new Intent(getBaseContext(), ResultsPageActivity.class);
		goToResults.putExtra("SEARCH_URL", myUrl);
		startActivity(goToResults);
	}
}

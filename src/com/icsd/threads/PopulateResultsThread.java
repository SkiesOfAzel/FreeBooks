package com.icsd.threads;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import com.icsd.structs.SearchResult;
import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.ResultsPageActivity;
import com.icsd.handlers.ResultsHandler;
import android.os.AsyncTask;
import android.util.Log;

public class PopulateResultsThread extends AsyncTask<Void, SearchResult, Void> implements ErrorCodes
{
	private final static String TAG = "POPULATE_RESULTS_THREAD";
	
    private final String url;
    private final boolean searchByAuthor;
    private WeakReference<ResultsPageActivity> parent;
    
	private short ERROR;

    
    public PopulateResultsThread(WeakReference<ResultsPageActivity> parent, String url, boolean searchByAuthor)
    {
    	ERROR = NO_ERROR;
		this.url = url;
		this.parent = parent;
    	this.searchByAuthor = searchByAuthor;

    }
    
    public void onProgressUpdate(SearchResult result)
    {
    	publishProgress(result);
    }
    
    @Override
    protected void onPreExecute()
    {

    }
    
    @Override
    protected void onProgressUpdate(SearchResult... values)
    {
    	if(parent != null)
    		parent.get().updateList(values[0]);
    }
    
    @Override
    protected void onCancelled()
    {
    	if(parent != null)
    		parent.get().threadFinished(THREAD_CANCELLED);
    }
    
	@Override
    protected void onPostExecute(Void params)
    {
		if(parent != null)
			parent.get().threadFinished(ERROR);
    }
    
	@Override
	protected Void doInBackground(Void... params)
	{
		int pass = 1;
		boolean repeat = false;
		URL newUrl;
		//Log.e("ON_THREAD", "pass");

		final String page;
		
		if (searchByAuthor)
			page = "?page=";
		else
			page = "&page=";

		try
		{
			//sax stuff
			final SAXParserFactory factory = SAXParserFactory.newInstance(); 
			final SAXParser parser = factory.newSAXParser(); 
		 
			final XMLReader reader = parser.getXMLReader(); 
		 
			//initialize our parser logic
			final ResultsHandler resultHandler = new ResultsHandler(this, parent.get().getResources()); 
			reader.setContentHandler(resultHandler); 
			
			//the search results per page are 20, so we need to parse more than one page when
			//the results are more than 20
			do
			{ 
				if(isCancelled())
					break;

				//tell the garbage collector to free up memory from the previous URL object 
				newUrl = null;

				//form the url of the new feed
				if(pass == 1)
					newUrl = new URL(url);
				else 
					newUrl = new URL(url+page+pass);
				
				//get the xml feed
				reader.parse(new InputSource(newUrl.openStream())); 

				//check to see if we are on the last page of the results
				if(resultHandler.getTotalResults() >= (resultHandler.getItemsPerPage()*pass))
					repeat = true;
				else
					repeat = false;
				
				//Log.e("RESULTS_SIZE", "= "+results.size());
				
				pass ++;
				
			} while(repeat);
		}
		catch (MalformedURLException e)
		{
			ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: "+e+"\nOn pass: "+pass);
			repeat = false;
		}
		catch(ParserConfigurationException pce)
		{ 
			ERROR = PARSER_CONFIG_ERROR;
			Log.e(TAG, "ParserConfigurationException: "+pce+"\nOn pass: "+pass);
			repeat = false;
		}
		catch(SAXException se)
		{ 
			ERROR = SAX_ERROR;
			Log.e(TAG, "SAXException: "+se+"\nOn pass: "+pass);
			repeat = false;
		}
		catch(IOException ioe)
		{ 
			ERROR = IO_ERROR;
			Log.e(TAG, "IOException: "+ioe+"\nOn pass: "+pass);
			repeat = false;
		}
		return null;
	}
}
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
import com.icsd.structs.SubcategoriesResult;
import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.SubcategoriesPageActivity;
import com.icsd.handlers.SubcategoriesHandler;

import android.os.AsyncTask;
import android.util.Log;

public class PopulateSubcategoriesThread extends AsyncTask<Void, SubcategoriesResult, Void> implements ErrorCodes
{
	private final static String PAGE = "&page=";
	private final static String TAG = "POPULATE_SUBCATEGORIES_THREAD";
	
    private final String url;
    private final WeakReference<SubcategoriesPageActivity> parent;
	private short ERROR;

    
    public PopulateSubcategoriesThread(WeakReference<SubcategoriesPageActivity> parent, String url)
    {
    	this.parent = parent;
    	ERROR = NO_ERROR;
		this.url = url;
    }
    
    public void onProgressUpdate(SubcategoriesResult result)
    {
    	publishProgress(result);
    }
    
    @Override
    protected void onPreExecute()
    {

    }
    
    @Override
    protected void onCancelled()
    {
    	parent.get().threadFinished(ERROR);
    }
    
    @Override
    protected void onProgressUpdate(SubcategoriesResult... values)
    {
    	parent.get().updateSubcategories(values[0]);
    }
    
	@Override
    protected void onPostExecute(Void params)
    {
		parent.get().threadFinished(ERROR);
    }
    
	@Override
	protected Void doInBackground(Void... params)
	{
		URL newUrl;
		int pass = 1;
		boolean repeat = false;
		
		try
		{
			newUrl = new URL(url);
			
			//sax stuff
			final SAXParserFactory factory = SAXParserFactory.newInstance(); 
			final SAXParser parser = factory.newSAXParser(); 
		 
			final XMLReader reader = parser.getXMLReader(); 
		 
			//initialize our parser logic
			final SubcategoriesHandler subcategoriesHandler = new SubcategoriesHandler(this); 
			reader.setContentHandler(subcategoriesHandler); 

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
					newUrl = new URL(url+PAGE+pass);
				
				//get the xml feed
				reader.parse(new InputSource(newUrl.openStream()));
				
				//check to see if we are on the last page of the results
				if(subcategoriesHandler.getTotalResults() >= (subcategoriesHandler.getItemsPerPage()*pass))
					repeat = true;
				else
					repeat = false;
				
				pass++;			
			 
			}while(repeat);
		}
		
		catch (MalformedURLException e)
		{
			ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: " + e);
		}
		catch(ParserConfigurationException pce)
		{ 
			ERROR = PARSER_CONFIG_ERROR;
			Log.e(TAG, "ParserConfigurationException: " + pce);
		}
		catch(SAXException se)
		{ 
			ERROR = SAX_ERROR;
			Log.e(TAG, "SAXException: " + se);
		}
		catch(IOException ioe)
		{ 
			ERROR = IO_ERROR;
			Log.e(TAG, "IOException: " + ioe);
		}
		return null;
	}
}
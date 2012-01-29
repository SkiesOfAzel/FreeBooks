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

import com.icsd.structs.CategoriesResult;
import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.FreeBooksActivity;
import com.icsd.handlers.CategoriesHandler;
import android.os.AsyncTask;
import android.util.Log;

public class PopulateCategoriesThread extends AsyncTask<Void, CategoriesResult, Void> implements ErrorCodes
{
	private final static String TAG = "POPULATE_CATEGORIES_THREAD";
	
    private final String url;
    private final WeakReference<FreeBooksActivity> parent;
    
	private short ERROR;

    
    public PopulateCategoriesThread(WeakReference<FreeBooksActivity> parent, String url)
    {
    	ERROR = NO_ERROR;
		this.url = url;
		this.parent = parent;
    }
    
    public void onProgressUpdate(CategoriesResult result)
    {
    	publishProgress(result);
    }
    
    public void updateProgress(CategoriesResult result)
    {
    	this.publishProgress(result);
    }
    
    @Override
    protected void onPreExecute()
    {

    }
    
    @Override
    protected void onProgressUpdate(CategoriesResult... values)
    {
    	parent.get().updateCategories(values[0]);
    }
    
    @Override
    protected void onCancelled()
    {
    	Log.w(TAG, "onCancelled()");
    	parent.get().categoriesThreadFinished(true, ERROR);
    }
    
	@Override
    protected void onPostExecute(Void params)
    {
		parent.get().categoriesThreadFinished(false, ERROR);
    }
    
	@Override
	protected Void doInBackground(Void... params)
	{
		URL newUrl;
		try
		{
			newUrl = new URL(url);
			
			//sax stuff
			final SAXParserFactory factory = SAXParserFactory.newInstance(); 
			final SAXParser parser = factory.newSAXParser(); 
		 
			final XMLReader reader = parser.getXMLReader(); 
		 
			//initialize our parser logic
			final CategoriesHandler categoriesHandler = new CategoriesHandler(this); 
			reader.setContentHandler(categoriesHandler); 

			//get the xml feed
			reader.parse(new InputSource(newUrl.openStream())); 
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
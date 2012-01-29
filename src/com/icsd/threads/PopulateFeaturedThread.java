package com.icsd.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.icsd.structs.FeaturedResult;
import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.FreeBooksActivity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class PopulateFeaturedThread extends AsyncTask<Void, FeaturedResult, Void> implements ErrorCodes
{
	private final static String TAG = "POPULATE_FEATURED_THREAD";
    private final String url;
    private final WeakReference<FreeBooksActivity> parent;
        
	private short ERROR;
	//private short COVER_ERROR;

    
    public PopulateFeaturedThread(WeakReference<FreeBooksActivity> parent, String url)
    {
    	ERROR = NO_ERROR;
    	//COVER_ERROR = NO_ERROR;
		this.url = url;
		this.parent = parent;
    }
    
    @Override
    protected void onPreExecute()
    {

    }
    
    @Override
    protected void onProgressUpdate(FeaturedResult... values)
    {
   		parent.get().updateFeatured(values[0]);
    }
    
    @Override
    protected void onCancelled()
    {
    	Log.w(TAG, "onCancelled()");
    	parent.get().featuredThreadFinished(true, ERROR);
    }
    
	@Override
    protected void onPostExecute(Void params)
    {
		parent.get().featuredThreadFinished(false, ERROR);
    }
    
	@Override
	protected Void doInBackground(Void... params)
	{
		URL newUrl;

		try
		{
			System.setProperty("http.agent", "");
			newUrl = new URL(url);
			final HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
            
	        connection.setDoInput(true);
	        connection.connect();
	        
	        if(this.isCancelled())
	        	return null;
       
	        BufferedReader r = new BufferedReader(new InputStreamReader(newUrl.openStream(), "UTF-8"));
	        StringBuilder total = new StringBuilder();
	        String line;
	        while ((line = r.readLine()) != null)
	        {
	            total.append(line);
	        }
	        
	        String page = total.toString();
	        
	        int	book = page.indexOf("http://schema.org/Book");
	        int coverStart = 0;
	        int coverEnd = 0;
	        int coverBigEnd = 0;
	        int titleStart = 0;
	        int titleEnd = 0;
	        int urlStart = 0;
	        int urlEnd = 0;
	        int authorStart = 0;
	        int authorEnd = 0;

	        if(this.isCancelled())
	        	return null;
	        
	        while(book != -1)
	        {
	        	final FeaturedResult featuredResult = new FeaturedResult();
	        	
	        	page = page.substring(book);

	        	urlStart = page.indexOf("<a href=\"") +9;
	        	page = page.substring(urlStart);
	        	urlEnd = page.indexOf("\"");
	        	final String bookUrl = page.substring(0, urlEnd);
	        	featuredResult.setBookUrl(bookUrl);
	        	featuredResult.setEpubUrl(bookUrl.substring(0, bookUrl.lastIndexOf("/"))+".epub");
	        	
	        	coverStart = page.indexOf("src=\"") +5;
	        	page = page.substring(coverStart);
	        	coverEnd = page.indexOf("\"");
	        	coverBigEnd = page.indexOf("?");
	        	
	        	featuredResult.setCoverUrl(page.substring(0, coverBigEnd));
	        	
	        	final String imgurl = page.substring(0, coverEnd);
	        	
	        	titleStart = page.indexOf("title=\"") +7;
	        	page = page.substring(titleStart);
	        	titleEnd = page.indexOf("\"");
	        	final String title = (page.substring(0, titleEnd)).replace("amp;", "");
	        	featuredResult.setTitle(title);

	        	authorStart = page.indexOf("gray\">") +6;
	        	page = page.substring(authorStart);
	        	authorEnd = page.indexOf("<");
	        	featuredResult.setAuthor(page.substring(0, authorEnd));
	        	
	        	final Drawable cover = LoadImageFromWebOperations(imgurl);
	        	featuredResult.setCover(cover);
	        	
	        	this.publishProgress(featuredResult);
	        	book = page.indexOf("http://schema.org/Book");
	        	
	        	if(this.isCancelled())
		        	return null;
	        }
		}
		catch (MalformedURLException e)
		{
			ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: " + e + "\nOn FeaturedResult");
		}
		catch(IOException ioe)
		{ 
			ERROR = IO_ERROR;
			Log.e(TAG, "IOException: " + ioe + "\nOn FeaturedResult");
		}
		return null;
	}
	
	private Drawable LoadImageFromWebOperations(String url)
	{
		try
		{
			final InputStream is = (InputStream) new URL(url).getContent();
			final Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		}
		catch (Exception e)
		{
			//COVER_ERROR = GENERAL_ERROR;
			Log.e(TAG, "Exception: " + e + "\nOn LoadImageFromWebOperations()");
			return null;
		}
	}
}
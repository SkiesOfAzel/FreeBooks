package com.icsd.threads;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import com.icsd.structs.Book;
import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.BookPageActivity;
import com.icsd.handlers.BookHandler;
import com.icsd.handlers.CategoriesHandler;
import com.icsd.handlers.CommentsHandler;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class PopulateBookThread extends AsyncTask<Void, Short, Void> implements ErrorCodes
{
	private final static String TAG = "POPULATE_BOOK_THREAD";
    private final String url;
    private final WeakReference<BookPageActivity> parent;
    private final WeakReference<Book> book;
    
	private short ERROR;
	private short COVER_ERROR;
	private short CATEGORIES_ERROR;
	private short COMMENTS_ERROR;

    
    public PopulateBookThread(WeakReference<BookPageActivity> parent, String url)
    {
    	ERROR = NO_ERROR;
    	COVER_ERROR = NO_ERROR;
    	CATEGORIES_ERROR = NO_ERROR;
    	COMMENTS_ERROR = NO_ERROR;
    	
		this.url = url;
		this.parent = parent;
		this.book = new WeakReference<Book>(parent.get().getBook());
    }
    
    public Book getBook()
    {
    	return book.get();
    }
    
    @Override
    protected void onPreExecute()
    {

    }
    
    @Override
    protected void onProgressUpdate(Short... values)
    {
    	if(parent != null)
    		parent.get().updateBook(values[0]);
    }
    
    @Override
    protected void onCancelled()
    {
    	Log.w(TAG, "onCancelled()");
    	
    	if(parent != null)
    		parent.get().threadFinished(ERROR);
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
		if(!isCancelled())
		{
			fetchDescription();
			this.publishProgress((short)1);
		}
		else
			return null;
		
		if(!isCancelled())
		{
			fetchCategories();
			this.publishProgress((short)2);
		}
		else
			return null;
		
		if(!isCancelled())
		{
			fetchCover();
			this.publishProgress((short)3);
		}
		else
			return null;
		
		if(!isCancelled())
		{
			fetchComments();
			this.publishProgress((short)4);
		}
		else
			return null;
		
		return null;
	}
	
	private void fetchComments()
	{
		try
		{
			final URL newUrl = new URL(book.get().getCommentsUrl());
		
			//sax stuff
			final SAXParserFactory factory = SAXParserFactory.newInstance(); 
			final SAXParser parser = factory.newSAXParser(); 
			
			final XMLReader reader = parser.getXMLReader(); 
		
			//initialize our parser logic
			final CommentsHandler commentsHandler = new CommentsHandler(book.get().getComments()); 
			reader.setContentHandler(commentsHandler); 

			//get the xml feed
			reader.parse(new InputSource(newUrl.openStream())); 
		}
		catch (MalformedURLException e)
		{
			COMMENTS_ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: " + e + "\nIn fetchComments()");
		}
		catch(ParserConfigurationException pce)
		{ 
			COMMENTS_ERROR = PARSER_CONFIG_ERROR;
			Log.e(TAG, "ParserConfigurationException: " + pce + "\nIn fetchComments()");
		}
		catch(SAXException se)
		{ 
			COMMENTS_ERROR = SAX_ERROR;
			Log.e(TAG, "SAXException: " + se + "\nIn fetchComments()");
		}
		catch(IOException ioe)
		{ 
			COMMENTS_ERROR = IO_ERROR;
			Log.e(TAG, "IOException: " + ioe + "\nIn fetchComments()");
		}
	}
	
	private void fetchCover()
	{
		final Drawable cover;
		
		try
		{
			final InputStream is = (InputStream) new URL(book.get().getCoverUrl()+"?size=thumbnail").getContent();
			cover  = Drawable.createFromStream(is, "src name");
			book.get().setCover(cover);
		}
		catch (MalformedURLException e)
		{
			ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: " + e + "\nIn fetchCover()");
		}
		catch (IOException e)
		{
			ERROR = IO_ERROR;
			Log.e(TAG, "IOException: " + e + "\nIn fetchCover()");
		}
	}
	
	private void fetchCategories()
	{
		try
		{
			//sax stuff
			final SAXParserFactory factory = SAXParserFactory.newInstance(); 
			final SAXParser parser = factory.newSAXParser(); 
		 
			final XMLReader reader = parser.getXMLReader(); 
		 
			//initialize our parser logic
			final URL catUrl = new URL(book.get().getCategoriesUrl());
            final CategoriesHandler categoriesHandler = new CategoriesHandler(this); 
			reader.setContentHandler(categoriesHandler);
			reader.parse(new InputSource(catUrl.openStream())); 
		}
		catch (MalformedURLException e)
		{
			CATEGORIES_ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: " + e + "\nIn fetchCategories()");
		}
		catch(ParserConfigurationException pce)
		{ 
			CATEGORIES_ERROR = PARSER_CONFIG_ERROR;
			Log.e(TAG, "ParserConfigurationException: " + pce + "\nIn fetchCategories()");
		}
		catch(SAXException se)
		{ 
			CATEGORIES_ERROR = SAX_ERROR;
			Log.e(TAG, "SAXException: " + se + "\nIn fetchCategories()");
		}
		catch(IOException ioe)
		{ 
			CATEGORIES_ERROR = IO_ERROR;
			Log.e(TAG, "IOException: " + ioe + "\nIn fetchCategories()");
		}
		
	}
	
	private void fetchDescription()
	{
		final URL newUrl;
		try
		{
			newUrl = new URL(url);
			
			//sax stuff
			final SAXParserFactory factory = SAXParserFactory.newInstance(); 
			final SAXParser parser = factory.newSAXParser(); 
		 
			final XMLReader reader = parser.getXMLReader(); 
		 
			//initialize our parser logic
			final BookHandler bookHandler = new BookHandler(book); 
			reader.setContentHandler(bookHandler); 

			//get the xml feed
			reader.parse(new InputSource(newUrl.openStream())); 
		}
		
		catch (MalformedURLException e)
		{
			ERROR = MALFORMED_URL;
			Log.e(TAG, "MalformedURLException: " + e + "\nIn fetchDescription()");
		}
		catch(ParserConfigurationException pce)
		{ 
			ERROR = PARSER_CONFIG_ERROR;
			Log.e(TAG, "ParserConfigurationException: " + pce + "\nIn fetchDescription()");
		}
		catch(SAXException se)
		{ 
			ERROR = SAX_ERROR;
			Log.e(TAG, "SAXException: " + se + "\nIn fetchDescription()");
		}
		catch(IOException ioe)
		{ 
			ERROR = IO_ERROR;
			Log.e(TAG, "IOException: " + ioe + "\nIn fetchDescription()");
		}
	}
}
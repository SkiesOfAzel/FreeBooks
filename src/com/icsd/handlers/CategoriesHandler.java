package com.icsd.handlers;

import java.lang.ref.WeakReference;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.icsd.structs.Book;
import com.icsd.structs.CategoriesResult;
import com.icsd.threads.PopulateBookThread;
import com.icsd.threads.PopulateCategoriesThread;

public class CategoriesHandler extends DefaultHandler
{
	private final PopulateCategoriesThread categoriesThread;
	private final PopulateBookThread bookThread;
	private final WeakReference<Book> book;
	private boolean inEntry;
	private boolean inTitle;
	private boolean inUri;
	
	private String name;
	private String url;
	
	public CategoriesHandler(PopulateCategoriesThread categoriesThread)
	{
		this.categoriesThread = categoriesThread;
		this.bookThread = null;
		this.book = null;
		name = "";
		url="";
	}
	
	public CategoriesHandler(PopulateBookThread bookThread)
	{
		this.bookThread = bookThread;
		this.categoriesThread = null;
		this.book = new WeakReference<Book>(bookThread.getBook());
		name = "";
		url="";
	}
	
	@Override 
	public void startDocument() throws SAXException 
	{ 

	}
	
	@Override 
	public void endDocument() throws SAXException
	{ 
	 
	}
	
	@Override 
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException
	{
	
		if(localName.equals("entry"))
			inEntry = true;

		else if(localName.equals("title"))
			inTitle = true;

		else if(localName.equals("id"))
			inUri = true;
	}
	
	@Override 
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		//Log.v("endElement", localName);

		if(localName.equals("entry"))
		{
			final CategoriesResult result = new CategoriesResult();
			result.setName(name);
			result.setUrl(url);
			if(bookThread == null)
			{
				categoriesThread.updateProgress(result);
			
				if(categoriesThread.isCancelled())
				{
					Log.e("PARSHING", "terminated");
					throw new SaxTerminationException();
				}
			}
			
			else
			{
				result.setName(name);
				result.setUrl(url);
				book.get().getCategories().add(result);
				
				if(bookThread.isCancelled())
				{
					Log.e("PARSHING", "terminated");
					throw new SaxTerminationException();
				}
			}
			
			name = "";
			url = "";
			
			inEntry = false;
		}
		else if(localName.equals("title"))
			inTitle = false;
		
		else if(localName.equals("id"))
			inUri = false;
	}
	
	@Override 
	public void characters(char ch[], int start, int length)
	{
		String chars = new String(ch, start, length); 
	    chars = chars.trim();
    
		if(inEntry && inTitle)
			name = name + chars;

		if(inEntry && inUri)
			url = url + chars;
	}
}

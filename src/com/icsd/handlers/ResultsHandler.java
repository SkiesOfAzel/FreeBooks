package com.icsd.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.Resources;
import android.util.Log;

import com.icsd.freebooks.R;
import com.icsd.structs.SearchResult;
import com.icsd.threads.PopulateResultsThread;

//import android.util.Log;

public class ResultsHandler extends DefaultHandler
{
	private final Resources res;
	private boolean inEntry;
	private boolean inTitle;
	private boolean inUri;
	private boolean inAuthor;
	private boolean inName;
	private boolean inLanguage;
	private boolean inTotalResults;
	private boolean inItemsPerPage;
	
	private final PopulateResultsThread parent;
	private short totalResults;
	private short itemsPerPage;
	
	private String title;
	private String url;
	private String author;
	private String language;
	
	public ResultsHandler(PopulateResultsThread parent, Resources res)
	{
		this.parent = parent;
		totalResults = 0;
		itemsPerPage = 0;
		title = "";
		author="";
		url = "";
		language = "";
		this.res = res;
	}
	
	public short getTotalResults()
	{
		return totalResults;
	}
	
	public short getItemsPerPage()
	{
		return itemsPerPage;
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
		if(localName.equals("totalResults"))
			inTotalResults = true;
		
		else if(localName.equals("itemsPerPage"))
			inItemsPerPage = true;
		
		else if(localName.equals("entry"))
			inEntry = true;
		
		else if(localName.equals("title"))
			inTitle = true;

		else if(localName.equals("id"))
			inUri = true;
			
		else if(localName.equals("author"))
			inAuthor = true;
		
		else if(localName.equals("name"))
			inName = true;
		
		else  if(localName.equals("language"))
			inLanguage = true;
	}
	
	@Override 
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		//Log.v("endElement", localName);

		if(localName.equals("totalResults"))
			inTotalResults = false;

		else if(localName.equals("itemsPerPage"))
			inItemsPerPage = false;

		else if(localName.equals("entry"))
		{
			inEntry = false;
			final SearchResult result = new SearchResult();
			result.setTitle(title);
			result.setBookUrl(url);
			result.setAuthor(author);
			
			if(language.equals("en"))
				language = res.getString(R.string.english)+" ";
			
			else if (language.equals("fr"))
				language = res.getString(R.string.french)+" ";
			
			else if (language.equals("de"))
				language = res.getString(R.string.german)+" ";
			
			else if (language.equals("es"))
				language = res.getString(R.string.spanish)+" ";
			
			else if (language.equals("ru"))
				language = res.getString(R.string.russian)+" ";
			
			result.setLanguage(language);
			parent.onProgressUpdate(result);
			if(parent.isCancelled())
			{
				Log.e("PARSHING", "terminated");
				throw new SaxTerminationException();
			}
			title = "";
			url = "";
			author= "";
			language = "";
		}
		
		else if(localName.equals("title"))
			inTitle = false;
		
		else if(localName.equals("id"))
			inUri = false;
		
		else if(localName.equals("author"))
			inAuthor = false;
		
		else if(localName.equals("name"))
			inName = false;
		
		else if(localName.equals("language"))
			inLanguage = false;
		
	}
	
	@Override 
	public void characters(char ch[], int start, int length)
	{
		String chars = new String(ch, start, length); 
	    chars = chars.trim();

	    //Log.v("Sax_Handler", "in characters");
	    
		if(inEntry && inTitle)
			title = title + chars;
		
		if(inEntry && inUri)
			url = chars;
		
		if(inEntry && inAuthor && inName)
			author = author + chars;


		
		if(inEntry && inLanguage)
			language = chars;

		
		if(inTotalResults)
			totalResults = Short.valueOf(chars);

		if(inItemsPerPage)
			itemsPerPage = Short.valueOf(chars);
	}
}

package com.icsd.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.icsd.structs.SubcategoriesResult;
import com.icsd.threads.PopulateSubcategoriesThread;

public class SubcategoriesHandler extends DefaultHandler
{
	private final PopulateSubcategoriesThread parent;
	private boolean inEntry;
	private boolean inTitle;
	private boolean inUri;
	private boolean inContent;
	private boolean inItemsPerPage;
	private boolean inTotalResults;
	
	private String name;
	private String url;
	private String items;
	
	private short totalResults;
	private short itemsPerPage;
	
	public SubcategoriesHandler(PopulateSubcategoriesThread parent)
	{
		this.parent = parent;
		name = "";
		url="";
	}
	
	public short getItemsPerPage()
	{
		return itemsPerPage;
	}
	
	public short getTotalResults()
	{
		return totalResults;
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
		
		else if(localName.equals("content"))
			inContent = true;
		
		else if(localName.equals("totalResults"))
			inTotalResults = true;
		
		else if(localName.equals("itemsPerPage"))
			inItemsPerPage = true;
	}
	
	@Override 
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		//Log.v("endElement", localName);

		if(localName.equals("entry"))
		{
			final SubcategoriesResult result = new SubcategoriesResult();
			result.setName(name.replace("&", " & "));
			result.setUrl(url);
			result.setItems(items);
			parent.onProgressUpdate(result);
			
			if(parent.isCancelled())
			{
				Log.e("PARSHING", "terminated");
				throw new SaxTerminationException();
			}
			
			name = "";
			url = "";
			items="";
			
			inEntry = false;
		}
		
		else if(localName.equals("title"))
			inTitle = false;
		
		else if(localName.equals("id"))
			inUri = false;
		
		else if(localName.equals("content"))
			inContent = false;
		
		else if(localName.equals("totalResults"))
			inTotalResults = false;
		
		else if(localName.equals("itemsPerPage"))
			inItemsPerPage = false;
		
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
		
		if(inEntry && inContent)
			items = chars+" ";
		
		if(inTotalResults)
			totalResults = Short.valueOf(chars);
		
		if(inItemsPerPage)
			itemsPerPage = Short.valueOf(chars);
	}
}

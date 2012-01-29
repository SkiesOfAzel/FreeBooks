package com.icsd.handlers;

import java.lang.ref.WeakReference;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.icsd.structs.Book;

//import com.icsd.threads.PopulateBookThread;
//import android.util.Log;

public class BookHandler extends DefaultHandler
{
	private boolean inTitle;
	private boolean inId;
	private boolean inAuthor;
	private boolean inName;
	private boolean inUri;
	private boolean inLanguage;
	private boolean inIssued;
	private boolean inExtent;
	private boolean inContent;
	
	private String title;
	private String author;
	private String content;
	
	private final WeakReference<Book> book;
	//final private PopulateBookThread parent;
	
	public BookHandler(WeakReference<Book> book)
	{
		//this.parent = parent;
		this.book = book;
		title = "";
		author = "";
		content = "";
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
		
		if(localName.equals("title"))
			inTitle = true;
		
		else if(localName.equals("id"))
			inId = true;
		
		else if(localName.equals("author"))
			inAuthor = true;
		
		else if(localName.equals("name"))
			inName = true;

		else if(localName.equals("uri"))
			inUri = true;

		else if(localName.equals("language"))
			inLanguage = true;
			
		else if(localName.equals("issued"))
			inIssued = true;
		
		else if(localName.equals("extent"))
			inExtent = true;
		
		else  if(localName.equals("content"))
			inContent = true;
	}
	
	@Override 
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if(localName.equals("title"))
			inTitle = false;
		
		else if(localName.equals("id"))
			inId = false;
		
		else if(localName.equals("author"))
			inAuthor = false;
		
		else if(localName.equals("name"))
			inName = false;

		else if(localName.equals("uri"))
			inUri = false;

		else if(localName.equals("language"))
			inLanguage = false;
			
		else if(localName.equals("issued"))
			inIssued = false;
		
		else if(localName.equals("extent"))
			inExtent = false;
		
		else  if(localName.equals("content"))
			inContent = false;
		
		else  if(localName.equals("entry"))
		{
			book.get().setTitle(title);
			book.get().setAuthor(author);
			book.get().setContent(content);
		}
			

	}
	
	@Override 
	public void characters(char ch[], int start, int length)
	{
		String chars = new String(ch, start, length); 
	    chars = chars.trim();

	    //Log.e("Sax_Handler", "in characters");
	    
		if(inTitle)
			title = title + chars;
		
		if(inId)
		{
			book.get().setBookId(chars.substring(24));
			book.get().setBookUrl(chars+".atom");
			book.get().setEpubUrl(chars+".epub");
			book.get().setCommentsUrl(chars+"/comments.atom");
			book.get().setCategoriesUrl(chars+"/categories.atom");
			book.get().setCoverUrl("http://covers.feedbooks.net"+chars.substring(24)+".jpg");
		}
		
		if(inAuthor && inName)
			author = author + chars;

		if(inAuthor && inUri)
			book.get().setAuthorUrl(chars+"/books/top.atom");
		
		if(inLanguage)
			book.get().setLanguage(chars);

		if(inIssued)
			book.get().setIssued(chars);
		
		if(inExtent)
		{
			book.get().setExtent(chars);
			//Log.e("IN_EXTEND", chars);
		}
		if(inContent)
			content = content + chars;
	}
}

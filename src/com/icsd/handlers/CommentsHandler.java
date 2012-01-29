package com.icsd.handlers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.icsd.structs.Comment;

public class CommentsHandler extends DefaultHandler
{
	private final ArrayList<Comment> comments;
	
	private boolean inEntry;
	private boolean inAuthor;
	private boolean inName;
	private boolean inContent;
	private boolean inPublished;
	private short counter;
	private String text;
	
	public CommentsHandler(ArrayList<Comment> comments)
	{
		this.comments = comments;
		counter = 0;
		text = "";
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
		{
			inEntry = true;
			final Comment comment = new Comment();
			comments.add(comment);
		}
		
		else if(localName.equals("author"))
			inAuthor = true;
		
		else if(localName.equals("name"))
			inName = true;
		
		else if(localName.equals("content"))
			inContent = true;
		
		else if(localName.equals("published"))
			inPublished = true;
	}
	
	@Override 
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if(localName.equals("entry"))
		{
			inEntry = false;
			++counter;
			text = "";
		}
		
		else if(localName.equals("author"))
			inAuthor = false;
		
		else if(localName.equals("name"))
			inName = false;
		
		else if(localName.equals("content"))
			inContent = false;

		else if(localName.equals("published"))
			inPublished = false;
	}
	
	@Override 
	public void characters(char ch[], int start, int length)
	{
		String chars = new String(ch, start, length); 
		
	    chars = chars.trim();
	    
		if(inEntry && inAuthor && inName)
			comments.get(counter).setAuthor(chars);

		if(inEntry && inPublished)
			comments.get(counter).setDate(chars);
		
		if(inEntry && inContent)
		{
			text = text + chars;
			comments.get(counter).setComment(text);
		}
	}
}


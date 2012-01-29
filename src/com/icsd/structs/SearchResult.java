package com.icsd.structs;

public class SearchResult 
{
	
	private String title;
	private String author;
	private String bookUrl;
	private String language;
	
	public SearchResult()
	{
		title = null;
		author = null;
		bookUrl = null;
		language = null;
	}
    
	public String getTitle()
	{
		return title;
	}
        
	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAuthor()
	{
		return author;
	}
        
	public void setAuthor(String author)
	{
		this.author = author;
	}
        
	public String getBookUrl()
	{
		return bookUrl;
	}
        
	public void setBookUrl(String bookUrl)
	{
		this.bookUrl = bookUrl;
	}
        
        
	public String getLanguage()
	{
		return language;
	}
	
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	public void clear()
	{
		title = null;
		author = null;
		bookUrl = null;
		language = null;
	}
}

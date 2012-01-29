package com.icsd.structs;

import android.graphics.drawable.Drawable;


public class FeaturedResult
{
	private String title;
	private String author;
	private String bookUrl;
	private String epubUrl;
	private String coverUrl;
	private Drawable cover;
	
	public FeaturedResult()
	{
		clear();
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
	
	public String getEpubUrl()
	{
		return epubUrl;
	}
        
	public void setEpubUrl(String epubUrl)
	{
		this.epubUrl = epubUrl;
	}
	
	public String getCoverUrl()
	{
		return coverUrl;
	}
	
	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
	}
	
	public Drawable getCover()
	{
		return cover;
	}
	
	public void setCover(Drawable cover)
	{
		this.cover = cover;
	}
	
	public void clear()
	{
		title = null;
		author = null;
		bookUrl = null;
		epubUrl = null;
		coverUrl = null;
		cover = null;
	}
}

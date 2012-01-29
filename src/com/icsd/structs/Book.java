package com.icsd.structs;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;


public class Book extends SearchResult
{
	private String authorUrl;
	private String issued;
	private String extent;
	private String epubUrl;
	private String coverUrl;
	private Drawable cover;
	private String commentsUrl;
	private String categoriesUrl;
	private String content;
	private String id;
	private ArrayList<Comment> comments;
	private ArrayList<CategoriesResult> categories;
	
	public Book()
	{
		super();
		comments = new ArrayList<Comment>();
		categories = new ArrayList<CategoriesResult>();
		
		authorUrl = null;
		issued = null;
		extent = null;
		epubUrl = null;
		coverUrl = null;
		commentsUrl = null;
		categoriesUrl = null;
		content = null;
		id = null;
		cover = null;
	}
	
	public String getAuthorUrl()
	{
		return authorUrl;
	}
        
	public void setAuthorUrl(String authorUrl)
	{
		this.authorUrl = authorUrl;
	}
	
	public String getIssued()
	{
		return issued;
	}
        
	public void setIssued(String issued)
	{
		this.issued = issued;
	}
	
	public String getExtent()
	{
		return extent;
	}
        
	public void setExtent(String extent)
	{
		this.extent = extent;
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
	
	public String getCommentsUrl()
	{
		return commentsUrl;
	}
        
	public void setCommentsUrl(String commentsUrl)
	{
		this.commentsUrl = commentsUrl;
	}
	
	public String getCategoriesUrl()
	{
		return categoriesUrl;
	}

	public void setCategoriesUrl(String categoriesUrl)
	{
		this.categoriesUrl = categoriesUrl;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public void setContent(String content)
	{
		this.content = content;
	}
	
	public ArrayList<Comment> getComments()
	{
		return comments;
	}
	
	public void setComments(ArrayList<Comment> comments)
	{
		this.comments = comments;
	}
	
	public ArrayList<CategoriesResult> getCategories()
	{
		return categories;
	}
	
	public void setCategories(ArrayList<CategoriesResult> categories)
	{
		this.categories = categories;
	}
	public void setBookId(String id)
	{
		this.id = id;
	}
	
	public String getBookId()
	{
		return id;
	}
	
	public Drawable getCover()
	{
		return cover;
	}
	
	public void setCover(Drawable cover)
	{
		this.cover = cover;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		authorUrl = null;
		issued = null;
		extent = null;
		epubUrl = null;
		coverUrl = null;
		commentsUrl = null;
		categoriesUrl = null;
		id = null;
		content = null;
		categories.clear();
		comments.clear();
	}
}

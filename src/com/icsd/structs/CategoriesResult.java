package com.icsd.structs;

public class CategoriesResult 
{
	
	private String name;
	private String url;
	
	public CategoriesResult()
	{
		clear();
	}
    
	public String getName()
	{
		return name;
	}
        
	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}
        
	public void setUrl(String url)
	{
		this.url = url;
	}
        
	public void clear()
	{
		name = null;
		url = null;
	}
}

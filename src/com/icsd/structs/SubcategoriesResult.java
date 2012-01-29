package com.icsd.structs;

public class SubcategoriesResult extends CategoriesResult
{
	private String items;
	
	public SubcategoriesResult()
	{
		clear();
	}
    
	public String getItems()
	{
		return items;
	}
        
	public void setItems(String items)
	{
		this.items = items;
	}

	public void clear()
	{
		items = null;
		setName(null);
		setUrl(null);
	}
}

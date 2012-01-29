package com.icsd.structs;

public class DownloadBookInfo
{
	private String title;
	private String author;
	private String fileUrl;
	private String coverUrl;
	
	public DownloadBookInfo()
	{
		title = null;
		author = null;
		fileUrl = null;
		coverUrl = null;
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
        
	public String getFileUrl()
	{
		return fileUrl;
	}
        
	public void setFileUrl(String fileUrl)
	{
		this.fileUrl = fileUrl;
	}
        
        
	public String getCoverUrl()
	{
		return coverUrl;
	}
	
	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
	}
	
	public void clear()
	{
		title = null;
		author = null;
		fileUrl = null;
		coverUrl = null;
	}
}

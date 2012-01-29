package com.icsd.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.util.Log;

public class FileUtil
{
	private static final String TAG = "FILE_UTILITY";
	
	public static boolean clear(String filePath)
	{
		final String bookDir = filePath.substring(0, filePath.lastIndexOf("/"));
		final File fileDir = new File(bookDir);

		Log.i(TAG, "fileDir: " + fileDir);
		
		if(fileDir.isDirectory())
		{
			try
			{
				FileUtils.deleteDirectory(fileDir);
			}
			catch(IOException ioe)
			{
				Log.e(TAG, "IOException: " + ioe + "\nOn File: " + fileDir);
				return false;
			}
		}
		else
			return false;
		
		final File authorDir = new File(bookDir.substring(0, bookDir.lastIndexOf("/")));
		
		if(authorDir.isDirectory() && (authorDir.list().length == 0))
		{
			try
			{
				FileUtils.deleteDirectory(authorDir);
			}
			catch(IOException ioe)
			{
				Log.e(TAG, "IOException: " + ioe + "\nOn File: " + authorDir);
				return false;
			}
		}
		else
			return false;
		
		return true;
	}
}

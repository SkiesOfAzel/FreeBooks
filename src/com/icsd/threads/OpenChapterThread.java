package com.icsd.threads;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import com.icsd.freebooks.EpubReaderActivity;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;
import android.os.AsyncTask;
import android.util.Log;

public class OpenChapterThread extends AsyncTask<Void, Integer, Void>
{
	private final static String HEADER = "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
			"<head>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/monocle.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/compat/env.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/compat/css.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/compat/stubs.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/compat/browser.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/events.js\"></script>\n" + 
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/factory.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/styles.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/reader.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/book.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/component.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/core/place.js\"></script>\n\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/controls/panel.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/panels/twopane.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/panels/eink.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/dimensions/columns.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/flippers/slider.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/flippers/instant.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/dimensions/vert.js\"></script>\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/flippers/legacy.js\"></script>\n\n" +
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/monocle/styles/monocore.css\" />\n" +
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/monocle/styles/test.css\" />\n" +
			"<script type=\"text/javascript\" src=\"file:///android_asset/monocle/src/init.js\"></script>\n</head>";
	
	private final static String FOOTER = "</div>\n</body>\n</html>";
	private final static String TAG = "OPEN_CHAPTER_THREAD";
	
	private final WeakReference<EpubReaderActivity> parent;
	private final String epubPath;
	private final int currentChapter;
	
	private String bookContent;
	private int maxChapter;
	
	
	public OpenChapterThread(WeakReference<EpubReaderActivity> parent, String epubPath, int currentChapter)
	{
		this.parent = parent;
		this.epubPath = epubPath;
		this.currentChapter = currentChapter;
	}
	
	@Override
    protected void onPreExecute()
    {
		parent.get().setIsOpenChapterRunning(true);
		Log.i(TAG, "onPreExecute()");
    }

	@Override
    protected void onPostExecute(Void params)
    {
		Log.i(TAG, "onPostExecute()");
		if(parent != null)
		{
			parent.get().setPageContent(bookContent, maxChapter);
			parent.get().setIsOpenChapterRunning(false);
		}
    }
	
	@Override
	protected Void doInBackground(Void... arg0)
	{
		try
		{
			if(parent == null)
				return null;
			else
			{
				bookContent = parent.get().getBookContent();
				
				final Book book = (new EpubReader()).readEpub(new FileInputStream(epubPath));
				final Spine spine = book.getSpine();
				maxChapter = spine.size() - 1;
				Log.i(TAG, "maxChapter: "+maxChapter);
				bookContent = getBookData(spine);
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String getBookData(Spine spine)
	{
		Log.i(TAG, "getbookData()");
		
		final String bookData;
		
		bookData = (new String (spine.getSpineReferences().get(currentChapter).getResource().getData()));
		
		final String data = HEADER + "<body>\n<div id=\"reader\">\n" + bookData + FOOTER;
		return data;
	}
}

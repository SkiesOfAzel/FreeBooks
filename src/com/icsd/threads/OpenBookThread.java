package com.icsd.threads;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.FreeBooksActivity;
import com.icsd.freebooks.R;
import com.icsd.sql.DataBaseHelper;
import com.icsd.utils.ZipUtil;

public class OpenBookThread  extends AsyncTask<Void, Void, Void> implements ErrorCodes
{
	private final static String TAG = "OpenBookThread";
	private final static String APP_FOLDER = "/.fBooks/";
	
	private final WeakReference<FreeBooksActivity> parent;
	private final DataBaseHelper databaseHelper;
	private final ProgressDialog dialog;
	
	private final String author;
	private final String title;
	private final String baseUrl;
	private final String epubPath;
	
	private final int rowId;
	
	private short ERROR;
	
	private final boolean isBookOpen;
	
	public OpenBookThread(WeakReference<FreeBooksActivity> parent, int rowId)
	{
		this.parent = parent;
		this.dialog = new ProgressDialog(parent.get());
		this.databaseHelper = DataBaseHelper.getInstance(parent.get());
		this.rowId = rowId;
		this.author = databaseHelper.getBookAuthor(rowId);
		this.title = databaseHelper.getBookTitle(rowId);
		this.baseUrl = Environment.getExternalStorageDirectory()+APP_FOLDER+author+"/"+title+"/.book/";
		this.epubPath = databaseHelper.getBookPath(rowId);
		this.isBookOpen = databaseHelper.isBookOpen(rowId);		
		ERROR = NO_ERROR;
	}
	
	@Override
    protected void onPreExecute()
    {
		Log.i(TAG, "onPreExecute()");
		dialog.setCancelable(false);
		dialog.setMessage(parent.get().getResources().getString(R.string.open_book));
		dialog.show();
    }
	
	@Override
    protected void onPostExecute(Void params)
    {
		Log.i(TAG, "onPostExecute()");
		dialog.dismiss();
		parent.get().goToReaderOrPostError(ERROR, rowId, author, title, baseUrl, epubPath);
    }
	
	@Override
	protected Void doInBackground(Void... params)
	{
		final String bookPath = databaseHelper.getBookPath(rowId);
		final File bookFile = new File(bookPath);
		
		if(!bookFile.exists())
		{
			ERROR = BOOK_DOESNT_EXIST;
			databaseHelper.removeBook(rowId, true);
			return null;
		}
		
		if(!isBookOpen)
		{
			try
			{
				ZipUtil.unzipAll(new File(epubPath), new File(baseUrl));
			}
			catch (IOException e)
			{
				Log.i(TAG, "IOEXception: " + e + "\nIn openBook()");
				ERROR = IO_ERROR;
				return null;
			}
			databaseHelper.setIsBookOpen(rowId);
		}
		
		return null;
	}

}

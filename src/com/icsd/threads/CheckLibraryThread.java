package com.icsd.threads;

import java.io.File;
import java.lang.ref.WeakReference;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.FreeBooksActivity;
import com.icsd.sql.DataBaseHelper;

public class CheckLibraryThread extends AsyncTask<Void, Void, Void> implements ErrorCodes
{
	private final static String TAG = "CheckLibraryThread";
	private final WeakReference<FreeBooksActivity> parent;

	
	public CheckLibraryThread(WeakReference<FreeBooksActivity> parent)
	{
		this.parent = parent;
	}

	@Override
    protected void onPreExecute()
    {
		Log.i(TAG, "onPreExecute()");
    }
	
	@Override
    protected void onPostExecute(Void params)
    {
		Log.i(TAG, "onPostExecute()");
		parent.get().populateLibrary();
    }
	
	@Override
	protected Void doInBackground(Void... params)
	{
		Log.i(TAG, "doInBackground()");
		final DataBaseHelper databaseHelper = DataBaseHelper.getInstance(parent.get());
		final Cursor books = databaseHelper.fetchAllBooks();

		books.moveToFirst();
		while((!books.isAfterLast()) && (!isCancelled()))
		{
			final String bookPath = books.getString(books.getColumnIndexOrThrow("path"));
			final File book = new File(bookPath);
			if(!book.exists())
			{
				final int bookId = books.getInt(books.getColumnIndexOrThrow("_id"));
				databaseHelper.removeBook(bookId, true);
			}
			books.moveToNext();
		}
		books.close();
		return null;
	}

}

package com.icsd.threads;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.icsd.constants.ErrorCodes;
import com.icsd.freebooks.BookPageActivity;
import com.icsd.freebooks.FreeBooksActivity;
import com.icsd.freebooks.R;
import com.icsd.sql.DataBaseHelper;
import com.icsd.structs.Book;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadBookThread  extends AsyncTask<Void, Integer, Void> implements ErrorCodes
{
	// constants
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    private static final String BOOKS_FOLDER = "/eBooks/";
    private static final String APP_FOLDER = "/.fBooks/";
    private static final String TEMP_FOLDER = "/.fBooks/.temp/";
    private static final String COVER = ".cover.jpg";
    private static final String TAG = "DOWNLOAD_BOOK_THREAD";
    
    //variables
    private final WeakReference<BookPageActivity> bookActivity;
    private final WeakReference<FreeBooksActivity> mainActivity;
	private final String url;
	private final String coverUrl;
	private final String fileName;
	private final String title;
	private final String author;
	private final String language;
	private final ProgressDialog dialog;
	private final String tempPath;
	private final String fileStorePath;
	private final String coverStorePath;
	private final boolean isInLibrary;
	
	private boolean IS_EPUB;
	private short COVER_ERROR;
	private short ERROR;
	
	public DownloadBookThread(WeakReference<BookPageActivity> bookActivity, WeakReference<Book> book, boolean isInLibrary)
	{
		this.bookActivity = bookActivity;
		this.mainActivity = null;
		this.isInLibrary = isInLibrary;
		this.url = book.get().getEpubUrl();
		this.coverUrl = book.get().getCoverUrl();
		this.fileName = book.get().getTitle()+".epub";
		this.title = book.get().getTitle();
		this.author = book.get().getAuthor();
		this.language = book.get().getLanguage();
		
		if(url == null)
			ERROR = URL_IS_NULL;
		else
			ERROR = NO_ERROR;
		
		if(coverUrl == null)
			COVER_ERROR = URL_IS_NULL;
		else
			COVER_ERROR = NO_ERROR;
		
		this.tempPath = Environment.getExternalStorageDirectory()+TEMP_FOLDER;
		
		this.fileStorePath = Environment.getExternalStorageDirectory()+BOOKS_FOLDER+author+"/"+title+"/";
		this.coverStorePath = Environment.getExternalStorageDirectory()+APP_FOLDER+author+"/"+title+"/";
		
		this.IS_EPUB = true;
		this.dialog = new ProgressDialog(bookActivity.get());
	}
	
	public DownloadBookThread(WeakReference<FreeBooksActivity> mainActivity, WeakReference<Book> book, boolean isInLibrary, boolean i)
	{
		this.bookActivity = null;
		this.mainActivity = mainActivity;
		this.isInLibrary = isInLibrary;
		this.url = book.get().getEpubUrl();
		this.coverUrl = book.get().getCoverUrl();
		this.fileName = book.get().getTitle()+".epub";
		this.title = book.get().getTitle();
		this.author = book.get().getAuthor();
		this.language = book.get().getLanguage();
		
		if(url == null)
			ERROR = URL_IS_NULL;
		else
			ERROR = NO_ERROR;
		
		if(coverUrl == null)
			COVER_ERROR = URL_IS_NULL;
		else
			COVER_ERROR = NO_ERROR;
		
		this.tempPath = Environment.getExternalStorageDirectory()+TEMP_FOLDER;
		
		this.fileStorePath = Environment.getExternalStorageDirectory()+BOOKS_FOLDER+author+"/"+title+"/";
		this.coverStorePath = Environment.getExternalStorageDirectory()+APP_FOLDER+author+"/"+title+"/";
		
		this.IS_EPUB = true;
		this.dialog = new ProgressDialog(mainActivity.get());
	}
	
	@Override
    protected void onCancelled()
    {
    	Log.i(TAG, "onCancelled()");
    	cleanUp();
    	ERROR = THREAD_CANCELLED;
    	postError();
    }
	
	@Override
    protected void onPreExecute()
    {
		if(bookActivity != null)
			dialog.setMessage(bookActivity.get().getResources().getString(R.string.fetching));
		else if (mainActivity != null)
			dialog.setMessage(mainActivity.get().getResources().getString(R.string.fetching));
		
    	dialog.setCancelable(true);
    	dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	
    	dialog.setOnCancelListener(new OnCancelListener()
    	{

            public void onCancel(DialogInterface arg0)
            {
                Log.w(TAG, "onCancel(): Cancel Download");
                DownloadBookThread.this.cancel(true);
            }
        });
    	dialog.show();
    }
	
	@Override
    protected void onPostExecute(Void params)
    {
		dialog.setProgress(100);
		
		if(ERROR == NO_ERROR)
		{
			if(moveToFolder())
			{
				if(!isInLibrary)
				{
					final String[] bookInfo;
					
					if(COVER_ERROR == NO_ERROR)
						bookInfo = new String[]{title, author, language, fileStorePath+fileName, coverStorePath+COVER};
					else
						bookInfo = new String[]{title, author, language, fileStorePath+fileName, null};
				
					if(bookActivity != null)
					{
						final DataBaseHelper databaseHelper = DataBaseHelper.getInstance(bookActivity.get());
						databaseHelper.addBook(bookInfo);
						postSuccess();
					}
					else if(mainActivity != null)
					{
						final DataBaseHelper databaseHelper = DataBaseHelper.getInstance(mainActivity.get());
						databaseHelper.addBook(bookInfo);
						mainActivity.get().updateLibrary();
						postSuccess();
					}
				}
			}
			else
			{
				Log.w(TAG, "onPostExecute(): Failed to overwrite");
				ERROR = OVERWRITE_FAILED;
			}
		}
		else
		{
			postError();
		}
		dialog.dismiss();
    }
	
	@Override
    public void onProgressUpdate(Integer... progress)
	{
		dialog.setProgress(progress[0]);
    }

	@Override
	protected Void doInBackground(Void... arg0)
	{
		//final String fileStorePath = "/"+preferences.getString("editTextPref", "feedBooks").trim()+"/"+tile[0]+"/";
		
		if(ERROR == NO_ERROR)
		{
			downloadFile(url, fileName, tempPath);
			
			IS_EPUB = false;
			
			if(ERROR == NO_ERROR)
				downloadFile(coverUrl, COVER, tempPath);

		}
		return null;
	}
	
	private void downloadFile(String url, String fileName, String storePath)
	{
		try
        {
			final URLConnection connection;
            final int fileSize;
            final BufferedInputStream inputStream;
            final BufferedOutputStream outputStream;
            final File file;
            final FileOutputStream fileStream;
        	final URL fileUrl = new URL(url);
        	
        	connection = fileUrl.openConnection();
        	connection.setUseCaches(false);
        	Log.i(TAG, "url: "+url);
        	//ensure the folder exists
            final File storeDir = new File(storePath);
            
            if(!storeDir.exists())
            	if(!storeDir.mkdirs())
            	{
            		ERROR = CREATE_FOLDER_FAILED;
            		return;
            	}
            
        	fileSize = connection.getContentLength();
        	
        	if(isCancelled())
        		return;
        	
        	// start download
        	inputStream = new BufferedInputStream(connection.getInputStream());
            file = new File(storePath, fileName);
            fileStream = new FileOutputStream(file);
            outputStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
            byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
            
            int bytesRead = 0, totalRead = 0;
            
            while(!isCancelled() && (bytesRead = inputStream.read(data, 0, data.length)) >= 0)
            {
            	outputStream.write(data, 0, bytesRead);
                    
            	// update progress bar
            	totalRead += bytesRead;
            	if(IS_EPUB)
            		publishProgress((totalRead * 90 / fileSize));
            }
            
            if(!IS_EPUB)
        		publishProgress(95);
            
            outputStream.close();
            fileStream.close();
            inputStream.close();
            
            if(fileSize != -1)
            	if(totalRead != fileSize)
            		ERROR = SIZE_MISSMATCH;

        }
        catch(MalformedURLException e)
        {
        	if(IS_EPUB)
        	{
        		ERROR = MALFORMED_URL;
        		Log.e(TAG, "MalformedURLException: " + e + "\nOn Epub");
        	}
        	else
        	{
        		Log.e(TAG, "MalformedURLException: " + e + "\nOn Cover");
        		COVER_ERROR = MALFORMED_URL;
        	}
        }
        catch(FileNotFoundException e)
        {
        	if(IS_EPUB)
        	{
        		Log.e(TAG, "FileNotFoundException: " + e + "\nOn Epub");
        		ERROR = FILE_NOT_FOUND;
        	}
        	else
        	{
        		Log.e(TAG, "FileNotFoundException: " + e + "\nOn Cover");
        		COVER_ERROR = FILE_NOT_FOUND;
        	}
        }
        catch(Exception e)
        {
        	if(IS_EPUB)
        	{
        		Log.e(TAG, "Exception: " + e + "\nOn Epub");
        		ERROR = GENERAL_ERROR;
        	}
        	else
        	{
        		Log.e(TAG, "Exception: " + e + "\nOn Cover");
        		COVER_ERROR = GENERAL_ERROR;
        	}
        }
	}
	
	private boolean moveToFolder()
	{
		boolean isSuccess = true;
		
		final File oldEpub = new File(tempPath, fileName);
		final File epubPath = new File(fileStorePath);
		final File newEpub = new File(fileStorePath+fileName);
		
		if(!epubPath.exists())
			isSuccess = epubPath.mkdirs();
		
		if(isSuccess)
			isSuccess = oldEpub.renameTo(newEpub);
		else
			return false;
		
		final File oldCover = new File(tempPath, COVER);
		final File coverPath = new File(coverStorePath);
		final File newCover = new File(coverStorePath+COVER);
		
		if(COVER_ERROR != FILE_NOT_FOUND)
		{
			if(!coverPath.exists())
				isSuccess = coverPath.mkdirs();

			if(isSuccess)			
				isSuccess = oldCover.renameTo(newCover);
		}
		return true;
	}
	
	private void cleanUp()
	{
		final File epub = new File(tempPath, fileName);
		if(epub.exists())
			epub.delete();
		
		final File cover = new File(tempPath, COVER);
		if(cover.exists())
			cover.delete();
	}
	
	private void postSuccess()
	{
		if(bookActivity != null)
		{
			final View view = LayoutInflater.from(bookActivity.get()).inflate(R.xml.book_notification, null);
			((TextView) view.findViewById(R.id.message)).setText(bookActivity.get().getResources().getString(R.string.download_success));
			
			final Drawable cover;
			final File coverFile = new File(coverStorePath+COVER);
			
			if(coverFile.exists())
				cover = Drawable.createFromPath(coverStorePath+COVER);
			else
				cover = bookActivity.get().getResources().getDrawable(R.drawable.book_cover);
			
			((ImageView) view.findViewById(R.id.cover)).setImageDrawable(cover);

			Toast toast = new Toast(bookActivity.get());
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(view);
			toast.setGravity(Gravity.CENTER, 0, 0);

			toast.show();
		}
		else if (mainActivity != null)
		{
			final View view = LayoutInflater.from(mainActivity.get()).inflate(R.xml.book_notification, null);
			((TextView) view.findViewById(R.id.message)).setText(mainActivity.get().getResources().getString(R.string.download_success));
			
			final Drawable cover;
			final File coverFile = new File(coverStorePath+COVER);
			
			if(coverFile.exists())
				cover = Drawable.createFromPath(coverStorePath+COVER);
			else
				cover = mainActivity.get().getResources().getDrawable(R.drawable.book_cover);
			
			((ImageView) view.findViewById(R.id.cover)).setImageDrawable(cover);

			Toast toast = new Toast(mainActivity.get());
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(view);
			toast.setGravity(Gravity.CENTER, 0, 0);

			toast.show();
		}
    }
	
	private void postError()
	{
		final WeakReference<Context> context;
		
		if(bookActivity == null)
			context = new WeakReference<Context>(mainActivity.get());
		else
			context = new WeakReference<Context>(bookActivity.get());
		
		final String message;
		
		switch(ERROR)
		{
		case MALFORMED_URL:
			message = context.get().getResources().getString(R.string.malformed_url_exception);
			break;
		case FILE_NOT_FOUND:
			message = context.get().getResources().getString(R.string.file_not_found_exception);
			break;
		case SIZE_MISSMATCH:
			message = context.get().getResources().getString(R.string.size_missmatch);
			break;
		case THREAD_CANCELLED:
			message = context.get().getResources().getString(R.string.download_cancelled);
			break;
		case OVERWRITE_FAILED:
			message = context.get().getResources().getString(R.string.write_failed);
			break;
		default:
			message = context.get().getResources().getString(R.string.error);
			break;
		}
		
		final Toast msg = Toast.makeText(context.get(), message, Toast.LENGTH_LONG);
		msg.show();
	}
}

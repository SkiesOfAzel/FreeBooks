package com.icsd.sql;

import java.io.File;

import com.icsd.utils.FileUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper
{
	public static final String KEY_TITLE = "title";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PATH = "path";
    public static final String KEY_COVER_PATH = "cover_path";
    public static final String KEY_IS_OPEN = "is_open";
    public static final String KEY_CURRENT_CHAPTER = "chapter";
    public static final String KEY_CURRENT_PERCENTAGE = "percentage";
    public static final String KEY_ROWID = "_id";
	private static final String DATABASE_NAME = "freebooks";
	private static final String DATABASE_TABLE = "books";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "create table books (_id integer primary key autoincrement, "
            + "title text not null, author text not null, language text not null, "
            + "path text not null, cover_path text null, is_open integer null, "
            + "chapter integer not null, percentage float not null);";
    
	private static DataBaseHelper mInstance;
	
	private final Context context;
	private SQLiteDatabase mDb;

	private boolean isAddBook;
	
	public boolean isAddBook()
	{
		final boolean addBook = isAddBook;
		isAddBook = false;
		return addBook;
	}
	
	public static DataBaseHelper getInstance(Context context)
	{
        if(mInstance == null)
            mInstance = new DataBaseHelper(context.getApplicationContext());
        
        return mInstance;
	}
	
	private DataBaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		isAddBook = false;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(DATABASE_CREATE);
		Log.w("DATABASE_HELPER", "create database");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w("DATABASE_HELPER", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
	}
	
	public synchronized DataBaseHelper open() throws SQLException
	{
        mDb = getInstance(context).getWritableDatabase();
        return this;
    }
	
	public synchronized boolean isBookOpen(long rowId)
	{
		final String WHERE = KEY_ROWID + " = ?";
		final Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_IS_OPEN}, WHERE, new String[]{String.valueOf(rowId)}, null, null, null);
		cursor.moveToFirst();
		final int isOpen = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_IS_OPEN));
		cursor.close();
		
		if(isOpen == 1)
			return true;
		else
			return false;
	}
	
	public synchronized int getCurrentChapter(long rowId)
	{
		final String WHERE = KEY_ROWID + " = ?";
		final Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_CURRENT_CHAPTER}, WHERE, new String[]{String.valueOf(rowId)}, null, null, null);
		cursor.moveToFirst();
		final int currentChapter = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_CURRENT_CHAPTER));
		cursor.close();
		
		return currentChapter;
	}
	
	public synchronized float getCurrentPercentage(long rowId)
	{
		final String WHERE = KEY_ROWID + " = ?";
		final Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_CURRENT_PERCENTAGE}, WHERE, new String[]{String.valueOf(rowId)}, null, null, null);
		cursor.moveToFirst();
		final float currentPercentage = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_CURRENT_PERCENTAGE));
		cursor.close();
		
		return currentPercentage;
	}
	
	public synchronized String getBookTitle(long rowId)
	{
		final String WHERE = KEY_ROWID + " = ?";
		final Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_TITLE}, WHERE, new String[]{String.valueOf(rowId)}, null, null, null);
		cursor.moveToFirst();
		final String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
		cursor.close();
		return title;
	}
	
	public synchronized String getBookAuthor(long rowId)
	{
		final String WHERE = KEY_ROWID + " = ?";
		final Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_AUTHOR}, WHERE, new String[]{String.valueOf(rowId)}, null, null, null);
		cursor.moveToFirst();
		final String author = cursor.getString(cursor.getColumnIndexOrThrow(KEY_AUTHOR));
		cursor.close();
		return author;
	}
	
	public synchronized boolean setCurrentPosition(long rowId, int currentChapter, float currentPercentage)
	{
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_CURRENT_CHAPTER, currentChapter);
		newValues.put(KEY_CURRENT_PERCENTAGE, currentPercentage);
		
		final String WHERE = KEY_ROWID + " = ?";
				
		return mDb.update(DATABASE_TABLE, newValues, WHERE, new String[]{String.valueOf(rowId)}) > 0;
	}
	
	public synchronized boolean setIsBookOpen(long rowId)
	{
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_IS_OPEN, 1);
		
		final String WHERE = KEY_ROWID + " = ?";
		
		return mDb.update(DATABASE_TABLE, newValues, WHERE, new String[]{String.valueOf(rowId)}) > 0;
	}
	
	public synchronized long addBook(String book[])
	{
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, book[0]);
        initialValues.put(KEY_AUTHOR, book[1]);
        initialValues.put(KEY_LANGUAGE, book[2]);
        initialValues.put(KEY_PATH, book[3]);
        initialValues.put(KEY_COVER_PATH, book[4]);
        initialValues.put(KEY_IS_OPEN, 0);
        initialValues.put(KEY_CURRENT_CHAPTER, 1);
        initialValues.put(KEY_CURRENT_PERCENTAGE, 0.0);
        
        isAddBook = true;

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
	
	public synchronized String getBookPath(long rowId)
	{
		final String WHERE = KEY_ROWID+" = ?";
		final Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_PATH}, WHERE, new String[]{String.valueOf(rowId)}, null, null, null);
		cursor.moveToFirst();
		final String bookPath = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PATH));
		cursor.close();
		return bookPath;
	}
	
	public synchronized boolean isInLibrary(String title, String author, String language)
	{
		final String WHERE = "(title = ? and author = ? and language = ?)";
		final Cursor cursor = mDb.query(DATABASE_TABLE, null, WHERE, new String[]{title, author, language}, null, null, null);
		boolean exists = (cursor.getCount() > 0);
		cursor.close();
		return exists;
	}
	
	public synchronized boolean removeBook(long rowId, boolean isDelete)
	{
		if(isDelete)
		{
			final String query = "select * from books where _id = "+rowId;
			final Cursor cursor = mDb.rawQuery(query, null);
			
			final String filePath;
			final String coverPath;
			
			if (cursor.moveToFirst())
			{
				filePath = cursor.getString(cursor.getColumnIndexOrThrow("path"));
				coverPath = cursor.getString(cursor.getColumnIndexOrThrow("cover_path"));
			}
			else
			{
				cursor.close();
				return false;
			}
			cursor.close();
			
			final File epub = new File(filePath);
			if(epub.exists())
				FileUtil.clear(filePath);
			
			if(coverPath != null)
			{
				final File cover = new File(coverPath);
				if(cover.exists())
					FileUtil.clear(coverPath);
			}
		}
		
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
	
	public synchronized Cursor fetchAllBooks()
	{

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_AUTHOR, KEY_LANGUAGE, KEY_PATH, KEY_COVER_PATH}, null, null, null, null, null);
    }
	
	public synchronized Cursor fetchBooks()
	{
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_AUTHOR, KEY_COVER_PATH}, null, null, null, null, null);
	}
}

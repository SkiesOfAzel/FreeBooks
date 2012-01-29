package com.icsd.adapters;

import java.util.HashMap;
import java.util.Map;

import com.icsd.freebooks.R;
import com.icsd.sql.DataBaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryAdapter extends CursorAdapter
{
	private final LayoutInflater inflater;
	private final Map<Integer, Drawable> CACHE;
	
	private boolean isDelVisible;
	
	public LibraryAdapter(Context context, Cursor c, boolean autoRequery)
	{
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
		DataBaseHelper.getInstance(context).open();
		
		this.inflater = LayoutInflater.from(context);
		this.CACHE = new HashMap<Integer, Drawable>();
		isDelVisible = false;
	}
	
	public void setDelVisibility(boolean isDelVisible)
	{
		this.isDelVisible = isDelVisible;
	}
	
	public void removeCover(int id)
	{
		CACHE.remove(id);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		ViewHolder holder = (ViewHolder) view.getTag();
		
        if (holder == null)
        {
        	//Log.e("LIBRARY_ADAPTER", "in viewholder");
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.libraryTextTitle);
            holder.author = (TextView) view.findViewById(R.id.libraryTextAuthor);
            holder.cover = (ImageView) view.findViewById(R.id.libraryCover);
            holder.delete = (Button) view.findViewById(R.id.libraryDelBook);
            holder.titleColumn = cursor.getColumnIndexOrThrow("title");
            holder.authorColumn = cursor.getColumnIndexOrThrow("author");
            holder.coverColumn = cursor.getColumnIndexOrThrow("cover_path");
            holder.idColumn = cursor.getColumnIndexOrThrow("_id");
            view.setTag(holder);
        }
        
        final int id = cursor.getInt(holder.idColumn);
        holder.author.setText(cursor.getString(holder.authorColumn));
        holder.title.setText(cursor.getString(holder.titleColumn));
        holder.delete.setId(id + 100);
        holder.cover.setId(id + 200);
        
        if(isDelVisible)
        	holder.delete.setVisibility(View.VISIBLE);
        else
        	holder.delete.setVisibility(View.GONE);
        
        final Drawable cover;
        final String path = cursor.getString(holder.coverColumn);
        
        if(CACHE.containsKey(id))
        	cover = CACHE.get(id);
        else
        {
        	if(cursor.getString(holder.coverColumn) != null)
            	cover = Drawable.createFromPath(path);
            else
            	cover = context.getResources().getDrawable(R.drawable.book_cover);
        	CACHE.put(id, cover);
        }

        holder.cover.setImageDrawable(cover);
        //Log.e("LIBRARY_ADAPTER", "cache length = "+CACHE.size());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return inflater.inflate(R.xml.grid_row, null);
	}
	
	private static class ViewHolder
	{
		private TextView title;
		private TextView author;
		private ImageView cover;
		private Button delete;
		
		private int titleColumn;
		private int authorColumn;
		private int coverColumn;
		private int idColumn;
	}

}

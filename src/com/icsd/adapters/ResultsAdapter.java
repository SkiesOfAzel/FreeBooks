package com.icsd.adapters;

import java.util.ArrayList;

import com.icsd.freebooks.R;
import com.icsd.structs.SearchResult;
import android.content.Context;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ResultsAdapter extends ArrayAdapter<SearchResult>
{

	private ArrayList<SearchResult> results;
	private int viewResourceId;

	public ResultsAdapter(Context context, int viewResourceId, ArrayList<SearchResult> results)
	{
		super(context, viewResourceId, results);
		this.results = results;
		this.viewResourceId = viewResourceId;
    }
	
	private static class ViewHolder
	{
		private TextView title;
		private TextView author;
		private TextView language;
	}

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
    	ViewHolder holder;
            
    	if (view == null) 
    	{
    		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     		view = inflater.inflate(viewResourceId, null);
     		
     		holder = new ViewHolder();
     		holder.title = (TextView) view.findViewById(R.id.textListTitle);
     		holder.author = (TextView) view.findViewById(R.id.textListAuthor);
     		holder.language = (TextView) view.findViewById(R.id.textListLanguage);
     		
     		view.setTag(holder);
    	}
    	else
    		holder = (ViewHolder)view.getTag();
    	
    	final SearchResult row = results.get(position);

    	if ((row != null) && (row.getAuthor() != null) && (row.getTitle() != null) && (row.getLanguage() != null))
    	{
    		if (holder.title != null)
    		{
    			holder.title.setText(row.getTitle());
    		}
    			
    		if(holder.author != null)
    		{
    			holder.author.setText(row.getAuthor()+" ");
    		}
    		
    		if(holder.language != null)
    		{
    			holder.language.setText(row.getLanguage());
    		}
    	}
            return view;
    }
}
package com.icsd.adapters;

import com.icsd.freebooks.R;
import android.content.Context;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LanguagesAdapter extends ArrayAdapter<String>
{

	private String[] results;
	private int viewResourceId;

	public LanguagesAdapter(Context context, int viewResourceId, String[] results)
	{
		super(context, viewResourceId, results);
		this.results = results;
		this.viewResourceId = viewResourceId;
    }
	
	private static class ViewHolder
	{
		private TextView name;
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
     		holder.name = (TextView) view.findViewById(R.id.textCategoriesName);
     		
     		view.setTag(holder);
    	}
    	else
    		holder = (ViewHolder)view.getTag();
    	
    	final String row = results[position];

    	if (row != null)
    	{
    		if (holder.name != null)
    		{
    			holder.name.setText(row);
    		}
    	}
        return view;
    }
}
package com.icsd.adapters;

import java.util.ArrayList;

import com.icsd.freebooks.R;
import com.icsd.structs.SubcategoriesResult;
import android.content.Context;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SubcategoriesAdapter extends ArrayAdapter<SubcategoriesResult>
{

	private ArrayList<SubcategoriesResult> results;
	private int viewResourceId;

	public SubcategoriesAdapter(Context context, int viewResourceId, ArrayList<SubcategoriesResult> results)
	{
		super(context, viewResourceId, results);
		this.results = results;
		this.viewResourceId = viewResourceId;
    }
	
	private static class ViewHolder
	{
		private TextView name;
		private TextView items;
	}

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
    	//Log.e("IN_GET_VIEW", "results size "+results.size());
    	ViewHolder holder;
            
    	if (view == null) 
    	{
    		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     		view = inflater.inflate(viewResourceId, null);
     		
     		holder = new ViewHolder();
     		holder.name = (TextView) view.findViewById(R.id.textSubcategoriesName);
     		holder.items = (TextView) view.findViewById(R.id.textSubcategoriesItems);
     		
     		view.setTag(holder);
    	}
    	else
    		holder = (ViewHolder)view.getTag();
    	
    	final SubcategoriesResult row = results.get(position);

    	if (row != null)
    	{
    		if (holder.name != null)
    		{
    			holder.name.setText(row.getName());
    		}
    		
    		if (holder.items != null)
    		{
    			holder.items.setText(row.getItems());
    		}
    	}
        return view;
    }
}
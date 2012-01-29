package com.icsd.adapters;

import java.util.ArrayList;

import com.icsd.freebooks.R;
import com.icsd.structs.FeaturedResult;
import android.content.Context;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FeaturedAdapter extends ArrayAdapter<FeaturedResult>
{

	private ArrayList<FeaturedResult> results;
	private int viewResourceId;

	public FeaturedAdapter(Context context, int viewResourceId, ArrayList<FeaturedResult> results)
	{
		super(context, viewResourceId, results);
		this.results = results;
		this.viewResourceId = viewResourceId;
    }
	
	private static class ViewHolder
	{
		private TextView title;
		private TextView author;
		private ImageView cover;
		private Button download;
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
     		holder.title = (TextView) view.findViewById(R.id.featuredListTitle);
     		holder.author = (TextView) view.findViewById(R.id.featuredListAuthor);
     		holder.cover = (ImageView) view.findViewById(R.id.featuredBook);
     		holder.download = (Button) view.findViewById(R.id.featuredDownloadButton);
     		
     		view.setTag(holder);
    	}
    	else
    		holder = (ViewHolder)view.getTag();
    	
    	final FeaturedResult row = results.get(position);

    	if (row != null)
    	{
    		if (holder.title != null)
    		{
    			holder.title.setText(row.getTitle());
    		}
    			
    		if(holder.author != null)
    		{
    			holder.author.setText(row.getAuthor()+" ");
    		}
    		
    		if(holder.cover != null)
    		{
    			holder.cover.setImageDrawable(row.getCover());
    		}
    		
    		if(holder.download != null)
    		{
    			holder.download.setId(position +300);
    		}
    	}
        return view;
    }
}
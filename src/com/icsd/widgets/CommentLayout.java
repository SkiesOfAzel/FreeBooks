package com.icsd.widgets;

import com.icsd.freebooks.R;

import android.content.Context;
//import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentLayout extends LinearLayout
{
	private final static String HTML_HEADER = "<html>\n<head></head>\n<body style=\"text-align:justify;background:#D9EFF7; margin: 0; padding: 0\">";
	private final static String HTML_FOOTER = "</body>\n</html>";
	
	public CommentLayout(Context context)
	{
		super(context);
		View.inflate(context, R.layout.comment, this);
	}
	
	public void setComment(String author, String date, String comment)
	{
		((TextView)(this.findViewById(R.id.commentTextAuthor))).setText(author);
		
		final String year;
		final String month;
		final String day;
		final String time;
		
		
		if((date != null) && (date.length() > 0) )
		{
			final String[] fullDateParams = date.split("T");
			if((fullDateParams != null) && (fullDateParams.length > 1))
			{
				if(fullDateParams[0] != null)
				{
					final String[] dateParams = fullDateParams[0].split("-");
				
					if((dateParams != null) && (dateParams.length > 2))
					{
						year = dateParams[0];
						month = getMonth(dateParams[1]);
						day = dateParams[2];
					}
					else
					{
						year = "";
						month = "";
						day = "";
					}
				}
				else
				{
					year = "";
					month = "";
					day = "";
				}
				
				if(fullDateParams[1] != null)
				{
					if(fullDateParams[1].length() > 3)
						time = fullDateParams[1].substring(0, fullDateParams[1].length() - 4);
					else
						time = "";
				}
				else
					time = "";
			}
			else
			{
				year = "";
				month = "";
				day = "";
				time = "";
			}
		}
		else
		{
			year = "";
			month = "";
			day = "";
			time = "";
		}
		
		((TextView)(this.findViewById(R.id.commentTextDate))).setText("on "+day+" "+month+", "+year+" at "+time);
		final WebView webView = (WebView) this.findViewById(R.id.commentText);
		WebSettings webSettings = webView.getSettings();
		webSettings.setDefaultFontSize(14);
		webView.setVerticalScrollBarEnabled(false); 
		webView.loadDataWithBaseURL(null, HTML_HEADER+comment+HTML_FOOTER,"text/html", "utf-8", null);//().setText(comment);
		//Log.e("IN_COMMENT", comment);
	}

	private String getMonth(String monthNum)
	{
		final String month;
		switch(Short.valueOf(monthNum))
		{
		case 1:
			month = "Jan";
			break;
		case 2:
			month = "Feb";
			break;
		case 3:
			month = "Mar";
			break;
		case 4:
			month = "Apr";
			break;
		case 5:
			month = "May";
			break;
		case 6:
			month = "Jun";
			break;
		case 7:
			month = "Jul";
			break;
		case 8:
			month = "Aug";
			break;
		case 9:
			month = "Sep";
			break;
		case 10:
			month = "Oct";
			break;
		case 11:
			month = "Nov";
			break;
		default:
			month = "Dec";
			break;
		}
		return month;
	}
}

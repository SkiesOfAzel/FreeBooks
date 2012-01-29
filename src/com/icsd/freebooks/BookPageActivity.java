package com.icsd.freebooks;

import java.lang.ref.WeakReference;
import com.icsd.widgets.CommentLayout;
import com.icsd.constants.ErrorCodes;
import com.icsd.sql.DataBaseHelper;
import com.icsd.structs.Book;
import com.icsd.threads.DownloadBookThread;
import com.icsd.threads.PopulateBookThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class BookPageActivity extends Activity implements OnClickListener, AnimationListener, ErrorCodes
{
	private final static String HTML_HEADER = "<html>\n<head></head>\n<body style=\"text-align:justify;background:#D9EFF7; margin: 0; padding: 0\">";
	private final static String HTML_FOOTER = "</body>\n</html>";
	private DataBaseHelper databaseHelper;
	private PopulateBookThread bookThread;
	private Book book;
	private String url;
	private boolean isLoading;
	private boolean isFromFeatured;
	//private boolean isCancelled;
	private Resources res;
	private AlertDialog.Builder inLibraryDialog;
	//private boolean isCanDownload;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.book_page);
                
        Bundle extras = getIntent().getExtras();
        
        if(extras!=null)
        {
        	url = extras.getString("BOOK_URL")+".atom";
        	isFromFeatured = extras.getBoolean("FROM_FEATURED");
        }
        else
        {
        	url = "";
        	isFromFeatured = false;
        }
        
        res = getResources();
        databaseHelper = DataBaseHelper.getInstance(this);
        databaseHelper.open();
        final Button stopButton = (Button) this.findViewById(R.id.bookStopButton);
        final Button reloadButton = (Button) this.findViewById(R.id.bookReloadButton);
        final Button downloadButton = (Button) this.findViewById(R.id.bookDownloadButton);
        
        final TextView textAuthor = (TextView)this.findViewById(R.id.bookTextAuthor);
		final TextView textLanguage = (TextView)this.findViewById(R.id.bookTextLanguage);
		final TextView textIssued = (TextView)this.findViewById(R.id.bookTextPublished);
		final TextView textCategory1 = (TextView)this.findViewById(R.id.bookTextCategories);
		final TextView textCategory2 = (TextView)this.findViewById(R.id.bookTextCategories2);
		
		 inLibraryDialog = new AlertDialog.Builder(this);
		 inLibraryDialog.setTitle(res.getString(R.string.book_in_library));
		 inLibraryDialog.setMessage(res.getString(R.string.overwrite));
		 inLibraryDialog.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener()
		 	{
			 	public void onClick(DialogInterface dialog, int id)
			 	{
			 		downloadBook(true);
			 	}
		 	});
		 
		 inLibraryDialog.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener()
		 	{
			 	public void onClick(DialogInterface dialog, int id)
			 	{
			 		dialog.cancel();
                }
		 	});
		
        
        stopButton.setOnClickListener(this);
        reloadButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        
        textAuthor.setOnClickListener(this);
        textLanguage.setOnClickListener(this);
        textIssued.setOnClickListener(this);
        textCategory1.setOnClickListener(this);
        textCategory2.setOnClickListener(this);
        
        isLoading = false;
        //isCancelled = false;
        
        Log.e("BOOK_PAGE_ACTIVITY", "url is : "+url);
        
        book = new Book();  
        
        bookPage();
    }
    
	/////////////////////////////////////////////////////////
	// Animations
	/////////////////////////////////////////////////////////

	public void startAnimation(View view)
	{
		Animation loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_animation);
		loadingAnimation.setAnimationListener(this);
		view.startAnimation(loadingAnimation);
	}

	public void onAnimationEnd(Animation animation)
	{
		if(isLoading)
			startAnimation(this.findViewById(R.id.bookImageLoading));

	}

	public void onAnimationRepeat(Animation animation)
	{
		// TODO Auto-generated method stub

	}

	public void onAnimationStart(Animation animation)
	{
		// TODO Auto-generated method stub

	}
    
	public void stopAnimation()
	{
		isLoading = false;
		this.findViewById(R.id.bookPlaceholderRight).setVisibility(View.GONE);
        this.findViewById(R.id.bookImageLoading).setVisibility(View.GONE);
	}
	
	///////////////////////////////////////////////////////
	//Buttons
	///////////////////////////////////////////////////////
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.bookReloadButton:
			bookPage();
			break;
			
		case R.id.bookStopButton:
			if((bookThread != null) && (bookThread.getStatus() != AsyncTask.Status.FINISHED))
				bookThread.cancel(true);
			//isCancelled = true;
			break;
			
		case R.id.bookTextAuthor:
			Log.e("ON_CLICK", "in textAuthor");
			goToResults((short)0);
			break;
		
		case R.id.bookTextLanguage:
			goToResults((short)1);
			break;
		
		case R.id.bookTextPublished:
			goToResults((short)2);
			break;
			
		case R.id.bookTextCategories:
			Log.e("ON_CLICK", "in textCategories");
			goToResults((short)3);
			break;
			
		case R.id.bookTextCategories2:
			goToResults((short)4);
			break;
			
		case R.id.bookDownloadButton:
			checkDownloadBook();
			break;
			
		}
		
	}
	
	///////////////////////////////////////////////////////
	//Book Page
	///////////////////////////////////////////////////////
	
	private void bookPage()
	{
		Log.i("BOOK_PAGE", url);
		book.clear();
		//book.getCategories().clear();
		//book.getComments().clear();
		((ViewGroup)this.findViewById(R.id.bookCommentsLayout)).removeAllViews();
		this.findViewById(R.id.bookTextTitle).setVisibility(View.GONE);
        this.findViewById(R.id.bookTextBy).setVisibility(View.GONE);
        this.findViewById(R.id.bookTextAuthor).setVisibility(View.GONE);
        this.findViewById(R.id.bookTextCategories).setVisibility(View.GONE);
        this.findViewById(R.id.bookTextCategories2).setVisibility(View.GONE);
        this.findViewById(R.id.bookLayout2).setVisibility(View.GONE);
        this.findViewById(R.id.bookTextCont).setVisibility(View.GONE);
        this.findViewById(R.id.bookContentLayout).setVisibility(View.GONE);
        this.findViewById(R.id.bookTextCom).setVisibility(View.GONE);
        this.findViewById(R.id.bookDownloadButton).setVisibility(View.GONE);
        
		isLoading = true;
		this.findViewById(R.id.bookReloadButton).setVisibility(View.GONE);
		this.findViewById(R.id.bookDownloadButton).setVisibility(View.GONE);
		this.findViewById(R.id.bookStopButton).setVisibility(View.VISIBLE);
		
		startAnimation(this.findViewById(R.id.bookImageLoading));
		
		bookThread = null;
		bookThread = new PopulateBookThread(new WeakReference<BookPageActivity>(this), url);
        bookThread.execute();
	}
	
	public void threadFinished(short error)
	{
		stopAnimation();
		this.findViewById(R.id.bookStopButton).setVisibility(View.GONE);
		this.findViewById(R.id.bookReloadButton).setVisibility(View.VISIBLE);
		
		postError(error);
	}
	
    public Book getBook()
    {
    	return book;
    }
    
    public void updateBook(short fase)
    {
    	switch(fase)
    	{
    	case 1:
    		updateDescription();
    		break;
    	case 2:
    		updateCategories();
    		break;
    	case 3:
    		updateCover();
    		break;
    	case 4:
    		updateComments();
    		break;
    	}
    }
    
    private void updateDescription()
    {
    	((TextView)findViewById(R.id.bookTextTitle)).setText(book.getTitle());
		this.findViewById(R.id.bookTextTitle).setVisibility(View.VISIBLE);
	
		this.findViewById(R.id.bookTextBy).setVisibility(View.VISIBLE);
	
		((TextView)findViewById(R.id.bookTextAuthor)).setText(book.getAuthor()+" ");
		this.findViewById(R.id.bookTextAuthor).setVisibility(View.VISIBLE);
	
		final String language;
		
		if(book.getLanguage().equals("en"))
			language = res.getString(R.string.english);
		
		else if(book.getLanguage().equals("fr"))
			language = res.getString(R.string.french);
	
		else if(book.getLanguage().equals("de"))
			language = res.getString(R.string.german);
	
		else if(book.getLanguage().equals("es"))
			language = res.getString(R.string.spanish);
	
		else if(book.getLanguage().equals("ru"))
			language = res.getString(R.string.russian);
	
		else
			language = book.getLanguage();
	
		this.findViewById(R.id.bookLayout2).setVisibility(View.VISIBLE);
	
		((TextView)this.findViewById(R.id.bookTextLanguage)).setText(language);
		((TextView)this.findViewById(R.id.bookTextPublished)).setText(book.getIssued());
		((TextView)this.findViewById(R.id.bookTextWords)).setText(book.getExtent());
	
		findViewById(R.id.bookDownloadButton).setVisibility(View.VISIBLE);
	    	
		if((book.getContent() != null) && (book.getContent().length() > 2))
		{
			final LinearLayout contentLayout = (LinearLayout) this.findViewById(R.id.bookContentLayout);
			contentLayout.removeAllViews();
			final WebView webView = new WebView(this);
			WebSettings webSettings = webView.getSettings();
			webSettings.setDefaultFontSize(14);
			webView.setVerticalScrollBarEnabled(false);
		
			this.findViewById(R.id.bookTextCont).setVisibility(View.VISIBLE);
			this.findViewById(R.id.bookContentLayout).setVisibility(View.VISIBLE);
		
			final String temp = Html.fromHtml(book.getContent()).toString();
			final String content;
			content = HTML_HEADER + temp.substring(0,temp.length()-2).replace("\"", " \" ") + HTML_FOOTER;

			webView.loadDataWithBaseURL(null, content,"text/html", "utf-8", null);
			contentLayout.addView(webView);
		}    	
    	/*catch(NullPointerException npe)
    	{
    		Log.e("IN_BOOK_PAGE", "NullPointerException "+npe);
    	}*/
    }
    
    private void updateCategories()
    {
    	if(book.getCategories().size() > 0)
    	{
			((TextView)this.findViewById(R.id.bookTextCategories)).setText(book.getCategories().get(0).getName());
			this.findViewById(R.id.bookTextCategories).setVisibility(View.VISIBLE);
    	}
	        
		if(book.getCategories().size() > 1)
		{
			((TextView)this.findViewById(R.id.bookTextCategories2)).setText(", "+book.getCategories().get(1).getName());    	
			this.findViewById(R.id.bookTextCategories2).setVisibility(View.VISIBLE);
		}
    }
    
    private void updateCover()
    {
    	if(book.getCover() != null)
    		((ImageView)this.findViewById(R.id.bookImage)).setImageDrawable(book.getCover());
    	else
    		((ImageView)this.findViewById(R.id.bookImage)).setImageDrawable(res.getDrawable(R.drawable.book_cover));
    }
    
    private void updateComments()
    {
    	final LinearLayout commentsLayout = (LinearLayout)this.findViewById(R.id.bookCommentsLayout);
    	commentsLayout.removeAllViews();
		
		if(book.getComments().size() > 0)
		{
			this.findViewById(R.id.bookTextCom).setVisibility(View.VISIBLE);
			commentsLayout.setVisibility(View.VISIBLE);
			for(short i =0; i < book.getComments().size(); i++)
			{	
				final CommentLayout comment = new CommentLayout(getBaseContext());
				comment.setComment(book.getComments().get(i).getAuthor(), book.getComments().get(i).getDate(), book.getComments().get(i).getComment());
				commentsLayout.addView(comment);
			}
		}
    }
    
    private void postError(short ERROR)
	{
		if(ERROR != NO_ERROR)
		{
			final String message;
			
			switch(ERROR)
			{
				case MALFORMED_URL:
					message = res.getString(R.string.malformed_url_exception);
					break;
				case PARSER_CONFIG_ERROR:
					message = res.getString(R.string.parser_config_exception);
					break;
				case SAX_ERROR:
					message = res.getString(R.string.parser_exception);
					break;
				case IO_ERROR:
					message = res.getString(R.string.book_no_connection);
					break;
				default:
					message = res.getString(R.string.error);
					break;
			}
			
			final Toast msg = Toast.makeText(this, message, Toast.LENGTH_LONG);
			msg.show();
		}
	}
    
    ////////////////////////////////////////////////////////////////////
    //SEARCH
    ////////////////////////////////////////////////////////////////////
    
	public void goToResults(short searchType)
	{
		final String url;
		final boolean searchByAuthor;
		
		switch(searchType)
		{
		case 0:
			url = book.getAuthorUrl();
			searchByAuthor = true;
			break;
		case 1:
			url= "http://www.feedbooks.com/books/recent.atom?lang="+book.getLanguage();
			searchByAuthor = false;
			break;
		case 2:
			url="http://www.feedbooks.com/books.atom?year="+book.getIssued();
			searchByAuthor = false;
			break;
		case 3:
			url=book.getCategories().get(0).getUrl();
			searchByAuthor = false;
			break;
		case 4:
			url=book.getCategories().get(1).getUrl();
			searchByAuthor = false;
			break;
		default:
			url = book.getAuthorUrl();
			searchByAuthor = false;
			break;
		}
				
		if(isFromFeatured)
		{
			if (bookThread != null && bookThread.getStatus() != AsyncTask.Status.FINISHED)
				bookThread.cancel(true);
			
			final Intent intent = new Intent(getBaseContext(), ResultsPageActivity.class);
			intent.putExtra("SEARCH_URL", url);
			intent.putExtra("FROM_BOOK", true);
			intent.putExtra("URL_TYPE", searchByAuthor);
			startActivityForResult(intent, 2);
		}
		else
		{
			final Intent intent= getIntent();
			intent.putExtra("RETURNED_URL", url);
			intent.putExtra("URL_TYPE", searchByAuthor);
			setResult(RESULT_OK, intent);
			finish();
		}
		//Log.e("SEARCH_THREAD", url);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK && requestCode==2)
		{
			url = data.getStringExtra("RETURNED_URL")+".atom";
			bookPage();
		}
	}
	
	private void checkDownloadBook()
	{
		if(databaseHelper.isInLibrary(book.getTitle(), book.getAuthor(), book.getLanguage()))
			inLibraryDialog.show();
		else
			downloadBook(false);
	}
	
	private void downloadBook(boolean isInLibrary)
	{
		final DownloadBookThread downloadBook = new DownloadBookThread(new WeakReference<BookPageActivity>(this), new WeakReference<Book>(book), isInLibrary);
		downloadBook.execute();
	}
    
    @Override
	protected void onDestroy()
	{
		if ((bookThread != null) && (bookThread.getStatus() != AsyncTask.Status.FINISHED))
			bookThread.cancel(true);
		//databaseHelper.close();
		super.onDestroy();
	}
}

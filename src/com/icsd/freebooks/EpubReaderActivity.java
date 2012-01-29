package com.icsd.freebooks;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import com.icsd.animations.BookPageAnimation;
import com.icsd.sql.DataBaseHelper;
import com.icsd.threads.OpenChapterThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewConfiguration;

import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.TextView;

public class EpubReaderActivity extends Activity
{
	private final static String TAG = "EPUB_READER_ACTIVITY";
	
	//private FrameLayout pageSwitcher;
	private BookPageAnimation pageAnimation;
	private View bookPage;
	private View dummyPage;
	private View loadingPage;
	private BitmapDrawable dummyPageBitmap;
	private TextView pageTitle;
	private TextView pageTime;
	private TextView pageAuthor;
	private TextView pageNumber;
	private WebView pageContent;
	private DataBaseHelper databaseHelper;
	private ProgressDialog dialog;
	private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    
	private String epubPath = null;
	private String baseUrl = null;
	private String title = null;
	private String author = null;
	private String bookContent = null;
	
	private int bookId = 0;
	private int currentChapter = 1;
	private int maxChapter = 1;
	private int currentPage = 1;
	private int maxPage = 1;
	
	private float currentPercentage = 0.0f;
	
	private int screenDensity;
	private int screenWidth;
	
	//private boolean isBookOpen;
	private boolean isOpenChapterRunning;
	private boolean isNextPage = false;
	private boolean isPrevPage = false;
	private boolean isNextChapter = false;
	private boolean isPrevChapter = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.epub_reader);
		
		Bundle extras = getIntent().getExtras();
        bookId = extras.getInt("BOOK_ID");
        title = extras.getString("TITLE");
		author = extras.getString("AUTHOR");
		epubPath = extras.getString("EPUB_PATH");
		baseUrl = extras.getString("BASE_URL");
		
	    Log.i(TAG, "bookId: "+bookId);
	    
	    initViews();
	    initListeners();
	    initVariables();
	    showDialog();
		final OpenChapterThread openChapter = new OpenChapterThread(new WeakReference<EpubReaderActivity>(this), epubPath, currentChapter);
		openChapter.execute();
	}
	
	private void initViews()
	{
		//pageSwitcher = (FrameLayout) this.findViewById(R.id.pageSwitcher);
        bookPage = this.findViewById(R.id.readerPage);
        dummyPage = this.findViewById(R.id.readerDummyPage);
        loadingPage = this.findViewById(R.id.readerLoadingPage);
        dummyPage.setVisibility(View.GONE);
             
		pageContent = (WebView) this.findViewById(R.id.readerPageContent);
		pageContent.setHorizontalScrollBarEnabled(false);
		pageContent.setVerticalScrollBarEnabled(false);
		pageContent.addJavascriptInterface(new AndroidBridge(), "android");

		WebSettings webSetting = pageContent.getSettings();
		webSetting.setDefaultZoom(this.getZoomDensity());
		webSetting.setDefaultFontSize(18);
		webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		webSetting.setJavaScriptEnabled(true);
		
		pageTitle = (TextView) this.findViewById(R.id.readerPageTitle);
	    pageTime = (TextView) this.findViewById(R.id.readerPageTime);
		pageAuthor = (TextView) this.findViewById(R.id.readerPageAuthor);
		pageNumber = (TextView) this.findViewById(R.id.readerPageNum);
		
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
	}
	
	private void initListeners()
	{
		pageAnimation = new BookPageAnimation(this, dummyPage, bookPage);
		gestureDetector = new GestureDetector(new ReaderGestureListener());
		
        gestureListener = new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
 		pageContent.setOnTouchListener(gestureListener);
	    
		pageContent.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result)
			{
				Log.i(TAG, "[CALLBACK_WV] boolean onJsAlert(view:" + view + ", url:" + url + ", message:" + message
						+ ", result:" + result + ")");
				Log.d(TAG, "message: " + message);
				
				if(message.equals("FINISHED_LOADING"))
					setupChapter();

				
				result.confirm();
				return true;
			}
		});
	}
	
	private void initVariables()
	{
	    final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		screenWidth = metrics.widthPixels;
		screenDensity = metrics.densityDpi;
		Log.i("initVariables()", "bookId = "+bookId);
		databaseHelper = DataBaseHelper.getInstance(getBaseContext());
		databaseHelper.open();
		currentChapter = databaseHelper.getCurrentChapter(bookId);
		currentPercentage = databaseHelper.getCurrentPercentage(bookId);
		//isBookOpen = databaseHelper.isBookOpen(bookId);
		
		isOpenChapterRunning = false;
	}
	
	@Override
	protected void onStart()
	{
		Log.i(TAG, "[CALLBACK] void onStart()");
		super.onStart();
	}

	@Override
	protected void onResume()
	{
		Log.i(TAG, "[CALLBACK] void onResume()");
		super.onResume();
		Log.d(TAG, "onResume: dialog: " + dialog 
				+"\nbookPage: " + bookPage
				+ "\npageContent: " + pageContent
				+ "\ndummyPage: " + dummyPage
				+ "\ndummyPageBitmap: " + dummyPageBitmap
				+ "\npageAnimation: " + pageAnimation
				+ "\ngestureDetector: " + gestureDetector
				+ "\ngestureListener: " + gestureListener
				+ "\npageTitle: " + pageTitle
				+ "\npageAuthor: " + pageAuthor
				+ "\ndatabaseHelper: " + databaseHelper);
		
		if(dialog == null)
			dialog = new ProgressDialog(this);
		
		bookPage.bringToFront();
	}

	@Override
	protected void onPause()
	{
		Log.i(TAG, "[CALLBACK] void onPause()");
		super.onPause();
		databaseHelper.setCurrentPosition(bookId, currentChapter, currentPercentage);
	}

	@Override
	protected void onStop()
	{
		Log.i(TAG, "[CALLBACK] void onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Log.i(TAG, "[CALLBACK] void onDestroy()");
		//databaseHelper.close();
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    Log.i(TAG, "onConfigurationChanged");
	    super.onConfigurationChanged(newConfig);
	    
	    if(!isOpenChapterRunning)
	    {
	    	showDialog();	    	
	    	pageContent.loadDataWithBaseURL("file://"+baseUrl+"OPS/", bookContent, "text/html", "UTF-8", null);
	    }
	}
	
	public void setPageContent(String bookContent, int maxChapter)
	{
		this.bookContent = bookContent;
		this.maxChapter = maxChapter;
		pageContent.loadDataWithBaseURL("file://"+baseUrl+"OPS/", bookContent, "text/html", "UTF-8", null);
	}
	
	public String getBookContent()
	{
		return bookContent;
	}
	
	public void setIsOpenChapterRunning(boolean isOpenChapterRunning)
	{
		this.isOpenChapterRunning = isOpenChapterRunning;
	}
	
	private void setupChapter()
	{
		pageContent.loadUrl("javascript:getTotalPageNum()");
		pageContent.loadUrl("javascript:openPageByPercentage(" + currentPercentage + ")");
	}
	
	private ZoomDensity getZoomDensity()
	{
		ZoomDensity zd;
		
		switch(screenDensity)
		{
		case 240:
			zd = WebSettings.ZoomDensity.FAR;
			break;
		case 120:
			zd = WebSettings.ZoomDensity.CLOSE;
			break;
		default:
			zd = WebSettings.ZoomDensity.MEDIUM;
			break;
		}
		return zd;
	}
	
	private class ReaderGestureListener extends SimpleOnGestureListener
	{

		@Override
		public boolean onDown(MotionEvent event)
		{
			//Log.i(TAG, "[CALLBACK_GL] boolean onDown(e:" + e + ")");
			return super.onDown(event);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event)
		{
			Log.i(TAG, "[CALLBACK_GL] boolean onSingleTapConfirmed(e:" + event + ")");

			float posX = event.getX();
			int flingAreaSize = screenWidth/3;
			Log.d(TAG, "posX: " + posX);
			Log.d(TAG, "screenWidth: " + screenWidth);
			Log.d(TAG, "flingAreaSize: " + flingAreaSize);

			// open previous
			if (0 <= posX && posX <= flingAreaSize)
			{
				Log.d(TAG, "if (0 <= " + posX + " <= " + flingAreaSize + ")");
				previousPage();
			}
			// open next
			else if ((screenWidth - flingAreaSize) <= posX && posX <= screenWidth)
			{
				Log.d(TAG, "if(" + (screenWidth - flingAreaSize) + " <= " + posX + " <= " + screenWidth + ")");
				nextPage();
			}

			return super.onSingleTapConfirmed(event);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e)
		{
			Log.i(TAG, "[CALLBACK_GL] boolean onDoubleTap(e:" + e + ")");
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
		{
			Log.i(TAG, "[CALLBACK_GL] boolean onFling(e1:" + event1 + ", e2:" + event2 + ", velocityX:" + velocityX
					+ ", velocityY:" + velocityY + "");
			final ViewConfiguration vc = ViewConfiguration.get(EpubReaderActivity.this);
			
			float x1 = event1.getX();
			float x2 = event2.getX();
			int movement = (int) (x2 - x1) / vc.getScaledTouchSlop();
			Log.d(TAG, "movement: " + movement);

			if (movement > 0)
				previousPage();
			
			else if (movement < 0)
				nextPage();

			return super.onFling(event1, event2, velocityX, velocityY);
		}

		@Override
		public void onShowPress(MotionEvent e)
		{
			//Log.i(TAG, "[CALLBACK_GL] void onShowPress(e:" + e + ")");
			super.onShowPress(e);
		}

		@Override
		public void onLongPress(MotionEvent e)
		{
			//Log.i(TAG, "[CALLBACK_GL] void onLongPress(e:" + e + ")");
			super.onLongPress(e);
		}
	};
	
	class AndroidBridge
	{
		public void setTotalPageNum(final int page)
		{
			Log.i(TAG, "[FIRST_BRIDGE] void setTotalPageNum(page:" + page + ")");
			maxPage = page;
		}
		
		public void setCurPageLocation(final int page, final float percentage) throws InterruptedException
		{
			Log.i(TAG, "[FIRST_BRIDGE] void setCurPageLocation(page:" + page + ", percentage:" + percentage + ")");
			currentPage = page;
			currentPercentage = percentage;
			

			runOnUiThread( new Runnable()
			{
				public void run()
				{
					setupPage();

				}
			});
		}
	}
	
	private void setupPage()
	{
		pageTitle.setText(title + " ");
		
		final String minutes;
		
		final Calendar calendar = Calendar.getInstance();
		
		if(calendar.get(Calendar.MINUTE) < 10)
			minutes = "0"+calendar.get(Calendar.MINUTE);
		else
			minutes = ""+calendar.get(Calendar.MINUTE);
		
		pageTime.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + minutes);
		
		pageAuthor.setText(author + " ");
		
		pageNumber.setText(currentPage + "/" + maxPage);
		
		dismissDialog();
		
		if(isPrevPage)
		{
			isPrevPage = false;
			pageAnimation.animate(false);
		}
		else if (isNextPage)
		{
			isNextPage = false;
			pageAnimation.animate(true);
		}
		else if(isNextChapter)
		{
			isNextChapter = false;
			pageAnimation.animate(true);
		}
		else if(isPrevChapter)
		{
			isPrevChapter = false;
			pageAnimation.animate(false);
		}
	}
	
	private void previousPage()
	{
		if(currentPage == 1)
		{	
			clearDummyPage();
			fetchPreviousChapter();
		}
		else
		{
			loadDummyPage();
			pageContent.loadUrl("javascript:prevPage()");
			isPrevPage = true;
		}
	}
	
	private void nextPage()
	{
		if(currentPage == maxPage)
		{
			clearDummyPage();
			fetchNextChapter();
		}
		
		else
		{
			loadDummyPage();
			pageContent.loadUrl("javascript:nextPage()");
			isNextPage = true;
		}
	}
	
	private void clearDummyPage()
	{
		dummyPageBitmap = null;
    	dummyPage.setBackgroundDrawable(null);
	}
	
	private void loadDummyPage()
	{
		dummyPageBitmap = null;
		dummyPageBitmap = getPageBitmap();
		dummyPage.setBackgroundDrawable(dummyPageBitmap);
		dummyPage.bringToFront();
		dummyPage.setVisibility(View.VISIBLE);
	}
	
	private final BitmapDrawable getPageBitmap()
	{
		final BitmapDrawable page;
		
		bookPage.layout(0, 0, bookPage.getWidth(), bookPage.getHeight());
    	bookPage.buildDrawingCache(false);
    	final Bitmap drawingCache = bookPage.getDrawingCache();
		page = new BitmapDrawable(drawingCache.copy(drawingCache.getConfig(), false));
		bookPage.destroyDrawingCache();
		
		return page;
	}
	
	private void fetchPreviousChapter()
	{
		if(currentChapter == 1)
			return;
		
		loadDummyPage();
		isPrevChapter = true;
		currentPercentage = 1.0f;
		--currentChapter;
		bookContent = null;
		dummyPage.setVisibility(View.VISIBLE);
		dummyPage.bringToFront();
		dialog.show();
		final OpenChapterThread openChapter = new OpenChapterThread(new WeakReference<EpubReaderActivity>(this), epubPath, currentChapter);
		openChapter.execute();
	}
	
	private void fetchNextChapter()
	{
		if(currentChapter == maxChapter)
			return;
		
		loadDummyPage();
		isNextChapter = true;
		currentPercentage = 0.0f;
		++currentChapter;
		bookContent = null;
		dummyPage.setVisibility(View.VISIBLE);
		dummyPage.bringToFront();
		dialog.show();
		final OpenChapterThread openChapter = new OpenChapterThread(new WeakReference<EpubReaderActivity>(this), epubPath, currentChapter);
		openChapter.execute();
	}
	
	private void showDialog()
	{
		loadingPage.setVisibility(View.VISIBLE);
    	loadingPage.bringToFront();
    	dialog.show();
	}
	
	private void dismissDialog()
	{
		dialog.dismiss();
		loadingPage.setVisibility(View.GONE);
    	bookPage.bringToFront();
	}
}

package com.icsd.animations;

import com.icsd.freebooks.R;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class BookPageAnimation
{
	private final Context context;
	private final View firstPage;
	private final View secondPage;
	
	public BookPageAnimation(Context context, View firstPage, View secondPage)
	{
		this.context = context;
		this.firstPage = firstPage;
		this.secondPage = secondPage;
	}
	
	public void animate(boolean GOTO_NEXT_PAGE)
	{
		if(GOTO_NEXT_PAGE)
			goNext();
		else
			goPrevious();
	}
	
	private void goNext()
	{
		firstPage.setVisibility(View.VISIBLE);
		final Animation slideLeftIn = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
		final Animation slideLeftOut = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);
		slideLeftOut.setAnimationListener(new PageTurnListener());
		firstPage.startAnimation(slideLeftOut);
		secondPage.startAnimation(slideLeftIn);
	}
	
	private void goPrevious()
	{
		firstPage.setVisibility(View.VISIBLE);
		final Animation slideRightIn = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
		final Animation slideRightOut = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);
		slideRightOut.setAnimationListener(new PageTurnListener());
		firstPage.startAnimation(slideRightOut);
		secondPage.startAnimation(slideRightIn);
	}
	
	private class PageTurnListener implements Animation.AnimationListener
	{

		public void onAnimationEnd(Animation animation)
		{
			firstPage.setVisibility(View.GONE);
			secondPage.bringToFront();
		}

		public void onAnimationRepeat(Animation animation)
		{
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation)
		{
			// TODO Auto-generated method stub
			
		}
	}
}

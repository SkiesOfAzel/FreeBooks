package com.icsd.animations;

//import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

public class Flip3dDoAnimation
{
	private View firstView;
	private View secondView;
	private boolean isFirstView;
	
	public void animate(View firstView, View secondView)
	{
		this.firstView = firstView;
		this.secondView = secondView;
		isFirstView = true;
		
		applyRotation(0, 90, firstView, secondView);
	}
	
	private void applyRotation(float start, float end, View firstView, View secondView)
	{
		// Find the center of image
		final float centerX = firstView.getWidth() / 2.0f;
		final float centerY = firstView.getHeight() / 2.0f;

		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
		rotation.setDuration(300);
		rotation.setFillAfter(true);
		
		if(isFirstView)
		{
			isFirstView = false;
			rotation.setInterpolator(new AccelerateInterpolator());
			rotation.setAnimationListener(new DisplayNextView());
		}
		else
			rotation.setInterpolator(new DecelerateInterpolator());

		firstView.startAnimation(rotation);
	}
	
	private class DisplayNextView implements Animation.AnimationListener
	{
		public void onAnimationEnd(Animation animation)
		{
			firstView.post(new Swap());
			//Swap();
			
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
	
/*	private void Swap()
	{
		Log.e("IN_SWAP", "second view id is "+secondView.getId());
		firstView.setVisibility(View.GONE);
		secondView.setVisibility(View.VISIBLE);
		secondView.requestFocus();
		
		applyRotation(-90, 0, secondView, firstView);
	}*/
	
	private class Swap implements Runnable
	{
		
		public void run()
		{

			firstView.setVisibility(View.GONE);
			secondView.setVisibility(View.VISIBLE);
			secondView.bringToFront();
			
			applyRotation(-90, 0, secondView, firstView);
		}
	}
}

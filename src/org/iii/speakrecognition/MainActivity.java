package org.iii.speakrecognition;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MainActivity extends Activity
{

	private VoiceRecognition	voice		= null;
	private ImageButton			btnSpeak	= null;
	private boolean				mbSpeak		= false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();

		setContentView(R.layout.activity_main);

		voice = VoiceRecognition.getInstance(this);

		btnSpeak = (ImageButton) this.findViewById(R.id.btnSpeak);
		btnSpeak.setOnClickListener(itemClick);

	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		voice.stop();
		voice.destroy();
		super.onDestroy();
	}

	OnClickListener itemClick = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (v.getId() == R.id.btnSpeak)
			{
				mbSpeak = mbSpeak ? false : true;
				if (mbSpeak)
				{
					btnSpeak.setImageResource(R.drawable.mic_on);
					voice.start();
				}
				else
				{
					btnSpeak.setImageResource(R.drawable.mic_off);
					voice.stop();
				}
			}
		}
	};

}

package org.iii.speakrecognition;

import android.app.Activity;
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
		setContentView(R.layout.activity_main);

		voice = VoiceRecognition.getInstance(this);

		btnSpeak = (ImageButton) this.findViewById(R.id.btnSpeak);
		btnSpeak.setOnClickListener(itemClick);

	}

	@Override
	protected void onPause()
	{
		voice.stop();
		voice.destroy();
		super.onPause();
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
					voice.start();
				}
				else
				{
					voice.stop();
				}
			}
		}
	};

}

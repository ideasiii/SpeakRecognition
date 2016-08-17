package org.iii.speakrecognition;

import org.iii.speakrecognition.VoiceRecognition.OnRecognitionResult;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import sdk.ideas.common.Logs;

public class MainActivity extends Activity
{

	private VoiceRecognition	voice		= null;
	private ImageButton			btnSpeak	= null;
	private TextView			tvSpeech	= null;
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
		tvSpeech = (TextView) this.findViewById(R.id.txtSpeechInput);

		voice.setOnRecognitionResultListener(RecognitionListener);

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		voice.stop();
		voice.destroy();
	}

	@Override
	protected void onResume()
	{
		voice.init(this);
		voice.start();
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{

		super.onDestroy();
	}

	OnClickListener		itemClick			= new OnClickListener()
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

	OnRecognitionResult	RecognitionListener	= new OnRecognitionResult()
											{
												@Override
												public void onRecognitionResult(int nErrorCode,
														SparseArray<String> listResult)
												{
													String strText = "";

													//if (0 == nErrorCode)
													//{
													for (int i = 0; i < listResult.size(); ++i)
													{
														strText += listResult.get(i);
														strText += "\n";
													}
													tvSpeech.setText(strText);
													Logs.showTrace(strText);

													//}

												}
											};
}

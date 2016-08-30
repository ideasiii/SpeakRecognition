package org.iii.speakrecognition;

import org.iii.speakrecognition.VoiceRecognition.OnRecognitionResult;
import org.iii.speakrecognition.VoiceRecognition.OnRmsResult;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import sdk.ideas.common.Logs;

public class MainActivity extends Activity
{

	private VoiceRecognition	voice		= null;
	private ImageButton			btnSpeak	= null;
	private TextView			tvSpeech	= null;
	private boolean				mbSpeak		= false;
	private String				strText		= "";
	private ProgressBar			progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();

		setContentView(R.layout.activity_main);

		voice = new VoiceRecognition(this);

		btnSpeak = (ImageButton) this.findViewById(R.id.btnSpeak);
		btnSpeak.setOnClickListener(itemClick);
		tvSpeech = (TextView) this.findViewById(R.id.txtSpeechInput);

		voice.setOnRecognitionResultListener(RecognitionListener);

		progressBar = (ProgressBar) findViewById(R.id.progressBarSpeech);
		progressBar.setMax(10);

		voice.setOnRmsResultListener(rmsResult);
	}

	@Override
	protected void onPause()
	{
		voice.stop();
		//voice.destroy();
		super.onPause();

	}

	@Override
	protected void onResume()
	{
		//	voice.init(this);
		//	voice.start();
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		//	voice.stop();
		//	voice.destroy();
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
															progressBar.setIndeterminate(false);
															btnSpeak.setImageResource(R.drawable.mic_on);
															voice.start();
															handler.sendEmptyMessageDelayed(666, 3000);
															tvSpeech.setText("");
														}
														else
														{
															progressBar.setIndeterminate(true);

															//	btnSpeak.setImageResource(R.drawable.mic_off);
															//	voice.stop();
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
													tvSpeech.setText("");
													strText = "";

													if (SpeechRecognizer.ERROR_CLIENT == nErrorCode)
													{
														progressBar.setIndeterminate(false);
														tvSpeech.setText("無法辨識，再試一次");
														btnSpeak.setImageResource(R.drawable.mic_on);
														voice.start();
														handler.sendEmptyMessageDelayed(666, 3000);
														return;
													}

													if (SpeechRecognizer.ERROR_RECOGNIZER_BUSY == nErrorCode)
													{
														return;
													}

													if (0 == nErrorCode)
													{
														for (int i = 0; i < listResult.size(); ++i)
														{
															strText += listResult.get(i);
															strText += "\n";
														}

														tvSpeech.setText(strText);
													}
													Logs.showTrace(strText);

												}
											};

	OnRmsResult			rmsResult			= new OnRmsResult()
											{

												@Override
												public void onRms(float fRms)
												{
													progressBar.setProgress((int) fRms);
												}

											};

	@SuppressLint("HandlerLeak")
	private Handler		handler				= new Handler()
											{

												@Override
												public void handleMessage(Message msg)
												{
													if (msg.what == 666)
													{
														btnSpeak.setImageResource(R.drawable.mic_off);
														voice.stop();
														tvSpeech.setText(strText);

														progressBar.setIndeterminate(true);

													}
												}

											};
}

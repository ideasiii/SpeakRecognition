package org.iii.speakrecognition;

import org.iii.speakrecognition.VoiceRecognition.OnPartialResult;
import org.iii.speakrecognition.VoiceRecognition.OnRecognitionResult;
import org.iii.speakrecognition.VoiceRecognition.OnRmsResult;

import android.app.Application;
import sdk.ideas.common.Logs;

public class MainApplication extends Application
{

	private VoiceRecognition voice = null;

	@Override
	public void onCreate()
	{
		Logs.showTrace("Application Create");
		super.onCreate();

		voice = new VoiceRecognition(this.getApplicationContext());

	}

	@Override
	public void onTerminate()
	{
		Logs.showTrace("Application Terminate");
		super.onTerminate();
	}

	@Override
	protected void finalize() throws Throwable
	{
		Logs.showTrace("Application Finalize");
		super.finalize();
	}

	public void setOnRecognitionResultListener(OnRecognitionResult listener)
	{
		voice.setOnRecognitionResultListener(listener);
	}

	public void setOnRmsResultListener(OnRmsResult listener)
	{
		voice.setOnRmsResultListener(listener);
	}

	public void setOnPartialResultListener(OnPartialResult listener)
	{
		voice.setOnPartialResultListener(listener);
	}

	public void speechStart()
	{
		voice.start();
	}

	public void speechStop()
	{
		voice.stop();
	}
}

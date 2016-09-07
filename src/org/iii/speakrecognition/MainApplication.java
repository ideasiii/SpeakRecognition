package org.iii.speakrecognition;

import java.util.ArrayList;
import org.iii.speakrecognition.VoiceRecognition.OnPartialResult;
import org.iii.speakrecognition.VoiceRecognition.OnRecognitionResult;
import org.iii.speakrecognition.VoiceRecognition.OnRmsResult;
import android.Manifest;
import android.app.Activity;
import android.app.Application;
import sdk.ideas.common.Logs;
import sdk.ideas.common.OnCallbackResult;
import sdk.ideas.tool.premisson.RuntimePermissionHandler;
import sdk.ideas.tool.speech.tts.TextToSpeechHandler;

public class MainApplication extends Application
{

	private VoiceRecognition			voice						= null;
	private RuntimePermissionHandler	mRuntimePermissionHandler	= null;
	private TextToSpeechHandler			mTextToSpeechHandler		= null;

	@Override
	public void onCreate()
	{
		Logs.showTrace("Application Create");
		super.onCreate();

		voice = new VoiceRecognition(this.getApplicationContext());
		voice.init(this.getApplicationContext());

		mTextToSpeechHandler = new TextToSpeechHandler(this);
	}

	public void startRequestPermissions(Activity mActivity, OnCallbackResult permissionOnCallbackResultListener)
	{

		ArrayList<String> permissions = new ArrayList<String>();
		permissions.add(Manifest.permission.RECORD_AUDIO);
		permissions.add(Manifest.permission.INTERNET);
		permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
		permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
		permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
		permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
		permissions.add(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS);

		mRuntimePermissionHandler = new RuntimePermissionHandler(mActivity, permissions);
		mRuntimePermissionHandler.setOnCallbackResultListener(permissionOnCallbackResultListener);
		mRuntimePermissionHandler.startRequestPermissions();

	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		mRuntimePermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public void setTTSOnCallbackResultListener(OnCallbackResult OnCallbackResultListener)
	{
		mTextToSpeechHandler.setOnCallbackResultListener(OnCallbackResultListener);
	}

	public void setTTSInit()
	{
		mTextToSpeechHandler.init();
	}

	public void textToSpeech(String textID, String text)
	{
		mTextToSpeechHandler.textToSpeech(text, textID);
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

	public void speechReStart()
	{
		voice.reStart();
	}

	public void speechStop()
	{
		voice.stop();
	}
}

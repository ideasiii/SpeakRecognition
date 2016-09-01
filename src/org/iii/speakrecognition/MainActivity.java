package org.iii.speakrecognition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iii.speakrecognition.HttpClient.HttpResponseListener;
import org.iii.speakrecognition.VoiceRecognition.OnPartialResult;
import org.iii.speakrecognition.VoiceRecognition.OnRecognitionResult;
import org.iii.speakrecognition.VoiceRecognition.OnRmsResult;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import sdk.ideas.common.OnCallbackResult;
import sdk.ideas.tool.premisson.RuntimePermissionHandler;

public class MainActivity extends Activity
{
	private ImageButton					btnSpeak					= null;
	private TextView					tvSpeech					= null;
	private boolean						mbSpeak						= false;
	private String						strText						= "";
	private ProgressBar					progressBar;
	private FacebookHandler				facebook					= null;
	private RuntimePermissionHandler	mRuntimePermissionHandler	= null;
	private MainApplication				mainApplication				= null;
	final private int					TIMEOUT_SPEECH				= 7000;					//million seconds
	private HttpClient					httpClient					= null;
	private final String				TARGET_HOST					= "http://jieba.srm.pw";
	private final String				PATH_API_JIEBA				= "/jieba/pos";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		mainApplication = (MainApplication) this.getApplication();

		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();

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

		mRuntimePermissionHandler = new RuntimePermissionHandler(this, permissions);
		mRuntimePermissionHandler.startRequestPermissions();
		mRuntimePermissionHandler.setOnCallbackResultListener(new OnCallbackResult()
		{

			@Override
			public void onCallbackResult(int result, int what, int from, HashMap<String, String> message)
			{
				Logs.showTrace("what: " + String.valueOf(what) + " Message: " + message);
			}

		});
		showLogin();

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		MainActivity.this.mRuntimePermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		FacebookHandler.callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private void showLogin()
	{

		setContentView(R.layout.login);
		findViewById(R.id.textViewLoginFacebook).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (Utility.checkInternet(MainActivity.this))
				{
					showFacebookLogin();
				}
				else
				{
					DialogHandler.showNetworkError(MainActivity.this, false, handler);
				}
			}
		});
		findViewById(R.id.textViewLoginSkip).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (Utility.checkInternet(MainActivity.this))
				{
					showSpeech();
				}
				else
				{
					DialogHandler.showNetworkError(MainActivity.this, false, handler);
				}
			}
		});
	}

	private void showSpeech()
	{
		setContentView(R.layout.activity_main);
		btnSpeak = (ImageButton) this.findViewById(R.id.btnSpeak);
		btnSpeak.setOnClickListener(itemClick);
		tvSpeech = (TextView) this.findViewById(R.id.txtSpeechInput);

		mainApplication.setOnRecognitionResultListener(RecognitionListener);
		progressBar = (ProgressBar) findViewById(R.id.progressBarSpeech);
		progressBar.setMax(10);
		mainApplication.setOnRmsResultListener(rmsResult);
		mainApplication.setOnPartialResultListener(partialResult);

		httpClient = new HttpClient();
		httpClient.setOnHttpResponseListener(httpResponse);
	}

	private void showFacebookLogin()
	{
		Logs.showTrace("Facebook Login Start");

		facebook = new FacebookHandler(this);
		facebook.init();
		facebook.setOnFacebookLoginResultListener(new FacebookHandler.OnFacebookLoginResult()
		{
			@Override
			public void onLoginResult(String strFBID, String strName, String strEmail, int nErrorCode, String strError)
			{
				Logs.showTrace("get facebook FBID: " + strFBID + " Token: " + facebook.getToken());
				switch (nErrorCode)
				{
				case FacebookHandler.ERR_SUCCESS:
				case FacebookHandler.ERR_SIGNED:
					showSpeech();
					break;
				case FacebookHandler.ERR_EXCEPTION:
					break;
				case FacebookHandler.ERR_CANCEL:
					DialogHandler.showAlert(MainActivity.this, strError, false, handler);
					break;

				}
			}
		});
		facebook.login();
	}

	OnClickListener			itemClick			= new OnClickListener()
												{
													@Override
													public void onClick(View v)
													{
														if (v.getId() == R.id.btnSpeak)
														{
															//mbSpeak = mbSpeak ? false : true;

															if (!mbSpeak)
															{
																tvSpeech.setText("");
																mbSpeak = true;
																progressBar.setIndeterminate(false);
																btnSpeak.setImageResource(R.drawable.mic_on);
																mainApplication.speechStart();
																handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);

															}
														}
													}
												};

	OnRecognitionResult		RecognitionListener	= new OnRecognitionResult()
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
															mainApplication.speechStart();
															handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);
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

															// get first string
															strText = listResult.get(0);

															tvSpeech.setText(strText);

															httpClient.httpPostRaw(777, TARGET_HOST + PATH_API_JIEBA,
																	strText);
														}
														Logs.showTrace(strText);

													}
												};

	OnRmsResult				rmsResult			= new OnRmsResult()
												{

													@Override
													public void onRms(float fRms)
													{
														progressBar.setProgress((int) fRms);
														//Logs.showTrace(String.valueOf(fRms));
													}

												};

	OnPartialResult			partialResult		= new OnPartialResult()
												{

													@Override
													public void onPartialResult(String strResult)
													{
														Logs.showTrace("Partial Result: " + strResult);
													}

												};

	@SuppressLint("HandlerLeak")
	private Handler			handler				= new Handler()
												{

													@Override
													public void handleMessage(Message msg)
													{
														if (msg.what == 666)
														{
															mbSpeak = false;
															btnSpeak.setImageResource(R.drawable.mic_off);
															mainApplication.speechStop();
															//	tvSpeech.setText(strText);
															progressBar.setIndeterminate(true);
														}

														if (msg.what == 777)
														{
															tvSpeech.setText((String) msg.obj);
														}
													}

												};

	HttpResponseListener	httpResponse		= new HttpResponseListener()
												{

													@Override
													public void response(int nId, int nCode, String strContent)
													{
														Logs.showTrace("HTTP RESPONSE - Code:" + String.valueOf(nCode)
																+ " Content:" + strContent + " ID: "
																+ String.valueOf(nId));

														if (200 == nCode)
														{
															Message msg = new Message();
															msg.what = 777;
															msg.obj = strContent;
															handler.sendMessage(msg);

														}
													}

												};
}

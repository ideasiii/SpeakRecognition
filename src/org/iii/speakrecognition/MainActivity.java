/**
 * 中文編碼用
 */

package org.iii.speakrecognition;

import java.util.HashMap;
import java.util.Map;
import org.iii.speakrecognition.HttpClient.HttpResponseListener;
import org.iii.speakrecognition.VoiceRecognition.OnPartialResult;
import org.iii.speakrecognition.VoiceRecognition.OnRecognitionResult;
import org.iii.speakrecognition.VoiceRecognition.OnRmsResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import sdk.ideas.common.CtrlType;
import sdk.ideas.common.Logs;
import sdk.ideas.common.OnCallbackResult;
import sdk.ideas.common.ResponseCode;

public class MainActivity extends Activity
{
	private ImageButton		btnSpeak							= null;
	private TextView		tvSpeech							= null;
	private boolean			mbSpeak								= false;
	private String			strText								= "";
	private ProgressBar		progressBar;
	private FacebookHandler	facebook							= null;
	private MainApplication	mainApplication						= null;
	final private int		TIMEOUT_SPEECH						= 1000;																																		// million
	// seconds
	private HttpClient		httpClient							= null;
	private final String	TARGET_HOST							= "http://jieba.srm.pw";
	private final String	PATH_API_JIEBA						= "/jieba/pos";
	private String			mstrFBID							= null;

	OnCallbackResult		permissionOnCallbackResultListener	= new OnCallbackResult()
																{

																	@Override
																	public void onCallbackResult(int result, int what,
																			int from, HashMap<String, String> message)
																	{
																		boolean needAskAgainRequest = false;
																		boolean neverAskAgain = false;
																		for (Map.Entry<String, String> map : message
																				.entrySet())
																		{
																			if (map.getValue().equals("0"))
																			{
																				needAskAgainRequest = true;
																			}
																			else if (map.getValue().equals("-1"))
																			{
																				neverAskAgain = true;
																			}
																		}
																		if (needAskAgainRequest == true)
																		{
																			showMessageOKCancel(
																					"You need to allow Permissions",
																					new DialogInterface.OnClickListener()
																																				{
																																					@Override
																																					public void onClick(
																																							DialogInterface dialog,
																																							int which)
																																					{
																																						mainApplication
																																								.startRequestPermissions(
																																										MainActivity.this,
																																										permissionOnCallbackResultListener);
																																					}
																																				});
																		}
																		else if (neverAskAgain == true)
																		{
																			showMessageOKCancel(
																					"由於你勾選never permission ask, 本程式無法再執行下去QAQ",
																					new DialogInterface.OnClickListener()
																																				{
																																					@Override
																																					public void onClick(
																																							DialogInterface dialog,
																																							int which)
																																					{
																																						MainActivity.this
																																								.finish();
																																					}
																																				});
																		}
																	}

																};

	private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
	{
		new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("OK", okListener).create()
				.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		mainApplication = (MainApplication) this.getApplication();

		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();

		mainApplication.startRequestPermissions(this, permissionOnCallbackResultListener);

		showLogin();

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		mainApplication.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

		mainApplication.setTTSOnCallbackResultListener(ttsOnCallbackResultListener);
		mainApplication.setTTSInit();

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
				{
					showSpeech();
					HashMap<String, String> message = new HashMap<String, String>();
					mstrFBID = strFBID;
					message.put("app_id", "1682812001970032");
					message.put("user_id", strFBID);
					message.put("accesstoken", facebook.getToken());
					httpClient.httpPost(999, "http://api.ser.ideas.iii.org.tw/api/fb_app_preference/accesstoken",
							message);

				}
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

	OnCallbackResult		ttsOnCallbackResultListener	= new OnCallbackResult()
														{

															@Override
															public void onCallbackResult(int result, int what, int from,
																	HashMap<String, String> message)
															{
																switch (what)
																{
																case CtrlType.MSG_RESPONSE_TEXT_TO_SPEECH_HANDLER:
																	Logs.showTrace("result:" + result + " what:" + what
																			+ " from:" + from + " message:" + message);

																	if (from == ResponseCode.METHOD_TEXT_TO_SPEECH_INIT)
																	{
																		switch (result)
																		{
																		case ResponseCode.ERR_PACKAGE_NOT_FIND:
																			// Android Google TTS APK 沒有安裝

																			break;
																		case ResponseCode.ERR_FILE_NOT_FOUND_EXCEPTION:
																			// 離線包未下載，而且無網路可以線上轉語音

																			break;
																		case ResponseCode.ERR_UNKNOWN:
																			// 未知錯誤

																			break;
																		case ResponseCode.ERR_SUCCESS:
																			// 成功初始化，能進行下一步驟

																			break;
																		default:

																			break;

																		}

																	}
																	else if (from == ResponseCode.METHOD_TEXT_TO_SPEECH_SPEECHING)
																	{
																		switch (what)
																		{
																		case ResponseCode.ERR_NOT_INIT:
																			// 尚未初始化
																			mainApplication.setTTSInit();
																			break;
																		case ResponseCode.ERR_UNKNOWN:
																			// 未知錯誤

																			break;

																		case ResponseCode.ERR_SUCCESS:
																			// 成功

																			String textID = message.get("TextID");

																			//語音強制中斷
																			if (message.get("TextStatus")
																					.equals("STOP"))
																			{

																			}
																			//語音開始
																			else if (message.get("TextStatus")
																					.equals("START"))
																			{

																			}
																			//語音結束
																			else if (message.get("TextStatus")
																					.equals("DONE"))
																			{

																			}

																			break;
																		default:

																			break;

																		}

																	}

																	break;
																default:

																	break;
																}

															}

														};

	OnClickListener			itemClick					= new OnClickListener()
														{
															@Override
															public void onClick(View v)
															{
																if (v.getId() == R.id.btnSpeak)
																{
																	// mbSpeak = mbSpeak
																	// ? false : true;

																	if (!mbSpeak)
																	{
																		tvSpeech.setText("");
																		mbSpeak = true;
																		progressBar.setIndeterminate(false);
																		btnSpeak.setImageResource(R.drawable.mic_on);
																		mainApplication.speechStart();
																		// handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);

																	}
																}
															}
														};

	OnRecognitionResult		RecognitionListener			= new OnRecognitionResult()
														{
															@Override
															public void onRecognitionResult(int nErrorCode,
																	SparseArray<String> listResult)
															{
																if (7878 == nErrorCode)																		// speech end
																{
																	handler.sendEmptyMessageDelayed(666,
																			TIMEOUT_SPEECH);
																	return;
																}

																//tvSpeech.setText("");
																strText = "";
																for (int i = 0; i < listResult.size(); ++i)
																{
																	strText += listResult.get(i);
																	strText += "\n";
																}

																//tvSpeech.setText(strText);
																//handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);
																Logs.showTrace(strText);

																// if (SpeechRecognizer.ERROR_CLIENT == nErrorCode)
																// {
																// progressBar.setIndeterminate(false);
																// tvSpeech.setText("無法辨識");
																// btnSpeak.setImageResource(R.drawable.mic_on);
																// mainApplication.speechStart();
																// handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);
																// return;
																// }
																//
																// if (SpeechRecognizer.ERROR_RECOGNIZER_BUSY == nErrorCode)
																// {
																// return;
																// }
															}
														};

	OnRmsResult				rmsResult					= new OnRmsResult()
														{

															@Override
															public void onRms(float fRms)
															{
																progressBar.setProgress((int) fRms);
																// Logs.showTrace(String.valueOf(fRms));
															}

														};

	OnPartialResult			partialResult				= new OnPartialResult()
														{

															@Override
															public void onPartialResult(String strResult)
															{

																if (null != strResult && 0 < strResult.length())
																{
																	Logs.showTrace("Partial Result: " + strResult);

																	/********************test case Start***************************/
																	//Logs.showTrace("@@@@@@Start to Speech@@@@@@@");
																	String textID = "001";
																	//	mainApplication.textToSpeech(textID,strResult);
																	/********************test case End***************************/

																	String strLowResult = strResult.toLowerCase();
																	Logs.showTrace("low case result: " + strLowResult);

																	if (strLowResult.contains("推薦文章"))
																	{
																		//https://api.ser.ideas.iii.org.tw/api/fb_app_preference/accesstoken
																		HashMap<String, String> mapParameters = new HashMap<String, String>();
																		mapParameters.put("app_id", "1682812001970032");
																		if (null == mstrFBID)
																			mstrFBID = "test";
																		mapParameters.put("user_id", mstrFBID);
																		mapParameters.put("mode", "user");
																		mapParameters.put("limit", "3");
																		httpClient.httpGet(888,
																				"http://api.ser.ideas.iii.org.tw/api/fb_app_preference/user_recommendation",
																				mapParameters);
																		handler.sendEmptyMessageDelayed(666,
																				TIMEOUT_SPEECH);
																		return;

																	}
																	if ((strLowResult.contains("ok google")
																			&& strLowResult.contains("google ok"))
																			|| ((strLowResult.contains("ok htc")
																					&& strLowResult.contains("htc ok")))
																			|| ((strLowResult.contains("ok 宏達電")
																					&& strLowResult
																							.contains("宏達電 ok"))))
																	{
																		handler.sendEmptyMessageDelayed(666,
																				TIMEOUT_SPEECH);
																		String strReplace = strLowResult
																				.replace("ok google", "")
																				.replace("google ok", "")
																				.replace("htc ok", "")
																				.replace("ok htc", "")
																				.replace("宏達電 ok", "")
																				.replace("ok 宏達電", "");

																		if (0 < strReplace.length())
																		{
																			Logs.showTrace("Parse: " + strReplace);
																			httpClient.httpPostRaw(777,
																					TARGET_HOST + PATH_API_JIEBA,
																					strReplace);

																			//mainApplication.textToSpeech(textID, strReplace);
																		}
																	}
																}
															}

														};

	@SuppressLint("HandlerLeak")
	private Handler			handler						= new Handler()
														{

															@Override
															public void handleMessage(Message msg)
															{
																if (msg.what == 666)
																{
																	mbSpeak = false;
																	btnSpeak.setImageResource(R.drawable.mic_off);
																	mainApplication.speechStop();
																	progressBar.setIndeterminate(true);

																	// restart
																	//tvSpeech.setText("");
																	mbSpeak = true;
																	progressBar.setIndeterminate(false);
																	btnSpeak.setImageResource(R.drawable.mic_on);
																	mainApplication.speechStart();
																}

																if (msg.what == 777)
																{
																	try
																	{

																		JSONArray jarry = new JSONArray(
																				(String) msg.obj);
																		tvSpeech.setText(jarry.toString());
																		Logs.showTrace(jarry.toString());
																		String strAnswer = Semantics
																				.parser((String) msg.obj);
																		Logs.showTrace("Answer: " + strAnswer);
																		mainApplication.textToSpeech("001", strAnswer);
																	}
																	catch (Exception e)
																	{
																		e.printStackTrace();
																		Logs.showError(e.getMessage());
																	}

																}

																if (888 == msg.what)
																{
																	try
																	{

																		JSONObject jobj = new JSONObject(
																				(String) msg.obj);
																		tvSpeech.setText(jobj.toString());
																		Logs.showTrace(jobj.toString());
																		JSONArray jarray = jobj.getJSONArray("result");
																		JSONObject cont = jarray.getJSONObject(0);
																		String strContent = cont.getString("content");
																		mainApplication.textToSpeech("001", strContent);

																	}
																	catch (Exception e)
																	{
																		e.printStackTrace();
																		Logs.showError(e.getMessage());
																	}
																}

																if (999 == msg.what)
																{

																}
															}

														};

	HttpResponseListener	httpResponse				= new HttpResponseListener()
														{

															@Override
															public void response(int nId, int nCode, String strContent)
															{
																Logs.showTrace("HTTP RESPONSE - Code:"
																		+ String.valueOf(nCode) + " Content:"
																		+ strContent + " ID: " + String.valueOf(nId));

																if (200 == nCode)
																{
																	Message msg = new Message();
																	msg.what = nId;
																	msg.obj = strContent;
																	handler.sendMessage(msg);

																}
															}

														};
}

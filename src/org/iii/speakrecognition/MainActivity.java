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
	private ImageButton btnSpeak = null;
	private TextView tvSpeech = null;
	private boolean mbSpeak = false;
	private String strText = "";
	private ProgressBar progressBar;
	private FacebookHandler facebook = null;
	private MainApplication mainApplication = null;
	final private int TIMEOUT_SPEECH = 1000; // million
	// seconds
	private HttpClient httpClient = null;
	private final String TARGET_HOST = "http://jieba.srm.pw";
	private final String PATH_API_JIEBA = "/jieba/pos";

	OnCallbackResult permissionOnCallbackResultListener = new OnCallbackResult()
	{

		@Override
		public void onCallbackResult(int result, int what, int from, HashMap<String, String> message)
		{
			boolean needAskAgainRequest = false;
			boolean neverAskAgain = false;
			for (Map.Entry<String, String> map : message.entrySet())
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
				showMessageOKCancel("You need to allow Permissions", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						mainApplication.startRequestPermissions(MainActivity.this, permissionOnCallbackResultListener);
					}
				});
			}
			else if (neverAskAgain == true)
			{
				showMessageOKCancel("由於你勾選never permission ask, 本程式無法再執行下去QAQ", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						MainActivity.this.finish();
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

	OnCallbackResult ttsOnCallbackResultListener = new OnCallbackResult()
	{

		@Override
		public void onCallbackResult(int result, int what, int from, HashMap<String, String> message)
		{
			switch (what)
			{
			case CtrlType.MSG_RESPONSE_TEXT_TO_SPEECH_HANDLER:
				Logs.showTrace("result:" + result + " what:" + what + " from:" + from + " message:" + message);

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
						if (message.get("TextStatus").equals("STOP"))
						{
							
						}
						//語音開始
						else if (message.get("TextStatus").equals("START"))
						{
							
						}
						//語音結束
						else if (message.get("TextStatus").equals("DONE"))
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

	OnClickListener itemClick = new OnClickListener()
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

	OnRecognitionResult RecognitionListener = new OnRecognitionResult()
	{
		@Override
		public void onRecognitionResult(int nErrorCode, SparseArray<String> listResult)
		{
			tvSpeech.setText("");
			strText = "";
			for (int i = 0; i < listResult.size(); ++i)
			{
				strText += listResult.get(i);
				strText += "\n";
			}

			if (0 == nErrorCode)
			{
				// parse first string
				// httpClient.httpPostRaw(777, TARGET_HOST + PATH_API_JIEBA,
				// listResult.get(0));
			}
			tvSpeech.setText(strText);
			handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);
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

	OnRmsResult rmsResult = new OnRmsResult()
	{

		@Override
		public void onRms(float fRms)
		{
			progressBar.setProgress((int) fRms);
			// Logs.showTrace(String.valueOf(fRms));
		}

	};

	OnPartialResult partialResult = new OnPartialResult()
	{

		@Override
		public void onPartialResult(String strResult)
		{

			if (null != strResult && 0 < strResult.length())
			{
				Logs.showTrace("Partial Result: " + strResult);
				
				
				
				/********************test case Start***************************/
				Logs.showTrace("@@@@@@Start to Speech@@@@@@@");
				String textID = "001";
				mainApplication.textToSpeech(textID,strResult);
				/********************test case End***************************/
				
				
				
				
				if (strResult.contains("Google OK") || strResult.contains("Google ok"))
				{
					handler.sendEmptyMessageDelayed(666, TIMEOUT_SPEECH);
					String strReplace = strResult.replace("OK Google", "");
					strReplace = strResult.replace("ok Google", "");
					strReplace = strReplace.replace("Google OK", "");
					strReplace = strReplace.replace("Google ok", "");
					Logs.showTrace("Parse: " + strReplace);
					httpClient.httpPostRaw(777, TARGET_HOST + PATH_API_JIEBA, strReplace);
				}
			}
		}

	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler()
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
				tvSpeech.setText("");
				mbSpeak = true;
				progressBar.setIndeterminate(false);
				btnSpeak.setImageResource(R.drawable.mic_on);
				mainApplication.speechStart();
			}

			if (msg.what == 777)
			{
				try
				{
					JSONArray jarry = new JSONArray((String) msg.obj);
					tvSpeech.setText(jarry.toString());
					Logs.showTrace(jarry.toString());

				}
				catch (Exception e)
				{
					// TODO
					// Auto-generated
					// catch block
					e.printStackTrace();
					Logs.showError(e.getMessage());
				}

			}
		}

	};

	HttpResponseListener httpResponse = new HttpResponseListener()
	{

		@Override
		public void response(int nId, int nCode, String strContent)
		{
			Logs.showTrace("HTTP RESPONSE - Code:" + String.valueOf(nCode) + " Content:" + strContent + " ID: "
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

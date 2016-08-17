package org.iii.speakrecognition;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.SparseArray;

public class VoiceRecognition implements RecognitionListener
{
	static private VoiceRecognition	voiceRecognition	= null;
	private String					LOG_TAG				= "VoiceRecognition";
	private SpeechRecognizer		speech				= null;
	private Intent					recognizerIntent;
	private OnRecognitionResult		RecognitionResult	= null;
	private Context					theContext			= null;

	/**
	 * Speech result callback.
	 */
	public static interface OnRecognitionResult
	{
		void onRecognitionResult(final int nErrorCode, final SparseArray<String> listResult);
	}

	public void setOnRecognitionResultListener(OnRecognitionResult listener)
	{
		RecognitionResult = listener;
	}

	private void callbackRecognitionResult(final int nErrorCode, final SparseArray<String> listResult)
	{
		if (null != RecognitionResult)
		{
			RecognitionResult.onRecognitionResult(nErrorCode, listResult);
		}
	}

	private VoiceRecognition(Context context)
	{
		theContext = context;
	}

	public void init(Context context)
	{
		Log.i(LOG_TAG, "speech init");
		if (null != speech)
		{
			speech.stopListening();
			speech.destroy();
		}
		promptSpeechInput();
		speech = SpeechRecognizer.createSpeechRecognizer(context);
		speech.setRecognitionListener(this);
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
	}

	static VoiceRecognition getInstance(Context context)
	{
		if (null == voiceRecognition)
		{
			voiceRecognition = new VoiceRecognition(context);
		}

		return voiceRecognition;
	}

	public void start()
	{
		if (speech != null)
		{
			speech.startListening(recognizerIntent);
			Log.i(LOG_TAG, "speech start");
		}
		else
		{
			init(theContext);
		}
	}

	public void stop()
	{
		if (speech != null)
		{
			speech.stopListening();
			Log.i(LOG_TAG, "speech stop");
		}
	}

	public void destroy()
	{
		if (speech != null)
		{
			speech.cancel();
			speech.destroy();
			speech = null;
			Log.i(LOG_TAG, "destroy");
		}

	}

	@Override
	public void onReadyForSpeech(Bundle params)
	{
		Log.i(LOG_TAG, "onReadyForSpeech");

	}

	@Override
	public void onBeginningOfSpeech()
	{
		Log.i(LOG_TAG, "onBeginningOfSpeech");
	}

	@Override
	public void onRmsChanged(float rmsdB)
	{
		// Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
	}

	@Override
	public void onBufferReceived(byte[] buffer)
	{
		Log.i(LOG_TAG, "onBufferReceived: " + buffer);
	}

	@Override
	public void onEndOfSpeech()
	{
		Log.i(LOG_TAG, "onEndOfSpeech");
	}

	@Override
	public void onError(int error)
	{
		String errorMessage = getErrorText(error);
		Log.d(LOG_TAG, "FAILED " + errorMessage);
		SparseArray<String> listResult = new SparseArray<String>();
		listResult.put(0, "»y­µ¿ëÃÑ¥¢±Ñ");
		listResult.put(1, errorMessage);
		callbackRecognitionResult(error, listResult);
	}

	@Override
	public void onResults(Bundle results)
	{
		Log.i(LOG_TAG, "onResults");
		SparseArray<String> listResult = new SparseArray<String>();
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String text = "";
		for (String result : matches)
		{
			text += result + "\n";
			listResult.put(listResult.size(), result);
		}

		callbackRecognitionResult(0, listResult);

		Log.i(LOG_TAG, text);
		listResult = null;

	}

	@Override
	public void onPartialResults(Bundle partialResults)
	{
		Log.i(LOG_TAG, "onPartialResults");

	}

	@Override
	public void onEvent(int eventType, Bundle params)
	{
		Log.i(LOG_TAG, "onEvent");

	}

	public static String getErrorText(int errorCode)
	{
		String message;
		switch (errorCode)
		{
		case SpeechRecognizer.ERROR_AUDIO:
			message = "Audio recording error";
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			message = "Client side error";
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			message = "Insufficient permissions";
			break;
		case SpeechRecognizer.ERROR_NETWORK:
			message = "Network error";
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			message = "Network timeout";
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			message = "No match";
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			message = "RecognitionService busy";
			break;
		case SpeechRecognizer.ERROR_SERVER:
			message = "error from server";
			break;
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			message = "No speech input";
			break;
		default:
			message = "Didn't understand, please try again.";
			break;
		}
		return message;
	}

	/**
	 * Showing google speech input dialog
	 * */
	private void promptSpeechInput()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
	}
}

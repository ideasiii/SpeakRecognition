/**
 * @author Louis Ju
 * @since 2016-02-15
 * @note:this is pure java code, if it is used for android that must add uses-permission in manifest file.
 * @permission: <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 * @permission: <uses-permission android:name="android.permission.INTERNET"/>
 */
package org.iii.speakrecognition;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpClient
{
	private ArrayList<HttpResponseListener>	listResponseListener	= null;
	private int								mnReadTimeout			= 5000;
	private int								mnConnectTimeout		= 10000;

	enum HTTP_METHOD
	{
		POST, GET
	}

	enum POST_DATA_TYPE
	{
		FORM_DATA, X_WWW_FORM, RAW, BINARY
	}

	public static interface HttpResponseListener
	{
		public void response(final int nId, final int nCode, final String strContent);
	}

	/**
	 * 
	 * @param listener
	 *            : HttpResponseListener
	 */
	public void setOnHttpResponseListener(HttpResponseListener listener)
	{
		if (null != listener)
		{
			listResponseListener.add(listener);
		}
	}

	private class HttpResponse
	{
		public int		nCode;
		public String	strContent;
	}

	public HttpClient()
	{
		listResponseListener = new ArrayList<HttpResponseListener>();
	}

	@Override
	protected void finalize() throws Throwable
	{
		listResponseListener.clear();
		listResponseListener = null;
		super.finalize();
	}

	private void callback(final int nId, final int nCode, final String strContent)
	{
		for (int i = 0; i < listResponseListener.size(); ++i)
		{
			listResponseListener.get(i).response(nId, nCode, strContent);
		}
	}

	/**
	 * 
	 * @param nId
	 *            : callback id
	 * @param strTargetURL
	 *            : HTTP target URL
	 * @param mapParameters
	 *            : HTTP POST method parameters
	 */
	public void httpPost(final int nId, final String strTargetURL, final HashMap<String, String> mapParameters)
	{
		if (null == strTargetURL)
		{
			callback(nId, -1, "Invalid Target URL");
		}
		else
		{
			try
			{
				HttpThread httpThread = new HttpThread(nId, HTTP_METHOD.POST, strTargetURL, mapParameters);
				httpThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param nId
	 *            : callback id
	 * @param strTargetURL
	 *            : HTTP target URL
	 * @param strData
	 *            : Raw Data
	 */
	public void httpPostRaw(final int nId, final String strTargetURL, final String strData)
	{
		if (null == strTargetURL)
		{
			callback(nId, -1, "Invalid Target URL");
		}
		else
		{
			try
			{
				HttpThread httpThread = new HttpThread(nId, HTTP_METHOD.POST, strTargetURL, strData);
				httpThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param nId
	 *            : callback id
	 * @param strTargetURL
	 *            : Target URL
	 * @param mapParameters
	 *            : HTTP GET method parameters
	 */
	public void httpGet(final int nId, final String strTargetURL, final HashMap<String, String> mapParameters)
	{
		if (null == strTargetURL)
		{
			callback(nId, -1, "Invalid Target URL");
		}
		else
		{
			try
			{
				HttpThread httpThread = new HttpThread(nId, HTTP_METHOD.GET, strTargetURL, mapParameters);
				httpThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private class HttpThread extends Thread
	{
		private POST_DATA_TYPE	mpostDataType;
		private HTTP_METHOD		method;
		private String			mstrTargetURL	= null;
		private String			mstrParameters	= null;
		private int				mnId			= -1;

		@Override
		public void run()
		{
			HttpResponse httpResponse = new HttpResponse();
			switch (method)
			{
			case POST:
				System.out.println("HTTP POST:" + mstrTargetURL + " PARAMETERS:" + mstrParameters);
				runPOST(mstrTargetURL, mstrParameters, httpResponse, mpostDataType);
				callback(mnId, httpResponse.nCode, httpResponse.strContent);
				break;
			case GET:
				System.out.println("HTTP GET:" + mstrTargetURL + "?" + mstrParameters);
				runGET(mstrTargetURL, mstrParameters, httpResponse);
				callback(mnId, httpResponse.nCode, httpResponse.strContent);
				break;
			}
		}

		public HttpThread(final int nId, final HTTP_METHOD enuMethod, final String strTargetURL,
				final HashMap<String, String> mapParameters) throws UnsupportedEncodingException
		{
			mnId = nId;
			method = enuMethod;
			mstrTargetURL = strTargetURL;
			mpostDataType = POST_DATA_TYPE.X_WWW_FORM;
			boolean bFirst = true;
			String strKey = null;
			String strValue = null;
			for (Object key : mapParameters.keySet())
			{
				strKey = ((String) key) + "=";
				strValue = mapParameters.get(key);

				if (bFirst)
				{
					bFirst = false;
					mstrParameters = strKey + URLEncoder.encode(strValue, "UTF-8");
				}
				else
				{
					mstrParameters = mstrParameters + "&" + strKey + URLEncoder.encode(strValue, "UTF-8");
				}
			}
		}

		public HttpThread(final int nId, final HTTP_METHOD enuMethod, final String strTargetURL,
				final String strRawData) throws UnsupportedEncodingException
		{
			mnId = nId;
			method = enuMethod;
			mstrTargetURL = strTargetURL;
			mstrParameters = strRawData;
			mpostDataType = POST_DATA_TYPE.RAW;
		}
	}

	private int runPOST(final String strTargetURL, final String strParameters, HttpResponse httpResponse,
			final POST_DATA_TYPE postDataType)
	{
		URL url;
		HttpURLConnection connection = null;
		httpResponse.nCode = -1;

		try
		{
			url = new URL(strTargetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setReadTimeout(mnReadTimeout);
			connection.setConnectTimeout(mnConnectTimeout);

			switch (postDataType)
			{
			case X_WWW_FORM:
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", "" + Integer.toString(strParameters.getBytes().length));
				break;
			case RAW:
				connection.setRequestProperty("Content-Type", "application/raw; charset=UTF-8");
				break;
			default:

				break;
			}
			connection.setRequestProperty("Content-Language", "UTF-8");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			//wr.writeBytes(strParameters);
			wr.write(strParameters.getBytes());
			wr.flush();
			wr.close();

			httpResponse.nCode = connection.getResponseCode();
			InputStream is = connection.getInputStream();
			httpResponse.strContent = convertStreamToString(is, "UTF-8");
		}
		catch (Exception e)
		{
			httpResponse.strContent = e.getMessage();
			e.printStackTrace();
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		return httpResponse.nCode;

	}

	private int runGET(final String strTargetURL, final String strParameters, HttpResponse httpResponse)
	{
		URL url;
		HttpURLConnection connection = null;
		httpResponse.nCode = -1;
		String strURL = strTargetURL + "?" + strParameters;
		try
		{
			url = new URL(strURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(mnReadTimeout);
			connection.setConnectTimeout(mnConnectTimeout);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Language", "UTF-8");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			httpResponse.nCode = connection.getResponseCode();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();

			httpResponse.strContent = response.toString();
		}
		catch (Exception e)
		{
			httpResponse.strContent = e.getMessage();
			e.printStackTrace();
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}

		return httpResponse.nCode;
	}

	public void setReadTimeout(final int nMilliSecond)
	{
		mnReadTimeout = nMilliSecond;
	}

	public void setConnectTimeout(final int nMilliSecond)
	{
		mnConnectTimeout = nMilliSecond;
	}

	private static String convertStreamToString(InputStream is, String encoding)
	{
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(is, encoding));
		}
		catch (UnsupportedEncodingException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();

		String line;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}

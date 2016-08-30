package org.iii.speakrecognition;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.ImageView;

public abstract class Utility
{

	static class ImageFromUrl extends AsyncTask<String, Integer, Bitmap>
	{
		ImageView	imgView	= null;
		String		strURL	= null;

		public ImageFromUrl(ImageView imageView)
		{
			imgView = imageView;
		}

		protected Bitmap doInBackground(String... strUrl)
		{
			strURL = strUrl[0];
			Bitmap bitmap = null;
			try
			{
				URL url = new URL(strURL);
				URLConnection conn = url.openConnection();
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setRequestMethod("GET");
				httpConn.connect();

				if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					InputStream inputStream = httpConn.getInputStream();
					bitmap = BitmapFactory.decodeStream(inputStream);
					inputStream.close();
				}
			}
			catch (MalformedURLException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return bitmap;
		}

		protected void onPostExecute(Bitmap bitmap)
		{
			if (null != bitmap && null != imgView)
			{
				imgView.setImageBitmap(bitmap);
			}
		}
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx)
	{
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static boolean isValidStr(String strStr)
	{
		if (null != strStr && 0 < strStr.trim().length())
			return true;
		return false;
	}

	public static String convertNull(String strStr)
	{
		String strValue = strStr;
		if (null == strValue)
		{
			strValue = "";
		}
		return strValue;
	}

	/** convert null string to default string **/
	public static String convertNull(String strStr, String strDefault)
	{
		String strValue = strStr;

		if (!isValidStr(strStr))
		{
			strValue = strDefault;
		}

		return strValue;
	}

	/** Check String is numeric data type **/
	public static boolean isNumeric(String str)
	{
		if (!isValidStr(str))
			return false;
		return str.matches("[-+]?\\d*\\.?\\d+");
	}

	public static String UrlEncode(final String strText)
	{
		try
		{
			return URLEncoder.encode(convertNull(strText), "utf8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static boolean checkInternet(Context conetxt)
	{
		boolean bValid = true;
		ConnectivityManager conManager = (ConnectivityManager) conetxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networInfo = conManager.getActiveNetworkInfo();

		if (null == networInfo || !networInfo.isAvailable())
		{
			bValid = false;
		}

		return bValid;
	}

	public static String getTime()
	{
		SimpleDateFormat formatter = null;
		formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault());
		Date curDate = new Date(System.currentTimeMillis());
		return formatter.format(curDate);
	}

}

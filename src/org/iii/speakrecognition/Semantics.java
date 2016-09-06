package org.iii.speakrecognition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Semantics
{
	public static String parser(String strInput)
	{
		String strResult = "無法分析語意";
		if (null != strInput && 0 < strInput.length())
		{
			try
			{
				JSONArray jarry = new JSONArray(strInput);
				JSONObject jobj = null;
				String strWord = null;
				String strPos = null;
				String strMean = null;
				strResult = "";

				for (int i = 0; i < jarry.length(); ++i)
				{
					jobj = jarry.getJSONObject(i);
					strWord = jobj.getString("word");
					strPos = jobj.getString("pos");
					strMean = getMean(strWord, strPos);
					if (null != strMean)
						strResult += strMean;
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return strResult;
	}

	private static String getMean(String strWord, String strPos)
	{
		String strResult = null;
		if (strPos.equals("r"))
		{
			if (strWord.equals("我"))
			{
				return "你";
			}
		}

		if (strPos.equals("v"))
		{
			if (strWord.equals("要") || strWord.equals("想") || strWord.equals("叫"))
			{
				return strWord;
			}
		}

		if (strPos.equals("n"))
		{
			if (strWord.equals("車") || strWord.equals("計程車"))
			{
				return strWord;
			}
		}

		return strResult;
	}
}

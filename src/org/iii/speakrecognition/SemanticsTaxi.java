package org.iii.speakrecognition;

import sdk.ideas.common.Logs;

public abstract class SemanticsTaxi
{
	public static String semantics(String strInput)
	{
		Logs.showTrace("叫車服務");
		String strResult = "已幫您叫車到";

		int nIndex = strInput.indexOf("我在");
		if (-1 == nIndex)
		{
			String strLocation = strInput.substring(nIndex + 2);
			strResult += strLocation;
		}

		return strResult;
	}
}

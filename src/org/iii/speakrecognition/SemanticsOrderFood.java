package org.iii.speakrecognition;

import sdk.ideas.common.Logs;

public abstract class SemanticsOrderFood
{

	public static String semantics(String strInput)
	{
		Logs.showTrace("點餐服務");
		String strResult = "無法辨識語音內容，請您再說一次";

		// 情境一
		// 我想/要/吃麥當勞 幫我點/餐 1號套餐(大麥克，飲料可樂) 我在 民生東路4段133號8樓

		if (null != wantEatWhere(strInput))
		{
			Logs.showTrace("情境一");
			WordsPosition eatwhere = wantEatWhere(strInput);
			WordsPosition eat = null;
			WordsPosition person = null;

			if (null != wantEatWhat(strInput))
			{
				eat = wantEatWhat(strInput);

				if (null != personLocation(strInput))
				{
					person = personLocation(strInput);

					try
					{
						String eatWhere = strInput.substring(eatwhere.end, eat.first);
						String eatWhat = strInput.substring(eat.end, person.first);
						String personLocation = strInput.substring(person.end, strInput.length());
						String result = "已幫你在" + eatWhere + " 點餐" + eatWhat + "餐點將外送至" + personLocation;
						return result;
					}
					catch (Exception e)
					{
						Logs.showTrace(e.toString());
					}

				}
			}

		}
		// 情境二
		// 我要去 福華飯店 幫我點/餐 巨大牛肉堡
		else if (null != wantBookWhere(strInput))
		{
			Logs.showTrace("情境二");
			WordsPosition book = wantBookWhere(strInput);
			WordsPosition eat = null;
			if (null != wantEatWhat(strInput))
			{
				eat = wantEatWhat(strInput);

				try
				{
					String bookWhere = strInput.substring(book.end, eat.first);
					String eatWhat = strInput.substring(eat.end, strInput.length());
					String result = "以幫你訂位" + bookWhere + "並幫您點餐" + eatWhat;

					return result;
				}
				catch (Exception e)
				{
					Logs.showTrace(e.toString());
				}
			}
		}

		return strResult;
	}

	public static WordsPosition wantEatWhere(String str)
	{
		WordsPosition mWordsPosition = new WordsPosition();
		if (-1 != str.indexOf("想要吃"))
		{
			mWordsPosition.first = str.indexOf("想要吃");
			mWordsPosition.end = str.indexOf("想要吃") + 3;
		}
		else if (-1 != str.indexOf("要吃"))
		{
			mWordsPosition.first = str.indexOf("要吃");
			mWordsPosition.end = str.indexOf("要吃") + 2;
		}
		else if (-1 != str.indexOf("想吃"))
		{
			mWordsPosition.first = str.indexOf("想吃");
			mWordsPosition.end = str.indexOf("想吃") + 2;
		}

		if (-1 == mWordsPosition.first && -1 == mWordsPosition.end)
			return null;
		else
			return mWordsPosition;

	}

	public static WordsPosition wantEatWhat(String str)
	{
		WordsPosition mWordsPosition = new WordsPosition();

		if (-1 != str.indexOf("幫我點餐"))
		{
			mWordsPosition.first = str.indexOf("幫我點餐");
			mWordsPosition.end = str.indexOf("幫我點餐") + 4;
		}
		if (-1 != str.indexOf("幫我剪餐"))
		{
			mWordsPosition.first = str.indexOf("幫我剪餐");
			mWordsPosition.end = str.indexOf("幫我剪餐") + 4;
		}
		
		else if (-1 != str.indexOf("幫我點"))
		{
			mWordsPosition.first = str.indexOf("幫我點");
			mWordsPosition.end = str.indexOf("幫我點") + 3;
		}
		else if (-1 != str.indexOf("我想點"))
		{
			mWordsPosition.first = str.indexOf("我想點");
			mWordsPosition.end = str.indexOf("我想點") + 3;
		}
		
		else if (-1 != str.indexOf("幫我剪"))
		{
			mWordsPosition.first = str.indexOf("幫我剪");
			mWordsPosition.end = str.indexOf("幫我剪") + 3;
		}
		else if (-1 != str.indexOf("我想剪"))
		{
			mWordsPosition.first = str.indexOf("我想剪");
			mWordsPosition.end = str.indexOf("我想剪") + 3;
		}
		
		else if (-1 != str.indexOf("想點"))
		{
			mWordsPosition.first = str.indexOf("想點");
			mWordsPosition.end = str.indexOf("想點") + 2;
		}
		else if (-1 != str.indexOf("想剪"))
		{
			mWordsPosition.first = str.indexOf("想剪");
			mWordsPosition.end = str.indexOf("想剪") + 2;
		}

		if (-1 == mWordsPosition.first && -1 == mWordsPosition.end)
			return null;
		else
			return mWordsPosition;
	}

	public static WordsPosition personLocation(String str)
	{
		WordsPosition mWordsPosition = new WordsPosition();
		if (-1 != str.indexOf("我的位置在"))
		{
			mWordsPosition.first = str.indexOf("我的位置在");
			mWordsPosition.end = str.indexOf("我的位置在") + 5;
		}
		else if (-1 != str.indexOf("我的地址在"))
		{
			mWordsPosition.first = str.indexOf("我的地址在");
			mWordsPosition.end = str.indexOf("我的地址在") + 5;
		}
		else if (-1 != str.indexOf("我人在"))
		{
			mWordsPosition.first = str.indexOf("我人在");
			mWordsPosition.end = str.indexOf("我人在") + 3;
		}
		else if (-1 != str.indexOf("我們在"))
		{
			mWordsPosition.first = str.indexOf("我們在");
			mWordsPosition.end = str.indexOf("我們在") + 3;
		}
		else if (-1 != str.indexOf("我在"))
		{
			mWordsPosition.first = str.indexOf("我在");
			mWordsPosition.end = str.indexOf("我在") + 2;
		}

		if (-1 == mWordsPosition.first && -1 == mWordsPosition.end)
			return null;
		else
			return mWordsPosition;
	}

	public static WordsPosition wantBookWhere(String str)
	{
		WordsPosition mWordsPosition = new WordsPosition();
		if (-1 != str.indexOf("想要去"))
		{
			Logs.showTrace("IN 想要去");
			
			mWordsPosition.first = str.indexOf("想要去");
			mWordsPosition.end = str.indexOf("想要去") + 3;
		}
		else if (-1 != str.indexOf("要去"))
		{
			mWordsPosition.first = str.indexOf("要去");
			mWordsPosition.end = str.indexOf("要去") + 2;
		}
		else if (-1 != str.indexOf("想去"))
		{
			mWordsPosition.first = str.indexOf("想去");
			mWordsPosition.end = str.indexOf("想去") + 2;
		}

		if (-1 == mWordsPosition.first && -1 == mWordsPosition.end)
			return null;
		else
			return mWordsPosition;
	}

	static class WordsPosition
	{
		public int first = -1;
		public int end = -1;

	}

}

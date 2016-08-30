package org.iii.speakrecognition;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import sdk.ideas.common.Common;

public abstract class DialogHandler
{
	public static void showNetworkError(Context context, final boolean bTerminate, final Handler handler)
	{
		final Dialog dialog = new Dialog(context, R.style.MyDialog);
		dialog.setContentView(R.layout.dialog);

		TextView tvTitle = (TextView) dialog.findViewById(R.id.textViewDlgTitle);
		TextView tvMessage = (TextView) dialog.findViewById(R.id.textViewDlgMessage);
		tvTitle.setText(R.string.error);
		tvTitle.setTextColor(0xFFFF0000);
		tvMessage.setText(R.string.network_error);
		tvMessage.setTextColor(0xFFCCCCCC);
		Button btn = (Button) dialog.findViewById(R.id.dialog_button_ok);
		btn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
				if (bTerminate && null != handler)
					Common.postMessage(handler, MSG.FINISH, 0, 0, null);
			}
		});
		dialog.show();
	}

	public static void showAlert(Context context, final String strMsg, final boolean bTerminate, final Handler handler)
	{
		final Dialog dialog = new Dialog(context, R.style.MyDialog);
		dialog.setContentView(R.layout.dialog);

		TextView tvTitle = (TextView) dialog.findViewById(R.id.textViewDlgTitle);
		TextView tvMessage = (TextView) dialog.findViewById(R.id.textViewDlgMessage);
		tvTitle.setText(R.string.attention);
		tvMessage.setText(strMsg);
		tvTitle.setTextColor(0xFFFF0000);
		tvMessage.setTextColor(0xFFCCCCCC);
		Button btn = (Button) dialog.findViewById(R.id.dialog_button_ok);
		btn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
				if (bTerminate)
					Common.postMessage(handler, MSG.FINISH, 0, 0, null);
			}
		});
		dialog.show();
	}

	public static void showAlert(Context context, final String strMsg, final int nId, final Handler handler)
	{
		final Dialog dialog = new Dialog(context, R.style.MyDialog);
		dialog.setContentView(R.layout.dialog);
		TextView tvTitle = (TextView) dialog.findViewById(R.id.textViewDlgTitle);
		TextView tvMessage = (TextView) dialog.findViewById(R.id.textViewDlgMessage);
		tvTitle.setText(R.string.attention);
		tvMessage.setText(strMsg);
		tvTitle.setTextColor(0xFFFF0000);
		tvMessage.setTextColor(0xFFCCCCCC);
		Button btnOK = (Button) dialog.findViewById(R.id.dialog_button_ok);
		Button btnCancel = (Button) dialog.findViewById(R.id.dialog_button_cancel);
		btnCancel.setVisibility(View.VISIBLE);
		btnOK.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (MotionEvent.ACTION_DOWN == event.getAction())
				{
					Common.postMessage(handler, MSG.DIALOG_CLICKED, nId, 0, null);
				}
				return true;
			}
		});

		btnCancel.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (MotionEvent.ACTION_DOWN == event.getAction())
				{
					dialog.dismiss();
				}
				return true;
			}
		});
		dialog.show();
	}

	public static Dialog showLoading(Context context)
	{
		final Dialog dialog = new Dialog(context, R.style.DialogReward);
		dialog.setContentView(R.layout.loading);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}
}

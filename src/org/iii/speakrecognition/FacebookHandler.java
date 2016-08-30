/**
 * @author Louis Ju
 * @date 2015/9/10
 * @note �NFacebookHandler.callbackManager.onActivityResult(requestCode, resultCode, data); �[�� Activity��onActivityResult
 */

package org.iii.speakrecognition;

import java.util.Arrays;

import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import sdk.ideas.common.Logs;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

public class FacebookHandler
{
	static public final int			ERR_SUCCESS				= 0;
	static public final int			ERR_EXCEPTION			= -1;
	static public final int			ERR_CANCEL				= -2;
	static public final int			ERR_SIGNED				= -3;

	private Activity				theActivity				= null;
	public static CallbackManager	callbackManager			= CallbackManager.Factory.create();
	private AccessToken				accessToken;
	private LoginManager			loginManager;
	private OnFacebookLoginResult	onFacebookLoginResult	= null;
	private String					mstrToken				= null;

	public FacebookHandler(Activity activity)
	{
		theActivity = activity;
	}

	@Override
	protected void finalize() throws Throwable
	{
		onFacebookLoginResult = null;
		super.finalize();
	}

	public void init()
	{
		FacebookSdk.sdkInitialize(theActivity.getApplicationContext());
		loginManager = LoginManager.getInstance();
		loginManager.registerCallback(callbackManager, facebookCallback);
	}

	/**
	 * Facebook result callback.
	 */
	public static interface OnFacebookLoginResult
	{
		void onLoginResult(final String strFBID, final String strName, final String strEmail, final int nErrorCode,
				final String strError);
	}

	public void setOnFacebookLoginResultListener(FacebookHandler.OnFacebookLoginResult listener)
	{
		onFacebookLoginResult = listener;
	}

	private void callbackFacebookResult(final String strFBID, final String strName, final String strEmail,
			final int nErrCode, final String strError)
	{
		if (null != onFacebookLoginResult)
		{
			onFacebookLoginResult.onLoginResult(strFBID, strName, strEmail, nErrCode, strError);
		}
	}

	/**
	 * Show Facebook Login Activity.
	 */
	public void login()
	{
		if (null == theActivity || null == callbackManager || null == loginManager)
			return;

		loginManager.logInWithReadPermissions(theActivity, Arrays.asList("email", "public_profile", "user_likes","user_posts"));
	}

	private void callGraph(final AccessToken strToken)
	{
		if (null != strToken && !strToken.isExpired())
		{
			Logs.showTrace("Call Facebook Graph API");
			GraphRequest request = GraphRequest.newMeRequest(strToken, new GraphRequest.GraphJSONObjectCallback()
			{
				@Override
				public void onCompleted(JSONObject object, GraphResponse response)
				{
					//					Logs.showTrace("Facebook Token:" + strToken.getToken());
					//					Logs.showTrace("Facebook ID:" + object.optString("id"));
					//					Logs.showTrace("Facebook Name:" + object.optString("name"));
					//					Logs.showTrace("Facebook Email:" + object.optString("email"));
					//					Logs.showTrace("Facebook Gender:" + object.optString("gender"));
					//					Logs.showTrace("Facebook Locale:" + object.optString("locale"));
					//					Logs.showTrace("Facebook Timezone:" + object.optString("timezone"));
					//					Logs.showTrace("Facebook Update Time:" + object.optString("updated_time"));
					callbackFacebookResult(object.optString("id"), object.optString("name"), object.optString("email"),
							ERR_SUCCESS, null);
				}

			});

			Bundle parameters = new Bundle();
			parameters.putString("fields", "id,name,link,email,birthday,gender,locale,timezone,updated_time");
			request.setParameters(parameters);
			request.executeAsync();
		}
	}

	private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>()
	{

		@Override
		public void onSuccess(LoginResult loginResult)
		{
			Logs.showTrace("Facebook Login Success");
			accessToken = loginResult.getAccessToken();
			mstrToken = accessToken.getToken();
			callGraph(accessToken);
		}

		@Override
		public void onCancel()
		{
			Logs.showTrace("Facebook Login Cancel Relogin");
			LoginManager.getInstance().logOut();
			callbackFacebookResult(null, null, null, ERR_CANCEL, "Facebook Login Cancel");
		}

		@Override
		public void onError(FacebookException error)
		{
			Logs.showTrace("Facebook Exception:" + error.toString());
			if (error instanceof FacebookAuthorizationException)
			{
				if (AccessToken.getCurrentAccessToken() != null)
				{
					callbackFacebookResult(null, null, null, ERR_SIGNED, "Already Signed");
				}
			}
			else
			{
				callbackFacebookResult(null, null, null, ERR_EXCEPTION, error.toString());
			}
		}

	};

	public void logout()
	{
		if (null != LoginManager.getInstance())
			LoginManager.getInstance().logOut();
	}

	public String getToken()
	{
		return mstrToken;
	}
}

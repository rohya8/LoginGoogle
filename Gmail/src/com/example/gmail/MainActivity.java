package com.example.gmail;

import java.io.InputStream;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends AppCompatActivity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener
{

	private static final int RC_SIGN_IN = 0;

	private GoogleApiClient mGoogleApiClient;


	private boolean mIntentInProgress;
	private boolean signedInUser;
	private ConnectionResult mConnectionResult;
	private SignInButton signinButton;
	private ImageView image;
	private TextView username, emailLabel;
	private LinearLayout profileFrame, signinFrame;




	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		signinButton = (SignInButton) findViewById(R.id.signin);

		signinButton.setOnClickListener(this);
		image = (ImageView) findViewById(R.id.image);

		username = (TextView) findViewById(R.id.username);

		emailLabel = (TextView) findViewById(R.id.email);

		profileFrame = (LinearLayout) findViewById(R.id.profileFrame);
		signinFrame = (LinearLayout) findViewById(R.id.signinFrame);

		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();


	}
	protected void onStart() {

		super.onStart();

		mGoogleApiClient.connect();

	}



	protected void onStop() 
	{

		super.onStop();

		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();

		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) 
	{

		// TODO Auto-generated method stub


		if (!result.hasResolution()) 
		{

			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();

			return;
		}


		if (!mIntentInProgress) 
		{
			// store mConnectionResult

			mConnectionResult = result;

			if (signedInUser) 
			{
				resolveSignInError();

			}

		}


	}


	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {

		switch (requestCode) 
		{
		case RC_SIGN_IN:
			if (responseCode == RESULT_OK) {
				signedInUser = false;
			}
			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();

			}

			break;

		}

	}



	private void resolveSignInError() {

		if (mConnectionResult.hasResolution()) {

			try {
				mIntentInProgress = true;

				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) 
	{
		// TODO Auto-generated method stub

		signedInUser = false;

		Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();

		getProfileInformation();


	}
	@Override
	public void onConnectionSuspended(int cause) 
	{
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();

		updateProfile(false);


	}
	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.signin:

			googlePlusLogin();

			break;

		}

	}


	private void googlePlusLogin() {

		if (!mGoogleApiClient.isConnecting()) {

			signedInUser = true;

			resolveSignInError();

		}

	}

	public void signIn(View v) {

		googlePlusLogin();

	}



	public void logout(View v) {

		googlePlusLogout();

	}


	private void googlePlusLogout() {

		if (mGoogleApiClient.isConnected()) {

			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);

			mGoogleApiClient.disconnect();

			mGoogleApiClient.connect();
			updateProfile(false);

		}

	}



	private void getProfileInformation() 
	{

		try {

			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) 
			{

				Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();

				String personPhotoUrl = currentPerson.getImage().getUrl();

				String email = Plus.AccountApi.getAccountName(mGoogleApiClient);



				username.setText(personName);

				emailLabel.setText(email);



				new LoadProfileImage(image).execute(personPhotoUrl);



				// update profile frame with new info about Google Account

				// profile

				updateProfile(true);

			}

		} 
		catch (Exception e) 
		{

			e.printStackTrace();

		}


	}

	private class LoadProfileImage extends AsyncTask {

		ImageView downloadedImage;

		public LoadProfileImage(ImageView image) {
			this.downloadedImage = image;
		}

		protected Bitmap doInBackground(String... urls) {

			String url = urls[0];
			Bitmap icon = null;

			try {

				InputStream in = new java.net.URL(url).openStream();

				icon = BitmapFactory.decodeStream(in);

			} catch (Exception e) {
				Log.e("Error", e.getMessage());

				e.printStackTrace();

			}

			return icon;

		}


		protected void onPostExecute(Bitmap result) {

			downloadedImage.setImageBitmap(result);

		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			return null;
		}

	}


	private void updateProfile(boolean isSignedIn) {

		if (isSignedIn) {

			signinFrame.setVisibility(View.GONE);

			profileFrame.setVisibility(View.VISIBLE);


		} else {

			signinFrame.setVisibility(View.VISIBLE);

			profileFrame.setVisibility(View.GONE);

		}

	}


}

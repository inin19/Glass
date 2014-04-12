package com.demo.prompt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;









import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.panderasystems.glassninja.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

public class NinjaService extends Service {

	public static final String LIVE_CARD_TAG = "PromptLiveCard";
	private static final int MENU_ACTIVITY_REQUEST_CODE = 1337;
	private static final String uri = "http://inin-pc.no-ip.biz/MicroStrategy/servlet/taskAdmin?taskId=reportDataService&taskEnv=xml&taskContentType=xml&server=127.0.0.1&project=MicroStrategy+Tutorial&userid=Administrator&password=&styleName=CustomXMLReportStyle&reportID=1D5591A74999A408FE26C0AB245BA17D&valuePromptAnswers=2012";   
	
//	private static final String uri = "http://google.com";

	private LiveCard mLiveCard;
	private RemoteViews mRemoteViews;
	
	private RequestTask asyncTask;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		asyncTask =  new RequestTask();
		asyncTask.execute(uri);
		
		publishLiveCardToTimeline();
		
		return START_STICKY;
	}

	
	
	
	
	
//	@Override
//	public void onCreate() {
//		// TODO Auto-generated method stub
//		asyncTask.delegate = this;
//	}

//	@Override
//	public void processFinish(String output) {
//		publishLiveCardToTimeline();
//	}


	private class RequestTask extends AsyncTask<String, String, String>{
//		public AsyncResponse delegate=null;
		
		
		
		@Override
		protected String doInBackground(String... uri) {
			
			HttpClient httpclient = new DefaultHttpClient();

			
			
			HttpGet request = new HttpGet(uri[0]);
			HttpResponse response;
			String responseString = null;
			
			
			try {
				
//				String credentials = "googleglass" + ":" + "googleglass";
//				String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);  			
//				request.addHeader("Authorization", "Basic" + base64EncodedCredentials);

				
				
				Credentials creds = new UsernamePasswordCredentials("googleglass", "googleglass");
				((AbstractHttpClient) httpclient).getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
				
				
				Log.d("Glass","addHeader");
				

				
				
				response  = httpclient.execute(request);
				
				
				
				
				
				Log.d("Glass","After Execute");
				
				StatusLine statusLine = response.getStatusLine();
				
				
				Log.d("Glass",statusLine.toString());
				
				if(statusLine.getStatusCode() == HttpStatus.SC_OK){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                
	                Log.d("Glass",responseString.toString());
	                
				}else{
					response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
				}
				
				
				
			} catch (ClientProtocolException e) {
				Log.d("Glass","exception1");
				Log.d("Glass",e.getMessage());
				
			} catch (IOException e) {
				Log.d("Glass","exception2");
				Log.d("Glass",e.getMessage());
			}
			
//			Log.d("Glass",responseString);
			return responseString;
		}

//		@Override
//		protected void onPostExecute(String result) {
//			delegate.processFinish(result);
//		}
		
		
		
		
	}
	
	
	
	private void publishLiveCardToTimeline() {
		if(mLiveCard!=null)
			return;
		
		TimelineManager timelineManager = TimelineManager.from(this);
		mLiveCard = timelineManager.createLiveCard(LIVE_CARD_TAG);
		
		mRemoteViews = new RemoteViews(this.getPackageName(), R.layout.live_card_default);
		mLiveCard.setViews(mRemoteViews);

		Intent menuActivityIntent = new Intent(this, MenuActivity.class);
		
		mLiveCard.setAction(PendingIntent.getActivity(this, MENU_ACTIVITY_REQUEST_CODE, menuActivityIntent, 0));
		
		mLiveCard.publish(PublishMode.REVEAL);
		
	}

	@Override
	public void onDestroy() {
		if(mLiveCard!=null)
			mLiveCard.unpublish();
	}



	
	
	

}

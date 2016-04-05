package com.example.zachlister.tedtabletapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ThreadActivity extends Activity {

	private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
	private boolean CONTINUE_READ_WRITE = true;

	// this is to keep track of the the track that is being played from the app
	private int currentTrack = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thread);
		//Always make sure that Bluetooth server is discoverable during listening...
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);
        addListenerOnButton();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		android.util.Log.e("TrackingFlow", "Creating thread to start listening...");
		new Thread(reader).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(socket != null){
			try{
				is.close();
				os.close();
				socket.close();
			}catch(Exception e){}
			CONTINUE_READ_WRITE = false;
		}
	}

	private BluetoothSocket socket;
	private InputStream is;
	private OutputStreamWriter os;
	private Runnable reader = new Runnable() {
		public void run() {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			UUID uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66");
			try {
				BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord("BLTServer", uuid);
				android.util.Log.e("TrackingFlow", "Listening...");
				socket = serverSocket.accept();
				android.util.Log.e("TrackingFlow", "Socket accepted...");
				is = socket.getInputStream();
				os = new OutputStreamWriter(socket.getOutputStream());
				new Thread(writter).start();

				int bufferSize = 1024;
				int bytesRead = -1;
				byte[] buffer = new byte[bufferSize];

				//Keep reading the messages while connection is open...
				while(CONTINUE_READ_WRITE){
					final StringBuilder sb = new StringBuilder();
					bytesRead = is.read(buffer);
					if (bytesRead != -1) {
						String result = "";
						while ((bytesRead == bufferSize) && (buffer[bufferSize-1] != 0)){
							result = result + new String(buffer, 0, bytesRead - 1);
							bytesRead = is.read(buffer);
						}
						result = result + new String(buffer, 0, bytesRead - 1);
						sb.append(result);
					}
					android.util.Log.e("TrackingFlow", "Read: " + sb.toString());
					//Show message on UIThread
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//Toast.makeText(ThreadActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
							int sectionCount = 0;
							ImageView image = (ImageView) findViewById(R.id.imageView);
							String mDrawableName = "";
							String mRawAudioName1 = "";
							String mRawAudioName2 = "";
							String mRawAudioName3 = "";
							String mRawAudioName4 = "";
							String mRawAudioName5 = "";
							String readInData = sb.toString();

							// to account for a fixed byte length message being sent over bluetooth
							// the message will be comma separated for the image and audio files
							for (int i = 0; i < 50; i++) {
								if (readInData.charAt(i) == ',') {				// move to the next section
									sectionCount++;
								} else if (readInData.charAt(i) != 0) {			// if the char isn't null and not a comma
									if (sectionCount == 0) {
										mDrawableName += readInData.charAt(i);
									} else if (sectionCount == 1) {
										mRawAudioName1 += readInData.charAt(i);
									} else if (sectionCount == 2) {
										mRawAudioName2 += readInData.charAt(i);
									} else if (sectionCount == 3) {
										mRawAudioName3 += readInData.charAt(i);
									} else if (sectionCount == 4) {
										mRawAudioName4 += readInData.charAt(i);
									} else {
										mRawAudioName5 += readInData.charAt(i);
									}
								} else {										// char is null, message has ended
									break;
								}
							}

							// always guaranteed an image and 1 audio file
							int imageID = getResources().getIdentifier(mDrawableName , "drawable", getPackageName());
							int audioID1 = getResources().getIdentifier(mRawAudioName1, "raw", getPackageName());
							int audioID2 = 0;
							int audioID3 = 0;
							int audioID4 = 0;
							int audioID5 = 0;

							// if there are a second, third, fourth, and fifth audio clip, retrieve them
							if (mRawAudioName2 != null) audioID2 = getResources().getIdentifier(mRawAudioName2, "raw", getPackageName());
							if (mRawAudioName3 != null) audioID3 = getResources().getIdentifier(mRawAudioName3, "raw", getPackageName());
							if (mRawAudioName4 != null) audioID4 = getResources().getIdentifier(mRawAudioName4, "raw", getPackageName());
							if (mRawAudioName5 != null) audioID5 = getResources().getIdentifier(mRawAudioName5, "raw", getPackageName());

							// set the image on the screen
							image.setImageResource(imageID);

							// audio playing section
							final int[] tracks = new int[5]; // max number of tracks is 5
							tracks[0] = audioID1;
							tracks[1] = audioID2;
							tracks[2] = audioID3;
							tracks[3] = audioID4;
							tracks[4] = audioID5;
							final MediaPlayer mediaPlayer;
							mediaPlayer = MediaPlayer.create(getApplicationContext(), tracks[0]);			// set up the mediaplayer with the first track
							currentTrack = 1;
							mediaPlayer.start();															// play the first sound bit
							mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
								@Override
								public void onCompletion(MediaPlayer mp) {									// when it's done playing one sound, see if there is another sound to play
									mp.release();
									if (currentTrack < tracks.length && tracks[currentTrack] != 0) {		// if it's not the end of the array plus there is an actual ID of the audio track
										mp = MediaPlayer.create(getApplicationContext(), tracks[currentTrack]);
										currentTrack++;
										mp.setOnCompletionListener(this);
										mp.start();
									}
								}
							});
						}
					});
				}
			} catch (IOException e) {e.printStackTrace();}
		}
	};

	private Runnable writter = new Runnable() {

		@Override
		public void run() {
			int index = 0;
			while(CONTINUE_READ_WRITE){
				try {
					os.write("Message From Server" + (index++) + "\n");
					os.flush();
					Thread.sleep(2000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private void addListenerOnButton() {
		final ImageView image;
		Button button;

		button = (Button) findViewById(R.id.button);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), SelectionGame.class);
                startActivity(i);
			}

		});
	}
}

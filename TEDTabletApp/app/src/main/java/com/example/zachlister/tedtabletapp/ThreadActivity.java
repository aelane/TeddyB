package com.example.zachlister.tedtabletapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ThreadActivity extends Activity {

	private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
	private boolean CONTINUE_READ_WRITE = true;

	private long mLastClickTime = 0;

	// this is to keep track of the the track that is being played from the app
	private int currentTrack = 0;

	private int currentAudioFile;
	private	int currentImageFile;

	// keeps track of the progress through the lesson
	private int progress;

	private int teachingMode;
	private int numWordsInLesson;
	private String lang;

	// all of the items in the view
	private ImageView image;
	private ProgressBar bar;
	private Button repeatButton;
	private Button skipButton;
	private Button gameButton;

	// used to make sure the word "learning" is only sent once to the edison
	private boolean	ONCREATE = true;

	private Context mContext;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		if (savedInstanceState != null) {
			image = (ImageView) findViewById(R.id.imageView);
			image.setImageResource(savedInstanceState.getInt("image"));
			currentImageFile = savedInstanceState.getInt("image");
			currentAudioFile = savedInstanceState.getInt("audio");
		} else {
			// set up all of the items in the view
			setContentView(R.layout.activity_thread);
			image = (ImageView) findViewById(R.id.imageView);
			bar = (ProgressBar) findViewById(R.id.pBar);
			repeatButton = (Button) findViewById(R.id.repeat);
			skipButton = (Button) findViewById(R.id.skip);
			gameButton = (Button) findViewById(R.id.game);
			progress = 0;

			// start the Bluetooth loop
			new Thread(reader).start();

			// add all of the listeners
			addListenerOnButton();
		}
	}


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putInt("image", currentImageFile);
		savedInstanceState.putInt("audio", currentAudioFile);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		android.util.Log.e("TrackingFlow", "Creating thread to start listening...");
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!ONCREATE) new Thread(new BluetoothWriter("learning")).start(); // to avoid sending twice on the oncreate call
		ONCREATE = false;
	}

	private BluetoothSocket socket;
	private InputStream is;
	private OutputStreamWriter os;
	private Runnable reader = new Runnable() {
		public void run() {

			try {
				// get the socket from the application class that was saved from the main screen
				socket = ((TEDTablet) getApplication()).getSocket();

				// set up the input and output streams
				is = socket.getInputStream();
				os = new OutputStreamWriter(socket.getOutputStream());

				// Tell the edison to send data
				new Thread(new BluetoothWriter("learning")).start();

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

					//Show the proper pictures and play right sound bits on ui thread
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							android.util.Log.e("InsideRun", "Read: " + sb.toString());

							// while there is sound playing, disable the buttons
							disableButtons();

							// all of these are used to read the buffer
							int sectionCount = 0;
							String mDrawableName = "";
							String mRawAudioName1 = "";
							String mRawAudioName2 = "";
							String mRawAudioName3 = "";
							String mRawAudioName4 = "";
							String mRawAudioName5 = "";
							String readInData = sb.toString();

							// to account for a fixed byte length message being sent over bluetooth
							// the message will be comma separated for the image and audio files
							for (int i = 0; i < 128; i++) {
								if (readInData.charAt(i) == ',') {				// move to the next section
									sectionCount++;
								} else if (readInData.charAt(i) != 0) {			// if the char isn't null and not a comma
									if (sectionCount == 0) {
										mDrawableName += readInData.charAt(i);  // first word is the pic name
									} else if (sectionCount == 1) {
										mRawAudioName1 += readInData.charAt(i); // second word is the first audio file name
									} else if (sectionCount == 2) {
										mRawAudioName2 += readInData.charAt(i); // more audio file names
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

							// This is to process the first time to set the data for the lesson
							if (mRawAudioName1.equals("1") || mRawAudioName1.equals("2") || mRawAudioName1.equals("3")) {

								// save the language
								((TEDTablet) getApplication()).setLang(mDrawableName);
								lang = mDrawableName;

								// save the teaching mode and number of words in the lesson
								teachingMode = Integer.parseInt(mRawAudioName1);
								numWordsInLesson = Integer.parseInt(mRawAudioName2);

								// set up the progress bar
								bar.setMax(numWordsInLesson - 1);
								bar.setProgress(0);
								progress = 0;

							// This is sent when the user gets three incorrect guesses
							} else if (mDrawableName.equals("wrong")) {

								// moving on to the next word
								progress++;
								bar.incrementProgressBy(1);

								// if the end of the lesson has been reached
								if (progress == numWordsInLesson) nextLesson();

							// This is to process every other lesson part
							} else {

								// if the guess was correct, increment the bar
								if (mRawAudioName1.equals("good_job")) {
									bar.incrementProgressBy(1);
									progress++;
								}

								// get the image from drawable
								int imageID = getResources().getIdentifier(mDrawableName, "drawable", getPackageName());

								// save the file name except the xmark pic
								if (!(mDrawableName.equals("xmark"))) currentImageFile = imageID;

								// teachingMode 1 == "Repeat after me"
								// teachingMode 2 == "Foreign to English"
								// teachingMode 3 == "English to Foreign"
								// this if statement makes sure that the correct audio file is played because they may have
								// a language label at the end to differentiate words that are spelled the same in two
								// different languages
								//
								// the teaching mode depends on which sound byte is the word that is in a foreign language
								if (teachingMode == 1 ) {
									if (hasLabel(mRawAudioName3)) {
										currentAudioFile = getResources().getIdentifier(mRawAudioName3+getLabel(), "raw", getPackageName());
									} else {
										currentAudioFile = getResources().getIdentifier(mRawAudioName3, "raw", getPackageName());
									}
								} else {
									if (hasLabel(mRawAudioName2) && teachingMode == 2) {
										currentAudioFile = getResources().getIdentifier(mRawAudioName2+getLabel(), "raw", getPackageName());
									} else {
										currentAudioFile = getResources().getIdentifier(mRawAudioName2, "raw", getPackageName());
									}
								}

								// initialize all of the audioIDs
								int audioID1 = getResources().getIdentifier(mRawAudioName1, "raw", getPackageName());
								int audioID2 = 0;
								int audioID3 = 0;
								int audioID4 = 0;
								int audioID5 = 0;

								// if there are a second, third, fourth, and fifth audio clip, retrieve them
								if (mRawAudioName2 != null)
									// same as above, making sure that the right audio file is played
									if (teachingMode == 2 || teachingMode == 3 ) audioID2 = currentAudioFile;
									else audioID2 = getResources().getIdentifier(mRawAudioName2, "raw", getPackageName());
								if (mRawAudioName3 != null)
									if (teachingMode == 1) audioID3 = currentAudioFile;
									else audioID3 = getResources().getIdentifier(mRawAudioName3, "raw", getPackageName());
								if (mRawAudioName4 != null)
									audioID4 = getResources().getIdentifier(mRawAudioName4, "raw", getPackageName());
								if (mRawAudioName5 != null)
									if (teachingMode == 1) audioID5 = currentAudioFile;
									else audioID5 = getResources().getIdentifier(mRawAudioName5, "raw", getPackageName());

								// set the image on the screen
								image.setImageResource(0);
								image.setImageResource(imageID);

								// audio playing section
								final int[] tracks = new int[5]; // max number of tracks is 5
								tracks[0] = audioID1;
								tracks[1] = audioID2;
								tracks[2] = audioID3;
								tracks[3] = audioID4;
								tracks[4] = audioID5;
								final MediaPlayer mediaPlayer;
								mediaPlayer = MediaPlayer.create(mContext, tracks[0]);            // set up the mediaplayer with the first track
								currentTrack = 1;
								mediaPlayer.start();                                                            // play the first sound bit
								mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {                                    // when it's done playing one sound, see if there is another sound to play
										mp.release();
										if (currentTrack < tracks.length && tracks[currentTrack] != 0) {        // if it's not the end of the array plus there is an actual ID of the audio track
											mp = MediaPlayer.create(mContext, tracks[currentTrack]);
											currentTrack++;
											mp.setOnCompletionListener(this);
											mp.start();
										} else {
											// this enables the buttons once the sounds are done playing
											enableButtons();
										}
									}
								});

								// this is to redisplay the image associated with the word after an incorrect guess
								if (mDrawableName.equals("xmark")) {

									new Handler().postDelayed(new Runnable() {
										public void run () {
											image.setImageResource(currentImageFile);
										}
									}, 2100L); //2.1 second delay
								}
								if (progress == numWordsInLesson) nextLesson();

							}
						}
					});
				}
			} catch (IOException e) {e.printStackTrace();}
		}
	};

	// This is used to write data to the Edison over Bluetooth
    public class BluetoothWriter implements Runnable {
        String command;

        BluetoothWriter(String s) {
            command = s;
        }
        @Override
        public void run() {
            while (CONTINUE_READ_WRITE) {
                try {
                    os.write(command);
                    os.flush();
					android.util.Log.e("TrackingFlow", "Sending: " + command);
					return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

	// add all of the listeners on the buttons
	private void addListenerOnButton() {

		gameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(mContext, SelectionGame.class);
				new Thread(new BluetoothWriter("menu")).start();   // tell the edison that the user has gone into the game
				startActivity(i);
			}

		});

		repeatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final MediaPlayer mediaPlayer;
				mediaPlayer = MediaPlayer.create(mContext, currentAudioFile);	// set up the mediaplayer with the first track
				mediaPlayer.start();
			}
		});

		skipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// Tell the edison to skip this word
				new Thread(new BluetoothWriter("skip")).start();

				// disable the buttons so multiple skips cannot be played
				disableButtons();

				// update the progress bar
				ProgressBar bar = (ProgressBar) findViewById(R.id.pBar);
				bar.incrementProgressBy(1);
				progress++;

				// if that was the last word in the lesson
				if (progress == numWordsInLesson) nextLesson();
			}
		});
	}

	// creates a dialogue that tells the user that they have completed their lesson and if they want to continue
	private void nextLesson() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						//Yes button clicked
						new Thread(new BluetoothWriter("learning")).start();  // tell the Edison that the user wants to continue
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						Intent i = new Intent(mContext, SelectionGame.class);
						new Thread(new BluetoothWriter("menu")).start();   // tell the edison that the user has gone into the game
						startActivity(i);
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("Congratulations! You just finished the lesson. Want to go to the next lesson?").setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}

	// checks to see if the word has a language label on it
	private boolean hasLabel(String audioFile) {
		String[] wordsWithLabels = {"banana","do","elephant","keik","mama","papa","six","table","television"};
		for (int i = 0; i < wordsWithLabels.length; i++) {
			if (wordsWithLabels[i].equals(audioFile)) return true;
		}
		return false;
	}

	// returns the label based on the language that is being set for the system
	private String getLabel() {
		if (lang.equals("French")) return "_fr";
		else if (lang.equals("Greek")) return "_gr";
		else if (lang.equals("Spanish")) return "_sp";
		else if (lang.equals("Persian")) return "_pe";
		else return "";
	}

	// disables all buttons
	private void disableButtons() {
		gameButton.setEnabled(false);
		repeatButton.setEnabled(false);
		skipButton.setEnabled(false);
	}

	// enable all buttons
	private void enableButtons() {
		gameButton.setEnabled(true);
		repeatButton.setEnabled(true);
		skipButton.setEnabled(true);
	}

}

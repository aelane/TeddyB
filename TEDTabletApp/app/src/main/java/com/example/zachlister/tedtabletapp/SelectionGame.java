package com.example.zachlister.tedtabletapp;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SelectionGame extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            finish();
            return false;
        }
    };
    private int currentTrack = 0;

    // game stats
    private int guesses = 0;
    private int round = 0;
    private int correct = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selection_game);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.button).setOnTouchListener(mDelayHideTouchListener);

        startGame();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void startGame() {
        // set up the imagebuttons first
        final ImageButton button1, button2, button3, button4, button5, button6;
        button1 = (ImageButton) findViewById(R.id.pic1);
        button2 = (ImageButton) findViewById(R.id.pic2);
        button3 = (ImageButton) findViewById(R.id.pic3);
        button4 = (ImageButton) findViewById(R.id.pic4);
        button5 = (ImageButton) findViewById(R.id.pic5);
        button6 = (ImageButton) findViewById(R.id.pic6);

        final ImageButton[] buttons = new ImageButton[6];
        buttons[0] = button1;
        buttons[1] = button2;
        buttons[2] = button3;
        buttons[3] = button4;
        buttons[4] = button5;
        buttons[5] = button6;

        // this is the array of all of the images stored on the tablet
        TypedArray images = getResources().obtainTypedArray(R.array.images);

        // these will be the randomly selected pictures to be displayed and the chosen picture
        final int[] selectedPics = new int[6];
        final int[] chosenPic = new int[1];
        getPics(buttons, selectedPics, chosenPic);

        // this is used to delay the changing on the cards once correct
        final Handler handler = new Handler();

        // setting up all of the onClick listeners for each button
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                guesses++;                                    // a click means a guess
                if (selectedPics[0] == chosenPic[0]) {        // if the picture is the correct picture
                    correct++;                                // increment the correct number and move on to the next round
                    round++;
                    button1.setBackgroundColor(Color.GREEN);  // set the border green so it's a visual stimulant of correct
                    correct();                                // play the sound bit
                    handler.postDelayed(new Runnable() {      // this is to delay going to the next round of pictures
                        @Override
                        public void run() {
                            if (round == 10) {                // if this is the last round show the results
                                String results = "CONGRATULATIONS! Your score is: " + correct + " correct guesses out of " + guesses + " attempts";
                                Toast.makeText(SelectionGame.this, results, Toast.LENGTH_LONG).show();
                            } else {                          // else call the main function for getting new pictures
                                getPics(buttons, selectedPics, chosenPic);
                            }
                        }
                    }, 1200);

                } else {                                      // the guess is incorrect
                    button1.setBackgroundColor(Color.RED);    // change the background red
                    incorrect();                              // play the incorrect sound bit
                }
            }

        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                guesses++;
                if (selectedPics[1] == chosenPic[0]) {
                    correct++;
                    round++;
                    button2.setBackgroundColor(Color.GREEN);
                    correct();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (round == 10) {
                                String results = "CONGRATULATIONS! Your score is: " + correct + " correct guesses out of " + guesses + " attempts";
                                Toast.makeText(SelectionGame.this, results, Toast.LENGTH_LONG).show();
                            } else {
                                getPics(buttons, selectedPics, chosenPic);
                            }
                        }
                    }, 1200);
                } else {
                    button2.setBackgroundColor(Color.RED);
                    incorrect();
                }

            }

        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                guesses++;
                if (selectedPics[2] == chosenPic[0]) {
                    correct++;
                    round++;
                    button3.setBackgroundColor(Color.GREEN);
                    correct();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (round == 10) {
                                String results = "CONGRATULATIONS! Your score is: " + correct + " correct guesses out of " + guesses + " attempts";
                                Toast.makeText(SelectionGame.this, results, Toast.LENGTH_LONG).show();
                            } else {
                                getPics(buttons, selectedPics, chosenPic);
                            }
                        }
                    }, 1200);
                } else {
                    button3.setBackgroundColor(Color.RED);
                    incorrect();
                }

            }

        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                guesses++;
                if (selectedPics[3] == chosenPic[0]) {
                    correct++;
                    round++;
                    button4.setBackgroundColor(Color.GREEN);
                    correct();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (round == 10) {
                                String results = "CONGRATULATIONS! Your score is: " + correct + " correct guesses out of " + guesses + " attempts";
                                Toast.makeText(SelectionGame.this, results, Toast.LENGTH_LONG).show();
                            } else {
                                getPics(buttons, selectedPics, chosenPic);
                            }
                        }
                    }, 1200);
                } else {
                    button4.setBackgroundColor(Color.RED);
                    incorrect();
                }

            }

        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                guesses++;
                if (selectedPics[4] == chosenPic[0]) {
                    correct++;
                    round++;
                    button5.setBackgroundColor(Color.GREEN);
                    correct();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (round == 10) {
                                String results = "CONGRATULATIONS! Your score is: " + correct + " correct guesses out of " + guesses + " attempts";
                                Toast.makeText(SelectionGame.this, results, Toast.LENGTH_LONG).show();
                            } else {
                                getPics(buttons, selectedPics, chosenPic);
                            }
                        }
                    }, 1200);
                } else {
                    button5.setBackgroundColor(Color.RED);
                    incorrect();
                }

            }

        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                guesses++;
                if (selectedPics[5] == chosenPic[0]) {
                    correct++;
                    round++;
                    button6.setBackgroundColor(Color.GREEN);
                    correct();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (round == 10) {
                                String results = "CONGRATULATIONS! Your score is: " + correct + " correct guesses out of " + guesses + " attempts";
                                Toast.makeText(SelectionGame.this, results, Toast.LENGTH_LONG).show();
                            } else {
                                getPics(buttons, selectedPics, chosenPic);
                            }
                        }
                    }, 1200);
                } else {
                    button6.setBackgroundColor(Color.RED);
                    incorrect();
                }

            }

        });


    }

    // gets the random pictures assigned to the imagebuttons
    private void getPics(ImageButton[] buttons, int[] selectedPics, int[] chosenPic) {
        // this is the array of all of the images stored on the tablet
        TypedArray images = getResources().obtainTypedArray(R.array.images);

        int choice;

        for (int i = 0; i < buttons.length; i++) {
            choice = (int) (Math.random() * images.length());

            // checking to make sure all of the pics are unique
            if (contains(selectedPics,choice)) {
                while (contains(selectedPics,choice)) {
                    choice = (int) (Math.random() * images.length());
                }
            }
            buttons[i].setImageResource(images.getResourceId(choice,R.drawable.banana)); // set the button with the right image
            selectedPics[i] = choice;                                                    // record the id of the selected picture for later
            buttons[i].setBackgroundColor(Color.LTGRAY);                                 // set the background of the button
        }

        chosenPic[0] = selectedPics[(int) (Math.random() * 6)];                          // randomly choose one of the pictures from the selected group

        int audio1 = getResources().getIdentifier(getResources().getResourceEntryName(images.getResourceId(chosenPic[0], R.drawable.banana)),"raw",getPackageName());
        int audio2 = getResources().getIdentifier("in_english_is","raw",getPackageName());
        final int[] tracks = new int[2];                                                 // plays the word then "in english is"
        tracks[0] = audio1;
        tracks[1] = audio2;
        final MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), tracks[0]);			 // set up the mediaplayer with the first track
        currentTrack = 1;
        mediaPlayer.start();															 // play the first sound bit
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {                                   // when it's done playing one sound, see if there is another sound to play
                mp.release();
                if (currentTrack < tracks.length && tracks[currentTrack] != 0) {         // if it's not the end of the array plus there is an actual ID of the audio track
                    mp = MediaPlayer.create(getApplicationContext(), tracks[currentTrack]);
                    currentTrack++;
                    mp.setOnCompletionListener(this);
                    mp.start();
                }
            }
        });
    }

    // checks to see if an int is already in an array
    private boolean contains(int[] nums, int num){
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == num) return true;
        }
        return false;
    }

    // plays the sound bit when the guess is correct
    private void correct(){
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), getResources().getIdentifier("good_job", "raw", getPackageName()));
        mp.start();
    }

    // plays the try again sound bit when it is incorrect
    private void incorrect() {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), getResources().getIdentifier("try_again", "raw", getPackageName()));
        mp.start();
    }
}

package de.frizzware.pacrun;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private ImageView ivTitlePac;
	
	//Animationen
	private Animation leftRightIn;
	
	//MediaPlayer
	MediaPlayer welcomeSound;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initGUI();
        
        Button start = (Button)findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, GameActivity.class));
			}
		});
        welcomeSound.start();
    }
    
    //Inizialisiert alle GUI-Elemente
    private void initGUI(){
    	//Animationen lesen
    	leftRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
    	
    	//MediaPlayer erstellen
    	welcomeSound = MediaPlayer.create(this, R.raw.opening_sounds);
    	
    	ivTitlePac = (ImageView) findViewById(R.id.ivTitlePac);
    	ivTitlePac.startAnimation(leftRightIn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

package de.frizzware.pacrun;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private ImageView ivTitlePac;
	
	//Animationen
	private Animation leftRightIn;
	private Animation fadeIN;
	private Animation fadeOUT;
	private Animation leftRightOut;
	
	private ImageView pacMan;
	private ImageView dot1;
	private ImageView dot2;
	
	//MediaPlayer
	MediaPlayer welcomeSound;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initGUI();
        
        welcomeSound.start();
    }
    
    //Inizialisiert alle GUI-Elemente
    private void initGUI(){
    	//Animationen lesen
    	leftRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
    	leftRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
    	fadeIN = AnimationUtils.loadAnimation(this, R.anim.fadein);
    	fadeOUT = AnimationUtils.loadAnimation(this, R.anim.fadeout);
    	
    	//MediaPlayer erstellen
    	welcomeSound = MediaPlayer.create(this, R.raw.opening_sounds);
    	
    	dot1 = (ImageView) findViewById(R.id.dot1);
    	dot2 = (ImageView) findViewById(R.id.dot2);
    	
    	dot1.startAnimation(fadeIN);
    	dot2.startAnimation(fadeIN);
    	
    	ivTitlePac = (ImageView) findViewById(R.id.ivTitlePac);
    	ivTitlePac.startAnimation(leftRightIn);
    	
    	
    	pacMan = (ImageView) findViewById(R.id.ivTitlePac);
    	pacMan.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ivTitlePac.startAnimation(leftRightOut);
				dot1.startAnimation(fadeOUT);
				dot2.startAnimation(fadeOUT);
				//startActivity(new Intent(MainActivity.this, GameActivity.class));
			}
		});
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
}

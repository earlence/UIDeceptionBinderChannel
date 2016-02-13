package com.example.quickdraw2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button showK, hideK, ex;
	static AttackService ass;
	
	public static void setAss(AttackService _ass)
	{
		ass = _ass;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		showK = (Button) findViewById(R.id.cmdShowK);
		hideK = (Button) findViewById(R.id.cmdHideK);
		ex = (Button) findViewById(R.id.cmdExercise);
		
		startService(new Intent(getApplicationContext(), AttackService.class));
		
		showK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showKeyboard();
				
				/*new Thread() {
					public void run()
					{
						try {
							Thread.currentThread().sleep(10000);
							ass.showKeyboard();
							
						} catch(Exception e) {}
					}
				}.start();*/
				
				ass.showKeyboard();
				
			}
		});
		
		hideK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//hideKeyboard();
				
				
				
				ass.hideKeyboard();
			}
		});
		
		ex.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Toast t = Toast.makeText(getApplicationContext(), Helper.getTopActivity(), Toast.LENGTH_SHORT);
				t.show();
			}
		});
	}
}

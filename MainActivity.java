package com.oblivionburn.realai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import com.oblivionburn.realai.R;
import com.oblivionburn.realai.Data;
import com.oblivionburn.realai.Logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity
{		
	private static final String LOG_TAG = null;

	//Variables
	private ImageView background = null;
	
	private TextToSpeech speech;
	private SpeechRecognizer hearing = null;
	private Intent recognizerIntent;
	private static AudioManager audioManager;
	
	private int int_Time = 7;
	private int int_Delay = 0;
	private Boolean bl_Ready = false;
	private Boolean bl_Listen = false;
	public static Boolean bl_Feedback = false;
	public Boolean bl_Speaking = false;
	public static File Brain_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain_Voice/" );
	private Handler handler;
	private final int REQUEST_CODE = 100;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        
        background = (ImageView)findViewById(R.id.imageView1);        
        handler = new Handler(); 
        
        hearing = SpeechRecognizer.createSpeechRecognizer(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		
        speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() 
        {
            @Override
            public void onInit(int status) {
               if(status != TextToSpeech.ERROR) {
            	   speech.setLanguage(Locale.US);
               }
            }
         });
        
        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        
        if (!Brain_dir.exists())
        {
        	Brain_dir.mkdirs(); 
        }
        
        File file = new File(Brain_dir, "Words.txt");
    	if (!file.exists())
        {    
    		try
    		{
				file.createNewFile();
			}
    		catch (IOException e)
    		{
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
			}
        }
    	
    	file = new File(Brain_dir, "InputList.txt");
    	if (!file.exists())
        {
    		try
    		{
				file.createNewFile();
			}
    		catch (IOException e)
    		{
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
			}
        }
        
    	bl_Ready = false;
		bl_Speaking = true;
        startTimer();
    }

    @Override
    public void onDestroy() 
    {
	    if (speech != null) 
	    {
		    speech.stop();
		    speech.shutdown();
	    }
	    
	    if (hearing != null)
	    {
	    	hearing.stopListening();
	    	hearing.cancel();
			hearing.destroy();
			hearing = null;
	    }
	    
	    stopTimer();
	    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
	    android.os.Process.killProcess(android.os.Process.myPid());
	    super.onDestroy();
    }
    
    //MessageBox
    public void PopUp(String message)
    {
    	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle("System Message");
        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
        {
        	public void onClick(DialogInterface dialog, int which)
        	{

        	}
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
    
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) 
    	{
	        case REQUEST_CODE: 
	        {
	            if (resultCode == RESULT_OK && data != null && speech.isSpeaking() == false) 
	            {
	            	ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            	Logic.str_Input = results.get(0);
	            	
	            	if (Logic.str_Input != null)
	            	{
	            		if (bl_Listen)
	            		{
	            			try
			                {
	            				for (String result : results)
	            				{
	            					Log.d(LOG_TAG, "Received: " + result);
	            				}
	            				
			                	Logic.prepInput();
			        			Logic.ClearLeftovers();
			        			
			        			bl_Ready = false;
			            		bl_Speaking = true;
			            		startTimer();
			        		}
			                catch (IOException e)
			                {
			                	e.printStackTrace();
			                	Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
			        		}
	            		}
	            		else
	            		{
	            			bl_Feedback = false;
		            		background.setBackgroundResource(R.drawable.alien_big_highlight);
		            		
			                try
			                {
			                	for (String result : results)
	            				{
	            					Log.d(LOG_TAG, "Received: " + result);
	            				}
			                	
			                	Logic.prepInput();
			        			Logic.Respond();
			
			        			Logic.ClearLeftovers();
			        			speech.speak(Logic.str_Output, TextToSpeech.QUEUE_FLUSH, null);
			        			
			        			bl_Ready = false;
			            		bl_Speaking = true;
			            		startTimer();
			        		}
			                catch (IOException e)
			                {
			                	e.printStackTrace();
			                	Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
			        		}
	            		}
	            	}
	            }
	            else	            	
	            {
	            	if (bl_Listen)
	            	{
	            		stopTimer();
	            	}
	            	else	
	            	{
	            		background.setBackgroundResource(R.drawable.alien_big);
		            	bl_Speaking = false;
	        	        startTimer();
	            	}
	            }
	            
	            break;
	        }
        }
    }
        
    //Timer
    Runnable StatusChecker = new Runnable() 
    {
        @Override 
        public void run()
        {  
        	if (bl_Ready == false && speech.isSpeaking() == false && bl_Speaking)
        	{
        		bl_Ready = true;
        		
        		try
        		{
        			startActivityForResult(recognizerIntent, REQUEST_CODE);
        		}
        		catch(ActivityNotFoundException a)
        		{
        			String appPackageName = "com.google.android.googlequicksearchbox";
        		    try 
        		    {
        		        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        		    } 
        		    catch (android.content.ActivityNotFoundException b) 
        		    {
        		    	try
        		    	{
        		    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        		    	}
        		    	catch (android.content.ActivityNotFoundException c)
        		    	{
        		    		onErrorSpeech();
        		    	}
        		    }
        		}
        	}

        	if (bl_Listen)
        	{
        		if (int_Delay < int_Time)
            	{
            		int_Delay++;
            		handler.postDelayed(StatusChecker, 1000);
            	}
            	else if (int_Delay >= int_Time)
            	{
            		bl_Ready = false;
            		bl_Speaking = true;
            		startTimer();
            	}
        	}
        	else
        	{
        		if (int_Delay < int_Time)
            	{
            		int_Delay++;
            		handler.postDelayed(StatusChecker, 1000);
            	}
            	else if (int_Delay >= int_Time && bl_Speaking == false)
            	{
            		try
            		{
    					try 
    					{
    						AttentionSpan();
    					} 
    					catch (InterruptedException e) 
    					{
    						e.printStackTrace();
    						Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
    					}
    				}
            		catch (IOException e)
            		{
    					e.printStackTrace();
    				}
            	}
        	}
        }
    };      
    
    void startTimer() 
    {
    	stopTimer();
    	int_Delay = 0;
    	StatusChecker.run();  	
    }
    
    void stopTimer() 
    {
        handler.removeCallbacks(StatusChecker);
    }
    
	public void AttentionSpan() throws IOException, InterruptedException
    {
    	Random random = new Random();
        int int_choice = random.nextInt(100);
        if (int_choice > 50)
        {
        	if (Logic.bl_NewInput == true)
    		{
    			CleanMemory();
    			Discourage();
    		}
        	
        	background.setBackgroundResource(R.drawable.alien_big);
    		Logic.FeedbackLoop();
    		startTimer();
        }
        else if (int_choice > 0 && int_choice <= 50)
        {
            Logic.bl_Initiation = true;
            try
        	{
            	bl_Feedback = false;
            	background.setBackgroundResource(R.drawable.alien_big_highlight);
            	Logic.Respond();
				
				if (speech != null)
				{
					speech.speak(Logic.str_Output, TextToSpeech.QUEUE_FLUSH, null);
				}
				
				if (Logic.bl_NewInput == true)
				{
					CleanMemory();
					Discourage();
				}
				
				bl_Ready = false;
				bl_Speaking = true;
				startTimer();
			}
        	catch (IOException e)
        	{
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
			}
        }
        
        Logic.bl_NewInput = false;
    }
    
	public void onErrorSpeech()
    {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Your device doesn't support Text-to-Speech.");
        dlgAlert.setTitle("System Message");
        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
        {
        	public void onClick(DialogInterface dialog, int which)
        	{
        		onDestroy();
        	}
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
	
    public void onExit(View view)
    {
    	stopTimer();
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	        	if (speech != null)
    	    	    {
    	    		    speech.stop();
    	    		    speech.shutdown();
    	    	    }
    	            android.os.Process.killProcess(android.os.Process.myPid());
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	        	PassiveListen();
    	            break;
    	        }
    	    }
    	};
    	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Exit Real AI?");
        dlgAlert.setTitle("System Message");
        dlgAlert.setNegativeButton("No", dialogClickListener);
        dlgAlert.setPositiveButton("Yes", dialogClickListener);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
     
    public void onErase(View view)
    {
    	stopTimer();
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	        	
    	        	EraseMemory(Brain_dir);
    	        	
    	        	if (!Brain_dir.exists())
    	            {
    	            	Brain_dir.mkdirs(); 
    	            }
    	            
    	            File file = new File(Brain_dir, "Words.txt");
    	        	if (!file.exists())
    	            {    
    	        		try
    	        		{
    	    				file.createNewFile();
    	    			}
    	        		catch (IOException e)
    	        		{
    	    				e.printStackTrace();
    	    			}
    	            }
    	        	
    	        	file = new File(Brain_dir, "InputList.txt");
    	        	if (!file.exists())
    	            {    
    	        		try
    	        		{
    	    				file.createNewFile();
    	    			}
    	        		catch (IOException e)
    	        		{
    	    				e.printStackTrace();
    	    			}
    	            }
    	        	
    	        	PopUp("Memory has been erased.");
    	        	
    	            break;
    	        case DialogInterface.BUTTON_NEGATIVE:
    	        	startTimer();
    	            break;
    	        }
    	    }
    	};
    	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Erase the memory?");
        dlgAlert.setTitle("System Message");
        dlgAlert.setNegativeButton("No", dialogClickListener);
        dlgAlert.setPositiveButton("Yes", dialogClickListener);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
    
    public void onSpeak(View view)
    {
    	bl_Ready = false;
		bl_Speaking = true;
        startTimer();
    }
    
    private void PassiveListen()
    {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() 
    	{
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) 
    	    {
    	        switch (which)
    	        {
    	        	case DialogInterface.BUTTON_POSITIVE:
	    	        	if (bl_Listen)
	    	        	{
	    	        		bl_Listen = false;
	    	        		int_Time = 7;
	    	        		
	    	        		audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
	    	        		bl_Ready = false;
	                		bl_Speaking = true;
	                		startTimer();
	    	        	}
	    	        	else
	    	        	{
	    	        		bl_Listen = true;
	    	        		int_Time = 5;
	    	        		
	    	        		audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
	    	        		bl_Ready = false;
	                		bl_Speaking = true;
	                		startTimer();
	    	        	}
	    	            break;
    	            
	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	        	bl_Ready = false;
	            		bl_Speaking = true;
	    	        	startTimer();
	    	            break;
    	        }
    	    }
    	};
    	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
    	
    	if (bl_Listen)
    	{
    		dlgAlert.setMessage("Disable passive listening?");
    	}
    	else		
    	{
    		dlgAlert.setMessage("Enable passive listening?");
    	}
        
        dlgAlert.setTitle("System Message");
        dlgAlert.setNegativeButton("No", dialogClickListener);
        dlgAlert.setPositiveButton("Yes", dialogClickListener);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
        
    private void EraseMemory(File fileOrDirectory) 
    {
        if (fileOrDirectory.isDirectory())
        {
        	for (File child : fileOrDirectory.listFiles())
        	{
        		EraseMemory(child);
        	}
        }
        fileOrDirectory.delete();
    }
    
    private void CleanMemory()
    {
    	Data.getInputList();
    	
    	if (Data.InputList.size() > 0)
    	{
    		for (int i = 0; i < Data.InputList.size(); i++)
        	{
        		String MemoryCheck = Data.InputList.get(i).toString();
        		File file = new File(Brain_dir, MemoryCheck + ".txt");
        		
        		if (file.exists())
                {
        			Data.getOutputList(MemoryCheck);
            		if (Data.OutputList.size() == 0)
            		{
            			file.delete();
            			Data.InputList.remove(i);
            			if (i > 0)
            			{
            				i--;
            			}
            		}
                }
        		else
        		{
        			Data.InputList.remove(i);
        			if (i > 0)
        			{
        				i--;
        			}
        		}
        	}
        	
        	try 
        	{
    			Data.saveInputList();
    		}
        	catch (IOException e)
        	{
    			e.printStackTrace();
    		}
    	}
    }
    
    private void Discourage()
    {
    	if (!Logic.str_last_response.equals("") && !Logic.str_last_response.equals(null))
    	{
    		if (Logic.str_last_response.contains("."))
            {
            	String str = Logic.str_last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.str_last_response.indexOf("."), Logic.str_last_response.indexOf(".") + 1, " .");
                Logic.str_last_response = sb.toString();
            }
    		
    		String[] WordArray = Logic.str_last_response.split(" ");
    		for (int i = 0; i < WordArray.length; i++)
            {
                if (WordArray[i].equals("."))
                {
                	WordArray[i] = " .";
                }
            }
    		
    		
    		for (int pro = 0; pro < WordArray.length - 1; pro++)
    		{
    			Data.getProWords(WordArray[pro]);
    			if (Data.Words.contains(WordArray[pro + 1]))
    			{
    				int index = Data.Words.indexOf(WordArray[pro + 1]);
    				if (Data.Frequencies.get(index) > 0)
    				{
    					Data.Frequencies.set(index, Data.Frequencies.get(index) - 1);
    				}
    			}
    		}
    		
    		for (int pre = 1; pre < WordArray.length; pre++)
    		{
    			Data.getPreWords(WordArray[pre]);
    			if (Data.Words.contains(WordArray[pre - 1]))
    			{
    				int index = Data.Words.indexOf(WordArray[pre - 1]);
    				if (Data.Frequencies.get(index) > 0)
    				{
    					Data.Frequencies.set(index, Data.Frequencies.get(index) - 1);
    				}
    			}
    		}
    	}
    }

}

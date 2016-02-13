package com.example.quickdraw2;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

public class Helper
{
	static final String TAG = "Helper";
	
	public static String getTopActivity()
	{
		String focusedAppLine = sudoForResult("dumpsys window windows | grep -E 'mFocusedApp'");
        int sindex = focusedAppLine.indexOf("ActivityRecord");
        String actName = "none";
        //Log.i(TAG, focusedAppLine);
        if(sindex != -1)
        {
            sindex += "ActivityRecord{".length();
            String toParse = focusedAppLine.substring(sindex);
            String [] comps = toParse.split(" ");
            actName = comps[2].replace("/", "");
            Log.i(TAG, actName);
        }
        
        return actName;
	}
	
	public static String getTopActivity2(Context ctx)
	{
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
	    List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
	    Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
	    ComponentName componentInfo = taskInfo.get(0).topActivity;
	    return componentInfo.getPackageName();
	}
	
	//root-only methods
    public static String sudoForResult(String...strings) {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try{
            Process su = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = readFully(response);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            closeSilently(outputStream, response);
        }
        return res;
    }
    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

    public static void closeSilently(Object... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs) {
            if (x != null) {
                try {
                    Log.d(TAG, "closing: "+x);
                    if (x instanceof Closeable) {
                        ((Closeable)x).close();
                    } else if (x instanceof Socket) {
                        ((Socket)x).close();
                    } else if (x instanceof DatagramSocket) {
                        ((DatagramSocket)x).close();
                    } else {
                        Log.d(TAG, "cannot close: "+x);
                        throw new RuntimeException("cannot close "+x);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
}
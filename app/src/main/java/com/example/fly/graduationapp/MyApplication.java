package com.example.fly.graduationapp;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import com.example.fly.graduationapp.network.NetworkConnectChangedReceiver;
import com.example.fly.graduationapp.PublicMethod.PubMethod;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import de.mindpipe.android.logging.log4j.LogConfigurator;

//import org.apache.log4j.Level;
public class MyApplication extends Application {
    private  static  Logger log=Logger.getLogger(MyApplication.class);
   // private NetworkConnectChangedReceiver m_networkConnectChangedReceiver;
    private static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        initLog4j();
        initTranslateMap();
        registerNetworkStateListenReceiver();
        //registerNetworkStateListenReceiver();
        //创建界面之前初始化Data还是创建界面之后
    }
    private void registerNetworkStateListenReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        MyApplication.getAppContext().registerReceiver(new NetworkConnectChangedReceiver(),filter);
    }

    private   void initLog4j()
    {
        String path=createLogFileAndPath("log4j.txt");
        if(path==null)
        {
            log.error("log4j init failure");
            return;
        }
        lo4jConfig(path);
        log.info("log4j init success");
    }
    private String createLogFileAndPath(String fileName)
    {
        if(appContext==null)
            return null;
        if(!getExternalStorageState())
        {
            log.error("external storage state cannot read and write");
            return null;
        }
        String logPath;
        try
        {
            logPath=appContext.getExternalFilesDir("logsFile").getAbsolutePath();
        }
        catch (NullPointerException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get logfile getAbsolutePath failure"+ExceMsg);
            return null;
        }
        File logFile=new File(logPath);
        if(!logFile.exists())
        {
            if(!logFile.mkdirs())
            {
                log.error("mkdirs create logpath failure");
                return null;
            }
        }
        logPath+=File.separator+fileName;
        logFile=new File(logPath);
        if(!logFile.exists())
        {
            try
            {
                if(logFile.createNewFile())
                {
                    log.error("createNewFile create logFile failure");
                    return null;
                }
            }
            catch (IOException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("createNewFile logFile failure"+ExceMsg);
                return null;
            }
        }
        return logPath;
    }
    private void lo4jConfig(String path)
    {
        LogConfigurator logConfigurator=new LogConfigurator();
        logConfigurator.setResetConfiguration(true);
        logConfigurator.setFileName(path);
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache",Level.DEBUG);
        logConfigurator.setFilePattern("[%d{MM-dd HH:mm:ss}]-[%c{2}:%M:%L]-[%m]%n");
        logConfigurator.setLogCatPattern("[%d{MM-dd HH:mm:ss}]-[%c{2}:%M:%L]-flylog4j-[%m]%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        //logConfigurator.setUseLogCatAppender(false);
        logConfigurator.configure();
    }

    private boolean getExternalStorageState()
    {
        String state= Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))//we can read and write
        {
            log.info("External Storage can read and write");
            return true;
        }
        else
        if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))//only read
        {
            log.info("External Storage can only read ");
        }
        else
        {
            log.info("External Storage cannot read and write ");
        }
        return false;
    }

    private void initTranslateMap()
    {
        for(int i=0;i<PubMethod.en_Arr.length;i++)
        {
            PubMethod.fromENToZH_Map.put(PubMethod.en_Arr[i],PubMethod.zh_Arr[i]);
            PubMethod.fromZHToEN_Map.put(PubMethod.zh_Arr[i],PubMethod.en_Arr[i]);
        }
    }
    public static  Context getAppContext(){
        return appContext;
    }

}

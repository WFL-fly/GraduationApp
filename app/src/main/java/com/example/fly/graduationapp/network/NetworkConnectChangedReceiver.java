package com.example.fly.graduationapp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Parcelable;

import com.example.fly.graduationapp.CMD.CenterMsg_CMD;
import com.example.fly.graduationapp.Data.msgObj;
import com.example.fly.graduationapp.Centre;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.MyApplication;
import com.example.fly.graduationapp.PublicMethod.PubMethod;

import org.apache.log4j.Logger;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    private  static Logger log=Logger.getLogger(NetworkConnectChangedReceiver.class);
    private  static volatile boolean networkState;
    public NetworkConnectChangedReceiver()
    {
        super();
        isNetworkAvailable();
    }

    public  static boolean getNetworkState()
    {
        return networkState;
    }

    // 这个监听wifi的打开与关闭，与wifi的连接无关
    private void wifiStateChangeAction(Intent intent)
    {
        /*
        WiFi 的状态目前有五种, 分别是: WifiManager.WIFI_STATE_ENABLING: WiFi正要开启的状态, 是 Enabled 和 Disabled 的临界状态;
        WifiManager.WIFI_STATE_ENABLED: WiFi已经完全开启的状态;
        WifiManager.WIFI_STATE_DISABLING: WiFi正要关闭的状态, 是 Disabled 和 Enabled 的临界状态;
        WifiManager.WIFI_STATE_DISABLED: WiFi已经完全关闭的状态;
        WifiManager.WIFI_STATE_UNKNOWN: WiFi未知的状态, WiFi开启, 关闭过程中出现异常, 或是厂家未配备WiFi外挂模块会出现的情况;
         */
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        switch (wifiState)
        {
            case WifiManager.WIFI_STATE_DISABLED:
               //wifi 被关闭
                break;
            case WifiManager.WIFI_STATE_DISABLING:

                break;
            case WifiManager.WIFI_STATE_ENABLING:
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                //wifi 被打开
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                break;
            default:
                break;
        }
    }
    /*
     这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager
     .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
     在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
     当然刚打开wifi肯定还没有连接到有效的无线
     */
    private  void networkStateChangeAction(Intent intent)
    {
        NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (null != networkInfo)
        {
            NetworkInfo.State state = networkInfo.getState();
            if (state == NetworkInfo.State.CONNECTED)
            {
                networkState=true;
            }
            else
            {
                networkState=false;
            }
        }
    }
    // 这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
    // 最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
    // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
    private  void connectivityAction(Context context,Intent intent)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null)
        { // connected to the internet
            if (activeNetwork.isConnected())
            {
                if( (!networkState) && (Data.get_DataInitOrigin()!=Data.Data_Init_Origin.INIT_FROM_MYSQL))
                {
                    //网络有不可用变为可用状态时，发送mysql数据更新消息
                    Handler handler=Centre.getThreadHandler("Centre");
                    if(handler!=null)
                    {
                        log.info("connectivityAction");
                        PubMethod.sendMsgToThread(handler,
                                CenterMsg_CMD.INTERNET_CONNECTED_UPDATE_DATA.ordinal(),
                                new msgObj(false,null,null));
                    }
                }
                networkState=true;
                //此处更新发送数据更新消息到 center thread
                //if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                //{
                //    //当前WiFi连接可用
                //}
                //else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                //{
                //      //当前移动网络连接可用
                //}
            }
            else
            {
                networkState=false;
                Handler handler=Centre.getThreadHandler("Centre");
                if(handler!=null)
                {
                    PubMethod.sendMsgToThread(handler,
                            CenterMsg_CMD.INTERNET_DISCONNECT.ordinal(),
                            new msgObj(false,null,null));
                }
                log.error("internet disconnected");
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch (intent.getAction())
        {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                 connectivityAction(context,intent);
                 break;
            //case WifiManager.WIFI_STATE_CHANGED_ACTION:
            //     wifiStateChangeAction(intent);
            //     break;
            //case WifiManager.NETWORK_STATE_CHANGED_ACTION:
            //     networkStateChangeAction(intent);
            //     break;
            default:
                 break;
        }
    }
    public static  boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager=(ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null)
        {
            networkState=false;
            return networkState;
        }
        networkState=false;
        NetworkInfo info=connectivityManager.getActiveNetworkInfo();
        if(info!=null)
        {
           if(info.isConnected())
           {
               networkState=true;
           }
        }
        return networkState;
    }
    /*
    private  void isNetworkAvailable()
    {
        ConnectivityManager connectivityManager=(ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null)
        {
            networkState=false;
            return ;
        }
        NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
        if(info!=null&&info.length>0)
        {
            for(int i=0;i<info.length;i++)
            {
                if(info[i].getState()==NetworkInfo.State.CONNECTED)
                {
                    networkState=true;
                    return ;
                }

            }
        }
        networkState=false;
    }
    */
}

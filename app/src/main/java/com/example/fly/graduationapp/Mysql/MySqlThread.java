package com.example.fly.graduationapp.Mysql;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.fly.graduationapp.CMD.CenterMsg_CMD;
import com.example.fly.graduationapp.CMD.Mysql_CMD;
import com.example.fly.graduationapp.CMD.SQLite_CMD;
import com.example.fly.graduationapp.Centre;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.DownloadTrendRate;
import com.example.fly.graduationapp.Data.SaveTrendDataMsg;
import com.example.fly.graduationapp.Data.TrendRateMsg;
import com.example.fly.graduationapp.Data.childTbRec;
import com.example.fly.graduationapp.Data.msgObj;
import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.SQLite.SQLiteThread;

import org.apache.log4j.Logger;

import java.util.List;
/**
 * Created by wf on 2018/4/29.
 */

public class MySqlThread extends Thread {
    //private static Log log= LogFactory.getLog(Centre.class);

    private  static Logger log=Logger.getLogger(MySqlThread.class);
    private Handler m_CentreHandler=null;
    private static Handler mHandler=null;
    public static Handler getMySqlThreadHandler()
    {
        return mHandler;
    }
    //单例模式
    private static volatile MySqlThread m_instance;
    private static volatile boolean runFuncIsRunning=false;
    public static  MySqlThread getInstance()
    {
        if (m_instance == null) {
            synchronized (Centre.class) {
                if (m_instance == null) {
                    m_instance = new MySqlThread();
                }
            }
        }
        return m_instance;
    }
    @Override
    public synchronized void start() {
        if(!runFuncIsRunning)
        {
            super.start();
            runFuncIsRunning=true;
        }
    }
    private MySqlThread(String name){
        super(name);
        m_CentreHandler= Centre.getCenterThreadHandler();
    }
    private MySqlThread(){
        super();
        m_CentreHandler= Centre.getCenterThreadHandler();
    }
    private  void sendMsgToCentreThread(CenterMsg_CMD msgWhat, Object obj)
    {
        Message msg=Message.obtain();
        CenterMsg_CMD uiMsgCmd=msgWhat;
        msg.what = uiMsgCmd.ordinal();
        msg.obj=obj;
        m_CentreHandler.sendMessageDelayed(msg,5);
    }
    private boolean downloadTrendRateDataProcess(msgObj _msgObj)
    {
        boolean res=false;
        if(_msgObj.result !=null)
        {
            DownloadTrendRate tempmag=(DownloadTrendRate)_msgObj.result;
            if(tempmag.isUpdateAllRate)
            {
                if(tempmag.UpdateAllRateStartDate!=null)
                {
                    //更新全部数据
                    res=downloadAllTrendRateData(tempmag.UpdateAllRateStartDate);
                }
            }
            else
            {
                if(tempmag.m_trendRateMsgList!=null)
                {
                    //更新全部数据
                    res= downloadSomeTrendRateData(tempmag.m_trendRateMsgList);
                }
            }
        }
        return res;
    }
    private boolean downloadAllTrendRateData(String startDate)
    {
        List<String> curList=Data.get_currencyList_Copy();
        if(curList==null)
        {
            return  false;
        }
        for(String fromCurName:curList)
        {
            for(String toCurName:curList)
            {
                if(fromCurName.equals(toCurName))
                   continue;
                if(!downloadOneTrendRateData( fromCurName, toCurName, startDate))
                {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean downloadSomeTrendRateData(List<TrendRateMsg> list)
    {
        boolean res=false;
        for(int i=0;i<list.size();i++)
        {
            TrendRateMsg rec=list.get(i);
            res=downloadOneTrendRateData(rec.fromCurName,rec.toCurName,rec.startDate);
            if(!res)
            {
               break;
            }
        }
       return res;
    }
    //保证数据的完整性
    private boolean downloadOneTrendRateData(String fromCurName,String toCurName,String startDate)
    {
        boolean isFinish=false;
        boolean res=false;
        Handler SqliteHandler=Centre.getThreadHandler("SQLite");
        if(SqliteHandler==null)
            return false;
        do
        {
           List<childTbRec> list= MysqlManager.getRateList(fromCurName,toCurName,startDate);
           if(list==null)
             break;
           if(list.size()>0)
           {
               startDate=list.get(list.size()-1).UpTime;
               PubMethod.sendMsgToThread(SqliteHandler,
                       SQLite_CMD.SQLite_SAVE_TREND_RATE_DATA.ordinal(),
                       new msgObj(true,mHandler,new SaveTrendDataMsg(fromCurName,toCurName,list)));
               log.info("getRateList "+"from "+fromCurName+"to "+toCurName+" success,record number:"+list.size());
           }
           else
           {
               isFinish=true;
               res=true;
           }
       }while (!isFinish);
        log.info("get from "+fromCurName+" to "+toCurName+" all rate  success");
        return res;
    }

    private Handler.Callback MySqlThreadCallback=new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            Mysql_CMD MsgCmd=Mysql_CMD.get_Mysql_CMD(msg.what);
            if(MsgCmd==null)
                return false;
            if(msg.obj==null)
                return false;
            msgObj _msgObj=(msgObj)msg.obj;
            switch (MsgCmd)
            {
                case Mysql_DOWNLOAD_TREND_RATE_DATA:
                     boolean res=downloadTrendRateDataProcess(_msgObj);
                     if(!res)
                     {
                        log.error("Mysql_UPDATE_TREND_RATE_DATA failure");
                     }
                     Handler SQLiteHandler=Centre.getThreadHandler("SQLite");
                     if(SQLiteHandler!=null)
                     {
                         PubMethod.sendMsgToThread(SQLiteHandler,
                                 SQLite_CMD.MYSQL_DOWNLOAD_TREND_RATE_DATA_FINISH.ordinal(),
                                 new msgObj(false,null,res));
                     }
                     if(_msgObj.isACK)
                        PubMethod.sendMsgToThread(_msgObj.ackHandler,
                                CenterMsg_CMD.MYSQL_DOWNLOAD_TREND_RATE_DATA_FINISH.ordinal(),
                                new msgObj(false,null,res));
                    break;
                case SQLite_SAVE_TREND_RATE_DATA_FINISH:

                    break;

                default:
                    break;
            }
            return false;
        }
    };
    @Override
    public void run() {
        Looper.prepare();
        mHandler=new Handler(MySqlThreadCallback);
        Centre.addHandler("Mysql",mHandler);
        sendMsgToCentreThread(CenterMsg_CMD.MYSQL_THREAD_MSG_LOOP_FINISH,new msgObj(false,null,true));
        Looper.loop();
    }
}

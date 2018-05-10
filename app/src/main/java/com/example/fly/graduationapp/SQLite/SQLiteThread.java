package com.example.fly.graduationapp.SQLite;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.fly.graduationapp.CMD.CenterMsg_CMD;
import com.example.fly.graduationapp.CMD.SQLite_CMD;
import com.example.fly.graduationapp.CMD.UIMsg_CMD;
import com.example.fly.graduationapp.Centre;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.SaveTrendDataMsg;
import com.example.fly.graduationapp.Data.msgObj;
import com.example.fly.graduationapp.Data.requestTR;
import com.example.fly.graduationapp.PublicMethod.PubMethod;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

public class SQLiteThread extends Thread {
    private  static Logger log=Logger.getLogger(SQLiteThread.class);
    private Handler m_CentreHandler=null;
    private static Handler mHandler=null;
    //private boolean MysqlDownloadTrendRateFinish=false;
    public static Handler getMySqlThreadHandler()
    {
        return mHandler;
    }
    //单例模式
    private static volatile SQLiteThread m_instance;
    private static volatile boolean runFuncIsRunning=false;
    public static  SQLiteThread getInstance()
    {
        if (m_instance == null) {
            synchronized (Centre.class) {
                if (m_instance == null) {
                    m_instance = new SQLiteThread();
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
    private SQLiteThread(String name){
        super(name);
        m_CentreHandler= Centre.getCenterThreadHandler();
    }
    private SQLiteThread(){
        super();
        m_CentreHandler= Centre.getCenterThreadHandler();
    }
    private  void sendMsgToCentreThread(CenterMsg_CMD msgWhat, Object obj)
    {
        Message msg= Message.obtain();
        CenterMsg_CMD uiMsgCmd=msgWhat;
        msg.what = uiMsgCmd.ordinal();
        msg.obj=obj;
        m_CentreHandler.sendMessageDelayed(msg,5);
    }
    private boolean saveTrendRateDataRes=true;
    private Handler.Callback  SQLiteThreadCallback=new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            SQLite_CMD MsgCmd=SQLite_CMD.get_SQLite_CMD(msg.what);
            if(MsgCmd==null)
                return false;
            if(msg.obj==null)
                return false;
            msgObj _msgObj=(msgObj)msg.obj;
            switch (MsgCmd)
            {
                case SQLite_SAVE_TREND_RATE_DATA:
                    //gengxin
                    boolean res=false;
                    if(_msgObj.result!=null)
                    {
                        SaveTrendDataMsg save=(SaveTrendDataMsg)_msgObj.result;
                        res=SQLiteManager.save_TrendRateList_To_SQLite(save.fromCurName,save.toCurName,save.dataList);
                    }
                    if(!res)
                    {
                        saveTrendRateDataRes=false;
                        log.error("SQLite_SAVE_TREND_RATE_DATA failure");
                    }
                    //if(_msgObj.isACK)
                    //    PubMethod.sendMsgToThread(_msgObj.ackHandler,
                    //            Mysql_CMD.SQLite_SAVE_TREND_RATE_DATA_FINISH.ordinal(),
                    //            new msgObj(false,null,res));
                    break;
                case MYSQL_DOWNLOAD_TREND_RATE_DATA_FINISH:
                    //当接受到这个消息时，SQLite肯定已经保存玩数据了，
                    // 因为线程消息时队列关系，先进先出，并且线程时书序执行，
                    // 只有一条消息处理完，再去取其他消息处理，MYSQL的保存数据的消息肯定在此消息之前进入队列，
                    // 所以只有以前的消息都执行完了，才会处理这个消息
                    Handler centreHandler=Centre.getThreadHandler("Centre");
                    if(centreHandler!=null)
                    {
                        boolean mysqlDownDataRes=(boolean)_msgObj.result;
                        boolean lastRes=saveTrendRateDataRes&&mysqlDownDataRes;
                        PubMethod.sendMsgToThread(centreHandler,
                                CenterMsg_CMD.SQLITE_SAVE_TREND_RATE_DATA_FINISH.ordinal(),
                                new msgObj(false,null,lastRes));
                        saveTrendRateDataRes=true;
                        if(lastRes)
                        {
                            //gengxin biao
                            log.info("SQLITE_SAVE_TREND_RATE_DATA_FINISH");
                            Data.update_TR_UpRec_Map_FromSqlite();
                        }
                    }

                    break;

                case SQLite_GET_TREND_RATE_DATA:
                    //gengxin
                    //gengxin
                    boolean res2=false;
                    if(_msgObj.result!=null)
                    {
                        List<requestTR> requestTRList=(List<requestTR>)_msgObj.result;
                        for(int i=0;i<requestTRList.size();i++)
                        {
                            requestTR requestTR=requestTRList.get(i);
                            res2= Data.addOneTRData(requestTR.TRName,requestTR.startDate,requestTR.endDate);
                        }
                    }
                    if(!res2)
                    {
                        log.error("SQLite_GET_TREND_RATE_DATA failure");
                    }
                    if(_msgObj.isACK&&_msgObj.ackHandler!=null)
                        PubMethod.sendMsgToThread(_msgObj.ackHandler,
                                UIMsg_CMD.SQLite_GET_TREND_RATE_DATA_FINISH.ordinal(),//////////
                                new msgObj(false,null,res2));
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
        mHandler=new Handler(SQLiteThreadCallback);
        Centre.addHandler("SQLite",mHandler);
        sendMsgToCentreThread(CenterMsg_CMD.SQLITE_THREAD_MSG_LOOP_FINISH,new msgObj(false,null,true));
        Looper.loop();
    }
}

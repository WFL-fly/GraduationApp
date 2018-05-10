package com.example.fly.graduationapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.fly.graduationapp.CMD.CenterMsg_CMD;
import com.example.fly.graduationapp.CMD.Mysql_CMD;
import com.example.fly.graduationapp.CMD.UIMsg_CMD;
import com.example.fly.graduationapp.Data.msgObj;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.DownloadTrendRate;
import com.example.fly.graduationapp.Data.TR_UpRec_Rec;
import com.example.fly.graduationapp.Data.TrendRateMsg;
import com.example.fly.graduationapp.Data.childTbRec;
import com.example.fly.graduationapp.Mysql.MySqlThread;
import com.example.fly.graduationapp.Mysql.MysqlManager;
import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.SQLite.SQLiteManager;
import com.example.fly.graduationapp.SQLite.SQLiteThread;
import com.example.fly.graduationapp.network.NetworkConnectChangedReceiver;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import com.mysql.jdbc.log.LogFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
public class Centre extends Thread {
    private static Logger log = Logger.getLogger(Centre.class);
    private static final Map<String, Handler> AllThreadHandlerMap = new Hashtable<>();
    public static boolean m_isInitData = false;
    private Handler m_UIHandler = null;
    private static Handler centerThreadHandler = null;
    private MySqlThread m_mySqlThread = null;
    private SQLiteThread m_sqLiteThread = null;
    private boolean UIThreadMsgLoopState = false;
    private static  boolean MysqlThreadMsgLoopState = false;
    private boolean SQLiteThreadMsgLoopState = false;
    private boolean m_ThreadMsgLoopState = false;
    public static  volatile boolean MysqlDownloadAndSqliteSave_Res=false;
    public static boolean getMysqlDownloadAndSqliteSave_Res()
    {
        return MysqlDownloadAndSqliteSave_Res;
    }

    public static boolean addHandler(String threadName, Handler handler) {
        if (threadName == null || handler == null)
            return false;
        AllThreadHandlerMap.put(threadName, handler);
        return true;
    }

    public static Handler getThreadHandler(String threadName) {
        if (threadName == null)
            return null;
        return AllThreadHandlerMap.get(threadName);
    }

    public static Handler getCenterThreadHandler() {
        return centerThreadHandler;
    }

    //单例模式
    private static volatile Centre m_instance;
    private static volatile boolean runFuncIsRunning=false;
    public static  Centre getInstance()
    {
        if (m_instance == null) {
            synchronized (Centre.class) {
                if (m_instance == null) {
                    m_instance = new Centre();
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

    private Centre(String name) {
        super(name);
        m_UIHandler = MainActivity.getUIThreadHandler();
        testInit();
        m_isInitData = false;
    }


    private Centre() {
        super();
        m_UIHandler = MainActivity.getUIThreadHandler();
        testInit();
        m_isInitData = false;
    }

    private boolean testInit() {
        boolean status = false;
        List<String> temList = new ArrayList<>();
        temList.add("CNY");
        temList.add("USD");
        temList.add("JPY");
        temList.add("RUB");
        temList.add("GBP");
        temList.add("EUR");
        try {
            if (Data.currencyList_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS)) {
                if (Data.currencyList != null)
                    Data.currencyList.clear();
                Data.currencyList = temList;

                status = true;
                Data.currencyList_lock.writeLock().unlock();
            } else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        } catch (InterruptedException e) {
            String ExceMsg = GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:" + ExceMsg);
        }

        log.info("test init success");
        return status;
    }

    private void sendMsgToUIThread(UIMsg_CMD msgWhat, Object obj) {
        Message msg = Message.obtain();
        UIMsg_CMD uiMsgCmd = msgWhat;
        msg.what = uiMsgCmd.ordinal();
        msg.obj = obj;
        m_UIHandler.sendMessageDelayed(msg, 5);
    }

    private void sendMsgToOtherThread(Handler handler, UIMsg_CMD msgWhat, Object obj) {
        Message msg = Message.obtain();
        UIMsg_CMD uiMsgCmd = msgWhat;
        msg.what = uiMsgCmd.ordinal();
        msg.obj = obj;
        handler.sendMessageDelayed(msg, 5);
    }

    private boolean initDataFromMysql() {
        boolean status = false;
        if (Data.update_allCurTbMap("Mysql"))//得到总表的数据记录以及可以兑换的所有货币list
        {
            if (MysqlManager.getAllCurTbDataFromMysqlDB())//的到所有的数据汇率记录
            {
                if (MysqlManager.updateAllRateData())//得到所有汇率数据
                    status = true;
                else
                    log.error("from mysql,get rate data input to Data.exchangeRate failure");
            } else
                log.error("from mysql,get rate data record input to Data.lineRateMap  failure");
        } else
            log.error("from mysql,get all_currency_tb data input to Data.allCurTbMap failure");
        return status;
    }

    private boolean initDataFromSQLite() {
        boolean status = false;
        if (Data.update_allCurTbMap("SQLite"))//得到总表的数据记录以及可以兑换的所有货币list
        {
            if (Data.update_curTbsRec_FromSqlite())//的到所有的数据汇率记录
            {
                if (Data.update_exchangeRate_FromSqlite())//得到所有汇率数据
                    status = true;
                else
                    log.error("from SQLite,get rate data input to Data.exchangeRate failure");
            } else
                log.error("from SQLite,get rate data record input to Data.lineRateMap  failure");
        } else
            log.error("from SQLite,get all_currency_tb data input to Data.allCurTbMap failure");
        return status;
    }

    private static boolean isExist(List<String> list, String str) {
        if (list == null || str == null) {
            return false;
        }
        for (String ele : list) {
            if (ele.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private static boolean MysqlDownloadAndSqliteSave_TR() {
        if(!Data.update_TR_UpRec_Map_FromSqlite())
            return false;
        //在网络开通状态下 Data 是从 Mysql初始化的情况下，才 Update trend rate data
        if ((!NetworkConnectChangedReceiver.getNetworkState()) ||
                Data.get_DataInitOrigin() != Data.Data_Init_Origin.INIT_FROM_MYSQL) {
            //不更新
            log.info("unnecessary update trend rate data");
            return false;
        }
        //判断 那些 trend rate 需要更新，是否是第一次更新 有SQLite 判断 并做处理第一次更新
        List<String> curList = Data.get_currencyList_Copy();
        Map<String, TR_UpRec_Rec> TRMap = Data.get_TR_UpRec_Map_Copy();
        if (curList == null || TRMap == null) {
            log.error("error is serious.");
            return false;
        }
        List<TrendRateMsg> TrendRateMsgList = new LinkedList<>();
        for (String ele_1 : curList) {
            for (String ele_2 : curList) {
                if (ele_1.equals(ele_2))
                    continue;
                String TRRecName = ele_1 + "_" + ele_2 + "_tb";
                TR_UpRec_Rec rec = TRMap.get(TRRecName);
                if (rec != null&&SQLiteManager.isTbExistInSQLiteDB(TRRecName))
                {
                    TrendRateMsgList.add(new TrendRateMsg(ele_1, ele_2, rec.newestDate));
                    log.info("rec.newestDate :"+rec.newestDate);
                }
                else
                {
                    TrendRateMsgList.add(new TrendRateMsg(ele_1, ele_2, "2010-01-01 00:00:00.0"));
                    log.info("2010-01-01 00:00:00.0");
                }
            }
        }
        if (TrendRateMsgList.size() <= 0)
            return true;
        Handler mysqlHandler = getThreadHandler("Mysql");
        if (mysqlHandler != null && MysqlThreadMsgLoopState) {

            DownloadTrendRate tempmag = new DownloadTrendRate(false, TrendRateMsgList, null);
            PubMethod.sendMsgToThread(mysqlHandler,
                    Mysql_CMD.Mysql_DOWNLOAD_TREND_RATE_DATA.ordinal(),
                    new msgObj(true, centerThreadHandler, tempmag));
        }
        return true;
    }

    private Handler.Callback centreCallback=new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            CenterMsg_CMD centerMsgCmd=CenterMsg_CMD.get_CenterMsg_CMD(msg.what);
            if(centerMsgCmd==null)
                return false;
            msgObj msgobj=(msgObj)msg.obj;
            switch (centerMsgCmd)
            {
                case UI_INIT_FINISH:
                    break;
                case UI_ACK_INIT_DATA:
                {
                    boolean m_isInitData=false;
                    if(!NetworkConnectChangedReceiver.getNetworkState()&&UIThreadMsgLoopState)
                        sendMsgToUIThread(UIMsg_CMD.INTERNET_DISCONNECT,new msgObj(false,null,null));
                    else
                    {
                        MysqlManager.initMysqlDB();
                        if(initDataFromMysql())
                        {
                            Data.set_DataInitOrigin(Data.Data_Init_Origin.INIT_FROM_MYSQL);
                            m_isInitData=true;
                            if(msgobj.isACK&&UIThreadMsgLoopState)
                                sendMsgToOtherThread(msgobj.ackHandler,UIMsg_CMD.DATA_INIT_FINISH,new msgObj(false,null,m_isInitData));
                            ////未保存数据 ************
                            if(SQLiteManager.save_allCurTbMap_to_SQLiteDB())
                            {
                                if(!SQLiteManager.save_All_CurTbMap_to_SQLiteDB())
                                    log.error(" save from mysql get all CurTbMap data to SQLiteDB failure");
                            }
                            else
                                log.error(" save from mysql get allCurTbMap data to SQLiteDB failure");
                        }
                    }
                    SQLiteManager.initSQLiteDB();
                    if(Data.get_DataInitOrigin()==Data.Data_Init_Origin.NO_INIT)
                    {
                        if(initDataFromSQLite())
                        {
                            Data.set_DataInitOrigin(Data.Data_Init_Origin.INIT_FROM_SQLITE);
                            m_isInitData=true;
                            if(msgobj.isACK&&UIThreadMsgLoopState)
                                sendMsgToOtherThread(msgobj.ackHandler,UIMsg_CMD.DATA_INIT_FINISH,new msgObj(false,null,m_isInitData));
                        }
                        else
                        {
                            if(msgobj.isACK&&UIThreadMsgLoopState)
                                sendMsgToOtherThread(msgobj.ackHandler,UIMsg_CMD.DATA_INIT_FINISH,new msgObj(false,null,m_isInitData));
                            return false;
                        }
                    }
                    if(Data.get_DataInitOrigin()!=Data.Data_Init_Origin.NO_INIT)
                    {
                        if(m_mySqlThread==null)
                        {
                            m_mySqlThread= MySqlThread.getInstance();
                            m_mySqlThread.start();
                        }
                        if(m_sqLiteThread==null)
                        {
                            m_sqLiteThread= SQLiteThread.getInstance();
                            m_sqLiteThread.start();
                        }
                    }
                }
                break;
                case INTERNET_CONNECTED_UPDATE_DATA:
                {
                    //一直在被调用
                    if(Data.get_DataInitOrigin()!=Data.Data_Init_Origin.INIT_FROM_MYSQL)
                    {
                        MysqlManager.initMysqlDB();
                        if(initDataFromMysql())
                        {
                            Data.set_DataInitOrigin(Data.Data_Init_Origin.INIT_FROM_MYSQL);
                            if(SQLiteManager.save_allCurTbMap_to_SQLiteDB())
                            {
                                if(!SQLiteManager.save_All_CurTbMap_to_SQLiteDB())
                                    log.error(" save from mysql get all CurTbMap data to SQLiteDB failure");
                            }
                            else
                                log.error(" save from mysql get allCurTbMap data to SQLiteDB failure");
                        }
                    }
                    if(MysqlThreadMsgLoopState&&SQLiteThreadMsgLoopState)
                    {
                        if(!MysqlDownloadAndSqliteSave_TR())
                            log.error("MysqlDownloadAndSqliteSave_TR failure");
                    }
                }
                break;
                case INTERNET_DISCONNECT:
                {
                    //转发消息给 UI Thread 让UI发出界面提醒
                    Handler UIHandler = getThreadHandler("UI");
                    if (UIHandler != null )
                    {
                        PubMethod.sendMsgToThread(UIHandler,
                                UIMsg_CMD.INTERNET_DISCONNECT.ordinal(),
                                new msgObj(false, null, null));
                    }
                }
                break;
                case MYSQL_THREAD_MSG_LOOP_FINISH:
                {
                    MysqlThreadMsgLoopState=true;
                }
                break;
                case MYSQL_DOWNLOAD_TREND_RATE_DATA_FINISH:
                {
                    //msgObj msgobj=(msgObj)msg.obj;
                    if(msgobj.result!=null)
                    {
                        boolean res=(boolean)msgobj.result;
                        if(!res)
                        {
                            log.error("mysql download trend rate data failure");
                        }
                    }
                    else
                    {
                        log.error("failure");
                    }
                }
                break;
                case SQLITE_THREAD_MSG_LOOP_FINISH:
                {
                    SQLiteThreadMsgLoopState=true;
                    if(!SQLiteManager.isTbExistInSQLiteDB("TrendRate_UpdateRecord_tb"))
                    {
                        if(!SQLiteManager.create_TrendRate_UpdateRecord_tb())
                        {
                            //错误比较严重 待处理
                            log.error("TrendRate_UpdateRecord_tb is not exist ,but cannot create it,error is serious.");
                            return false;
                        }
                        // 更新全部数据
                    }
                    //获得更新表数据
                    if(!MysqlDownloadAndSqliteSave_TR())
                    {
                        log.error("download trend rate failure");
                    }
                }
                break;
                case SQLITE_SAVE_TREND_RATE_DATA_FINISH:
                {
                    if(msgobj.result!=null)
                    {
                        MysqlDownloadAndSqliteSave_Res=(boolean)msgobj.result;
                    }
                }
                break;
                default:
                    break;
            }
            return false;
        }
    };
    @Override
    public void run() {
        //init
        UIThreadMsgLoopState=true;
        Looper.prepare();
        centerThreadHandler=new Handler(centreCallback);
        addHandler("Centre",centerThreadHandler);
        sendMsgToOtherThread(m_UIHandler,UIMsg_CMD.CentreThreadMSGLoop_INIT_FINISH,new msgObj(false,null,true));
        //sendMsgToUIThread(UIMsg_CMD.CentreThreadMSGLoop_INIT_FINISH,true);
        Looper.loop();
    }
}

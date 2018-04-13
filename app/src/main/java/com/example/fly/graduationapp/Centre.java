package com.example.fly.graduationapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.fly.graduationapp.CMD.CenterMsg_CMD;
import com.example.fly.graduationapp.CMD.UIMsg_CMD;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Mysql.MysqlManager;
import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.SQLite.SQLiteManager;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import com.mysql.jdbc.log.LogFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
public class Centre extends Thread {
    //private static Log log= LogFactory.getLog(Centre.class);

    private  static Logger log=Logger.getLogger(Centre.class);
    public static boolean m_isInitData=false;
    public Centre(String name){
        super(name);
        testInit();
        m_isInitData=false;
    }
    public Centre(){
        super();
        testInit();
        m_isInitData=false;
    }
    private static boolean testInit()
    {
        boolean status=false;
        List<String> temList=new ArrayList<>();
        temList.add("CNY");
        temList.add("USD");
        temList.add("JPY");
        temList.add("RUB");
        temList.add("GBP");
        temList.add("EUR");
        try
        {
            if(Data.currencyList_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.currencyList!=null)
                    Data.currencyList.clear();
                Data.currencyList=temList;

                status=true;
                Data.currencyList_lock.writeLock().unlock();
            }
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
        }

        log.info("test init success");
       return status;
    }
    private static boolean initDataFromMysql()
    {
        boolean status=false;
        MysqlManager.updateMysqlDBAllTbList();
        if (MysqlManager.updateAllCurrencyTb())
        {
            if(MysqlManager.getAllCurTbDataFromMysqlDB())
            {
                status=true;
                MysqlManager.updateAllRateData();
            }
            else
                log.error("from mysql,update rate data failure");
        }
        else
            log.error("from mysql,update all_currency_tb data failure");
        return status;
    }
    private static boolean initDataFromSQLite()
    {
        //更新Data的数据库所有表
        SQLiteManager.updateDada_TbNameList_FromSQLite();
        SQLiteManager.updateAll_Data_CurTbMap_fromSQLite();
        //更新Data的all_currency_tb map表
        return true;
    }

    private boolean init() {
        boolean status;
        //m_isInitDataFinish=testInit();
        if(PubMethod.isNetworkAvailable()&&initDataFromMysql())
       {
           SQLiteManager.updateAllCurTb_to_SQLiteDB();
           SQLiteManager.updateAll_CurTbs_to_SQLiteDB();
           status=true;
       }
       else
       {
           status=initDataFromSQLite();
       }
       return status;
    }
    public static Handler centerThreadHandler;
    private Handler.Callback centreCallback=new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            CenterMsg_CMD centerMsgCmd=CenterMsg_CMD.values()[msg.what];
            switch (centerMsgCmd)
            {
                case UI_INIT_FINISH:
                    break;
                case UI_CMD_1:
                    {
                        Message msg1=Message.obtain();
                        String msgStr="rec UI_CMD_1,and ack CTR_CMD_1";
                        UIMsg_CMD uiMsgCmd=UIMsg_CMD.CTR_CMD_1;
                        msg1.what = uiMsgCmd.ordinal();
                        msg1.obj=msgStr;
                        MainActivity.mHandler.sendMessage(msg1);
                    }
                    break;
                case UI_CMD_2:
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
        m_isInitData=init();
        if(m_isInitData)
            log.info("Data init success!");
        else
            log.error("Data init failure!");
        //启动centre 消息循环
        Looper.prepare();
        centerThreadHandler=new Handler(centreCallback);
        Message msg=Message.obtain();
        //String msgStr="INIT_FINISH";
        UIMsg_CMD uiMsgCmd=UIMsg_CMD.DATA_INIT_FINISH;
        msg.what = uiMsgCmd.ordinal();
        msg.obj=m_isInitData;
        MainActivity.mHandler.sendMessageDelayed(msg,10);
        Looper.loop();
    }
}

package com.example.fly.graduationapp.Data;

import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.Mysql.MysqlManager;
import com.example.fly.graduationapp.SQLite.SQLiteManager;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
public class Data {
    public enum Data_Init_Origin {
        NO_INIT,
        INIT_FROM_MYSQL,
        INIT_FROM_SQLITE
    }
    private static Logger log=Logger.getLogger(Data.class);//= LogFactory.getLog(Mysql.class);

    private static volatile Data_Init_Origin m_data_init_origin=Data_Init_Origin.NO_INIT;
    public static Data_Init_Origin get_DataInitOrigin()
    {
        return  m_data_init_origin;
    }

    public static void set_DataInitOrigin(Data_Init_Origin value)
    {
        m_data_init_origin=value;
    }

    //没有意义 Tb name list 应该有数据库管理
    public static ReentrantReadWriteLock allTbList_lock=new ReentrantReadWriteLock();
    public static volatile List<String> allTbList=null;//保存mysql中所有表名称
    public static boolean update_allTbList_FromSqlite()
    {
        boolean res=false;
        List<String> value= SQLiteManager.get_SQLiteDB_TbNameList();
        if(value==null)
            return res;
        return  set_allTbList(value);
    }
    //ok
    public static boolean set_allTbList(List<String> value)
    {
        boolean res=false;
        try
        {
            if(Data.allTbList_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.allTbList!=null)
                    Data.allTbList.clear();
                Data.allTbList=value;
                res=true;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.writeLock() exception:"+ExceMsg);
            return res;
        }
        finally {
            Data.allTbList_lock.writeLock().unlock();
        }
        return res;
    }
    //总表的记录
    public static ReentrantReadWriteLock allCurTbMap_lock=new ReentrantReadWriteLock();
    public static volatile  Map<String,allCurTbRec> allCurTbMap=null;//保存 mysql中all_currency_tb表数据

    public static boolean update_allCurTbMap(String from)
    {
        if(from==null)
            return false;
        boolean res=false;
        switch (from)
        {
            case "Mysql"://从mysql 更新
                res=MysqlManager.updateAllCurrencyTb();
                break;
            case "SQLite":
                res=update_allCurTbMap_FromSqlite();
                break;
        }
        if(res)
        {
            update_currencyList_from_allCurTbMap();
        }
        return res;
    }
    private static boolean update_allCurTbMap_FromSqlite()
    {
        boolean res=false;
        Map<String,allCurTbRec> value= SQLiteManager.get_allCurTbMap_FromSQLite();
        if(value==null)
            return res;
        return  Data.set_allCurTbMap(value);
    }
    private static boolean set_allCurTbMap(Map<String,allCurTbRec> value)
    {
        boolean res=false;
        try
        {
            if(Data.allCurTbMap_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.allCurTbMap!=null)
                    Data.allCurTbMap.clear();
                Data.allCurTbMap=value;
                res=true;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allCurTbMap_lock.writeLock() exception:"+ExceMsg);
            return res;
        }
        finally {
            Data.allCurTbMap_lock.writeLock().unlock();
        }
        return res;
    }
    private static List<String> get_currencyList_From_allCurTbMap()
    {
        List<String> tempList=new LinkedList<>();
        try
        {
            if(Data.allCurTbMap_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.allCurTbMap!=null)
                {
                    for(allCurTbRec rec:Data.allCurTbMap.values())
                    {
                        tempList.add(rec.CurName);
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allCurTbMap_lock.writeLock() exception:"+ExceMsg);
            return null;
        }
        finally {
            Data.allCurTbMap_lock.readLock().unlock();
        }
        return tempList;
    }

    //可供查询的货币list
    public static ReentrantReadWriteLock currencyList_lock=new ReentrantReadWriteLock();
    public static volatile List<String> currencyList=null;//保存可供查询的货币名称
    public static List<String> get_currencyList_Copy()
    {
        List<String> res=null;
        try
        {
            if(Data.currencyList_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.currencyList!=null)
                {
                    res=new ArrayList<>(Data.currencyList);
                }
            }
            else
            {
                log.error("get Data.currencyList_lock.readLock() failure ");
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.currencyList_lock.readLock() exception:"+ExceMsg);
            return null;
        }
        finally {
            Data.currencyList_lock.readLock().unlock();
        }
        return res;
    }
    private static boolean set_currencyList(List<String> value)
    {
        boolean res=false;
        try
        {
            if(Data.currencyList_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.currencyList!=null)
                    Data.currencyList.clear();
                Data.currencyList=value;
                res=true;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.currencyList_lock.writeLock() exception:"+ExceMsg);
            return res;
        }
        finally {
            Data.currencyList_lock.writeLock().unlock();
        }
        return res;
    }
    private static boolean update_currencyList_from_allCurTbMap()
    {
        List<String> tempList=get_currencyList_From_allCurTbMap();
        if(tempList==null)
            return false;
        return  set_currencyList(tempList);
    }
    public static ReentrantReadWriteLock exchangeRate_lock=new ReentrantReadWriteLock();
    public static volatile Map<String,curTbRec> exchangeRate=null;//所有可以查询货币对的汇率
    public static boolean update_exchangeRate_FromSqlite()
    {
        boolean res=false;
        Map<String,curTbRec> value= SQLiteManager.get_All_CurTbRec_Map();
        if(value==null)
            return res;
        return  set_exchangeRate(value);
    }
    //ok
    public static boolean set_exchangeRate(Map<String,curTbRec> value)
    {
        boolean res=false;
        try
        {
            if(Data.exchangeRate_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.exchangeRate!=null)
                    Data.exchangeRate.clear();
                Data.exchangeRate=value;
                res=true;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allCurTbMap_lock.writeLock() exception:"+ExceMsg);
            return res;
        }
        finally {
            Data.exchangeRate_lock.writeLock().unlock();
        }
        return res;
    }

    public static ReentrantReadWriteLock curTbsRec_lock=new ReentrantReadWriteLock();
    public static volatile Map<String,Map<String,curTbRec> > curTbsRec=null;
    public static boolean update_curTbsRec_FromSqlite()
    {
        boolean res=false;
        Map<String,Map<String,curTbRec> > value= SQLiteManager.get_All_CurTbMap_Map();
        if(value==null)
            return res;
        return  set_curTbsRec(value);
    }
    public static boolean set_curTbsRec(Map<String,Map<String,curTbRec> > value)
    {
        boolean res=false;
        try
        {
            if(Data.curTbsRec_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.curTbsRec!=null)
                    Data.curTbsRec.clear();
                Data.curTbsRec=value;
                res=true;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.writeLock() exception:"+ExceMsg);
            return res;
        }
        finally {
            Data.curTbsRec_lock.writeLock().unlock();
        }
        return res;
    }

    //趋势线
    public static ReentrantReadWriteLock lineRateMap_lock=new ReentrantReadWriteLock();
    public static volatile Map<String,LinkedList<trendLinePoint> > lineRateMap=null;
    public static boolean addNewListToMap(String key,LinkedList<trendLinePoint> value)
    {
        if(key==null||value==null)
            return false;
        try
        {
            if(Data.lineRateMap_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.lineRateMap==null)
                    Data.lineRateMap=new Hashtable<>();
                Data.lineRateMap.put(key,value);
            }
            else
                log.error("get Data.lineRateMap_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.lineRateMap_lock.writeLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.lineRateMap_lock.writeLock().unlock();
        }
        return true;
    }

    public static LinkedList<trendLinePoint> get_lineRateMap_Ele(String key)
    {
        LinkedList<trendLinePoint> lineData=null;
        if(key==null||lineRateMap==null)
            return  lineData;
        try
        {
            if(Data.lineRateMap_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.lineRateMap!=null)
                    lineData=Data.lineRateMap.get(key);
            }
            else
                log.error("get Data.lineRateMap_lock.readLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.lineRateMap_lock.readLock() exception:"+ExceMsg);
        }
        finally {
            Data.lineRateMap_lock.readLock().unlock();
        }
        return lineData;
    }
    public static boolean addNewTRDataListToMapOldList(String TRName,LinkedList<trendLinePoint> addList)
    {
        if(TRName==null||addList==null)
            return false;
        LinkedList<trendLinePoint> oldList=get_lineRateMap_Ele(TRName);
        if(oldList!=null)
        {
            int oldListSize=oldList.size();
            int addListSize=addList.size();
            if(addListSize<=0)
                return true;
            if(oldListSize<=0)
            {
               return addNewListToMap(TRName,addList);
            }
            int startIndex;
            if(!oldList.getFirst().m_date.before(addList.getLast().m_date))
            {
                if(oldList.getFirst().m_date.equals(addList.getLast().m_date))
                    startIndex=addListSize-2;
                else
                    startIndex=addListSize-1;
                for(int i=startIndex;i>=0;i--)
                {
                    oldList.addFirst(addList.get(i));
                }
            }else  if(!oldList.getLast().m_date.after(addList.getFirst().m_date))
            {
                if(oldList.getLast().m_date.equals(addList.getFirst().m_date))
                    startIndex=1;
                else
                    startIndex=0;
                for(int i=startIndex;i<addListSize;i++)
                {
                    oldList.addFirst(addList.get(i));
                }
            }
            else
            {
                log.error("two data has overlab");
                return false;
            }
        }
        else
        {
            return addNewListToMap(TRName,addList);
        }
        return true;
    }

    public static boolean addOneTRData(String TRName, Date startDate,Date endDate)
    {
        if(TRName==null||startDate==null||endDate==null)
            return false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startDateStr = formatter.format(startDate);
        String endDateStr = formatter.format(endDate);
        LinkedList<trendLinePoint> addList=SQLiteManager.get_TrendRate_List(TRName,startDateStr,endDateStr);
        if(addList==null)
            return false;
        return addNewTRDataListToMapOldList(TRName,addList);
    }

    public static ReentrantReadWriteLock TR_UpRec_Map_lock=new ReentrantReadWriteLock();
    public static volatile Map<String,TR_UpRec_Rec> TR_UpRec_Map=null;
    public static boolean set_TR_UpRec_Map(Map<String,TR_UpRec_Rec> value)
    {
        boolean res=false;
        try
        {
            if(Data.TR_UpRec_Map_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.TR_UpRec_Map!=null)
                    Data.TR_UpRec_Map.clear();
                Data.TR_UpRec_Map=value;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.TR_UpRec_Map_lock.writeLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.TR_UpRec_Map_lock.writeLock().unlock();
        }
        return true;
    }
    public static Map<String,TR_UpRec_Rec> get_TR_UpRec_Map_Copy()
    {
        boolean res=false;
        Map<String,TR_UpRec_Rec> resMap=null;
        try
        {
            if(Data.TR_UpRec_Map_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.TR_UpRec_Map!=null)
                   resMap=new Hashtable<>(Data.TR_UpRec_Map);
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.TR_UpRec_Map_lock.writeLock() exception:"+ExceMsg);
            return null;
        }
        finally {
            Data.TR_UpRec_Map_lock.readLock().unlock();
        }
        return resMap;
    }
    public static TR_UpRec_Rec get_TR_UpRec_Map_OneRec_Copy(String key)
    {
        if(key==null||TR_UpRec_Map==null)
            return null;
        boolean res=false;
        TR_UpRec_Rec resRec=null;
        try
        {
            if(Data.TR_UpRec_Map_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                resRec=new TR_UpRec_Rec(Data.TR_UpRec_Map.get(key));
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.TR_UpRec_Map_lock.writeLock() exception:"+ExceMsg);
            return null;
        }
        finally {
            Data.TR_UpRec_Map_lock.readLock().unlock();
        }
        return resRec;
    }
    public static boolean add_OneRec_TR_UpRec_Map(String key,TR_UpRec_Rec value)
    {
        if(key==null||value==null||TR_UpRec_Map==null)
            return false;
        boolean res=false;
        TR_UpRec_Rec resRec=null;
        try
        {
            if(Data.TR_UpRec_Map_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                Data.TR_UpRec_Map.put(key,value);
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.TR_UpRec_Map_lock.writeLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.TR_UpRec_Map_lock.writeLock().unlock();
        }
        return true;
    }
    public static boolean update_TR_UpRec_Map_FromSqlite()
    {
        Map<String,TR_UpRec_Rec> value=SQLiteManager.get_TR_UpRec_Map();
        return  value!=null&&set_TR_UpRec_Map(value);
    }
    //未使用到
    public static boolean update_TR_UpRec_Map_FromMySQL()
    {
        Map<String,TR_UpRec_Rec> value=MysqlManager.get_TR_UpRec_Map();
        return  value!=null&&set_TR_UpRec_Map(value);
    }
}


package com.example.fly.graduationapp.SQLite;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.TR_UpRec_Rec;
import com.example.fly.graduationapp.Data.Table;
import com.example.fly.graduationapp.Data.allCurTbRec;
import com.example.fly.graduationapp.Data.childTbRec;
import com.example.fly.graduationapp.Data.curTbRec;
import com.example.fly.graduationapp.Data.trendLinePoint;
import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.Mysql.Mysql;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
public class SQLiteManager {
    private  static Logger log=Logger.getLogger(SQLiteManager.class);
    private  static final String createTrendRate_UpdateRecord_tb="create table if not exists TrendRate_UpdateRecord_tb(" +
            "TRName VARCHAR(20) NOT NULL primary key,"+
            "newestPubTime datetime NOT NULL,"+
            "oldestPubTime datetime NOT NULL )";
    private  static final String createChild_tb="create table if not exists %s(" +
            "_id integer primary key autoincrement,"+
            "Exchange_Rate FLOAT(24.6) NOT NULL,"+
            "PubTime datetime NOT NULL )";
    private static final String createCurTb="create table if not exists %s(" +
            "_id integer primary key autoincrement,"+
            "exchange_currency_name VARCHAR(20) NOT NULL,"+
            "child_tb_name VARCHAR(20) NOT NULL,"+
            "NewestExRate FLOAT(24.6) NOT NULL,"+
            "NewestPubTime datetime NOT NULL )";
    private static final String createAllCurTb="create table if not exists all_currency_tb(" +
            "_id integer primary key autoincrement,"+
            "currency_name VARCHAR(20) NOT NULL,"+
            "update_datetime datetime NOT NULL,"+
            "currency_tb_name VARCHAR(20) NOT NULL )";
    private static final String createRateTrendTb="create table if not exists %s(" +
            "_id integer primary key autoincrement,"+
            "Exchange_Rate FLOAT(24.6) NOT NULL,"+
            "PubTime datetime NOT NULL )";
    private static final String selectAllCurTb="select * from all_currency_tb";
    private static final String insertRecToAllCurTb="insert into %s (currency_name,update_datetime,currency_tb_name) values(\'%s\',\'%s\',\'%s\')";
    private static final String insertRecToCurTb="insert into %s (exchange_currency_name,child_tb_name,NewestExRate,NewestPubTime) values(\'%s\',\'%s\',%f,\'%s\')";
    private static final String insertRecToTrendTb="insert into %s (Exchange_Rate,PubTime) values(%f,\'%s\')";
    private static final String selectRateList="select * from %s where (PubTime>\'%s\' AND PubTime<=\'%s\') ORDER BY PubTime ASC LIMIT 500";
    private static final String selectRecFromCurTb="select exchange_currency_name,child_tb_name,NewestExRate,NewestPubTime from %s";
    private static final String selectTR_UpRec="select * from TrendRate_UpdateRecord_tb";
    private static final String selectTRUpRec_TRName="select TRName from TrendRate_UpdateRecord_tb where TRName=\'%s\'";
    private static final String insertTRUpRec="insert into TrendRate_UpdateRecord_tb (TRName,newestPubTime,oldestPubTime) values(\'%s\',\'%s\',\'%s\')";
    private static final String updateTRUpRec_newest="UPDATE TrendRate_UpdateRecord_tb SET newestPubTime=\'%s\' where TRName=\'%s\'";
    private static final String updateTRUpRec_oldest="UPDATE TrendRate_UpdateRecord_tb SET oldestPubTime=\'%s\' where TRName=\'%s\'";
    private static final String updateTRUpRec="UPDATE TrendRate_UpdateRecord_tb SET newestPubTime=\'%s\',oldestPubTime=\'%s\'  where TRName=\'%s\'";

    //private static final String selectTR_Max="select MAX(PubTime) AS max from %s";//Exchange_Rate
    //private static final String selectTR_Min="select MIN(PubTime) AS min from %s";

    private  static List<String> tbNameList=null;
    private  static Map<String,allCurTbRec> m_allCurTbMap=null;
    private static volatile SQLiteDatabase m_db=null;
    //test success
    private static SQLiteDatabase get_db(){
        SQLiteDatabase m_db=MyDBHelper.getInstance().getWritableDatabase();
        if(m_db==null)
            log.error("getWritableDatabase  failure;");
        return m_db;
    }
    //test success
    public static boolean dropTableFromSQLiteDB(String tb_name)
    {
        if(tb_name!=null&&SQLiteManager.SQLite_exeSQL("drop table if exists "+tb_name))
        {
            if(!updateAllTbNameListFromSQLite()) {
                log.error("From SQLite,update all Tb name list failure");
            }
            return true;
        }
        return false;
    }
    //test success
    public static  boolean create_currency_tb(String curTbName)
    {
        if(curTbName==null)
        {
            return false;
        }
        String sql=String.format(Locale.getDefault(),createCurTb,curTbName);
        if(sql!=null&&createTableInSQLite(sql))
        {
            log.error("create curTb :"+curTbName+" failure");
            return false;
        }
        return true;
    }
    public  static boolean create_TrendRate_UpdateRecord_tb()
    {
        if(!createTableInSQLite(createTrendRate_UpdateRecord_tb))
        {
            log.error("create TrendRate_UpdateRecord_tb  failure");
            return false;
        }
        return true;
    }
    public  static boolean create_all_currency_tb()
    {
        if(!createTableInSQLite(createAllCurTb))
        {
            log.error("create AllCurTb  failure");
            return false;
        }
        return true;
    }
    public  static boolean create_TrendRateUpdate_tb(String TrendTb_name)
    {
        if(TrendTb_name==null)
            return false;
        String sql=String.format(createRateTrendTb,TrendTb_name);
        if(!createTableInSQLite(sql))
        {
            log.error("create TrendTb_name :"+TrendTb_name+" failure");
            return false;
        }
        return true;
    }
    public  static boolean createTableInSQLite(String sql)
    {
        if(sql!=null&&SQLiteManager.SQLite_exeSQL(sql))
        {
          if(!updateAllTbNameListFromSQLite()) {
              log.error("From SQLite,update all Tb name list failure");
          }
          return true;
        }
        return false;
    }
    //test success
    public static boolean isTbExistInSQLiteDB(String tb_name){
        if(tb_name==null||tbNameList==null)
        {
            return false;
        }
        for(int i=0;i<tbNameList.size();i++)
        {
            if(tb_name.equals(tbNameList.get(i)))
            {
                return true;
            }
        }
        return false;
    }
    public static boolean updateAllTbNameListFromSQLite(){
        boolean status=false;
        SQLiteDatabase db=get_db();
        if(db==null)
            return false;
        List<String> res=new LinkedList<>();
        try{
            String sql="select name from sqlite_master where type=\'table\' ";
            Cursor cur=db.rawQuery(sql,null);
            while(cur.moveToNext()){
                res.add(cur.getString(0));
            }
            cur.close();
            if(tbNameList!=null)
                tbNameList.clear();
            tbNameList=res;
            status=true;
        }
        catch (RuntimeException e){
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("NullPointerException"+ExceMsg);
        }
        finally {
            db.close();
        }
        return status;
    }
    private static Cursor SQLite_rawQuery(SQLiteDatabase db,String sql){
        if(db==null)
            return null;
        Cursor res=null;
        try{
            res=db.rawQuery(sql,null);
        }
        catch (RuntimeException e){
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("NullPointerException"+ExceMsg);
        }
        return res;
    }
    //test success
    private static boolean SQLite_exeSQL(String sql){
        SQLiteDatabase db=get_db();
        if(db==null)
            return false;
        try{
            db.execSQL(sql);
        }
        catch (SQLException e){
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("change sqlite failure;"+ExceMsg);
            return false;
        }
        finally {
            db.close();
        }
        return true;
    }
    public static boolean get_SQLite_AllCurTbRec()
    {
        if(!isTbExistInSQLiteDB(Table.allCurTbName))
            return false;
        SQLiteDatabase db=get_db();
        if(db==null)
            return false;
        String sql="select * from "+Table.allCurTbName;
        Cursor cur=db.rawQuery(sql,null);
        if(cur!=null)
        {
            Map<String,allCurTbRec> temp=new Hashtable<>();
            try
            {
                while(cur.moveToNext()){
                    String cur_name=cur.getString(cur.getColumnIndex("currency_name"));
                    String up_datetime=cur.getString(cur.getColumnIndex("update_datetime"));
                    String cur_tb_name=cur.getString(cur.getColumnIndex("currency_tb_name"));
                    allCurTbRec record=new allCurTbRec(cur_name,up_datetime,cur_tb_name);
                    temp.put(cur_name,record);
                }
                if(m_allCurTbMap!=null)
                    m_allCurTbMap.clear();
                m_allCurTbMap=temp;
            }
            catch (RuntimeException e){
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("RuntimeException"+ExceMsg);
            }
            finally {
                cur.close();
                db.close();
            }
        }
        else
        {
            log.error("get "+Table.allCurTbName+" Cursor failure");
            db.close();
            return false;
        }
        return true;
    }
    private static boolean delete_TbAllRec(String tb_name)
    {
       if(!isTbExistInSQLiteDB(tb_name))
       {
           return false;
       }
        return tb_name!=null&&SQLiteManager.SQLite_exeSQL("delete from "+tb_name);
    }

    public static boolean save_allCurTbMap_to_SQLiteDB()
    {
        List<allCurTbRec> tempList=null;
        try
        {
            if(Data.allCurTbMap_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
                tempList=new ArrayList<>(Data.allCurTbMap.values());
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.allCurTbMap_lock.readLock().unlock();
        }
        if(tempList==null) {
            return false;
        }
        if(isTbExistInSQLiteDB(Table.allCurTbName))
            delete_TbAllRec(Table.allCurTbName);
        else
            create_all_currency_tb();
        SQLiteDatabase db=get_db();
        if(db==null)
            return false;
        db.beginTransaction();
        try
        {
            for(allCurTbRec record:tempList)
            {
                String sql=String.format(insertRecToAllCurTb,Table.allCurTbName,record.CurName,record.CurUpdTime,record.CurTbName);
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("update all_currency_tb all record failure;"+ExceMsg);
            return false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return true;
    }
    public static boolean save_All_CurTbMap_to_SQLiteDB()
    {
        for(String name:Data.currencyList)
        {
            if(!save_OneCurTbMap_to_SQLiteDB(name))
            {
                log.error(String.format("update %s cur_tb failure ",name));
                return false;
            }
        }
        return true;
    }
    public static boolean save_OneCurTbMap_to_SQLiteDB(String cur_name)
    {
        List<curTbRec> tempList=null;
        if(cur_name==null)
            return false;
        try
        {
            if(Data.curTbsRec_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
                tempList=new ArrayList<>(Data.curTbsRec.get(cur_name).values());
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.curTbsRec_lock.readLock().unlock();
        }
        if(tempList==null||tempList.size()<=0)
            return false;
        String cur_tb_name=cur_name+"_tb";
        if(isTbExistInSQLiteDB(cur_tb_name))
            delete_TbAllRec(cur_tb_name);
        else if(create_currency_tb(cur_tb_name))
        {
            log.error("cur_tb_name:"+cur_tb_name+"is not exsit and cannot create it");
            return false;
        }

        SQLiteDatabase db=get_db();
        if(db==null)
            return false;
        db.beginTransaction();
        try
        {
            String sql;
            for(curTbRec record:tempList)
            {
                sql=String.format(Locale.getDefault(),insertRecToCurTb,cur_tb_name,record.excCurName,record.chiTbName,record.excRate,record.newestUpTime);
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("update currency_tb all record failure;"+ExceMsg);
            return false;
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
        return true;
    }
    public static void initSQLiteDB()
    {
        updateAllTbNameListFromSQLite();
    }
    //test success
    public static List<String> get_SQLiteDB_TbNameList()
    {
        updateAllTbNameListFromSQLite();
        return tbNameList;
    }
    //得到所有货币表的数据
    public  static boolean updateAll_Data_CurTbMap_fromSQLite()
    {
        boolean status=false;
        Map<String,Map<String,curTbRec> > tempMap=new Hashtable<>();
        int size=Data.currencyList.size();
        for(int i=0;i<size;i++)
        {
            String name=Data.currencyList.get(i);
            Map<String,curTbRec> resMap = get_One_CurTb_fromSQLite(name);
            if(resMap==null)
            {
                log.error("fly+ get "+name+"_tb rate data failure");
                return false;
            }
            tempMap.put(name,resMap);
        }
        if(tempMap.size()<=0)
            return false;
        try
        {
            if(Data.curTbsRec_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.curTbsRec!=null)
                    Data.curTbsRec.clear();
                Data.curTbsRec=tempMap;
                status=true;
            }
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
        }
        finally {
            Data.curTbsRec_lock.writeLock().unlock();
        }
        return status;
    }

    //test success
    public  static Map<String,curTbRec> get_One_CurTb_fromSQLite(String curName)
    {
        Map<String,curTbRec> resMap=null;
        boolean m_resStatus=false;
        if(curName==null)
        {
            log.error("method formal parameter is null.");
            return null;
        }

        String curTbName=curName+"_tb";
        if(!isTbExistInSQLiteDB(curTbName))
            return null;
        SQLiteDatabase db=get_db();
        if(db==null)
        {
            log.error("cannot open SQLite DB .");
            return null;
        }
        String sql=String.format(selectRecFromCurTb,curTbName);
        Cursor cur=db.rawQuery(sql,null);
        if(cur!=null)
        {
            try
            {
                resMap=new Hashtable<>();
                while(cur.moveToNext())
                {
                    String exc_cur_name=cur.getString(cur.getColumnIndex("exchange_currency_name"));
                    Float  rate=cur.getFloat(cur.getColumnIndex("NewestExRate"));
                    String up_datetime=cur.getString(cur.getColumnIndex("NewestPubTime"));
                    String chi_tb_name=cur.getString(cur.getColumnIndex("child_tb_name"));
                    curTbRec record=new curTbRec(exc_cur_name,chi_tb_name,rate,up_datetime);
                    resMap.put(exc_cur_name,record);
                }
            }
            catch (RuntimeException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("connect mysql failure"+"exception msg:"+ExceMsg);
            }
            finally {
                cur.close();
                db.close();
            }
        }
        else {
            log.error("get SQLite DB Cursor failure.");
            db.close();
        }
        return resMap;
    }

    public static Map<String,allCurTbRec> get_allCurTbMap_FromSQLite()
    {
        if(isTbExistInSQLiteDB(Table.allCurTbName))
           return null;
        Map<String,allCurTbRec> resMap=null;
        boolean m_resStatus=false;
        SQLiteDatabase db=get_db();
        if(db==null)
        {
            log.error("cannot open SQLite DB ");
            return null;
        }
        Cursor cur=db.rawQuery(selectAllCurTb,null);
        if(cur!=null)
        {
            try
            {
                resMap=new Hashtable<>();
                while(cur.moveToNext())
                {
                    String cur_name=cur.getString(cur.getColumnIndex("currency_name"));
                    String update_datetime=cur.getString(cur.getColumnIndex("update_datetime"));
                    String currency_tb_name=cur.getString(cur.getColumnIndex("currency_tb_name"));
                    allCurTbRec record=new allCurTbRec(cur_name,currency_tb_name,update_datetime);
                    resMap.put(cur_name,record);
                }
            }
            catch (RuntimeException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                return null;
            }
            finally {
                cur.close();
                db.close();
            }
        }
        else
        {
            db.close();
        }
        return resMap;
    }
    public static List<curTbRec> get_One_CurTb_AllRecordList(String curName)
    {
        if(curName==null)
        {
            log.error("method formal parameter is null.");
            return null;
        }
        String curTbName=curName+"_tb";
        if(!isTbExistInSQLiteDB(curTbName))
        {
            return null;
        }
        List<curTbRec> resList=null;
        boolean m_resStatus=false;
        SQLiteDatabase db=get_db();
        if(db==null)
        {
            log.error("cannot open SQLite DB .");
            return null;
        }
        String sql=String.format(selectRecFromCurTb,curTbName);
        Cursor cur=db.rawQuery(sql,null);
        if(cur!=null)
        {
            try
            {
                 resList=new LinkedList<>();
                while(cur.moveToNext())
                {
                    String exc_cur_name=cur.getString(cur.getColumnIndex("exchange_currency_name"));
                    Float  rate=cur.getFloat(cur.getColumnIndex("NewestExRate"));
                    String up_datetime=cur.getString(cur.getColumnIndex("NewestPubTime"));
                    String chi_tb_name=cur.getString(cur.getColumnIndex("child_tb_name"));
                    curTbRec record=new curTbRec(exc_cur_name,chi_tb_name,rate,up_datetime);
                    resList.add(record);
                }
            }
            catch (RuntimeException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                return null;
            }
            finally {
                cur.close();
                db.close();
            }
        }
        else {
            log.error("get SQLite DB Cursor failure.");
            db.close();
        }
        return resList;
    }
    public static Map<String,curTbRec> get_All_CurTbRec_Map()
    {
        Map<String,curTbRec> tempMap=new Hashtable<>();
        int size=Data.currencyList.size();
        for(int i=0;i<size;i++)
        {
            String name=Data.currencyList.get(i);
            List<curTbRec> res= get_One_CurTb_AllRecordList(name);
            if(res==null)
            {
                log.error("update "+name+" rate data failure");
                return null;
            }
            for(int j=0;j<res.size();j++)
            {
                curTbRec tempRec=res.get(j);
                tempMap.put(name+"_"+tempRec.excCurName,tempRec);
            }
        }
        if(tempMap.size()<=0)
            return null;
        return tempMap;
    }
    public static List<String> get_SQLiteDBAllTbName_List()
    {
        return tbNameList;
    }
    public static Map<String,Map<String,curTbRec> > get_All_CurTbMap_Map()
    {
        Map<String,Map<String,curTbRec> > tempMap=new Hashtable<>();
        int size=Data.currencyList.size();
        for(int i=0;i<size;i++)
        {
            String name=Data.currencyList.get(i);
            Map<String,curTbRec> resMap = get_One_CurTb_fromSQLite(name);
            if(resMap==null)
            {
                log.error("get "+name+"_tb rate data failure");
                return null;
            }
            tempMap.put(name,resMap);
        }
        if(tempMap.size()<=0)
            return null;
        return  tempMap;
    }
    private static int  isExist_TrendRateUpRec(String TrendTb_name)
    {
        if(!isTbExistInSQLiteDB("TrendRate_UpdateRecord_tb")||TrendTb_name==null)
        {
            log.error("TrendRate_UpdateRecord_tb is not exist or TrendTb_name==null");
            return -1;
        }
        SQLiteDatabase db=get_db();
        if(db==null)
        {
            log.error("cannot open SQLite DB .");
            return -1;
        }
        int res;
        String sql=String.format(selectTRUpRec_TRName,TrendTb_name);
        Cursor cur=db.rawQuery(sql,null);
        if(cur!=null)
        {
           if(cur.getCount()>0)
               res=1;
            else
               res=0;
            cur.close();
            db.close();
        }
        else {
            log.error("get SQLite DB Cursor failure.");
            db.close();
            return -1;
        }
        return res;
    }
    private static boolean changeTrendRateUpRec(String sql)
    {
        return sql!=null&&SQLiteManager.SQLite_exeSQL(sql);
    }

    private static boolean update_TrendRateUpRec(boolean recState,boolean isFirstUpdate,String TrendTb_name,String oldestDate,String newestDate)
    {
        String sql;
        if(!recState) //插入
            sql=String.format(insertTRUpRec,TrendTb_name,newestDate,oldestDate);
        else// 更新最新 最老 时间
        {
            if(isFirstUpdate)
                sql=String.format(updateTRUpRec,newestDate,oldestDate,TrendTb_name);
            else
                sql=String.format(updateTRUpRec_newest,newestDate,TrendTb_name);
        }
        return changeTrendRateUpRec(sql);
    }
    private static String get_Update_TrendRateUpRec_Sql(boolean recState,boolean isFirstUpdate,String TrendTb_name,String oldestDate,String newestDate)
    {
        String sql;
        if(!recState) //插入
            sql=String.format(insertTRUpRec,TrendTb_name,newestDate,oldestDate);
        else// 更新最新 最老 时间
        {
            if(isFirstUpdate)
                sql=String.format(updateTRUpRec,newestDate,oldestDate,TrendTb_name);
            else
                sql=String.format(updateTRUpRec_newest,newestDate,TrendTb_name);
        }
        return sql;
    }
    public static boolean save_TrendRateList_To_SQLite(String curName, String exchCurName, List<childTbRec> valuesList)
    {
        if(curName==null||exchCurName==null||valuesList==null||valuesList.size()<=0)
        {
            log.error("参数不能为空");
            return false;
        }
        String TrendTb_name=curName+"_"+exchCurName+"_tb";
        String sql;
        boolean TbIsExist=isTbExistInSQLiteDB(TrendTb_name);
        //判断是否是第一次更新
        int isExist=isExist_TrendRateUpRec(TrendTb_name);
        if(isExist<0)
        {
            log.error(" get TrendRateUpRec："+TrendTb_name+"error ");
            return false;
        }
        boolean isFirstUpdate=false;
        if(isExist==1&&TbIsExist)
            isFirstUpdate=false;
        else if(isExist==1&&!TbIsExist)
            isFirstUpdate=true;
        else if(isExist==0&&TbIsExist)
        {
            isFirstUpdate=true;
            TbIsExist=dropTableFromSQLiteDB(TrendTb_name);
        }
        else if(isExist==0&&!TbIsExist)
            isFirstUpdate=true;
        if(!TbIsExist)
        {
            if(!create_TrendRateUpdate_tb(TrendTb_name))
            {
                log.error("TrendTb_name:"+TrendTb_name+" is not exsit and cannot create it");
                return false;
            }
        }
        SQLiteDatabase db=get_db();
        if(db==null)
            return false;
        db.beginTransaction();
        try
        {
            for(childTbRec Rec:valuesList)
            {
                sql=String.format(Locale.getDefault(),insertRecToTrendTb,TrendTb_name,Rec.rate,Rec.UpTime);
                db.execSQL(sql);
            }
            String startDate=valuesList.get(0).UpTime;
            String stopDate=valuesList.get(valuesList.size()-1).UpTime;
            sql=get_Update_TrendRateUpRec_Sql(isExist==1,isFirstUpdate,TrendTb_name,startDate,stopDate);
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (SQLException e){
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("update currency_tb all record failure;"+ExceMsg);
            return false;
        }
        finally {
            db.endTransaction();
            db.close();
        }
        return true;//update_TrendRateUpRec(isExist==1,isFirstUpdate,TrendTb_name,startDate,stopDate);
    }
    public static LinkedList<trendLinePoint> get_TrendRate_List(String rateName,String startDate,String stopDate)
    {
        if(rateName==null||startDate==null||stopDate==null)
        {
            return null;
        }
        if(!isTbExistInSQLiteDB(rateName))
        {
            log.error("rateName :"+rateName+" is not exsit");
            return null;
        }
        LinkedList<trendLinePoint> resList=null;
        boolean m_resStatus=false;
        SQLiteDatabase db=get_db();
        if(db==null)
        {
            log.error("cannot open SQLite DB ");
            return null;
        }
        String sql=String.format(selectRateList,rateName,startDate,stopDate);
        Cursor cur=db.rawQuery(sql,null);
        if(cur!=null)
        {
            try
            {
                resList=new LinkedList<>();
                DateFormat  df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
                while(cur.moveToNext())
                {
                    Float  rate=cur.getFloat(cur.getColumnIndex("Exchange_Rate"))*100;
                    String  dateStr=cur.getString(cur.getColumnIndex("PubTime"));
                    Date date;
                    try{
                        date=df.parse(dateStr);
                    }
                    catch (ParseException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("ParseException exception msg:"+ExceMsg);
                        continue;
                    }
                    trendLinePoint record=new trendLinePoint(date,rate);
                    resList.add(record);
                }
            }
            catch (RuntimeException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                return null;
            }
            finally {
                cur.close();
                db.close();
            }
        }
        else {
            log.error("get SQLite DB Cursor failure.");
            db.close();
        }
        return resList;
    }
    public static Map<String,TR_UpRec_Rec> get_TR_UpRec_Map()
    {
        if(!isTbExistInSQLiteDB("TrendRate_UpdateRecord_tb"))
        {
            log.error("TrendRate_UpdateRecord_tb is not exsit");
            return null;
        }
        Map<String,TR_UpRec_Rec> resMap=null;
        SQLiteDatabase db=get_db();
        if(db==null)
            return null;
        Cursor cur=db.rawQuery(selectTR_UpRec,null);
        if(cur!=null)
        {
            try
            {
                resMap=new Hashtable<>();
                while(cur.moveToNext())
                {
                    String  TRName=cur.getString(cur.getColumnIndex("TRName"));
                    String  newestPubTime=cur.getString(cur.getColumnIndex("newestPubTime"));
                    String  oldestPubTime=cur.getString(cur.getColumnIndex("oldestPubTime"));
                    TR_UpRec_Rec record=new TR_UpRec_Rec(TRName,oldestPubTime,newestPubTime);
                    resMap.put(TRName,record);
                }
            }
            catch (RuntimeException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                return null;
            }
            finally {
                cur.close();
                db.close();
            }
        }
        else {
            log.error("get SQLite DB Cursor failure.");
            db.close();
        }
        return resMap;
    }
}

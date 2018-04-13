
package com.example.fly.graduationapp.SQLite;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.Table;
import com.example.fly.graduationapp.Data.allCurTbRec;
import com.example.fly.graduationapp.Data.curTbRec;
import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.MainActivity;
import com.example.fly.graduationapp.MyApplication;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SQLiteManager {
    private  static Logger log=Logger.getLogger(SQLiteManager.class);
    public static final String createChild_tb="create table if not exists %s(" +
            "_id integer primary key autoincrement,"+
            "Exchange_Rate FLOAT(24.6) NOT NULL,"+
            "PubTime datetime NOT NULL )";
    public static final String createCurTb="create table if not exists %s(" +
            "_id integer primary key autoincrement,"+
            "exchange_currency_name VARCHAR(20) NOT NULL,"+
            "child_tb_name VARCHAR(20) NOT NULL,"+
            "NewestExRate FLOAT(24.6) NOT NULL,"+
            "NewestPubTime datetime NOT NULL )";
    public static final String createAllCurTb="create table if not exists all_currency_tb(" +
            "_id integer primary key autoincrement,"+
            "currency_name VARCHAR(20) NOT NULL,"+
            "update_datetime datetime NOT NULL,"+
            "currency_tb_name VARCHAR(20) NOT NULL )";
    private static final String insertRecToAllCurTb="insert into %s (currency_name,update_datetime,currency_tb_name) values(\'%s\',\'%s\',\'%s\')";
    private static final String insertRecToCurTb="insert into %s (exchange_currency_name,child_tb_name,NewestExRate,NewestPubTime) values(\'%s\',\'%s\',%f,\'%s\')";
    private static final String selectRecFromCurTb="select exchange_currency_name,child_tb_name,NewestExRate,NewestPubTime from %s";
    private  static List<String> tbNameList;
    private  static Map<String,allCurTbRec> m_allCurTbMap;
    //test success
    private static SQLiteDatabase get_db(){
        MyDBHelper DBHelper;
        SQLiteDatabase m_DB;
        DBHelper=new MyDBHelper(MyApplication.getAppContext());
        m_DB=DBHelper.getWritableDatabase();
        if(m_DB==null){
            log.error("getWritableDatabase  failure;");
        }
        return m_DB;
    }
    //test success
    private static boolean dropTableFromSQLiteDB(String tb_name)
    {
        return tb_name!=null&&SQLiteManager.SQLite_exeSQL("drop table if exists "+tb_name);
    }
    //test success
    private static boolean createTableInSQLite(String sql)
    {
        return sql!=null&&SQLiteManager.SQLite_exeSQL(sql);
    }
    //test success
    private static boolean tbExistInSQLiteDB(String tb_name){
        int size=tbNameList.size();
        for(int i=0;i<size;i++){
            if(tbNameList.get(i)==tb_name){
                return true;
            }
        }
        return true;
    }
    private static boolean updateAllTbNameListFromSQLite(){
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
    public static boolean updateAllCurTb_to_SQLiteDB()
    {
        boolean status=false;
        List<allCurTbRec> tempList=null;
        try
        {
            if(Data.allCurTbMap_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                tempList=new ArrayList<>(Data.allCurTbMap.values());
                Data.allCurTbMap_lock.readLock().unlock();
            }
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
        }
        if(tempList==null) {
            return false;
        }
        if(dropTableFromSQLiteDB(Table.allCurTbName))
        {
            if(createTableInSQLite(createAllCurTb))
            {
                SQLiteDatabase db=get_db();
                if(db==null) {
                    return false;
                }
                db.beginTransaction();
                try
                {
                    for(allCurTbRec record:tempList)
                    {
                        String sql=String.format(insertRecToAllCurTb,Table.allCurTbName,record.CurName,record.CurUpdTime,record.CurTbName);
                        db.execSQL(sql);
                    }
                    db.setTransactionSuccessful();
                    status=true;
                }
                catch (SQLException e){
                    String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                    log.error("update all_currency_tb all record failure;"+ExceMsg);
                }
                finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }
        return status;
    }
    public static void updateAll_CurTbs_to_SQLiteDB()
    {
        boolean status;
        for(String name:Data.currencyList)
        {
            status=updateOneCurTb_to_SQLiteDB(name);
            if(!status)
            {
                log.error(String.format("update %s cur_tb failure ",name));
            }
        }
    }
    public static boolean updateOneCurTb_to_SQLiteDB(String cur_name)
    {
        boolean status=false;
        List<curTbRec> tempList=null;
        try
        {
            if(Data.curTbsRec_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                tempList=new ArrayList<>(Data.curTbsRec.get(cur_name).values());
                Data.curTbsRec_lock.readLock().unlock();
            }
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
        }
        if(tempList==null)
            return false;
        String cur_tb_name=cur_name+"_tb";
        if(dropTableFromSQLiteDB(cur_tb_name))
        {
            String sql=String.format(createCurTb,cur_tb_name);
            if(createTableInSQLite(sql))
            {
                SQLiteDatabase db=get_db();
                if(db==null)
                    return false;
                db.beginTransaction();
                try
                {
                    for(curTbRec record:tempList)
                    {
                        sql=String.format(Locale.getDefault(),insertRecToCurTb,cur_tb_name,record.excCurName,record.chiTbName,record.excRate,record.newestUpTime);
                        db.execSQL(sql);
                    }
                    db.setTransactionSuccessful();
                    status=true;
                }
                catch (SQLException e){
                    String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                    log.error("update currency_tb all record failure;"+ExceMsg);
                }
                finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }
        return status;
    }

    public static boolean initSQLiteDB(){
        //tbNameList= SQLiteManager.get_SQLiteDb_all__tb();
        //dropTableFromSQLiteDB("Book");
        return true;
    }
    //test success
    public static List<String> get_SQLiteDB_TbNameList()
    {
        updateAllTbNameListFromSQLite();
        return tbNameList;
    }
    public  static boolean updateAll_Data_CurTbMap_fromSQLite()
    {
        boolean status=false;
        Map<String,Map<String,curTbRec> > tempMap=new Hashtable<>();
        int size=Data.currencyList.size();
        for(int i=0;i<size;i++)
        {
            String name=Data.currencyList.get(i);
            Map<String,curTbRec> resMap = SQLiteManager.update_One_Data_CurTbMap_fromSQLite(name);
            if(resMap==null)
            {
                log.error("update "+name+" rate data failure");
                continue;
            }
            tempMap.put(name,resMap);
        }
        try
        {
            if(Data.curTbsRec_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.curTbsRec!=null)
                    Data.curTbsRec.clear();
                Data.curTbsRec=tempMap;
                status=true;
                Data.allCurTbMap_lock.readLock().unlock();
            }
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
        }
        return status;
    }

    //test success
    public  static Map<String,curTbRec> update_One_Data_CurTbMap_fromSQLite(String curName)
    {
        Map<String,curTbRec> resMap=null;
        boolean m_resStatus=false;
        if(curName==null)
        {
            log.error("method formal parameter is null.");
            return null;
        }
        SQLiteDatabase db=get_db();
        if(db==null)
        {
            log.error("cannot open SQLite DB .");
            return null;
        }
        String sql=String.format(selectRecFromCurTb,curName+"_tb");
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
    //test success
    public static boolean updateDada_TbNameList_FromSQLite()
    {
        boolean status=false;
        List<String> TbNameList=new LinkedList<>(get_SQLiteDB_TbNameList());
        try
        {
            status=Data.allTbList_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.writeLock() exception:"+ExceMsg);
            return false;
        }
        if(status)
        {
            if(Data.allTbList!=null)
                Data.allTbList.clear();
            Data.allTbList=TbNameList;
            status=true;
            Data.allTbList_lock.writeLock().unlock();
        }
        else
        {
            log.error("get Data.allTbList_lock.writeLock() failure ");
        }
       return status;
    }

}

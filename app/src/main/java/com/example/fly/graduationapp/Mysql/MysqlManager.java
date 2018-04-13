package com.example.fly.graduationapp.Mysql;

import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.allCurTbRec;
import com.example.fly.graduationapp.Data.curTbRec;
import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.SQLite.SQLiteManager;

import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MysqlManager {
    private  static Logger log=Logger.getLogger(MysqlManager.class);
    //test success
    public static boolean updateAllCurrencyTb() {
        boolean m_resStatus=false;
        Map<String,allCurTbRec> tempMap=null;
        List<String> tempList=null;
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {
                ResultSet res= Mysql.query("select * from all_currency_tb");
                if (res == null)
                {
                    log.error("get all_currency_tb record failure:");
                    Mysql.closeStatement();
                    Mysql.closeConn();
                    return false;
                }
                else
                {
                    try
                    {
                        tempMap=new Hashtable<>();
                        tempList=new LinkedList<>();
                        while(res.next())
                        {
                            String cur_name=res.getString("currency_name");
                            String up_datetime=res.getString("update_datetime");
                            String cur_tb_name=res.getString("currency_tb_name");
                            allCurTbRec record=new allCurTbRec(cur_name,up_datetime,cur_tb_name);
                            tempList.add(cur_name);
                            tempMap.put(cur_name,record);
                        }
                    }
                    catch (SQLException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                        return false;
                    }
                    finally {
                        Mysql.closeStatement();
                        Mysql.closeConn();
                    }
                }
            }
            else
            {
                Mysql.closeConn();
            }
        }
        //加锁
        if(tempMap==null||tempList==null)
            return false;
        try
        {
            if(Data.allCurTbMap_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.allCurTbMap!=null)
                    Data.allCurTbMap.clear();
                Data.allCurTbMap=tempMap;
                if(Data.currencyList!=null)
                    Data.currencyList.clear();
                Data.currencyList=tempList;
                m_resStatus=true;
            }
            else
            {
                log.error("get Data.allCurTbMap_lock.writeLock() failure ");
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allCurTbMap_lock.writeLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.allCurTbMap_lock.writeLock().unlock();
        }
        return m_resStatus;
    }
    //test success
    public static List<curTbRec> updateCurTb(String curName)
    {
        if(curName==null)
            return null;
        List<curTbRec> tempList=new ArrayList<>();
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {
                String sql= String.format("select * from %s_tb",curName);
                ResultSet res= Mysql.query(sql);
                if (res == null)
                {
                    log.error("get currency_tb "+curName+"_tb record failure:");
                    Mysql.closeStatement();
                    Mysql.closeConn();
                    return null;
                }
                else
                {
                    try
                    {
                        while(res.next())
                        {
                            String exc_cur_name=res.getString("exchange_currency_name");
                            Float  rate=res.getFloat("NewestExRate");
                            String up_datetime=res.getString("NewestPubTime");
                            String chi_tb_name=res.getString("child_tb_name");
                            curTbRec record=new curTbRec(exc_cur_name,chi_tb_name,rate,up_datetime);
                            tempList.add(record);
                        }
                    }
                    catch (SQLException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                        return null;
                    }
                    finally {
                        Mysql.closeStatement();
                        Mysql.closeConn();
                    }
                }
            }
            else {
                Mysql.closeConn();
            }
        }
        if(tempList.size()>0)
            return tempList;
        else
            return null;
    }
    //test success
    public static curTbRec updateOneRateData(String curName,String excCurName)
    {
        curTbRec record=null;
        if(curName==null||excCurName==null)
            return record;
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {
                //String sql="select * from "+curName"_tb "+"where exchange_currency_name=\'"+excCurName+"\'";
                String sql= String.format("select * from %s_tb where exchange_currency_name=\'%s\' ",curName,excCurName);
                ResultSet res= Mysql.query(sql);
                if (res == null)
                {
                    log.error("get currency_tb record failure:");
                    Mysql.closeStatement();
                    Mysql.closeConn();
                    return record;
                }
                else
                {
                    try
                    {
                        while(res.next())
                        {
                            Float  rate=res.getFloat("NewestExRate");
                            String up_datetime=res.getString("NewestPubTime");
                            String chi_tb_name=res.getString("child_tb_name");
                            record=new curTbRec(excCurName,chi_tb_name,rate,up_datetime);
                            //String exc_rate_name=curName+"_"+excCurName;
                            //Data.exchangeRate.put(exc_rate_name,record);
                        }
                    }
                    catch (SQLException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                        return null;
                    }
                    finally {
                        Mysql.closeStatement();
                        Mysql.closeConn();
                    }
                }
            }
            else
            {
                Mysql.closeConn();
            }
        }
        return record;
    }
    //test success
    public static Map<String,curTbRec> getOneCurTbDataFromMysqlDB(String curName)
    {
        if(curName==null)
            return null;
        Map<String,curTbRec> resMap=null;
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {
                String sql= String.format("select * from %s_tb",curName);
                ResultSet res= Mysql.query(sql);
                if (res == null)
                {
                    log.error("get currency_tb "+curName+"_tb record failure:");
                    Mysql.closeStatement();
                    Mysql.closeConn();
                    return null;
                }
                else
                {
                    try
                    {
                        resMap=new Hashtable<>();
                        while(res.next())
                        {
                            String exc_cur_name=res.getString("exchange_currency_name");
                            Float  rate=res.getFloat("NewestExRate");
                            String up_datetime=res.getString("NewestPubTime");
                            String chi_tb_name=res.getString("child_tb_name");
                            curTbRec record=new curTbRec(exc_cur_name,chi_tb_name,rate,up_datetime);
                            resMap.put(exc_cur_name,record);
                        }
                    }
                    catch (SQLException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                        return null;
                    }
                    finally {
                        Mysql.closeStatement();
                        Mysql.closeConn();
                    }
                }
            }
            else
            {
                Mysql.closeConn();
            }
       }
        return resMap;
    }
    //test success
    public static boolean getAllCurTbDataFromMysqlDB()
    {
        boolean status=false;
        Map<String,Map<String,curTbRec> > tempMap=new Hashtable<>();
        int size=Data.currencyList.size();
        for(int i=0;i<size;i++)
        {
            String name=Data.currencyList.get(i);
            Map<String,curTbRec> resMap = MysqlManager.getOneCurTbDataFromMysqlDB(name);
            if(resMap==null)
            {
                log.error("update "+name+" rate data failure");
                continue;
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
    public static boolean updateAllRateData()
    {
        Map<String,curTbRec> tempMap=new Hashtable<>();
        int size=Data.currencyList.size();
        for(int i=0;i<size;i++)
        {
            String name=Data.currencyList.get(i);
            List<curTbRec> res= MysqlManager.updateCurTb(name);
            if(res==null)
            {
                log.error("update "+name+" rate data failure");
               continue;
            }
            for(int j=0;j<res.size();j++)
            {
                curTbRec tempRec=res.get(j);
                tempMap.put(name+"_"+tempRec.excCurName,tempRec);
            }
        }
        if(tempMap.size()<=0)
            return false;
        try
        {
            if(Data.exchangeRate_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.exchangeRate!=null)
                    Data.exchangeRate.clear();
                Data.exchangeRate=tempMap;
            }
            else
                log.error("get Data.allTbList_lock.writeLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.writeLock() exception:"+ExceMsg);
            return false;
        }
        finally {
            Data.exchangeRate_lock.writeLock().unlock();
        }
        return true;
    }
    //test success
    public static boolean updateMysqlDBAllTbList()
    {
        boolean m_resStatus=false;
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {

                ResultSet res= Mysql.query("show tables");
                if (res == null)
                {
                    log.error("get all tb name  failure:");
                    Mysql.closeStatement();
                    Mysql.closeConn();
                    return false;
                }
                else
                {   List<String> newList=new LinkedList<>();
                    try
                    {
                        while(res.next())
                        {
                            String tb_name=res.getString("TABLE_NAME");
                            newList.add(tb_name);
                            //log.info(tb_name);
                        }
                        log.info("get data success");
                    }
                    catch (SQLException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("get all tb name data  failure:"+ExceMsg);
                        return false;
                    }
                    finally {
                        Mysql.closeStatement();
                        Mysql.closeConn();
                    }
                    try
                    {
                        if(Data.allTbList_lock.writeLock().tryLock(10, TimeUnit.MILLISECONDS))
                        {
                            if(Data.allTbList!=null)
                                Data.allTbList.clear();
                            Data.allTbList=newList;
                            m_resStatus=true;
                        }
                        else
                            log.error("get Data.allTbList_lock.writeLock() failure ");
                    }
                    catch (InterruptedException e)
                    {
                        String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                        log.error("get Data.allTbList_lock.writeLock() exception:"+ExceMsg);
                        return false;
                    }
                    finally {
                        Data.allTbList_lock.writeLock().unlock();
                    }
                }
            }
            else
            {
                Mysql.closeConn();
            }
        }
        return m_resStatus;
    }
}

package com.example.fly.graduationapp.Mysql;

import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.TR_UpRec_Rec;
import com.example.fly.graduationapp.Data.allCurTbRec;
import com.example.fly.graduationapp.Data.childTbRec;
import com.example.fly.graduationapp.Data.curTbRec;
import com.example.fly.graduationapp.GetExceptionMsg;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MysqlManager {
    private static final String selectRateList_CNY="select MiddleRate ,PubTime from %s where PubTime>\'%s\' ORDER BY PubTime ASC LIMIT 500";
    private static final String selectRateList_Other="select * from %s where PubTime>\'%s\' ORDER BY PubTime ASC LIMIT 500";//Exchange_Rate
    private static final String selectTR_Max="select MAX(PubTime) AS max from %s";//Exchange_Rate
    private static final String selectTR_Min="select MIN(PubTime) AS min from %s";
    private  static Logger log=Logger.getLogger(MysqlManager.class);
    private static List<String> MysqlDBAllTbNameList=new LinkedList<>();
    public static boolean initMysqlDB()
    {
        if(!MysqlManager.updateMysqlDBAllTbList()) //得到mysql db 所有表的名字list
        {
            log.error("init MysqlDBAllTbNameList failure");
            return false;
        }
        return true;
    }

    public static boolean isExistTbInMysqlDB(String tbName)
    {
        if(tbName==null)
            return false;
        if(MysqlDBAllTbNameList==null)
            updateMysqlDBAllTbList();
        for(String ele:MysqlDBAllTbNameList)
        {
            if(ele.equals(tbName))
            {
                return true;
            }
        }
        return false;
    }

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
    public void getTableNameByCon() {

    }
    public static boolean updateMysqlDBAllTbList()
    {
        boolean m_resStatus=false;
        List<String> newList=null;
        if(Mysql.openConn())
        {
            Connection conn=Mysql.getConn();
            try
            {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
                newList=new LinkedList<>();
                while(res.next())
                {
                    String tb_name=res.getString(3);
                    newList.add(tb_name);
                }
                log.info("get data success");
                res.close();
            }
            catch (SQLException e)
            {
                String ExceMsg= GetExceptionMsg.getExcpMsg(e);
                log.error("get Data.allTbList_lock.writeLock() exception:"+ExceMsg);
                return false;
            }
            finally
            {
                Mysql.closeConn();
            }
        }
        if(newList==null)
        {
            return false;
        }
        if(MysqlDBAllTbNameList!=null)
            MysqlDBAllTbNameList.clear();
        MysqlDBAllTbNameList=newList;
        return true;
    }
    public static List<childTbRec> getRateList(String curName,String exchCurName,String startDate)
    {
       if(curName==null||exchCurName==null||startDate==null)
           return null;

        boolean m_resStatus=false;
        List<childTbRec> resList=null;
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {
                String sql;
                String rateColName;
                String tbName=curName+"_"+exchCurName+"_tb";
                boolean isCNY=false;
                if("CNY".equals(curName))
                {
                    sql=String.format(selectRateList_CNY,tbName,startDate);
                    rateColName="MiddleRate";
                    isCNY=true;
                }
                else
                {
                    sql=String.format(selectRateList_Other,tbName,startDate);
                    rateColName="Exchange_Rate";
                    isCNY=false;
                }
                ResultSet res= Mysql.query(sql);
                if (res == null)
                {
                    log.error("get child_tb "+tbName+" record failure:");
                    Mysql.closeStatement();
                    Mysql.closeConn();
                    return null;
                }
                else
                {
                    try
                    {
                        float  rate=0;
                        String up_datetime;
                        childTbRec record;
                        resList=new LinkedList<>();
                        while(res.next())
                        {
                            if(isCNY)
                                rate=1/res.getFloat(rateColName);
                            else
                                rate=res.getFloat(rateColName);
                            up_datetime=res.getString("PubTime");
                            record=new childTbRec(rate,up_datetime);
                            resList.add(record);
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
        return resList;
    }

    //只能在 特定函数get_TR_UpRec_Map() 中使用，请勿 在其他地方调用
    private static TR_UpRec_Rec get_TR_UpRec_Rec(String tbName)
    {
        if(tbName==null)
            return null;
        if(!isExistTbInMysqlDB(tbName))
            return null;
        String newestDate=null;
        String sql=String.format(selectTR_Max,tbName);
        ResultSet res= Mysql.query(sql);
        if (res == null)
            return null;
        try
        {
            while(res.next())
                newestDate=res.getString("max");
            res.close();
        }
        catch (SQLException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("connect mysql failure"+"exception msg:"+ExceMsg);
            return null;
        }
        String oldestDate=null;
        sql=String.format(selectTR_Min,tbName);
        res= Mysql.query(sql);
        if (res == null)
            return null;
        try
        {
            while(res.next())
                oldestDate=res.getString("max");
            res.close();
        }
        catch (SQLException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("connect mysql failure"+"exception msg:"+ExceMsg);
            return null;
        }
        if(oldestDate==null||newestDate==null)
            return null;
        return  new TR_UpRec_Rec(tbName,oldestDate,newestDate);
    }
    public static Map<String,TR_UpRec_Rec> get_TR_UpRec_Map()
    {
        List<String> CurList=Data.get_currencyList_Copy();
        if(CurList==null)
            return null;
        boolean m_resStatus=false;
        Map<String,TR_UpRec_Rec> resMap=null;
        if(Mysql.openConn())
        {
            if(Mysql.createOnlyReadStatement())
            {
                String sql;
                String tbName;
                resMap=new Hashtable<>();
                for(String ele_1 :CurList)
                {
                    for(String ele_2 :CurList)
                    {
                        if(ele_1.equals(ele_2))
                            continue;
                        tbName=ele_1+"_"+ele_2+"_tb";
                        TR_UpRec_Rec rec=get_TR_UpRec_Rec(tbName);
                        if(rec!=null)
                            resMap.put(tbName,rec);
                    }
                }
                Mysql.closeStatement();
            }
            else
            {
                Mysql.closeConn();
            }
        }
        return resMap;
    }

}

package com.example.fly.graduationapp.Utils;

import android.widget.Toast;

import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.curTbRec;
import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.MainActivity;
import com.example.fly.graduationapp.PublicMethod.PubMethod;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 综合数据管理
 */

public class Manager {
    private  static Logger log=Logger.getLogger(MainActivity.class);
    public static double getFromToExchRate(String from,String to)
    {
        double rate=-1;
        if(from==null||to==null)
        {
            Toast.makeText(MainActivity.mContext,"原始货币和目标货币不能为空",Toast.LENGTH_SHORT).show();
            return rate;
        }
        String en_from= PubMethod.fromZHToEN_Map.get(from);
        String en_to= PubMethod.fromZHToEN_Map.get(to);
        try
        {
            if(Data.exchangeRate_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.exchangeRate!=null)
                {
                    curTbRec rec=Data.exchangeRate.get(en_from+"_"+en_to);
                    if(rec!=null)
                    {
                        rate=rec.excRate;
                    }
                }
                else
                {
                    log.error("Data.exchangeRate has no init ");
                }
            }
            else
                log.error("get Data.exchangeRate_lock.readLock() failure ");
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.allTbList_lock.readLock() exception:"+ExceMsg);
        }
        finally {
            Data.exchangeRate_lock.readLock().unlock();
        }
        if(rate<0)
          Toast.makeText(MainActivity.mContext,"找不到汇率数据",Toast.LENGTH_SHORT).show();
        return rate;
    }

}

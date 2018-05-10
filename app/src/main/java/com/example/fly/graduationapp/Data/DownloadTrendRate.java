package com.example.fly.graduationapp.Data;

import java.util.List;

/**
 * Created by wf on 2018/4/29.
 */

public class DownloadTrendRate {
    public boolean isUpdateAllRate;
    //public TrendRateMsg[] array;
    public List<TrendRateMsg> m_trendRateMsgList;
    public String UpdateAllRateStartDate;
    public  DownloadTrendRate()
    {
        this.isUpdateAllRate=false;
        this.m_trendRateMsgList=null;
        this.UpdateAllRateStartDate=null;
    }
    public  DownloadTrendRate(boolean isUpdateAllRate,List<TrendRateMsg> m_trendRateMsgList,String UpdateAllRateStartDate)
    {
        this.isUpdateAllRate=isUpdateAllRate;
        this.m_trendRateMsgList=m_trendRateMsgList;
        this.UpdateAllRateStartDate=UpdateAllRateStartDate;
    }
}

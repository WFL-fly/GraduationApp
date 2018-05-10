package com.example.fly.graduationapp.Data;

/**
 * Created by wf on 2018/4/29.
 */

public class TrendRateMsg {
    public String startDate;
    public String fromCurName;
    public String toCurName;
    public TrendRateMsg()
    {
        this.startDate=null;
        this.fromCurName=null;
        this.toCurName=null;
    }
    public TrendRateMsg(String fromCurName, String toCurName,String startDate)
    {
        this.startDate=startDate;
        this.fromCurName=fromCurName;
        this.toCurName=toCurName;
    }
}

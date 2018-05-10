package com.example.fly.graduationapp.Utils;

import java.util.Date;

/**
 * Created by wf on 2018/4/25.
 */

public class trendLineSelect {
    public String fromCur;
    public String toCur;
    public String strStartDate;
    public String strStopDate;
    public trendLineSelect()
    {
        fromCur="";
        toCur="";
        strStartDate="";
        strStopDate="";
    }
    public trendLineSelect(String fromCur,String toCur,String strStartDate,String strStopDate)
    {
        this.fromCur=fromCur;
        this.toCur=toCur;
        this.strStartDate=strStartDate;
        this.strStopDate=strStopDate;
    }

}

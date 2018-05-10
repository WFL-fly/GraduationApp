package com.example.fly.graduationapp.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by wf on 2018/5/2.
 */

public class requestTR {
    public String TRName;
    public Date startDate;
    public Date endDate;

    public requestTR()
    {
        this.TRName=null;
        this.startDate=null;
        this.endDate=null;
    }
    public requestTR(String TRName,Date startDate,Date endDate)
    {
        this.TRName=TRName;
        this.startDate=startDate;
        this.endDate=endDate;
    }
    public requestTR(final requestTR value)
    {
        if(value==null)
            return;
        this.TRName=value.TRName;
        this.startDate=value.startDate;
        this.endDate=value.endDate;
    }
}

package com.example.fly.graduationapp.Data;

import java.util.Date;

/**
 * Created by wf on 2018/4/24.
 */

public class trendLinePoint {
    public Date m_date;
    public float m_rate;

    public trendLinePoint(Date date,float rate)
    {
        this.m_date=date;
        this.m_rate=rate;
    }
    public trendLinePoint()
    {
        this.m_date=new Date();
        this.m_rate=0;
    }
}

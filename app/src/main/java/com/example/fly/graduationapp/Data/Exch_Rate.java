package com.example.fly.graduationapp.Data;

import com.example.fly.graduationapp.PublicMethod.PubMethod;

/**
 * Created by wf on 2018/4/13.
 */

public class Exch_Rate {
    public String mFromCur;
    public String mToCur;
    public double mRate;
    public boolean mgetedRate;
    public Exch_Rate()
    {
        mFromCur="";
        mToCur="";
        mRate=0.0;
        mgetedRate=false;
    }
    public Exch_Rate(String fromCur,String toCur,double rate)
    {
        if(fromCur==null||toCur==null||rate<=0)
        {
            mFromCur="";
            mToCur="";
            mRate=0.0;
            mgetedRate=false;
            return;
        }
        mFromCur=fromCur;
        mToCur=toCur;
        mRate=rate;
        mgetedRate=true;
    }

}

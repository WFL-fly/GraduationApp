package com.example.fly.graduationapp.Data;

import com.example.fly.graduationapp.PublicMethod.PubMethod;

/**
 * Created by wf on 2018/4/13.
 */

public class Exch_Rate {
    public String mFromCur;
    public String mToCur;
    public double mRate;
    public Exch_Rate()
    {}
    public Exch_Rate(String fromCur,String toCur,double rate)
    {
        mFromCur=fromCur;
        mToCur=toCur;
        mRate=rate;
    }

}

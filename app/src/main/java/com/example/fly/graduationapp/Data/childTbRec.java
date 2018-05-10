package com.example.fly.graduationapp.Data;

/**
 * Created by wf on 2018/4/29.
 */

public class childTbRec {
    public String UpTime;
    public float rate;
    public  childTbRec()
    {
        UpTime="";
        rate=0;
    }
    public  childTbRec(float rate,String UpTime)
    {
        this.UpTime=UpTime;
        this.rate=rate;
    }

}

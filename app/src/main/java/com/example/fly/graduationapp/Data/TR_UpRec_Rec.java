package com.example.fly.graduationapp.Data;

/**
 * Created by wf on 2018/4/30.
 */

public class TR_UpRec_Rec {
    public String TR_Name;
    public String oldestDate;
    public String newestDate;
    public TR_UpRec_Rec()
    {
        this.TR_Name=null;
        this.oldestDate=null;
        this.newestDate=null;
    }
    public TR_UpRec_Rec(String TR_Name,String oldestDate,String newestDate)
    {
        this.TR_Name=TR_Name;
        this.oldestDate=oldestDate;
        this.newestDate=newestDate;
    }
    public TR_UpRec_Rec(final TR_UpRec_Rec value)
    {
        if(value==null)
        {
            this.TR_Name=null;
            this.oldestDate=null;
            this.newestDate=null;
        }
        else
        {
            this.TR_Name=value.TR_Name;
            this.oldestDate=value.oldestDate;
            this.newestDate=value.newestDate;
        }
    }
}

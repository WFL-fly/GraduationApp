package com.example.fly.graduationapp.Data;

import java.util.List;

/**
 * Created by wf on 2018/4/29.
 */

public class SaveTrendDataMsg {
    public String fromCurName;
    public String toCurName;
    public List<childTbRec> dataList;
    public SaveTrendDataMsg()
    {
         this.fromCurName=null;
        this.toCurName=null;
        this.dataList=null;
    }
    public SaveTrendDataMsg(String fromCurName,String toCurName,List<childTbRec> dataList)
    {
        this.fromCurName=fromCurName;
        this.toCurName=toCurName;
        this.dataList=dataList;
    }
    public SaveTrendDataMsg(final SaveTrendDataMsg value)
    {
        if(value==null)
            return;
        this.fromCurName=value.fromCurName;
        this.toCurName=value.toCurName;
        this.dataList=value.dataList;
    }
}

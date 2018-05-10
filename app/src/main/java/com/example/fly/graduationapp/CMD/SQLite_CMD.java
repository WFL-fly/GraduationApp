package com.example.fly.graduationapp.CMD;

/**
 * Created by wf on 2018/4/29.
 */

public enum SQLite_CMD {
    SQLite_SAVE_TREND_RATE_DATA(0),
    SQLite_GET_TREND_RATE_DATA(1),
    MYSQL_DOWNLOAD_TREND_RATE_DATA_FINISH(2),
    ADD_TR_DATA(3);
    private static int m_maxValue=3;
    private  int m_value;
    SQLite_CMD(int value){
        this.m_value = value;
    }
    public int getValue()
    {
        return m_value;
    }
    public boolean isValid()
    {
        if(m_value<0||m_value>m_maxValue)
        {
            return false;
        }
        return true;
    }
    static  public SQLite_CMD get_SQLite_CMD(int value)
    {
        if(value<0||value>m_maxValue)
        {
            return null;
        }
        return SQLite_CMD.values()[value];
    }
    static  public boolean isValid(int value)
    {
        if(value<0||value>m_maxValue)
        {
            return false;
        }
        return true;
    }
}
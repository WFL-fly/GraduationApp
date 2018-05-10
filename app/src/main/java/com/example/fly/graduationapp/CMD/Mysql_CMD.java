package com.example.fly.graduationapp.CMD;

public enum Mysql_CMD {
    Mysql_DOWNLOAD_TREND_RATE_DATA(0),
    SQLite_SAVE_TREND_RATE_DATA_FINISH(1);
    private static int m_maxValue=1;
    private  int m_value;
    Mysql_CMD(int value){
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
    static  public Mysql_CMD get_Mysql_CMD(int value)
    {
        if(value<0||value>m_maxValue)
        {
            return null;
        }
        return Mysql_CMD.values()[value];
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
package com.example.fly.graduationapp.CMD;

public enum UIMsg_CMD {
    DATA_INIT_FINISH(0),
    CentreThreadMSGLoop_INIT_FINISH(1),
    CTR_CMD_1(2),
    CTR_CMD_2(3),
    INTERNET_DISCONNECT(4),
    MYSQL_AND_SQLite_CANNOT_GET_DATA(5),
    SQLite_GET_TREND_RATE_DATA_FINISH(6);
    private static int m_maxValue=6;
    private  int m_value;
    UIMsg_CMD(int value){
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
    static  public UIMsg_CMD get_UIMsg_CMD(int value)
    {
        if(value<0||value>m_maxValue)
        {
            return null;
        }
        return UIMsg_CMD.values()[value];
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
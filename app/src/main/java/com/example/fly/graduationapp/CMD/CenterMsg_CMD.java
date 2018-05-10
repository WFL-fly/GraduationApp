package com.example.fly.graduationapp.CMD;

/**
 * Created by wf on 2018/4/13.
 */

public enum CenterMsg_CMD {
    UI_INIT_FINISH(0),
    UI_ACK_INIT_DATA(1),//请求初始化数据
    INTERNET_CONNECTED_UPDATE_DATA(2),//网络连接成功，从mysql 更新数据
    INTERNET_DISCONNECT(3),
    MYSQL_THREAD_MSG_LOOP_FINISH(4),
    MYSQL_DOWNLOAD_TREND_RATE_DATA_FINISH(5),
    SQLITE_THREAD_MSG_LOOP_FINISH(6),
    UI_CMD_1(7),
    UI_CMD_2(8),
    SQLITE_SAVE_TREND_RATE_DATA_FINISH(9);
    private static int m_maxValue=9;
    private  int m_value;
    CenterMsg_CMD(int value){
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
    static  public CenterMsg_CMD get_CenterMsg_CMD(int value)
    {
        if(value<0||value>m_maxValue)
        {
            return null;
        }
        return CenterMsg_CMD.values()[value];
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


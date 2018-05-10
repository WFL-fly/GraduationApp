package com.example.fly.graduationapp.Data;

/**
 * Created by wf on 2018/5/1.
 */

public class AckMsg {
    public int ackIndex;
    public boolean ackResState;
    public Object ackResData;
    public AckMsg()
    {
        this.ackIndex=-1;
        this.ackResState=false;
        this.ackResData=null;
    }
    public AckMsg(int ackIndex,boolean ackResState,Object ackResData)
    {
        this.ackIndex=ackIndex;
        this.ackResState=ackResState;
        this.ackResData=ackResData;
    }
    public AckMsg(final AckMsg value)
    {
        if(value==null)
            return;
        this.ackIndex=value.ackIndex;
        this.ackResState=value.ackResState;
        this.ackResData=value.ackResData;
    }

}

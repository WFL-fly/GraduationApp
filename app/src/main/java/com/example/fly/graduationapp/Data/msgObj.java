package com.example.fly.graduationapp.Data;

import android.os.Handler;

public class msgObj {
    public boolean isACK;
    public Handler ackHandler;
    public Object  result;
    public msgObj()
    {
        this.isACK=false;
        this.ackHandler=null;
        this.result=null;
    }
    public msgObj(boolean isACK,Handler ackHandler,Object res)
    {
        this.isACK=isACK;
        this.ackHandler=ackHandler;
        this.result=res;
    }

}

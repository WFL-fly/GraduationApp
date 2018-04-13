package com.example.fly.graduationapp.Data;

/**
 * Created by wf on 2018/4/7.
 */
import java.lang.*;
public class curTbRec {
    public String excCurName;
    public Float  excRate;
    public String newestUpTime;
    public String chiTbName;
    public curTbRec(){}
    public curTbRec(String exccurname,String chitbname,Float  excrate,String newestuptime){
        excCurName=exccurname;
        chiTbName=chitbname;
        excRate=excrate;
        newestUpTime=newestuptime;
    }
}

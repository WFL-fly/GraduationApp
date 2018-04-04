package com.example.fly.graduationapp;
import java.io.*;
public class GetExceptionMsg {
    public static String getExcpMsg(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}

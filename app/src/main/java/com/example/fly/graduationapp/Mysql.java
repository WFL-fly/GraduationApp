package com.example.fly.graduationapp;
import org.apache.commons.logging.*;
import java.lang.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public  class  Mysql {
    //log 日志生成
    private  static Log log=LogFactory.getLog(Mysql.class);
    //mysql 账户信息
    private  static final String REMOTE_IP="119.23.34.166";
    private  static final String URL="jbdc:mysql://"+REMOTE_IP+"exchange_rate";
    private  static final String USER="android_user1";
    private  static final String PW="us1and";
    //mysql 连接对象
    private static Connection conn=null;
    private static Statement  statement=null;
    private static boolean openConn()
    {
        if(conn!=null)
        {
            log.info("conn connected");
            return true;
        }

        try{
            final String DRIVER_NAME="com.mysql.jdbc.Driver";
            Class.forName(DRIVER_NAME);
            try{
                 conn = DriverManager.getConnection(URL, USER, PW);
                 log.info("connect mysql successful");
                 return true;
            }
            catch (SQLException e) {

                conn = null;
                String ExceMsg=GetExceptionMsg.getExcpMsg(e);
                log.error("connect mysql failure"+"exception msg:"+ExceMsg);
                return false;
            }
        } catch (ClassNotFoundException e) {
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("load jdbc drvicer falure"+ExceMsg);
            return false;
        }
    }
    private static boolean createStatement()
    {
        if(conn==null)
            return  false;
        if(statement!=null)
            return true;
        try{
            statement=conn.createStatement();
        }
        catch (SQLException e){
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("Statement interface falure"+ExceMsg);
        }
        return true;
    }
    public static ResultSet query(String sql){
        ResultSet resultSet=null;
        if(conn==null||statement==null)
        {
            return resultSet;
        }
        try{
            resultSet=statement.executeQuery(sql);
        }
        catch (NullPointerException e){
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("NullPointerException"+ExceMsg);
        }
        catch (SQLException e){
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("executeQuery falure"+ExceMsg);
        }
        return resultSet;
    }
    public static boolean closeConn(){
        if (statement!=null)
        {
            try {
                statement.close();
                statement=null;
            }
            catch (SQLException e)
            {
                String ExceMsg=GetExceptionMsg.getExcpMsg(e);
                log.error("close statement failure ,"+ExceMsg);
                return  false;
            }
        }
        if (conn!=null)
        {
            try {
                conn.close();
                conn=null;
            }
            catch (SQLException e)
            {
                String ExceMsg=GetExceptionMsg.getExcpMsg(e);
                log.error("close conn failure ,"+ExceMsg);
                return  false;
            }
        }
        log.info("close mysql connection successful");
        return  true;
    }
    public static ResultSet getAll_Currency_tb() {
        ResultSet res = query("select * from all_currency_tb");
        if (res == null) {
            log.error("get all_currency_tb record failure:");
        }
        return res;
    }
}

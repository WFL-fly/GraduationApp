package com.example.fly.graduationapp.Mysql;

import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.SQLite.SQLiteManager;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public  class  Mysql {
    //log 日志生成
    private  static Logger log=Logger.getLogger(Mysql.class);
    //mysql 账户信息
    private  static final String URL="jdbc:mysql://119.23.34.166:3306/exchange_rate";
    private  static final String USER="android_user1";
    private  static final String PW="us1and";
    //mysql 连接对象
    private static Connection conn=null;
    private static Statement  statement=null;
    public static boolean openConn()
    {
        if(conn!=null)
        {
            log.info("conn connected");
            return true;
        }
        try
        {
            final String DRIVER_NAME="com.mysql.jdbc.Driver";
            Class.forName(DRIVER_NAME);
        }
        catch (ClassNotFoundException e)
        {
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("load jdbc drvicer falure"+ExceMsg);
            return false;
        }
        try
        {
            conn = DriverManager.getConnection(URL, USER, PW);
        }
        catch (SQLException e)
        {
            conn = null;
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("connect mysql failure"+"exception msg:"+ExceMsg);
            return false;
        }
        log.info("connect mysql successful");
        return true;
    }
    public static boolean createUpdateStatement()
    {
        if(conn==null)
            return  false;
        if(statement!=null)
        {
            return true;
        }
        try
        {
            statement=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
        }
        catch (SQLException e)
        {
            statement=null;
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error(" create statement interface falure"+ExceMsg);
            return false;
        }
        log.info("create statement success");
        return true;
    }

    public static boolean createOnlyReadStatement()
    {
        if(conn==null)
            return  false;
        if(statement!=null)
            return true;
        try
        {
            statement=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        }
        catch (SQLException e)
        {
            statement=null;
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("Statement interface falure"+ExceMsg);
        }
        log.info("create statement success");
        return true;
    }
    public static ResultSet query(String sql){
        ResultSet resultSet=null;
        if(conn==null||statement==null)
        {
            return resultSet;
        }
        try
        {
            resultSet=statement.executeQuery(sql);
        }
        catch (NullPointerException e)
        {
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("NullPointerException"+ExceMsg);
            return null;
        }
        catch (SQLException e)
        {
            String ExceMsg=GetExceptionMsg.getExcpMsg(e);
            log.error("executeQuery falure"+ExceMsg);
            return null;
        }
        return resultSet;
    }
    public static boolean closeStatement(){
        if (statement!=null)
        {
            try
            {
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
        log.info("close statement successful");
        return  true;
    }
    public static boolean closeConn(){

        if (conn!=null)
        {
            try
            {
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

}

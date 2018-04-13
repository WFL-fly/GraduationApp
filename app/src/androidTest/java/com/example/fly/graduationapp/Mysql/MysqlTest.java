package com.example.fly.graduationapp.Mysql;

import junit.framework.TestCase;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by wf on 2018/4/10.
 */
public class MysqlTest extends TestCase {
    @Test
    public void openConn() throws Exception {
        assertFalse(Mysql.openConn());
    }

    @Test
    public void createUpdateStarement() throws Exception {
    }

    @Test
    public void createOnlyReadStatement() throws Exception {
    }

    @Test
    public void query() throws Exception {
    }

    @Test
    public void closeStatement() throws Exception {
    }

    @Test
    public void closeConn() throws Exception {
    }

}
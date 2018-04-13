package com.example.fly.graduationapp.Mysql;

import com.example.fly.graduationapp.Centre;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.curTbRec;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by wf on 2018/4/11.
 */
public class MysqlManagerTest extends TestCase {

    private static Log log= LogFactory.getLog(Centre.class);
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUpdateAllCurrencyTb() throws Exception {
    }

    @Test
    public void testUpdateCurTb() throws Exception {
    }

    @Test
    public void testUpdateOneRateData() throws Exception {
    }

    @Test
    public void testGetOneCurTbDataFromMysqlDB() throws Exception {
    }

    @Test
    public void testGetAllCurTbDataFromMysqlDB() throws Exception {
        log.info("test mysql and get data !!!!!!!!!!!!");
        boolean res=MysqlManager.getAllCurTbDataFromMysqlDB();
        if(!res)
        {
            log.info("get data failure");
            return;
        }
        curTbRec rec= Data.curTbsRec.get("CNY").get("JPY");
        log.info(rec.excCurName+rec.newestUpTime+rec.chiTbName+rec.excRate);
    }

    @Test
    public void testUpdateAllRateData() throws Exception {
    }

    @Test
    public void testUpdateMysqlDBAllTbList() throws Exception {
    }

}
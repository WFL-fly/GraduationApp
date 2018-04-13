package com.example.fly.graduationapp.Data;

import java.util.Deque;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Data {
    public static ReentrantReadWriteLock allTbList_lock=new ReentrantReadWriteLock();
    public static List<String> allTbList;//保存mysql中所有表名称

    public static ReentrantReadWriteLock allCurTbMap_lock=new ReentrantReadWriteLock();
    public static Map<String,allCurTbRec> allCurTbMap=new Hashtable<>();//保存 mysql中all_currency_tb表数据

    public static ReentrantReadWriteLock currencyList_lock=new ReentrantReadWriteLock();
    public static List<String> currencyList=null;//保存可供查询的货币名称

    public static ReentrantReadWriteLock allCurrency_lock=new ReentrantReadWriteLock();
    public static Set<String> allCurrency;//记录可以查询汇率的所有货币名称

    public static ReentrantReadWriteLock exchangeRate_lock=new ReentrantReadWriteLock();
    public static Map<String,curTbRec> exchangeRate;//所有可以查询货币对的汇率
    public static ReentrantReadWriteLock curTbsRec_lock=new ReentrantReadWriteLock();
    public static Map<String,Map<String,curTbRec> > curTbsRec;
    //趋势线
    public static ReentrantReadWriteLock lineRate_lock=new ReentrantReadWriteLock();
    public static Map<String,Deque<Float> > lineRate;
}

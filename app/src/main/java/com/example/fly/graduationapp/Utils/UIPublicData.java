package com.example.fly.graduationapp.Utils;

import android.graphics.Color;
import android.widget.ArrayAdapter;

import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.myLine;
import com.example.fly.graduationapp.GetExceptionMsg;
import com.example.fly.graduationapp.MyApplication;
import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.R;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;

/**
 * Created by wf on 2018/4/23.
 */
public class UIPublicData {
    private  static Logger log=Logger.getLogger(UIPublicData.class);//= LogFactory.getLog(Mysql.class);
    public static boolean dataInit=false;
    public static List<String>  cur_zh_list;
    public static List<String> cur_en_list;
    public static ArrayAdapter<String> m_Adapter;
    public static List<Line> trend_lines = new ArrayList<>(1);
    public static LineChartData m_lineChartData = new LineChartData();
    public static Map<String,myLine> trendLineMap=new Hashtable<>();
    private static Axis m_axisX=new Axis();
    private static Axis m_axisY=new Axis();
    public static  boolean getCurListFromData() {
        List<String> tempList = null;
        try {
            if (Data.currencyList_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS)) {
                if (Data.currencyList != null)
                    tempList = new ArrayList<>(Data.currencyList);
                else
                    tempList = new ArrayList<>();
                Data.currencyList_lock.readLock().unlock();
            } else {
                log.error("get Data.allTbList_lock.writeLock() failure ");
                return false;
            }
        } catch (InterruptedException e) {
            String ExceMsg = GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.currencyList_lock.readLock() exception:" + ExceMsg);
        }
        if (tempList == null) {
            return false;
        }
        if (cur_en_list == null)
            cur_en_list = new ArrayList<>();
        cur_en_list.clear();
        cur_en_list.addAll(tempList);
        tempList.clear();
        for (int i = 0; i < cur_en_list.size(); i++) {
            String tmp = cur_en_list.get(i);
            tmp = PubMethod.fromENToZH_Map.get(tmp);
            if (tmp == null)
                continue;
            tempList.add(tmp);
        }
        if (cur_zh_list == null)
            cur_zh_list = new ArrayList<>();
        cur_zh_list.clear();
        cur_zh_list.addAll(tempList);

        log.info("getCurListFromData successful");
        return true;
    }
    public static boolean update_trendLineMap()
    {
        if(cur_en_list==null)
            return false;
        if(trendLineMap==null)
            trendLineMap=new Hashtable<>();
        for(String fromCur:cur_en_list)
        {
            for(String toCur:cur_en_list)
            {
                if(fromCur.equals(toCur))
                    continue;
                String TRName=fromCur+"_"+toCur+"_tb";
                if(trendLineMap.get(TRName)==null)
                {
                    trendLineMap.put(TRName,new myLine());
                }
            }
        }
        return true;
    }

    public static void  updateCurSelectSpiner()
    {
        getCurListFromData();
        update_trendLineMap();
        m_Adapter.notifyDataSetChanged();
    }
    public static void initSpinerData()
    {
        if(cur_zh_list==null)
            cur_zh_list=new ArrayList<>();
        if(cur_en_list==null)
            cur_en_list=new ArrayList<>();
        if(m_Adapter==null)
        {
            m_Adapter=new ArrayAdapter<>(MyApplication.getAppContext(), R.layout.custom_spiner_text_item,cur_zh_list);
            m_Adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        }
    }

    public static void setXLable()
    {
        m_axisX.setAutoGenerated(true);
        m_axisX.setName("时间");//y轴标注
        m_axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        m_axisX.setLineColor(Color.GRAY);// 设置X轴轴线颜色
        m_axisX.setTextColor(Color.GREEN);  //设置字体颜色
        m_axisX.setTextSize(5);//设置字体大小
        m_axisX.setHasLines(true); //x 轴分割线
        m_axisX.setInside(true);
        m_axisX.setMaxLabelChars(5);//轴标签最大字符数
        m_axisX.setHasSeparationLine(true);//间隔线
        m_lineChartData.setAxisXBottom(m_axisX); //x 轴在底部
    }
    public static void setXLableValues(List<AxisValue> axisXList)
    {
        m_axisX.setMaxLabelChars(axisXList.size()); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        m_axisX.setValues(axisXList);  //填充X轴的坐标名称
    }
    public static void setYLable()
    {
        m_axisY.setAutoGenerated(true);
        SimpleAxisValueFormatter simpleAxisValueFormatter=new SimpleAxisValueFormatter(6);
        m_axisY.setFormatter(simpleAxisValueFormatter);
        m_axisY.setHasTiltedLabels(false);
        m_axisY.setLineColor(Color.GRAY);
        m_axisY.setTextColor(Color.GREEN);
        m_axisY.setName("汇率");
        m_axisY.setTextSize(5);
        m_axisY.setHasLines(true);
        m_axisY.setInside(true);
        m_axisY.setMaxLabelChars(10);
        m_lineChartData.setAxisYLeft(m_axisY);
    }
    public static void setYLableValues(List<AxisValue> axisYList)
    {
        m_axisY.setMaxLabelChars(axisYList.size()); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        m_axisY.setValues(axisYList);  //填充X轴的坐标名称
    }
    public static void setLineChartData(LineChartData lineChartData)
    {
        lineChartData.setBaseValue(0);//设置数据的初始值
        //lineChartData.setBaseValue(Float.NEGATIVE_INFINITY);
        lineChartData.setValueLabelBackgroundEnabled(true);
        lineChartData.setValueLabelBackgroundAuto(false);// 设置数据背景是否跟随节点颜色
        lineChartData.setValueLabelBackgroundColor(Color.BLUE);// 设置数据背景颜色

        lineChartData.setValueLabelBackgroundEnabled(true);// 设置是否有数据背景
        lineChartData.setValueLabelsTextColor(Color.GREEN);// 设置数据文字颜色
        lineChartData.setValueLabelTextSize(8);// 设置数据文字大小
    }
}

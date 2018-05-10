package com.example.fly.graduationapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fly.graduationapp.CMD.SQLite_CMD;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.TR_UpRec_Rec;
import com.example.fly.graduationapp.Data.msgObj;
import com.example.fly.graduationapp.Data.myLine;
import com.example.fly.graduationapp.Data.requestTR;
import com.example.fly.graduationapp.Data.trendLinePoint;
import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.Utils.UIPublicData;
import com.example.fly.graduationapp.Utils.trendLineSelect;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import android.widget.AdapterView.OnItemSelectedListener;
public class TrendLineActivity extends AppCompatActivity {
    private static  Logger log=Logger.getLogger(TrendLineActivity.class);//= LogFactory.getLog(Mysql.class);
    private TextView m_tv_startDate;
    private TextView m_tv_stopDate;
    private Spinner m_tl_sn_fromSpinner;
    private Spinner m_tl_sn_toSpinner;
    private static LineChartView m_lineChartView;
    private trendLineSelect m_TLSelect;
    private int getDateFlag=0;
    private static Date m_startDate;
    private static Date m_stopDate;
    private static String currentTrendLineName;
    private OnItemSelectedListener sp_from=new OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            m_startDate=null;
            m_stopDate=null;
            String str1= m_tv_startDate.getText().toString();
            String str2= m_tv_stopDate.getText().toString();
            if(str1.isEmpty()&&str2.isEmpty())
            {
                return;
            }
            m_tv_startDate.setText("");
            m_tv_stopDate.setText("");
            showToastMsg("请重新选择起始时间");
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
    };
    private OnItemSelectedListener sp_to=new OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            m_startDate=null;
            m_stopDate=null;
            String str1= m_tv_startDate.getText().toString();
            String str2= m_tv_stopDate.getText().toString();
            if(str1.isEmpty()&&str2.isEmpty())
            {
                return;
            }
            m_tv_startDate.setText("");
            m_tv_stopDate.setText("");
            showToastMsg("请重新选择起始时间");
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_line);
        m_tv_startDate=findViewById(R.id.tv_tl_startdate);
        m_tv_stopDate=findViewById(R.id.tv_tl_stopdate);
        m_tl_sn_fromSpinner=findViewById(R.id.Spi_trendline_FromCurList);
        m_tl_sn_fromSpinner.setOnItemSelectedListener(sp_from);
        m_tl_sn_toSpinner=findViewById(R.id.Spi_trendline_ToCurList);
        m_tl_sn_toSpinner.setOnItemSelectedListener(sp_to);
        m_TLSelect=new trendLineSelect();
        UIPublicData.initSpinerData();
        m_tl_sn_fromSpinner.setAdapter(UIPublicData.m_Adapter);
        m_tl_sn_toSpinner.setAdapter(UIPublicData.m_Adapter);
        if(UIPublicData.dataInit)
            UIPublicData.updateCurSelectSpiner();
        m_lineChartView=findViewById(R.id.trendline);
        //setmAxisXLable();
        //setAxisPoints();
        initLineChart();
    }
    private void setLineChartView()
    {
        //设置行为属性，支持缩放、滑动以及
        m_lineChartView.setInteractive(true);//交互
        m_lineChartView.setZoomEnabled(true);//缩放
        m_lineChartView.setScrollEnabled(true);//滑动
        m_lineChartView.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);//平滑
        m_lineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);//缩放
        m_lineChartView.setMaxZoom((float)20);//最大缩放比例
        m_lineChartView.setValueTouchEnabled(true);
        m_lineChartView.setValueSelectionEnabled(true);
        m_lineChartView.setVisibility(View.INVISIBLE);
    }
    private static void setLineChartViewValues(LineChartData lineChartData,myLine myline)
    {
        m_lineChartView.setLineChartData(lineChartData);
        int right=myline.m_pointValues.size();
        Viewport v = new Viewport(m_lineChartView.getMaximumViewport());
        //float differenceValue=myline.maxValue-myline.minValue;
        //float coefficient=0.20f;
        v.left =-1;
        if(right>15)
           v.right= 15;
        else
            v.right=right;
        v.top=myline.maxValue;
        v.bottom=myline.minValue;
        Viewport maxV = new Viewport(m_lineChartView.getMaximumViewport());
        maxV.left = -1;
        if(right>100)
            right= 99;
        maxV.right=right+1;
        maxV.top=myline.maxValue;
        maxV.bottom=myline.minValue;
        m_lineChartView.setCurrentViewport(v);
        m_lineChartView.setMaximumViewport(maxV);
        m_lineChartView.setVisibility(View.VISIBLE);
        m_lineChartView.startDataAnimation();
    }

    private void initLineChart()
    {
        UIPublicData.setYLable();
        UIPublicData.setXLable();
        UIPublicData.m_lineChartData.setLines(UIPublicData.trend_lines);
        UIPublicData.setLineChartData(UIPublicData.m_lineChartData);
        setLineChartView();
    }
    private DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            //Date selectDate=new Date(year-1990,monthOfYear,dayOfMonth);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            switch(getDateFlag)
            {
                case 1:
                    m_startDate=new Date(year-1900,monthOfYear,dayOfMonth);
                    m_tv_startDate.setText(formatter.format(m_startDate));
                    break;
                case 2:
                    m_stopDate=new Date(year-1900,monthOfYear,dayOfMonth);
                    m_tv_stopDate.setText(formatter.format(m_stopDate));
                    break;
                default:
                    break;
            }
            getDateFlag=0;
        }
    };
    private void getDate(Calendar minDate, Calendar maxDate)
    {
        //得到当前时间
        DatePickerDialog datePickerDialog=new DatePickerDialog(TrendLineActivity.this,dateSetListener,
                minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH), minDate.get(Calendar.DAY_OF_MONTH));
        //设置起始日期和结束日期
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(minDate.getTimeInMillis());//最小时间要比最大时间和当前时间小
        datePicker.setMaxDate(maxDate.getTimeInMillis());//设置最大日期
        datePickerDialog.show();
    }
    public void startDate_onClick(View view)
    {
        String fromCur=m_tl_sn_fromSpinner.getSelectedItem().toString();
        fromCur= PubMethod.fromZHToEN_Map.get(fromCur);
        String toCur=m_tl_sn_toSpinner.getSelectedItem().toString();
        toCur= PubMethod.fromZHToEN_Map.get(toCur);
        if(fromCur==null||toCur==null||fromCur.isEmpty()||toCur.isEmpty())
        {
            showToastMsg("换算货币类型不能为空，请重新换算货币种类");
            return;
        }
        if(fromCur.equals(toCur))
        {
            showToastMsg("选择的原始货币与目标货币不能相同，请重新选择");
            return;
        }
        String TRName=fromCur+"_"+toCur+"_tb";
        TR_UpRec_Rec rec=Data.get_TR_UpRec_Map_OneRec_Copy(TRName);
        if(rec==null)
        {
            log.error("cannot get start date and end date record from Data.get_TR_UpRec_Map_OneRec_Copy");
            showToastMsg("遇到错误");
            return;
        }
        String startDateStr=rec.oldestDate;
        String endDateStr=rec.newestDate;

        Calendar minCalendar = new GregorianCalendar();
        Calendar maxCalendar = new GregorianCalendar();
        Date maxDate;
        Date minDate;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            minDate= df.parse(startDateStr);    //start_date是类似"2013-02-02"的字符串
        }
        catch (ParseException e)
        {
            return;
        }

        try
        {
            maxDate=df.parse(endDateStr);    //start_date是类似"2013-02-02"的字符串
        }
        catch (ParseException e)
        {
            return;
        }
        minCalendar.setTime(minDate);
        maxCalendar.setTime(maxDate);
        getDateFlag=1;
        getDate( minCalendar, maxCalendar);
    }
    public void stopDate_onClick(View view)
    {
        String fromCur=m_tl_sn_fromSpinner.getSelectedItem().toString();
        fromCur= PubMethod.fromZHToEN_Map.get(fromCur);
        String toCur=m_tl_sn_toSpinner.getSelectedItem().toString();
        toCur= PubMethod.fromZHToEN_Map.get(toCur);
        if(fromCur==null||toCur==null||fromCur.isEmpty()||toCur.isEmpty())
        {
            showToastMsg("换算货币类型不能为空，请重新换算货币种类");
            return;
        }
        if(fromCur.equals(toCur))
        {
            showToastMsg("选择的原始货币与目标货币不能相同，请重新选择");
            return;
        }
        String TRName=fromCur+"_"+toCur+"_tb";
        TR_UpRec_Rec rec=Data.get_TR_UpRec_Map_OneRec_Copy(TRName);
        if(rec==null)
        {
            log.error("cannot get start date and end date record from Data.get_TR_UpRec_Map_OneRec_Copy");
            showToastMsg("遇到错误");
            return;
        }
        String startDateStr=rec.oldestDate;
        String endDateStr=rec.newestDate;
        Calendar minCalendar = new GregorianCalendar();
        Calendar maxCalendar = new GregorianCalendar();
        Date maxDate;
        Date minDate;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            minDate= df.parse(startDateStr);    //start_date是类似"2013-02-02"的字符串
        }
        catch (ParseException e)
        {
            return;
        }
        try
        {
            maxDate=df.parse(endDateStr);    //start_date是类似"2013-02-02"的字符串
        }
        catch (ParseException e)
        {
            return;
        }
        minCalendar.setTime(minDate);
        maxCalendar.setTime(maxDate);
        getDateFlag=2;
        getDate( minCalendar, maxCalendar);
    }
    public static int getStartDateIndex(LinkedList<trendLinePoint> lineData,Date startDate)
    {
        if(lineData==null||startDate==null||lineData.size()<1)
            return -1;
        int size=lineData.size();
        if(startDate.after(lineData.get(size-1).m_date))
            return -1;
        if(startDate.equals(lineData.get(size-1).m_date))
            return size-1;
        if(!startDate.after(lineData.get(0).m_date))
            return 0;
        if(size==1)
            if(!startDate.after(lineData.get(0).m_date))
                return 0;
            else
                return -1;
        if(size==2)
          return 0;
        int middle,max=size,min=0;
        do {
            middle=(max+min)/2;
            if (startDate.equals(lineData.get(middle).m_date))
            {
                return middle;
            }
            else
            {
                if(startDate.before(lineData.get(middle).m_date))
                    max=middle;
                else
                    min=middle;
                if((min+1)==max||min==max)
                    return min;
            }
        }while(true);
    }
    public static int getEndDateIndex(LinkedList<trendLinePoint> lineData,Date endDate)
    {
        if(lineData==null||endDate==null||lineData.size()<1)
            return -1;
        int size=lineData.size();
        if(endDate.before(lineData.get(0).m_date))
            return -1;
        if(endDate.equals(lineData.get(0).m_date))
            return 0;
        if(!endDate.before(lineData.get(size-1).m_date))
            return size-1;
        if(size==1)
            if(!endDate.before(lineData.get(size-1).m_date))
                return 0;
            else
                return -1;
        if(size==2)
            return 1;
        int middle,max=size,min=0;
        do {
            middle=(max+min)/2;
            if (endDate.equals(lineData.get(middle).m_date))
            {
                return middle;
            }
            else
            {
                if(endDate.before(lineData.get(middle).m_date))
                    max=middle;
                else
                    min=middle;
                if((min+1)==max||min==max)
                    return max;
            }
        }while(true);
    }

    public void updateShow_onClick(View view)
    {

        String fromCur=m_tl_sn_fromSpinner.getSelectedItem().toString();
        fromCur= PubMethod.fromZHToEN_Map.get(fromCur);
        String toCur=m_tl_sn_toSpinner.getSelectedItem().toString();
        toCur= PubMethod.fromZHToEN_Map.get(toCur);
        if(fromCur==null||toCur==null||fromCur.isEmpty()||toCur.isEmpty())
        {
            showToastMsg("请重新换算货币种类");
            return;
        }
        if(fromCur.equals(toCur))
        {
            showToastMsg("选择的原始货币与目标货币不能相同，请重新选择");
            return;
        }
        String strStartDate=m_tv_startDate.getText().toString();
        String strStopDate=m_tv_stopDate.getText().toString();
        if(m_startDate==null||m_stopDate==null||strStartDate==null||strStopDate==null||strStartDate.isEmpty()||strStopDate.isEmpty())
        {
            showToastMsg("请重新选择起始时间");
            return;
        }
        if((fromCur.equals(m_TLSelect.fromCur)&&toCur.equals(m_TLSelect.toCur) &&
                strStartDate.equals(m_TLSelect.strStartDate)&&strStopDate.equals(m_TLSelect.strStopDate)))
        {
            return;
        }
        if(!Centre.MysqlDownloadAndSqliteSave_Res)
        {
            log.info("Mysql download trend rate data and Sqlite save trend rate data is not over ");
            showToastMsg("正在初始化，请稍后重试");
            return;
        }
        //boolean res=updateShow(fromCur,toCur,m_startDate,m_stopDate);
        boolean res=requestTrendData(fromCur,toCur, m_startDate,m_stopDate);
        if(!res)
            return;
        m_TLSelect.fromCur=fromCur;
        m_TLSelect.toCur=toCur;
        m_TLSelect.strStartDate=strStartDate;
        m_TLSelect.strStopDate=strStopDate;
        log.info("show onclick success");
    }
    public  void showToastMsg(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    public static boolean OuterUpdateShow()
    {
        LinkedList<trendLinePoint> lineData=Data.get_lineRateMap_Ele(currentTrendLineName);
        if(lineData==null)
            return false;
        int startDateIndex=getStartDateIndex(lineData,m_startDate);
        int endDateIndex=getEndDateIndex(lineData,m_stopDate);
        if(startDateIndex<0||endDateIndex<0)
        {
            log.error("error ");
            return false;
        }
        return updateShow(lineData, startDateIndex, endDateIndex);
    }

    private List<requestTR> getAddTRDataList(String TRName,Date startDate,Date endDate,LinkedList<trendLinePoint> lineData)
    {
        if(TRName==null||startDate==null||endDate==null||lineData==null)
            return null;
        Date addData_startDate;
        Date addData_endDate;
        List<requestTR> requestTRList=new LinkedList<>();
        if(lineData.size()>1)
        {
            //请求数据
            Date oldestDate=lineData.get(0).m_date;
            Date newestDate=lineData.get(lineData.size()-1).m_date;
            if( (!startDate.before(oldestDate)) && (!endDate.after(newestDate)) )
            {
                //4
                //数据已存在
                //得到起始坐标
                int startDateIndex=getStartDateIndex(lineData,startDate);
                int endDateIndex=getEndDateIndex(lineData,endDate);
                if(startDateIndex<0||endDateIndex<0)
                {
                    log.error("error ");
                    return requestTRList;
                }
                updateShow(lineData, startDateIndex, endDateIndex);
                return requestTRList;
            }
            else if( (!startDate.after(oldestDate)) && (endDate.before(newestDate)) )//startDate>newestDate
            {
                //1
                addData_startDate=newestDate;
                addData_endDate=endDate;
                requestTRList.add(new requestTR(currentTrendLineName,addData_startDate,addData_endDate));
            }
            else if(startDate.before(startDate)&&(!endDate.after(oldestDate)))
            {
                //2
                addData_startDate=startDate;
                addData_endDate=oldestDate;
                requestTRList.add(new requestTR(currentTrendLineName,addData_startDate,addData_endDate));
            }
            else if( startDate.before(oldestDate) && endDate.after(newestDate) )
            {
                //3
                addData_startDate=startDate;
                addData_endDate=oldestDate;
                requestTRList.add(new requestTR(currentTrendLineName,addData_startDate,addData_endDate));
                addData_startDate=newestDate;
                addData_endDate=endDate;
                requestTRList.add(new requestTR(currentTrendLineName,addData_startDate,addData_endDate));
            }
        }
        else
        {
            lineData.clear();
            addData_startDate=startDate;
            addData_endDate=endDate;
            requestTRList.add(new requestTR(currentTrendLineName,addData_startDate,addData_endDate));
        }
        return requestTRList;
    }

    private boolean requestTrendData( String fromCur,String toCur,Date startDate,Date stopDate)
    {
        if(fromCur==null||toCur==null||startDate==null||stopDate==null)
            return false;
        if(stopDate.before(startDate)||stopDate.equals(startDate))
        {
            //保证 结束时间大于开始时间
            m_tv_startDate.setText("");
            m_tv_startDate.setText("");
            showToastMsg("请保证结束日期大于开始日期");
            return false;
        }
        currentTrendLineName=fromCur+"_"+toCur+"_tb";
        LinkedList<trendLinePoint> lineData;
        lineData=Data.get_lineRateMap_Ele(currentTrendLineName);
        List<requestTR> requestTRList;
        if(lineData==null)
        {
            requestTRList=new LinkedList<>();
            requestTRList.add(new requestTR(currentTrendLineName,startDate,stopDate));
        }
        else
        {
            requestTRList=getAddTRDataList(currentTrendLineName,startDate,stopDate,lineData);
        }
        if(requestTRList==null)
        {
            //发送消息给SQLite
            log.error("getAddTRDataList error");
            return false;
        }
        if(requestTRList.size()>0)
        {
            Handler SQLiteHandler= Centre.getThreadHandler("SQLite");
            Handler UIHandler=Centre.getThreadHandler("UI");
            if(SQLiteHandler==null||UIHandler==null)
                return false;
            PubMethod.sendMsgToThread(SQLiteHandler,
                    SQLite_CMD.SQLite_GET_TREND_RATE_DATA.ordinal(),
                    new msgObj(true,UIHandler,requestTRList));
        }
        return true;
    }
    //内部调用
    private static boolean updateShow(LinkedList<trendLinePoint> lineData,int start,int end)
    {
        if(lineData==null||(start>=end))
        {
            log.error("cannot get trend rate data");
            return false;
        }
        if(currentTrendLineName==null)
            return false;
        myLine myline=UIPublicData.trendLineMap.get(currentTrendLineName);
        if(myline==null)
        {
            log.error(String.format("cannot find %s rate from UIPublicData.trendLineMap",currentTrendLineName));
            return false;
        }
        myline.setAxisXLableAndPoints(lineData,start,end);
        myline.setAxisY();
        UIPublicData.setXLableValues(myline.m_axisXValueList);
        UIPublicData.setYLableValues(myline.m_axisYValueList);
        if(UIPublicData.trend_lines.size()>0)
            UIPublicData.trend_lines.set(0,myline.m_line);
        else
            UIPublicData.trend_lines.add(myline.m_line);
        setLineChartViewValues(UIPublicData.m_lineChartData,myline);
        log.info("updateShow run finish");
        return true;
    }

}

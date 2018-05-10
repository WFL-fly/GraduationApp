package com.example.fly.graduationapp.Data;

import android.graphics.Color;

import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.Utils.UIPublicData;

import java.util.LinkedList;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;

public class myLine
{
    public Line m_line;
    public List<AxisValue> m_axisXValueList;
    public List<AxisValue> m_axisYValueList;
    public List<PointValue> m_pointValues;
    public float minValue=0;
    public float maxValue=0;
    //设置x轴
    public myLine()
    {
        this.m_line=new Line();
        this.m_axisXValueList=new LinkedList<>();
        this.m_axisYValueList=new LinkedList<>();
        this.m_pointValues=new LinkedList<>();
        setLine();
       // setAxisY();
    }
    public myLine(Line line,List<AxisValue> axisXValueList,List<AxisValue> axisYValueList,List<PointValue> pointValues)
    {
        this.m_line=line;
        this.m_axisXValueList=axisXValueList;
        this.m_axisYValueList=axisYValueList;
        this.m_pointValues=pointValues;
        setLine();
        //setAxisY();
    }
    public  void setLine()
    {
        LineChartValueFormatter chartValueFormatter=new SimpleLineChartValueFormatter(3);
        m_line.setFormatter(chartValueFormatter);//线数据格式
        m_line.setPointRadius(2);//点的大小
        m_line.setPointColor(Color.YELLOW);
        m_line.setAreaTransparency(0);//线的透明度
        m_line.setColor(Color.RED);  //折线的颜色（red） Color.parseColor("#FF4081"
        m_line.setShape(ValueShape.DIAMOND);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        m_line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        m_line.setFilled(false);//是否填充曲线的面积
        // line.setHasLabels(true);//曲线的数据坐标是否加上备注
        m_line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        m_line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        m_line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        m_line.setStrokeWidth(1);//线宽
        m_line.setHasLabelsOnlyForSelected(true);
        m_line.setValues(m_pointValues);
    }
    public void setAxisXLableAndPoints(LinkedList<trendLinePoint> dateList, int start, int stop)
    {
        this.minValue=10000000;
        this.maxValue=0;
        m_axisXValueList.clear();
        m_pointValues.clear();
        for(int i=0;i<stop-start;i++)
        {
            AxisValue axisValue= new AxisValue(i);
            trendLinePoint tempPoint= dateList.get(i+start);
            if(tempPoint.m_rate<this.minValue)
                this.minValue=tempPoint.m_rate;
            if(tempPoint.m_rate>this.maxValue)
                this.maxValue=tempPoint.m_rate;
            axisValue.setLabel(PubMethod.getDateStr(tempPoint.m_date));
            m_axisXValueList.add(axisValue);
            m_pointValues.add(new PointValue(i,tempPoint.m_rate));
        }
    }
    public void setAxisY()
    {
        float temp1=maxValue-minValue;
        float temp=temp1/100;
        m_axisYValueList.clear();
        m_axisYValueList.add(new AxisValue( (int)(minValue-temp1*10f) ) );
        float yValue;
        for(int i=0;i<101;i++)
        {
            yValue=i*temp+minValue;
            m_axisYValueList.add(new AxisValue(yValue));
        }
        m_axisYValueList.add(new AxisValue( (int)(minValue+temp1*10f) ) );
        minValue=minValue-temp1*10f;
        maxValue=minValue+temp1*10f;
    }
}

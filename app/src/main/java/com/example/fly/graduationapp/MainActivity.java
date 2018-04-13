package com.example.fly.graduationapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fly.graduationapp.CMD.UIMsg_CMD;
import com.example.fly.graduationapp.Data.Data;
import com.example.fly.graduationapp.Data.Exch_Rate;
import com.example.fly.graduationapp.PublicMethod.PubMethod;
import com.example.fly.graduationapp.Utils.Manager;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
public class MainActivity extends AppCompatActivity {

    private  static Logger log=Logger.getLogger(MainActivity.class);//= LogFactory.getLog(Mysql.class);
    private Button   m_bn_search_rate;
    private Button  m_bn_calculate_cost;
    private TextView m_tv_rate;
    private Exch_Rate exchange_rate=new Exch_Rate();
    private EditText m_et_exch_num;
    private double  exchange_num=0.0;
    private TextView m_tv_spend_cost;
    private double  exchange_cost=0.0;
    private Spinner m_sn_fromspinner;
    private Spinner m_sn_tospinner;
    private List<String> cur_en_list;
    private List<String> cur_zh_list;
    private ArrayAdapter<String> m_Adapter;

    private Button m_menu_bn_exch_cur;
    private Button m_menu_bn_trendline;

    private TextView m_test_tv;
    private static boolean dataInit=false;
    private Handler.Callback mainCallback=new Handler.Callback()
    {@Override
        public boolean handleMessage(Message msg)
        {
            UIMsg_CMD UiMsgCmd=UIMsg_CMD.values()[msg.what];
            String msgStr;
            switch (UiMsgCmd)
            {
                case DATA_INIT_FINISH:
                {
                    dataInit=(boolean)msg.obj;
                    if(dataInit)
                    {
                        updateCurSelectSpiner();
                    }
                    break;
                }
                case CTR_CMD_1:
                    msgStr=m_test_tv.getText().toString();
                    msgStr  +="\n\r new msg:"+msg.obj;
                    m_test_tv.setText(msgStr);
                break;
                case CTR_CMD_2:
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public static  Handler mHandler=null;
    public static Centre myCentre=null;
    public static Context mContext=null;
    public MainActivity() {
        super();
        if(mHandler==null)
            mHandler = new Handler(mainCallback);
        if(myCentre==null)
        {
            myCentre= new Centre();
            myCentre.start();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=getApplicationContext();
        m_bn_search_rate=findViewById(R.id.search_rate);
        m_bn_calculate_cost=findViewById(R.id.calculate_cost);
        m_tv_rate=findViewById(R.id.rate);

        m_et_exch_num=findViewById(R.id.exch_num);
        m_sn_fromspinner=findViewById(R.id.Spi_FromCurList);
        m_sn_tospinner=findViewById(R.id.Spi_ToCurList);
        m_tv_spend_cost=findViewById(R.id.spend_cost);
        m_menu_bn_exch_cur=findViewById(R.id.exch_cur);
        m_menu_bn_trendline=findViewById(R.id.trendline);

        m_test_tv=findViewById(R.id.test_tv);
        cur_zh_list=new ArrayList<>();
        cur_en_list=new ArrayList<>();
        m_Adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,cur_zh_list);
        m_Adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        m_sn_fromspinner.setAdapter(m_Adapter);
        m_sn_tospinner.setAdapter(m_Adapter);
        if(dataInit)
            updateCurSelectSpiner();
    }
    private  void  updateCurSelectSpiner()
    {
        getCurListFromData();
        m_Adapter.notifyDataSetChanged();
        //m_sn_fromspinner.setSelection(0, true);
        //m_sn_tospinner.setSelection(0, true);
    }
    private boolean getCurListFromData()
    {
        List<String> tempList=null;
        try
        {
            if(Data.currencyList_lock.readLock().tryLock(10, TimeUnit.MILLISECONDS))
            {
                if(Data.currencyList!=null)
                    tempList=new ArrayList<>(Data.currencyList);
                else
                    tempList=new ArrayList<>();
                Data.currencyList_lock.readLock().unlock();
            }
            else
            {
                log.error("get Data.allTbList_lock.writeLock() failure ");
                return false;
            }
        }
        catch (InterruptedException e)
        {
            String ExceMsg= GetExceptionMsg.getExcpMsg(e);
            log.error("get Data.currencyList_lock.readLock() exception:"+ExceMsg);
        }
        if(tempList==null)
        {
            return false;
        }
        if(cur_en_list==null)
            cur_en_list=new ArrayList<>();
        cur_en_list.clear();
        cur_en_list.addAll(tempList);
        tempList.clear();
        for(int i=0;i<cur_en_list.size();i++)
        {
            String tmp=cur_en_list.get(i);
            tmp=PubMethod.fromENToZH_Map.get(tmp);
            if (tmp==null)
                continue;
            tempList.add(tmp);
        }
        if(cur_zh_list==null)
            cur_zh_list=new ArrayList<>();
        cur_zh_list.clear();
        cur_zh_list.addAll(tempList);

        log.info("getCurListFromData successful");
        return true;
    }
    public boolean  updateRateShow()
    {
        double rate=-1.0;
        if(!dataInit)
        {
            log.info("data init uncompleted");
            return false;
        }
        String fromCur=m_sn_fromspinner.getSelectedItem().toString();
        String toCur=m_sn_tospinner.getSelectedItem().toString();
        String rate_text="1"+fromCur+"="+"%.4f"+toCur;
        if(fromCur!=toCur)
        {
            rate=Manager.getFromToExchRate(fromCur,toCur);
        }
        else
        {
            rate=1;
        }
        if(rate<=0)
            return false;
        rate_text=String.format(Locale.CANADA,rate_text,rate);
        m_tv_rate.setText(rate_text);
        exchange_rate.mFromCur=fromCur;
        exchange_rate.mToCur=toCur;
        exchange_rate.mRate=rate;
        return true;
    }

    public void search_rate_bn_onClick(View view)
    {
        updateRateShow();
    }
    public  void update_calculate_cost()
    {
        String exch_num_str=m_et_exch_num.getText().toString();
        if(exch_num_str==null||exch_num_str.isEmpty())
        {
            Toast.makeText(this,"兑换数目不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        double num=Double.valueOf(exch_num_str);
        if(num<0)
        {
            Toast.makeText(this,"兑换数目不能负值",Toast.LENGTH_SHORT).show();
            return;
        }
        String fromCur=m_sn_fromspinner.getSelectedItem().toString();
        String toCur=m_sn_tospinner.getSelectedItem().toString();
        if(!(fromCur.equals(exchange_rate.mFromCur)&&toCur.equals(exchange_rate.mToCur)))
        {
            updateRateShow();
        }
        double cost=num*exchange_rate.mRate;
        String text="%.4f "+"("+exchange_rate.mFromCur+")";
        text=String.format(Locale.CANADA,text,cost);
        m_tv_spend_cost.setText(text);
    }

    public void calculate_cost_bn_onClick(View view)
    {
        update_calculate_cost();
    }
    public void menu_exch_cur_onClick(View view)
    {

    }
    public void menu_trendline_onClick(View view)
    {

    }

}

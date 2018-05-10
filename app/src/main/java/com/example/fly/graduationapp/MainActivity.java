package com.example.fly.graduationapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fly.graduationapp.CMD.CenterMsg_CMD;
import com.example.fly.graduationapp.CMD.SQLite_CMD;
import com.example.fly.graduationapp.CMD.UIMsg_CMD;
import com.example.fly.graduationapp.Data.Exch_Rate;
import com.example.fly.graduationapp.Data.msgObj;
import com.example.fly.graduationapp.Utils.Manager;
import com.example.fly.graduationapp.Utils.UIPublicData;

import org.apache.log4j.Logger;

import java.util.Locale;

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
    private Button m_menu_bn_exch_cur;
    private Button m_menu_bn_trendline;

    private TextView m_test_tv;
    //private static boolean dataInit=false;
    private boolean CentreThreadMsgLoopInit=false;
    private Intent intent;
    private Handler m_CenterThreadHandler=null;
    private static  Handler mHandler=null;
    public static  Handler getUIThreadHandler()
    {
        return mHandler;
    }

    public static Centre myCentre=null;
    public static Context mContext=null;
    private void sendMsgToCenterThread(CenterMsg_CMD msgWhat,Object obj)
    {
        Message msg=Message.obtain();
        CenterMsg_CMD MsgCmd=msgWhat;
        msg.what = MsgCmd.ordinal();
        msg.obj=obj;
        m_CenterThreadHandler.sendMessageDelayed(msg,1);
    }
    private Handler.Callback mainCallback=new Handler.Callback()
    {@Override
        public boolean handleMessage(Message msg)
        {

            UIMsg_CMD UiMsgCmd=UIMsg_CMD.get_UIMsg_CMD(msg.what);
            if(UiMsgCmd==null)
                return false;
            String msgStr;
            switch (UiMsgCmd)
            {
                case DATA_INIT_FINISH:
                {
                    msgObj msgobj=(msgObj)msg.obj;
                    UIPublicData.dataInit=(boolean)msgobj.result;
                    if(UIPublicData.dataInit)
                    {
                        UIPublicData.updateCurSelectSpiner();
                    }
                    else
                    {
                        //数据初始化失败
                    }
                    break;
                }
                case CentreThreadMSGLoop_INIT_FINISH:
                {
                    msgObj msgobj=(msgObj)msg.obj;
                    CentreThreadMsgLoopInit=(boolean)msgobj.result;
                    m_CenterThreadHandler=Centre.getCenterThreadHandler();
                    sendMsgToCenterThread(CenterMsg_CMD.UI_ACK_INIT_DATA,new msgObj(true,mHandler,null));
                    break;
                }
                case INTERNET_DISCONNECT:
                {
                    showToastMsg("未检查到网络连接，请打开网络连接");
                    break;
                }
                case MYSQL_AND_SQLite_CANNOT_GET_DATA:
                {
                   //严重问
                    log.error("can not get data from mysql and SQLite,");
                    showToastMsg("请检查网络连接");
                    break;
                }
                case SQLite_GET_TREND_RATE_DATA_FINISH:
                {
                    if(msg.obj==null)
                        return false;
                    msgObj msgobj=(msgObj)msg.obj;
                    if(msgobj.result==null)
                        return false;
                    boolean res=(boolean) msgobj.result;
                    if(res)
                    {
                        res=TrendLineActivity.OuterUpdateShow();
                        if(!res)
                            log.error("TrendLineActivity.OuterUpdateShow failure");
                    }
                    else
                    {
                        log.error("SQLite_GET_TREND_RATE_DATA_FINISH failure");
                    }
                }
                break;
                default:
                    break;
            }
            return false;
        }
    };
    public  void showToastMsg(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }


    public MainActivity() {
        super();
        if(mHandler==null)
            mHandler = new Handler(mainCallback);
        Centre.addHandler("UI",mHandler);
        if(myCentre==null)
        {
            myCentre= Centre.getInstance();
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
        UIPublicData.initSpinerData();
        m_sn_fromspinner.setAdapter(UIPublicData.m_Adapter);
        m_sn_tospinner.setAdapter(UIPublicData.m_Adapter);
        log.info("UI init success");
    }


    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quit_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.quit_item:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
    public boolean  updateRateShow()
    {
        double rate=-1.0;
        if(!UIPublicData.dataInit)
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
        if(fromCur.equals("人民币"))
        {
            rate=1/rate;
        }
        rate_text=String.format(Locale.CANADA,rate_text,rate);
        m_tv_rate.setText(rate_text);
        exchange_rate.mFromCur=fromCur;
        exchange_rate.mToCur=toCur;
        exchange_rate.mRate=rate;
        exchange_rate.mgetedRate=true;
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
        if( (!exchange_rate.mgetedRate)
                ||
                (!(fromCur.equals(exchange_rate.mFromCur)&&toCur.equals(exchange_rate.mToCur)))
                )
        {
            updateRateShow();
        }
        double cost=num/exchange_rate.mRate;
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
        if(intent==null)
            intent=new Intent(MainActivity.this,TrendLineActivity.class);
        startActivity(intent);
    }

}

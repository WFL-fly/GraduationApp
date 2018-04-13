package com.example.fly.graduationapp.PublicMethod;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.fly.graduationapp.MyApplication;
import com.example.fly.graduationapp.Data.Data;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by wf on 2018/4/8.
 */

public class PubMethod {
    public  static final  String zh_Arr[] =new String[]{"人民币","阿联酋迪拉姆","澳大利亚元","巴西里亚尔","加拿大元","瑞士法郎",
            "丹麦克朗","欧元", "英镑","港币","印尼卢比","印度卢比","日元","韩国元","澳门元","林吉特","挪威克朗","新西兰元",
            "菲律宾比索","卢布","沙特里亚尔","瑞典克朗","新加坡元","泰国铢","土耳其里拉","新台币","美元","南非兰特"};
    public  static final String en_Arr[] =new String[]{"CNY","AED","AUD","BRL","CAD","CHF","DKK","EUR","GBP","HKD",
            "IDR","INR","JPY","KRW","MOP","MYR","NOK","NZD","PHP","RUB","SAR","SEK","SGD","THB",
            "TRY","TWD","USD","ZAR"};
    public static final Map<String,String> fromZHToEN_Map=new Hashtable<>();
    public static final Map<String,String> fromENToZH_Map=new Hashtable<>();
    public static boolean tbIsExistInAllTbList(String tbname)
    {
        for (String i : Data.allTbList)
        {
            if(tbname==i)
                return true;
        }
        return false;
    }
    //判断网络连接是否正常
    public static boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager=(ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null)
            return false;
        NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
        if(info!=null&&info.length>0)
        {
            for(int i=0;i<info.length;i++)
            {
                if(info[i].getState()==NetworkInfo.State.CONNECTED)
                    return true;
            }
        }
        return false;
    }
}

package com.example.fly.graduationapp.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mysql.jdbc.log.LogFactory;

import org.apache.log4j.Logger;

public class MyDBHelper extends SQLiteOpenHelper {

    private  static Logger log=Logger.getLogger(MyDBHelper.class);
    private static Context m_Context;
    private static String db_name="exchange_rate.db";
    public MyDBHelper(Context context){
           super(context,db_name,null,1);
           m_Context=context;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //sqLiteDatabase.execSQL(sql_1);//第一次创建数据库时创建总表，其他表后期灵活建立
        //创建表
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_version, int new_version) {
        if(new_version>old_version){
            //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS all_currency_tb");
            //onCreate(sqLiteDatabase);
        }
        //版本号变更会被调用
    }
}

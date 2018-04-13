package com.example.fly.graduationapp.Utils;

import java.util.*;
public class Currency {
    public String currency_name;
    public String currency_tb_name;
    public String currency_update_datetime;
    public Currency(){
    }
    public Currency(String cur_name){
        this.currency_name=cur_name;
    }
    public Currency(String cur_name,String cur_tb_name,String cur_update_datetime){
        this.currency_name=cur_name;
        this.currency_tb_name=cur_tb_name;
        this.currency_update_datetime=cur_update_datetime;
    }
}

package com.chetan.mytwitterapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by hussienalrubaye on 11/13/16.
 */

public class SaveSettings {
    public  static String UserID="";

    Context context;
    SharedPreferences ShredRef;
    public  SaveSettings(Context context){
        this.context=context;
        ShredRef=context.getSharedPreferences("myRef",Context.MODE_PRIVATE);
    }

    void SaveData(String UserID){
        SharedPreferences.Editor editor=ShredRef.edit();
        editor.putString("UserIDD",UserID);
         editor.commit();
        //Toast.makeText(context,UserID+"Save"+ShredRef.getString("UserIDD","0"),Toast.LENGTH_LONG).show();
         LoadData();
    }

    void LoadData(){
        UserID= ShredRef.getString("UserIDD","0");
        //Toast.makeText(context,UserID+"Load",Toast.LENGTH_LONG).show();
        if (UserID.equals("0")){
            Intent intent=new Intent(context, login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}

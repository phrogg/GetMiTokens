package eu.roggstar.getmitokens;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

public class LoadingActivity extends AppCompatActivity {

    final static String path = "/sdcard/Android/data/eu.roggstar.getmitokens";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        execu("mkdir "+path);
        execu("cp /data/data/com.yeelight.cherry/shared_prefs/miot.xml "+path+"/");

    }

    void execu (String com){
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes( com+"\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            //su.waitFor();
        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(LoadingActivity.this,MainActivity.class));
    }
}

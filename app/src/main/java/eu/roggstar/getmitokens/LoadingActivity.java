package eu.roggstar.getmitokens;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LoadingActivity extends AppCompatActivity {

    final static String path = "/sdcard/Android/data/eu.roggstar.getmitokens";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        execu("mkdir "+path);
        execu("cp /data/data/com.yeelight.cherry/shared_prefs/miot.xml "+path+"/"); //delete after read
        startActivity(new Intent(LoadingActivity.this, MainActivity.class));
    }

    void execu (String com){
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(su.getInputStream()));

            outputStream.writeBytes( com+"\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            su.waitFor();

        } catch (IOException | InterruptedException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
    }
}

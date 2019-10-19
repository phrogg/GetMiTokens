package eu.roggstar.getmitokens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;
import static java.lang.System.exit;

public class MainActivity extends AppCompatActivity {

    private SimpleAdapter mAdapter;
    private final ArrayList<HashMap<String,String>> tokens = new ArrayList<>();
    private ListView lvTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setTitle
        setTitle("Get Mi Tokens");

        mAdapter = new SimpleAdapter(
                this,
                tokens,
                android.R.layout.simple_list_item_2,
                new String[] {"device","token"},
                new int[] {android.R.id.text1,android.R.id.text2}

        );

        lvTokens = findViewById(R.id.lvTokens);


        //add copy option
        lvTokens.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                copyToken2Clip(position);
            }
        });
        
        lvTokens.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                shareToken(position);
                return true;
            }

        });

        prepReading();
    }

    private void prepReading(){
        try {
            xml(getStringFromFile());
        } catch (Exception e) {
            Log.d("Philz",e.toString());
        }
    }

    private void shareToken(final int pos){
        Toast.makeText(this, "Remember to not share your tokens public!", Toast.LENGTH_LONG).show();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = tokens.get(pos)+"";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    //http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public String getStringFromFile () throws Exception {
        return execu("cat /data/data/com.yeelight.cherry/shared_prefs/miot.xml");
    }

    private void xml(String xml) {
        try{
            xml = xml.replace("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n    <set name=\"deviceList\">\n        <string>","")
                    .replace("&quot;","\"")
                    .replace("<string>","")
                    .replace("</string>","")
                    .replace("</set>","")
                    .replace("</map>","")
                    .replace("        ","");

            xml = xml.replaceAll("<string name=\"account\">.*","");

            String[] xmls;
            xmls = xml.split("\n");

            for(int i = 0;i<xmls.length;i++){
                JSONObject reader = new JSONObject(xmls[i]);

                HashMap<String,String> temp = new HashMap<>();
                temp.put("device", reader.get("localip")+" ⇄ "+reader.get("mac")+"\n ⇨ "+reader.getString("name"));
                temp.put("token", reader.getString("token"));
                tokens.add(temp);
            }

        } catch(Exception e) {
            Log.d("Philz", e.toString());
        }
        lvTokens.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void copyToken2Clip(final int pos){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(tokens.get(pos).get("device"), tokens.get(pos).get("token")); //evtl. -1
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Token copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    //My Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.developer:
                startActivity(new Intent(MainActivity.this, VersionActivity.class));
                return true;
            case R.id.aboutme:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.phrogg.de"));
                startActivity(browserIntent);
                return true;
        }
        return false;
    }

    private String execu (String com){
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

            return output.toString();

        } catch (IOException | InterruptedException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}

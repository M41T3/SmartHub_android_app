package de.maltemueller.smarthub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.util.ArrayList;
import java.util.Locale;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends ActionBarActivity {

    BottomBar mBottomBar;
    ImageButton mMicButton;
    TextView mStatus;
   //TextView mTemp;

    private Socket mSocket;
    private boolean connected;
    //String temp_data;

    final static int SPEECHRECOGNITION_REQ_CODE = 22;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setCustomView(R.layout.actionbar_custom);



        mSocket = SocketInit.getSocket();
        mSocket.connect();
        mSocket.emit("app_connected", "Hello SMARTHUB");
        Log.d("SOCKET","Setup completed");

        //Button für Sprachaufnahme

        mMicButton = (ImageButton) findViewById(R.id.imageButtonMic);
        mMicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de");
                startActivityForResult(speechRecognitionIntent, SPEECHRECOGNITION_REQ_CODE);
            }
        });

        mStatus = (TextView) findViewById(R.id.textViewStatusMain);
        mStatus.setText("not connected");
        mStatus.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.smartHubRedStatus));

        WebView myWebView = (WebView) findViewById(R.id.main_webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(Constants.SMARTHUB_SERVER_URL); //http://192.168.178.38:8080/




        //Empfangen der welcome-msg

        mSocket.on("welcome_client", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                connected = (boolean) args[0];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connected) {
                            mStatus.setText("connected");
                            mStatus.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.smartHubGreenStatus));
                        } else {
                            mStatus.setText("not connected");
                            mStatus.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.smartHubRedStatus));
                        }
                    }
                });
            }
        });

        //Empfangen der Raumtemperatur

        //mSocket.on("room/temp", new Emitter.Listener() {

          //  @Override
          //  public void call(Object... args) {

          //      temp_data = args[0].toString();
                //sendInstance(Integer.parseInt(temp_data));

            //}
        //});



    }

    //Daten aus der speech-Recognition:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SPEECHRECOGNITION_REQ_CODE && resultCode == RESULT_OK){

            ArrayList<String> speechResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = speechResults.get(0);

            Log.d("speech", result);

            //Toast zur Textausgabe:

            Context context = getApplicationContext();
            CharSequence text = result;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            //-

            mSocket.emit("speech",result);

        } else if(resultCode != RESULT_OK ){

            Log.d("SPEECH", "Fail!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatus.setText("not connected");
        mStatus.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.smartHubRedStatus));
        mSocket.emit("app_connected", "Resume...");

    }

    @Override
    protected void onStop() {
        super.onStop();
        mStatus.setText("not connected");
        mStatus.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.smartHubRedStatus));
        mSocket.emit("app_disconnected", "disconnected");

    }
}
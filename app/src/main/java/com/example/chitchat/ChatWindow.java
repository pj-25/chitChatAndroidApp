package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;

import networkConnection.MessageConsumer;
import networkConnection.NetworkConnection;

public class ChatWindow extends AppCompatActivity {

    private NetworkConnection networkService=null;
    private String userName;
    private LinearLayout displayPane;

    public static ChatWindow chatWindow = null;
    public static final String MESSAGE_CONSUMER = "msgConsumer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        userName = (String)getIntent().getExtras().get(MainActivity.USER_NAME);
        displayPane = findViewById(R.id.chatpane);

        establishConnection();
        chatWindow = this;
    }

    public void promptMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void establishConnection(){

        Intent networkServiceIntent = new Intent(this, NetworkConnection.class);
        networkServiceIntent.putExtra(MainActivity.USER_NAME, userName);
        MessageConsumer msgConsumer = new MessageConsumer();
        networkServiceIntent.putExtra(MESSAGE_CONSUMER,msgConsumer);

        startService(networkServiceIntent);

        ServiceConnection networkConnectionService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                networkService = NetworkConnection.getNetworkService();
                promptMsg("Network Service Enabled");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                networkService = null;
                promptMsg("Network Service disabled");
            }
        };
        bindService(new Intent(this, NetworkConnection.class), networkConnectionService, Context.BIND_AUTO_CREATE);

    }


    public void sendMsg(View view){
        if(networkService!=null){
            TextView receiverName = findViewById(R.id.receiverName);
            TextView msgBox = findViewById(R.id.msgBox);
            if(receiverName.getText().length()>0 && msgBox.getText().length()>0) {
                try {
                    networkService.sendTo(receiverName.getText().toString(), msgBox.getText().toString());
                    displayMessage("To-> "+receiverName.getText().toString(), msgBox.getText().toString(), Gravity.RIGHT);
                    msgBox.setText("");
                }
                catch (IOException ioe){
                    displayMessage("Internet", "Server disconnected :(", Gravity.CENTER);
                }
            }
        }
        else{
            promptMsg("Connection not established :(");
        }
    }

    public static void displayMessage(String to, String msg, int gravityType){
        chatWindow.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TextView nameLbl = new TextView(chatWindow);
                nameLbl.setText(to);
                nameLbl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                nameLbl.setTextColor(Color.BLACK);
                nameLbl.setTypeface(null, Typeface.BOLD);

                TextView msgLbl = new TextView(chatWindow);
                msgLbl.setText(msg);
                msgLbl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                msgLbl.setTextColor(Color.BLACK);
                msgLbl.setTypeface(null, Typeface.BOLD);

                LinearLayout msgBox = new LinearLayout(chatWindow);
                msgBox.setOrientation(LinearLayout.VERTICAL);
                msgBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                msgBox.addView(nameLbl);
                msgBox.addView(msgLbl);
                msgBox.setGravity(gravityType);

                chatWindow.displayPane.addView(msgBox);
            }
        });
    }

}
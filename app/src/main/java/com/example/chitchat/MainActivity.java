package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String USER_NAME = "userName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void promptMsg(String msg){
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public void login(View view){
        String userID = ((EditText)findViewById(R.id.emailid)).getText().toString();
        String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
        if(userID.isEmpty()){
            promptMsg("*User ID is required!");
        }
        else{
            Intent chatWindow = new Intent(this, ChatWindow.class);
            chatWindow.putExtra(USER_NAME, userID);
            startActivity(chatWindow);
            promptMsg("Login successful :)");
        }
    }
}
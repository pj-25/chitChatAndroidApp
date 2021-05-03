package networkConnection;


import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat.ChatWindow;

import java.io.Serializable;

public class MessageConsumer implements DataConsumer, Serializable {

    @Override
    public void consume(String ...data){
        ChatWindow.displayMessage("From-> " +data[0], data[1], Integer.parseInt(data[2]));
    }
}

package networkConnection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.chitchat.ChatWindow;
import com.example.chitchat.MainActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkConnection extends Service {

    private String name;
    private ServerChannel socketHandler;
    private MessageConsumer msgConsumer;
    private MessageDecoder msgDecoder;

    public static Map<String, FriendChannel> friends = new HashMap<String, FriendChannel>();
    public static NetworkConnection networkService = null;

    public class LocalBinder extends Binder {
        NetworkConnection getService() {
            return NetworkConnection.this;
        }
    }

    public NetworkConnection(){}

    public NetworkConnection(String name, MessageConsumer msgConsumer){
        this.name = name;
        this.msgConsumer = msgConsumer;
    }

    public NetworkConnection(String name, MessageConsumer msgConsumer, String serverIP, int serverPort) throws IOException{
        this(name, msgConsumer);
        connect(serverIP, serverPort);
    }

    public NetworkConnection(String name, ServerChannel serverChannel) throws IOException{
        this.name = name;
        connect(serverChannel);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static NetworkConnection getNetworkService() {
        return networkService;
    }

    public static void setNetworkService(NetworkConnection networkService) {
        NetworkConnection.networkService = networkService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String userName = (String)intent.getExtras().get(MainActivity.USER_NAME);
        init(userName, new MessageConsumer());
        new Thread(()->{
            try{
                networkService = this;
                connect();
            }
            catch (IOException e){
                stopSelf();
            }
        }).start();
        return START_NOT_STICKY;
    }

    private void init(String userName, MessageConsumer dataConsumer) {
        this.name = userName;
        this.msgConsumer = dataConsumer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            close();
        } catch (IOException ignored) {
        }
    }

    private void connect(String serverIP, int serverPort) throws IOException{
        msgDecoder = new MessageDecoder(msgConsumer);
        connect(new ServerChannel(name, serverIP, serverPort, msgDecoder));
    }

    private void connect() throws IOException{
        msgDecoder = new MessageDecoder(msgConsumer);
        
        connect(new ServerChannel(name, msgDecoder));
    }

    private void connect(ServerChannel serverChannel) throws IOException{
        socketHandler = serverChannel;
        msgDecoder = (MessageDecoder) serverChannel.getDataConsumer();
        msgConsumer = (MessageConsumer) msgDecoder.getMsgConsumer();
        serverChannel.run();
        msgDecoder.setServerChannel(serverChannel);
    }

    public void connectTo(String friendID) throws IOException {
        FriendChannel friendChannel = new FriendChannel(friendID, msgConsumer);
        String connectionRequest = "1:" + friendID + ":" + friendChannel.getPort();
        socketHandler.send(connectionRequest);
        System.out.println("Connection request sent to " + friendID);
        friends.put(friendID, friendChannel);
    }

    public void sendTo(String name, String msg) throws IOException{
        FriendChannel friendChannel = friends.get(name);
        if(friendChannel!=null){
            try{
                friendChannel.writeMessage(msg);
            }
            catch (IOException e){
                System.out.println("Enable to send msg to "+name);
            }
        }
        else
            socketHandler.send("0:" + name + ":" + msg);
    }

    static public void disconnectWith(String friendID){
        friends.remove(friendID);
    }

    void close() throws IOException{
        if(socketHandler!=null)
            socketHandler.close();
            stopSelf();
        for(FriendChannel friendChannel: friends.values()){
            System.out.println( friendChannel.getFriendName() + " is disconnecting.....");
            friendChannel.close();
        }
        friends = null;
    }

    //getters and setters
    public ServerChannel getSocketHandler() {
        return socketHandler;
    }

    public void setSocketHandler(ServerChannel socketHandler) {
        this.socketHandler = socketHandler;
    }

    public MessageConsumer getMsgConsumer() {
        return msgConsumer;
    }

    public void setMsgConsumer(MessageConsumer msgConsumer) {
        this.msgConsumer = msgConsumer;
    }

    public MessageDecoder getMsgDecoder() {
        return msgDecoder;
    }

    public void setMsgDecoder(MessageDecoder msgDecoder) {
        this.msgDecoder = msgDecoder;
    }

    public static Map<String, FriendChannel> getFriends() {
        return friends;
    }

    public static void setFriends(Map<String, FriendChannel> friends) {
        NetworkConnection.friends = friends;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }
}

package server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class serverController implements Initializable {
    @FXML
    private TextField txtf_IpAddress;
    @FXML
    private TextField txtf_Port;
    @FXML
    private TextArea txta_ReceivedMessage;
    @FXML
    private TextArea txta_SendMessage;
    @FXML
    private ListView lv_ChooseCli;
    private ObservableList<String> clientList = FXCollections.observableArrayList();
    @FXML
    private Button btn_Send;
    private ConcurrentHashMap<String,Socket> clientMap;
    @Override
    public void initialize(URL location, ResourceBundle resources){
        lv_ChooseCli.setItems(clientList);
        lv_ChooseCli.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clientMap = new ConcurrentHashMap<String, Socket>();
        int port = Integer.parseInt(txtf_Port.getText());
        Thread listen = new listeningThread(port);
        listen.setDaemon(true);
        listen.start();
    }

    public void btn_SendAction(){
        ObservableList selectedList = lv_ChooseCli.getSelectionModel().getSelectedItems();
      //  for (Object s : selectedList) System.out.println(s.toString());
        for (Object s:selectedList){
            sendThread st = new sendThread(clientMap.get(s.toString()),txta_SendMessage.getText());
            st.start();
        }
    }

    private class sendThread extends Thread{
        private  Socket connection;
        String message;
        sendThread(Socket connection , String message){
            this.connection = connection;
            this.message = message;
        }
        @Override
        public void run(){
            try {
                PrintStream out = new PrintStream(connection.getOutputStream());
                out.println(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class receivedThread extends Thread{
        private  Socket connection;
        private  String clientAddress;
        receivedThread(Socket connection){
            this.connection = connection;
            //txta_ChooseCli.appendText(connection.getInetAddress().getHostAddress()+":"+String.format("%d",connection.getPort())+"\n");
            Platform.runLater(() -> {
                clientAddress = connection.getInetAddress().getHostAddress()+":"+String.format("%d",connection.getPort());
                clientList.add(clientAddress);
                clientMap.put(clientAddress,connection);
            });
        }
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                //  StringBuilder message = new StringBuilder();
                String s;
                while (true) {
                    s = in.readLine();
                    System.out.println("接收的信息：" + s);
                    //    message.append(s);
                    txta_ReceivedMessage.appendText(clientAddress+": "+s+"\n");
                }
            } catch (IOException e) {
                //  e.printStackTrace();
                System.out.println("断开连接");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < clientList.size(); i++) {
                            if (clientList.get(i).equals(clientAddress)) {
                                clientList.remove(i);
                                break;
                            }
                        }
                    }
                });
            }
        }
    }
    private class listeningThread extends Thread{
        private  int port;
        listeningThread(int port){
            this.port = port;
        }
        @Override
        public void run(){
            try(ServerSocket server = new ServerSocket(port)){
                while (true){
                        Socket connection = server.accept();
                        Thread task = new receivedThread(connection);
                        task.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

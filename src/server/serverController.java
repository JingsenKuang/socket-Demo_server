package server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class serverController {
    @FXML
    private TextField txtf_IpAddress;
    @FXML
    private TextField txtf_Port;
    @FXML
    private TextArea txta_ReceivedMessage;
    @FXML
    private TextArea txta_SendMessage;
    @FXML
    private TextArea txta_ChooseCli;
    @FXML
    private Button btn_Send;
    public void btn_SendAction() throws IOException {

    }
    public void startListening(){
        int port = Integer.parseInt(txtf_Port.getText());
        Thread listen = new listeningThread(port);
        listen.setDaemon(true);
        listen.start();
    }

    private class massageThread extends Thread{
        private  Socket connection;
        massageThread(Socket connection){
            this.connection = connection;
            txta_ChooseCli.appendText(connection.getInetAddress().getHostName());
        }
        @Override
        public void run(){
            try{
                BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
              //  StringBuilder message = new StringBuilder();
                String s;
                while(true){
                    s = in.readLine();
                    System.out.println("接收的信息："+s);
                //    message.append(s);
                    txta_ReceivedMessage.appendText(s);
                }

            }
            catch (IOException e) {
              //  e.printStackTrace();
                System.out.println("断开连接");
            }

        }
    }
    private class listeningThread extends Thread{
        int port;
        listeningThread(int port){
            this.port = port;
        }
        @Override
        public void run(){
            try(ServerSocket server = new ServerSocket(port)){
                while (true){
                    try{
                        Socket connection = server.accept();
                        Thread task = new massageThread(connection);
                        task.start();
                    }catch (IOException ex){}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

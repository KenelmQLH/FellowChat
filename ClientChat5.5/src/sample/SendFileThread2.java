package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;


public class SendFileThread2 extends Thread {
    private Socket clientSocket;
    private String name;
    private File sourceFile;
    private Stage stage;

    int MODE = 0; //if_G_upload:mode = 1, if_G_push:mode=2
    int Gid;
    int file_index = 0;
    boolean if_G_push = false;
    String filename_to_push;

    public SendFileThread2(Stage stage, Socket socket, String name, File file) {
        this.clientSocket = socket;
        this.name = name;
        this.sourceFile = file;
        this.stage = stage;
    }

    //后面还要加上 int file_index = 0
    //if_G_upload:mode = 1, if_G_push:mode=2
    public SendFileThread2(Stage stage, Socket socket, String name, File file, int mode, int Gid) {
        this.clientSocket = socket;
        this.name = name;
        this.sourceFile = file;
        this.stage = stage;
        this.MODE = mode;
        this.Gid = Gid;
    }


    public boolean f_alertt_confirmDialog(Alert.AlertType alertType, String p_header, String p_message, Stage stage){
//        按钮部分可以使用预设的也可以像这样自己 new 一个
        Alert _alert;
        if(alertType == Alert.AlertType.INFORMATION){
            _alert = new Alert(alertType, p_message ,new ButtonType("取消", ButtonBar.ButtonData.NO),
                    new ButtonType("确定", ButtonBar.ButtonData.YES));
        }
        else{
            _alert = new Alert(alertType, p_message);
        }
//        设置窗口的标题
        //_alert.setTitle("确认");
        _alert.setHeaderText(p_header);
//        设置对话框的 icon 图标，参数是主窗口的 stage
        _alert.initOwner(stage);
        //_alert.showAndWait();   //将在对话框消失以前不会执行之后的代码
        Optional<ButtonType> _buttonType = _alert.showAndWait();
//        根据点击结果返回ButtonType.OK

        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES) ){
            return true;
        }
        else {
            return false;
        }
    }


    public void run() {
        System.out.println("【send file to】ip = "+clientSocket.getLocalAddress()+"， port  = "+clientSocket.getPort());
        String sourceFilePath = sourceFile.getAbsolutePath();
        String fileName = sourceFile.getName();

        //System.out.println(sourceFilePath+"\\"+fileName);
        try {
            //命令连接
            PrintWriter sendToClient = new PrintWriter(clientSocket.getOutputStream());
            //BufferedReader getFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //文件连接
            ServerSocket fileSenderSocket = null;
            try {
                fileSenderSocket = new ServerSocket(0);
            } catch (IOException ex1) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        f_alertt_confirmDialog(Alert.AlertType.ERROR, "错误", "无法分配端口以发送此文件!",stage);
                    }
                });
            }

            String to_send_before;
            //%%%%%% f_next_index 以后扩充文件夹管理应该用得到，这里先不用
            // f_next_index应该由用户给（文件夹索引 + 文件索引）
            if(MODE == 1){
                to_send_before = "FILE_G_ADD_NEW#" + Gid  + "#" + fileSenderSocket.getLocalPort() + "#" + fileName + "#"
                        + String.valueOf(sourceFile.length()) + "#" + clientSocket.getLocalAddress() + "#" + name;
            }
            else if(MODE == 2){
                to_send_before = "FILE_G_PUSH#" + Gid + "#" + fileSenderSocket.getLocalPort() + "#" + fileName + "#"
                        + String.valueOf(sourceFile.length()) + "#" + clientSocket.getLocalAddress() + "#" + name;
            }
            else{
                to_send_before = "FILE_P2P#" + fileSenderSocket.getLocalPort() + "#" + fileName + "#"
                        + String.valueOf(sourceFile.length()) + "#" + clientSocket.getLocalAddress() + "#" + name;
            }
            sendToClient.println(to_send_before);
            sendToClient.flush();
            System.out.println("to_send_before : "+to_send_before);



            Socket fileReceiverSocket = fileSenderSocket.accept();
            BufferedReader getFromReceiver = null;
            PrintWriter sendToReceiver = null;
            try {
                getFromReceiver = new BufferedReader(new InputStreamReader(fileReceiverSocket.getInputStream()));
                sendToReceiver = new PrintWriter(fileReceiverSocket.getOutputStream());
            }
            catch (IOException ex) {
            } finally {
            }

            String judge = getFromReceiver.readLine();
            boolean if_finish = false;
            if (judge.equals("accepted")) {
                System.out.println(" i am accepted");
                DataOutputStream sendFileToReceiver = new DataOutputStream(fileReceiverSocket.getOutputStream());
                ProgressMonitorInputStream monitor;
                FileInputStream sendFileStream = null;

                try {
                    sendFileStream = new FileInputStream(sourceFile);

                } catch (FileNotFoundException ex) {
                    sendToReceiver.flush();
                    sendFileToReceiver.writeByte(0);
                    sendFileToReceiver.flush();
                    try {
                        fileReceiverSocket.close();
                        fileSenderSocket.close();
                    } catch (IOException ex1) {
                    }

                    return;
                }
                sendFileToReceiver.writeByte(1);
                sendFileToReceiver.flush();

                BufferedInputStream fis = new BufferedInputStream(sendFileStream);
                byte[] buf = new byte[1024];
                int length;
                try{
                    while((length = fis.read(buf, 0, buf.length))!=-1)
                    {
                        sendFileToReceiver.write(buf, 0, length);
                        sendFileToReceiver.flush();
                    }
                }
                catch (IOException e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            f_alertt_confirmDialog(Alert.AlertType.ERROR, "错误", "传输中断!",stage);
                        }
                    });
                }
                finally {
                    if (sendFileStream != null)
                        try {
                            sendFileStream.close();
                            if_finish = true;
                        } catch (IOException e) {
                        }
                }

            }
            else if (judge.equals("refused")) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        f_alertt_confirmDialog(Alert.AlertType.ERROR, "错误", "传输中断!",stage);
                    }
                });
            }

            //释放socket
            try {
                fileReceiverSocket.close();
                fileSenderSocket.close();

                if(if_finish){
                    //更新文件区
                    //something to de
                }
            } catch (IOException ex1) {
            }

        }
        catch (IOException ex) {
        }
        finally {
        }

    }
}


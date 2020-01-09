package sample;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;

public class C_ReceiveFileThread extends Thread {
    private String fileSenderIPAddress;
    private int fileSenderPortNumber;
    private String fileName;
    private long fileSize;
    private String otherNickname;
    private boolean permit;
    private File selectedDirectory;
    boolean if_G_down = false;

    Stage stage;
    TextArea textArea;

    public C_ReceiveFileThread(TextArea textArea, Stage stage, String ip, int port_number, String file_name, long file_size, String other_nickname,boolean permit,File selectedDirectory) {
        this.fileSenderIPAddress = ip;
        this.fileSenderPortNumber = port_number;
        this.fileName = file_name;
        this.fileSize = file_size;
        this.otherNickname = other_nickname;

        this.stage = stage;
        this.textArea = textArea;
        this.permit = permit;
        this.selectedDirectory = selectedDirectory;
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
        Socket fileSenderSocket = null;
        try {
            fileSenderSocket = new Socket(fileSenderIPAddress, fileSenderPortNumber);
        }
        catch(IOException ex) {
            f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"错误","无法连接到服务器接收文件", stage);
        }
        finally {
        }

        DataInputStream getFromSender = null;
        DataOutputStream sendToSender = null;
        try {
            getFromSender = new DataInputStream(new BufferedInputStream(fileSenderSocket.getInputStream()));
            sendToSender = new DataOutputStream(new BufferedOutputStream(fileSenderSocket.getOutputStream()));
            BufferedReader getInfoFromSender =new BufferedReader(new InputStreamReader(getFromSender));

            if(permit) {
                sendToSender.writeBytes("accepted\n");
                sendToSender.flush();
                //JFileChooser destinationFileChooser = new JFileChooser(".");
                //destinationFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //int status = destinationFileChooser.showSaveDialog(frame);

                try {
                    byte judge = getFromSender.readByte();
                    if (judge > 0) {
                        File savedFile = new File(selectedDirectory.getAbsolutePath()+"\\"+fileName);
                        FileOutputStream saveFileStream = new FileOutputStream(savedFile);
                        DataOutputStream fileOutput = new DataOutputStream(saveFileStream);

                        try {
                            byte[] buf = new byte[1024];
                            int length;
                            while((length = getFromSender.read(buf))!=-1) {
                                fileOutput.write(buf, 0, length);
                                fileOutput.flush();
                            }

                            fileOutput.flush();
                            if(savedFile.length() < fileSize) {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","传输中断!", stage);
                            }
                            textArea.appendText("接受文件："+fileName+"    保存地址："+selectedDirectory.getAbsolutePath()+"\r\n");
                        }
                        catch(IOException e){
                            f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","传输中断!", stage);
                        }
                        finally {
                            try {
                                fileOutput.close();
                                saveFileStream.close();
                            }
                            catch(IOException e){
                            }
                        }
                    }
                    else {
                        f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","源文件没有找到!", stage);
                    }
                }
                catch(IOException e){
                }
                finally {
                    fileSenderSocket.close();
                }



            }
            else{
                sendToSender.writeBytes("refused\n");
                sendToSender.flush();
            }
        }
        catch(IOException ex) {
        }
        finally {
        }
        //System.out.println("Receiver: "+fileReceiverSocket.getRemoteSocketAddress());
    }

}

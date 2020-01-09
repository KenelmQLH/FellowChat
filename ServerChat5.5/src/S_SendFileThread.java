import com.sun.org.apache.xpath.internal.objects.XBoolean;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;


public class S_SendFileThread extends Thread {
    private Socket clientSocket;
    private String name;
    JFileChooser sourceFileChooser;
    int status;
    JFrame JFrame;
    Boolean if_G_download = false;
    String filePath;

    public S_SendFileThread(JFrame frame, Socket socket, String name, JFileChooser sourceFileChooser, int status) {
        this.clientSocket = socket;
        this.name = name;
        this.sourceFileChooser = sourceFileChooser;
        this.status = status;
        this.JFrame = frame;
    }

    public S_SendFileThread(JFrame frame, Socket socket, String name, boolean if_G_download, String filePath) {

        this.clientSocket = socket;
        this.name = name;
        this.if_G_download =if_G_download;
        this.filePath = filePath;
        this.JFrame = frame;
    }

    public void run() {
//        JFileChooser sourceFileChooser = new JFileChooser(".");
//        sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        int status = sourceFileChooser.showOpenDialog(null);
        File sourceFile = null;
        String sourceFilePath;
        String fileName;
        if(if_G_download){
            sourceFile = new File(filePath);
        }
        else if (status == JFileChooser.APPROVE_OPTION) {
            sourceFile = new File(sourceFileChooser.getSelectedFile().getPath());

        }

        if(if_G_download || status == JFileChooser.APPROVE_OPTION){
            sourceFilePath = sourceFile.getAbsolutePath();
            fileName = sourceFile.getName();
            //System.out.println(sourceFilePath+"\\"+fileName);
            try {
                PrintWriter sendToClient = new PrintWriter(clientSocket.getOutputStream());
                BufferedReader getFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ServerSocket fileSenderSocket = null;
                try {
                    fileSenderSocket = new ServerSocket(0);
                } catch (IOException ex1) {
                    JOptionPane.showMessageDialog(JFrame, "无法分配端口以发送此文件!", "错误", JOptionPane.ERROR_MESSAGE);
                }

                String to_send_before;
                if(if_G_download){
                    to_send_before = "FILE_DOWNLOAD_OK#" + fileSenderSocket.getLocalPort() + "#" + fileName + "#"
                            + String.valueOf(sourceFile.length()) + "#" + clientSocket.getLocalAddress() + "#" + name;
                }
                else{
                    to_send_before = "FILE_P2P#" + fileSenderSocket.getLocalPort() + "#" + fileName + "#"
                            + String.valueOf(sourceFile.length()) + "#" + clientSocket.getLocalAddress() + "#" + name;
                }
                sendToClient.println(to_send_before);
                sendToClient.flush();

                System.out.println("to_send_before: "+to_send_before);
                //System.out.println("Sender: "+fileSenderSocket.getLocalSocketAddress());
                Socket fileReceiverSocket = fileSenderSocket.accept();

                BufferedReader getFromReceiver = null;
                PrintWriter sendToReceiver = null;
                try {
                    getFromReceiver = new BufferedReader(new InputStreamReader(fileReceiverSocket.getInputStream()));
                    sendToReceiver = new PrintWriter(fileReceiverSocket.getOutputStream());
                } catch (IOException ex) {
                } finally {
                }
                String judge = getFromReceiver.readLine();
                if (judge.equals("accepted")) {
                    System.out.println("accepted!!!!");
                    DataOutputStream sendFileToReceiver = new DataOutputStream(new BufferedOutputStream(fileReceiverSocket.getOutputStream()));
                    ProgressMonitorInputStream monitor;
                    FileInputStream sendFileStream = null;
                    try {
                        sendFileStream = new FileInputStream(sourceFile);
                    } catch (FileNotFoundException ex) {
                        sendToReceiver.flush();
                        sendFileToReceiver.writeByte(0);
                        sendFileToReceiver.flush();
                        return;
                    }
                    monitor = new ProgressMonitorInputStream(JFrame, "正在发送： " + fileName, sendFileStream);
                    ProgressMonitor progressMonitor = monitor.getProgressMonitor();

                    int read_unit = 500;
                    long fileSize = monitor.available();
                    int readed = 0;
                    byte[] data = new byte[read_unit];
                    try {
                        sendFileToReceiver.writeByte(1);
                        sendFileToReceiver.flush();
                        while (monitor.available() > 0) {
                            int in = monitor.read(data);
                            readed += in;
                            float process = (float) readed / fileSize * 100;
                            progressMonitor.setNote(process + " % 完成");
                            if (in > 0) {
                                sendFileToReceiver.write(data, 0, in);
                            }
                        }
                        sendFileToReceiver.flush();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(JFrame, "传输中断!", "错误", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        if (sendFileStream != null)
                            try {
                                sendFileStream.close();
                                monitor.close();
                            } catch (IOException e) {
                            }
                    }
                    try {
                        fileReceiverSocket.close();
                    } catch (IOException ex1) {
                    }
                } else if (judge.equals("refused")) {
                    JOptionPane.showMessageDialog(JFrame, "对方拒绝接受文件 " + fileName, "错误！", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (IOException ex) {
            } finally {
            }
        }

    }
}
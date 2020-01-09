package sample;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class C_SendFileThread extends Thread {
    private Socket clientSocket;
    private String name;
    JFileChooser sourceFileChooser;
    int status;
    JFrame JFrame;
    boolean if_G_upload =false;
    int Gid;
    int file_index = 0;

    boolean if_G_push = false;
    String filename_to_push;
    public C_SendFileThread(JFrame frame, Socket socket, String name, JFileChooser sourceFileChooser, int status) {
        this.clientSocket = socket;
        this.name = name;
        this.sourceFileChooser = sourceFileChooser;
        this.status = status;
        this.JFrame = frame;
    }

    //后面还要加上 int file_index = 0
    public C_SendFileThread(JFrame frame, Socket socket, String name, JFileChooser sourceFileChooser, int status, boolean if_G_upload, int Gid) {
        this.clientSocket = socket;
        this.name = name;
        this.sourceFileChooser = sourceFileChooser;
        this.status = status;
        this.JFrame = frame;
        this.if_G_upload = if_G_upload;
        this.Gid = Gid;
    }

    public C_SendFileThread(JFrame frame, Socket socket, String name, JFileChooser sourceFileChooser, int status, boolean if_G_push, int Gid, String filename_to_push) {
        this.clientSocket = socket;
        this.name = name;
        this.sourceFileChooser = sourceFileChooser;
        this.status = status;
        this.JFrame = frame;
        this.if_G_push = if_G_push;
        this.Gid = Gid;
        this.filename_to_push = filename_to_push;
    }


    public void run() {
//        JFileChooser sourceFileChooser = new JFileChooser(".");
//        sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        int status = sourceFileChooser.showOpenDialog(null);
        System.out.println("send file to:ip = "+clientSocket.getLocalAddress());
        System.out.println("[send file to]: port  = "+clientSocket.getPort());
        if (status == JFileChooser.APPROVE_OPTION) {
            File sourceFile = new File(sourceFileChooser.getSelectedFile().getPath());
            String sourceFilePath = sourceFile.getAbsolutePath();
            String fileName = sourceFile.getName();
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
                //%%%%%% f_next_index 以后扩充文件夹管理应该用得到，这里先不用
                // f_next_index应该由用户给（文件夹索引 + 文件索引）
                if(if_G_upload){
                    to_send_before = "FILE_G_ADD_NEW#" + Gid  + "#" + fileSenderSocket.getLocalPort() + "#" + fileName + "#"
                            + String.valueOf(sourceFile.length()) + "#" + clientSocket.getLocalAddress() + "#" + name;
                }
                else if(if_G_push){
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
                } catch (IOException ex) {
                } finally {
                }
                String judge = getFromReceiver.readLine();
                if (judge.equals("accepted")) {
                    System.out.println(" i am accepted");
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

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.StringTokenizer;


public class S_ReceiveFileThread extends Thread {
    private String fileSenderIPAddress;
    private int fileSenderPortNumber;
    private String fileName;
    private long fileSize;
    private String otherNickname;
    int upload_mode = 1;
    String path;
    int tmp_gid;
    boolean if_G_push = false;

    JFrame frame;
    JTextArea textArea;

    public S_ReceiveFileThread(JTextArea textArea, JFrame frame, String ip, int port_number, String file_name, long file_size, String other_nickname) {
        this.fileSenderIPAddress = ip;
        this.fileSenderPortNumber = port_number;
        this.fileName = file_name;
        this.fileSize = file_size;
        this.otherNickname = other_nickname;

        this.frame = frame;
        this.textArea = textArea;
    }

    //接收upload新文件 mode = 1(p2p) 2(add/upload) 3(push)
    public S_ReceiveFileThread(JTextArea textArea, JFrame frame, String ip, int port_number, String file_name, long file_size, String other_nickname, int upload_mode,String path,int tmp_gid ) {
        this.fileSenderIPAddress = ip;
        this.fileSenderPortNumber = port_number;
        this.fileName = file_name;
        this.fileSize = file_size;
        this.otherNickname = other_nickname;
        this.frame = frame;
        this.textArea = textArea;

        this.upload_mode = upload_mode;
        this.path = path;
        this.tmp_gid = tmp_gid;
    }


    public void run() {
            Socket fileSenderSocket = null;
            try {
                fileSenderSocket = new Socket(fileSenderIPAddress, fileSenderPortNumber);
            }
            catch(IOException ex) {
                JOptionPane.showMessageDialog(frame, "无法连接到服务器接收文件!", "错误", JOptionPane.ERROR_MESSAGE);
            }
            finally {
            }
            DataInputStream getFromSender = null;
            DataOutputStream sendToSender = null;
            try {
                getFromSender = new DataInputStream(new BufferedInputStream(fileSenderSocket.getInputStream()));
                sendToSender = new DataOutputStream(new BufferedOutputStream(fileSenderSocket.getOutputStream()));
                boolean if_start_receive = false;  //是否接收文件
                File destinationPath = null; //保存地址
                File savedFile;

                BufferedReader getInfoFromSender =new BufferedReader(new InputStreamReader(getFromSender));
                if(upload_mode >= 2){
                    sendToSender.writeBytes("accepted\n");
                    sendToSender.flush(); //###12_13坑死！！

                    if_start_receive = true;
                }
                else{
                    int permit = JOptionPane.showConfirmDialog(frame, "接受文件:"+fileName+" 从 "+
                            otherNickname+"?", "文件传输请求：", JOptionPane.YES_NO_OPTION);
                    if(permit == JOptionPane.YES_OPTION) {
                        sendToSender.writeBytes("accepted\n");
                        sendToSender.flush();

                        JFileChooser destinationFileChooser = new JFileChooser(".");
                        destinationFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int status = destinationFileChooser.showSaveDialog(frame);
                        //File destinationPath = null;
                        if (status == JFileChooser.APPROVE_OPTION) {
                            destinationPath = new File(destinationFileChooser.getSelectedFile().getPath());
                            System.out.println("[1 destinationPath] = "+destinationPath);
                             if_start_receive = true;
                        }
                        //try catch finally
                    }
                    else if(permit == JOptionPane.NO_OPTION) {
                        sendToSender.writeBytes("refused\n");
                        sendToSender.flush();
                    }
                }
                //System.out.println("the path : "+destinationPath.getPath());
                System.out.println("if_start_receive : " + if_start_receive);
                //开始接收文件
                if(if_start_receive){
                    //System.out.println("check0");
                    try {
                        //System.out.println("check1.0");
                        byte judge = getFromSender.readByte();
                        //System.out.println("check1");
                        if (judge > 0) {
                            //System.out.println("check2");
                            if(upload_mode >= 2){
                                savedFile = new File(path);
                            }
                            else{
                                savedFile = new File(destinationPath.getAbsolutePath()+"\\"+fileName);

                            }

                            FileOutputStream saveFileStream = new FileOutputStream(savedFile);
                            DataOutputStream fileOutput = new DataOutputStream(saveFileStream);
                            ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(frame, "接受文件： "+fileName, getFromSender);
                            //ProgressMonitor progressMonitor = new ProgressMonitor(null, "Receiving "+fileName, "", 0, (int)fileSize);
                            ProgressMonitor progressMonitor = monitor.getProgressMonitor();
                            progressMonitor.setMaximum((int)fileSize);
                            //System.out.println("check3");
                            int read_unit = 500;
                            int readed = 0;
                            float process = 0;
                            try {
                                //System.out.println("check3");
                                while (true) {
                                    byte[] data = new byte[read_unit];
                                    int in = monitor.read(data);
                                    readed += in;
                                    process = (float) readed / fileSize * 100;
                                    progressMonitor.setNote(process+" % 完成");
                                    progressMonitor.setProgress(readed);
                                    if (in <= 0) {
                                        break;
                                    }
                                    fileOutput.write(data,0,in);
                                }

                                fileOutput.flush();
                                if(savedFile.length() < fileSize) {
                                    JOptionPane.showMessageDialog(frame, "传输中断!", "错误", JOptionPane.ERROR_MESSAGE);
                                }
                                textArea.append("接受文件："+fileName+"    保存地址："+savedFile.getPath()+"\r\n");
                            }
                            catch(IOException e){
                                JOptionPane.showMessageDialog(frame, "传输中断!", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                            finally {
                                try {
                                    fileOutput.close();
                                    saveFileStream.close();
                                    progressMonitor.close();
                                }
                                catch(IOException e){
                                }
                            }
                        }
                        else {
                            JOptionPane.showMessageDialog(frame, "源文件没有找到!", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch(IOException e){
                        System.out.println("[start_receive error]");
                    }
                    finally {
                        //接收文件结束，开始操作数据库
                        fileSenderSocket.close();
                        if(upload_mode == 2){
                            //更改cgroups和file
                            try {
                                Connection con = null; //定义一个MYSQL链接对象
                                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                                Statement stmt; //创建声明
                                stmt = con.createStatement();
                                //######更改cgoroups#######//
                                String selectSql ="select * from cgroups where GID='"+tmp_gid+"';";
                                ResultSet selectRes = stmt.executeQuery(selectSql);
                                //获取组ids和组数量
                                selectRes.next();
                                int t_file_num = selectRes.getInt("file_num");
                                String t_file_ids = selectRes.getString("file_ids");

                                //待改：以后要与文件夹管理一致%%%%%12_15
                                int file_id_new = tmp_gid*100 + t_file_num+1;

                                //更新file_ids / file_num
                                t_file_num += 1;
                                t_file_ids += "#" + file_id_new;
                                String updateSql = "UPDATE cgroups SET file_num = '"+ t_file_num +"' WHERE GID = " + tmp_gid + "";
                                stmt.executeUpdate(updateSql);
                                updateSql = "UPDATE cgroups SET file_ids ='"+ t_file_ids+ "'WHERE GID = " + tmp_gid + "";
                                stmt.executeUpdate(updateSql);
                                System.out.println("file_id_new = "+file_id_new);
                                //String fileName_with_gid = tmp_gid + "_" + fileName;另一种方法
                                //######添加file#######//
                                String insertSql = "INSERT INTO file (FID, FileName, new_num,his_num,gid) VALUES ('" + file_id_new + "','"
                                        + fileName + "', '" + 0 + "','" + 0 + "','" + tmp_gid+"');";
                                stmt.execute(insertSql);

                            } catch (Exception e) {
                                System.out.print("MYSQL ERROR 【mode2】" + e.getMessage());
                                return ;
                            }
                            System.out.println("[final of if_G_upload_of_add]");
                        }
                        else if(upload_mode == 3){
                            try {
                                Connection con = null; //定义一个MYSQL链接对象
                                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                                Statement stmt; //创建声明
                                stmt = con.createStatement();
                                //######更改cgoroups#######//
                                String selectSql ="select * from file where FileName='"+fileName+"' and gid = '" + tmp_gid + "';";
                                ResultSet selectRes = stmt.executeQuery(selectSql);
                                //获取组ids和组数量
                                selectRes.next();
                                int new_num = selectRes.getInt("new_num") + 1;

                                String updateSql = "UPDATE file SET new_num ='"+ new_num+ "' WHERE gid = '" + tmp_gid + "' and FileName = '" + fileName + "';";
                                stmt.executeUpdate(updateSql);

                            } catch (Exception e) {
                                System.out.print("MYSQL ERROR 【mode3】:" + e.getMessage());
                            }

                            System.out.println("[final of if_G_push]");
                        }


                    }
                }

            }
            catch(IOException ex) {
            }
            finally {

            }
        //System.out.println("Receiver: "+fileReceiverSocket.getRemoteSocketAddress());
    }

}
import com.mysql.cj.xdevapi.Client;
import sun.misc.BASE64Encoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//2019_12_16

public class Server {

    private JFrame frame;
    private JTextArea contentArea;
    private JTextField txt_message;
    private JTextField txt_max;
    private JTextField txt_port;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private JButton btn_send_file;
    private JPanel northPanel;
    private JPanel southPanel;
    private JPanel sendPanel;
    private JScrollPane rightPanel;
    private JScrollPane leftPanel;
    private JScrollPane rightPanel2;
    private JSplitPane centerSplit;
    private JSplitPane centerSplit2;
    private JList userList;
    private JList all_userList;
    private DefaultListModel listModel;
    private static DefaultListModel all_listModel;

    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private ArrayList<ClientThread> clients;//客户线程数组

    private boolean isStart = false;//标志服务器是否启动或关闭

    private int send_for_who = 0;//监听左边jlist，保存给哪个用户发消息
    private HashMap<String,Integer> username_to_id;
    private HashMap<Integer,String> id_to_name;
    // 主方法,程序执行入口
    public static void main(String[] args) {
        try {

            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动jdbc:mysql://172.81.245.233:3306/demo
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();
        } catch (Exception e) {
            System.out.println("[main] MYSQL ERROR:" + e.getMessage());
            //System.out.println("MYSQL ERROR:" + e.toString()) ;
        }
        new Server();

    }

    // 构造方法
    public Server() {
        //System.out.println("in server!!");
        username_to_id = new HashMap<String,Integer>();
        id_to_name = new HashMap<Integer, String>();
        SelecTry selectIndex = new SelecTry();
        frame = new JFrame("服务器");
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(Color.blue);
        txt_message = new JTextField();
        txt_max = new JTextField("30");
        txt_port = new JTextField("6666");
        btn_start = new JButton("启动");
        btn_stop = new JButton("停止");
        btn_send = new JButton("发送");
        btn_send_file = new JButton("文件");
        btn_stop.setEnabled(false);
        listModel = new DefaultListModel();
        all_listModel = new DefaultListModel();
        //listModel.addElement("全部用户");
        userList = new JList(all_listModel);//listModel
        userList.addListSelectionListener(selectIndex);

        user_name_update();//更新用户状态
//        all_userList = new JList(all_listModel);

        southPanel = new JPanel(new BorderLayout());
        sendPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("写消息"));
        southPanel.add(txt_message, "Center");
        sendPanel.add(btn_send, BorderLayout.NORTH);
        sendPanel.add(btn_send_file, BorderLayout.SOUTH);

        southPanel.add(sendPanel, "East");

        leftPanel = new JScrollPane(userList);
        leftPanel.setBorder(new TitledBorder("用户列表"));

//        rightPanel2 = new JScrollPane(all_userList);
//        rightPanel2.setBorder(new TitledBorder("全部用户"));

        rightPanel = new JScrollPane(contentArea);
        rightPanel.setBorder(new TitledBorder("消息显示区"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
                rightPanel);
        centerSplit.setDividerLocation(150);

//        centerSplit2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerSplit,
//                rightPanel2);
//        centerSplit2.setDividerLocation(450);

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 6));
        northPanel.add(new JLabel("          人数上限"));
        northPanel.add(txt_max);
        northPanel.add(new JLabel("           端口"));
        northPanel.add(txt_port);
        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(new TitledBorder("配置信息"));

        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        //frame.add(rightPanel2,BorderLayout.EAST);
        frame.add(southPanel, "South");
        frame.setSize(600, 400);
        //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2,
                (screen_height - frame.getHeight()) / 2);
        frame.setVisible(true);

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isStart) {
                    closeServer();// 关闭服务器
                }
                System.exit(0);// 退出程序
            }
        });

        // 文本框按回车键时事件
        txt_message.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 单击发送按钮时事件
        btn_send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

        //单机文件按钮时事件(我顶，居然是给所有在线的用户发送文件)
        btn_send_file.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                //文件选择对话框启动，当选择了文件以后给每一个client发送文件
                JFileChooser sourceFileChooser = new JFileChooser(".");
                sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int status = sourceFileChooser.showOpenDialog(frame);
                File sourceFile = new File(sourceFileChooser.getSelectedFile().getPath());
                //服务器text area提示
                contentArea.append("发送文件：" + sourceFile.getName() + "\r\n");
                for (int i = clients.size() - 1; i >= 0; i--) {
                    S_SendFileThread sendFile = new S_SendFileThread(frame, clients.get(i).socket, "服务器", sourceFileChooser, status);
                    sendFile.start();
                    //client端提示
                    clients.get(i).getWriter().println("服务器发送一个文件：" + sourceFile.getName() + "(多人发送)");
                    clients.get(i).getWriter().flush();
                }

            }
        });

        // 单击启动服务器按钮时事件
        btn_start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (isStart) {
                    JOptionPane.showMessageDialog(frame, "服务器已处于启动状态，不要重复启动！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int max;//人数
                int port;//端口号
                try {
                    try {
                        max = Integer.parseInt(txt_max.getText());
                    } catch (Exception e1) {
                        throw new Exception("人数上限为正整数！");
                    }
                    if (max <= 0) {
                        throw new Exception("人数上限为正整数！");
                    }
                    try {
                        port = Integer.parseInt(txt_port.getText());
                    } catch (Exception e1) {
                        throw new Exception("端口号为正整数！");
                    }
                    if (port <= 0) {
                        throw new Exception("端口号 为正整数！");
                    }
                    serverStart(max, port);
                    contentArea.append("服务器已成功启动!   人数上限：" + max + ",  端口：" + port
                            + "\r\n");
                    JOptionPane.showMessageDialog(frame, "服务器成功启动!");
                    btn_start.setEnabled(false);
                    txt_max.setEnabled(false);
                    txt_port.setEnabled(false);
                    btn_stop.setEnabled(true);
                    listModel.addElement("全部用户");
                    user_name_update();
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 单击停止服务器按钮时事件
        btn_stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isStart) {
                    JOptionPane.showMessageDialog(frame, "服务器还未启动，无需停止！", "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    closeServer();
                    btn_start.setEnabled(true);
                    txt_max.setEnabled(true);
                    txt_port.setEnabled(true);
                    btn_stop.setEnabled(false);
                    contentArea.append("服务器成功停止!\r\n");
                    JOptionPane.showMessageDialog(frame, "服务器成功停止！");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, "停止服务器发生异常！", "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /*--------------------------辅助函数------------------------------*/

    /*------------------------服务器 C/S 交互（可广播可P2P）-------------------------------*/


    /**
     * 刚服务器突然关闭时，把所有用户状态置为离线
     */
    public void set_user_state_off() {
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            int id = 0;
            String selectSql = "UPDATE user SET state = 0";
            stmt.executeUpdate(selectSql);
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
    }
    /**
     * 更新用户状态
     */
    public void user_name_update() {

        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();
            //####12_16
            all_listModel.removeAllElements();
            username_to_id.clear();
            id_to_name.clear();
            all_listModel.addElement("全部用户");

            String username_db;
            int state = 0;
            int user_id = 0;
            //查询用户名
            String selectSql = "SELECT * FROM user where state = '1';";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            //显示在线的人，并记录user and id
            while (selectRes.next()) { //循环输出结果集
                username_db = selectRes.getString("username");
                user_id = selectRes.getInt("Id");
                state = selectRes.getInt("state");
                all_listModel.addElement(username_db + "---(在线)");

                username_to_id.put(username_db, user_id);
                id_to_name.put(user_id,username_db);
            }

            //显示离线的人,并记录user and id
            selectSql = "SELECT * FROM user where state = '0';";
            selectRes = stmt.executeQuery(selectSql);
            while (selectRes.next()) { //循环输出结果集
                username_db = selectRes.getString("username");
                user_id = selectRes.getInt("Id");state = selectRes.getInt("state");

                all_listModel.addElement(username_db + "---(离线)");

                username_to_id.put(username_db, user_id);
                id_to_name.put(user_id,username_db);

            }
        } catch (Exception e) {
            System.out.println("[user_name_update] MYSQL ERROR:" + e.getMessage() + "(user list  update error)");
        }

    }

    /**
     * 执行消息发送
     */
    public void send() {
        if (!isStart) {
            JOptionPane.showMessageDialog(frame, "服务器还未启动,不能发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
//        if (clients.size() == 0) {
//            JOptionPane.showMessageDialog(frame, "没有用户在线,不能发送消息！", "错误",
//                    JOptionPane.ERROR_MESSAGE);
//            return;
//        }
        String message = txt_message.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendServerMessage(message, send_for_who);// 群发服务器消息
        contentArea.append("对  "+listModel.getElementAt(send_for_who)+"  说：" + txt_message.getText() + "\r\n");
        txt_message.setText(null);
    }

    /**
     * 群发服务器消息
     *
     * @param message
     * @param who
     */
    public void sendServerMessage(String message, int who) {
        if (who == 0) {
            StringTokenizer stringTokenizer;
            int flag = 0;
            for (int i = all_listModel.size(); i > 0; i--) {
                flag = 0;
                String msg = all_listModel.getElementAt(i - 1) + "";
                stringTokenizer = new StringTokenizer(
                        msg, "---");
                String user_name = stringTokenizer.nextToken();
                for (int j = clients.size() - 1; j >= 0; j--) {
                    if (user_name.equals(clients.get(j).getUser().getName())) {
                        clients.get(j).getWriter().println("服务器对你说   " + message);
                        clients.get(j).getWriter().flush();
                        flag = 1;//该用户在线状态，已发出去
                        break;
                    }
                }
                if (flag == 0) {
                    //用户离线状态，则留言
                    send_messageTo_board("服务器", user_name, message);
                }
            }
            contentArea.append("对  全部用户   发送：" + message + "\r\n");
        }
        else {
            int flag = 0;
            String msg = "" + all_listModel.getElementAt(who);
            StringTokenizer stringTokenizer = new StringTokenizer(
                    msg, "---");
            String user_name = stringTokenizer.nextToken();
            for (int i = clients.size() - 1; i >= 0; i--) {
                if (user_name.equals(clients.get(i).getUser().getName())) {
                    clients.get(i).getWriter().println("服务器对你说   " + message);
                    clients.get(i).getWriter().flush();
                    flag = 1;//该用户在线状态，已发出去
                    break;
                }
            }
            if (flag == 0) {
//                JOptionPane.showMessageDialog(frame, "该用户不在线，已存为留言板！", "错误",
//                        JOptionPane.ERROR_MESSAGE);
                send_messageTo_board("服务器", user_name, message);
                contentArea.append("对  " + user_name + "  留言：" + message + "\r\n");
            }
            else {
                contentArea.append("对  " + user_name + "  说：" + message + "\r\n");
            }
        }

    }

    /**
     * 监听左边jlist选择的是哪一个用户
     */
    class SelecTry implements ListSelectionListener {
        int change = 0, who;

        public void valueChanged(ListSelectionEvent e) {
            send_for_who = userList.getSelectedIndex();
        }

    }


    /*------------------------处理用户 登录页面请求-------------------------------*/

    /**
     * 找回密码模块
     *
     * @param username
     * @param mail
     * @param new_password
     * @return
     */
    public int user_forget(String username, String mail, String new_password) {
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            String codingpassword = EncoderByMd5(new_password);

            //查询数据，不能有相同的用户名
            String selectSql = "SELECT * FROM user";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            while (selectRes.next()) { //循环输出结果集
                int userid = selectRes.getInt("Id");
                String username_db = selectRes.getString("username");
                String mail_db = selectRes.getString("mail");
                if (username.equals(username_db)) {
                    if (mail_db.equals(mail)) {
                        //更新一条数据
                        String updateSql = "UPDATE user SET password = '" + codingpassword + "' WHERE Id = " + userid + "";
                        long updateRes = stmt.executeUpdate(updateSql);
                        return 1;
                    }

                }
            }
            return 0;
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
        return 0;
    }

    /**
     * 注册模块
     *
     * @param username
     * @param password
     * @param mail
     * @return
     */
    public int user_register(String username, String password, String mail) {

        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            String codingPassword = EncoderByMd5(password);
            //查询数据，不能有相同的用户名
            String selectSql = "SELECT * FROM user";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            int id=1;
            boolean flag = false;
            while (selectRes.next()) { //循环输出结果集
                String username_db = selectRes.getString("username");
                if (username.equals(username_db)) {
                    return 2;
                    //名字重复
                }
                id++;
                //System.out.println("######id = \n"+id);
            }
            //新增一条数据
            stmt.execute("INSERT INTO user (Id,username, password,mail,state) VALUES ('" + id + "','"
                    + username + "', '" + codingPassword + "','" + mail + "','" + 0 + "');"
            );
            all_listModel.addElement(username);
            return 1;
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
        return 0;
    }

    /**
     * 当有用户下线时，在服务器改变状态
     *
     * @param name
     * @return
     */
    public int user_offLine(String name) {
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            //
            String username_fromDb;
            int id = 0;
            String selectSql = "SELECT * FROM user";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            while (selectRes.next()) { //循环输出结果集
                username_fromDb = selectRes.getString("username");
                id = selectRes.getInt("Id");
                if (name.equals(username_fromDb)) {
                    selectSql = "UPDATE user SET state = 0  WHERE Id = " + id + "";
                    stmt.executeUpdate(selectSql);
                    selectSql = "UPDATE user SET serverPort = 0  WHERE Id = " + id + "";
                    stmt.executeUpdate(selectSql);
                    selectSql = "UPDATE user SET ipAddress = ''  WHERE Id = " + id + "";
                    stmt.executeUpdate(selectSql);
                    return 1;
                }
            }
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
        return 0;
    }

    /**
     * 登陆模块
     *
     * @param username
     * @param password
     * @return
     */
    //12_16
    public int user_login(String username, String password,int serverPort,String myIP) {
        int state = 0, id = 0;
        boolean ifFound = true;
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            String codingNewPassword;
             codingNewPassword = EncoderByMd5(password);
            //System.out.println("the codingNewPassword = "+codingNewPassword);
            String selectSql = "SELECT * FROM user where username = '" +username +"' and password = '" +codingNewPassword +"';";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            selectRes.next();
            id = selectRes.getInt("Id");
            state = selectRes.getInt("state");

        } catch (Exception e) {
            System.out.print("[MYSQL ERROR]: not found user , " + e.toString());
            ifFound = false;
        }
        //若登录合法则更新用户ip、port、状态
        if(ifFound){
            if (state == 0) {
                try {
                    Connection con = null; //定义一个MYSQL链接对象
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                    con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                    Statement stmt; //创建声明
                    stmt = con.createStatement();

                    String selectSql = "UPDATE user SET state = 1  WHERE Id = " + id + "";
                    stmt.executeUpdate(selectSql);
                    selectSql = "UPDATE user SET serverPort = " + serverPort + "  WHERE Id = " + id + "";
                    stmt.executeUpdate(selectSql);
                    selectSql = "UPDATE user SET ipAddress = '" + myIP + "'  WHERE Id = " + id + "";
                    stmt.executeUpdate(selectSql);
                    return 1;//还没有登陆，可以登陆
                } catch (Exception e) {
                    System.out.print("[MYSQL ERROR]: update user err , " + e.toString());
                    return 0;
                }
            }
            else {
                return 2;//已登陆状态，无法登陆
            }
        }
        return 0;
    }
    /**利用MD5进行加密
     * @param str  待加密的字符串
     * @return  加密后的字符串
     * @throws NoSuchAlgorithmException  没有这种产生消息摘要的算法
     * @throws UnsupportedEncodingException
     */
    public String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }
    /**判断用户密码是否正确
     * @param newpasswd  用户输入的密码
     * @param oldpasswd  数据库中存储的密码－－用户密码的摘要
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public boolean checkpassword(String newpasswd,String oldpasswd) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if(EncoderByMd5(newpasswd).equals(oldpasswd))
            return true;
        else
            return false;
    }

    /*------------------------处理用户 主页面请求-------------------------------*/
    /**
     * 用户不在线时，保存 点对点 的离线消息
     *
     */
    public int send_messageTo_board(String send_from, String send_for, String message) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String msg = send_from + "#" + df.format(new Date()) + "#" + message + "#";
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            //查询数据，不能有相同的用户名
            String selectSql = "SELECT * FROM user where username = '" + send_for +"';";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            selectRes.next();//循环输出结果集
            int Id = selectRes.getInt("Id");
            String old_message = selectRes.getString("message");
            String updateSql = "UPDATE user SET message = '" + old_message + msg + "' WHERE Id = " + Id + "";
            stmt.executeUpdate(updateSql);
            return 1;
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
        return 0;
    }
    /**
     * 创建群组
     * */
    public int create_group(String msg){
        //somgthing to do
        System.out.println(msg);
        StringTokenizer msgTokenizer = new StringTokenizer(
                msg, "#");
        String cmd = msgTokenizer.nextToken();//去除首命令
        int new_gid = Integer.parseInt(msgTokenizer.nextToken());//组id
        String new_gname = msgTokenizer.nextToken();//组名
        int master_id = Integer.parseInt(msgTokenizer.nextToken());//创建者
        int member_number = 1;

        String g_users_id = "#" + master_id;
        ArrayList<Integer> users_in_g = new ArrayList<Integer>();
        users_in_g.add(master_id);
        while(msgTokenizer.hasMoreTokens()){
            int user_id = Integer.parseInt(msgTokenizer.nextToken());
            g_users_id += ("#" + user_id);
            users_in_g.add(user_id);
            member_number++;
        }

        //在cgroups中添加记录
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            //新增一条数据
            stmt.execute("INSERT INTO cgroups (GID,master, member_num,member_ids,Gname) VALUES ('" + new_gid + "','"
                    + master_id + "', '" + member_number + "','" + g_users_id + "','"+ new_gname + "');"
            );
        }
        catch (Exception e) {
            System.out.println("[create_group] MYSQL ERROR——1:" + e.getMessage());
            //System.out.println("MYSQL ERROR:" + e.toString()) ;
            return 0;
        }

        //在user中添加记录
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();
            for(int user : users_in_g){
                String sql = "SELECT * FROM user where Id ='"+user+"' ;";
                //修改G_num\G_next\GIDs
                ResultSet selectRes = stmt.executeQuery(sql);
                selectRes.next();
                int g_num = Integer.parseInt(selectRes.getString("G_num")) + 1;
                int g_next = Integer.parseInt(selectRes.getString("G_next"));

                if(user == master_id){
                    g_next++;//g_last为创建者创建的群组下一个索引
                }
                String gids = selectRes.getString("GIDs") + "#" + new_gid;
                String sql2 = "update user SET G_num ='" + g_num + "',G_next ='" + g_next +
                        "',GIDs = '" + gids +"' where Id ='"+user+"';";
                //新增一条数据
                stmt.executeUpdate(sql2);
                System.out.println("[Updated] g_num = "+g_num+", g_last="+g_next+",gids = "+gids);

            }
        } catch (Exception e) {
            System.out.println("[create_group] MYSQL ERROR——2:" + e.getMessage());
            return 0;
            //System.out.println("MYSQL ERROR:" + e.toString()) ;
        }
        try{
            File file=new File("D:\\chat\\file\\" + new_gid);
            if(!file.exists()){//如果文件夹不存在
                file.mkdir();//创建文件夹
            }
        }catch (Exception e){
            System.out.println("[create group dir] MYSQL ERROR——3:" + e.getMessage());
            return 0;
        }


        return 1;//成功创建群组
    }
    /**
     * 打开群组
     * */
    //###12_14

    /*------------------------处理用户 群聊页面请求-------------------------------*/
    /**
     * 用户不在线时，保存 群组 的离线消息
     *
     */
    //待改12_16
    public int G_send_messageTo_board(String send_from, String send_for , int gid, String message) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String msg = gid +"#" + send_from + "#" + df.format(new Date()) + "#" + message + "#"; //这里用’$‘拆分
        System.out.println("[ G_send_messageTo_board ]" + msg);
        try {

            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            //查询数据，不能有相同的用户名
            String selectSql = "SELECT * FROM user where username = '" + send_for +"';";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            selectRes.next();//循环输出结果集
            int Id = selectRes.getInt("Id");
            String old_message = selectRes.getString("G_message");
            String updateSql = "UPDATE user SET G_message = '" + old_message + msg + "' WHERE Id = " + Id + "";
            stmt.executeUpdate(updateSql);
            return 1;
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
        return 0;
    }
    /**
     * 获取指定群组的信息
     * **/
    public static String open_group_for_info(int c_gid) {
        //###12_14
        String info = "";
        //@@@查询数据库返回群组的初始化信息
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();

            String g_selectSql = "select * from cgroups where GID = '" + c_gid +"';";
            //System.out.print("aaa");
            ResultSet g_selectRes = stmt.executeQuery(g_selectSql);
            g_selectRes.next();

            String groupname = g_selectRes.getString("Gname");
            int member_num = g_selectRes.getInt("member_num");
            String member_ids = g_selectRes.getString("member_ids");
            int file_num = g_selectRes.getInt("file_num");
            String file_ids = g_selectRes.getString("file_ids");
            String file_names_with_info = "";
            try {

                String g_selectSql2 = "select * from file where gid = '" + c_gid +"';";
                ResultSet g_selectRes2 = stmt.executeQuery(g_selectSql2);
                while(g_selectRes2.next()){
                    file_names_with_info += ("#" + g_selectRes2.getString("FileName") + "/ " +g_selectRes2.getString("new_num"));
                }


            } catch (Exception e) {
                System.out.print("[MYSQL ERROR]: return group message err , " + e.toString());
                return "";
            }

            info += groupname + "#" + member_num +member_ids+ "#" + file_num + file_ids + file_names_with_info ;
        } catch (Exception e) {
            System.out.print("[MYSQL ERROR]: return group message err , " + e.toString());
            return "";
        }

        return info;
    }
    /**
     * 上传 新的文件
     * **/
    public int add_new(String msg){
        System.out.println("in [FILE_G_ADD_NEW]");
        StringTokenizer stringTokenizer = new StringTokenizer(
                msg, "#/");
        String command = stringTokenizer.nextToken();//  去除命令

        //something to do with uploaded files
        int tmp_gid = Integer.parseInt(stringTokenizer.nextToken());
        int tmp_portNumber = Integer.parseInt(stringTokenizer.nextToken());
        String fileName = stringTokenizer.nextToken();
        long fileSize = Long.parseLong(stringTokenizer.nextToken());
        String tmp_ip = stringTokenizer.nextToken();
        String Nickname = stringTokenizer.nextToken();

        boolean if_same=true;
        //先做一下判别测试，如果是同名文件，应该选择push而不是add
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
            Statement stmt; //创建声明
            stmt = con.createStatement();
            //######更改cgoroups#######//
            System.out.print("xxxA");
            String selectSql ="select * from file where FileName='"+ fileName+"' and gid = '" + tmp_gid + "';";
            ResultSet selectRes = stmt.executeQuery(selectSql);
            selectRes.next();
            String xFileName =selectRes.getString("FileName");
            System.out.println("xFileName = "+ xFileName);
        }
        catch (Exception e) {
            System.out.print("MYSQL ERROR [add xxe] :" + e.getMessage() + "\n");
            if_same = false;
        }

        //若存在同名文件，则返回
        if(if_same){
            return 2;
        }

        StringTokenizer filenameTokenizer = new StringTokenizer(
                fileName, ".");
        String head = filenameTokenizer.nextToken();
        String tail = filenameTokenizer.nextToken();
        //+ tmp_gid + "\\"
        String path = "D:\\chat\\file\\"  + tmp_gid  + "\\"+ head + "_k." +tail;
        S_ReceiveFileThread receiveFile = new S_ReceiveFileThread(contentArea,frame,
                tmp_ip, tmp_portNumber, fileName, fileSize, Nickname,2,path, tmp_gid);
        receiveFile.start();
        contentArea.append("从"+Nickname+"接受文件(upload):"+fileName+",大小为:"+fileSize
                +"ip:"+tmp_ip+"port:"+tmp_portNumber+"\r\n");

        return 1;
    }
    /**
     * 上传 某一文件的新的版本
     * **/
    public int push(String msg){
        //####12_13
        //待改: 服务端接受客户端上传的文件，并存储文件和记录
        System.out.println("in [FILE_G_push]");
        StringTokenizer stringTokenizer = new StringTokenizer(
                msg, "#/");
        String command = stringTokenizer.nextToken();// 去除命令

        int tmp_gid = Integer.parseInt(stringTokenizer.nextToken());
        //%%%%%%12_15暂时不考虑文件夹吧,暂时根据选择的文件索引来更新文件版本
        //int tmp_f_chosen_index = Integer.parseInt(stringTokenizer.nextToken());
        String str_port = stringTokenizer.nextToken();
        System.out.println("str_port = " +str_port);
        int tmp_portNumber = Integer.parseInt(str_port);

        String fileName = stringTokenizer.nextToken();
        long fileSize = Long.parseLong(stringTokenizer.nextToken());
        String tmp_ip = stringTokenizer.nextToken();
        String Nickname = stringTokenizer.nextToken();
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
            int new_index = selectRes.getInt("new_num") + 1;
            StringTokenizer filenameTokenizer = new StringTokenizer(
                    fileName, ".");
            String head = filenameTokenizer.nextToken();
            String tail = filenameTokenizer.nextToken();
            String new_path = "D:\\chat\\file\\" + tmp_gid + "\\"+  head + "_n" + new_index + "." + tail;

            S_ReceiveFileThread receiveFile = new S_ReceiveFileThread(contentArea,frame,
                    tmp_ip, tmp_portNumber, fileName, fileSize, Nickname,3,new_path,tmp_gid);
            receiveFile.start();
            contentArea.append("从"+Nickname+"接受文件(push):"+fileName+",大小为:"+fileSize
                    +"ip:"+tmp_ip+"port:"+tmp_portNumber+"\r\n");

        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
            return 0;
        }


        return 1;
    }
    /**
     * 下载 指定文件
     * **/

    /**
     * 下载 全部文件
     * **/

    /*------------------------主要服务框架-----------------------------*/

    /**
     * 启动服务器
     *
     * @param max
     * @param port
     * @throws java.net.BindException
     */
    public void serverStart(int max, int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket, max);
            serverThread.start();
            isStart = true;
        } catch (BindException e) {
            isStart = false;
            throw new BindException("端口号已被占用，请换一个！");
        } catch (Exception e1) {
            e1.printStackTrace();
            isStart = false;
            throw new BindException("启动服务器异常！");
        }
    }
    /**
     * 关闭服务器
     */
    @SuppressWarnings("deprecation")
    public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();// 停止服务器线程

            for (int i = clients.size() - 1; i >= 0; i--) {
                // 给所有在线用户发送关闭命令
                clients.get(i).getWriter().println("CLOSE");
                clients.get(i).getWriter().flush();
                // 释放资源
                clients.get(i).stop();// 停止此条为客户端服务的线程
                clients.get(i).reader.close();
                clients.get(i).writer.close();
                clients.get(i).socket.close();
                clients.remove(i);
            }
            if (serverSocket != null) {
                serverSocket.close();// 关闭服务器端连接
            }
            listModel.removeAllElements();// 清空用户列表
            isStart = false;
            set_user_state_off();
            user_name_update();
        } catch (IOException e) {
            e.printStackTrace();
            isStart = true;
        }
    }


    /**
     * 服务器线程
     */
    class ServerThread extends Thread {
        private ServerSocket serverSocket;
        private int max;// 人数上限

        // 服务器线程的构造方法
        public ServerThread(ServerSocket serverSocket, int max) {
            this.serverSocket = serverSocket;
            this.max = max;
        }

        public void run() {
            System.out.println("[start server ok] "+new Date());
            while (true) {// 不停的等待客户端的链接
                try {
                    Socket socket = serverSocket.accept();
                    if (clients.size() == max) {// 如果已达人数上限
                        BufferedReader r = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        PrintWriter w = new PrintWriter(socket
                                .getOutputStream());
                        // 接收客户端的基本用户信息
                        String inf = r.readLine();
                        StringTokenizer st = new StringTokenizer(inf, "#");
                        User user = new User(st.nextToken(), st.nextToken());
                        // 反馈连接成功信息
                        w.println("MAX#服务器：对不起，" + user.getName()
                                + user.getIp() + "，服务器在线人数已达上限，请稍后尝试连接！");
                        w.flush();
                        // 释放资源
                        r.close();
                        w.close();
                        socket.close();
                        continue;
                    }
                    ClientThread client = new ClientThread(socket);
                    client.start();// 开启对此客户端服务的线程
                    client.getUser().setState(1);//在线状态
                    clients.add(client);
                    listModel.addElement(client.getUser().getName());// 更新在线列表
                    contentArea.append(client.getUser().getName()
                            + client.getUser().getIp() + "上线!\r\n");
                    user_name_update();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 为一个客户端服务的线程
     */
    class ClientThread extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private User user;
        @SuppressWarnings("deprecation")
        public void run() {// 不断接收客户端的消息，进行处理。
            String message = null;
            while (true) {
                try {
                    message = reader.readLine();// 接收客户端消息
                    System.out.println(message);
                    StringTokenizer stringTokenizer = new StringTokenizer(
                            message, "#/");
                    String command = stringTokenizer.nextToken();// 命令
                    if (command.equals("CLOSE")) {// 下线命令
                        contentArea.append(this.getUser().getName()
                                + this.getUser().getIp() + "下线!\r\n");
                        // 断开连接释放资源
                        user_offLine(this.getUser().getName());
                        this.getUser().setState(0);
                        reader.close();
                        writer.close();
                        socket.close();

                        user_name_update();//更新用户状态


                        //反馈用户状态
                        String liststr = "";
                        for (int j = 1; j < all_listModel.size(); j++) {
                            liststr += all_listModel.get(j) + "#";
                        }
                        // 向所有在线用户发送该用户上线命令
                        for (int j = clients.size()-1 ; j >= 0; j--) {
                            clients.get(j).getWriter().println(
                                    "USERLIST#" + all_listModel.size() + "#" + liststr);
                            clients.get(j).getWriter().flush();
                        }
                        //user_name_update();//更新用户状态

                        listModel.removeElement(user.getName());// 更新在线列表

                        // 删除此条客户端服务线程
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            if (clients.get(i).getUser() == user) {
                                ClientThread temp = clients.get(i);
                                clients.remove(i);// 删除此用户的服务线程
                                temp.stop();// 停止这条服务线程
                                return;
                            }
                        }
                    }
                    else if (command.equals("USERLOGIN")) {//登录
                        String c_username = stringTokenizer.nextToken();
                        String c_password = stringTokenizer.nextToken();
                        int serverPort = Integer.parseInt(stringTokenizer.nextToken());
                        //String myIP = stringTokenizer.nextToken();
                        String myIP = this.getUser().getIp();//这个IP
                        //System.out.println(c_username.toString()+","+c_password.toString()+","+myIP.toString());

                        int i = user_login(c_username, c_password, serverPort, myIP);

                        System.out.println("[ user_login ]:i="+i);

                        if (1 == i) {
                            //System.out.println("user_name_update : "+new Date());
                            user_name_update();

                            //发送个人离线消息和群组离线消息
                            String msg = get_and_send_message(c_username);

                            //反馈用户状态
                            String temp_names_with_stat = "";   //实质包含name和state
                            String temp_names_with_id = "";   //实质包含name和群组
                            for (int j = 1; j < all_listModel.size(); j++) {
                                String temp_username = all_listModel.get(j).toString();
                                temp_names_with_stat += temp_username + "#";

                                StringTokenizer tmpToken = new StringTokenizer(temp_username, "---()");
                                String only_name = tmpToken.nextToken();
                                temp_names_with_id += (only_name + "#" + username_to_id.get(only_name) + "#");
                            }
                            // 向所有在线用户发送该用户上线命令,注意是 “所有用户”，emmm可优化
                            for (int j = clients.size()-1 ; j >= 0; j--) {
                                clients.get(j).getWriter().println(
                                        "USERLIST#" + all_listModel.size() + "#" + temp_names_with_stat);
                                clients.get(j).getWriter().flush();
                            }
                            // 向所有在线用户发送该用户对应的id信息，emmm可优化
                            for (int j = clients.size()-1 ; j >= 0; j--) {
                                clients.get(j).getWriter().println(
                                        "USER_ID_LIST#" + all_listModel.size() + "#" + temp_names_with_id);
                                clients.get(j).getWriter().flush();
                            }
                            //反馈用户的群组名称和gid
                            //System.out.println("group info  1: "+new Date());
                            try {
                                Connection con = null; //定义一个MYSQL链接对象
                                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                                Statement stmt; //创建声明
                                stmt = con.createStatement();
                                String selectSql ="select * from user where username='"+c_username+"';";
                                //String selectSql = "SELECT * FROM user WHERE username ="+c_username + "";;
                                ResultSet selectRes = stmt.executeQuery(selectSql);
                                //获取组ids和组数量
                                selectRes.next();
                                int groupnum = selectRes.getInt("G_num");
                                int groupnext = selectRes.getInt("G_next");
                                String groupids = selectRes.getString("GIDs");
                                //System.out.println("groupids = "+groupids);
                                //继续查询组名
                                try {
                                    Connection g_con = null; //定义一个MYSQL链接对象
                                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                                    g_con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                                    Statement g_stmt; //创建声明
                                    g_stmt = g_con.createStatement();

                                    StringTokenizer strToken = new StringTokenizer(
                                            groupids, "#");
                                    String g_infos = "";

                                    while(strToken.hasMoreTokens()){
                                        //System.out.println("g = "+g);
                                        int ss_gid = Integer.parseInt(strToken.nextToken());
                                        //System.out.println("ss_gid = "+ss_gid);
                                        String g_selectSql = "select * from cgroups where GID = '" + ss_gid +"';";
                                        //System.out.print("aaa");
                                        ResultSet g_selectRes = g_stmt.executeQuery(g_selectSql);
                                        g_selectRes.next();

                                        int groupid = g_selectRes.getInt("GID");
                                        String groupname = g_selectRes.getString("Gname");
                                        //System.out.println("groupid = "+groupid + " ,groupname"+groupname);
                                        if (ss_gid == groupid) {
                                            g_infos += "#" + ss_gid +"#"+groupname;
                                            //System.out.println(g_infos);
                                        }
                                    }
                                    System.out.println("[ USERLOGIN ] g_infos = "+g_infos);
                                    //向该用户发送他的群组信息
                                    for (int j = clients.size()-1 ; j >= 0; j--) {
                                        if(clients.get(j).getUser().getName().equals(c_username)){
                                            clients.get(j).getWriter().println(
                                                    "GROUPLIST#" + groupnum +"#" + groupnext + g_infos);
                                            clients.get(j).getWriter().flush();
                                            break; //####12_13_qlh
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.print("[MYSQL ERROR In Do Login]find groups' name:" + e.getMessage());
                                }
                            } catch (Exception e) {
                                System.out.print("MYSQL ERROR:" + e.getMessage());
                            }

                            System.out.println("[GROUP INIT OK] "+new Date());
                        }
                        else if (2 == i) {
                            writer.println("USERLOGIN#ALREADY");
                            writer.flush();
                        }
                        else {
                            writer.println("USERLOGIN#NO");
                            writer.flush();
                        }

                        //user_name_update();
                        //System.out.println("user_name_update : "+new Date());
                    }
                    else if (command.equals("USERZHUCE")) {//注册
                        String username = stringTokenizer.nextToken();
                        String password = stringTokenizer.nextToken();
                        String mail = stringTokenizer.nextToken();
                        int i = user_register(username, password, mail);
                        //System.out.println("\nAfter register # # # return = \n"+i);
                        if (1 == i) {
                            writer.println("USERZHUCE#OK");
                            writer.flush();
                            contentArea.append("有新用户注册！     用户名：" + username + "\r\n");
                            user_name_update();//更新用户状态
                        } else if (i == 2) {
                            writer.println("USERZHUCE#exict");
                            writer.flush();
                        } else {
                            writer.println("USERZHUCE#NO");
                            writer.flush();
                        }
                    }
                    else if (command.equals("USERFORGET")) {//找回密码
                        String username = stringTokenizer.nextToken();
                        String mail = stringTokenizer.nextToken();
                        String new_password = stringTokenizer.nextToken();
                        int i = user_forget(username, mail, new_password);
                        if (1 == i) {
                            //JOptionPane.showMessageDialog(frame, "登陆成功!" );
                            writer.println("USERFORGET#OK");
                            writer.flush();
                            contentArea.append("   用户：" + username + "  修改密码！\r\n");
                        } else if (i == 2) {
                            writer.println("USERFORGET#YOUXIANG_WRONG");
                            writer.flush();
                        } else if (i == 3) {
                            writer.println("USERFORGET#NAME_NO_exict");
                            writer.flush();
                        } else {
                            writer.println("USERFORGET#NO");
                            writer.flush();
                        }
                    }
                    else if(command.equals("LIXIAN")) {//下线
                        String username_sent = stringTokenizer.nextToken();
                        String username_receive = stringTokenizer.nextToken();
                        String msg = stringTokenizer.nextToken();

                        send_messageTo_board(username_sent,username_receive,msg);
                        System.out.println("离线发送ok");
                    }
                    //可能待改
                    else if (command.equals("P2P")) {//点对点通信
                        String username = stringTokenizer.nextToken();
                        int u_port = get_user_serverPort(username);
                        String u_ip = get_user_serverIP(username);
                        if(u_port!=0){
                            writer.println("P2P#OK#"+username+"#"+u_port+"#"+u_ip);
                            writer.flush();
                        }
                        else{
                            writer.println("P2P#NO#"+username);
                            writer.flush();
                        }
                    }
                    else if(command.equals("FILE_P2P")){
                        System.out.println("in [FILE]");
                        //something to do with uploaded files
                        int portNumber = Integer.parseInt(stringTokenizer.nextToken());
                        String fileName = stringTokenizer.nextToken();
                        long fileSize = Long.parseLong(stringTokenizer.nextToken());
                        String ip = stringTokenizer.nextToken();
                        String Nickname = stringTokenizer.nextToken();
                        S_ReceiveFileThread receiveFile = new S_ReceiveFileThread(contentArea,frame,ip, portNumber, fileName, fileSize, Nickname);
                        receiveFile.start();
                        contentArea.append("从"+Nickname+"接受文件:"+fileName+",大小为:"+fileSize
                                +"ip:"+ip+"port:"+portNumber+"\r\n");
                    } //点对点传文件（这里是C/S的P2P）

                    else if(command.equals("CREATE_GROUP")){
                        //创建群组
                        int flag = create_group(message);
                        System.out.println("[CREATE_GROUP] flag= "+flag);
                        if(flag == 1){
                            stringTokenizer.nextToken();//组ID
                            stringTokenizer.nextToken();//组名
                            int c_userid = Integer.parseInt(stringTokenizer.nextToken());  //####12_12bug
                            for (int j = clients.size()-1 ; j >= 0; j--) {
                                if(clients.get(j).getUser().getName().equals(id_to_name.get(c_userid))){
                                    clients.get(j).getWriter().println(
                                            "CREATE_GROUP_OK");
                                    clients.get(j).getWriter().flush();
                                    break;
                                }
                            }
                        }
                    }
                    else if(command.equals("OPEN_GROUP")){
                        //###12_14
                        //待改: 返回客户端请求的群组数据
                        int c_gid = Integer.parseInt(stringTokenizer.nextToken());
                        String info = open_group_for_info(c_gid);
                        System.out.println("[OPEN_GROUP] info= " + info);
                        String c_username = stringTokenizer.nextToken();
                        //if(info != ""){//先便于测试，待改
                        writer.println("OPEN_GROUP_OK#" + info);
                        writer.flush();
                        System.out.println("Send OPEN_GROUP_OK");

                       /* for (int j = clients.size()-1 ; j >= 0; j--) {
                            if(clients.get(j).getUser().getName().equals(c_username)){
                                clients.get(j).getWriter().println(
                                        "OPEN_GROUP_OK#" + info);
                                clients.get(j).getWriter().flush();
                                System.out.println("Send OPEN_GROUP_OK");
                                break;
                            }
                        }*/
                        //}
                    }

                    else if(command.equals("FILE_G_ADD_NEW")){
                        int flag = add_new(message);
                        if(flag == 2){//如果是重名的文件，则不add, 而是push
                            push(message);
                        }
                        //结束上传新文件（其实还在上传中。。。线程）
                        else{
                            this.writer.println("FILE_UPLOAD_FINISH");
                            this.writer.flush();
                        }
                    }
                    else if(command.equals("FILE_G_PUSH")){
                        push(message);
                    }
                    else if(command.equals("FILE_G_DOWNLOAD")){
                        //###12_14
                        //待改: 服务端同意客户端下载的文件，开始并传送
                        int c_gid = Integer.parseInt(stringTokenizer.nextToken());;
                        String c_user_name = stringTokenizer.nextToken();
                        String fileName = stringTokenizer.nextToken();

                        StringTokenizer filenameTokenizer = new StringTokenizer(
                                fileName, ".");
                        String head = filenameTokenizer.nextToken();
                        String tail = filenameTokenizer.nextToken();
                        String path = "D:\\chat\\file\\" + c_gid +"\\"+ head + "_k." + tail;

                        for (int i = clients.size() - 1; i >= 0; i--) {
                            if(clients.get(i).getUser().getName().equals(c_user_name)){
                                //响应下载请求
                                S_SendFileThread sendFile = new S_SendFileThread(frame, clients.get(i).socket, "服务器", true, path);
                                sendFile.start();
                                //服务器text area提示
                                contentArea.append("服务器给" + c_user_name+ "发送了一个文件：" + fileName + "\r\n");
                            }
                            //client端提示
                            //clients.get(i).getWriter().println( c_user_name +"下载了一个文件(download in group)：" + fileName);
                            //clients.get(i).getWriter().flush();
                        }


                        try {//#&&&&&&&
                            Connection con = null; //定义一个MYSQL链接对象
                            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                            con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                            Statement stmt; //创建声明
                            stmt = con.createStatement();
                        } catch (Exception e) {
                            System.out.print("MYSQL ERROR:" + e.getMessage());
                        }


                    }
                    else if(command.equals("GROUP_MESSAGE")){
                        G_dispatcherMessage(message);// 转发消息
                    }
                    else {
                        dispatcherMessage(message);// 转发消息
                    }//初始连接消息

                }
                catch (java.net.SocketException e){

                    clients.remove(this);
                    return ;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    //应该是
                    clients.remove(this);
                    return ;
                }
            }
        }
        // 客户端线程的构造方法
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                // 接收客户端的基本用户信息
                String inf = reader.readLine();
                StringTokenizer st = new StringTokenizer(inf, "#");
                user = new User(st.nextToken(), st.nextToken());
                // 反馈连接成功信息
                writer.println(user.getName() + user.getIp() + "与服务器连接成功!");
                writer.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public BufferedReader getReader() {
            return reader;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public User getUser() {
            return user;
        }

        public int get_user_serverPort(String user_name){
            try {
                Connection con = null; //定义一个MYSQL链接对象
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                Statement stmt; //创建声明
                stmt = con.createStatement();

                //查询数据，不能有相同的用户名
                String selectSql = "SELECT * FROM user";
                ResultSet selectRes = stmt.executeQuery(selectSql);
                while (selectRes.next()) { //循环输出结果集
                    String username_db = selectRes.getString("username");
                    if (user_name.equals(username_db)) {
                        int serverPort = selectRes.getInt("serverPort");
                        return serverPort;
                    }
                }
            } catch (Exception e) {
                System.out.print("MYSQL ERROR:" + e.getMessage());
            }
            return 0;
        }

        public String get_user_serverIP(String user_name){
            try {
                Connection con = null; //定义一个MYSQL链接对象
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                Statement stmt; //创建声明
                stmt = con.createStatement();

                //查询数据，不能有相同的用户名
                String selectSql = "SELECT * FROM user";
                ResultSet selectRes = stmt.executeQuery(selectSql);
                while (selectRes.next()) { //循环输出结果集
                    String username_db = selectRes.getString("username");
                    if (user_name.equals(username_db)) {
                        String serverIP = selectRes.getString("ipAddress");
                        return serverIP;
                    }
                }
            } catch (Exception e) {
                System.out.print("MYSQL ERROR:" + e.getMessage());
            }
            return "";
        }

        //这里可以分别查询个人离线消息和群组离线消息
        //这里全都查，以‘$’分割
        //干脆直接在这发送好了
        public String get_and_send_message(String name) {
            try {
                Connection con = null; //定义一个MYSQL链接对象
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                Statement stmt; //创建声明
                stmt = con.createStatement();

                //查询用户离线消息
                String selectSql = "SELECT * FROM user where username = '" + name +"';" ;
                ResultSet selectRes = stmt.executeQuery(selectSql);
                selectRes.next();
                int Id = selectRes.getInt("Id");
                String message = selectRes.getString("message");
                String G_message = selectRes.getString("G_message");

                //用户离线消息的话可返回后立即清空
                String updateSql = "UPDATE user SET message = '' WHERE Id = " + Id + "";
                stmt.executeUpdate(updateSql);
                if (message == null || message.equals("")) {
                    writer.println("USERLOGIN#Message#");
                    writer.flush();
                }
                else {
                    writer.println("USERLOGIN#Message#" + message);
                    writer.flush();
                }

                //群组离线消息的话可返回后立即清空
                updateSql = "UPDATE user SET G_message = '' WHERE Id = " + Id + "";
                stmt.executeUpdate(updateSql);
                if (G_message == null || G_message.equals("")) {
                    writer.println("USERLOGIN#G_Message#");
                    writer.flush();
                }
                else {
                    writer.println("USERLOGIN#G_Message#" + G_message);
                    writer.flush();
                }
                System.out.println("[in get_and_send_message ] "+ message+ "$" +G_message);
                return  message + "$" + G_message;

            } catch (Exception e) {
                System.out.print("MYSQL ERROR:" + e.getMessage());
            }
            return "";
        }


        //转发群组信息
        public void G_dispatcherMessage(String message) {
            StringTokenizer stringTokenizer = new StringTokenizer(message, "#");
            stringTokenizer.nextToken();//去除首命令

            int c_gid = Integer.parseInt(stringTokenizer.nextToken());
            String u_sender = stringTokenizer.nextToken();
            String content = stringTokenizer.nextToken();

            String msg =  "GROUP_MESSAGE#"+ u_sender + "说：" + content;
            //找到群组成员
            try {
                Connection con = null; //定义一个MYSQL链接对象
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();//MYSQL驱动
                con = DriverManager.getConnection("jdbc:mysql://172.81.245.233:3306/demo", "root", "QLH765"); //链接本地MYSQL
                Statement stmt; //创建声明
                stmt = con.createStatement();
                String selectSql ="select * from cgroups where GID='"+c_gid+ "';";
                ResultSet selectRes = stmt.executeQuery(selectSql);
                selectRes.next();
                String all_members_string = selectRes.getString("member_ids");
                StringTokenizer membersTokenizer = new StringTokenizer(
                        all_members_string, "#");

                ArrayList<Integer> users_id = new ArrayList<Integer>();

                while(membersTokenizer.hasMoreTokens()){
                    int c_uid = Integer.parseInt(membersTokenizer.nextToken());
                    users_id.add(c_uid);
                    //System.out.println("c_user = " + id_to_name.get(c_uid));
                    boolean if_offline = true;
                    for(int i=0;i<clients.size();i++){
                        if(clients.get(i).getUser().getName().equals(id_to_name.get(c_uid))){
                            clients.get(i).getWriter().println(msg);
                            clients.get(i).getWriter().flush();
                            System.out.println("[G_dispatcherMessage] [send to " +c_uid + " in group ]");
                            if_offline = false;
                            break;
                        }
                    }

                    //若有用户离线状态，则保存为离线群组信息
                    if(if_offline){
                        G_send_messageTo_board(u_sender, id_to_name.get(c_uid) ,c_gid, content);
                        //保存离线的群组消息
                    }


                    //qqqqqqqqqqqqqq可优化，还有离线群组消息保存
                }
                //System.out.println("users_id = " + users_id);

            } catch (Exception e) {
                System.out.print("[Group DispatcherMessage] : MYSQL ERROR, " + e.getMessage());
                return ;
            }
        }

        // 转发消息
        public void dispatcherMessage(String message) {
            StringTokenizer stringTokenizer = new StringTokenizer(message, "#");
            String source = stringTokenizer.nextToken();
            String owner = stringTokenizer.nextToken();
            String content = stringTokenizer.nextToken();

            if (owner.equals("ALL")) {// 群发
                message = source + "说：" + content;
                contentArea.append(message + "\r\n");
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(message + "(多人发送)");
                    clients.get(i).getWriter().flush();
                }
            } else {
                for (int i = clients.size() - 1; i >= 0; i--) {
                    if (clients.get(i).user.getName().equals(owner)) {
                        clients.get(i).getWriter().println(owner + "  对你说: " + content);
                        clients.get(i).getWriter().flush();
                        //contentArea.append(owner+"  对    "+ clients.get(i).user.getName()+ "  说  :"+ content+"\r\n");
                    }
                    if (clients.get(i).user.getName().equals(source)) {
                        clients.get(i).getWriter().println("对   " + source + "  说: " + content);
                        clients.get(i).getWriter().flush();
                    }
                }
            }
        }
    }




}

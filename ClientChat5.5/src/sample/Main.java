package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.swing.*;


import java.awt.*;
import java.io.*;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    //*--------------loginStage----------------------*/
    /*
    private int xx;
    private int yy;
    private GridPane logPanle;
    private Stage loginStage;
    private Label label_username;
    private Label label_password;
    private TextField txt_login_name;
    private TextField txt_password;
    private TextField txt_login_ip;
    private TextField txt_login_port;
    private TextField txt_login_forget;
    private Button btn_submit;
    private Button btn_zhuce;
    private Button btn_forget_pass;
    private Scene loginscene;
    */
    private Stage LoginStage;
    private Pane logPanle;
    private Label label_username;
    private Label label_password;
    private TextField txt_login_name;
    private TextField txt_password;
    private HBox L_tophbox;
    private HBox L_hBox;
    private HBox L_bottomhbox;
    private VBox loginvbox;
    private Button btn_submit;
    private Button btn_zhuce;
    private Button btn_forget_pass;
    private Scene loginscene;


    TextField txt_login_ip = new TextField("127.0.0.1");  //server's port
    TextField txt_login_port = new TextField("6666");     //server's ip
    
    //*--------------MainStage----------------------*/
    private Button btn_cancel_create_group = new Button("取消");

    private Stage MainStage;

    private Pane sendPanel;

    private Button btn_send_file;
    //###############add
    private ToggleButton btn_create_group;
    private Button btn_open_group;

    private Pane rootPane;
    private ListView M_listView_user;
    private ListView M_listView_group;//#####add
    private ArrayList<String> M_userlist;
    private ArrayList<String> M_grouplist;
    private ObservableList<String> M_observableList_group;
    private ObservableList<String> M_observableList_user;



    private TextArea M_textSendArea;

    private TextField txt_port;
    private TextField txt_hostIp;
    private TextField txt_name;
    private Button btn_start;
    private Button btn_stop;
    private Button btn_send;
    private Button btn_confirm_create_group;
    private ToggleButton btn_change;
    private GridPane northPanel;
    private Pane southPanel;
    private Label port;
    private Label serverip;
    private Label name;
    //private Label chat;
    private TextArea chat;


    private VBox leftvbox;
    private VBox leftbottomvbox;
    private VBox lefttopvbox;
    private VBox users;
    private VBox rightvbox;
    private HBox groups;
    private VBox holduserscroll;
    private VBox holdgroupscroll;
    private HBox rightbottomhbox;
    private StackPane users_groups;

    private Image profile_picture;

    private ScrollPane users_Scroll;
    private ScrollPane groups_Scroll;
    private SplitPane leftScroll;
    private ScrollPane right_Scroll;

    private SplitPane centerSplit;
    private Scene mainscene;
    /*--------------------------------------------*/



    //*--------------groupStage----------------------*/
    private Stage groupStage;

    private VBox G_leftPane;
    private HBox G_infoHead;
    private TextField G_groupname;

    private ScrollPane G_projScroll;
    private ScrollPane G_userScroll;
    private VBox G_projPane;
    private VBox G_userPane;
    private Label G_projLabel;
    private Label G_userLabel;
    private ListView<String> G_listView_user;
    private ObservableList<String> G_observableList_user;
    private ObservableList<String> G_observableList_project;
    private ListView<String> G_listView_proj;
    
    private VBox G_rightPane;
    private BorderPane G_right_Head;
    private StackPane G_right_Body;
    private Button G_chat_bar, G_FILE_bar;
    private VBox G_msgPane;

    private VBox G_filePane;
    private AnchorPane G_fileHead;
    private Button btn_downloadAll;
    private Button btn_addNew;
    private ScrollPane G_fileBody;
    private VBox G_fileBox;
    private singleFilePane [] G_listView_file;
    private Pane textShowPane;
    private TextArea G_textContent_show;
    private VBox textSendPane;
    private TextArea G_textarea;
    private Button G_send;

    private HBox G_rootPane;
    private Scene G_scene;

    private ArrayList<String> G_userlist;
    private ArrayList<String> G_projlist;
    private ArrayList<String> G_fNameList;


    //----------------------其他信息-------------------------//
    private boolean isConnected = false;
    private int send_for_who;
    private int open_for_group= 0;
    private int server_port=0;
    private int chosen_GID;//#####add

    //记录所有用户: name<->id
    private HashMap<String,Integer> username_to_id;
    //记录用户的所有群组: gname<->gid
    private HashMap<String,Integer> G_name_to_id;//#####add
    private HashMap<Integer,String> G_id_to_name;//#####add
    private int my_new_group_next;//创建群组设置gid时用的。
    private String my_new_group_name;
    private int my_new_group_gid;

    private String G_chosen_file = "";



    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private Socket socketfor_p2p;
    private boolean isConnected_p2p = false;
    private ArrayList<ClientThread> clients;//客户线程数组

    private PrintWriter P2P_printWriter;//点对点服务的输出流
    private BufferedReader P2P_bufferReader;//点对点服务的输入流
    private MessageThread_P2P messageThread_for_p2p;// 负责接收p2p消息的线程
    private Map<String, Boolean> P2P_connected_user = new HashMap<String, Boolean>();

    private Socket socket; //对应服务器端的socket
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;// 负责接收消息的线程
    private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户
    private String myIP = "";//每一个客户端都有唯一的IP地址


    //---------测试用的-----------//
    JFrame frame = new JFrame("主页面");
    private JFrame G_frame =  new JFrame("群聊");

    public void start(Stage primaryStage) throws BindException{
        //serverStart(0);
        G_name_to_id = new HashMap<String,Integer>();
        G_id_to_name = new HashMap<Integer,String>();
        username_to_id = new HashMap<String,Integer>();

        LoginPage();
        MainPage();
        Q_GroupPage();
        setID();
        //groupStage.show();
        //MainStage.show();
    }

    public void setID() {
        /*----------------------------主页面---------------------------------*/
        btn_confirm_create_group.setId("confirm_creat_group_btn");
        btn_create_group.setId("btn_create_group");
        btn_change.setId("btn_change");

        /*--------------------------群聊页面-------------------------------*/
        G_leftPane.setId("G_leftPane");
        G_infoHead.setId("G_infoHead");
        G_projPane.setId("G_projPane");
        G_projScroll.setId("G_projScroll");
        G_listView_proj.setId("G_listView_proj");
        G_userPane.setId("G_userPane");
        G_userScroll.setId("G_userScroll");
        G_listView_user.setId("G_listView_user");
        G_rightPane.setId("G_rightPane");
        G_right_Body.setId("G_right_Body");
        textSendPane.setId("textSendPane");
        G_textarea.setId("G_textarea");
        G_send.setId("btn_send");
        textShowPane.setId("textShowPane");
        G_textContent_show.setId("G_textContent_show");
        G_filePane.setId("G_filePane");
        G_fileHead.setId("G_fileHead");
        G_fileBody.setId("G_fileBody");
        G_fileBox.setId("G_fileBox");
        G_right_Head.setId("G_right_Head");
        G_groupname.setId("G_groupname");
        G_rootPane.setId("G_rootPane");
    }

    public void LoginPage() {
        LoginStage = new Stage();
        LoginStage.setTitle("登陆窗口");


        logPanle = new Pane();


        L_tophbox=new HBox();
        L_tophbox.setStyle("-fx-background-color:#030c35"); //背景色
        L_tophbox.setPrefSize(450,93);
        L_tophbox.setPadding(new Insets(70, 12, 10, 80)); //节点到边缘的距离
        L_tophbox.setSpacing(10);
        label_username = new Label("用户名:");
        label_username.setPrefSize(100,20);
        txt_login_name = new TextField("Kenelm");
        //txt_login_name.setStyle("-fx-background-color: white"); //背景色
        L_tophbox.getChildren().addAll(label_username,txt_login_name);
        L_tophbox.setLayoutX(0);
        L_tophbox.setLayoutY(0);
        logPanle.getChildren().add(L_tophbox);


        L_hBox=new HBox();
        L_hBox.setStyle("-fx-background-color:#030c35"); //背景色
        L_hBox.setPrefSize(450,43);
        L_hBox.setPadding(new Insets(20, 12, 10, 80)); //节点到边缘的距离
        L_hBox.setSpacing(10);
        label_password = new Label("密 码:");
        label_password.setPrefSize(100,20);
        txt_password = new TextField("qlh765");
        //txt_password.setStyle("-fx-background-color: #17285c"); //背景色
        L_hBox.getChildren().addAll(label_password,txt_password);
        L_hBox.setLayoutX(0);
        L_hBox.setLayoutY(93);
        logPanle.getChildren().add(L_hBox);


        L_bottomhbox=new HBox();
        L_bottomhbox.setStyle("-fx-background-color:#030c35"); //背景色
        L_bottomhbox.setPrefSize(450,143);
        L_bottomhbox.setPadding(new Insets(20, 12, 10, 190)); //节点到边缘的距离
        L_bottomhbox.setSpacing(20);
        btn_submit = new Button("登陆");
        btn_submit.setPrefSize(70,20);
        btn_submit.setStyle("-fx-background-color: #17285c"); //背景色
        btn_zhuce = new Button("注册");
        btn_zhuce.setPrefSize(70,20);
        btn_zhuce.setStyle("-fx-background-color: #17285c"); //背景色
        L_bottomhbox.getChildren().addAll(btn_submit,btn_zhuce);
        L_bottomhbox.setLayoutX(0);
        L_bottomhbox.setLayoutY(136);
        logPanle.getChildren().add(L_bottomhbox);


        btn_submit.setOnAction((ActionEvent e) -> {
            try {
                serverStart(0);
            }
            catch(Exception ex){
                System.out.println("server start err: "+ ex);
            }
            int port;
            String message_name = txt_login_name.getText().trim();
            if (message_name == null || message_name.equals("")) {
                Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","用户名不能为空！",LoginStage);
                            }
                        });

                return;
            }
            String message_pw = txt_password.getText().trim();
            if (message_pw == null || message_pw.equals("")) {
                Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","密码不能为空！",LoginStage);
                            }
                        });

                return;
            }
            if (isConnected) {
                String message1 = txt_login_name.getText().trim();
                if (message1 == null || message1.equals("")) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","用户名不能为空！",LoginStage);
                            }
                        });
                    return;
                }
                String message2 = txt_password.getText().trim();
                if (message2 == null || message2.equals("")) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","密码不能为空！",LoginStage);
                            }
                        });
                    return;
                }
                sendMessage("USERLOGIN#" + message1 + "#" + message2 + "#" + server_port + "#" + myIP);
                return;
            }
            else {
                try {
                    try {
                        port = Integer.parseInt(txt_login_port.getText().trim());
                    } catch (NumberFormatException e2) {
                        throw new Exception("端口号不符合要求!端口为整数!");
                    }
                    String Ip_of_server = txt_login_ip.getText().trim();
                    String name = txt_login_name.getText().trim();
                    if (name.equals("") || Ip_of_server.equals("")) {
                        throw new Exception("姓名、服务器IP不能为空!");
                    }
                    boolean flag = connectServer(port, Ip_of_server, name);
                    if (flag == false) {
                        throw new Exception("与服务器连接失败!");
                    }
                    MainStage.setTitle(name);

                    sendMessage("USERLOGIN#" + message_name + "#" + message_pw + "#" + server_port + "#" + myIP);

                } catch (Exception exc) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.WARNING,"Warning",exc.getMessage(),LoginStage);
                            }
                        });
                    return;
                }
            }
        });

        btn_zhuce.setOnAction((ActionEvent e) -> {
            int port;
            String message_name = txt_login_name.getText().trim();
            if (message_name == null || message_name.equals("")) {
                Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","用户名不能为空！",LoginStage);
                            }
                        });
                return;
            }
            String message_pw = txt_password.getText().trim();
            if (message_pw == null || message_pw.equals("")) {
                Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","密码不能为空！",LoginStage);
                            }
                        });

                return;
            }
            String message_yx = "mail"; //txt_login_forget.getText().trim();
            /*
            if (message_yx == null || message_yx.equals("")) {
                Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","注册邮箱不能为空！",LoginStage);
                            }
                        });
                return;
            }
            */
            if (isConnected) {
                String message1 = txt_login_name.getText().trim();
                if (message1 == null || message1.equals("")) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","用户名不能为空！",LoginStage);
                            }
                        });


                    return;
                }
                String message2 = txt_password.getText().trim();
                if (message2 == null || message2.equals("")) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","密码不能为空！",LoginStage);
                            }
                        });
                    return;
                }

                String message3 = "mail";
                /*String message3 = txt_login_forget.getText().trim();
                if (message3 == null || message3.equals("")) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","注册邮箱不能为空！",LoginStage);
                            }
                        });
                }
                */
                sendMessage("USERZHUCE#"+message1+"#"+message2+"#"+message3);
                return;
            }
            else{
                try {
                    try {
                        port = Integer.parseInt(txt_login_port.getText().trim());
                    } catch (NumberFormatException e2) {
                        throw new Exception("端口号不符合要求!端口为整数!");
                    }
                    String hostIp = txt_login_ip.getText().trim();
                    String name = txt_login_name.getText().trim();
                    if (name.equals("") || hostIp.equals("")) {
                        throw new Exception("姓名、服务器IP不能为空!");
                    }
                    boolean flag = connectServer(port, hostIp, name);
                    if (flag == false) {
                        throw new Exception("与服务器连接失败!");
                    }
                    MainStage.setTitle(name);
                } catch (Exception exc) {
                    Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.WARNING,"Warning",exc.getMessage(),LoginStage);
                            }
                        });
                    //System.out.println(exc.getMessage());
                    //return;
                }
            }

            sendMessage("USERZHUCE#"+message_name+"#"+message_pw+"#"+message_yx);
        });


        LoginStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("监听到窗口关闭");
                if (isConnected) {
                    closeConnection();// 关闭连接
                    closeServer();//关闭服务程序
                }
                System.exit(0);// 退出程序
            }
        });

        loginscene = new Scene(logPanle,450,279);
        LoginStage.setScene(loginscene);
        loginscene.getStylesheets().add(getClass().getResource("./groupCSS.css").toExternalForm());

        LoginStage.setResizable(false);
        LoginStage.show();

    }


    //----------------------聊天主页面------------------------//

    public void MainPage() {
        MainStage=new Stage();
        rootPane = new Pane();


        lefttopvbox=new VBox();
        lefttopvbox.setStyle("-fx-background-color:#030c35"); //背景色
        lefttopvbox.setPrefSize(150,200);
        lefttopvbox.setPadding(new Insets(70, 12, 10, 12)); //节点到边缘的距离
        lefttopvbox.setSpacing(10);
        name=new Label("昵 称");
        name.setPrefSize(100,20);
        txt_name=new TextField();
        txt_name.setStyle("-fx-background-color: #324da6"); //背景色
        lefttopvbox.getChildren().addAll(name,txt_name);
        lefttopvbox.setLayoutX(0);
        lefttopvbox.setLayoutY(0);
        rootPane.getChildren().add(lefttopvbox);



        leftvbox=new VBox();
        leftvbox.setStyle("-fx-background-color: #030c35"); //背景色
        leftvbox.setPrefSize(150,293);
        leftvbox.setPadding(new Insets(25,10,10,10)); //内边距
        leftvbox.setSpacing(10); //节点之间的间距
        port=new Label("端 口");
        serverip=new Label(("服 务 器 I P"));
        port.setPrefSize(100,20);
        serverip.setPrefSize(100,20);
        txt_port=new TextField();
        txt_port.setStyle("-fx-background-color: #324da6"); //背景色
        txt_hostIp=new TextField();
        txt_hostIp.setStyle("-fx-background-color: #324da6"); //背景色

        leftvbox.getChildren().addAll(serverip,txt_hostIp,port,txt_port);
        leftvbox.setLayoutX(0);
        leftvbox.setLayoutY(200);
        rootPane.getChildren().add(leftvbox);

        leftbottomvbox=new VBox();
        leftbottomvbox.setStyle("-fx-background-color: #030c35"); //背景色
        leftbottomvbox.setPrefSize(150,125);
        leftbottomvbox.setPadding(new Insets(35, 10, 10, 10)); //节点到边缘的距离
        leftbottomvbox.setSpacing(10); //节点之间的间距
        btn_start=new Button("连 接");
        btn_stop=new Button("断 开");
        //btn_start.setStyle("-fx-background-color: #324da6"); //背景色
        //btn_stop.setStyle("-fx-background-color: #324da6"); //背景色
        btn_start.setPrefSize(130,20);
        btn_stop.setPrefSize(130,20);
        leftbottomvbox.getChildren().addAll(btn_start,btn_stop);
        leftbottomvbox.setLayoutX(0);
        leftbottomvbox.setLayoutY(493);
        rootPane.getChildren().add(leftbottomvbox);



        groups=new HBox();
        groups.setStyle("-fx-background-color: #0a1340"); //背景色
        groups.setPrefSize(160,40);
        groups.setPadding(new Insets(10, 10, 10, 10)); //节点到边缘的距离
        groups.setSpacing(30); //节点之间的间距
        btn_create_group=new ToggleButton("创 建 群 聊");
        btn_change=new ToggleButton(("待 改"));
        btn_create_group.setId("btn_create_group");
        btn_change.setId("btn_change");
        //btn_create_group.setStyle("-fx-background-color: #324da6"); //背景色
        //btn_change.setStyle("-fx-background-color: #324da6"); //背景色
        btn_create_group.setPrefSize(100,20);
        btn_change.setPrefSize(20,20);

        Callback<ListView<String>,ListCell<String>> call= CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String s) {
                System.out.println("[call]  s"+s+" , ");
                return null;
            }
        });

        final ToggleGroup creat_Group = new ToggleGroup();
        btn_create_group.setToggleGroup(creat_Group);
        creat_Group.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov,
                 Toggle toggle, Toggle new_toggle) -> {
                    if (new_toggle == null)
                    {
                        M_listView_user.setCellFactory(null);
                        btn_confirm_create_group.setVisible(false);
                        btn_cancel_create_group.setVisible(false);

                    }
                    else
                    {
                        M_listView_user.setEditable(true);
                        //M_listView_user.setCellFactory((M_listView_user) -> new myCheckBoxListCell());
                        M_listView_user.setCellFactory(call);

                        btn_confirm_create_group.setVisible(true);

                        btn_confirm_create_group.setOnAction((ActionEvent e) -> {
                            btn_confirm_create_group.setVisible(false);
                            M_listView_user.setCellFactory(null);
                            new_toggle.selectedProperty().setValue(false);

                            //记录选中 的 成员
                            //M_listView_user.getCellFactory().call("bb");
                            /*不对
                            System.out.println( M_listView_user.getCellFactory().call("bb").toString());
                            System.out.println( M_listView_user.getCellFactory().call("cc").toString());
                            System.out.println("!");*/
                            create_group();

                        });

                        btn_cancel_create_group.setVisible(true);
                        btn_cancel_create_group.setOnAction((ActionEvent e) -> {
                            btn_confirm_create_group.setVisible(false);
                            btn_cancel_create_group.setVisible(false);
                            M_listView_user.setCellFactory(null);
                            new_toggle.selectedProperty().setValue(false);

                        });
                    }
                });//单击创建群聊键后显示复选框，再次单击关闭复选框

        final ToggleGroup Group = new ToggleGroup();
        btn_change.setToggleGroup(Group);
        Group.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov,
                 Toggle toggle, Toggle new_toggle) -> {
                    if (new_toggle == null)
                    {
                        holduserscroll.setVisible(true);
                        holdgroupscroll.setVisible(false);
                    }
                    else
                    {
                        holduserscroll.setVisible(false);
                        holdgroupscroll.setVisible(true);

                    }
                });
        groups.getChildren().addAll(btn_create_group,btn_change);



        users=new VBox();
        users.setStyle("-fx-background-color: #0a1340"); //背景色
        users.setPrefSize(300,618);
        users.setPadding(new Insets(10, 10, 10, 10)); //节点到边缘的距离
        users.setSpacing(10); //节点之间的间距
        users.setLayoutX(150);
        users.setLayoutY(0);




        M_listView_group = new ListView<>();
        M_listView_group.setStyle("-fx-background-color: #0a1340"); //背景色
        M_listView_group.setPrefSize(300,618);
        //M_listView_group.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        M_grouplist = new ArrayList<String>();
        M_observableList_group = FXCollections.observableArrayList(M_grouplist);
        M_listView_group.setItems(M_observableList_group);
        M_listView_group.setCellFactory((M_listView_group) -> new group_Cell());

        /*my_groupsList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val,
                 String new_val) -> {
                    label.setText(new_val);
                    label.setTextFill(Color.web(new_val));
                });*/


        M_listView_user = new ListView<>();
        M_listView_user.setStyle("-fx-background-color: #0a1340"); //背景色
        M_listView_user.setPrefSize(300,618);
        //M_listView_user.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        M_userlist = new ArrayList<String>();
        M_observableList_user = FXCollections.observableArrayList(M_userlist);
        M_listView_user.setItems(M_observableList_user);

        /*M_listView_user.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val,
                 String new_val) -> {
                    isConnected_p2p=false;
                });*/



        users_Scroll=new ScrollPane();
        users_Scroll.setStyle("-fx-background-color: #0a1340"); //背景色
        users_Scroll.setPrefSize(300,500);
        users_Scroll.setLayoutX(150);
        users_Scroll.setLayoutY(0);
        users_Scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        users_Scroll.setPannable(true); //允许通过鼠标滚动来滚动
        users_Scroll.setFitToWidth(true); //将内容宽度设置为视口宽度
        users_Scroll.setContent(M_listView_user);

        holduserscroll=new VBox();
        holduserscroll.setPrefSize(300,618);
        holduserscroll.setLayoutX(150);
        holduserscroll.setLayoutY(0);
        holduserscroll.getChildren().add(users_Scroll);



        groups_Scroll=new ScrollPane();
        groups_Scroll.setStyle("-fx-background-color: #0a1340"); //背景色
        groups_Scroll.setPrefSize(300,500);
        groups_Scroll.setLayoutX(150);
        groups_Scroll.setLayoutY(0);
        groups_Scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        groups_Scroll.setPannable(true); //允许通过鼠标滚动来滚动
        groups_Scroll.setFitToWidth(true); //将内容宽度设置为视口宽度
        groups_Scroll.setContent(M_listView_group);

        holdgroupscroll=new VBox();
        holdgroupscroll.setPrefSize(300,618);
        holdgroupscroll.setLayoutX(150);
        holdgroupscroll.setLayoutY(0);
        holdgroupscroll.getChildren().add(groups_Scroll);

        users_groups=new StackPane();
        users_groups.setStyle("-fx-background-color: #0a1340"); //背景色
        users_groups.setPrefSize(300,618);
        users_groups.setLayoutX(150);
        users_groups.setLayoutY(0);
        holduserscroll.setVisible(true);
        holdgroupscroll.setVisible(false);
        users_groups.getChildren().addAll(holdgroupscroll,holduserscroll);

        btn_confirm_create_group=new Button("确认");
        btn_confirm_create_group.setLayoutX(400);
        btn_confirm_create_group.setLayoutY(500);
        holduserscroll.getChildren().add(btn_confirm_create_group);
        btn_confirm_create_group.setVisible(false);
        btn_confirm_create_group.setId("confirm_creat_group_btn");
        //创建群聊确认按钮，默认设为不可见

        users.getChildren().addAll(groups,users_groups);
        rootPane.getChildren().add(users);

        rightvbox=new VBox();
        rightvbox.setStyle("-fx-background-color: #030c35"); //背景色
        rightvbox.setPrefSize(550,618);
        rightvbox.setPadding(new Insets(35, 10, 10, 10)); //节点到边缘的距离
        rightvbox.setSpacing(10); //节点之间的间距
        rightvbox.setLayoutX(450);
        rightvbox.setLayoutY(0);


        right_Scroll=new ScrollPane();
        right_Scroll.setStyle("-fx-background-color: #030c35"); //背景色
        right_Scroll.setPrefSize(550,593);
        right_Scroll.setLayoutX(450);
        right_Scroll.setLayoutY(0);
        chat=new TextArea();
        chat.setPrefSize(550,593);
        chat.setStyle("-fx-background-color: #030c35");
        right_Scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        right_Scroll.setPannable(true); //允许通过鼠标滚动来滚动
        right_Scroll.setFitToWidth(true); //将内容宽度设置为视口宽度
        right_Scroll.setContent(chat);

        rightbottomhbox=new HBox();
        rightbottomhbox.setStyle("-fx-background-color: #030c35"); //背景色
        rightbottomhbox.setPrefSize(550,25);
        rightbottomhbox.setPadding(new Insets(5, 5, 5, 5)); //节点到边缘的距离
        rightbottomhbox.setSpacing(10); //节点之间的间距
        rightbottomhbox.setLayoutX(450);
        rightbottomhbox.setLayoutY(593);

        M_textSendArea=new TextArea();
        M_textSendArea.setStyle("-fx-background-color: #324da6"); //背景色
        M_textSendArea.setPrefSize(400,25);
        btn_send=new Button("发 送");
        //btn_send.setStyle("-fx-background-color: #324da6"); //背景色
        btn_send.setPrefSize(50,30);
        btn_send_file=new Button("文 件");
        //btn_send_file.setStyle("-fx-background-color: #324da6"); //背景色
        btn_send_file.setPrefSize(50,30);
        rightbottomhbox.getChildren().addAll(M_textSendArea,btn_send,btn_send_file);



        rightvbox.getChildren().addAll(right_Scroll,rightbottomhbox);
        rootPane.getChildren().add(rightvbox);



        btn_send_file.setOnAction((ActionEvent e) -> {
            sendFile();
        });
        btn_send.setOnAction((ActionEvent e) -> {
            send();
        });


        /*btn_open_group.setOnAction((ActionEvent e) -> {
            open_for_group = M_listView_group.getSelectionModel().getSelectedIndex();
            //@_@
            String groupName = M_listView_group.getSelectionModel().getSelectedItem().toString();
            //getElementAt(open_for_group).toString();
            System.out.println("choose the groupName : "+groupName);
            chosen_GID = G_name_to_id.get(groupName);
            open_group();
        });*/
        /*btn_create_group.setOnAction((ActionEvent e) -> {
            System.out.println("click [btn_create_group]");
            //create_group();
        });*/

        MainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("监听到窗口关闭");
                try {
                    boolean flag = closeConnection();   // 断开连接
                    closeServer();
                    if (flag == false) {
                        throw new Exception("断开连接发生异常！");
                    }
                    else{
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                //更新JavaFX的主线程的代码放在此处
                                if(f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","成功断开！",MainStage)){
                                    MainStage.hide();
                                    LoginStage.show();
                                }
                            }
                        });
                    }
                    //listModel.removeAllElements();
                    //my_group_listModel.removeAllElements(); //###12_13
                } catch (Exception exc) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            f_alertt_confirmDialog(Alert.AlertType.ERROR,"Error",exc.getMessage(),MainStage);
                        }
                    });
                }

                //System.exit(0);// 退出程序
            }
        });

        btn_stop.setOnAction((ActionEvent e) -> {
            if (!isConnected) {
                    /*JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    */
                return;
            }
            try {
                boolean flag = closeConnection();// 断开连接
                closeServer();
                if (flag == false) {
                    throw new Exception("断开连接发生异常！");
                }
                else{
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","成功断开！",MainStage)){
                                MainStage.hide();
                                LoginStage.show();
                            }
                        }
                    });
                }
                //listModel.removeAllElements();
                //my_group_listModel.removeAllElements(); //###12_13

            }catch (Exception exc) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误",exc.getMessage(),MainStage);
                    }
                });

            }
        });

        mainscene= new Scene(rootPane,1000,618);
        //mainscene.getStylesheets().add("D:\\计网class project\\UI\\src\\sample\\mainCSS.css");
        mainscene.getStylesheets().add(getClass().getResource("./groupCSS.css").toExternalForm());
        MainStage.setScene(mainscene);
        MainStage.setResizable(false);
        //frame.getIcons().add(new Image("/icon.png"));

    }

    class group_Cell extends ListCell<String> {
        //
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            //rect = new Rectangle(100, 20);
            if (item != null) {
                //rect.setFill(Color.RED);
                //setGraphic(rect);
                setText(item);
            }

            this.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {

                    if (me.getClickCount() == 2) {
                        System.out.println("Mouse pressed: [group_Cell]");
                        open_for_group = M_listView_group.getSelectionModel().getSelectedIndex();
                        //@_@
                        String groupName = M_listView_group.getSelectionModel().getSelectedItem().toString();
                        //getElementAt(open_for_group).toString();
                        System.out.println("choose the groupName : "+groupName);
                        chosen_GID = G_name_to_id.get(groupName);
                        open_group();
                    }
                }
            });
        }
    }

    HashMap<String,Boolean> user_to_chosen = new HashMap<String, Boolean>();
    class myCheckBoxListCell extends CheckBoxListCell<String> {
        //

        public void updateItem(String item, boolean empty){
            super.updateItem(item, empty);

            this.setOnMousePressed(new EventHandler<MouseEvent>() {
                /*StringTokenizer st = new StringTokenizer(item, "---()");
                String user_name = st.nextToken();
                String user_state = st.nextToken();*/
                public void handle(MouseEvent me) {
                   /* boolean fg  = user_to_chosen.get(user_name);
                    user_to_chosen.put(user_name, !fg);*/
                    System.out.println("click @");
                }
            });
            //user_to_chosen.put(user_name,empty);
            /*ObservableValue<Boolean> xx = this.getSelectedStateCallback().call(this.getItem());
            System.out.println("xx = "+xx);*/
        }

    }

    class user_Cell extends ListCell<String> {
        //
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            //rect = new Rectangle(100, 20);
            if (item != null) {
                //rect.setFill(Color.RED);
                //setGraphic(rect);
                setText(item);
            }

            this.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    System.out.println("Mouse pressed: [user_Cell]");
                    if (me.getClickCount() == 2) {

                    }
                }
            });
        }
    }

    private final Desktop desktop = Desktop.getDesktop();
    //发送文件给选定的用户 （或者 给服务器，可能待改）
    public void sendFile() {
        FileChooser fileChooser = new FileChooser();
        //configureFileChooser(fileChooser);
        File file = fileChooser.showOpenDialog(MainStage);

        if (file != null) {
            EventQueue.invokeLater(() -> {
                try {
                    //服务器text area提示
                    chat.appendText("发送文件：" + file.getName() + "\r\n");
                    send_for_who = M_listView_user.getSelectionModel().getSelectedIndex();
                    System.out.println("【send_for_who】 = "+send_for_who);
                    if(send_for_who==0){
                        chat.appendText("对服务器发送文件!");
                        //##################add
                        SendFileThread2 sendFile = new SendFileThread2(MainStage, socket, MainStage.getTitle(), file);
                        sendFile.start();
                        //client端提示
                        chat.appendText("给  服务器  上传一个文件（用于后续群聊）：" + file.getName() + "\r\n");
                    }
                    else{
                        //@_@
                        StringTokenizer st = new StringTokenizer(M_listView_user.getItems().get(send_for_who)+"", "---()");
                        String user_name = st.nextToken();
                        String user_state = st.nextToken();
                        if (user_state.equals("在线")) {
                            //client端提示
                            //先看对方是不是已经连接我了（成为我的客户了），是的话就直接通信，并结束函数
                            //my add!!!!!
                            for (int i = clients.size()-1; i >= 0; i--) {
                                if (clients.get(i).getUser().getName().equals(user_name)) {

                                    chat.appendText("给  "+user_name+"  发送一个文件：" + file.getName() + "\r\n");
                                    SendFileThread2 sendFile = new SendFileThread2(MainStage, clients.get(i).socket, MainStage.getTitle(), file);
                                    sendFile.start();

                                    return;//很灵性
                                }
                            }
                            if (!isConnected_p2p) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","点对点即将连接！",MainStage);
                                        sendMessage("P2P#"+user_name);
                                    }
                                });
                            }
                            else{
                                chat.appendText("给  "+user_name+"  发送一个文件：" + file.getName() + "\r\n");
                                SendFileThread2 sendFile = new SendFileThread2(MainStage, socketfor_p2p, MainStage.getTitle(), file);
                                sendFile.start();
                            }

                        }
                        else{

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    f_alertt_confirmDialog(Alert.AlertType.ERROR,"信息","用户不在线，不能发送文件！",MainStage);
                                }
                            });

                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Main.
                            class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            });
        }


        //OLD: 文件选择对话框启动，当选择了文件以后client发送文件

        /*JFileChooser sourceFileChooser = new JFileChooser(".");
        sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int status = sourceFileChooser.showOpenDialog(frame);
        File sourceFile = new File(sourceFileChooser.getSelectedFile().getPath());*/



    }

    //执行文本信息
    public void send() {
        if (!isConnected) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    f_alertt_confirmDialog(Alert.AlertType.ERROR,"信息","还没有连接服务器，无法发送消息！",MainStage);
                }
            });
            return;
        }
        String message = M_textSendArea.getText().trim();
        if (message == null || message.equals("")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","消息不能为空！",MainStage);
                }
            });
            return;
        }
        //sendMessage(MainStage.getTitle() + "#" + "ALL" + "#" + message);
        send_for_who = M_listView_user.getSelectionModel().getSelectedIndex();

        System.out.println("【send_for_who】 = "+send_for_who);
        if(send_for_who==0){
            sendMessage(MainStage.getTitle() + "#" + "ALL" + "#" + message);
            M_textSendArea.setText("");
        }
        else{
            StringTokenizer st = new StringTokenizer(M_listView_user.getItems().get(send_for_who)+"", "---()");
            String user_name = st.nextToken();
            String user_state = st.nextToken();
            if (user_state.equals("在线")) {
                //先看对方是不是已经连接我了（成为我的客户了），是的话就直接通信，并结束函数
                for (int i = clients.size()-1; i >= 0; i--) {
                    if (clients.get(i).getUser().getName().equals(user_name)) {
                        clients.get(i).writer_ptp.println("对 "+user_name+"  说：  "+message+"\r\n");
                        clients.get(i).writer_ptp.flush();
                        chat.appendText("对  "+user_name+"  说： "+message+"\r\n");
                        M_textSendArea.setText("");
                        return;//很灵性
                    }
                }
                //否则先连接
                if (!isConnected_p2p) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","点对点即将连接！",MainStage);
                            sendMessage("P2P#"+user_name);
                        }
                    });
                }
                else{
                    P2P_printWriter.println(message);
                    P2P_printWriter.flush();
                    chat.appendText("对  "+user_name+"  说： "+message+"\r\n");
                    M_textSendArea.setText("");
                }

            }
            else{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","用户不在线，已存为留言！",MainStage);
                    }
                });
                sendMessage("LIXIAN#"+MainStage.getTitle() + "#" + user_name + "#" + message);
                chat.appendText("对  "+user_name+"  留言： "+message+"\r\n");
                M_textSendArea.setText("");
            }
        }
    }

    //创建群聊 --->tell server
    public void create_group() {
        //nonooooooooooooooooooooooooo
        /*int num = M_listView_user.getSelectionModel().getSelectedIndices().size();

        for(int i = 0;i<M_userlist.size();i++){
            String username = M_userlist.get(i);

            System.out.println( M_listView_user.getCellFactory().call(username));
            System.out.println("!");
            //item.getSelectedStateCallback();
        }
        List<Integer> indexs = M_listView_user.getSelectionModel().getSelectedIndices();*/
        int num = 3;
        List<Integer> indexs = new ArrayList<Integer>();
        indexs.add(1);indexs.add(2);indexs.add(3);indexs.add(4);


        String msg = "#CREATE_GROUP";
        my_new_group_gid = username_to_id.get(MainStage.getTitle())*100 + my_new_group_next;
        my_new_group_next++;//用户自己更新一个my_new_group_next值，因为初值是login时才赋的
        msg += "#" + my_new_group_gid;
        //msg组名还有有让用户输入，后面要加上！！！！！！！！！！！
        //暂时用GID代替组名
        my_new_group_name = "" + my_new_group_gid;
        msg += "#" + my_new_group_name;

        //msg需要创建者id
        msg += ("#" + username_to_id.get(MainStage.getTitle()));
        for(int i=0; i<num; i++) {
            //System.out.print(" " + indexs[i]);
            StringTokenizer st = new StringTokenizer(M_listView_user.getItems().get(indexs.get(i))+"", "---()");
            String u_name = st.nextToken();
            int u_id = username_to_id.get(u_name);
            msg += "#"+u_id;
        }
        System.out.println("msg:"+msg);
        StringTokenizer strToke = new StringTokenizer(msg, "#");
        /*while(strToke.hasMoreTokens()){
            System.out.println("a split msg:"+strToke.nextToken());
        }//测试输出*/ //###12_12

        sendMessage(msg);
    }


    //打开群聊 --->tell server
    public void open_group() {
        //#####12_14
        String msg = "#OPEN_GROUP" + "#" + chosen_GID +  "#" + MainStage.getTitle() ;
        //@@@@告诉服务器要打开那个群组
        sendMessage(msg);
    }


    //----------------------点对点连接（我作为客户端）-----------------------------------//

    /**
     * 连接p2p
     *
     * @param port
     * @param hostIp
     * @param name
     */
    public boolean connectServer_p2p(int port, String hostIp, String name) {
        // 连接服务器
        try {
            socketfor_p2p = new Socket(hostIp, port);// 根据端口号和服务器ip建立连接
            P2P_printWriter = new PrintWriter(socketfor_p2p.getOutputStream());
            P2P_bufferReader = new BufferedReader(new InputStreamReader(socketfor_p2p
                    .getInputStream()));
            System.out.println("[connectServer_p2p] port = "+port+", ip = "+hostIp);
            messageThread_for_p2p = new MessageThread_P2P(P2P_bufferReader);
            messageThread_for_p2p.start();
            P2P_connected_user.put(name,true);
            isConnected_p2p = true;// 已经连接上了
            return true;
        }
        catch (Exception e) {
            chat.appendText("与端口号为：" + port + "    IP地址为：" + hostIp
                    + "   的服务连接失败!" + "\r\n Excepption:"+e.getMessage());
            isConnected_p2p = false;// 未连接上
            System.out.println("[connectServer_p2p] why: "+ e.getMessage());
            return false;

        }
    }

    // 不断接收p2p消息的线程
    class MessageThread_P2P extends Thread {
        private BufferedReader reader_ptp;

        // 接收消息线程的构造方法
        public MessageThread_P2P(BufferedReader reader) {
            this.reader_ptp = reader;

        }

        // 被动的关闭连接
        public synchronized void closeCon() throws Exception {
            System.out.println("close :*************");
            // 被动的关闭连接释放资源
            if (reader_ptp != null) {
                reader_ptp.close();
            }
            if (P2P_printWriter != null) {
                P2P_printWriter.close();
            }
            if (socketfor_p2p != null) {
                socketfor_p2p.close();
            }
            isConnected_p2p = false;// 修改状态为断开

        }

        public void run() {
            //String message = "";
            while (true) {
                try {
                    String message = reader_ptp.readLine();
                    StringTokenizer stringTokenizer = new StringTokenizer(
                            message, "#/");
                    String command = "";
                    if(stringTokenizer.hasMoreTokens()) {
                        command = stringTokenizer.nextToken();// 命令
                    }
                    if (command.equals("CLOSE"))// 服务器已关闭命令
                    {
                        String user = stringTokenizer.nextToken();
                        chat.appendText("用户 "+user+"  已下线，p2p服务已关闭!\r\n");
                        closeCon();// 被动的关闭连接
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"错误","用户 "+user+"  已下线，p2p服务已关闭!",MainStage);
                            }
                        });
                        return;// 结束线程
                    }
                    else if (command.equals("FILE_P2P")) {
                        int portNumber = Integer.parseInt(stringTokenizer.nextToken());
                        String fileName = stringTokenizer.nextToken();
                        long fileSize = Long.parseLong(stringTokenizer.nextToken());
                        String ip = stringTokenizer.nextToken();
                        String Nickname = stringTokenizer.nextToken();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                boolean permit = f_alertt_confirmDialog(Alert.AlertType.INFORMATION, "接受文件:"+fileName+" 从 "+Nickname+"?", "文件传输请求：",MainStage);
                                System.out.println("permit = "+permit);
                                if(permit) {
                                    DirectoryChooser directoryChooser = new DirectoryChooser();
                                    File selectedDirectory = directoryChooser.showDialog(MainStage);
                                    C_ReceiveFileThread receiveFile = new C_ReceiveFileThread(chat, MainStage, ip, portNumber, fileName, fileSize, Nickname, permit,selectedDirectory);
                                    receiveFile.start();
                                    chat.appendText("从 " + Nickname + " 接受文件:" + fileName + ",大小为:" + fileSize
                                            + "ip:" + ip + "port:" + portNumber + "\r\n");
                                }
                            }
                        });
                    }
                    else {// 普通消息

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                chat.appendText(""+message + "\r\n");
                            }
                        });

                    }

                    System.out.println("[MessageThread_P2P] : get message: "+ message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //----------------------点对点连接（我作为服务端）-----------------------------------//

    //为另一个主动链接的客户端提供服务的线程
    class ClientThread extends Thread {
        private Socket socket;
        private BufferedReader reader_ptp;
        private PrintWriter writer_ptp;
        private User user;

        public BufferedReader getReader() {
            return reader_ptp;
        }

        public PrintWriter getWriter() {
            return writer_ptp;
        }

        public User getUser() {
            return user;
        }

        // 客户端线程的构造方法
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader_ptp = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
                writer_ptp = new PrintWriter(socket.getOutputStream());

                // 接收客户端的基本用户信息
                String inf = reader_ptp.readLine();
                StringTokenizer st = new StringTokenizer(inf, "#");
                user = new User(st.nextToken(), socket.getLocalAddress().toString());
                // 反馈连接成功信息
                writer_ptp.println(frame.getTitle()+"  对你说：  "+user.getName()+"/"+user.getIp()+"你好！"+"你与我"+frame.getTitle()+"建立链接成功！");
                writer_ptp.flush();
//                // 反馈当前在线用户信息
//                if (clients.size() > 0) {
//                    String temp = "";
//                    for (int i = clients.size() - 1; i >= 0; i--) {
//                        temp += (clients.get(i).getUser().getName() + "/" + clients
//                                .get(i).getUser().getIp())
//                                + "#";
//                    }
//                    writer.println("USERLIST#" + clients.size() + "#" + temp);
//                    writer.flush();
//                }
//                // 向所有在线用户发送该用户上线命令
//                for (int i = clients.size() - 1; i >= 0; i--) {
//                    clients.get(i).getWriter().println(
//                            "ADD#" + user.getName() + user.getIp());
//                    clients.get(i).getWriter().flush();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("deprecation")
        public void run() {// 不断接收客户端的消息，进行处理。
            System.out.println("[ClientThread] port="+socket.getPort());
            while (true) {
                try {
                    String message = reader_ptp.readLine();// 接收客户端消息
                    StringTokenizer stringTokenizer = new StringTokenizer(message,"/#");
                    String command = stringTokenizer.nextToken();
                    if (command.equals("CLOSE"))// 下线命令
                    {
                        chat.appendText("与"+this.getUser().getName()
                                + this.getUser().getIp() + "建立连接成功!\r\n");
                        // 断开连接释放资源
                        this.getUser().setState(0);
                        reader.close();
                        writer.close();
                        socket.close();

                    }
                    else if (command.equals("FILE_P2P")) {
                        int portNumber = Integer.parseInt(stringTokenizer.nextToken());
                        String fileName = stringTokenizer.nextToken();
                        long fileSize = Long.parseLong(stringTokenizer.nextToken());
                        String ip = stringTokenizer.nextToken();
                        String Nickname = stringTokenizer.nextToken();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                boolean permit = f_alertt_confirmDialog(Alert.AlertType.INFORMATION, "接受文件:"+fileName+" 从 "+Nickname+"?", "文件传输请求：",MainStage);
                                if(permit) {
                                    DirectoryChooser directoryChooser = new DirectoryChooser();
                                    File selectedDirectory = directoryChooser.showDialog(MainStage);
                                    C_ReceiveFileThread receiveFile = new C_ReceiveFileThread(chat, MainStage, ip, portNumber, fileName, fileSize, Nickname, permit,selectedDirectory);
                                    receiveFile.start();
                                    chat.appendText("从 " + Nickname + " 接受文件 :" + fileName + ",大小为:  " + fileSize
                                            + "   ip: " + ip + "    port:" + portNumber + "\r\n");
                                }
                            }
                        });
                    }
                    else {
                        chat.appendText(user.getName()+"  对你说： "+message+"\r\n");
                    }
                    System.out.println("[ClientThread] get!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //服务线程
    class ServerThread extends Thread {
        private ServerSocket serverSocket;

        // 服务器线程的构造方法
        public ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            System.out.println("[ServerThread] port="+serverSocket.getLocalPort());
            while (true) {// 不停的等待客户端的链接
                try {
                    Socket socket = serverSocket.accept();
                    ClientThread client = new ClientThread(socket);
                    client.start();// 开启对此客户端服务的线程
                    clients.add(client);
                    chat.appendText("有新用户p2p链接\r\n");
//                    user_name_update();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //启动服务器（p2p）
    public void serverStart(int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serverSocket = new ServerSocket(port);
            System.out.println("[serverStart] port = "+ port+", serverport = " + serverSocket.getLocalPort());
            serverThread = new ServerThread(serverSocket);
            serverThread.start();

            server_port = serverSocket.getLocalPort();

            InetAddress addr = InetAddress.getLocalHost();
            myIP = addr.getHostAddress();//获得本机IP
            //若在本主机上运行不知道为什么是127.0.0.1
            System.out.println("mmyIP=="+myIP+"\r\n");
        } catch (BindException e) {
            throw new BindException("端口号已被占用，请换一个！");
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new BindException("启动服务器异常！");
        }
    }

    /**
     * 关闭服务
     */
    @SuppressWarnings("deprecation")
    public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();// 停止服务器线程

            for (int i = clients.size() - 1; i >= 0; i--) {
                // 给所有在线用户发送关闭命令
                clients.get(i).getWriter().println("CLOSE#"+MainStage.getTitle());
                clients.get(i).getWriter().flush();
                // 释放资源
                clients.get(i).stop();// 停止此条为客户端服务的线程
                clients.get(i).reader_ptp.close();
                clients.get(i).writer_ptp.close();
                clients.get(i).socket.close();
                clients.remove(i);
            }
            if (serverSocket != null) {
                serverSocket.close();// 关闭服务器端连接
            }
            //M_listView_user.getItems().removeAll();
            //listModel.removeAllElements();// 清空用户列表
            //isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
//            isStart = true;
        }
    }



    //----------------------------群组页面-----------------------------------------//



    String send_for_who_name;
    private void GroupPage_Left(){
        //----------------------左侧列表（组名、成员、项目）----------------------------//
        G_userlist = new ArrayList<String>();

        G_observableList_user = FXCollections.observableList(G_userlist);
        G_listView_user = new ListView<>();
        G_listView_user.setItems(G_observableList_user);

        //#######css
        G_userScroll = new ScrollPane(G_listView_user);
        G_userScroll.getStyleClass().add("scroll-pane-glist");
        G_userLabel = new Label("Members");
        G_userLabel.getStyleClass().add("list-label");
        //G_userPane.setPrefSize(115, 200);
        G_userScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        G_userScroll.setPannable(true); //允许通过鼠标滚动来滚动
        G_userScroll.setFitToWidth(true); //将内容宽度设置为视口宽度
        //G_userPane.setFitToHeight(true); //将内容宽度设置为视口宽度
        G_userPane = new VBox(G_userLabel, G_userScroll);

        //工作区列表
        G_projlist = new ArrayList<String>();
        G_observableList_project = FXCollections.observableList(G_projlist);
        G_listView_proj = new ListView<>();
        G_listView_proj.setItems(G_observableList_project);
        //#######css
        G_projLabel = new Label("Projects");
        G_projLabel.getStyleClass().add("list-label");

        G_projScroll = new ScrollPane(G_listView_proj);
        G_projScroll.getStyleClass().add("scroll-pane-glist");
        //G_projPane.setPrefSize(115, 200);
        G_projScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        G_projScroll.setPannable(true); //允许通过鼠标滚动来滚动
        G_projScroll.setFitToWidth(true); //将内容宽度设置为视口宽度
        //G_projPane.setFitToHeight(true);

        G_projPane = new VBox(G_projLabel, G_projScroll);
        //群组信息头
        G_groupname = new TextField("新群组XXX");
        G_groupname.setEditable(true);
        G_groupname.setId("G_groupname");
        G_infoHead = new HBox(G_groupname);
        G_infoHead.setId("G_infoHead");

        //选择按钮
        Button Guser_btn = new Button("成员");
        Button Gproj_btn = new Button("工作区");
        Guser_btn.setOnAction((ActionEvent e) -> {
            G_userPane.setVisible(true);
            G_projPane.setVisible(false);
        });
        Gproj_btn.setOnAction((ActionEvent e) -> {
            G_projPane.setVisible(true);
            G_userPane.setVisible(false);
        });
        BorderPane G_btnPane = new BorderPane();
        G_btnPane.setLeft(Gproj_btn);
        G_btnPane.setRight(Guser_btn);

        //###pane
        G_leftPane = new VBox();
        G_leftPane.getChildren().add(G_infoHead);
        G_leftPane.getChildren().add(G_projPane);
        G_leftPane.getChildren().add(G_userPane);
    }
    private void GroupPage_Right(){
        //----------------------------------文件区和消息区------------------------------------//
        G_chat_bar = new Button("Chat");
        G_FILE_bar = new Button("Files");

        G_chat_bar.getStyleClass().add("right_tar");
        G_FILE_bar.getStyleClass().add("right_tar");

        G_right_Head = new BorderPane();
        G_right_Head.setId("G_right_Head");
        G_right_Head.setLeft(G_chat_bar);
        G_right_Head.setRight(G_FILE_bar);



        //文件区
        G_fileBox = new VBox();
        G_fileBox.setPrefSize(550,450);
        G_fNameList = new ArrayList<String>();


        updateFileBox();

        G_fileBody = new ScrollPane();
        G_fileBody.setContent(G_fileBox);
        G_fileBody.setFitToWidth(true); //将内容宽度设置为视口宽度
        G_fileBody.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        G_fileBody.setPannable(true); //允许通过鼠标滚动来滚动
        G_fileBody.setId("fileBody");
        //G_fileBody.setPrefSize(120, 280);
        //G_userPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        //G_userPane.setPannable(true); //允许通过鼠标滚动来滚动
        //G_fileBody.setFitToHeight(true); //将内容宽度设置为视口宽度

        btn_downloadAll = new Button("下载全部");
        btn_addNew = new Button("上传新文件");
        //btn_downloadAll.widthProperty().bind((IntegerProperty)G_fileHead.widthProperty().divide(2));
        //btn_addNew.prefWidthProperty().bind(G_fileHead.widthProperty().divide(2));
        btn_downloadAll.getStyleClass().addAll("btn_tar","right_tar");
        btn_addNew.getStyleClass().addAll("btn_tar","right_tar");

        G_fileHead =  new AnchorPane();
        G_fileHead.getChildren().addAll(btn_downloadAll,btn_addNew);
        AnchorPane.setTopAnchor(btn_downloadAll, 5.0);
        AnchorPane.setRightAnchor(btn_downloadAll, 10.0);
        AnchorPane.setTopAnchor(btn_addNew, 5.0);
        AnchorPane.setRightAnchor(btn_addNew, 140.0);
        G_fileHead.setId("G_fileHead");

        G_filePane = new VBox(G_fileHead, G_fileBody);
        G_filePane.setId("G_filePane");
        //G_filePane.setPrefSize(120, 300);

        //G_userPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //从不显示垂直ScrollBar
        //G_userPane.setPannable(true); //允许通过鼠标滚动来滚动


        //消息显示区
        G_textContent_show = new TextArea("群聊消息"); // 创建一个多行输入框
        //G_textContent_show.setMaxHeight(200); // 设置多行输入框的最大高度
        G_textContent_show.setPrefSize(550, 430);
        G_textContent_show.setEditable(false);
        G_textContent_show.setId("G_textContent_show");
        textShowPane = new Pane(G_textContent_show);
        textShowPane.setId("textShowPane");

        textSendPane = new VBox(); // 创建一个水平箱子
        //textSendPane.setPrefSize(250, 100); // 设置水平箱子的推荐宽高

        G_textarea = new TextArea(); // 创建一个多行输入框
        //G_textarea.setMaxHeight(50); // 设置多行输入框的最大高度
        //area.setMaxWidth(300); // 设置多行输入框的最大宽度
        G_textarea.setPrefSize(550, 80); // 设置多行输入框的推荐宽高
        G_textarea.setEditable(true); // 设置多行输入框能否编辑
        G_textarea.setPromptText("请输入消息"); // 设置多行输入框的提示语
        G_textarea.setWrapText(true); // 设置多行输入框是否支持自动换行。true表示支持，false表示不支持
        G_textarea.setPrefColumnCount(15); // 设置多行输入框的推荐列数
        G_textarea.setPrefRowCount(6); // 设置多行输入框的推荐行数


        G_send = new Button("Send");
        G_send.setId("btn_send");
        AnchorPane anchorpane = new AnchorPane();
        anchorpane.getChildren().add(G_send);    //添加来自例1-5 的GridPane
        AnchorPane.setBottomAnchor(G_send, 20.0);
        AnchorPane.setRightAnchor(G_send, 20.0);

        textSendPane.getChildren().addAll(G_textarea, anchorpane); // 给水平箱子添加一个多行输入框
        textSendPane.setId("textSendPane");


        G_msgPane = new VBox();
        G_msgPane.getChildren().addAll(textShowPane, textSendPane);

        G_right_Body = new StackPane();
        G_filePane.setVisible(false);//一开始显示群聊信息面板
        G_right_Body.getChildren().addAll(G_filePane, G_msgPane);
        G_right_Body.setId("G_right_Body");

        G_rightPane = new VBox();
        G_rightPane.getChildren().addAll(G_right_Head, G_right_Body);
        //G_rightPane.setAlignment(Pos.TOP_CENTER);


        G_chat_bar.setOnAction((ActionEvent e) -> {
            G_msgPane.setVisible(true);
            G_filePane.setVisible(false);
        });
        G_FILE_bar.setOnAction((ActionEvent e) -> {
            G_filePane.setVisible(true);
            G_msgPane.setVisible(false);
        });

        btn_downloadAll.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                System.out.println("Mouse pressed: [下载全部]");
            }
        });
        btn_addNew.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                System.out.println("Mouse pressed: [上传新文件]");
                G_upload();

            }
        });

        G_send.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                System.out.println("Mouse pressed: [发送信息]");
                G_send();
                //G_listView_user.getItems().remove("bb");
                //G_listView_user.getItems().add("user new");
            }
        });

    }
    public void Q_GroupPage() {
        groupStage = new Stage();
        groupStage.setTitle("群组窗口");

        GroupPage_Left();
        GroupPage_Right();
        G_leftPane.setId("left-pane");
        G_rightPane.setId("right-pane");
        G_rootPane =  new HBox();
        G_rootPane.getChildren().addAll(G_leftPane, G_rightPane);
        G_rootPane.getStyleClass().add("root");



        groupStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("监听到窗口关闭");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //更新JavaFX的主线程的代码放在此处
                        groupStage.hide();
                        MainStage.show();
                    }
                });
            }
        });


        G_scene = new Scene(G_rootPane);
        G_scene.getStylesheets().add(getClass().getResource("./groupCSS.css").toExternalForm());
        groupStage.setScene(G_scene);
        //groupStage.show();
    }


    class singleFilePane extends GridPane{
        Label G_btn_download;
        Label G_btn_push;
        Label fnameLabel;
        ImageView image1;
        ImageView image2;
        public singleFilePane(String filename_with_info){
            super();
            image1 = new ImageView(new Image(getClass().getResourceAsStream("../pic/pull.png")));
            image1.setFitHeight(25);
            image1.setFitWidth(25);
            G_btn_download = new Label("Pull", image1);

            image2 = new ImageView(new Image(getClass().getResourceAsStream("../pic/push.png")));
            G_btn_push = new Label("Push", image2);
            image2.setFitHeight(25);
            image2.setFitWidth(25);


            //待以后为文件版本管理显示信息
            StringTokenizer stringTokenizer = new StringTokenizer(
                    filename_with_info, "/");
            String name = stringTokenizer.nextToken();
            if(stringTokenizer.hasMoreTokens()){
                String new_num = stringTokenizer.nextToken();
            }

            fnameLabel = new Label(name);
            Rectangle pic_rec = new Rectangle(100,20);
            pic_rec.setFill(Color.RED);
            fnameLabel.setGraphic(pic_rec);
            this.setHgap(50);
            this.add(fnameLabel,1,0);
            this.add(G_btn_push,2,0);
            this.add(G_btn_download,3,0);

            this.image1.getStyleClass().add("file-img");
            this.image2.getStyleClass().add("file-img");
            this.getStyleClass().add("file-cell");
            this.G_btn_download.getStyleClass().add("file-pull");
            this.G_btn_push.getStyleClass().add("file-push");


            G_btn_download.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    System.out.println("Mouse pressed: [PULL]");
                    G_chosen_file = fnameLabel.getText();
                    G_download();
                }
            });

            G_btn_push.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    System.out.println("Mouse pressed: [PUSH]");
                    G_chosen_file = fnameLabel.getText();
                    G_push();

                }
            });

        }

    }

    //-------------------------群聊页面------------------------//
    // 自定义列表中已经实现
    // class G_file_SelecTry implements ListSelectionListener {}
    //####12_14
    public void group_init(String msg){
        //G_listView_proj.setItems(null);
        //G_projlist.clear();
        //G_observableList_proj.removeAll();
        StringTokenizer stringTokenizer = new StringTokenizer(
                msg, "#");
        stringTokenizer.nextToken();

        String groupname = stringTokenizer.nextToken();
        groupStage.setTitle(groupname);
        G_groupname.setText(groupname);
        int usernum = Integer.parseInt(stringTokenizer.nextToken());
        G_userlist.clear();
        while (usernum>0) {
            G_userlist.add(stringTokenizer.nextToken());
            usernum--;
        }

        G_fNameList.clear();
        System.out.println("   after clear:    G_fNameList = " + G_fNameList.size());
        int filenum =  Integer.parseInt(stringTokenizer.nextToken());

        int count = filenum;
        while (count>0){
            stringTokenizer.nextToken();//文件ID
            count--;
        }
        count = filenum;
        while (count>0){
            G_fNameList.add(stringTokenizer.nextToken());
            count--;
        }

        System.out.println("before updateFileBox G_fNameList num = " + G_fNameList.size());
        //更新文件列表和用户列表
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateFileBox();

                G_listView_user.getItems().removeAll();
                G_observableList_user = FXCollections.observableList(G_userlist);
                G_listView_user.setItems(G_observableList_user);

                ///f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"消息","打开成功!", LoginStage);
                groupStage.show();
                MainStage.hide();
            }
        });

    }

    public boolean updateFileBox(){
        //更新文件列表
        int filenum = G_fNameList.size();
        //G_fileBox = new VBox();
        //G_fileBox.getChildren().removeAll();
        G_fileBox.getChildren().clear();
        System.out.println("filenum  =  " + filenum +", G_fileBox.getChildren().size = "+ G_fileBox.getChildren().size());

        G_listView_file = new singleFilePane[filenum];
        for(int i =0;i<filenum;i++){
            G_listView_file[i] = new singleFilePane(G_fNameList.get(i));
            System.out.println("[updateFileBox ] "+ G_fNameList.get(i));
            G_fileBox.getChildren().add(G_listView_file[i]);
        }
        return true;
    }

    public boolean f_alertt_confirmDialog(Alert.AlertType alertType,String p_header,String p_message, Stage stage){
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
        _alert.getDialogPane().setId("_alert");
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

    //群组发消息
    public void G_send(){
        //待改！！！！！
        String message = G_textarea.getText();
        if (message == null || message.equals("")) {
            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","消息不能为空！",groupStage);
                            }
                        });
            return;
        }

        sendMessage("GROUP_MESSAGE#"  + chosen_GID + "#" + MainStage.getTitle() + "#" + message);
        G_textarea.setText("");
    }

    //有待开发一键上传整个文件夹的功能（或整个项目）@@@@@@@@12_19
    /*
    public boolean uploadDirectory(String localDirectory,
                                   String remoteDirectoryPath) {
        File src = new File(localDirectory);
        try {
            remoteDirectoryPath = remoteDirectoryPath + src.getName() + "/";
            boolean makeDirFlag = this.ftpClient.makeDirectory(remoteDirectoryPath);
            System.out.println("localDirectory : " + localDirectory);
            System.out.println("remoteDirectoryPath : " + remoteDirectoryPath);
            System.out.println("src.getName() : " + src.getName());
            System.out.println("remoteDirectoryPath : " + remoteDirectoryPath);
            System.out.println("makeDirFlag : " + makeDirFlag);
            // ftpClient.listDirectories();
        }catch (IOException e) {
            e.printStackTrace();
            logger.info(remoteDirectoryPath + "目录创建失败");
        }
        File[] allFile = src.listFiles();
        for (int currentFile = 0;currentFile < allFile.length;currentFile++) {
            if (!allFile[currentFile].isDirectory()) {
                String srcName = allFile[currentFile].getPath().toString();
                uploadFile(new File(srcName), remoteDirectoryPath);
            }
        }
        for (int currentFile = 0;currentFile < allFile.length;currentFile++) {
            if (allFile[currentFile].isDirectory()) {
                // 递归
                uploadDirectory(allFile[currentFile].getPath().toString(),
                        remoteDirectoryPath);
            }
        }
        return true;
    }
    */

    File file_to_upload;
    public void G_upload(){
        //待改！！！
        //文件选择对话框启动，当选择了文件以后给每一个client发送文件
        FileChooser fileChooser = new FileChooser();
        //configureFileChooser(fileChooser);
        file_to_upload = fileChooser.showOpenDialog(MainStage);

        if (file_to_upload != null) {
            EventQueue.invokeLater(() -> {
                try {
                    //服务器text area提示
                    chat.appendText("对服务器发送文件!"+ file_to_upload.getName() + "\r\n");
                    //##################add
                    SendFileThread2 sendFile = new SendFileThread2(MainStage, socket, MainStage.getTitle(),
                            file_to_upload, 1, chosen_GID);
                    sendFile.start();
                    //client端提示
                    G_textContent_show.appendText("【给  群共享  上传文件】" + file_to_upload.getName() + "\r\n");

                } catch (Exception ex) {
                    Logger.getLogger(Main.
                            class.getName()).
                            log(Level.SEVERE, null, ex);
                }

            });
        }
    }

    public void G_push(){
        //待改！！！
        //文件选择对话框启动，当选择了文件以后给每一个client发送文件
        FileChooser fileChooser = new FileChooser();
        //configureFileChooser(fileChooser);
        File file = fileChooser.showOpenDialog(MainStage);

        if (file != null) {
            EventQueue.invokeLater(() -> {
                try {
                    //服务器text area提示
                    chat.appendText("对服务器发送文件!"+ file.getName() + "\r\n");
                    //##################add
                    SendFileThread2 sendFile = new SendFileThread2(MainStage, socket, MainStage.getTitle(),
                            file, 2, chosen_GID);
                    sendFile.start();
                    //client端提示
                    G_textContent_show.appendText("【给  群共享  上传(push)文件】" + file.getName() + "\r\n");

                } catch (Exception ex) {
                    Logger.getLogger(Main.
                            class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            });
        }
    }
    public void G_download(){
        //待改！！！！！
        //测试

        String fileName = G_chosen_file;
        int GID = chosen_GID;
        String msg ="FILE_G_DOWNLOAD#"+"#"+ chosen_GID +"#"+ MainStage.getTitle() + "#" +fileName;
        sendMessage(msg);
    }
    
    
    /**双击事件监听
    **
    **
     //绑定事件源 EventHandler传入Event为MouseEvent
    button.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
    //重写EventHandler接口实现方法
    @Override
    public void handle(MouseEvent event) {
    //执行事件发生后的动作
    if(event.getClickCount() == 2){ //连续点击次数等于2 则执行下列操作

    System.out.println("鼠标双击事件");
    }
    }
    });
    * */


    
    //--------------------客户登录页面----------------------//


    //--------------------客户主页面----------------------//


    //--------------------客户群聊页面----------------------//
    
    /*------------------------------------------------------------------------------------*/
    
    //发送消息（向服务器发消息）
    public void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    // 不断接收消息的线程（服务器发来的消息）
    class MessageThread extends Thread {
        private BufferedReader reader;
        private TextArea textArea_x;

        // 接收消息线程的构造方法
        public MessageThread(BufferedReader reader, TextArea textArea) {
            this.reader = reader;
            this.textArea_x = textArea;
        }

        public void run() {
            String message = "";
            while (true) {
                try {
                    message = reader.readLine();
                            //更新JavaFX的主线程的代码放在此处
                    StringTokenizer stringTokenizer = new StringTokenizer(
                            message, "#/");
                    System.out.println("message = " + message);
                    String command = stringTokenizer.nextToken();// 命令
                    if (command.equals("CLOSE"))// 服务器已关闭命令
                    {
                        textArea_x.appendText("服务器已关闭!\r\n");
                        closeCon();// 被动的关闭连接
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","服务器已关闭！",MainStage);
                                //更新JavaFX的主线程的代码放在此处
                                MainStage.hide();
                                LoginStage.show();
                            }
                        });

                        return;// 结束线程
                    }
                    else if (command.equals("ADD")) {// 有用户上线更新在线列表
                        String username = "";
                        String userIp = "";
                        if ((username = stringTokenizer.nextToken()) != null
                                && (userIp = stringTokenizer.nextToken()) != null) {
                            User user = new User(username, userIp);
                            onLineUsers.put(username, user);

                            M_userlist.add(username);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    M_listView_user.getItems().removeAll();
                                    M_observableList_user = FXCollections.observableArrayList(M_userlist);
                                    M_listView_user.setItems(M_observableList_user);
                                    M_listView_user.getSelectionModel().selectFirst();
                                }
                            });

                        }
                    }
                    else if (command.equals("DELETE")) {// 有用户下线更新在线列表
                        String username = stringTokenizer.nextToken();
                        User user = (User) onLineUsers.get(username);
                        onLineUsers.remove(user);
                        M_listView_user.getItems().remove(username);
                        //listModel.removeElement(username);
                    }
                    else if (command.equals("USERLIST")) {
                        // 加载在线用户列表
                        M_userlist.clear();
                        M_userlist.add("全部用户");
                        StringTokenizer strToken ;
                        String user ;// 命令
                        int size = Integer.parseInt(stringTokenizer.nextToken());
                        String username = null;
                        String userIp = null;
                        for (int i = 0; i < size-1; i++) {
                            username = stringTokenizer.nextToken();
                            strToken = new StringTokenizer(username, "---()");
                            if (strToken.nextToken().equals(MainStage.getTitle())) {
                                continue;
                            }
                            else{
                                M_userlist.add(username);
                            }
                            //userIp = stringTokenizer.nextToken();
                            //User user = new User(username, userIp);
                            //onLineUsers.put(username, user);
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                M_listView_user.getItems().removeAll();
                                M_observableList_user = FXCollections.observableArrayList(M_userlist);
                                M_listView_user.setItems(M_observableList_user);
                                M_listView_user.getSelectionModel().selectFirst();
                            }
                        });
                    }
                    else if (command.equals("USER_ID_LIST")){
                        stringTokenizer.nextToken();//去掉用户个数！！！
                        while(stringTokenizer.hasMoreTokens()){
                            String name = stringTokenizer.nextToken();
                            int id = Integer.parseInt(stringTokenizer.nextToken());
                            //12_16
                            if (username_to_id.containsKey(name)) {
                                username_to_id.replace(name,id);//应该是要更新
                            }
                            else {
                                username_to_id.put(name, id);
                            }
                            //System.out.println("uname:"+name+"\tuid:"+id);
                        }
                    }
                    else if (command.equals("MAX")) {// 人数已达上限
                        textArea_x.appendText(stringTokenizer.nextToken()
                                + stringTokenizer.nextToken() + "\r\n");
                        closeCon();// 被动的关闭连接
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","服务器缓冲区已满！",MainStage);
                                MainStage.hide();
                                LoginStage.show();
                            }
                        });

                        return;// 结束线程
                    }
                    else if (command.equals("GROUPLIST")) {
                        // 加载在线用户列表
                        //System.out.println("【init GROUPLIST】");
                        //my_group_listModel.removeAllElements();
                        M_grouplist.clear();
                        StringTokenizer strToken;
                        int num = Integer
                                .parseInt(stringTokenizer.nextToken());
                        my_new_group_next = Integer
                                .parseInt(stringTokenizer.nextToken());
                        String groupname = null;
                        String groupid = null;
                        while(stringTokenizer.hasMoreTokens()) {
                            //####12_13
                            //for(int i = 0;i<num;i++){//mmp不知道为什么for有时候会错
                            groupid = stringTokenizer.nextToken();
                            groupname = stringTokenizer.nextToken();
                            //my_group_listModel.addElement(groupname);
                            M_grouplist.add(groupname);
                            //System.out.println("groupid:"+groupid+"\tgroupname:"+groupname);
                            G_name_to_id.put(groupname, Integer.parseInt(groupid));
                            G_id_to_name.put(Integer.parseInt(groupid), groupname);
                        }
                        System.out.println("[ G_id_to_name ]" + G_id_to_name);
                        //System.out.println(my_group_listModel.size());
                        //M_listView_group.setModel(my_group_listModel);


                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                M_listView_group.getItems().removeAll();
                                M_observableList_group = FXCollections.observableArrayList(M_grouplist);
                                M_listView_group.setItems(M_observableList_group);
                                M_listView_group.getSelectionModel().selectFirst();
                                txt_name.setText(txt_login_name.getText());
                                chat.setText("");
                                M_textSendArea.setText("");
                            }
                        });
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","登陆成功！",LoginStage);
                                LoginStage.hide();
                                MainStage.show();
                            }
                        });

                    }
                    //file : server to client
                    else if(command.equals("FILE_P2P")){
                        int portNumber = Integer.parseInt(stringTokenizer.nextToken());
                        String fileName = stringTokenizer.nextToken();
                        long fileSize = Long.parseLong(stringTokenizer.nextToken());
                        String ip = stringTokenizer.nextToken();
                        String Nickname = stringTokenizer.nextToken();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                boolean permit = f_alertt_confirmDialog(Alert.AlertType.INFORMATION, "接受文件:"+fileName+" 从 "+Nickname+"?", "文件传输请求：",MainStage);
                                if(permit){
                                    DirectoryChooser directoryChooser  = new DirectoryChooser();
                                    File selectedDirectory = directoryChooser.showDialog(MainStage);

                                    C_ReceiveFileThread receiveFile = new C_ReceiveFileThread(chat,MainStage,ip, portNumber, fileName, fileSize, Nickname,permit,selectedDirectory);
                                    receiveFile.start();
                                    chat.appendText("从"+Nickname+"接受文件:"+fileName+",大小为:"+fileSize
                                            +"ip:"+ip+"port:"+portNumber+"\r\n");
                                }

                            }
                        });
                    }

                    else if(command.equals("USERLOGIN")){
                        //info:  ok if stat = 1 ,and the msg other leave for you
                        String st = stringTokenizer.nextToken();
                        System.out.println("[USERLOGIN]:st = "+st);
                        if(st.equals("Message")){
                            //System.out.println("accept from server : login ok !!!");
                            int count = stringTokenizer.countTokens();
                            while(true){
                                if(count==0){
                                    break;
                                }
                                textArea_x.appendText(stringTokenizer.nextToken()+"  发言 ，");
                                textArea_x.appendText("时间： "+stringTokenizer.nextToken()+"\r\n   ");
                                textArea_x.appendText("留言内容： "+stringTokenizer.nextToken()+"\r\n");
                                count-=3;
                            }

                        }
                        else if(st.equals("G_Message")){
                            //System.out.println("accept from server : login ok !!!");
                            int count = stringTokenizer.countTokens();
                            System.out.println("[IN G_Message] count = "+count);
                            while(true){
                                if(count==0){
                                    break;
                                }
                                String gid = stringTokenizer.nextToken();
                                textArea_x.appendText("【" + G_id_to_name.get(gid) + "@ ");
                                textArea_x.appendText(stringTokenizer.nextToken()+"  发言 】");
                                textArea_x.appendText("时间： "+stringTokenizer.nextToken()+"\r\n   ");
                                textArea_x.appendText("内容： "+stringTokenizer.nextToken()+"\r\n");
                                count-=4;
                            }

                        }
                        else if(st.equals("ALREADY")){
                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"错误","账号已登陆！",LoginStage);
                            }
                        });
                        }
                        else{
                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","登陆失败！",LoginStage);
                            }
                        });
                        }
                    }
                    else if(command.equals("USERZHUCE")){
                        String st = stringTokenizer.nextToken();
                        if(st.equals("OK")){

                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","注册成功！",LoginStage);
                            }
                        });

                        }else if(st.equals("exict")){

                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","用户名已存在！",LoginStage);
                            }
                        });
                        }else{
                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","注册失败！",LoginStage);
                            }
                        });
                        }
                    }
                    else if(command.equals("USERFORGET")){
                        String st = stringTokenizer.nextToken();
                        if(st.equals("OK")){
                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","修改密码成功！",LoginStage);
                            }
                        });
                        }
                        else if(st.equals("YOUXIANG_WRONG")){

                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"错误","邮箱错误！",LoginStage);
                            }
                        });
                        }
                        else if(st.equals("NAME_NO_exict")){
                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"信息","用户不存在",LoginStage);
                            }
                        });

                        }
                        else{

                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"信息","找回密码失败",LoginStage);
                            }
                        });

                        }
                    }
                    else if (command.equals("P2P")) {
                        String st = stringTokenizer.nextToken();
                        if(st.equals("OK")){
                            String username = stringTokenizer.nextToken();
                            int serverPort = Integer.parseInt(stringTokenizer.nextToken());
                            String ip = stringTokenizer.nextToken();
                            boolean cn = connectServer_p2p(serverPort,ip,username);
                            if (cn) {

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"信息","与"+username+"的连接成功，端口号为："+serverPort+"IP:"+ip,MainStage);

                                        P2P_printWriter.println(MainStage.getTitle()+"#"+myIP);
                                        P2P_printWriter.flush();

                                        String msg = M_textSendArea.getText().trim();
                                        P2P_printWriter.println(msg);
                                        P2P_printWriter.flush();

                                        textArea_x.appendText("对  "+username+"  说： "+msg+"\r\n");

                                        M_textSendArea.setText(null);
                                    }
                                });

                            }

                        }else{
                            String username = stringTokenizer.nextToken();

                            Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                f_alertt_confirmDialog(Alert.AlertType.ERROR,"信息","与"+username+"的连接失败！",MainStage);
                            }
                        });

                        }
                    }
                    else if (command.equals("CREATE_GROUP_OK")){
                        //my_group_listModel.addElement(my_new_group_name);

                        G_name_to_id.put(my_new_group_name,my_new_group_gid);//用户自己更新群组值
                        G_id_to_name.put(my_new_group_gid, my_new_group_name);//用户自己更新群组值
                        M_grouplist.add(my_new_group_name);
                        System.out.println("accept from server : create group  ok !!!");

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                M_listView_group.getItems().removeAll();
                                M_observableList_group = FXCollections.observableArrayList(M_grouplist);
                                M_listView_group.setItems(M_observableList_group);

                                f_alertt_confirmDialog(Alert.AlertType.INFORMATION,"消息","创建成功!", LoginStage);
                            }
                        });
                        //something to do with group init;
                        //chosen_GID = my_new_group_gid;
                        //group_init(message);//###12_12bug####12_14不需要初始化。。
                    }
                    else if (command.equals("OPEN_GROUP_OK")){
                        //待改！！！：得到群组数据，显示信息
                        System.out.println("accept from server : open group ok !!!");
                        group_init(message);    //####12_14
                    }
                    //结束上传了
                    else if (command.equals("FILE_UPLOAD_FINISH")){
                        //待改！！！：上传文件成功
                        //实质上已经完成sendthread中已做了处理。
                        //更新文件列表和用户列表
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                G_fNameList.add(file_to_upload.getName());
                                updateFileBox();

                            }
                        });
                    }
                    //允许下载
                    else if (command.equals("FILE_DOWNLOAD_OK")){
                        //#&&&&&&&
                        //待改！！！：允许下载文件
                        int portNumber = Integer.parseInt(stringTokenizer.nextToken());
                        String fileName = stringTokenizer.nextToken();
                        long fileSize = Long.parseLong(stringTokenizer.nextToken());
                        String ip = stringTokenizer.nextToken();
                        String Nickname = stringTokenizer.nextToken();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                boolean permit = f_alertt_confirmDialog(Alert.AlertType.INFORMATION, "接受文件:"+fileName+" 从 "+Nickname+"?", "文件传输请求：",groupStage);
                                if(permit) {
                                    DirectoryChooser directoryChooser = new DirectoryChooser();
                                    File selectedDirectory = directoryChooser.showDialog(groupStage);

                                    C_ReceiveFileThread receiveFile = new C_ReceiveFileThread(G_textContent_show, groupStage, ip, portNumber, fileName, fileSize, Nickname, permit,selectedDirectory);
                                    receiveFile.start();
                                    G_textContent_show.appendText("从" + Nickname + "接受文件:" + fileName + ",大小为:" + fileSize
                                            + "ip:" + ip + "port:" + portNumber + "\r\n");

                                }
                            }
                        });
                    }
                    else if (command.equals("GROUP_MESSAGE")){
                        String msg = stringTokenizer.nextToken();// 命令
                        //System.out.println("[GROUP_MESSAGE] msg : " + msg);
                        G_textContent_show.appendText(msg + "\r\n");
                    }
                    else {// 普通消息
                        textArea_x.appendText(message + "\r\n");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return; //12_16######待改
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 被动的关闭连接
        public synchronized void closeCon() throws Exception {
            // 清空用户列表
            //listModel.removeAllElements();
            M_listView_user.getItems().removeAll();
            //my_group_listModel.removeAllElements();//###12_13
            M_listView_group.getItems().removeAll();

            // 被动的关闭连接释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;// 修改状态为断开

        }

    }



    //---------------------------登录页面----------------------//

    //连接服务器
    public boolean connectServer(int port, String Ip_of_server, String name) {
        // 连接服务器
        try {
            socket = new Socket(Ip_of_server, port);// 根据端口号和服务器ip建立连接
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            // 发送客户端用户基本信息(用户名和ip地址)
            //System.out.println("i am seeing Ip_of_server: "+ Ip_of_server);
            System.out.println("i am seeing "+ socket.getLocalAddress().toString());

            sendMessage(name + "#" + socket.getLocalAddress().toString());

            // 开启接收消息的线程
            messageThread = new MessageThread(reader, chat);
            messageThread.start();

            isConnected = true;// 已经连接上了
            return true;
        } catch (Exception e) {
            chat.appendText("与端口号为：" + port + "    IP地址为：" + Ip_of_server
                    + "   的服务器连接失败!" + "\r\n");
            isConnected = false;// 未连接上
            return false;
        }
    }

    /*---------------------------------------------------------------*/
    // 客户端主动关闭连接
    @SuppressWarnings("deprecation")
    public synchronized boolean closeConnection() {
        try {
            sendMessage("CLOSE");// 发送断开连接命令给服务器
            System.out.println("[closeConnection]");
            messageThread.stop();// 停止接受消息线程
            // 释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;
            //M_listView_user.getItems().removeAll();
            //listModel.removeAllElements();
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isConnected = true;
            return false;
        }
    }
    
    
    
    //-----------------------------//
    

}

//用户信息类
public class User{
    private String name;
    private String ip;
    private int state;

    public User(String name, String ip) {
        this.name = name;
        this.ip = ip;
        state = 0;
    }

    public String getName() {
        return name;
    }

    public void getIp(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}

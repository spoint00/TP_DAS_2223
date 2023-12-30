package isec.tp.das.onlinecompiler.util;

public class Message {
    String msg;
    Boolean bool;

    public Message(String msg, Boolean bool) {
        this.msg = msg;
        this.bool = bool;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getBool() {
        return bool;
    }

    public void setBool(Boolean bool) {
        this.bool = bool;
    }
}

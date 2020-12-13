package org.pmm.simpleim.db;


import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class IMDbDataBean extends LitePalSupport {
    @Column
    private int id;

    @Column
    private int msgid;
    @Column
    private String cardname;
    @Column
    private String message;
    @Column
    private String addtime;

    public int getId() {
        return id;
    }

    public int getMsgid() {
        return msgid;
    }

    public void setMsgid(int msgid) {
        this.msgid = msgid;
    }

    public String getCardname() {
        return cardname == null ? "" : cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAddtime() {
        return addtime == null ? "" : addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }
}

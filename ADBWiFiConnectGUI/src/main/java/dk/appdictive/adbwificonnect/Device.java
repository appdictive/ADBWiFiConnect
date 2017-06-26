package dk.appdictive.adbwificonnect;

import java.io.Serializable;

/**
 * Created by JesperLR on 05-11-2016.
 */
public class Device implements Serializable {

    public static int DEVICE_TYPE_USB = 0, DEVICE_TYPE_REMOTE = 1, DEVICE_TYPE_SAVED_REMOTE = 2, DEVICE_TYPE_OFFLINE = 3;

    public Device() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public String getSerialID() {
        return serialID;
    }

    public void setSerialID(String serialID) {
        this.serialID = serialID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    String name;
    String remoteIP;
    String serialID;
    int type;



}

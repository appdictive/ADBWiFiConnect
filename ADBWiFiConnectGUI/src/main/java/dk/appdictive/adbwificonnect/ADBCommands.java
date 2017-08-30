package dk.appdictive.adbwificonnect;

import javafx.application.Platform;
import javafx.scene.control.Button;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by JesperLR on 05-11-2016.
 */
public class ADBCommands {

    private static final String ADB_DEVICE_PORT = "5555";
    private static Logger log = Logger.getLogger(Main.class.getName());

    public static String runCommand(String command) {
        try
        {
            Process p=Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader=new BufferedReader(
                    new InputStreamReader(p.getInputStream())
            );
            String result = "";
            String line;
            while((line = reader.readLine()) != null)
            {
                result += line + "\n";
            }
            result = result.replace("\n\n", "\n");
            return result;
        }
        catch(IOException e1) {log.error(e1.getMessage());}
        catch(InterruptedException e2) {log.error(e2.getMessage());}

        return null;
    }

    public static String runCommand(String[] cmd)
    {
        String s;
        String results = "";
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(cmd, null);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
            // read the output from the command
            //System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null)
            {
                System.out.println(s);
                results = results + s;
            }
            // read any errors from the attempted command
            //System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null)
            {
                System.out.println(s);
                results = results + s;
            }
            return results.toString();
        }
        catch (IOException e)
        {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    private static String getAdbSpecificDevice(Device device) {
        String adbCommand = Main.adbPath + " -s ";
        if (device.getType() == Device.DEVICE_TYPE_USB) {
            adbCommand += device.getSerialID();
        } else if (device.getType() == Device.DEVICE_TYPE_REMOTE) {
            adbCommand += device.getRemoteIP()+":"+ADB_DEVICE_PORT;
        }
        return adbCommand;
    }

    public static String getDeviceName(Device device) {
        String model = runCommand(getAdbSpecificDevice(device) + " shell getprop ro.product.model");
        String manufacturer = runCommand(getAdbSpecificDevice(device) + " shell getprop ro.product.manufacturer");
        String androidVersionName = runCommand(getAdbSpecificDevice(device) + " shell getprop ro.build.version.release");
        String androidVersionCode = runCommand(getAdbSpecificDevice(device) + " shell getprop ro.build.version.sdk");

        String deviceName = manufacturer.trim() + " " + model.trim() + " (Android " + androidVersionName.trim() + ", SDK " + androidVersionCode.trim() + ")";

        return deviceName;
    }

    public static String getDeviceSerialNo(Device device) {
        String output = runCommand(getAdbSpecificDevice(device) + " shell getprop ro.serialno");
        String serialNumber = output.trim();
        return serialNumber;
    }

    public static String getDeviceIP(Device device) {
        String output = runCommand(getAdbSpecificDevice(device) + " shell ip -f inet addr show wlan0");

        String ipFromText = IPHelper.getIPFromText(output);
        if (ipFromText == null) {
            log.error("Could not get device IP - make sure the device is connected to your WiFi");
        }
        return ipFromText;
    }

    public static boolean disconnectDevice(Device device) {
        String output = runCommand(Main.adbPath + " disconnect " + device.getRemoteIP() + ":" + ADB_DEVICE_PORT);
        if (output.contains("disconnected")) {
            return true;
        } else {
            return false;
        }
    }

    public static void disconnectDeviceAsync(Device device) {
        Runnable r = new Runnable() {
            public void run() {
                disconnectDevice(device);
            }
        };
        new Thread(r).start();
    }

    public static boolean usbConnectToDevice(Device device) {
        runCommand(getAdbSpecificDevice(device) + " tcpip " + ADB_DEVICE_PORT);
        String output = runCommand(Main.adbPath + " connect " + device.getRemoteIP() + ":" + ADB_DEVICE_PORT);
        log.debug(output);
        if (output.contains("unable")) {
            return false;
        } else {
            return true;
        }
    }

    public static void usbConnectToDeviceAsync(Device device, Button connectButton) {
        connectButton.setDisable(true);
        Runnable r = new Runnable() {
            public void run() {
                usbConnectToDevice(device);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (connectButton != null) connectButton.setDisable(false);
                    }
                });
            }
        };
        new Thread(r).start();
    }

    public static boolean remoteConnectToDevice(Device device) {
        String output = runCommand(Main.adbPath + " connect " + device.getRemoteIP() + ":" + ADB_DEVICE_PORT);
        log.debug(output);
        if (output.contains("unable")) {
            return false;
        } else {
            return true;
        }
    }

    public static void remoteConnectToDeviceAsync(Device device, Button connectButton) {
        connectButton.setDisable(true);
        Runnable r = new Runnable() {
            public void run() {
                remoteConnectToDevice(device);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (connectButton != null) connectButton.setDisable(false);
                    }
                });
            }
        };
        new Thread(r).start();
    }


}

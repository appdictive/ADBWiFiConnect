package dk.appdictive.adbwificonnect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JesperLR on 05-11-2016.
 */
public class IPHelper {

    private static final String IP_ADDRESS_REGEX = "(?:[0-9]{1,3}\\.){3}[0-9]{1,3}";

    public static String getIPFromText(String text) {
        Pattern ipAddressPattern = Pattern.compile(IP_ADDRESS_REGEX);

        Matcher m = ipAddressPattern.matcher(text);
        String ipAddress = null;
        while (m.find()) {
            String s = m.group(0);
            if (ipAddress == null) {
                //we assume the first IP address is the one we want to address
                ipAddress = s;
            }
        }
        return ipAddress;
    }

}

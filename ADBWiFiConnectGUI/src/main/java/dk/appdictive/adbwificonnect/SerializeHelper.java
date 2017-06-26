package dk.appdictive.adbwificonnect;

import java.io.*;
import java.util.Base64;

/**
 * Created by JesperLR on 21-May-17.
 */
public class SerializeHelper {

    public static String serializeArray(Device[] array) {
        // serialize
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(array);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // your string
        String serializedArray = new String(Base64.getEncoder().encode(out.toByteArray()));
        return serializedArray;
    }

    public static Device[] deserializeArray(String serializedArray) {
        // deserialize
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(serializedArray.getBytes()));
            return (Device[]) new ObjectInputStream(in).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        }
        return null;
    }

}

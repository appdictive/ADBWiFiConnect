package dk.appdictive.adbwificonnect.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.sdk.AndroidSdkUtils;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Created by JesperLR on 29-10-2016.
 */
public class RemoteConnect extends AnAction {

    private static final String PREF_JAR_LOCATION = "JAR_LOCATION", PREF_ADB_LOCATION = "ADB_LOCATION", PREFS_ID = "dk/appdictive/adbconnect";
    private Preferences prefs;

    private static final String WIFI_CONNECT_TITLE = "ADB WiFi Connect";
    private static final NotificationGroup NOTIFICATION_GROUP =
            NotificationGroup.balloonGroup(WIFI_CONNECT_TITLE);


    @Override
    public void actionPerformed(AnActionEvent e) {
//        Messages.showMessageDialog(e.getProject(), "isAndroidSdkAvailable: " + AndroidSdkUtils.isAndroidSdkAvailable() + " AndroidSDKPath: " + AndroidSdkUtils.getAdb(e.getProject()).getAbsolutePath(), "Information", Messages.getInformationIcon());
        if (isADBInstalled() && getAdbPath(e.getProject()) != null) {
            launchADBWiFiConnect(e);
        } else {
            showNotification("adb not found in your environment, please check Android SDK installation and setup, and confirm that adb is available from commandline.", NotificationType.ERROR);
        }
    }

    private void launchADBWiFiConnect(AnActionEvent e) {
        String pluginsPath = PathManager.getPluginsPath();
        String path = "\"" + pluginsPath + File.separator + "ADBWiFiConnect" + File.separator + "lib" + File.separator + "ADBWiFiConnectGUI.jar" + "\"";

        // Retrieve the user preference node for the package
        Preferences systemRoot = Preferences.userRoot();
        prefs = systemRoot.node(PREFS_ID);

        prefs.put(PREF_JAR_LOCATION, path);
        String adbPath = getAdbPath(e.getProject());
        prefs.put(PREF_ADB_LOCATION, adbPath);
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            ex.printStackTrace();
        }

        if (OSValidator.isMac() || OSValidator.isUnix()) {
            String[] commandtest = new String[] {
                    "/bin/sh",
                    "-c",
                    "cd / && " + "java -jar " + path+""
            };

            try {
                Runtime.getRuntime().exec(commandtest, null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (OSValidator.isWindows()) {
            String[] commandtest = new String[] {
                    "cmd.exe",
                    "/c",
                    "cd / && " + "javaw -jar " + path+""
            };

            try {
                Runtime.getRuntime().exec(commandtest, null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean isADBInstalled() {
        return AndroidSdkUtils.isAndroidSdkAvailable();
    }

    public static void showNotification(final String message,
                                        final NotificationType type) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override public void run() {
                Notification notification =
                        NOTIFICATION_GROUP.createNotification(WIFI_CONNECT_TITLE, message, type, null);
                Notifications.Bus.notify(notification);
            }
        });
    }

    private String getAdbPath(Project project) {
        String adbPath = "";
        File adbFile = AndroidSdkUtils.getAdb(project);
        if (adbFile != null) {
            adbPath = adbFile.getAbsolutePath();
        }
        return adbPath;
    }



}


package dk.appdictive.adbwificonnect.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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

    @Override
    public void actionPerformed(AnActionEvent e) {
//        Messages.showMessageDialog(e.getProject(), "isAndroidSdkAvailable: " + AndroidSdkUtils.isAndroidSdkAvailable() + " AndroidSDKPath: " + AndroidSdkUtils.getAdb(e.getProject()).getAbsolutePath(), "Information", Messages.getInformationIcon());

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

    private String getAdbPath(Project project) {
        String adbPath = "";
        File adbFile = AndroidSdkUtils.getAdb(project);
        if (adbFile != null) {
            adbPath = adbFile.getAbsolutePath();
        }
        return adbPath;
    }

}


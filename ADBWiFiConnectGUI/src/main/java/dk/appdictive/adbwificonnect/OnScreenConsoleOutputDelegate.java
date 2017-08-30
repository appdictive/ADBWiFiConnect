package dk.appdictive.adbwificonnect;

import javafx.scene.control.TextArea;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by tobalr on 3/1/17.
 */
public class OnScreenConsoleOutputDelegate extends AppenderSkeleton {
    public static final int MAX_LOG_LENGTH = 5000;
    private TextArea outputTextArea;
    private boolean showDebug = false;
    String fullLog = "", noDebugLog = "";

    public OnScreenConsoleOutputDelegate(TextArea outputTextArea) {
        this.outputTextArea = outputTextArea;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        String message = String.format("[%s]  %s", loggingEvent.getLevel(), loggingEvent.getRenderedMessage());

        if (loggingEvent.getLevel().equals(org.apache.log4j.Level.DEBUG)) {
            if (showDebug) addToLogOutput(message);
        } else {
            addToLogOutput(message);
            noDebugLog = noDebugLog.substring(Math.max(0, noDebugLog.length() - MAX_LOG_LENGTH), noDebugLog.length());
            noDebugLog += message + "\n";
        }

        fullLog = fullLog.substring(Math.max(0, fullLog.length() - MAX_LOG_LENGTH), fullLog.length());
        fullLog += message + "\n";
    }


    @Override
    public void close() {
        addToLogOutput("--Closing--");
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    private void addToLogOutput(String message) {
        int length = outputTextArea.getText().length();
        outputTextArea.deleteText(0, Math.max(0, length - MAX_LOG_LENGTH));
        outputTextArea.appendText(message + "\n");
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
        if (showDebug) {
            outputTextArea.setText(fullLog);
        } else {
            outputTextArea.setText(noDebugLog);
        }

    }
}

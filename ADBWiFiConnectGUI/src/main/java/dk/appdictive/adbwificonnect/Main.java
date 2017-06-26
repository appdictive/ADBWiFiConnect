package dk.appdictive.adbwificonnect;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Main extends Application implements Initializable {

    private static final String PREF_JAR_LOCATION = "JAR_LOCATION", PREF_ADB_LOCATION = "ADB_LOCATION", PREF_SAVED_CONNECTIONS = "SAVED_CONNECTIONS", PREFS_ID = "dk/appdictive/adbconnect";

    @FXML
    private TextArea outputTextArea;
    @FXML
    private CheckMenuItem menuItemDebug;
    @FXML
    private ListView listView;
    @FXML
    private ListView listViewSaved;
    ObservableList<Device> observableList = FXCollections.observableArrayList();
    ObservableList<Device> observableListSavedConnections = FXCollections.observableArrayList();
    private boolean isWindowActive = true;
    private Preferences prefs;
    private Logger log = Logger.getLogger(Main.class.getName());
    private OnScreenConsoleOutputDelegate onScreenConsoleOutputDelegate;
    public static String adbPath;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource = getClass().getResource("/main.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setController(this);
        Parent root = loader.load();

        primaryStage.setTitle("ADB WiFi Connect");
        Scene scene = new Scene(root, 700, 640);
        primaryStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    // not showing...
                    isWindowActive = false;
                    log.debug("Window not showing");
                } else {
                    // showing ...
                    isWindowActive = true;
                    startDeviceListUpdateThread();
                    log.debug("Window showing");
                }
            }
        });
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                isWindowActive = false;
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void setupLogAppender() {
        onScreenConsoleOutputDelegate = new OnScreenConsoleOutputDelegate(outputTextArea);
        Logger.getRootLogger().addAppender(onScreenConsoleOutputDelegate);
//        Logger.getRootLogger().addAppender(new AlertOnErrorsDelegate());
    }

    private void initializeSavedData() {
        // Retrieve the user preference node for the package
        Preferences systemRoot = Preferences.userRoot();
        prefs = systemRoot.node(PREFS_ID);

        String jarLocation = prefs.get(PREF_JAR_LOCATION, null);
        if (jarLocation != null) {
            log.info("Find the runnable jar file here: \n" + jarLocation + "\n");
        }

        adbPath = prefs.get(PREF_ADB_LOCATION, "adb");
        if (adbPath == null) adbPath = "adb";
        log.debug("ADB path: " + adbPath);

        String savedData = prefs.get(PREF_SAVED_CONNECTIONS, null);

        if (savedData != null) {
            Device[] deserializeArray = SerializeHelper.deserializeArray(savedData);
            if (deserializeArray != null) {
                observableListSavedConnections.addAll(new ArrayList<>(Arrays.asList(deserializeArray)));
            }

            if (observableListSavedConnections.size() == 0) {
                writeWelcome(false);
            } else {
                writeWelcome(true);
            }
        }
    }

    private void writeWelcome(boolean hasSavedConnections) {
        if (!hasSavedConnections) {
            log.info("Welcome to ADB WiFi Connect!\n\n" +
                    "Please plug in an Android device by USB cable, then click connect to establish a remote connection to the device. A new connection will show up on the list. Click save on the remote connection to put it on the list of saved connections and next time a remote connection is needed, simply click connect on the saved connection.\n" +
                    "\n" +
                    "Happy developing! :)");
        } else {
            log.info("Welcome back!\n\n" +
                    "Please click connect on a saved connection to reconnect to that, or plug in a new device with USB to make a new remote connection.\n" +
                    "\n" +
                    "Happy developing! :)");
        }


    }

    //should be run from background thread
    private void updateListOfDevices(String[] adbDevicesListOutput) {
        ArrayList<Device> currentDevices = new ArrayList<>();
        for (String adbDeviceLine : adbDevicesListOutput) {
            if (adbDeviceLine.contains("List") || adbDeviceLine.contains("daemon") || adbDeviceLine.trim().equals("")) {
                //ignore line
            } else {
                //is a device line so check for either IP or get device adb ID
                Device currentDevice = new Device();
                if (adbDeviceLine.contains("offline")) {
                    currentDevice.setType(Device.DEVICE_TYPE_OFFLINE);
                    currentDevice.setName(adbDeviceLine.replace("offline", "").trim());
                } else {
                    String ipFromText = IPHelper.getIPFromText(adbDeviceLine);
                    if (ipFromText == null) {
                        currentDevice.setSerialID(adbDeviceLine.replace("device", "").trim());
                        currentDevice.setRemoteIP(ADBCommands.getDeviceIP(currentDevice));
                        currentDevice.setType(Device.DEVICE_TYPE_USB);
                    } else {
                        currentDevice.setRemoteIP(ipFromText);
                        currentDevice.setSerialID(ADBCommands.getDeviceSerialNo(currentDevice));
                        currentDevice.setType(Device.DEVICE_TYPE_REMOTE);
                    }
                    currentDevice.setName(ADBCommands.getDeviceName(currentDevice));
                }

                currentDevices.add(currentDevice);
            }
        }
        updateData(currentDevices);
    }

    private void startDeviceListUpdateThread() {
        Runnable r = new Runnable() {
            public void run() {
                log.debug("Running update of adb devices with window active: " + isWindowActive);
                String output = ADBCommands.runCommand(Main.adbPath + " devices");
                log.debug(output);
                updateListOfDevices(output.split("\n"));

                try {
                    Thread.sleep(3000);
                    if (isWindowActive) {
                        new Thread(this).start();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }

            }
        };

        log.debug("Starting update thread for list of ADB devices");
        new Thread(r).start();
    }

    public void updateData(List<Device> devices) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // Update UI here.
                observableList.clear();
                observableList.addAll(devices);
            }
        });
    }

    private void saveUpdatedConnectionsList() {
        prefs.put(PREF_SAVED_CONNECTIONS,  SerializeHelper.serializeArray(observableListSavedConnections.toArray(new Device[observableListSavedConnections.size()])));
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public void saveConnection(Device device) {
        for (Device savedDevice : observableListSavedConnections) {
            if (device.getRemoteIP().equals(savedDevice.getRemoteIP())) {
                observableListSavedConnections.remove(savedDevice);
                break;
            }
        }

        Device newSavedConnection = new Device();
        newSavedConnection.setName(device.getName());
        newSavedConnection.setRemoteIP(device.getRemoteIP());
        newSavedConnection.setSerialID(device.getSerialID());
        newSavedConnection.setType(Device.DEVICE_TYPE_SAVED_REMOTE);
        observableListSavedConnections.add(0, newSavedConnection);

        saveUpdatedConnectionsList();
    }

    public void deleteConnection(Device device) {
        for (Device savedDevice : observableListSavedConnections) {
            if (device.getRemoteIP().equals(savedDevice.getRemoteIP())) {
                observableListSavedConnections.remove(savedDevice);
                saveUpdatedConnectionsList();
                return;
            }
        }
    }

    public void setupListView()
    {
        listView.setItems(observableList);
        listView.setCellFactory(new Callback<ListView<Device>, ListCell<Device>>()
        {
            @Override
            public ListCell<Device> call(ListView<Device> listView)
            {
                return new ListViewCell(Main.this);
            }
        });

        listViewSaved.setItems(observableListSavedConnections);
        listViewSaved.setCellFactory(new Callback<ListView<Device>, ListCell<Device>>()
        {
            @Override
            public ListCell<Device> call(ListView<Device> listView)
            {
                return new ListViewCell(Main.this);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assert listView != null : "fx:id=\"listView\" was not injected: check your FXML file.";
        setupListView();
        setupLogAppender();
        initializeSavedData();
        startDeviceListUpdateThread();

        menuItemDebug.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onScreenConsoleOutputDelegate.setShowDebug(menuItemDebug.isSelected());
            }
        });
    }


}

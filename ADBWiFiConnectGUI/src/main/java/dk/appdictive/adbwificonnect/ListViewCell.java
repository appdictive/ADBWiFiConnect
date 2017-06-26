package dk.appdictive.adbwificonnect;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListViewCell extends ListCell<Device> implements Initializable
{

    FXMLLoader fxmlLoader;

    @FXML
    private HBox hBox;
    @FXML
    private Label connectionType;
    @FXML
    private Label deviceName;
    @FXML
    private Label deviceAddress;
    @FXML
    private Button connectAndDisconnectButton;
    @FXML
    private Button saveAndDeleteButton;

    private Main main;

    public ListViewCell(Main main) {
        this.main = main;
    }

    @Override
    public void updateItem(Device device, boolean empty)
    {
        super.updateItem(device,empty);

        if(empty || device == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/list_cell_item.fxml"));
                fxmlLoader.setController(this);

                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            deviceName.setText(device.getName());
            saveAndDeleteButton.setVisible(false);
            connectAndDisconnectButton.setVisible(true);

            if (device.getType() == Device.DEVICE_TYPE_USB) {
                connectAndDisconnectButton.setText("Connect");
                connectionType.setText("USB");
                deviceAddress.setText(device.getSerialID());
            } else if (device.getType() == Device.DEVICE_TYPE_REMOTE) {
                connectAndDisconnectButton.setText("Disconnect");
                connectionType.setText("WI-FI");
                deviceAddress.setText(device.getRemoteIP());
                saveAndDeleteButton.setVisible(true);
                saveAndDeleteButton.setText("Save");
            } else if (device.getType() == Device.DEVICE_TYPE_SAVED_REMOTE) {
                connectAndDisconnectButton.setText("Connect");
                connectionType.setText("Saved");
                deviceAddress.setText(device.getRemoteIP());
                saveAndDeleteButton.setVisible(true);
                saveAndDeleteButton.setText("Delete");
            } else {
                connectAndDisconnectButton.setVisible(false);
                if (device.getType() == Device.DEVICE_TYPE_OFFLINE) connectionType.setText("OFFLINE");
            }

            setText(null);
            setGraphic(hBox);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Device item = getItem();
                if (item.getType() == Device.DEVICE_TYPE_SAVED_REMOTE){
                    TextInputDialog textInputDialog = new TextInputDialog(item.getName());
                    textInputDialog.setTitle("Set device name");
                    textInputDialog.setHeaderText("");
                    Optional<String> s = textInputDialog.showAndWait();

                    if (s.isPresent() && !s.get().trim().isEmpty()) {
                        item.setName(s.get().trim());
                        main.saveConnection(item);
                    }
                }
            }
        });

        connectAndDisconnectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Device item = getItem();
                if (item.getType() == Device.DEVICE_TYPE_USB) {
                    ADBCommands.usbConnectToDevice(item);
                } else if (item.getType() == Device.DEVICE_TYPE_SAVED_REMOTE){
                    ADBCommands.remoteConnectToDevice(item);
                } else if (item.getType() == Device.DEVICE_TYPE_REMOTE){
                    ADBCommands.disconnectDevice(item);
//                    connectionButton.setDisable(true);
                }
            }
        });

        saveAndDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Device item = getItem();
                if (item.getType() == Device.DEVICE_TYPE_REMOTE) {
                    main.saveConnection(item);
                } else if (item.getType() == Device.DEVICE_TYPE_SAVED_REMOTE){
                    main.deleteConnection(item);
                }
            }
        });
    }
}
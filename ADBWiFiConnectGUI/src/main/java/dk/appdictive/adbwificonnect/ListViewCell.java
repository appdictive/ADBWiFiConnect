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
    private Button connectAndSaveButton;
    @FXML
    private Button deleteAndDisconnectButton;

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
            deleteAndDisconnectButton.setVisible(true);
            connectAndSaveButton.setDisable(false);

            if (device.getType() == Device.DEVICE_TYPE_USB) {
                deleteAndDisconnectButton.setVisible(false);
                connectAndSaveButton.setText("CONNECT");
                connectionType.setText("USB");
                deviceAddress.setText(device.getSerialID());
            } else if (device.getType() == Device.DEVICE_TYPE_REMOTE) {
                deleteAndDisconnectButton.setText("DISCONNECT");
                connectionType.setText("WI-FI");
                deviceAddress.setText(device.getRemoteIP());
                connectAndSaveButton.setText("SAVE");
                if (main.hasRemoteIPSaved(device.getRemoteIP())) {
                    connectAndSaveButton.setDisable(true);
                }
            } else if (device.getType() == Device.DEVICE_TYPE_SAVED_REMOTE) {
                if (main.isCurrentlyConnectedToRemoteIP(device.getRemoteIP())) {
                    connectAndSaveButton.setDisable(true);
                }
                connectAndSaveButton.setText("CONNECT");
                connectionType.setText("");
                deviceAddress.setText(device.getRemoteIP());
                deleteAndDisconnectButton.setText("DELETE");
            } else {
                connectAndSaveButton.setVisible(false);
                deleteAndDisconnectButton.setVisible(false);
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
                if (item != null && item.getType() == Device.DEVICE_TYPE_SAVED_REMOTE){
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

        connectAndSaveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Device item = getItem();
                if (item.getType() == Device.DEVICE_TYPE_USB) {
                    ADBCommands.usbConnectToDeviceAsync(item, connectAndSaveButton);
                } else if (item.getType() == Device.DEVICE_TYPE_SAVED_REMOTE){
                    ADBCommands.remoteConnectToDeviceAsync(item, connectAndSaveButton);
                } else if (item.getType() == Device.DEVICE_TYPE_REMOTE){
                    main.saveConnection(item);
                }
            }
        });

        deleteAndDisconnectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Device item = getItem();
                if (item.getType() == Device.DEVICE_TYPE_REMOTE) {
                    ADBCommands.disconnectDeviceAsync(item);
                } else if (item.getType() == Device.DEVICE_TYPE_SAVED_REMOTE){
                    main.deleteConnection(item);
                }
            }
        });
    }
}
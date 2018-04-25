package contractmanager.view;

import contractmanager.controller.Controller;
import contractmanager.model.DataModel;
import contractmanager.utility.ConsoleApplication;
import contractmanager.utility.ConsoleWriter;
import cz.zcu.kiv.contractparser.model.ContractType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;


public class ContractManager extends Application {

    //final Logger logger = Logger.getLogger(String.valueOf(ContractManager.class));

    public static final ResourceBundle properties = ResourceBundle.getBundle("contractmanager");
    public static final ResourceBundle localization = ResourceBundle.getBundle("contractmanager",
            new Locale("en", "EN"));

    public static Stage stage;
    public static Scene scene;
    public static DataModel extractorDataModel;
    public static DataModel comparatorDataModel;
    public static ConsoleWriter consoleWriter;

    @Override
    public void start(Stage _stage) {

        stage = _stage;

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(properties.getString("sceneFileName")));
        loader.setResources(localization);

        try {
            Parent root = loader.load();

            stage.setTitle(localization.getString("windowTitle"));

            stage.getIcons().add(new Image(properties.getString("icon")));

            // set default window size
            double height = Double.parseDouble(properties.getString("mainWindowWidth"));
            double width = Double.parseDouble(properties.getString("mainWindowHeight"));
            stage.setHeight(height);
            stage.setWidth(width);
            stage.setMinHeight(height);
            stage.setMinWidth(width);

            scene = new Scene(root);
            stage.setScene(scene);

            stage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });

            extractorDataModel = new DataModel();

            Controller controller = loader.getController();
            controller.initController(stage);

            stage.show();

            GridPane gridDetails = (GridPane) scene.lookup("#gridDetails");
            GridPane gridGlobalStats = (GridPane) scene.lookup("#gridGlobalStats");

            consoleWriter = new ConsoleWriter();
            
            int row = 0;

            for(Map.Entry<ContractType, Boolean> entry : extractorDataModel.getContractTypes().entrySet()) {
                ContractType contractType = entry.getKey();
                boolean used = entry.getValue();

                ToolBar tb_filter = (ToolBar) scene.lookup("#tb_filter");
                CheckBox checkBox = new CheckBox();
                checkBox.setText(contractType.name());

                if(used) {
                    Label lblTitle = new Label();
                    lblTitle.setText(contractType.name() + " " + localization.getString("labelContracts") + ": ");
                    lblTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

                    Label lblValue = new Label();
                    lblValue.setId("lbl_" + contractType.name());
                    lblValue.setText("");

                    gridDetails.addRow(row + 2, lblTitle);
                    gridDetails.add(lblValue, 1, row + 2);


                    Label lblGlobalStatsTitle = new Label();
                    lblGlobalStatsTitle.setText(contractType.name() + " " + localization.getString("labelContracts") + ": ");
                    lblGlobalStatsTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

                    Label lblGlobalStatsValue = new Label();
                    lblGlobalStatsValue.setId("lblGlobalStats" + contractType.name());
                    lblGlobalStatsValue.setText("0");

                    gridGlobalStats.addRow(row + 5, lblGlobalStatsTitle);
                    gridGlobalStats.add(lblGlobalStatsValue, 1, row + 5);

                    row++;

                    checkBox.setSelected(true);
                }

                tb_filter.getItems().add(checkBox);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Main application method. It decides whether to run standard application with graphic user interface (no
     * arguments used) or console application (arguments used).
     *
     * @param args Application arguments
     */
    public static void main(String[] args) {

        // if there are no arguments run standard application with GUI
        if(args.length == 0){
            launch(args);
        }
        // otherwise run application in console
        else{
            ConsoleApplication.runConsoleApplication(args);
        }
    }
}

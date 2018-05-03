package contractmanager;

import contractmanager.controller.Controller;
import contractmanager.application.ApplicationData;
import contractmanager.utility.ConsoleApplication;
import contractmanager.utility.ResourceHandler;
import cz.zcu.kiv.contractparser.api.ApiFactory;
import cz.zcu.kiv.contractparser.api.BatchContractComparatorApi;
import cz.zcu.kiv.contractparser.api.BatchContractExtractorApi;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * This class is the Launcher for this program. It either runs the application with graphics interface or process given
 * arguments and runs the application as a single command.
 *
 * @author Vaclav Mares
 */
public class ContractManager extends Application {

    /** Log4j logger for this class */
    private final static Logger logger = Logger.getLogger(String.valueOf(ContractManager.class));

    /** Default font for labels */
    public static final Font DEFAULT_LABEL_TITLE_FONT = Font.font("System", FontWeight.BOLD, 12);

    /** Main stage of application */
    public static Stage stage;

    /** Main scene of application */
    public static Scene scene;

    /** Application data containing individual parts of application */
    private static ApplicationData applicationData;


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
            ApiFactory apiFactory = new ApiFactory();
            BatchContractExtractorApi batchContractExtractorApi = apiFactory.getBatchContractExtractorApi();
            BatchContractComparatorApi batchContractComparatorApi = apiFactory.getBatchContractComparatorApi();
            ConsoleApplication consoleApplication = new ConsoleApplication(batchContractExtractorApi, batchContractComparatorApi);
            consoleApplication.runConsoleApplication(args);
        }
    }


    /**
     * This methods overrides method of JavaFX and is used to start GUI version of application.
     *
     * @param _stage    Stage of main application
     */
    @Override
    public void start(Stage _stage) {

        stage = _stage;
        prepareWindow();
    }


    /**
     * Prepares main window of the application. It sets its title, icon, size etc. It also prepares on close and action,
     * controller and application management.
     */
    private void prepareWindow(){

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(ResourceHandler.getProperties()
                .getString("sceneFileName")));
        loader.setResources(ResourceHandler.getLocalization());

        try {
            Parent root = loader.load();

            // set window title
            stage.setTitle(ResourceHandler.getLocaleString("windowTitle"));

            // set window icon
            stage.getIcons().add(new Image(ResourceHandler.getProperties().getString("icon")));

            // set default window size
            double height = Double.parseDouble(ResourceHandler.getProperties().getString("mainWindowWidth"));
            double width = Double.parseDouble(ResourceHandler.getProperties().getString("mainWindowHeight"));
            stage.setHeight(height);
            stage.setWidth(width);
            stage.setMinHeight(height);
            stage.setMinWidth(width);

            // attach scene
            scene = new Scene(root);
            stage.setScene(scene);

            // set on close action
            stage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });

            // prepare application model
            applicationData = new ApplicationData();

            // prepare controller
            Controller controller = loader.getController();
            controller.initController(stage);

            stage.show();

            // init scenes of application tabs
            applicationData.getExtractorApplicationTab().initScene();
            applicationData.getComparatorApplicationTab().initScene();

        } catch (IOException e) {

            logger.error(ResourceHandler.getLocaleString("errorCannotLoadMainWindow"));
            logger.error(e.getMessage());
            closeApplication(1);
        }
    }


    /**
     * Closes the whole application by returning given number.
     *
     * @param returnValue   Error value (0 correct end)
     */
    public static void closeApplication(int returnValue) {
        
        Platform.exit();
        System.exit(returnValue);
    }


    // Getters and Setters
    public static ApplicationData getApplicationData() {
        return applicationData;
    }

    public static Scene getMainScene() {
        return scene;
    }
}

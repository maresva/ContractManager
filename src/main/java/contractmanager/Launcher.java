package contractmanager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckListView;

import java.util.Locale;
import java.util.ResourceBundle;


public class Launcher extends Application {

    final Logger logger = Logger.getLogger(String.valueOf(Launcher.class));

    public static final ResourceBundle properties = ResourceBundle.getBundle("contractmanager");
    public static final ResourceBundle localization = ResourceBundle.getBundle("contractmanager", new Locale("en", "EN"));

    public static Stage stage;
    public static Scene scene;
    public static DataModel dataModel;

    @Override
    public void start(Stage stage) throws Exception {

        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(properties.getString("sceneFileName")));
        loader.setResources(localization);
        Parent root = loader.load();

        stage.setTitle(localization.getString("windowTitle"));
        stage.getIcons().add(new Image("contract_manager.png"));

        scene = new Scene(root);
        stage.setScene(scene);


        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        dataModel = new DataModel();

        Controller controller = loader.getController();
        controller.setStage(stage);

        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package contractmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;


public class Launcher extends Application {

    final Logger logger = Logger.getLogger(String.valueOf(Launcher.class));

    final ResourceBundle properties = ResourceBundle.getBundle("contractmanager");
    final ResourceBundle localization = ResourceBundle.getBundle("contractmanager", new Locale("en", "EN"));

    public static Scene scene;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(properties.getString("sceneFileName")));
        loader.setResources(localization);
        Parent root = loader.load();

        stage.setTitle(localization.getString("windowTitle"));

        scene = new Scene(root);
        stage.setScene(scene);

        Controller controller = loader.getController();
        controller.setStage(stage);

        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

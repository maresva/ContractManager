package contractmanager.presentation.applicationtab;

import contractmanager.utility.ResourceHandler;
import contractmanager.view.ContractManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static contractmanager.utility.Utils.centerScene;


/**
 * Parent class representing Application Tabs. It contains its FileList and other presentation needed to represent
 * application tab.
 *
 * @author Vaclav Mares
 */
public class ApplicationTab {

    /** Loading window that is shown upon loading it can have progress bar */
    LoadingWindow loadingWindow;

    /** Current scene for details window */
    Scene detailsScene;


    public ApplicationTab() {
    }


    /**
     * Prepares window for details view. It creates object and sets the size of window and its position.
     * It also sets the title as well as icon.
     *
     * @param sceneFXMLName     Name of FXML scene file representing this Details window
     * @param WindowName        Title of the window
     */
    void prepareDetailsWindow(String sceneFXMLName, String WindowName) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(sceneFXMLName));
            loader.setResources(ResourceHandler.getLocalization());

            Parent root = loader.load();

            Stage stage = new Stage();
            detailsScene = new Scene(root);
            stage.setScene(detailsScene);

            // set default window size
            double height = Double.parseDouble(ResourceHandler.getProperties().getString("detailsWindowWidth"));
            double width = Double.parseDouble(ResourceHandler.getProperties().getString("detailsWindowHeight"));
            stage.setHeight(height);
            stage.setWidth(width);
            stage.setMinHeight(height);
            stage.setMinWidth(width);

            centerScene(ContractManager.scene, detailsScene);

            stage.setTitle(WindowName);

            stage.getIcons().add(new Image(ResourceHandler.getProperties().getString("icon")));
            stage.show();
        }
        catch (Exception e){
            // TODO could not show Details
        }
    }

    public LoadingWindow getLoadingWindow() {
        return loadingWindow;
    }
}

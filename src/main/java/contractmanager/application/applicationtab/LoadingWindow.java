package contractmanager.application.applicationtab;

import contractmanager.application.filelist.FileList;
import contractmanager.utility.ResourceHandler;
import contractmanager.ContractManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Represents loading window which is shown to disable application and show user that it is loading. It can also
 * contain progress bar.
 */
public class LoadingWindow {

    /** Stage of the loading window */
    private Stage loadingStage;

    /** Scene of the loading window */
    private Scene loadingScene;

    /** FXML file with scene of the window */
    private String sceneFile;

    
    public LoadingWindow(String sceneFile){
        this.sceneFile = sceneFile;
    }

    
    /**
     * Shows loading window which blocks any controls. It should be run from any thread (task) and hidden after the
     * job is done.
     */
    public void show() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(sceneFile));
        loader.setResources(ResourceHandler.getLocalization());

        try {
            Parent root = loader.load();
            loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.initOwner(ContractManager.stage);
            loadingStage.initStyle(StageStyle.UNDECORATED);

            loadingScene = new Scene(root);
            loadingStage.setScene(loadingScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When loading is done (finished on cancelled) - list is updated and loading window is hidden.
     */
    public void hide(FileList fileList) {

        fileList.updateList();
        loadingStage.hide();
    }



    // Getters and Setters
    public Stage getLoadingStage() {

        if(loadingStage == null){
            show();
        }

        return loadingStage;
    }

    public Scene getLoadingScene() {

        if(loadingScene == null){
            show();
        }

        return loadingScene;
    }
}

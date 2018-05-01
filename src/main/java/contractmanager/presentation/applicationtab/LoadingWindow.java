package contractmanager.presentation.applicationtab;

import contractmanager.presentation.filelist.FileList;
import contractmanager.utility.ResourceHandler;
import contractmanager.view.ContractManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoadingWindow {

    private Stage loadingStage;
    private Scene loadingScene;

    private String sceneFile;

    public LoadingWindow(String sceneFile){
        this.sceneFile = sceneFile;
    }

    /**
     * When loading of files is started - loading window is displayed for user. It shows progress of the action
     * on progress bar which is periodically updated.
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

package contractmanager.controller;

import contractmanager.presentation.filelist.ExtractorFileList;
import contractmanager.utility.ResourceHandler;
import contractmanager.view.ContractManager;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class FileHandler {

    public File chooseFolder(Stage stage) {

        DirectoryChooser chooser = new DirectoryChooser();
        return chooser.showDialog(stage);
    }


    public List<File> chooseFiles(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(ResourceHandler.getLocaleString("fileChooserFilter"),
                        "*.java","*.class"));

        return fileChooser.showOpenMultipleDialog(stage);
    }
    

    public int exportJavaFilesToJSON(Stage stage, ExtractorFileList fileList){

        // display directory chooser
        File selectedDirectory = chooseFolder(stage);

        int exportedFiles = 0;

        if(selectedDirectory != null) {

            exportedFiles = ContractManager.getApplicationData().getExtractorApplicationTab().getContractExtractorApi()
                    .exportJavaFilesToJson(fileList.getSelectedFiles(), selectedDirectory, true);
        }

        return exportedFiles;
    }
}

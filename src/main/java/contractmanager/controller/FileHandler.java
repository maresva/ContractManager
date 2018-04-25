package contractmanager.controller;

import contractmanager.filelist.FileList;
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
                new FileChooser.ExtensionFilter(ContractManager.localization.getString("fileChooserFilter"),
                        "*.java","*.class"));

        return fileChooser.showOpenMultipleDialog(stage);
    }
    

    public void exportToJSON(Stage stage, FileList fileList){

        // display directory chooser
        File selectedDirectory = chooseFolder(stage);

        if(selectedDirectory != null) {

            int exportedFiles = ContractManager.extractorDataModel.exportToJSON(fileList.getSelected(), selectedDirectory);

            ContractManager.consoleWriter.writeNumberOfExportedFiles(exportedFiles);
        }
    }
}

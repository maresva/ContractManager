package contractmanager.controller;

import contractmanager.presentation.filelist.ComparatorFileList;
import contractmanager.presentation.filelist.ExtractorFileList;
import contractmanager.utility.ResourceHandler;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.utils.IOServices;
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
    

    public int exportJavaFilesToJSONExtractor(Stage stage){

        // display directory chooser
        File selectedDirectory = chooseFolder(stage);

        int exportedFiles = 0;

        if(selectedDirectory != null) {

            exportedFiles = ContractManager.getApplicationData().getExtractorApplicationTab().getContractExtractorApi()
                    .exportJavaFilesToJson(ContractManager.getApplicationData().getExtractorApplicationTab()
                    .getFileList().getSelectedFiles(), selectedDirectory, !ContractManager.getApplicationData()
                    .getSettings().isMinJson());
        }

        return exportedFiles;
    }


    public int exportJavaFilesToJSONComparator(Stage stage){

        // display directory chooser
        File selectedDirectory = chooseFolder(stage);

        int exportedFiles = 0;

        if(selectedDirectory != null) {

            exportedFiles = ContractManager.getApplicationData().getComparatorApplicationTab().getContractComparatorApi()
                    .exportJavaFolderCompareReportToJson(ContractManager.getApplicationData().getComparatorApplicationTab()
                    .getFolderCompareReport(), selectedDirectory, !ContractManager.getApplicationData()
                    .getSettings().isMinJson());
        }

        return exportedFiles;
    }
}

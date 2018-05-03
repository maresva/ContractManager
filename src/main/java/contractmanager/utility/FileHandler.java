package contractmanager.utility;

import contractmanager.ContractManager;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * This class provides method to work with files and folders. It shows file/folder choosers and helps with data export.
 *
 * @author Vaclav Mares
 */
public class FileHandler {

    /** Log4j logger for this class */
    private final static Logger logger = Logger.getLogger(String.valueOf(FileHandler.class));

    
    /**
     * Opens a folder chooser and returns chosen folder.
     *
     * @param stage     Application stage
     * @return          Chosen folder
     */
    public File chooseFolder(Stage stage) {

        DirectoryChooser chooser = new DirectoryChooser();
        return chooser.showDialog(stage);
    }


    /**
     * Opens a file chooser for multiple files and returns those chosen files.
     *
     * @param stage     Application stage
     * @return          Chosen files
     */
    public List<File> chooseFiles(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(ResourceHandler.getLocaleString("fileChooserFilter"),
                        "*.java","*.class"));

        return fileChooser.showOpenMultipleDialog(stage);
    }


    /**
     * Exports data from extractor to JSON.
     *
     * @param stage     Application stage
     */
    public void exportJavaFilesToJSONExtractor(Stage stage){

        // display directory chooser
        File selectedDirectory = chooseFolder(stage);

        int exportedFiles = 0;

        if(selectedDirectory != null) {

            exportedFiles = ContractManager.getApplicationData().getExtractorApplicationTab().getContractExtractorApi()
                    .exportJavaFilesToJson(ContractManager.getApplicationData().getExtractorApplicationTab()
                    .getFileList().getSelectedFiles(), selectedDirectory, !ContractManager.getApplicationData()
                    .getSettings().isMinJson());
        }

        logger.info(ResourceHandler.getLocaleString("infoFilesExported", exportedFiles));
    }


    /**
     * Exports data from comparator to JSON.
     *
     * @param stage     Application stage
     */
    public void exportJavaFilesToJSONComparator(Stage stage){

        // display directory chooser
        File selectedDirectory = chooseFolder(stage);

        int exportedFiles = 0;

        if(selectedDirectory != null) {

            exportedFiles = ContractManager.getApplicationData().getComparatorApplicationTab().getContractComparatorApi()
                    .exportJavaFolderCompareReportToJson(ContractManager.getApplicationData().getComparatorApplicationTab()
                    .getFolderCompareReport(), selectedDirectory, !ContractManager.getApplicationData()
                    .getSettings().isMinJson());
        }

        logger.info(ResourceHandler.getLocaleString("infoReportsExported", exportedFiles));
    }
}
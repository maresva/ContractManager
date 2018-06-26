package contractmanager.controller;

import contractmanager.ContractManager;
import contractmanager.utility.FileHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.*;

import java.io.File;
import java.util.List;


/**
 * Controller that binds actions to buttons.
 */
public class Controller {

    /** Main stage of application */
    private Stage mainStage;

    /** Manipulates with files especially with file or folder choosers */
    private FileHandler fileHandler;


    /**
     * This method initialize variables of controller after the application window is loaded.
     *
     * @param mainStage     Main stage of application
     */
    public void initController(Stage mainStage) {
        this.mainStage = mainStage;
        this.fileHandler = new FileHandler();
    }


    /**
     * This method is called when Add files  button is pressed (or chose from menu). It displays
     * file chooser where user chooses Java source files which should be processed.
     *
     * @param event     JavaFX ActionEvent
     */
    @FXML
    private void extractorAddFiles(ActionEvent event){

        List<File> files = fileHandler.chooseFiles(mainStage);
        if (files != null) {
            ContractManager.getApplicationData().getExtractorApplicationTab().getLoadingWindow().show();

            ContractManager.getApplicationData().getExtractorApplicationTab().getFileList().addFiles(files, null);
        }
    }


    /**
     * This method is called when Add files from directory button is pressed (or chose from menu). It displays
     * directory chooser where user chooses directory from which should be recursively processed all Java source files.
     *
     * @param event     JavaFX ActionEvent
     */
    @FXML
    private void extractorAddDirectory(ActionEvent event) {

        // display directory chooser
        File selectedDirectory = fileHandler.chooseFolder(mainStage);

        if(selectedDirectory != null) {
            ContractManager.getApplicationData().getExtractorApplicationTab().getLoadingWindow().show();
            ContractManager.getApplicationData().getExtractorApplicationTab().getFileList().addFiles(null, selectedDirectory);
        }
    }


    /**
     * This method is called when user click on button to remove files. It removes files from the internal memory
     * as well from the displayed list. Actual source files will remain untouched.
     *
     * @param event     JavaFX ActionEvent
     */
    @FXML
    public void extractorRemoveFiles(ActionEvent event) {

        ContractManager.getApplicationData().getExtractorApplicationTab().getFileList().removeFiles();
    }


    /**
     * This action is called when (De)Select All button is pressed. It does as it says its label. If it says Select All
     * it select all items. Otherwise it deselects all.
     *
     * @param event JavaFX event
     */
    @FXML
    private void extractorSelectAll(ActionEvent event) {

        ContractManager.getApplicationData().getExtractorApplicationTab().getFileList().selectAll();
    }


    /**
     * This method is called when user chooses to see details about selected file. It shows new window with details
     * and fills it with values. It show basic information such as filename as well as statistics about number of
     * contracts etc. It also shows the export structure of selected file.
     *
     * @param event JavaFX event
     */
    @FXML
    public void extractorShowDetails(ActionEvent event){
        ContractManager.getApplicationData().getExtractorApplicationTab().showDetailsWindow();
    }


    /**
     * This method is called when clicks on export to JSON button. It show directory chooser a exports all selected
     * JavaFiles to chosen folder.
     *
     * @param event JavaFX event
     */
    @FXML
    public void extractorExportToJSON(ActionEvent event){
        fileHandler.exportJavaFilesToJSONExtractor(mainStage);
    }


    /**
     * This method is called when clicks on export to JSON button. It show directory chooser a exports all selected
     * JavaCompareReports to chosen folder.
     *
     * @param event JavaFX event
     */
    @FXML
    public void comparatorExportToJSON(ActionEvent event){
         fileHandler.exportJavaFilesToJSONComparator(mainStage);
    }


    /**
     * This method is called when user chooses to see details about selected file. It shows new window with details
     * and fills it with values. It show basic information such as filename as well as statistics about number of
     * contracts etc. It also shows the export structure of selected file.
     *
     * @param event JavaFX event
     */
    @FXML
    public void comparatorShowDetails(ActionEvent event) {

        ContractManager.getApplicationData().getComparatorApplicationTab().showDetailsWindow();
    }


    /**
     * This action shows folder chooser and after choice it sets the first folder for Comparator.
     *
     * @param event JavaFX event
     */
    @FXML
    public void comparatorAddDirectory1(ActionEvent event) {

        File selectedDirectory = fileHandler.chooseFolder(mainStage);

        if(selectedDirectory != null) {
            ContractManager.getApplicationData().getComparatorApplicationTab().getFileList().selectDirectory(
                    selectedDirectory, true);
        }
    }


    /**
     * This action shows folder chooser and after choice it sets the second folder for Comparator.
     *
     * @param event JavaFX event
     */
    @FXML
    public void comparatorAddDirectory2(ActionEvent event) {

        File selectedDirectory = fileHandler.chooseFolder(mainStage);

        if(selectedDirectory != null) {
            ContractManager.getApplicationData().getComparatorApplicationTab().getFileList().selectDirectory(
                    selectedDirectory, false);
        }
    }


    /**
     * This action does comparison of both folders. Then it updates statistics and file list
     *
     * @param event JavaFX event
     */
    @FXML
    public void comparatorCompare(ActionEvent event) {

        ContractManager.getApplicationData().getComparatorApplicationTab().getFileList().compareFolders();
    }


    /**
     * This action applies filter which consists of multiple check boxes.
     *
     * @param event JavaFX event
     */
    @FXML
    public void filterExtractor(ActionEvent event) {

        ContractManager.getApplicationData().filterData(true);
    }


    /**
     * This action applies filter which consists of multiple check boxes.
     *
     * @param event JavaFX event
     */
    @FXML
    public void filterComparator(ActionEvent event) {

        ContractManager.getApplicationData().filterData(false);
    }
}
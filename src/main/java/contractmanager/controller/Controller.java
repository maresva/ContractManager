package contractmanager.controller;

import contractmanager.applicationTab.ApplicationTab;
import contractmanager.filelist.FileList;
import contractmanager.filelist.LoadingWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.*;

import java.io.File;
import java.util.List;

public class Controller {

    /** Main stage of application */
    private Stage mainStage;

    /** Tab for extractor part of application */
    private ApplicationTab extractorApplicationTab;

    /** Loading window enables showing and hiding progress bar on actions */
    private LoadingWindow loadingWindow;

    /** Manipulates with files especially work with file or folder choosers */
    private FileHandler fileHandler;


    /**
     * This method initialize variables of controller after the application window is loaded.
     *
     * @param mainStage     Main stage of application
     */
    public void initController(Stage mainStage) {
        this.mainStage = mainStage;
        this.loadingWindow = new LoadingWindow();
        this.extractorApplicationTab = new ApplicationTab(new FileList(loadingWindow));
        this.extractorApplicationTab.getFileList().setApplicationTab(extractorApplicationTab);
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
            loadingWindow.show();
            extractorApplicationTab.getFileList().addFiles(files, null);
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
            loadingWindow.show();
            extractorApplicationTab.getFileList().addFiles(null, selectedDirectory);
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
        extractorApplicationTab.getFileList().removeFiles();
    }


    /**
     * This action is called when (De)Select All button is pressed. It does as it says its label. If it says Select All
     * it select all items. Otherwise it deselects all.
     *
     * @param event JavaFX event
     */
    @FXML
    private void extractorSelectAll(ActionEvent event) {
        extractorApplicationTab.getFileList().selectAll();
    }


    /**
     * This method is called when user chooses to see details about selected files. It shows new window with details
     * and fills it with values. It show basic information such filename or path as well as statistics about
     * number of contracts etc. It also shows the export structure of selected file
     *
     * @param event JavaFX event
     */
    @FXML
    public void extractorShowDetails(ActionEvent event){
        extractorApplicationTab.showDetails();
    }


    /**
     * This method is called when clicks on export to JSON button. It show directory chooser a exports all selected
     * JavaFiles to chosen folder.
     *
     * @param event JavaFX event
     */
    @FXML
    public void extractorExportToJSON(ActionEvent event){
        fileHandler.exportToJSON(mainStage, extractorApplicationTab.getFileList());
    }

    /**
     * This action closes the application.
     *
     * @param event JavaFX event
     */
    @FXML
    public void closeApplication(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
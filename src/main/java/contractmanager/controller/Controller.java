package contractmanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.io.IOServices;
import cz.zcu.kiv.contractparser.model.ContractType;
import cz.zcu.kiv.contractparser.model.JavaFile;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.*;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Controller {

    private Stage mainStage;
    private Stage loadingStage;
    private Scene loadingScene;
    

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }


    /**
     * This method is called when Add files  button is pressed (or chose from menu). It displays
     * file chooser where user chooses Java source files which should be processed.
     *
     * @param event     JavaFX ActionEvent
     */
    @FXML
    private void chooseFiles(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(ContractManager.localization.getString("fileChooserFilter"),
                        "*.java", "*.class")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(mainStage);
        if (files != null) {

            showLoadingWindow();
            addFiles(files, null);
        }
    }


    /**
     * This method is called when Add files from directory button is pressed (or chose from menu). It displays
     * directory chooser where user chooses directory from which should be recursively processed all Java source files.
     *
     * @param event     JavaFX ActionEvent
     */
    @FXML
    private void chooseDirectory(ActionEvent event) {

        // display directory chooser
        DirectoryChooser chooser = new DirectoryChooser();
        //File defaultDirectory = new File("D:\\My\\_ZCU\\dp\\anot-20170926T144409Z-001\\anot\\guava\\guava-10.0");
        //chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(mainStage);

        if(selectedDirectory != null) {
            showLoadingWindow();
            addFiles(null, selectedDirectory);
        }
    }


    /**
     * Parses contracts from all given files. JavaFiles are then loaded into program.
     * File processing is done via JavaFX task and during this action loading window is displayed which show current
     * progress on progress bar (each processed file updates progress bar).
     *
     * @param inputFiles     List of files to be parsed
     */
    private void addFiles(List<File> inputFiles, File selectedDirectory) {

        // get progress bar
        ProgressBar pb_loading = (ProgressBar) loadingScene.lookup("#pb_loading");

        // start task
        Task<Boolean> task = new Task<Boolean>() {
            @Override public Boolean call() {

                List<File> files;

                if(inputFiles == null){
                    files = new ArrayList<>();
                    IOServices.getFilesFromFolder(selectedDirectory, files);
                }
                else{
                    files = inputFiles;
                }

                // how much should bar increase with one completed file
                double progressIncrease = 1.0 / (double)files.size();
                int addedFiles = 0;

                for(File file : files){
                    if (!isCancelled()) {
                        if (ContractManager.dataModel.addFile(file)) {
                            addedFiles++;

                            // update progress bar
                            pb_loading.setProgress(pb_loading.getProgress() + progressIncrease);
                        }
                    } else {
                        // stop if action was cancelled
                        break;
                    }
                }

                ContractManager.consoleWriter.writeNumberOfAddedFiles(addedFiles);

                return true;
            }
        };

        // if task is running - show Loading window
        task.setOnRunning((e) -> loadingStage.show());

        // otherwise hide Loading window and update list
        task.setOnSucceeded((e) -> {
            hideLoadingWindow();
        });
        task.setOnFailed((e) -> {
            Throwable throwable = task.getException();
            throwable.printStackTrace();
            hideLoadingWindow();
        });
        task.setOnCancelled((e) -> {
            hideLoadingWindow();
        });

        new Thread(task).start();
    }


    /**
     * When loading of files is started - loading window is displayed for user. It shows progress of the aciton
     * on progress bar which is periodically updated.
     *
     * @return  true/false whether the loading window was displayed
     */
    private boolean showLoadingWindow() {

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(
                ContractManager.properties.getString("sceneLoadingFileName")));
        loader.setResources(ContractManager.localization);

        try {
            Parent root = loader.load();
            loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.initOwner(ContractManager.stage);
            loadingStage.initStyle(StageStyle.UNDECORATED);

            loadingScene = new Scene(root);
            loadingStage.setScene(loadingScene);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * When loading is done (finished on cancelled) - list is updated and loading window is hidden.
     */
    public void hideLoadingWindow() {
        
        updateFileList();
        loadingStage.hide();
    }


    /**
     * This method is called when user click on button to remove files. It removes files from the internal memory
     * as well from the displayed list. Actual source files will remain untouched.
     *
     * @param event     JavaFX ActionEvent
     */
    @FXML
    public void removeFiles(ActionEvent event) {

        List<Integer> checkedIndexes = getSelected();

        // remove selected files
        int deletedFiles = ContractManager.dataModel.removeFiles(checkedIndexes);

        ContractManager.consoleWriter.writeNumberOfDeletedFiles(deletedFiles);
        
        updateFileList();
    }


    public List<Integer> getSelected() {
        CheckListView clv_files = (CheckListView) ContractManager.scene.lookup("#clv_files");

        // create list with indexes of checked files
        List<Integer> checkedIndexes = new ArrayList<>();

        for(int i = 0 ; i < clv_files.getItems().size() ; i++ ) {
            if(clv_files.getCheckModel().isChecked(i)) {
                checkedIndexes.add(i);
            }
        }

        return checkedIndexes;
    }


    /**
     * This method will update file list when called. It is called whenever some change occurs such as adding or
     * removing file.
     *
     */
    public void updateFileList() {

        // get CheckListView with list of java files names
        CheckListView clv_files = (CheckListView) ContractManager.scene.lookup("#clv_files");

        // get current list with JavaFiles
        List<JavaFile> files = ContractManager.dataModel.getFiles();

        // update list of files in checkListView
        final ObservableList<String> fileItems = FXCollections.observableArrayList();
        for (int i = 0; i < files.size(); i++) {

            if(files.get(i) != null){
                fileItems.add(files.get(i).getShortPath());
            }
        }
        clv_files.setItems(fileItems);

        // get number of files in total and number of currently selected
        int numberOfFilesTotal = clv_files.getItems().size();
        int numberOfFilesChecked = clv_files.getCheckModel().getCheckedIndices().size();
        updateSelected(numberOfFilesTotal, numberOfFilesChecked);

        // if there are no files - show label informing about empty list and disable select all check box
        Label lbl_list_empty = (Label) ContractManager.scene.lookup("#lbl_list_empty");
        Button btn_select_all = (Button) ContractManager.scene.lookup("#btn_select_all");

        if(files.size() > 0){
            lbl_list_empty.setVisible(false);
            btn_select_all.setDisable(false);
        }
        else {
            lbl_list_empty.setVisible(true);
            btn_select_all.setDisable(true);
        }

        // set on CheckBox event
        clv_files.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {

                // get number of files in total and number of currently selected
                int numberOfFilesTotal = clv_files.getItems().size();
                int numberOfFilesChecked = clv_files.getCheckModel().getCheckedIndices().size();
                updateSelected(numberOfFilesTotal, numberOfFilesChecked);
            }
        });


        // set event on click of a file
        clv_files.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                updateDetails(clv_files);
            }
        });
    }


    /**
     * This method updates details in the right part of window each time user selects different file.
     *
     * @param clv_files     CheckListView with files
     */
    public void updateDetails(CheckListView clv_files) {

        // get selected file and save it into data model
        int selectedId = clv_files.getSelectionModel().getSelectedIndex();

        if(selectedId >= 0 && selectedId < ContractManager.dataModel.getFiles().size()) {

            JavaFile selectedFile = ContractManager.dataModel.getFiles().get(selectedId);
            ContractManager.dataModel.setCurrentFile(selectedFile);

            // update label with name of the file
            Label lbl_filename_value = (Label) ContractManager.scene.lookup("#lbl_filename_value");
            lbl_filename_value.setText(selectedFile.getCompleteFileName());

            // update label with file path
            Label lbl_path_value = (Label) ContractManager.scene.lookup("#lbl_path_value");
            lbl_path_value.setText(selectedFile.getShortPath());

            // display number of contracts for each selected design by contract type
            for (Map.Entry<ContractType, Boolean> entry : ContractManager.dataModel.getContractTypes().entrySet()) {
                ContractType contractType = entry.getKey();
                boolean used = entry.getValue();

                if (used) {
                    Label lbl_value = (Label) ContractManager.scene.lookup("#lbl_" + contractType.name());
                    lbl_value.setText("" + selectedFile.getJavaFileStatistics().getNumberOfContracts().get(contractType));
                }
            }

            // once some file is selected - Show details button becomes available
            Button btn_show_details = (Button) ContractManager.scene.lookup("#btn_show_details");
            btn_show_details.setDisable(false);
        }
    }


    /**
     * This method updates (De)select All button. If there are some unselected files it has Select All label.
     * Otherwise it has Deselect All label. It also updates label informing about the number of selected files.
     *
     * @param numberOfFilesTotal        Total number of files
     * @param numberOfFilesChecked      Number of selected files
     */
    public void updateSelected(int numberOfFilesTotal, int numberOfFilesChecked) {

        // update Select all check box (is checked only if all files are selected)
        Button btn_select_all = (Button) ContractManager.scene.lookup("#btn_select_all");

        Button btn_remove_files = (Button) ContractManager.scene.lookup("#btn_remove_files");
        Button btn_export_files = (Button) ContractManager.scene.lookup("#btn_export_files");

        if(numberOfFilesTotal-numberOfFilesChecked == 0){
            btn_select_all.setText(ContractManager.localization.getString("buttonDeselectAll"));
            btn_remove_files.setDisable(false);
            btn_export_files.setDisable(false);
        }
        else{
            btn_select_all.setText(ContractManager.localization.getString("buttonSelectAll"));
            btn_remove_files.setDisable(true);
            btn_export_files.setDisable(true);
        }

        // update info label about number of selected files
        Label lbl_selected = (Label) ContractManager.scene.lookup("#lbl_selected");
        lbl_selected.setText(ContractManager.localization.getString("labelSelectedFiles") + ": "
                + numberOfFilesChecked + " / " + numberOfFilesTotal);
    }


    /**
     * This action is called when (De)Select All button is pressed. It does as it says its label. If it says Select All
     * it select all items. Otherwise it deselects all.
     *
     * @param event
     */
    @FXML
    private void selectAll(ActionEvent event) {

        Button btn_select_all = (Button) ContractManager.scene.lookup("#btn_select_all");
        CheckListView clv_files = (CheckListView) ContractManager.scene.lookup("#clv_files");

        if(btn_select_all.getText().equals(ContractManager.localization.getString("buttonSelectAll"))) {
            clv_files.getCheckModel().checkAll();
        }
        else{
            clv_files.getCheckModel().clearChecks();
        }
    }


    /**
     * This method is called when user chooses to see details about selected files. It shows new window with details
     * and fills it with values. It show basic information such filename or path as well as statistics about
     * number of contracts etc. It also shows the export structure of selected file
     *
     * @param event
     */
    @FXML
    public void showDetails(ActionEvent event){

        if(ContractManager.dataModel.getCurrentFile() != null) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(
                        ContractManager.properties.getString("sceneDetailsFileName")));
                loader.setResources(ContractManager.localization);
                Parent root = loader.load();

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);

                // set default window size
                double height = Double.parseDouble(ContractManager.properties.getString("detailsWindowWidth"));
                double width = Double.parseDouble(ContractManager.properties.getString("detailsWindowHeight"));
                stage.setHeight(height);
                stage.setWidth(width);
                stage.setMinHeight(height);
                stage.setMinWidth(width);

                stage.setTitle(ContractManager.localization.getString("windowTitleDetails") + " - "
                        + ContractManager.dataModel.getCurrentFile().getShortPath());
                // TODO zprovoznit icon v .jar
                //stage.getIcons().add(new Image(ContractManager.properties.getString("icon")));
                stage.show();

                // get selected java file and convert it to JSON
                JavaFile javaFile = ContractManager.dataModel.getCurrentFile();
                Gson gson = new Gson();
                String jsonInString = gson.toJson(javaFile);

                // object in JSON convert to "pretty print" string
                JsonParser parser = new JsonParser();
                gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement el = parser.parse(jsonInString);
                jsonInString = gson.toJson(el);

                TextArea ta_details = (TextArea) scene.lookup("#ta_details");
                ta_details.setText(jsonInString);

                Label lbl_filename_value = (Label) scene.lookup("#lbl_filename_value");
                lbl_filename_value.setText(ContractManager.dataModel.getCurrentFile().getCompleteFileName());

                Label lbl_path_value = (Label) scene.lookup("#lbl_path_value");
                lbl_path_value.setText(ContractManager.dataModel.getCurrentFile().getShortPath());

                Label lbl_number_classes_value = (Label) scene.lookup("#lbl_number_classes_value");
                lbl_number_classes_value.setText("" + ContractManager.dataModel.getCurrentFile().getJavaFileStatistics().getNumberOfClasses());

                Label lbl_number_methods_value = (Label) scene.lookup("#lbl_number_methods_value");
                lbl_number_methods_value.setText("" + ContractManager.dataModel.getCurrentFile().getJavaFileStatistics().getNumberOfMethods());

            } catch (Exception e) {
                 e.printStackTrace();
            }
         }
    }

    
    @FXML
    public void exportToJSON(ActionEvent event){

        // display directory chooser
        DirectoryChooser chooser = new DirectoryChooser();
        File defaultDirectory = new File("D:\\test");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(mainStage);

        if(selectedDirectory != null) {

            int exportedFiles = ContractManager.dataModel.exportToJSON(getSelected(), selectedDirectory);

            ContractManager.consoleWriter.writeNumberOfExportedFiles(exportedFiles);
        }
    }
}

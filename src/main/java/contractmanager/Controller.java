package contractmanager;

import cz.zcu.kiv.contractparser.model.JavaFile;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private Stage stage;



    public void initialize() {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void addFiles(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java", "*.java"),
                new FileChooser.ExtensionFilter("Class", "*.class")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null) {

            int addedFiles = 0;

            for(File file : files){
                if(Launcher.dataModel.addFile(file)){
                    addedFiles++;
                }
            }

            TextArea ta_console = (TextArea) Launcher.scene.lookup("#ta_console");

            String message;

            if(addedFiles == 0){
                message = "No files loaded.";
            }
            else if(addedFiles == 1){
                message = "1 file successfully loaded.";
            }
            else{
                message = addedFiles + " files successfully loaded.";
            }

            ta_console.appendText(message + "\n");

            updateFileList();
        }
    }

    @FXML
    public void removeFiles(ActionEvent event) {
        CheckListView clv_files = (CheckListView) Launcher.scene.lookup("#clv_files");

        // create list with indexes of checked files
        List<Integer> checkedIndexes = new ArrayList<>();

        for(int i = 0 ; i < clv_files.getItems().size() ; i++ ) {
            if(clv_files.getCheckModel().isChecked(i)) {
                checkedIndexes.add(i);
            }
        }

        // remove selected files
        Launcher.dataModel.removeFiles(checkedIndexes);
        
        updateFileList();
    }


    public void updateFileList() {

        // get CheckListView with list of java files names
        CheckListView clv_files = (CheckListView) Launcher.scene.lookup("#clv_files");

        // get current list with JavaFiles
        List<JavaFile> files = Launcher.dataModel.getFiles();

        // update list of files in checkListView
        final ObservableList<String> fileItems = FXCollections.observableArrayList();
        for (int i = 0; i < files.size(); i++) {

            if(files.get(i) != null){
                fileItems.add(files.get(i).getPath());
            }
        }
        clv_files.setItems(fileItems);

        // get number of files in total and number of currently selected
        int numberOfFilesTotal = clv_files.getItems().size();
        int numberOfFilesChecked = clv_files.getCheckModel().getCheckedIndices().size();
        updateSelected(numberOfFilesTotal, numberOfFilesChecked);

        // if there are no files - show label informing about empty list and disable select all check box
        Label lbl_list_empty = (Label) Launcher.scene.lookup("#lbl_list_empty");
        Button btn_select_all = (Button) Launcher.scene.lookup("#btn_select_all");

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


    public void updateDetails(CheckListView clv_files) {

        int selectedId = clv_files.getSelectionModel().getSelectedIndex();

        JavaFile selectedFile = Launcher.dataModel.getFiles().get(selectedId);
        Launcher.dataModel.setCurrentFile(selectedFile);

        Label lbl_filename_value = (Label) Launcher.scene.lookup("#lbl_filename_value");
        lbl_filename_value.setText(selectedFile.getFileName());

        Button btn_show_details = (Button) Launcher.scene.lookup("#btn_show_details");
        btn_show_details.setDisable(false);
    }


    public void updateSelected(int numberOfFilesTotal, int numberOfFilesChecked) {

        // update Select all check box (is checked only if all files are selected)
        Button btn_select_all = (Button) Launcher.scene.lookup("#btn_select_all");

        if(numberOfFilesTotal-numberOfFilesChecked == 0){
            btn_select_all.setText("Deselect All");
        }
        else{
            btn_select_all.setText("Select All");
        }

        // update info label about number of selected files
        Label lbl_selected = (Label) Launcher.scene.lookup("#lbl_selected");
        lbl_selected.setText("Files selected: " + numberOfFilesChecked + " / " + numberOfFilesTotal);
    }

    
    @FXML
    private void selectAll(ActionEvent event) {

        Button btn_select_all = (Button) Launcher.scene.lookup("#btn_select_all");
        CheckListView clv_files = (CheckListView) Launcher.scene.lookup("#clv_files");

        if(btn_select_all.getText().equals("Select All")) {
            clv_files.getCheckModel().checkAll();
        }
        else{
            clv_files.getCheckModel().clearChecks();
        }
    }


    @FXML
    public void showDetails(ActionEvent event){

        if(Launcher.dataModel.getCurrentFile() != null) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(Launcher.properties.getString("sceneDetailsFileName")));
                loader.setResources(Launcher.localization);
                Parent root = loader.load();

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                
                stage.setTitle(Launcher.localization.getString("windowTitleDetails") + " - " + Launcher.dataModel.getCurrentFile().getPath());
                stage.getIcons().add(new Image("contract_manager.png"));
                stage.show();

                TextArea ta_details = (TextArea) scene.lookup("#ta_details");
                ta_details.setText(Launcher.dataModel.getCurrentFile().toString());

                Label lbl_filename_value = (Label) scene.lookup("#lbl_filename_value");
                lbl_filename_value.setText(Launcher.dataModel.getCurrentFile().getFileName());

                Label lbl_file_type_value = (Label) scene.lookup("#lbl_file_type_value");
                lbl_file_type_value.setText(Launcher.dataModel.getCurrentFile().getFileType().toString());

                Label lbl_path_value = (Label) scene.lookup("#lbl_path_value");
                lbl_path_value.setText(Launcher.dataModel.getCurrentFile().getPath());
                
            } catch (Exception e) {
                 e.printStackTrace();
            }
         }
    }
}

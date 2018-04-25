package contractmanager.filelist;

import contractmanager.applicationTab.ApplicationTab;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.utils.IOServices;
import cz.zcu.kiv.contractparser.model.JavaFile;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileList {

    private LoadingWindow loadingWindow;
    private CheckListView checkListView;
    private ApplicationTab applicationTab;

    
    public FileList(LoadingWindow loadingWindow) {

        this.loadingWindow = loadingWindow;

        // get CheckListView with list of java files names
        checkListView = (CheckListView) ContractManager.scene.lookup("#clv_files");
    }


    /**
     * Parses contracts from all given files. JavaFiles are then loaded into program.
     * File processing is done via JavaFX task and during this action loading window is displayed which show current
     * progress on progress bar (each processed file updates progress bar).
     *
     * @param inputFiles     List of files to be parsed
     */
    public void addFiles(List<File> inputFiles, File selectedDirectory) {

        // get progress bar
        ProgressBar pb_loading = (ProgressBar) loadingWindow.getLoadingScene().lookup("#pb_loading");

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
                        if (ContractManager.extractorDataModel.addFile(file)) {
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
        task.setOnRunning((e) -> loadingWindow.getLoadingStage().show());

        // otherwise hide Loading window and update list
        task.setOnSucceeded((e) -> {
            loadingWindow.hide(this);
        });
        task.setOnFailed((e) -> {
            Throwable throwable = task.getException();
            throwable.printStackTrace();
            loadingWindow.hide(this);
        });
        task.setOnCancelled((e) -> {
            loadingWindow.hide(this);
        });

        new Thread(task).start();
    }


    public void removeFiles() {

        List<Integer> checkedIndexes = getSelected();

        // remove selected files
        int deletedFiles = ContractManager.extractorDataModel.removeFiles(checkedIndexes);

        ContractManager.consoleWriter.writeNumberOfDeletedFiles(deletedFiles);

        updateList();
    }


    public List<Integer> getSelected() {

        // create list with indexes of checked files
        List<Integer> checkedIndexes = new ArrayList<>();

        for(int i = 0 ; i < checkListView.getItems().size() ; i++ ) {
            if(checkListView.getCheckModel().isChecked(i)) {
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
    public void updateList() {

        ContractManager.extractorDataModel.updateShortPath();

        checkListView = (CheckListView) ContractManager.scene.lookup("#clv_files");

        // get current list with JavaFiles
        List<JavaFile> files = ContractManager.extractorDataModel.getFiles();

        // update list of files in checkListView
        final ObservableList<String> fileItems = FXCollections.observableArrayList();
        for (JavaFile file : files) {

            if (file != null) {
                fileItems.add(file.getShortPath());
            }
        }
        checkListView.setItems(fileItems);

        // get number of files in total and number of currently selected
        int numberOfFilesTotal = checkListView.getItems().size();
        int numberOfFilesChecked = checkListView.getCheckModel().getCheckedIndices().size();
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
        checkListView.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {

                // get number of files in total and number of currently selected
                int numberOfFilesTotal = checkListView.getItems().size();
                int numberOfFilesChecked = checkListView.getCheckModel().getCheckedIndices().size();
                updateSelected(numberOfFilesTotal, numberOfFilesChecked);
            }
        });


        // update details when item is highlighted using mouse click
        checkListView.setOnMouseClicked(event -> applicationTab.updateDetails());

        // update details when item is highlighted using arrow keys
        checkListView.setOnKeyReleased(event -> {
            KeyCode keyCode = event.getCode();
            if(keyCode.isArrowKey()) {
                applicationTab.updateDetails();
            }
        });

        applicationTab.updateGlobalStatistics();
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
        else if(numberOfFilesChecked > 0){
            btn_select_all.setText(ContractManager.localization.getString("buttonSelectAll"));
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
     */
    public void selectAll() {

        Button btn_select_all = (Button) ContractManager.scene.lookup("#btn_select_all");

        if(btn_select_all.getText().equals(ContractManager.localization.getString("buttonSelectAll"))) {
            checkListView.getCheckModel().checkAll();
        }
        else{
            checkListView.getCheckModel().clearChecks();
        }
    }


    public CheckListView getCheckListView() {
        return checkListView;
    }

    public void setApplicationTab(ApplicationTab applicationTab) {
        this.applicationTab = applicationTab;
    }
}

package contractmanager.presentation.filelist;

import contractmanager.presentation.applicationtab.LoadingWindow;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.model.JavaFile;
import cz.zcu.kiv.contractparser.utils.IOServices;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExtractorFileList implements FileList {

    private static final String CLV_SELECTOR = "#clvFilesExtractor";

    private CheckListView checkListView;

    private List<JavaFile> files;

    public ExtractorFileList() {
        super();
        //this.checkListView = (CheckListView) Utils.lookup(CLV_SELECTOR, ContractManager.getMainScene());
        this.files = new ArrayList<>();
    }



    /**
     * Parses contracts from all given files. JavaFiles are then loaded into program.
     * File processing is done via JavaFX task and during this action loading window is displayed which show current
     * progress on progress bar (each processed file updates progress bar).
     *
     * @param inputFiles     List of files to be parsed
     */
    public void addFiles(List<File> inputFiles, File selectedDirectory) {

        LoadingWindow loadingWindow = ContractManager.getApplicationData().getExtractorApplicationTab().getLoadingWindow();

        // get progress bar
        ProgressBar pb_loading = (ProgressBar) loadingWindow.getLoadingScene().lookup("#pbLoadingBar");

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
                        if (addFile(file)) {
                            addedFiles++;

                            // update progress bar
                            pb_loading.setProgress(pb_loading.getProgress() + progressIncrease);
                        }
                    } else {
                        // stop if action was cancelled
                        break;
                    }
                }

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


    private boolean addFile(File newFile) {

        // check whether this file is not already present
        boolean found = false;
        for(JavaFile javaFile : files) {

            if(javaFile == null){
                System.out.println("javaFile je NULL");
                return false;
            }

            if(newFile == null) {
                System.out.println("newFile je NULL");
                return false;
            }

            if(javaFile.getFullPath().equals(newFile.getAbsolutePath())) {
                found = true;
                break;
            }
        }


        // if the file is not in the list yet - add it
        if (!found) {
            JavaFile javaFile = ContractManager.getApplicationData().getExtractorApplicationTab().getContractExtractorApi().retrieveContracts(newFile, true);

            if(javaFile != null) {
                files.add(javaFile);
                ContractManager.getApplicationData().getExtractorApplicationTab().getGlobalStatistics().mergeStatistics(javaFile.getJavaFileStatistics());

                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }


    public int removeFiles() {

        List<Integer> checkedIndexes = getSelected();

        // remove selected files
        int deletedFiles = 0;

        for(int index : checkedIndexes){

            int newIndex = index - deletedFiles;

            if(newIndex < files.size()) {

                ContractManager.getApplicationData().getExtractorApplicationTab().getGlobalStatistics()
                        .detachStatistics(files.get(newIndex).getJavaFileStatistics());
                files.remove(newIndex);
                deletedFiles++;
            }
        }

        updateList();

        return deletedFiles;
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


    public List<JavaFile> getSelectedFiles() {

        List<JavaFile> javaFiles = new ArrayList<>();

        for(int i = 0 ; i < checkListView.getItems().size() ; i++ ) {
            if(checkListView.getCheckModel().isChecked(i)) {
                javaFiles.add(files.get(i));
            }
        }

        return javaFiles;
    }


    /**
     * This method will update file list when called. It is called whenever some change occurs such as adding or
     * removing file.
     *
     */
    public void updateList() {

        updateShortPath();

        checkListView = (CheckListView) ContractManager.scene.lookup(CLV_SELECTOR);

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
        Label lblExtractorListEmpty = (Label) ContractManager.scene.lookup("#lblExtractorListEmpty");
        Button btn_select_all = (Button) ContractManager.scene.lookup("#btnExtractorSelectAll");

        if(files.size() > 0){
            lblExtractorListEmpty.setVisible(false);
            btn_select_all.setDisable(false);
        }
        else {
            lblExtractorListEmpty.setVisible(true);
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
        checkListView.setOnMouseClicked(event -> ContractManager.getApplicationData().getExtractorApplicationTab()
                .updateFileDetails());

        // update details when item is highlighted using arrow keys
        checkListView.setOnKeyReleased(event -> {
            KeyCode keyCode = event.getCode();
            if(keyCode.isArrowKey()) {
                ContractManager.getApplicationData().getExtractorApplicationTab().updateFileDetails();
            }
        });

        ContractManager.getApplicationData().getExtractorApplicationTab().updateGlobalStatistics();
    }




    /**
     * This method updates (De)select All button. If there are some unselected files it has Select All label.
     * Otherwise it has Deselect All label. It also updates label informing about the number of selected files.
     *
     * @param numberOfFilesTotal        Total number of files
     * @param numberOfFilesChecked      Number of selected files
     */
    private void updateSelected(int numberOfFilesTotal, int numberOfFilesChecked) {

        // update Select all check box (is checked only if all files are selected)
        Button btn_select_all = (Button) ContractManager.scene.lookup("#btnExtractorSelectAll");

        Button btn_remove_files = (Button) ContractManager.scene.lookup("#btnExtractorRemoveFiles");
        Button btn_export_files = (Button) ContractManager.scene.lookup("#btnExportFilesExtractor");

        if(numberOfFilesTotal-numberOfFilesChecked == 0){
            btn_select_all.setText(ResourceHandler.getLocaleString("buttonDeselectAll"));
            btn_remove_files.setDisable(false);
            btn_export_files.setDisable(false);
        }
        else if(numberOfFilesChecked > 0){
            btn_select_all.setText(ResourceHandler.getLocaleString("buttonSelectAll"));
            btn_remove_files.setDisable(false);
            btn_export_files.setDisable(false);
        }
        else{
            btn_select_all.setText(ResourceHandler.getLocaleString("buttonSelectAll"));
            btn_remove_files.setDisable(true);
            btn_export_files.setDisable(true);
        }

        // update info label about number of selected files
        Label lbl_selected = (Label) ContractManager.scene.lookup("#lblExtractorSelected");
        lbl_selected.setText(ResourceHandler.getLocaleString("labelSelectedFiles") + ": "
                + numberOfFilesChecked + " / " + numberOfFilesTotal);
    }


    private void updateShortPath() {

        ContractManager.getApplicationData().getExtractorApplicationTab().getContractExtractorApi()
                .updateShortPathOfJavaFiles(files);
    }


    /**
     * This action is called when (De)Select All button is pressed. It does as it says its label. If it says Select All
     * it select all items. Otherwise it deselects all.
     */
    public void selectAll() {

        Button btnExtractorSelectAll = (Button) ContractManager.scene.lookup("#btnExtractorSelectAll");

        if(btnExtractorSelectAll.getText().equals(ResourceHandler.getLocaleString("buttonSelectAll"))) {
            checkListView.getCheckModel().checkAll();
        }
        else{
            checkListView.getCheckModel().clearChecks();
        }
    }

    public List<JavaFile> getFiles() {
        return files;
    }

    public CheckListView getCheckListView() {
        return checkListView;
    }
}

package contractmanager.presentation.filelist;

import contractmanager.presentation.applicationtab.LoadingWindow;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.JavaFileCompareReport;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.JavaFolderCompareReport;
import cz.zcu.kiv.contractparser.utils.IOServices;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.CheckListView;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComparatorFileList implements FileList {

    private static final String CLV_SELECTOR = "#clvFilesComparator";

    private CheckListView checkListView;

    private List<JavaFileCompareReport> reports;

    private File firstFolder;

    private File secondFolder;

    private boolean compared;


    public ComparatorFileList() {
        compared = false;
        //checkListView = (CheckListView) Utils.lookup(CLV_SELECTOR, ContractManager.getMainScene());
    }



    public void selectDirectory(File selectedDirectory, boolean isFirst) {

        // set first or second directory
        if(isFirst){
            firstFolder = selectedDirectory;
            setFolderLabel(selectedDirectory, "#lblFolder1Comparator");
        }
        else{
            secondFolder = selectedDirectory;
            setFolderLabel(selectedDirectory, "#lblFolder2Comparator");
        }

        // if both folders are set - enable compare button
        if(firstFolder != null && secondFolder != null && !compared){
            Button btnCompare = (Button) Utils.lookup("#btnCompare", ContractManager.getMainScene());
            btnCompare.setDisable(false);
        }
        // else if folders has been compared - clear the scene and set compared to false
        else if(compared){
            clearScene(isFirst);
            compared = false;
        }
    }

    private void setFolderLabel(File selectedDirectory, String selector){

        String path;

        if(selectedDirectory != null) {
            path = IOServices.getAbsolutePath(selectedDirectory);
        }
        else{
            path = ResourceHandler.getLocaleString("comparatorFolderNotSelect");
        }

        Label lblFolder = (Label) Utils.lookup(selector, ContractManager.getMainScene());
        lblFolder.setText(path);

        Tooltip tooltip = new Tooltip();
        tooltip.setText(path);
        lblFolder.setTooltip(tooltip);
    }


    public void compareFolders() {

        LoadingWindow loadingWindow = ContractManager.getApplicationData().getComparatorApplicationTab().getLoadingWindow();

        // start task
        Task<Boolean> task = new Task<Boolean>() {
            @Override public Boolean call() {

                // compare folders
                JavaFolderCompareReport javaFolderCompareReport = ContractManager.getApplicationData().
                        getComparatorApplicationTab().getContractComparatorApi().compareJavaFolders(
                        firstFolder, secondFolder, ContractManager.getApplicationData().getSettings().isReportEqual(),
                        ContractManager.getApplicationData().getSettings().isReportOnlyContractChanges());

                // save all reports
                ContractManager.getApplicationData().getComparatorApplicationTab().setFolderCompareReport(javaFolderCompareReport);
                reports = javaFolderCompareReport.getJavaFileCompareReports();

                // disable compare button to prevent unnecessary repeat
                Button btnCompare = (Button) Utils.lookup("#btnCompare", ContractManager.getMainScene());
                btnCompare.setDisable(true);
                compared = true;

                // TODO print addedReports.size()
                System.out.println("addedReports: " + reports.size());

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


    public void clearScene(boolean isFirst){

        // clear the other folder
        if(isFirst){
            secondFolder = null;
            setFolderLabel(null, "#lblFolder2Comparator");
        }
        else{
            firstFolder = null;
            setFolderLabel(null, "#lblFolder1Comparator");
        }

        // clear the report list
        reports = new ArrayList<>();
        updateList();



        // clear global statistics and report details
        ContractManager.getApplicationData().getComparatorApplicationTab().setFolderCompareReport(null);
        ContractManager.getApplicationData().getComparatorApplicationTab().setCurrentReport(null);
        ContractManager.getApplicationData().getComparatorApplicationTab().updateGlobalStatistics();
        ContractManager.getApplicationData().getComparatorApplicationTab().updateReportDetails();
    }


    @Override
    public void updateList() {

        //ContractManager.getApplicationData().getComparatorApplicationTab().getGlobalStatistics();

        checkListView = (CheckListView) ContractManager.scene.lookup(CLV_SELECTOR);

        // update list of reports in checkListView
        final ObservableList<String> reportItems = FXCollections.observableArrayList();
        for (JavaFileCompareReport report : reports) {

            if (report != null) {
                reportItems.add(report.getThisFilePath());
            }
        }

        checkListView.setItems(reportItems);

        // get number of files in total and number of currently selected
        int numberOfReportsTotal = checkListView.getItems().size();
        int numberOfReportsChecked = checkListView.getCheckModel().getCheckedIndices().size();
        updateSelected(numberOfReportsTotal, numberOfReportsChecked);

        // if there are no files - show label informing about empty list and disable select all check box
        Label lblComparatorListEmpty = (Label) ContractManager.scene.lookup("#lblComparatorListEmpty");
        Button btnComparatorSelectAll = (Button) ContractManager.scene.lookup("#btnComparatorSelectAll");

        if(reports.size() > 0){
            lblComparatorListEmpty.setVisible(false);
            btnComparatorSelectAll.setDisable(false);
        }
        else {
            lblComparatorListEmpty.setVisible(true);
            btnComparatorSelectAll.setDisable(true);
        }

        // set on CheckBox event
        checkListView.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {

                // get number of files in total and number of currently selected
                int numberOfReportsTotal = checkListView.getItems().size();
                int numberOfReportsChecked = checkListView.getCheckModel().getCheckedIndices().size();
                updateSelected(numberOfReportsTotal, numberOfReportsChecked);
            }
        });

        // update details when item is highlighted using mouse click
        checkListView.setOnMouseClicked(event -> ContractManager.getApplicationData().getComparatorApplicationTab()
                .updateReportDetails());

        // update details when item is highlighted using arrow keys
        checkListView.setOnKeyReleased(event -> {
            KeyCode keyCode = event.getCode();
            if(keyCode.isArrowKey()) {
                ContractManager.getApplicationData().getComparatorApplicationTab().updateReportDetails();
            }
        });

        ContractManager.getApplicationData().getComparatorApplicationTab().updateGlobalStatistics();
    }

    private void updateSelected(int numberOfReportsTotal, int numberOfReportsChecked) {

        // TODO
        System.out.println("updateSelected");
    }





    private void updateShortPath() {

        // TODO pridat metodu do ContractComparatorAPI
        //ContractManager.getApplicationData().getExtractorApplicationTab().getContractExtractorApi().updateShortPathOfJavaFiles(reports);
    }


    /**
     * This action is called when (De)Select All button is pressed. It does as it says its label. If it says Select All
     * it select all items. Otherwise it deselects all.
     */
    public void selectAll() {

        Button btnComparatorSelectAll = (Button) ContractManager.scene.lookup("#btnComparatorSelectAll");

        if(btnComparatorSelectAll.getText().equals(ResourceHandler.getLocaleString("buttonSelectAll"))) {
            checkListView.getCheckModel().checkAll();
        }
        else{
            checkListView.getCheckModel().clearChecks();
        }
    }


    @Override
    public List<Integer> getSelected() {
        System.out.println("TODO getSelected returns NULL");
        return null;
    }

    @Override
    public List<?> getSelectedFiles() {
        System.out.println("TODO getSelectedFiles returns NULL");
        return null;
    }

    @Override
    public CheckListView getCheckListView() {
        return checkListView;
    }

    @Override
    public List<?> getFiles() {
        return reports;
    }
}

package contractmanager.application.filelist;

import contractmanager.application.applicationtab.LoadingWindow;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.ContractManager;
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
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents file list for Comparator Application Tab. It contains JavaFileCompareReports. It takes care
 * of adding and removing files from the list and all things connected to its display.
 *
 * @author Vaclav Mares
 */
public class ComparatorFileList implements FileList {

    /** Log4j logger for this class */
    private final static Logger logger = Logger.getLogger(String.valueOf(ComparatorFileList.class));

    /** CheckListView as a graphics representation of this FileList */
    private CheckListView checkListView;

    /** Selector for this file list to avoid multiple raw Strings */
    private static final String CLV_SELECTOR = "#clvFilesComparator";

    /** List of reports that are displayed in the CheckListView */
    private List<ReportItem> reports;

    /** First folder of the comparison */
    private File firstFolder;

    /** Second folder of the comparison */
    private File secondFolder;

    /** Flags whether or not folders has been already compared */
    private boolean compared;


    public ComparatorFileList() {
        compared = false;
        reports = new ArrayList<>();
    }


    /**
     * Selects one of the two folders needed form comparison. It updates labels and also other controls depending
     * whether comparison already happened or if the other folder is also selected.
     *
     * @param selectedDirectory     Selected directory to be displayed
     * @param isFirst               Whether this is the first folder (true) os second (false)
     */
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


    /**
     * Sets label for selected folder.
     *
     * @param selectedDirectory     Selected folder
     * @param selector              UI selector for the label
     */
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


    /**
     * Compare Java files from both selected folders. This option is enabled only if both folders are specified.
     * This action is connected with loading window which is shown during the job.
     */
    public void compareFolders() {

        LoadingWindow loadingWindow = ContractManager.getApplicationData().getComparatorApplicationTab().getLoadingWindow();

        // start task
        Task<Boolean> task = new Task<Boolean>() {
            @Override public Boolean call() {

                // compare folders
                JavaFolderCompareReport javaFolderCompareReport = ContractManager.getApplicationData().
                        getComparatorApplicationTab().getContractComparatorApi().compareJavaFolders(
                        firstFolder, secondFolder, true, true);

                // save all reports
                ContractManager.getApplicationData().getComparatorApplicationTab().setFolderCompareReport(javaFolderCompareReport);
                List<JavaFileCompareReport> javaFileCompareReports = javaFolderCompareReport.getJavaFileCompareReports();

                reports = new ArrayList<>();
                for(JavaFileCompareReport javaFileCompareReport : javaFileCompareReports) {

                    ReportItem reportItem = new ReportItem(javaFileCompareReport);
                    reportItem.updateVisibility();
                    reports.add(reportItem);

                }

                // disable compare button to prevent unnecessary repeat
                Button btnCompare = (Button) Utils.lookup("#btnCompare", ContractManager.getMainScene());
                btnCompare.setDisable(true);
                compared = true;

                logger.info(ResourceHandler.getLocaleString("infoReportsAdded", reports.size()));

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


    /**
     * This method clears the scene after new folder has been selected. It clears stats, details and the other folder.
     *
     * @param isFirst   If first folder has been selected to init this clear
     */
    private void clearScene(boolean isFirst){

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


    /**
     * Updates list of reports. Based on the state of list it adjust other controls for instance hides/show
     * list is empty label disables/enables buttons etc.
     */
    public void updateList() {

        checkListView = (CheckListView) ContractManager.scene.lookup(CLV_SELECTOR);

        // update list of reports in checkListView
        final ObservableList<String> reportItems = FXCollections.observableArrayList();
        for (ReportItem report : reports) {

            if (report != null) {
                report.updateVisibility();

                if(report.isVisible()) {
                    reportItems.add(report.getReport().getThisFilePath());
                }
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


    /**
     * This method updates (De)select All button. If there are some unselected files it has Select All label.
     * Otherwise it has Deselect All label. It also updates label informing about the number of selected files.
     *
     * @param numberOfFilesTotal        Total number of files
     * @param numberOfFilesChecked      Number of selected files
     */
    private void updateSelected(int numberOfFilesTotal, int numberOfFilesChecked) {

        // update Select all check box (is checked only if all files are selected)
        Button btnSelectAll = (Button) ContractManager.scene.lookup("#btnComparatorSelectAll");
        Button btnExportFiles = (Button) ContractManager.scene.lookup("#btnExportFilesComparator");

        if(numberOfFilesTotal-numberOfFilesChecked == 0){
            btnSelectAll.setText(ResourceHandler.getLocaleString("buttonDeselectAll"));
            btnExportFiles.setDisable(false);
        }
        else if(numberOfFilesChecked > 0){
            btnSelectAll.setText(ResourceHandler.getLocaleString("buttonSelectAll"));
            btnExportFiles.setDisable(false);
        }
        else{
            btnSelectAll.setText(ResourceHandler.getLocaleString("buttonSelectAll"));
            btnExportFiles.setDisable(true);
        }

        // update info label about number of selected files
        Label lbSelected = (Label) ContractManager.scene.lookup("#lblComparatorSelected");
        lbSelected.setText(ResourceHandler.getLocaleString("labelSelectedFiles") + ": "
                + numberOfFilesChecked + " / " + numberOfFilesTotal);
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


    // Getters and Setters
    public CheckListView getCheckListView() {
        return checkListView;
    }

    public List<ReportItem> getFiles() {
        return reports;
    }
}
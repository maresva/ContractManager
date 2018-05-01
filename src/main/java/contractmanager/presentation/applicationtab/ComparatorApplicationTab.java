package contractmanager.presentation.applicationtab;

import contractmanager.presentation.Settings;
import contractmanager.presentation.filelist.ComparatorFileList;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.api.ApiFactory;
import cz.zcu.kiv.contractparser.api.ContractComparatorApi;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.JavaFileCompareReport;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.JavaFolderCompareReport;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.JavaFolderCompareStatistics;
import cz.zcu.kiv.contractparser.model.ContractType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;

import java.util.Map;


public class ComparatorApplicationTab extends ApplicationTab {

    /** Object containing CheckListView with list of files */
    private ComparatorFileList fileList;

    private ContractComparatorApi contractComparatorApi;

    private JavaFolderCompareReport folderCompareReport;

    private JavaFileCompareReport currentReport;


    public ComparatorApplicationTab() {
        super();

        ApiFactory apiFactory = new ApiFactory();
        contractComparatorApi = apiFactory.getContractComparatorApi();
        fileList = new ComparatorFileList();
        loadingWindow = new LoadingWindow(ResourceHandler.getProperties().getString("sceneComparatorLoadingFileName"));
    }

    public void initScene() {

        // get filter tool bar to add contract types
        ToolBar tbFilterComparator = (ToolBar) Utils.lookup("#tbFilterComparator", ContractManager.getMainScene());

        ToggleButton btnToggleMinJsonComparator = (ToggleButton) Utils.lookup("#btnToggleMinJsonComparator",
                ContractManager.getMainScene());
        ToggleButton btnToggleShowNonContractObjectsComparator = (ToggleButton) Utils.lookup(
                "#btnToggleShowNonContractObjectsComparator", ContractManager.getMainScene());
        ToggleButton btnToggleReportOnlyContractChanges = (ToggleButton) Utils.lookup(
                "#btnToggleReportOnlyContractChanges", ContractManager.getMainScene());
        ToggleButton btnToggleReportEqual = (ToggleButton) Utils.lookup(
                "#btnToggleReportEqual", ContractManager.getMainScene());

        Settings settings = ContractManager.getApplicationData().getSettings();

        if(settings.isMinJson()){
            btnToggleMinJsonComparator.setSelected(true);
        }
        else{
            btnToggleMinJsonComparator.setSelected(false);
        }

        if(settings.isShowNonContractObjects()){
            btnToggleShowNonContractObjectsComparator.setSelected(true);
        }
        else{
            btnToggleShowNonContractObjectsComparator.setSelected(false);
        }

        if(settings.isReportOnlyContractChanges()){
            btnToggleReportOnlyContractChanges.setSelected(true);
        }
        else{
            btnToggleReportOnlyContractChanges.setSelected(false);
        }

        if(settings.isReportEqual()){
            btnToggleReportEqual.setSelected(true);
        }
        else{
            btnToggleReportEqual.setSelected(false);
        }

        int tbIndex = 2;

        // go through each contract type
        for(Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings()
                .getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();
            boolean contractTypeIsShown = entry.getValue();

            // prepare toggle buttons for tool bar filter
            ToggleButton toggleButtonComparator = new ToggleButton();
            toggleButtonComparator.setText(contractType.name());
            toggleButtonComparator.setFocusTraversable(false);

            if(contractTypeIsShown) {
                // toggle button if shown
                toggleButtonComparator.setSelected(true);
            }

            // add buttons to filter tool bars
            tbFilterComparator.getItems().add(tbIndex, toggleButtonComparator);

            tbIndex++;
        }
    }


    /**
     * Updates labels in table containing global statistics about the comparison. If the data are null it clears
     * the table instead.
     */
    public void updateGlobalStatistics() {

        Label lblContractEqual = (Label) Utils.lookup("#lblComparatorStatsContractEqualValue", ContractManager.getMainScene());
        Label lblApiEqual = (Label) Utils.lookup("#lblComparatorStatsApiEqualValue", ContractManager.getMainScene());
        Label lblFilesAdded = (Label) Utils.lookup("#lblComparatorStatsFilesAddedValue", ContractManager.getMainScene());
        Label lblFilesRemoved = (Label) Utils.lookup("#lblComparatorStatsFilesRemovedValue", ContractManager.getMainScene());
        Label lblContractsAdded = (Label) Utils.lookup("#lblComparatorStatsContractsAddedValue", ContractManager.getMainScene());
        Label lblContractsRemoved = (Label) Utils.lookup("#lblComparatorStatsContractsRemovedValue", ContractManager.getMainScene());

        // if folderReport and statistics are not null - fill in data - otherwise clear the table
        if(folderCompareReport != null && folderCompareReport.getJavaFolderCompareStatistics() != null) {

            JavaFolderCompareStatistics globalStatistics = folderCompareReport.getJavaFolderCompareStatistics();

            lblContractEqual.setText("" + folderCompareReport.isContractEqual());
            lblApiEqual.setText("" + folderCompareReport.isApiEqual());
            lblFilesAdded.setText("" + globalStatistics.getFilesAdded());
            lblFilesRemoved.setText("" + globalStatistics.getFilesRemoved());
            lblContractsAdded.setText("" + globalStatistics.getContractsAdded());
            lblContractsRemoved.setText("" + globalStatistics.getContractsRemoved());
        }
        else{
            String notDefined = "-";
            lblContractEqual.setText(notDefined);
            lblApiEqual.setText(notDefined);
            lblFilesAdded.setText(notDefined);
            lblFilesRemoved.setText(notDefined);
            lblContractsAdded.setText(notDefined);
            lblContractsRemoved.setText(notDefined);
        }
    }




    public void updateReportDetails() {

        Label lblFile = (Label) Utils.lookup("#lblComparatorFileDetailsValue", ContractManager.getMainScene());
        Label lblContractEqual = (Label) Utils.lookup("#lblComparatorDetailsContractEqualValue", ContractManager.getMainScene());
        Label lblApiEqual = (Label) Utils.lookup("#lblComparatorDetailsApiEqualValue", ContractManager.getMainScene());

        Button btnShowDetails = (Button) ContractManager.getMainScene().lookup("#btnShowDetailsComparator");

        // get selected file and save it into presentation model
        int selectedId = fileList.getCheckListView().getSelectionModel().getSelectedIndex();

        if(selectedId >= 0 && selectedId < fileList.getFiles().size()) {

            currentReport = (JavaFileCompareReport) fileList.getFiles().get(selectedId);

            // update labels
            lblFile.setText(currentReport.getThisFilePath());
            lblContractEqual.setText("" + currentReport.isContractEqual());
            lblApiEqual.setText("" + currentReport.isApiEqual());

            // once some file is selected - Show details button becomes available
            btnShowDetails.setDisable(false);
        }
        else{
            String notDefined = "-";
            lblFile.setText(notDefined);
            lblContractEqual.setText(notDefined);
            lblApiEqual.setText(notDefined);
            btnShowDetails.setDisable(true);
        }
    }



    public ComparatorFileList getFileList() {
        return fileList;
    }

    public ContractComparatorApi getContractComparatorApi() {
        return contractComparatorApi;
    }

    public JavaFolderCompareReport getFolderCompareReport() {
        return folderCompareReport;
    }

    public void setFolderCompareReport(JavaFolderCompareReport folderCompareReport) {
        this.folderCompareReport = folderCompareReport;
    }

    public void setCurrentReport(JavaFileCompareReport currentReport) {
        this.currentReport = currentReport;
    }
}

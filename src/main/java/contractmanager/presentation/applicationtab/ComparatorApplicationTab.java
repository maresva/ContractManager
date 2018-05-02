package contractmanager.presentation.applicationtab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import contractmanager.presentation.Settings;
import contractmanager.presentation.filelist.ComparatorFileList;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.api.ApiFactory;
import cz.zcu.kiv.contractparser.api.ContractComparatorApi;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.*;
import cz.zcu.kiv.contractparser.model.ContractType;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
        //ToggleButton btnToggleShowNonContractObjectsComparator = (ToggleButton) Utils.lookup("#btnToggleShowNonContractObjectsComparator", ContractManager.getMainScene());
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

        /*
        if(settings.isShowNonContractObjects()){
            btnToggleShowNonContractObjectsComparator.setSelected(true);
        }
        else{
            btnToggleShowNonContractObjectsComparator.setSelected(false);
        }*/

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

            Tooltip tooltip = new Tooltip(ResourceHandler.getLocaleString("tooltipShowContractsOfType",contractType.name()));
            toggleButtonComparator.setTooltip(tooltip);

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
        Label lblContractsChanged = (Label) Utils.lookup("#lblComparatorStatsContractsChangedValue", ContractManager.getMainScene());

        // if folderReport and statistics are not null - fill in data - otherwise clear the table
        if(folderCompareReport != null && folderCompareReport.getJavaFolderCompareStatistics() != null) {

            JavaFolderCompareStatistics globalStatistics = folderCompareReport.getJavaFolderCompareStatistics();

            lblContractEqual.setText("" + folderCompareReport.isContractEqual());
            lblApiEqual.setText("" + folderCompareReport.isApiEqual());
            lblFilesAdded.setText("" + globalStatistics.getFilesAdded());
            lblFilesRemoved.setText("" + globalStatistics.getFilesRemoved());
            lblContractsAdded.setText("" + globalStatistics.getContractsAdded());
            lblContractsRemoved.setText("" + globalStatistics.getContractsRemoved());
            lblContractsChanged.setText("" + globalStatistics.getContractsChanged());
        }
        else{
            String notDefined = "-";
            lblContractEqual.setText(notDefined);
            lblApiEqual.setText(notDefined);
            lblFilesAdded.setText(notDefined);
            lblFilesRemoved.setText(notDefined);
            lblContractsAdded.setText(notDefined);
            lblContractsRemoved.setText(notDefined);
            lblContractsChanged.setText(notDefined);
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
            lblFile.setText(ResourceHandler.getLocaleString("labelFileDefaultValue"));
            lblContractEqual.setText(notDefined);
            lblApiEqual.setText(notDefined);
            btnShowDetails.setDisable(true);
        }
    }


    /**
     * This method is called when user chooses to see details about selected files. It shows new window with details
     * and fills it with values. It show basic information such filename or path as well as statistics about
     * number of contracts etc. It also shows the export structure of selected file.
     */
    public void showDetailsWindow(){

        if(currentReport != null) {

            String sceneFXMLName = ResourceHandler.getProperties().getString("sceneDetailsComparatorFileName");
            String windowName = ResourceHandler.getLocaleString("windowTitleDetails") + " - "
                    + currentReport.getThisFilePath();

            super.prepareDetailsWindow(sceneFXMLName, windowName);

            // get selected java file and convert it to JSON
            Gson gson = new Gson();
            String jsonInString = gson.toJson(currentReport);

            // object in JSON convert to "pretty print" string
            JsonParser parser = new JsonParser();
            gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement el = parser.parse(jsonInString);
            jsonInString = gson.toJson(el);

            TextArea taJson = (TextArea) Utils.lookup("#taJson", detailsScene);
            taJson.setText(jsonInString);

            Label lblThisFileValue = (Label) Utils.lookup("#lblThisFileValue", detailsScene);
            lblThisFileValue.setText("" + currentReport.getThisFilePath());

            Label lblOtherFileValue = (Label) Utils.lookup("#lblOtherFileValue", detailsScene);
            lblOtherFileValue.setText("" + currentReport.getOtherFilePath());

            Label lblContractEqual = (Label) Utils.lookup("#lblContractEqualValue", detailsScene);
            lblContractEqual.setText("" + currentReport.isContractEqual());

            Label lblApiEqual = (Label) Utils.lookup("#lblApiEqualValue", detailsScene);
            lblApiEqual.setText("" + currentReport.isApiEqual());

            Label lblContractsChanged = (Label) Utils.lookup("#lblContractsChangedValue", detailsScene);
            lblContractsChanged.setText("" + currentReport.getJavaFileCompareStatistics().getContractsChanged());

            Label lblContractsAdded = (Label) Utils.lookup("#lblContractsAddedValue", detailsScene);
            lblContractsAdded.setText("" + currentReport.getJavaFileCompareStatistics().getContractsAdded());

            Label lblContractsRemoved = (Label) Utils.lookup("#lblContractsRemovedValue", detailsScene);
            lblContractsRemoved.setText("" + currentReport.getJavaFileCompareStatistics().getContractsRemoved());

            Label lblMethodsAdded = (Label) Utils.lookup("#lblMethodsAddedValue", detailsScene);
            lblMethodsAdded.setText("" + currentReport.getJavaFileCompareStatistics().getMethodsAdded());

            Label lblMethodsRemoved = (Label) Utils.lookup("#lblMethodsRemovedValue", detailsScene);
            lblMethodsRemoved.setText("" + currentReport.getJavaFileCompareStatistics().getMethodsRemoved());

            Label lblClassesAdded = (Label) Utils.lookup("#lblClassesAddedValue", detailsScene);
            lblClassesAdded.setText("" + currentReport.getJavaFileCompareStatistics().getClassesAdded());

            Label lblClassesRemoved = (Label) Utils.lookup("#lblClassesRemovedValue", detailsScene);
            lblClassesRemoved.setText("" + currentReport.getJavaFileCompareStatistics().getClassesRemoved());

            TreeView<String> treeView = (TreeView<String>) Utils.lookup("#tvContractDetails", detailsScene);
            prepareTreeView(treeView);
        }
    }


    private void prepareTreeView(TreeView<String> treeView){

        TreeItem<String> tiJavaFileCompareReport = new TreeItem<>(ResourceHandler.getLocaleString("javaFileCompareReport"));
        tiJavaFileCompareReport.setExpanded(true);

        TreeItem<String> tiApiChanges = new TreeItem<>(ResourceHandler.getLocaleString("apiChanges"));
        tiApiChanges.setExpanded(true);

        for(ApiChange apiChange : currentReport.getApiChanges()) {

            TreeItem<String> tiApiChange = new TreeItem<>(apiChange.getApiType() + " - " + apiChange.getApiState());
            tiApiChange.setExpanded(true);

            TreeItem<String> tiApiChangeSignature = new TreeItem<>(ResourceHandler.getLocaleString(
                    "signature") + " " + apiChange.getSignature());
            tiApiChange.getChildren().add(tiApiChangeSignature);

            TreeItem<String> tiApiChangeContracts = new TreeItem<>(ResourceHandler.getLocaleString(
                    "contractsAffected") + " " + apiChange.getNumberOfContracts());
            tiApiChange.getChildren().add(tiApiChangeContracts);

            tiApiChanges.getChildren().add(tiApiChange);
        }

        tiJavaFileCompareReport.getChildren().add(tiApiChanges);

        TreeItem<String> tiCompareReports = new TreeItem<>(ResourceHandler.getLocaleString("contractCompareReports"));
        tiCompareReports.setExpanded(true);

        for(ContractCompareReport compareReport: currentReport.getContractCompareReports()){

            TreeItem<String> tiCompareReport = new TreeItem<>(createCompareReportTitle(compareReport));
            tiCompareReport.setExpanded(true);

            TreeItem<String> tiClass = new TreeItem<>(ResourceHandler.getLocaleString("treeClass")
                    + " " + compareReport.getClassName());
            tiCompareReport.getChildren().add(tiClass);

            TreeItem<String> tiMethod = new TreeItem<>(ResourceHandler.getLocaleString("treeMethod")
                    + " " + compareReport.getMethodName());
            tiCompareReport.getChildren().add(tiMethod);

            TreeItem<String> tiThisContract = new TreeItem<>(ResourceHandler.getLocaleString("treeThisContract")
                    + " " + compareReport.getThisContractExpression());
            tiCompareReport.getChildren().add(tiThisContract);

            TreeItem<String> tiOtherContract = new TreeItem<>(ResourceHandler.getLocaleString("treeOtherContract")
                    + " " + compareReport.getOtherContractExpression());
            tiCompareReport.getChildren().add(tiOtherContract);

            tiCompareReports.getChildren().add(tiCompareReport);
        }

        tiJavaFileCompareReport.getChildren().add(tiCompareReports);
        treeView.setRoot(tiJavaFileCompareReport);
    }


    private String createCompareReportTitle(ContractCompareReport compareReport) {

        String title = ResourceHandler.getLocaleString("treeContract") + " - ";

        if(compareReport.getApiState() == ApiState.FOUND_PAIR){
            title += compareReport.getContractComparison();
        }
        else{
            title += compareReport.getApiState();
        }

        return title;
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

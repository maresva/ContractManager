package contractmanager.application.applicationtab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import contractmanager.application.Settings;
import contractmanager.application.filelist.ComparatorFileList;
import contractmanager.application.filelist.ReportItem;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.ContractManager;
import cz.zcu.kiv.contractparser.api.ApiFactory;
import cz.zcu.kiv.contractparser.api.ContractComparatorApi;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.*;
import cz.zcu.kiv.contractparser.model.ContractType;
import javafx.geometry.Insets;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Map;


/**
 * Class representing Comparator application tab. It contains the file list, currently selected report, api which
 * provides ContractParser methods etc.
 *
 * @author Vaclav Mares
 */
public class ComparatorApplicationTab extends ApplicationTab {

    /** Provides methods form ContractParser that are used for contract comparison */
    private ContractComparatorApi contractComparatorApi;

    /** Object containing CheckListView with list of files */
    private ComparatorFileList fileList;

    /** Current JavaFolderCompareReport that is displayed at right bottom. It also opened in details window */
    private ReportItem currentReport;

    /** Compare report of current two folders. Among others contains reports and statistics */
    private JavaFolderCompareReport folderCompareReport;


    public ComparatorApplicationTab() {
        super();

        ApiFactory apiFactory = new ApiFactory();
        contractComparatorApi = apiFactory.getContractComparatorApi();
        fileList = new ComparatorFileList();
        loadingWindow = new LoadingWindow(ResourceHandler.getProperties().getString("sceneComparatorLoadingFileName"));
    }


    /**
     * Prepares scene at the start of the application.
     */
    public void initScene() {

        // get filter tool bar to add contract types
        ToolBar tbFilterComparator = (ToolBar) Utils.lookup("#tbFilterComparator", ContractManager.getMainScene());

        CheckBox chBoxMinJson = (CheckBox) Utils.lookup("#btnToggleMinJsonComparator", ContractManager.getMainScene());
        CheckBox chBoxReportOnlyContractChanges = (CheckBox) Utils.lookup("#btnToggleReportOnlyContractChanges", ContractManager.getMainScene());
        CheckBox chBoxReportEqual = (CheckBox) Utils.lookup("#btnToggleReportEqual", ContractManager.getMainScene());

        Settings settings = ContractManager.getApplicationData().getSettings();

        if(settings.isMinJson()){
            chBoxMinJson.setSelected(true);
        }
        else{
            chBoxMinJson.setSelected(false);
        }

        if(settings.isReportOnlyContractChanges()){
            chBoxReportOnlyContractChanges.setSelected(true);
        }
        else{
            chBoxReportOnlyContractChanges.setSelected(false);
        }

        if(settings.isReportEqual()){
            chBoxReportEqual.setSelected(true);
        }
        else{
            chBoxReportEqual.setSelected(false);
        }

        int tbIndex = 2;

        // go through each contract type
        for(Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings()
                .getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();
            boolean contractTypeIsShown = entry.getValue();

            // prepare toggle buttons for tool bar filter
            CheckBox checkBox = new CheckBox();
            checkBox.setText(contractType.name());
            checkBox.setFocusTraversable(false);
            checkBox.setId("chBoxContractComparator" + contractType);
            int padding = Integer.parseInt(ResourceHandler.getProperties().getString("filterPadding"));
            checkBox.setPadding(new Insets(0, padding, 0, 0));


            Tooltip tooltip = new Tooltip(ResourceHandler.getLocaleString("tooltipShowContractsOfType",contractType.name()));
            checkBox.setTooltip(tooltip);

            if(contractTypeIsShown) {
                // toggle button if shown
                checkBox.setSelected(true);
            }

            // add buttons to filter tool bars
            tbFilterComparator.getItems().add(tbIndex, checkBox);

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

            //JavaFolderCompareStatistics globalStatistics = folderCompareReport.getJavaFolderCompareStatistics();

            JavaFolderCompareStatistics globalStatistics = new JavaFolderCompareStatistics(
                    folderCompareReport.getJavaFolderCompareStatistics().getFilesAdded(),
                    folderCompareReport.getJavaFolderCompareStatistics().getFilesRemoved(),
                    0, 0, 0);

            for(ReportItem reportItem : fileList.getFiles()){

                if(reportItem.isVisible()){
                    globalStatistics.mergeFileStatistics(reportItem.getReport());
                }
            }


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


    /**
     * This method updates details in the right part of window each time user selects different file.
     */
    public void updateReportDetails() {

        Label lblFile = (Label) Utils.lookup("#lblComparatorFileDetailsValue", ContractManager.getMainScene());
        Label lblContractEqual = (Label) Utils.lookup("#lblComparatorDetailsContractEqualValue", ContractManager.getMainScene());
        Label lblApiEqual = (Label) Utils.lookup("#lblComparatorDetailsApiEqualValue", ContractManager.getMainScene());

        Button btnShowDetails = (Button) ContractManager.getMainScene().lookup("#btnShowDetailsComparator");

        // get selected file and save it into application model
        int selectedId = fileList.getCheckListView().getSelectionModel().getSelectedIndex();

        if(selectedId >= 0 && selectedId < fileList.getFiles().size()) {

            currentReport = fileList.getFiles().get(selectedId);

            // update labels
            lblFile.setText(currentReport.getReport().getThisFilePath());
            lblContractEqual.setText("" + currentReport.getReport().isContractEqual());
            lblApiEqual.setText("" + currentReport.getReport().isApiEqual());

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
                    + currentReport.getReport().getThisFilePath();

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
            lblThisFileValue.setText("" + currentReport.getReport().getThisFilePath());

            Label lblOtherFileValue = (Label) Utils.lookup("#lblOtherFileValue", detailsScene);
            lblOtherFileValue.setText("" + currentReport.getReport().getOtherFilePath());

            Label lblContractEqual = (Label) Utils.lookup("#lblContractEqualValue", detailsScene);
            lblContractEqual.setText("" + currentReport.getReport().isContractEqual());

            Label lblApiEqual = (Label) Utils.lookup("#lblApiEqualValue", detailsScene);
            lblApiEqual.setText("" + currentReport.getReport().isApiEqual());

            Label lblContractsChanged = (Label) Utils.lookup("#lblContractsChangedValue", detailsScene);
            lblContractsChanged.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getContractsChanged());

            Label lblContractsAdded = (Label) Utils.lookup("#lblContractsAddedValue", detailsScene);
            lblContractsAdded.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getContractsAdded());

            Label lblContractsRemoved = (Label) Utils.lookup("#lblContractsRemovedValue", detailsScene);
            lblContractsRemoved.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getContractsRemoved());

            Label lblMethodsAdded = (Label) Utils.lookup("#lblMethodsAddedValue", detailsScene);
            lblMethodsAdded.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getMethodsAdded());

            Label lblMethodsRemoved = (Label) Utils.lookup("#lblMethodsRemovedValue", detailsScene);
            lblMethodsRemoved.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getMethodsRemoved());

            Label lblClassesAdded = (Label) Utils.lookup("#lblClassesAddedValue", detailsScene);
            lblClassesAdded.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getClassesAdded());

            Label lblClassesRemoved = (Label) Utils.lookup("#lblClassesRemovedValue", detailsScene);
            lblClassesRemoved.setText("" + currentReport.getReport().getJavaFileCompareStatistics().getClassesRemoved());

            TreeView<String> treeView = (TreeView<String>) Utils.lookup("#tvContractDetails", detailsScene);
            prepareTreeView(treeView);
        }
    }


    /**
     * Prepares Tree view in the Details window. It contains information from current JavaFileCompareReport and its
     * affected by current filters. It also merges some information for better display.
     *
     * @param treeView  TreeView to be prepared
     */
    private void prepareTreeView(TreeView<String> treeView){

        boolean reportOnlyContractChanges = ContractManager.getApplicationData().getSettings().isReportOnlyContractChanges();
        boolean reportEqual = ContractManager.getApplicationData().getSettings().isReportEqual();
        HashMap<ContractType, Boolean> contractTypes = ContractManager.getApplicationData().getSettings().getContractTypes();

        TreeItem<String> tiJavaFileCompareReport = new TreeItem<>(ResourceHandler.getLocaleString("javaFileCompareReport"));
        tiJavaFileCompareReport.setExpanded(true);

        TreeItem<String> tiApiChanges = new TreeItem<>(ResourceHandler.getLocaleString("apiChanges"));
        tiApiChanges.setExpanded(true);

        if(!currentReport.getReport().getApiChanges().isEmpty() && !reportOnlyContractChanges){
            for (ApiChange apiChange : currentReport.getReport().getApiChanges()) {

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
        }

        if(tiApiChanges.getChildren().isEmpty()){
            tiApiChanges.setValue(tiApiChanges.getValue() + " - " + ResourceHandler.getLocaleString("treeEmpty"));
        }

        tiJavaFileCompareReport.getChildren().add(tiApiChanges);

        TreeItem<String> tiCompareReports = new TreeItem<>(ResourceHandler.getLocaleString("contractCompareReports"));
        tiCompareReports.setExpanded(true);

        for(ContractCompareReport compareReport: currentReport.getReport().getContractCompareReports()){

            if(compareReport.getContractComparison() != ContractComparison.EQUAL || reportEqual) {

                if(contractTypes.get(compareReport.getContractType())) {

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
            }
        }

        if(tiCompareReports.getChildren().isEmpty()){
            tiCompareReports.setValue(tiCompareReports.getValue() + " - " + ResourceHandler.getLocaleString("treeEmpty"));
        }

        tiJavaFileCompareReport.getChildren().add(tiCompareReports);
        treeView.setRoot(tiJavaFileCompareReport);
    }


    /**
     * Prepares title for ContractCompareReport by merging two attributes for better human readability
     *
     * @param compareReport     ContractCompareReport for which the title is created
     * @return                  String with prepared title
     */
    private String createCompareReportTitle(ContractCompareReport compareReport) {

        String title = ResourceHandler.getLocaleString("treeContract") + " (" + compareReport.getContractType() + ") - ";

        if(compareReport.getApiState() == ApiState.FOUND_PAIR){
            title += compareReport.getContractComparison();
        }
        else{
            title += compareReport.getApiState();
        }

        return title;
    }
          

    // Getters and Setters
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

    public void setCurrentReport(ReportItem currentReport) {
        this.currentReport = currentReport;
    }
}
package contractmanager.application.applicationtab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import contractmanager.application.Settings;
import contractmanager.application.filelist.ExtractorFileList;
import contractmanager.application.filelist.JavaFileItem;
import contractmanager.utility.ResourceHandler;
import contractmanager.utility.Utils;
import contractmanager.ContractManager;
import cz.zcu.kiv.contractparser.api.ApiFactory;
import cz.zcu.kiv.contractparser.api.ContractExtractorApi;
import cz.zcu.kiv.contractparser.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing Extractor application tab. It contains the file list, currently selected file, api which
 * provides ContractParser methods etc.
 *
 * @author Vaclav Mares
 */
public class ExtractorApplicationTab extends ApplicationTab{

    /** Provides methods form ContractParser that are used for contract extracting */
    private ContractExtractorApi contractExtractorApi;

    /** Object containing CheckListView with list of files */
    private ExtractorFileList fileList;

    /** Current JavaFileItem that is displayed at right bottom. It also opened in details window */
    private JavaFileItem currentJavaFile;

    /** Global statistics created from the current file list */
    private JavaFileStatistics globalStatistics;


    public ExtractorApplicationTab() {
        super();

        ApiFactory apiFactory = new ApiFactory();
        contractExtractorApi = apiFactory.getContractExtractorApi();

        globalStatistics = new JavaFileStatistics();

        fileList = new ExtractorFileList();
        loadingWindow = new LoadingWindow(ResourceHandler.getProperties().getString("sceneExtractorLoadingFileName"));
    }


    /**
     * Prepares scene at the start of the application.
     */
    public void initScene() {

        // get Details and Statistics tables to add contract types
        GridPane gridDetailsExtractor = (GridPane) Utils.lookup("#gridDetailsExtractor", ContractManager.getMainScene());
        GridPane gridGlobalStatsExtractor = (GridPane) Utils.lookup("#gridGlobalStatsExtractor", ContractManager.getMainScene());

        // get filter tool bar to add contract types
        ToolBar tbFilterExtractor = (ToolBar) Utils.lookup("#tbFilterExtractor", ContractManager.getMainScene());

        CheckBox chBoxMinJson= (CheckBox) Utils.lookup("#btnToggleMinJsonExtractor", ContractManager.getMainScene());
        CheckBox chBoxShowNonContractObjects = (CheckBox) Utils.lookup("#btnToggleShowNonContractObjectsExtractor", ContractManager.getMainScene());

        Settings settings = ContractManager.getApplicationData().getSettings();

        if(settings.isMinJson()){
            chBoxMinJson.setSelected(true);
        }
        else{
            chBoxMinJson.setSelected(false);
        }

        if(settings.isShowNonContractObjects()){
            chBoxShowNonContractObjects.setSelected(true);
        }
        else{
            chBoxShowNonContractObjects.setSelected(false);
        }

        // current tool bar index and current table row
        int tbIndex = 2;
        int gridRow = 0;

        int ITEMS_IN_EXTRACTOR_STATISTICS = 5;
        int ITEMS_IN_EXTRACTOR_DETAILS = 2;

        // go through each contract type
        for(Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings().
                getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();
            boolean contractTypeIsShown = entry.getValue();

            // prepare toggle buttons for tool bar filter
            CheckBox checkBox = new CheckBox();
            checkBox.setText(contractType.name());
            checkBox.setFocusTraversable(false);
            checkBox.setId("chBoxContractExtractor" + contractType);
            
            int padding = Integer.parseInt(ResourceHandler.getProperties().getString("filterPadding"));
            checkBox.setPadding(new Insets(0, padding, 0, 0));

            Tooltip tooltip = new Tooltip(ResourceHandler.getLocaleString("tooltipShowContractsOfType",contractType.name()));
            checkBox.setTooltip(tooltip);


            // prepare label for number of contracts of given type in statistics table
            Label lblGlobalStatsTitle = new Label();
            lblGlobalStatsTitle.setText(contractType.name() + " " + ResourceHandler.getLocaleString("labelContracts") + ": ");
            lblGlobalStatsTitle.setId("lblTitleExtractorStats" + contractType.name());
            lblGlobalStatsTitle.setFont(ContractManager.DEFAULT_LABEL_TITLE_FONT);

            // prepare label for the value
            Label lblGlobalStatsValue = new Label();
            lblGlobalStatsValue.setId("lblExtractorStats" + contractType.name());
            lblGlobalStatsValue.setText("-");

            gridGlobalStatsExtractor.addRow(gridRow + ITEMS_IN_EXTRACTOR_STATISTICS, lblGlobalStatsTitle);
            gridGlobalStatsExtractor.add(lblGlobalStatsValue, 1, gridRow + ITEMS_IN_EXTRACTOR_STATISTICS);


            // prepare label for number of contracts of given type in details table
            Label lblTitle = new Label();
            lblTitle.setText(contractType.name() + " " + ResourceHandler.getLocaleString("labelContracts") + ": ");
            lblTitle.setId("lblTitleContractNumber" + contractType.name());
            lblTitle.setFont(ContractManager.DEFAULT_LABEL_TITLE_FONT);

            // prepare label for the value
            Label lblValue = new Label();
            lblValue.setId("lblContractNumber" + contractType.name());
            lblValue.setText("-");

            // add labels to statistics table
            gridDetailsExtractor.addRow(gridRow + ITEMS_IN_EXTRACTOR_DETAILS, lblTitle);
            gridDetailsExtractor.add(lblValue, 1, gridRow + ITEMS_IN_EXTRACTOR_DETAILS);

            if(!contractTypeIsShown) {
                // hide labels if contract type is hidden
                lblGlobalStatsTitle.setVisible(false);
                lblGlobalStatsValue.setVisible(false);
                lblTitle.setVisible(false);
                lblValue.setVisible(false);
            }
            else{
                // toggle buttons if shown
                checkBox.setSelected(true);
            }

            // add buttons to filter tool bars
            tbFilterExtractor.getItems().add(tbIndex, checkBox);

            gridRow++;
            tbIndex++;
        }
    }


    /**
     * Updates global statistics labels based on current data. If flag clear is present it resets the statistics.
     *
     * @param clear  If flag clear is present it resets the statistics
     */
    public void updateGlobalStatistics(boolean clear) {

        globalStatistics = new JavaFileStatistics();

        for(JavaFileItem javaFileItem : fileList.getFiles()){

            if(javaFileItem.isVisible()){
                globalStatistics.mergeStatistics(javaFileItem.getJavaFile().getJavaFileStatistics());
            }
        }

        Label lblGlobalStatsFilesValue = (Label) Utils.lookup("#lblExtractorStatsFilesValue", ContractManager.getMainScene());
        Label lblGlobalStatsClassesValue = (Label) Utils.lookup("#lblExtractorStatsClassesValue", ContractManager.getMainScene());
        Label lblGlobalStatsMethodsValue = (Label) Utils.lookup("#lblExtractorStatsMethodsValue", ContractManager.getMainScene());
        Label lblGlobalStatsMethodsWithValue = (Label) Utils.lookup("#lblExtractorStatsMethodsWithValue", ContractManager.getMainScene());
        Label lblGlobalStatsContractsValue = (Label) Utils.lookup("#lblExtractorStatsContractsValue", ContractManager.getMainScene());

        if(!clear) {
            lblGlobalStatsFilesValue.setText("" + globalStatistics.getNumberOfFiles());
            lblGlobalStatsClassesValue.setText("" + globalStatistics.getNumberOfClasses());
            lblGlobalStatsMethodsValue.setText("" + globalStatistics.getNumberOfMethods());
            lblGlobalStatsMethodsWithValue.setText("" + globalStatistics.getNumberOfMethodsWithContracts());
            lblGlobalStatsContractsValue.setText("" + globalStatistics.getTotalNumberOfContracts());
        }
        else{
            String notDefined = "-";
            lblGlobalStatsFilesValue.setText(notDefined);
            lblGlobalStatsClassesValue.setText(notDefined);
            lblGlobalStatsMethodsValue.setText(notDefined);
            lblGlobalStatsMethodsWithValue.setText(notDefined);
            lblGlobalStatsContractsValue.setText(notDefined);
        }

        // display number of contracts for each selected design by contract type
        for (Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings()
                .getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();
            boolean used = entry.getValue();

            Label lblContractValue = (Label) ContractManager.getMainScene().lookup("#lblExtractorStats" + contractType.name());
            Label lblTitleContractValue = (Label) ContractManager.getMainScene().lookup("#lblTitleExtractorStats" + contractType.name());

            if (used) {
                lblContractValue.setVisible(true);
                lblTitleContractValue.setVisible(true);

                if(!clear) {
                    lblContractValue.setText("" + globalStatistics.getNumberOfContracts().get(contractType));
                }
                else{
                    lblContractValue.setText("-");
                }
            }
            else{
                lblContractValue.setVisible(false);
                lblTitleContractValue.setVisible(false);
            }
        }
    }


    /**
     * This method updates details in the right part of window each time user selects different file.
     */
    public void updateFileDetails() {

        // get selected file and save it into application model
        int selectedId = fileList.getCheckListView().getSelectionModel().getSelectedIndex();

        Label lblFileValue = (Label) Utils.lookup("#lblFileValueExtractor", ContractManager.getMainScene());

        boolean fileAllowed = false;
        if(selectedId >= 0 && selectedId < fileList.getFiles().size()) {

            currentJavaFile = fileList.getVisibleFileById(selectedId);

            // update label with name of the file
            lblFileValue.setText(currentJavaFile.getJavaFile().getFullPath());

            // once some file is selected - Show details button becomes available
            Button btnShowDetails = (Button) ContractManager.getMainScene().lookup("#btnShowDetailsExtractor");
            btnShowDetails.setDisable(false);
            fileAllowed = true;
        }
        else{
            clearFileDetails();
        }

        // display number of contracts for each selected design by contract type
        for (Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings()
                .getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();
            boolean allowed = entry.getValue();

            Label lblContractNumber = (Label) Utils.lookup("#lblContractNumber" + contractType.name(), ContractManager.getMainScene());
            Label lblTitleContractNumber = (Label) Utils.lookup("#lblTitleContractNumber" + contractType.name(), ContractManager.getMainScene());

            if (allowed) {
                lblContractNumber.setVisible(true);
                lblTitleContractNumber.setVisible(true);

                if(fileAllowed) {
                    lblContractNumber.setText("" + currentJavaFile.getJavaFile().getJavaFileStatistics().getNumberOfContracts().get(contractType));
                }
            }
            else{
                lblContractNumber.setVisible(false);
                lblTitleContractNumber.setVisible(false);
            }

        }
    }


    /**
     * Clears File details section because file has been removed.
     */
    public void clearFileDetails(){

        currentJavaFile = null;

        Label lblFileValue = (Label) Utils.lookup("#lblFileValueExtractor", ContractManager.getMainScene());
        lblFileValue.setText(ResourceHandler.getLocaleString("labelFileDefaultValue"));

        for (Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings()
                .getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();
            boolean used = entry.getValue();

            if (used) {
                Label lblContractNumber = (Label) Utils.lookup("#lblContractNumber" + contractType.name(), ContractManager.getMainScene());
                lblContractNumber.setText("-");
            }
        }

        Button btnShowDetails = (Button) Utils.lookup("#btnShowDetailsExtractor", ContractManager.getMainScene());
        btnShowDetails.setDisable(true);
    }

    
    /**
     * This method is called when user chooses to see details about selected files. It shows new window with details
     * and fills it with values. It show basic information such filename or path as well as statistics about
     * number of contracts etc. It also shows the export structure of selected file.
     */
    public void showDetailsWindow(){

        if(currentJavaFile != null) {

            String sceneFXMLName = ResourceHandler.getProperties().getString("sceneDetailsExtractorFileName");
            String windowName = ResourceHandler.getLocaleString("windowTitleDetails") + " - "
                    + currentJavaFile.getJavaFile().getShortPath();

            super.prepareDetailsWindow(sceneFXMLName, windowName);

            // get selected java file and convert it to JSON
            Gson gson = new Gson();
            String jsonInString = gson.toJson(currentJavaFile);

            // object in JSON convert to "pretty print" string
            JsonParser parser = new JsonParser();
            gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement el = parser.parse(jsonInString);
            jsonInString = gson.toJson(el);

            TextArea taJson = (TextArea) detailsScene.lookup("#taJson");
            taJson.setText(jsonInString);

            Label lblFileValue = (Label) detailsScene.lookup("#lblFileValue");
            lblFileValue.setText(currentJavaFile.getJavaFile().getFullPath());

            Label lblClassesValue = (Label) detailsScene.lookup("#lblClassesValue");
            lblClassesValue.setText("" + currentJavaFile.getJavaFile().getJavaFileStatistics().getNumberOfClasses());

            Label lblMethodsValue = (Label) detailsScene.lookup("#lblMethodsValue");
            lblMethodsValue.setText("" + currentJavaFile.getJavaFile().getJavaFileStatistics().getNumberOfMethods());

            Label lblMethodsWithValue = (Label) detailsScene.lookup("#lblMethodsWithValue");
            lblMethodsWithValue.setText("" + currentJavaFile.getJavaFile().getJavaFileStatistics().getNumberOfMethodsWithContracts());

            Label lblContractsValue = (Label) detailsScene.lookup("#lblContractsValue");
            lblContractsValue.setText("" + currentJavaFile.getJavaFile().getJavaFileStatistics().getTotalNumberOfContracts());

            int row = 1;
            GridPane gridDetails = (GridPane) detailsScene.lookup("#gridDetails");

            for(Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings()
                    .getContractTypes().entrySet()) {
                ContractType contractType = entry.getKey();
                boolean used = entry.getValue();

                if(used){
                    Label lblContractTitle = new Label();
                    lblContractTitle.setText(contractType.name() + " " + ResourceHandler.getLocaleString("labelContracts") + ":");
                    lblContractTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

                    Label lblContractValue = new Label();
                    lblContractValue.setId("lblContractValue" + contractType.name());
                    lblContractValue.setText("" + currentJavaFile.getJavaFile().getJavaFileStatistics().getNumberOfContracts().get(contractType));

                    gridDetails.addRow(row, lblContractTitle);
                    gridDetails.add(lblContractValue, 3, row);
                    row++;
                }
            }

            TreeView<String> treeView = (TreeView<String>) detailsScene.lookup("#tvContractDetails");
            prepareTreeView(treeView);
        }
    }


    /**
     * Prepares Tree view in the Details window. It contains information from current JavaFile and its affected
     * by current filters. It also merges some information for better display.
     *
     * @param treeView  TreeView to be prepared
     */
    private void prepareTreeView(TreeView<String> treeView){

        boolean showNonContractObjects = ContractManager.getApplicationData().getSettings().isShowNonContractObjects();
        HashMap<ContractType, Boolean> allowedContractTypes = ContractManager.getApplicationData().getSettings().getContractTypes();

        TreeItem<String> tiJavaFile = new TreeItem<>(ResourceHandler.getLocaleString("javaFile"));
        tiJavaFile.setExpanded(true);

        TreeItem<String> tiClasses;
            
        tiClasses = new TreeItem<>(ResourceHandler.getLocaleString("classes"));
        tiClasses.setExpanded(true);

        // go through classes only if they have at least 1 contract or there is showNonContractObjects flag
        if(!currentJavaFile.getJavaFile().getJavaClasses().isEmpty() && (!currentJavaFile.getJavaFile().getContracts().isEmpty() || showNonContractObjects)) {

            for (JavaClass javaClass : currentJavaFile.getJavaFile().getJavaClasses()) {

                // class included only if it has contracts or "showNonContractObjects"
                if (javaClass.getTotalNumberOfContracts() > 0 || showNonContractObjects) {

                    TreeItem<String> tiClass = new TreeItem<>(javaClass.getSignature());
                    tiClass.setExpanded(true);

                    // prepare class invariants
                    TreeItem<String> tiInvariants = new TreeItem<>(ResourceHandler.getLocaleString("invariants"));
                    tiInvariants.setExpanded(true);

                    // add allowed invariants
                    if(!javaClass.getInvariants().isEmpty()){
                        for(Contract invariant : javaClass.getInvariants()){
                            if(allowedContractTypes.get(invariant.getContractType())) {
                                TreeItem<String> tiInvariant = createContractTreeItem(invariant);
                                tiInvariants.getChildren().add(tiInvariant);
                            }
                        }
                    }

                    // if there weren't added any invariants - add empty word
                    if(tiInvariants.getChildren().isEmpty()){
                        tiInvariants.setValue(tiInvariants.getValue() + " - " + ResourceHandler.getLocaleString("treeEmpty"));
                    }

                    tiClass.getChildren().add(tiInvariants);

                    TreeItem<String> tiMethods = new TreeItem<>(ResourceHandler.getLocaleString("methods"));
                    tiMethods.setExpanded(true);

                    // add methods if there are any
                    if (!javaClass.getJavaMethods().isEmpty()) {

                        for (JavaMethod javaMethod : javaClass.getJavaMethods()) {

                            // show method only if it has any contracts or "showNonContractObjects"
                            if(!javaMethod.getContracts().isEmpty() || showNonContractObjects){

                                TreeItem<String> tiMethod = new TreeItem<>(javaMethod.getSignature());
                                tiMethod.setExpanded(true);

                                TreeItem<String> tiContracts = new TreeItem<>(ResourceHandler.getLocaleString("contracts"));
                                tiContracts.setExpanded(true);

                                if(!javaMethod.getContracts().isEmpty()) {

                                    for (Contract contract : javaMethod.getContracts()) {

                                        // add contract only if it should be displayed
                                        if(allowedContractTypes.get(contract.getContractType())) {

                                            TreeItem<String> tiContract = createContractTreeItem(contract);
                                            tiContracts.getChildren().add(tiContract);
                                        }
                                    }
                                }

                                if(tiContracts.getChildren().isEmpty()){
                                    tiContracts.setValue(tiContracts.getValue() + " - " + ResourceHandler.getLocaleString("treeEmpty"));
                                }
                                
                                tiMethod.getChildren().add(tiContracts);
                                tiMethods.getChildren().add(tiMethod);
                            }
                        }
                    }

                    if(tiMethods.getChildren().isEmpty()){
                        tiMethods.setValue(tiMethods.getValue() + " - " + ResourceHandler.getLocaleString("treeEmpty"));
                    }

                    tiClass.getChildren().add(tiMethods);
                    tiClasses.getChildren().add(tiClass);
                }
            }
        }

        if(tiClasses.getChildren().isEmpty()){
            tiClasses.setValue(tiClasses.getValue() + " - " + ResourceHandler.getLocaleString("treeEmpty"));
        }

        tiJavaFile.getChildren().add(tiClasses);
        treeView.setRoot(tiJavaFile);
    }


    /**
     * This method prepares item for tree view based on given contract
     *
     * @param contract  Contract to be prepared for tree view
     * @return          Tree view item with contract data
     */
    private TreeItem<String> createContractTreeItem(Contract contract){

        TreeItem<String> tiContract = new TreeItem<>(contract.getContractType().toString() + " - " + contract.getConditionType());
        tiContract.setExpanded(true);

        tiContract.getChildren().add(new TreeItem<>(ResourceHandler.getLocaleString(
                "completeExpression") + " " + contract.getCompleteExpression()));
        tiContract.getChildren().add(new TreeItem<>(ResourceHandler.getLocaleString(
                "contractFunction") + " " + contract.getFunction()));
        tiContract.getChildren().add(new TreeItem<>(ResourceHandler.getLocaleString(
                "contractExpression") + " " + contract.getExpression()));

        // if there are any arguments add this part of a tree
        if (contract.getArguments().size() > 0) {
            TreeItem<String> tiArguments = new TreeItem<>(ResourceHandler
                    .getLocaleString("contractArguments"));
            tiArguments.setExpanded(true);

            for (String argument : contract.getArguments()) {
                tiArguments.getChildren().add(new TreeItem<>(argument));
            }
            tiContract.getChildren().add(tiArguments);
        }

        return tiContract;
    }


    
    // Getters and Setters
    public ContractExtractorApi getContractExtractorApi() {
        return contractExtractorApi;
    }

    public JavaFileStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public ExtractorFileList getFileList() {
        return fileList;
    }

    public JavaFileItem getCurrentJavaFile() {
        return currentJavaFile;
    }

    public void setCurrentJavaFile(JavaFileItem currentJavaFile) {
        this.currentJavaFile = currentJavaFile;
    }
}

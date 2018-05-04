package contractmanager.application;

import contractmanager.application.applicationtab.ComparatorApplicationTab;
import contractmanager.application.applicationtab.ExtractorApplicationTab;
import contractmanager.utility.Utils;
import contractmanager.ContractManager;
import cz.zcu.kiv.contractparser.model.ContractType;
import javafx.scene.control.CheckBox;

import java.util.Map;

/**
 * This class contains individual parts of application represented by Application Tab as well as global settings.
 *
 * @author Vaclav Mares
 */
public class ApplicationData {

    /** Settings of application that is possible to change via filters */
    private Settings settings;

    /** Application Tab representing Contract Extractor */
    private ExtractorApplicationTab extractorApplicationTab;

    /** Application Tab representing Contract Comparator */
    private ComparatorApplicationTab comparatorApplicationTab;

    
    public ApplicationData() {
        this.settings = new Settings();
        this.extractorApplicationTab = new ExtractorApplicationTab();
        this.comparatorApplicationTab = new ComparatorApplicationTab();
    }


    /**
     * This method applies filter choices to application data.
     *
     * @param isExtractor   Whether Filter button was pressed in Extractor part or Comparator part
     */
    public void filterData(boolean isExtractor) {

        updateSettings(isExtractor);

        extractorApplicationTab.getFileList().updateList();
        extractorApplicationTab.updateFileDetails();
        
        comparatorApplicationTab.getFileList().updateList();
        comparatorApplicationTab.updateReportDetails();
    }


    /**
     * This method updates settings based on chosen filters.
     *
     * @param isExtractor   Whether Filter button was pressed in Extractor part or Comparator part
     */
    private void updateSettings(boolean isExtractor) {

        // get all the filters
        CheckBox chBoxMinJsonExtractor = (CheckBox) Utils.lookup("#btnToggleMinJsonExtractor", ContractManager.getMainScene());
        CheckBox chBoxMinJsonComparator = (CheckBox) Utils.lookup("#btnToggleMinJsonComparator", ContractManager.getMainScene());
        CheckBox chBoxShowNonContractObjects = (CheckBox) Utils.lookup("#btnToggleShowNonContractObjectsExtractor", ContractManager.getMainScene());
        CheckBox chBoxReportOnlyContractChanges = (CheckBox) Utils.lookup("#btnToggleReportOnlyContractChanges", ContractManager.getMainScene());
        CheckBox chBoxReportEqual = (CheckBox) Utils.lookup("#btnToggleReportEqual", ContractManager.getMainScene());

        // set minJSON according to the other checkBox
        // save all values to settings
        if(isExtractor) {
            boolean isSelected = chBoxMinJsonExtractor.isSelected();
            settings.setMinJson(isSelected);
            chBoxMinJsonComparator.setSelected(isSelected);
        }
        else{
            boolean isSelected = chBoxMinJsonComparator.isSelected();
            settings.setMinJson(isSelected);
            chBoxMinJsonExtractor.setSelected(isSelected);
        }

        settings.setShowNonContractObjects(chBoxShowNonContractObjects.isSelected());
        settings.setReportOnlyContractChanges(chBoxReportOnlyContractChanges.isSelected());
        settings.setReportEqual(chBoxReportEqual.isSelected());

        // do the same for contract types
        for(Map.Entry<ContractType, Boolean> entry : ContractManager.getApplicationData().getSettings().
                getContractTypes().entrySet()) {
            ContractType contractType = entry.getKey();

            CheckBox chBoxExtractor = (CheckBox) Utils.lookup("#chBoxContractExtractor" + contractType, ContractManager.getMainScene());
            CheckBox chBoxComparator = (CheckBox) Utils.lookup("#chBoxContractComparator" + contractType, ContractManager.getMainScene());

            if(isExtractor) {
                boolean isSelected = chBoxExtractor.isSelected();
                settings.setContractType(contractType, isSelected);
                chBoxComparator.setSelected(isSelected);
            }
            else{
                boolean isSelected = chBoxComparator.isSelected();
                settings.setContractType(contractType, isSelected);
                chBoxExtractor.setSelected(isSelected);
            }
        }
    }


    // Getters and Setters
    public Settings getSettings() {
        return settings;
    }

    public ExtractorApplicationTab getExtractorApplicationTab() {
        return extractorApplicationTab;
    }

    public ComparatorApplicationTab getComparatorApplicationTab() {
        return comparatorApplicationTab;
    }
}

package contractmanager.application;

import contractmanager.utility.ResourceHandler;
import cz.zcu.kiv.contractparser.model.ContractType;

import java.util.HashMap;

/**
 * This class contains information about settings of the application. It contains boolean for each filter option.
 */
public class Settings {

    /** Every generated JSON will be in minimalistic format instead of human readable pretty print */
    private boolean minJson;

    /** Show every file, class or method even those without any contract */
    private boolean showNonContractObjects;

    /** Only changes affecting contracts will be reported */
    private boolean reportOnlyContractChanges;

    /** Even though both files, classes, methods or contracts are equal report will be created */
    private boolean reportEqual;

    /** Which contracts should be visible through out the application */
    private HashMap<ContractType,Boolean> contractTypes;

    
    public Settings() {
        minJson = Boolean.parseBoolean(ResourceHandler.getProperties().getString("defaultMinJson"));
        showNonContractObjects = Boolean.parseBoolean(ResourceHandler.getProperties().getString("defaultShowNonContractObjects"));
        reportOnlyContractChanges=Boolean.parseBoolean(ResourceHandler.getProperties().getString("defaultReportOnlyContractChanges"));
        reportEqual = Boolean.parseBoolean(ResourceHandler.getProperties().getString("defaultReportEqual"));

        // add all available contract types - default all true
        this.contractTypes = new HashMap<>();
        for(ContractType contractType : ContractType.values()){
            this.contractTypes.put(contractType, true);
        }
    }
    

    @Override
    public String toString() {
        return "Settings{" +
                "minJson=" + minJson +
                ", showNonContractObjects=" + showNonContractObjects +
                ", reportOnlyContractChanges=" + reportOnlyContractChanges +
                ", reportEqual=" + reportEqual +
                ", contractTypes=" + contractTypes +
                '}';
    }


    // Getters and Setters
    public boolean isMinJson() {
        return minJson;
    }

    public void setMinJson(boolean minJson) {
        this.minJson = minJson;
    }

    public boolean isShowNonContractObjects() {
        return showNonContractObjects;
    }

    public HashMap<ContractType, Boolean> getContractTypes() {
        return contractTypes;
    }

    public void setShowNonContractObjects(boolean showNonContractObjects) {
        this.showNonContractObjects = showNonContractObjects;
    }

    public boolean isReportEqual() {
        return reportEqual;
    }

    public void setReportEqual(boolean reportEqual) {
        this.reportEqual = reportEqual;
    }

    public boolean isReportOnlyContractChanges() {
        return reportOnlyContractChanges;
    }

    public void setReportOnlyContractChanges(boolean reportOnlyContractChanges) {
        this.reportOnlyContractChanges = reportOnlyContractChanges;
    }

    public void setContractType(ContractType contractType, boolean isEnabled) {

        contractTypes.replace(contractType, isEnabled);
    }
}

package contractmanager.presentation;

import contractmanager.utility.ResourceHandler;
import cz.zcu.kiv.contractparser.model.ContractType;

import java.util.HashMap;

public class Settings {

    private boolean minJson;
    private boolean showNonContractObjects;
    private boolean reportOnlyContractChanges;
    private boolean reportEqual;

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
}

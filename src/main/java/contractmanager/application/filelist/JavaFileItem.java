package contractmanager.application.filelist;

import contractmanager.ContractManager;
import cz.zcu.kiv.contractparser.model.Contract;
import cz.zcu.kiv.contractparser.model.ContractType;
import cz.zcu.kiv.contractparser.model.JavaFile;

import java.util.HashMap;

/**
 * Wrapper for JavaFile to enable set its visibility in the file list
 *
 * @author Vaclav Mares
 */
public class JavaFileItem {

    /** JavaFile containing the data */
    private JavaFile javaFile;

    /** Whether or not should be the file visible */
    private boolean visible;

    
    public JavaFileItem(JavaFile javaFile, boolean visible) {
        this.javaFile = javaFile;
        this.visible = visible;
    }


    /**
     * Updates the visibility of this file based on its data and current filter settings
     */
    public void updateVisibility(){

        boolean showNonContractObjects = ContractManager.getApplicationData().getSettings().isShowNonContractObjects();
        HashMap<ContractType, Boolean> contractTypes = ContractManager.getApplicationData().getSettings().getContractTypes();

        if(javaFile.getContracts().isEmpty()){
            visible = showNonContractObjects;
        }
        else{
            visible = false;
            for(Contract contract : javaFile.getContracts()){
                if(contractTypes.get(contract.getContractType())){
                    visible = true;
                    break;
                }
            }
        }
    }


    // Getters and Setters
    public JavaFile getJavaFile() {
        return javaFile;
    }

    public void setJavaFile(JavaFile javaFile) {
        this.javaFile = javaFile;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

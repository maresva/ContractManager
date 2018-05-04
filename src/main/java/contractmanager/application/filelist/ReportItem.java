package contractmanager.application.filelist;

import contractmanager.ContractManager;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.ContractCompareReport;
import cz.zcu.kiv.contractparser.comparator.comparatormodel.JavaFileCompareReport;
import cz.zcu.kiv.contractparser.model.ContractType;

import java.util.HashMap;

/**
 * Wrapper for JavaFileCompareReport to enable set its visibility in the file list
 *
 * @author Vaclav Mares
 */
public class ReportItem {

    /** JavaFileCompareReport containing the data */
    private JavaFileCompareReport report;

    /** Whether or not should be the file visible */
    private boolean visible;


    public ReportItem(JavaFileCompareReport report) {
        this.report = report;
    }


    /**
     * Updates the visibility of this file based on its data and current filter settings
     */
    public void updateVisibility(){

        boolean reportOnlyContractChanges = ContractManager.getApplicationData().getSettings().isReportOnlyContractChanges();
        boolean reportEqual = ContractManager.getApplicationData().getSettings().isReportEqual();
        HashMap<ContractType, Boolean> contractTypes = ContractManager.getApplicationData().getSettings().getContractTypes();

        if(!reportEqual && report.isContractEqual() && report.isApiEqual()){
            visible = false;
        }
        else if(reportOnlyContractChanges && (report.getContractCompareReports().isEmpty() || report.isContractEqual())){
            visible = false;
        }
        else if(!reportOnlyContractChanges && report.getContractCompareReports().isEmpty()){
            visible = true;
        }
        else{
            visible = false;
            for(ContractCompareReport contractCompareReport : report.getContractCompareReports()){
                if(contractTypes.get(contractCompareReport.getContractType())){
                    visible = true;
                    break;
                }
            }
        }
    }


    // Getters and Setters
    public JavaFileCompareReport getReport() {
        return report;
    }

    public void setReport(JavaFileCompareReport report) {
        this.report = report;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
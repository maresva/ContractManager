package contractmanager.utility;

import contractmanager.view.ContractManager;
import javafx.scene.control.TextArea;

public class ConsoleWriter {

    private TextArea ta_console;


    public ConsoleWriter() {
        this.ta_console = (TextArea) ContractManager.scene.lookup("#ta_console");
    }

    public void write(String message){
        ta_console.appendText("> " + message + "\n");
    }

    public void clear(){
        ta_console.setText("");
    }

    public void writeNumberOfAddedFiles(int addedFiles){

        if(addedFiles == 0){
            write(ContractManager.localization.getString("consoleAddNoFiles"));
        }
        else if(addedFiles == 1){
            write(ContractManager.localization.getString("consoleAddOneFile"));
        }
        else{
            write(addedFiles + " " + ContractManager.localization.getString("consoleAddManyFiles"));
        }
    }

    public void writeNumberOfDeletedFiles(int deletedFiles){

        if(deletedFiles == 0){
            write(ContractManager.localization.getString("consoleDeleteNoFiles"));
        }
        else if(deletedFiles == 1){
            write(ContractManager.localization.getString("consoleDeleteOneFile"));
        }
        else{
            write(deletedFiles + " " + ContractManager.localization.getString("consoleDeleteManyFiles"));
        }
    }

    public void writeNumberOfExportedFiles(int exportedFiles){

        if(exportedFiles == 0){
            write(ContractManager.localization.getString("consoleExportNoFiles"));
        }
        else if(exportedFiles == 1){
            write(ContractManager.localization.getString("consoleExportOneFile"));
        }
        else{
            write(exportedFiles + " " + ContractManager.localization.getString("consoleExportManyFiles"));
        }
    }
}

package contractmanager.utility;

import cz.zcu.kiv.contractparser.ContractComparatorApi;
import cz.zcu.kiv.contractparser.ContractExtractorApi;
import cz.zcu.kiv.contractparser.comparator.JavaFolderCompareReport;
import cz.zcu.kiv.contractparser.io.IOServices;
import cz.zcu.kiv.contractparser.model.ContractType;
import cz.zcu.kiv.contractparser.model.JavaFile;
import javafx.application.Platform;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class ConsoleApplication {

    public static void runConsoleApplication(String[] args){

        int argc = args.length;

        if(argc == 2){

            // get input folder
            checkFolder(args[0], false);
            File inputFolder = new File(args[0]);

            // get output folder
            checkFolder(args[1], true);
            File outputFolder = new File(args[1]);

            List<JavaFile> javaFiles = ContractExtractorApi.retrieveContractsFromFolder(inputFolder, false);
            IOServices.exportJavaFilesToJson(javaFiles, outputFolder);

            /*
            // get input file/folder - check if exist - if not throw error
            File input = new File(args[0]);
            if(!input.exists()){
                endWithError("ERROR: Input file or folder doesn't exist", false);
            }

            // get output folder - if output folder doesn't exists - try to create it
            File outputFolder = new File(args[1]);
            if (!outputFolder.exists()){
                boolean success = outputFolder.mkdirs();

                if(!success){
                    endWithError("ERROR: Output folder could not be created", false);
                }
            }

            if(outputFolder == null || !outputFolder.exists() || outputFolder.isFile()){
                endWithError("ERROR: Output folder doesn't exist", false);
            } */

            // TODO volba jestli *.java / *.class (oboje nebo jen jedno z toho)

            // TODO volba ktere kontrakty parsovat
            // add all available contract types
            /*
            HashMap<ContractType,Boolean> contractTypes = new HashMap<>();
            for(ContractType contractType : ContractType.values()){
                contractTypes.put(contractType, true);
            }

            // TODO volba ktery export provest
            if(input.isFile()){
                JavaFile javaFile = ContractExtractorApi.retrieveContracts(input, contractTypes, true);
                IOServices.exportJavaFileToJson(javaFile, outputFolder);
            }
            else{
                List<JavaFile> javaFiles = ContractExtractorApi.retrieveContractsFromFolder(input, contractTypes, true);
                IOServices.exportJavaFilesToJson(javaFiles, outputFolder);
            }

            System.out.println("End batch");
            */
        }
        else if(argc == 3){

            // get first java folder
            checkFolder(args[0], false);
            //File firstInputFolder = new File(args[0]);

            // get second java folder
            checkFolder(args[1], false);
            //File secondInputFolder = new File(args[1]);

            // get output folder
            checkFolder(args[2], true);
            File outputFolder = new File(args[2]);

            JavaFolderCompareReport javaFolderCompareReport = ContractComparatorApi.compareJavaFolders(args[0], args[1],
                    true, true);
            
            IOServices.exportJavaFolderCompareRoportsToJson(javaFolderCompareReport, outputFolder);
        }
        else {
            endWithError("Error: Wrong number of parameters", true);
        }

        Platform.exit();
        System.exit(1);
    }


    public static void showHelp() {
        System.out.println("HELP TODO");
    }

    
    public static void endWithError(String errorMessage, boolean showHelp) {
        
        System.err.println(errorMessage);

        if(showHelp){
            showHelp();
        }

        Platform.exit();
        System.exit(1);
    }


    private static void checkFolder(String folderName, boolean createFolder){

        File folder = new File(folderName);

        if(folder.isFile()){
            endWithError("Error: Given path: " + folderName + " is a file not a folder.", false);
        }

        if(!folder.exists()){

            if(createFolder){
                if(!folder.mkdirs()){
                    endWithError("Error: Folder: " + folderName + " couldn't be created.", false);
                }
            }
            else{
                endWithError("Error: Given folder: " + folderName + " doesn't exist.", false);
            }
        }
    }
}

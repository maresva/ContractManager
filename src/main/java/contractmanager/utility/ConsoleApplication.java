package contractmanager.utility;

import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.api.BatchContractComparatorApi;
import cz.zcu.kiv.contractparser.api.BatchContractExtractorApi;

import java.io.File;

/**
 * This class provides methods used to work with console application part of this program. It receives commands from
 * user, evaluates them and either executes them or report an error.
 *
 * @author Vaclav Mares
 * */
public class ConsoleApplication {

    /** API providing batch contract extractor methods */
    private BatchContractExtractorApi batchContractExtractorApi;

    /** API providing batch contract comparator methods */
    private BatchContractComparatorApi batchContractComparatorApi;


    public ConsoleApplication(BatchContractExtractorApi contractExtractorApi, BatchContractComparatorApi batchContractComparatorApi) {
        this.batchContractExtractorApi = contractExtractorApi;
        this.batchContractComparatorApi = batchContractComparatorApi;
    }
    

    /**
     * This method runs a console application command based on input arguments.
     *
     * @param args  Application input arguments
     */
    public void runConsoleApplication(String[] args){

        // first argument represents command to be executed
        // it can be either -h or --help for Help command, -e for Extractor command or -c for Comparator command
        // other values are returned as errors
        if(args[0] != null){
            args[0] = args[0].toLowerCase();
        }
        else{
            endWithError(ResourceHandler.getLocaleString("consoleAppErrorFirstParamIsNull"), true);
        }

        if(ResourceHandler.getProperties().getString("consoleAppCommandHelpShort").compareTo(args[0]) == 0 ||
                ResourceHandler.getProperties().getString("consoleAppCommandHelpLong").compareTo(args[0]) == 0) {

            helpCommand(args);
        }
        else if(ResourceHandler.getProperties().getString("consoleAppCommandExtractor").compareTo(args[0]) == 0) {
            extractorCommand(args);
        }
        else if(ResourceHandler.getProperties().getString("consoleAppCommandComparator").compareTo(args[0]) == 0) {
            comparatorCommand(args);
        }
        else{
            endWithError(ResourceHandler.getLocaleString("consoleAppErrorFirstParamUnknown", args[0]), true);
        }

        ContractManager.closeApplication(0);
    }


    /**
     * This method represents comparator command which is executed when user uses -c flag. It expects at least 3 other
     * arguments where there is first input folder and second input folder which are compared to each other. Then there
     * is a output folder for created JSON files. Then there are 3 optional arguments. First -q says whether should be
     * equal reports about contracts, methods, classes or files removed form the lists. Next -o says whether should be
     * reported only changes affecting contracts. Last -m says that JSON files will be in minimalistic format instead
     * of use of pretty print.
     *
     * -c <input_folder1> <input_folder2> <output_folder> [-q] [-o] [-m]
     *
     * @param args  Application input arguments
     */
    private void comparatorCommand(String[] args) {

        if(args.length >= 4) {

            if (args.length > 7) {
                endWithError(ResourceHandler.getLocaleString("consoleAppErrorComparatorWrongNumParamsMax"), true);
            }

            // get first input folder
            checkFolder(args[1], false);
            File firstInputFolder = new File(args[1]);

            // get second input folder
            checkFolder(args[2], true);
            File secondInputFolder = new File(args[2]);

            // get output folder
            checkFolder(args[3], true);
            File outputFolder = new File(args[3]);

            boolean reportOnlyContractChange = false;
            boolean removeEqual = false;
            boolean minJson = false;

            // go through remaining arguments
            for(int i = 4 ; i < args.length ; i++) {

                if(args[i] != null){
                    args[i] = args[i].toLowerCase();
                }
                else{
                    endWithError(ResourceHandler.getLocaleString("consoleAppErrorComparatorFlagUnknown", args[i]), true);
                }

                if(ResourceHandler.getProperties().getString("consoleAppFlagReportOnlyContractChanges").compareTo(args[i]) == 0){
                    reportOnlyContractChange = true;
                }
                else if(ResourceHandler.getProperties().getString("consoleAppFlagRemoveEqual").compareTo(args[i]) == 0){
                    removeEqual = true;
                }
                else if(ResourceHandler.getProperties().getString("consoleAppFlagMinJson").compareTo(args[i]) == 0){
                    minJson = true;
                }
                else{
                    endWithError(ResourceHandler.getLocaleString("consoleAppErrorComparatorFlagUnknown", args[i]), true);
                }
            }


            batchContractComparatorApi.compareJavaFoldersAndExportToJson(firstInputFolder, secondInputFolder, !removeEqual,
                    !reportOnlyContractChange, outputFolder, !minJson);
        }
        else{
            endWithError(ResourceHandler.getLocaleString("consoleAppErrorComparatorWrongNumParamsMin"), true);
        }

    }


    /**
     * This method represents extractor command which is executed when user uses -e flag. It expects at least 2 other
     * arguments where there is first input folder and then output folder. Then there are 2 optional arguments. First
     * -r says whether should be removed any classes or methods that does not contain any contract. Last argument -m
     * says that JSON files will be in minimalistic format instead of use of pretty print.
     *
     * -e <input_folder> <output_folder> [-r] [-m]
     *
     * @param args  Application input arguments
     */
    private void extractorCommand(String[] args) {
        
        if(args.length >= 3) {

            if(args.length > 5){
                endWithError(ResourceHandler.getLocaleString("consoleAppErrorExtractorWrongNumParamsMax"), true);
            }

            // get input folder
            checkFolder(args[1], false);
            File inputFolder = new File(args[1]);

            // get output folder
            checkFolder(args[2], true);
            File outputFolder = new File(args[2]);

            boolean minJson = false;
            boolean removeNonContractObjects = false;

            for(int i = 3 ; i < args.length ; i++) {

                if(args[i] != null){
                    args[i] = args[i].toLowerCase();
                }
                else{
                    endWithError(ResourceHandler.getLocaleString("consoleAppErrorExtractorFlagUnknown", args[i]), true);
                }

                if(ResourceHandler.getProperties().getString("consoleAppFlagMinJson").compareTo(args[i]) == 0){
                    minJson = true;
                }
                else if(ResourceHandler.getProperties().getString("consoleAppFlagRemoveNonContractObjects").compareTo(args[i]) == 0){
                    removeNonContractObjects = true;
                }
                else{
                    endWithError(ResourceHandler.getLocaleString("consoleAppErrorExtractorFlagUnknown", args[i]), true);
                }
            }

            batchContractExtractorApi.retrieveContractsFromFolderExportToJson(inputFolder, outputFolder, !minJson,
                    removeNonContractObjects);

        }
        else{
            endWithError(ResourceHandler.getLocaleString("consoleAppErrorExtractorWrongNumParamsMin"), true);
        }
    }


    /**
     * Executes help command which displays help for user. It can be displayed by typing -h or --help command.
     *
     * @param args  Application input arguments
     */
    private void helpCommand(String[] args) {

        if(args.length == 1){
            System.out.println(ResourceHandler.getLocaleString("consoleAppHelp"));
        }
        else{
            endWithError(ResourceHandler.getLocaleString("consoleAppErrorOneParamHelp"), true);
        }
    }


    /**
     * This method will end the application with an error message that is given. Optionally it can also display help
     * for the user before terminating the program.
     *
     * @param errorMessage  Error message that should be displayed
     * @param showHelp      If help message should be printed
     */
    private void endWithError(String errorMessage, boolean showHelp) {
        
        System.out.println(ResourceHandler.getLocaleString("consoleAppError") + " " + errorMessage + "\n");

        if(showHelp){
            System.out.println(ResourceHandler.getLocaleString("consoleAppHelp"));
        }

        ContractManager.closeApplication(1);
    }


    /**
     * This method checks given String which should be folder. If the folder is valid nothing happens otherwise error
     * is thrown and application terminated. There can be specified whether should be created non existing folders or not.
     *
     * @param folderName    Name of folder (path) to be checked
     * @param createFolder  Whether should be created non existing folders or not
     */
    private void checkFolder(String folderName, boolean createFolder){

        File folder = new File(folderName);

        if(folder.isFile()){
            endWithError(ResourceHandler.getLocaleString("consoleAppErrorFileNotFolder", folderName), false);
        }

        if(!folder.exists()){

            if(createFolder){
                if(!folder.mkdirs()){
                    endWithError(ResourceHandler.getLocaleString("consoleAppErrorFolderNotCreated", folderName), false);
                }
            }
            else{
                endWithError(ResourceHandler.getLocaleString("consoleAppErrorFolderNotExist", folderName), false);
            }
        }
    }
}
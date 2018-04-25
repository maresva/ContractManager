package contractmanager.model;

import cz.zcu.kiv.contractparser.ContractExtractorApi;
import cz.zcu.kiv.contractparser.utils.IOServices;
import cz.zcu.kiv.contractparser.model.ContractType;
import cz.zcu.kiv.contractparser.model.JavaFile;
import cz.zcu.kiv.contractparser.model.JavaFileStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * @author Václav Mareš
 */
public class DataModel {

    private List<JavaFile> files;
    private JavaFile currentFile;
    private HashMap<ContractType,Boolean> contractTypes;
    private JavaFileStatistics globalStatistics;


    public DataModel() {
        this.files = new ArrayList<>();
        this.contractTypes = new HashMap<>();
        this.globalStatistics = new JavaFileStatistics();

        // add all available contract types
        for(ContractType contractType : ContractType.values()){
            contractTypes.put(contractType, true);
        }
    }

    public boolean addFile(File newFile) {

        // check whether this file is not already present
        boolean found = false;
        for(JavaFile javaFile : files) {

            if(javaFile == null){
                System.out.println("javaFile je NULL");
                return false;
            }

            if(newFile == null) {
                System.out.println("newFile je NULL");
                return false;
            }

            if(javaFile.getFullPath().equals(newFile.getAbsolutePath())) {
                found = true;
                break;
            }
        }
        

        // if the file is not in the list yet - add it
        if (!found) {
            JavaFile javaFile = ContractExtractorApi.retrieveContracts(newFile, true);

            if(javaFile != null) {
                files.add(javaFile);
                globalStatistics.mergeStatistics(javaFile.getJavaFileStatistics());

                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    public int removeFiles(List<Integer> fileIds) {

        int deletedFiles = 0;

        for(int index : fileIds){

            int newIndex = index - deletedFiles;

            if(newIndex < files.size()) {

                globalStatistics.detachStatistics(files.get(newIndex).getJavaFileStatistics());
                files.remove(newIndex);
                deletedFiles++;
            }
        }

        return deletedFiles;
    }

    public int exportToJSON(List<Integer> fileIds, File outputFolder) {

        int exportedFiles = 0;

        ContractExtractorApi.exportJavaFilesToJson(files, outputFolder, true);

        return exportedFiles;
    }


    public void updateShortPath() {

        ContractExtractorApi.updateShortPathOfJavaFiles(files);
    }



    public List<JavaFile> getFiles() {
        Collections.sort(files, comparing(JavaFile::getFullPath));
        return files;
    }

    public JavaFile getCurrentFile() {
        return currentFile;
    }

    public HashMap<ContractType, Boolean> getContractTypes() {
        return contractTypes;
    }

    public JavaFileStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public void setCurrentFile(JavaFile currentFile) {
        this.currentFile = currentFile;
    }
}

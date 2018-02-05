package contractmanager;

import cz.zcu.kiv.contractparser.Api;
import cz.zcu.kiv.contractparser.model.ContractType;
import cz.zcu.kiv.contractparser.model.JavaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author Václav Mareš
 */
public class DataModel {

    private List<JavaFile> files;
    private JavaFile currentFile;
    private HashMap<ContractType,Boolean> contractTypes;


    public DataModel() {
        this.files = new ArrayList<>();
        this.contractTypes = new HashMap<>();

        contractTypes.put(ContractType.GUAVA, true);
        contractTypes.put(ContractType.JSR305, false);
    }

    public boolean addFile(File newFile) {

        // check whether this file is not already present
        boolean found = false;
        for(JavaFile javaFile : files) {
            if(javaFile.getPath().equals(newFile.getPath())) {
                found = true;
                break;
            }
        }

        // if the file is not in the list yet - add it
        if (!found) {
            JavaFile javaFile = Api.retrieveContracts(newFile, contractTypes);
            files.add(javaFile);
            return true;
        }
        else{
            return false;
        }
    }

    public void removeFiles(List<Integer> fileIds) {

        int deletedFiles = 0;

        for(int index : fileIds){

            int newIndex = index - deletedFiles;

            if(newIndex < files.size()) {
                files.remove(newIndex);
                deletedFiles++;
            }
        }
    }



    public List<JavaFile> getFiles() {
        return files;
    }

    public JavaFile getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(JavaFile currentFile) {
        this.currentFile = currentFile;
    }
}

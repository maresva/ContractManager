package contractmanager.application.filelist;

/**
 * Interface for File lists. It is used in loading window to avoid using multiple callings.
 */
public interface FileList {

    /**
     * Updates CheckViewList of File List based on current data
     */
    void updateList();
}

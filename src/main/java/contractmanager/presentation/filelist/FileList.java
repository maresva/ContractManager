package contractmanager.presentation.filelist;

import org.controlsfx.control.CheckListView;

import java.util.List;


public interface FileList {

    void updateList();
    List<Integer> getSelected();
    List<?> getSelectedFiles();
    CheckListView getCheckListView();
    List<?> getFiles();
}

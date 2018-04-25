package contractmanager.applicationTab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import contractmanager.filelist.FileList;
import contractmanager.view.ContractManager;
import cz.zcu.kiv.contractparser.model.ContractType;
import cz.zcu.kiv.contractparser.model.JavaFile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Map;

public class ApplicationTab {

    private FileList fileList;

    public ApplicationTab(FileList fileList) {
        this.fileList = fileList;
    }

    /**
     * This method is called when user chooses to see details about selected files. It shows new window with details
     * and fills it with values. It show basic information such filename or path as well as statistics about
     * number of contracts etc. It also shows the export structure of selected file.
     */
    public void showDetails(){

        if(ContractManager.dataModel.getCurrentFile() != null) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(
                        ContractManager.properties.getString("sceneDetailsFileName")));
                loader.setResources(ContractManager.localization);
                Parent root = loader.load();

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);

                // set default window size
                double height = Double.parseDouble(ContractManager.properties.getString("detailsWindowWidth"));
                double width = Double.parseDouble(ContractManager.properties.getString("detailsWindowHeight"));
                stage.setHeight(height);
                stage.setWidth(width);
                stage.setMinHeight(height);
                stage.setMinWidth(width);

                stage.setTitle(ContractManager.localization.getString("windowTitleDetails") + " - "
                        + ContractManager.dataModel.getCurrentFile().getShortPath());
                // TODO zprovoznit icon v .jar
                stage.getIcons().add(new Image(ContractManager.properties.getString("icon")));
                stage.show();

                // get selected java file and convert it to JSON
                JavaFile javaFile = ContractManager.dataModel.getCurrentFile();
                Gson gson = new Gson();
                String jsonInString = gson.toJson(javaFile);

                // object in JSON convert to "pretty print" string
                JsonParser parser = new JsonParser();
                gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement el = parser.parse(jsonInString);
                jsonInString = gson.toJson(el);

                TextArea ta_details = (TextArea) scene.lookup("#ta_details");
                ta_details.setText(jsonInString);

                Label lbl_filename_value = (Label) scene.lookup("#lbl_filename_value");
                lbl_filename_value.setText(ContractManager.dataModel.getCurrentFile().getCompleteFileName());

                Label lbl_path_value = (Label) scene.lookup("#lbl_path_value");
                lbl_path_value.setText(ContractManager.dataModel.getCurrentFile().getShortPath());

                Label lbl_number_classes_value = (Label) scene.lookup("#lbl_number_classes_value");
                lbl_number_classes_value.setText("" + ContractManager.dataModel.getCurrentFile().getJavaFileStatistics().getNumberOfClasses());

                Label lbl_number_methods_value = (Label) scene.lookup("#lbl_number_methods_value");
                lbl_number_methods_value.setText("" + ContractManager.dataModel.getCurrentFile().getJavaFileStatistics().getNumberOfMethods());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * This method updates details in the right part of window each time user selects different file.
     */
    public void updateDetails() {

        // get selected file and save it into data model
        int selectedId = fileList.getCheckListView().getSelectionModel().getSelectedIndex();

        if(selectedId >= 0 && selectedId < ContractManager.dataModel.getFiles().size()) {

            JavaFile selectedFile = ContractManager.dataModel.getFiles().get(selectedId);
            ContractManager.dataModel.setCurrentFile(selectedFile);

            // update label with name of the file
            Label lbl_filename_value = (Label) ContractManager.scene.lookup("#lbl_filename_value");
            lbl_filename_value.setText(selectedFile.getCompleteFileName());

            // update label with file path
            Label lbl_path_value = (Label) ContractManager.scene.lookup("#lbl_path_value");
            lbl_path_value.setText(selectedFile.getShortPath());

            // display number of contracts for each selected design by contract type
            for (Map.Entry<ContractType, Boolean> entry : ContractManager.dataModel.getContractTypes().entrySet()) {
                ContractType contractType = entry.getKey();
                boolean used = entry.getValue();

                if (used) {
                    Label lbl_value = (Label) ContractManager.scene.lookup("#lbl_" + contractType.name());
                    lbl_value.setText("" + selectedFile.getJavaFileStatistics().getNumberOfContracts().get(contractType));
                }
            }

            // once some file is selected - Show details button becomes available
            Button btn_show_details = (Button) ContractManager.scene.lookup("#btn_show_details");
            btn_show_details.setDisable(false);
        }
    }

    public FileList getFileList() {
        return fileList;
    }
}

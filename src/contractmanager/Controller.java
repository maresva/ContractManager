package contractmanager;

import cz.zcu.kiv.contractparser.Api;
import cz.zcu.kiv.contractparser.model.JavaFile;
import cz.zcu.kiv.contractparser.io.IOServices;
import cz.zcu.kiv.contractparser.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Controller {

    private Stage stage;



    public void initialize() {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void addFiles(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java", "*.java"),
                new FileChooser.ExtensionFilter("Class", "*.class")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null) {


            HashMap<ContractType,Boolean> contractTypes = new HashMap<>();

            contractTypes.put(ContractType.GUAVA, true);
            contractTypes.put(ContractType.JSR305, false);

            JavaFile javaFile = Api.retrieveContracts(files.get(0), contractTypes);
            //JavaFile javaFile1 = ContractParser.retrieveContracts(files.get(1));


            TextArea ta_details = (TextArea) Launcher.scene.lookup("#ta_details");

            System.out.println(ta_details);
            ta_details.setText("New file loaded: " + files.get(0).getName());
            //ta_details.appendText("New file loaded: " + files.get(1).getName());
            IOServices.exportToJson(javaFile, "D:/test/Test2");
        }
    }
}

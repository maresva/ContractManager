package contractmanager.utility;

import contractmanager.view.ContractManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.apache.log4j.Logger;

public class Utils {

    /** Log4j logger for this class */
    private final static Logger logger = Logger.getLogger(String.valueOf(Utils.class));


    public static void centerScene(Scene mainScene, Scene scene){

        double mainSceneX = mainScene.getWindow().getX();
        double mainSceneY = mainScene.getWindow().getY();
        double mainSceneWidth = mainScene.getWindow().getWidth();
        double mainSceneHeight = mainScene.getWindow().getHeight();

        double detailsSceneWidth = scene.getWindow().getWidth();
        double detailsSceneHeight = scene.getWindow().getHeight();

        scene.getWindow().setX(mainSceneX + (mainSceneWidth-detailsSceneWidth)/2);
        scene.getWindow().setY(mainSceneY + (mainSceneHeight-detailsSceneHeight)/2);
    }


    public static Node lookup(String selector, Scene scene){

        try {
            Node node = scene.lookup(selector);

            if(node == null){
                NodeNotFound(selector, null);
                return null;
            }

            return node;
        }
        catch (Exception e){
            NodeNotFound(selector, e);
            return null;
        }
    }

    private static void NodeNotFound(String selector, Exception e){

        logger.error(ResourceHandler.getLocaleString("errorElementNotRetrieved", selector));

        if(e != null) {
            logger.error(e.getMessage());
        }

        ContractManager.closeApplication(1);
    }
}

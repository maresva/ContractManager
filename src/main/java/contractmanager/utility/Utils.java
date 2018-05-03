package contractmanager.utility;

import contractmanager.ContractManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.apache.log4j.Logger;

/**
 * This class provides few utilities used across the application such as safe node lookup or centering of scene.
 *
 * @author Vaclav Mares
 */
public class Utils {

    /** Log4j logger for this class */
    private final static Logger logger = Logger.getLogger(String.valueOf(Utils.class));


    /**
     * This method centers the second scene prior to the first one.
     *
     * @param mainScene     Main scene that is positioned
     * @param scene         Sub scene to be positioned
     */
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


    /**
     * This method tries to find a node of JavaFX element. If the search is successful the node is returned. Otherwise
     * error is logged and application is terminated.
     *
     * @param selector  Selector containing ID of the element to be found
     * @param scene     Scene where the element should be
     * @return          Found node or nothing (application is terminated)
     */
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


    /**
     * This method is called when node could not be found. It saves the error and closes the application
     * @param selector     Selector containing ID of the element for error report
     * @param e            Exception that occurred during the search (if there was any)
     */
    private static void NodeNotFound(String selector, Exception e){

        logger.error(ResourceHandler.getLocaleString("errorElementNotRetrieved", selector));

        if(e != null) {
            logger.error(e.getMessage());
        }

        ContractManager.closeApplication(1);
    }
}

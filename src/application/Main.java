package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("uidesign.fxml"));
        Scene sceneMain = new Scene(root,1120,500);
        
        primaryStage.setTitle("Deep Neural Network Demo");
        primaryStage.setScene(sceneMain);
        primaryStage.show();
    }
}

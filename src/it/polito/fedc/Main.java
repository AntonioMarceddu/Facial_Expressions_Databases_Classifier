package it.polito.fedc;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application 
{
	@Override
	public void start(Stage primaryStage) 
	{
		try 
		{
			// Loading the FXML layout.
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			// Create thea reference to the root element.
			VBox rootElement = (VBox) loader.load();
			// Scene creation.
			Scene scene = new Scene(rootElement);
			// Create and visualization of the stage with the chosen title and with the scene previously created.
			primaryStage.setTitle("Facial Expressions Databases Classifier");
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
			primaryStage.setScene(scene);
			primaryStage.show();

			// Load and initialize the controller.
			Controller controller = loader.getController();
			controller.initializeController(primaryStage);

			// Ensure proper thread closings when exiting the program.
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
			{
				@Override
				public void handle(WindowEvent event) 
				{
					controller.stopClassification();
				}
			});
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		// Loading the native OpenCV library. N.B. the library is searched in the lib directory.
		String opencvpath = System.getProperty("user.dir") + "\\lib\\";
		System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");

		launch(args);
	}
}
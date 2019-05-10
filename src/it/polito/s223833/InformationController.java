package it.polito.s223833;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class InformationController 
{
	/* JAVAFX stage. */
	private Stage stage;
	
	/* FXML buttons. */
	@FXML
	private Button CloseButton;
	
	/* FXML Textflow. */
	@FXML
	private TextFlow InformationTextFlow;

	public InformationController() 
	{
		stage = null;
	}

	/* Public method for setting the stage. */
	public void SetStage(Stage stage) 
	{
		this.stage = stage;	
		WriteTextFlowContent();
	}
	
	/* Public method for window closing. */
	public void Close()
	{
		stage.close();
	}
	
	/* Method for text writing. */	
	private void WriteTextFlowContent()
	{
		Text text1 = new Text("\nINTRODUCTION TO FACIAL EXPRESSION DATABASE CLASSIFIER\n\n");
		text1.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text1);
		
		Text text2 = new Text("Facial Expression Database Classifier (FEDC) is a program able to automatically classify images of some of the most used databases, depicting posed human faces:\r\n" + 
				"�	Extended Cohn-Kanade Database (CK+);\r\n" + 
				"�	Facial Expression Recognition 2013 Database (FER2013);\r\n" + 
				"�	Japanese Female Facial Expression (JAFFE);\r\n" + 
				"�	Multimedia Understanding Group Database (MUG);\r\n" + 
				"�	Radboud Faces Database (RaFD).\r\n\n" + 
				"In practice, FEDC exploits the pre-classification implemented by the database creators. In addition to this, FEDC is also able to do several useful operations on the images, in order to simplify the neural network training operations:\r\n" + 
				"�	scaling of the horizontal and vertical resolutions;\r\n" + 
				"�	conversion in grayscale color space;\r\n" + 
				"�	histogram equalization;\r\n" + 
				"�	face detection to crop the images to faces only.\r\n\n" + 
				"This allows, for the people who make use of this databases, to minimize the time necessary for their classification, so that they can dedicate directly to other tasks, such as training of a neural network.\r\n\n\n");
		InformationTextFlow.getChildren().add(text2);
		
		Text text3 = new Text("THE FEDC INTERFACE\n\n");
		text3.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text3);
		
		Text text6 = new Text("FEDC has a clean and essential user interface, consisting of four macro areas:\r\n" + 
				"�	in the left column, it is possible to choose the database to be classified;\r\n" + 
				"�	in the right column, it is possible to select the operations to be performed on the photos: those available have already been mentioned previously;\r\n" + 
				"�	in the lower part of the window, there are the buttons for selecting the input file, the output folder, and for starting and canceling the classification;\r\n" + 
				"�	finally, above the buttons, there is the progress bar, that indicates the progression of the current operation.\r\n\n" + 
				"It should be noted that:\r\n" + 
				"�	the user must choose a size for the photos to be classified: it must be between 48x48 and 1024x1024 pixels. For the FER2013 database, since the starting images have a 48x48 pixels resolutions, this possibility, alongside to the face cropping feature, is not available;\r\n" + 
				"�	the JAFFE and the FER2013 databases only contain grayscale images;\r\n" + 
				"�	the RaFD database also contains photos taken in profile: the program excels in the recognition of frontal photos and allows recognition to be made even for this type of photo, although it is likely that it will not be able to classify all the photos of this type.\r\n\n\n");
		InformationTextFlow.getChildren().add(text6);
		
		Text text7 = new Text("FINAL NOTES\n\n");
		text7.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text7);
		
		Text text8 = new Text("The images automatically classified with this program can be used, for example, for the creation of a neural network with Keras: using Python with the Scikit-learn library, these images can be subdivided in the training, validation, and test dataset or cross-validated.\r\n\n"
				+ "Access to the databases mentioned above is usually allowed only for research purposes: for more information, refer to the databases sites.\r\n\n" + 
				"FEDC was created by ");
		InformationTextFlow.getChildren().add(text8);
				
		Hyperlink hyperlink1 = new Hyperlink("Antonio Costantino Marceddu");
		hyperlink1.setBorder(Border.EMPTY);
		hyperlink1.setPadding(new Insets(0,0,0,0));
		hyperlink1.setOnAction(e -> {
		    if(Desktop.isDesktopSupported())
		    {
		        try 
		        {
		            Desktop.getDesktop().browse(new URI("https://www.linkedin.com/in/antonio-marceddu/"));
		        } 
		        catch (IOException e1) {Close();} 
		        catch (URISyntaxException e2) {Close();}
		    }
		});
		InformationTextFlow.getChildren().add(hyperlink1);

			
		Text text9 = new Text(" resorting to Eclipse, with Java and the addition of the OpenCV framework; the code was released under the MIT license and is available on ");
		InformationTextFlow.getChildren().add(text9);
		
		Hyperlink hyperlink2 = new Hyperlink("GitHub");
		hyperlink2.setBorder(Border.EMPTY);
		hyperlink2.setPadding(new Insets(0,0,0,0));
		hyperlink2.setOnAction(e -> {
		    if(Desktop.isDesktopSupported())
		    {
		        try 
		        {
		            Desktop.getDesktop().browse(new URI("https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier"));
		        } 
		        catch (IOException e1) {Close();} 
		        catch (URISyntaxException e2) {Close();}
		    }
		});
		InformationTextFlow.getChildren().add(hyperlink2);
		
		Text text10 = new Text(".\r\n");
		InformationTextFlow.getChildren().add(text10);
	}
}
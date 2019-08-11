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

		Text text2 = new Text("Facial Expression Database Classifier (FEDC) is a program able to automatically classify images of some of the most used databases, depicting posed human faces:\r\n"
						+ "•	Extended Cohn-Kanade Database (CK+);\r\n" 
						+ "•	FACES Database;\r\n"
						+ "•	Facial Expression Recognition 2013 Database (FER2013);\r\n"
						+ "•	Japanese Female Facial Expression (JAFFE);\r\n"
						+ "•	Multimedia Understanding Group Database (MUG);\r\n"
						+ "•	Radboud Faces Database (RaFD);\r\n"
						+ "•	Static Facial Expressions in the Wild 2.0 (SFEW 2.0).\r\n\n"
						+ "In practice, FEDC exploits the pre-classification implemented by the database creators. In addition to this, FEDC is also able to do several useful operations on images:\r\n"
						+ "•	change image format;\r\n"
						+ "•	conversion in grayscale color space;\r\n"
						+ "•	face detection to crop the images to faces only;\r\n" 
						+ "•	histogram equalization (normal or CLAHE);\r\n"
						+ "•	scaling of horizontal and vertical resolutions;\r\n"
						+ "•	subdivision in train, validation (optional) and test dataset;\r\n"
						+ "•	transformation of rectangular images into squared ones through the addition of padding (SFEW 2.0 only).\r\n\n"
						+ "This allows, for the people who use these databases, to reduce to the minimum the time necessary for their classification and to minimize the code for activities such as, for example, the training of a neural network.\r\n\n\n"); 
		InformationTextFlow.getChildren().add(text2);

		Text text3 = new Text("THE FEDC INTERFACE\n\n");
		text3.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text3);

		Text text4 = new Text("FEDC has a clean and essential user interface, consisting of four macroareas:\r\n"
				+ "•	on the left column it is possible to choose the database to be classified;\r\n"
				+ "•	in the other columns it is possible to select the operations to be performed on the photos: those available have already been mentioned previously;\r\n"
				+ "•	on the lower part of the window there are buttons for selecting input file, output folder, and for starting and canceling the classification;\r\n"
				+ "•	finally, above the buttons, there is the progress bar, that indicates the progression of the current operation.\r\n\n\n");
		InformationTextFlow.getChildren().add(text4);
				
		Text text5 = new Text("INFORMATIONS ABOUT SUPPORTED DATABASES\n\n");
		text5.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text5);
		
		Text text6 = new Text("•	FER+ annotations will be applied by FEDC using the \"Majority Voting\" modality. For more information, please visit the "); 
		InformationTextFlow.getChildren().add(text6);
		
		Hyperlink hyperlink1 = new Hyperlink("FER+ annotations Github page");
		hyperlink1.setBorder(Border.EMPTY);
		hyperlink1.setPadding(new Insets(0, 0, 0, 0));
		hyperlink1.setOnAction(e -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://github.com/microsoft/FERPlus"));
				}
				catch (URISyntaxException | IOException e1) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink1);
			
		Text text7 = new Text(";\n•	JAFFE and FER2013 databases contains only grayscale images;\r\n"
				+ "•	RaFD database also contains photos taken in profile: FEDC excels in the recognition of frontal photos and allows recognition to be made even for this type of photo, although it is likely that it will not be able to classify all the photos of this type;\r\n"
				+ "•	SFEW 2.0 database has a natural subdivision between train, validation and test dataset, but only the first two have been classified: so, FEDC will optionally perform the subdivision only on these. However, the chosen transformations will be applied also to the test dataset, so as to give the user the freedom to use it;\r\n"
				+ "•	regarding the MUG database, FEDC only works with the manual annotated images;\r\n"
				+ "•	regarding the SFEW 2.0 database, FEDC only works with the aligned faces images.\r\n\n\n");
		InformationTextFlow.getChildren().add(text7);

		Text text8 = new Text("UPDATES\n\n");
		text8.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text8);

		Text text9 = new Text("•	10/05/2019 - Version 1.0.0 released.\r\n"
				+ "•	10/07/2019 - Version 2.0.0 released: subdivision function between train, validation, and test dataset has been added; many minor improvements to the code and its readability have also been made.\r\n"
				+ "•	14/07/2019 - Version 3.0.0 released: support to FACES database has been added; the change image format function has been added; many corrections and improvements to the code and its readability have also been made.\r\n"
				+ "•	26/07/2019 - Version 3.1.0 released: option for creating or not the validation folder during the subdivision added; small fixes to the code have also been made.\r\n"
				+ "•	11/08/2019 - Version 4.0.0 released: support to SFEW 2.0 database has been added; support to FER+ annotations added; CLAHE option added; support to PGM and PPM formats added; option to transform database with rectangular images into squared ones added (currently only for SFEW 2.0); many corrections and improvements to the code and its readability have also been made.\r\n\n\n");
		InformationTextFlow.getChildren().add(text9);

		Text text10 = new Text("FINAL NOTES\n\n");
		text10.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text10);

		Text text11 = new Text("The images automatically classified with FEDC can be used, for example, for the training of a neural network capable of recognizing facial expressions, through the use of Keras or similar frameworks.\r\n\n"
						+ "Since the code of the program is freely usable, it is possible to implement small changes to make the program work even in totally different contexts.\r\n\n"
						+ "Access to the databases mentioned above is usually allowed only for research purposes: for more information, refer to the databases sites.\r\n\n"
						+ "FEDC was created by ");
		InformationTextFlow.getChildren().add(text11);

		Hyperlink hyperlink2 = new Hyperlink("Antonio Costantino Marceddu");
		hyperlink2.setBorder(Border.EMPTY);
		hyperlink2.setPadding(new Insets(0, 0, 0, 0));
		hyperlink2.setOnAction(e -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://www.linkedin.com/in/antonio-marceddu/"));
				} 
				catch (URISyntaxException | IOException e1) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink2);

		Text text12 = new Text(" resorting to Eclipse, with Java and the addition of the OpenCV framework; the code was released under the MIT license and is available on ");
		InformationTextFlow.getChildren().add(text12);

		Hyperlink hyperlink3 = new Hyperlink("GitHub");
		hyperlink3.setBorder(Border.EMPTY);
		hyperlink3.setPadding(new Insets(0, 0, 0, 0));
		hyperlink3.setOnAction(e -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier"));
				}
				catch (URISyntaxException | IOException e1) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink3);

		Text text13 = new Text(".\r\n");
		InformationTextFlow.getChildren().add(text13);
	}
}

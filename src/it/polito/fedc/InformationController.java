package it.polito.fedc;

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
		Text text1 = new Text("\nINTRODUCTION TO FACIAL EXPRESSIONS DATABASES CLASSIFIER\n\n");
		text1.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text1);

		Text text2 = new Text("Facial Expressions Databases Classifier (FEDC) is a program able to automatically classify images of some of the most used databases, depicting posed human faces:\r\n"
						+ "•	Extended Cohn-Kanade Database (CK+);\r\n" 
						+ "•	FACES Database;\r\n"
						+ "•	Facial Expression Recognition 2013 Database (FER2013);\r\n"
						+ "•	Indian Movie Face Database (IMFDB);\r\n"
						+ "•	Japanese Female Facial Expression Database (JAFFE);\r\n"
						+ "•	Multimedia Understanding Group Database (MUG);\r\n"
						+ "•	NimStim Set Of Facial Expressions;\r\n"
						+ "•	Radboud Faces Database (RaFD);\r\n"
						+ "•	Real-world Affective Faces Database (RAF-DB);\r\n"
						+ "•	Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0).\r\n\n"
						+ "In practice, FEDC exploits the pre-classification implemented by the database creators. In addition to this, FEDC is also able to do several useful operations on images:\r\n"
						+ "•	change image format;\r\n"
						+ "•	conversion in grayscale color space;\r\n"
						+ "•	face detection to crop the images to faces only;\r\n" 
						+ "•	histogram equalization (normal or CLAHE);\r\n"
						+ "•	scaling of horizontal and vertical resolutions;\r\n"
						+ "•	subdivision in train, validation (optional) and test dataset;\r\n"
						+ "•	transformation of rectangular images into squared ones (can only be activated if the \"Face Detection and Crop\" option is not selected).\r\n\n"
						+ "This allows, for the people who use these databases, to reduce to the minimum the time necessary for their classification and to minimize the code for activities such as, for example, the training of a neural network.\r\n\n\n"); 
		InformationTextFlow.getChildren().add(text2);

		Text text3 = new Text("INFORMATION ABOUT THE PROGRAM\n\n");
		text3.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text3);

		Text text4 = new Text("Starting from 2018, FEDC has been developed, and continues to be so, using Java 8 with the JavaFX GUI package as the programming language with the addition of the OpenCV library. In the lib folder you can find the needed libraries for Windows. If you are using a different OS, you need to download and link the latest version of OpenCV 3 to the code.\r\n\n\n");
		InformationTextFlow.getChildren().add(text4);
				
		Text text5 = new Text("INFORMATIONS ABOUT SUPPORTED DATABASES\n\n");
		text5.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text5);
		
		Text text6 = new Text("•	FER+ annotations will be applied by FEDC using the \"Majority Voting\" modality. For more information, please visit the "); 
		InformationTextFlow.getChildren().add(text6);
		
		Hyperlink hyperlink1 = new Hyperlink("FER+ annotations Github page");
		hyperlink1.setBorder(Border.EMPTY);
		hyperlink1.setPadding(new Insets(0, 0, 0, 0));
		hyperlink1.setOnAction(h -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://github.com/microsoft/FERPlus"));
				}
				catch (IOException | URISyntaxException e) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink1);
			
		Text text7 = new Text(";\n•	FER2013 and JAFFE databases only contain grayscale images;\r\n"
				+ "•	not all IMFDB database images can be classified, as for many of them the emotional label is absent. FEDC will catalog only those that have this label;\r\n"
				+ "•	the IMFDB database has images of different sizes. It is recommended to set the same height and width for the output image and to add a padding by selecting the appropriate option when required;\r\n"
				+ "•	in the case of MUG database, FEDC will only work with manually annotated images;\r\n"
				+ "•	NimStim Set Of Facial Expressions database also has the calm state and the classification of cases in which the mouth is open or closed: if desired, FEDC can also distinguish between these features;\r\n"
				+ "•	RaFD database also contains photos taken in profile: if you choose not to use the face cropping option, no problem will occur, but, if you select this option, it must be said that the Haar cascade classifier used for the recognition of profile faces is not as refined as that for the recognition of frontal faces and is not able to successfully classify all the photos;\r\n"
				+ "•	RAF-DB database contains both basic and compound emotions: FEDC can classify the aligned images version of both;\r\n"
				+ "•	in the case of SFEW 2.0 database, FEDC will only work with aligned face images. It also presents a natural subdivision between train, validation, and test dataset, but only the first two have been classified: FEDC will therefore optionally perform the subdivision only on these. However, the chosen transformations will also be applied to the test dataset, so as to give the user the freedom to use it.\r\n\n\n");
		InformationTextFlow.getChildren().add(text7);
		
		Text text8 = new Text("OTHER INFORMATIONS\n\n");
		text8.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text8);

		Text text9 = new Text("Jasper project has many opened vulnerabilities which are not get fixed for a long time. Therefore, by default, OpenCV does not allow you to save images with the JPEG2000 format. This option can be enabled by setting the runtime option OPENCV_IO_ENABLE_JASPER to True. For more information, read ");
		InformationTextFlow.getChildren().add(text9);
		
		Hyperlink hyperlink2 = new Hyperlink("here");
		hyperlink2.setBorder(Border.EMPTY);
		hyperlink2.setPadding(new Insets(0, 0, 0, 0));
		hyperlink2.setOnAction(h -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://github.com/opencv/opencv/issues/14058"));
				}
				catch (IOException | URISyntaxException e) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink2);
		
		Text text10 = new Text(".\r\n\n\n");
		InformationTextFlow.getChildren().add(text10);

		Text text11 = new Text("UPDATES\n\n");
		text11.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text11);

		Text text12 = new Text("•	10/05/2019 - Version 1.0.0 released.\r\n"
				+ "•	10/07/2019 - Version 2.0.0 released: subdivision function between train, validation, and test dataset has been added; many minor improvements to the code and its readability have also been made.\r\n"
				+ "•	14/07/2019 - Version 3.0.0 released: support to FACES database has been added; the change image format function has been added; many corrections and improvements to the code and its readability have also been made.\r\n"
				+ "•	26/07/2019 - Version 3.1.0 released: option for creating or not the validation directory during the subdivision added; small fixes to the code have also been made.\r\n"
				+ "•	11/08/2019 - Version 4.0.0 released: support to SFEW 2.0 database has been added; support to FER+ annotations added; CLAHE option added; support to PGM and PPM formats added; option to transform database with rectangular images into squared ones added (currently only for SFEW 2.0); many corrections and improvements to the code and its readability have also been made.\r\n"
				+ "•	10/09/2019 - Version 4.0.1 released: minor corrections to GUI and JAFFE classifier.\r\n"
				+ "•	28/09/2019 - Version 4.0.2 released: option to transform images into square ones added for CK+, FACES and RaFD database.\r\n"
				+ "•	05/10/2019 - Version 4.0.3 released: minor corrections to the code and the GUI, minor changes to the program name.\r\n"
				+ "•	03/05/2020 - Version 5.0.0 released: support to IMFDB, NimStim Set Of Facial Expressions and RAF-DB has been added; log functions added; major corrections and improvements to the code and its readability have also been made.\r\n\n\n");
		InformationTextFlow.getChildren().add(text12);

		Text text13 = new Text("FINAL NOTES\n\n");
		text13.setFill(Color.DARKVIOLET);
		InformationTextFlow.getChildren().add(text13);

		Text text14 = new Text("The images automatically classified with FEDC can be used, for example, for the training of a neural network capable of recognizing facial expressions, through the use of Keras or similar frameworks.\r\n\n"
						+ "Since the code of the program is freely usable, it is possible to implement small changes to make the program work even in totally different contexts.\r\n\n"
						+ "Access to the databases mentioned above is usually allowed only for research purposes: for more information, consult the sites related to the databases.\r\n\n"
						+ "FEDC was created by ");
		InformationTextFlow.getChildren().add(text14);

		Hyperlink hyperlink3 = new Hyperlink("Antonio Costantino Marceddu");
		hyperlink3.setBorder(Border.EMPTY);
		hyperlink3.setPadding(new Insets(0, 0, 0, 0));
		hyperlink3.setOnAction(h -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://www.linkedin.com/in/antonio-marceddu/"));
				} 
				catch (IOException | URISyntaxException e) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink3);

		Text text15 = new Text(" for any information, do not hesitate to contact him; the code was released under the MIT license and is available on ");
		InformationTextFlow.getChildren().add(text15);

		Hyperlink hyperlink4 = new Hyperlink("GitHub");
		hyperlink4.setBorder(Border.EMPTY);
		hyperlink4.setPadding(new Insets(0, 0, 0, 0));
		hyperlink4.setOnAction(h -> 
		{
			if (Desktop.isDesktopSupported()) 
			{
				try 
				{
					Desktop.getDesktop().browse(new URI("https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier"));
				}
				catch (IOException | URISyntaxException e) 
				{
					Close();
				}
			}
		});
		InformationTextFlow.getChildren().add(hyperlink4);

		Text text16 = new Text(".\r\n");
		InformationTextFlow.getChildren().add(text16);
	}
}

package it.polito.s223833;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import it.polito.s223833.classifiers.*;

public class MainController 
{
	/* JAVAFX stage. */
	private Stage primaryStage;

	/* FXML buttons. */
	@FXML
	private Button CIDButton, CODButton, ClassifyButton, AbortClassificationButton, InformationButton;
	
	/* FXML checkboxes. */
	@FXML
	private CheckBox GrayscaleCheckBox, HistogramEqualizationCheckBox, FaceDetectionCheckBox, EnableSubdivisionCheckBox;

	/* FXML labels. */
	@FXML
	private Label ProgressBarIndicator, PhaseLabel;
	
	/* FXML progressbar. */
	@FXML
	private ProgressBar ProgressBar;

	/* FXML radio buttons. */
	@FXML
	private RadioButton CKRadioButton, FER2013RadioButton, JAFFERadioButton, MUGRadioButton, RaFDRadioButton;

	/* FXML textfields. */
	@FXML
	private TextField WidthTextField, HeightTextField;

	/* Variabili. */
	String inputFile = "", emotionFile = "", outputDirectory = "";

	/* Classification thread. */
	Thread classifierThread = null;

	public MainController() 
	{
		primaryStage = null;
	}

	/* Public method for setting the stage. */
	public void SetStage(Stage stage) 
	{
		primaryStage = stage;	
	}
	
	/* Public method for opening the window containing the program information. */
	public void OpenInformationWindow() 
	{
		try 
		{
			// Loading the FXML layout.
			FXMLLoader loader = new FXMLLoader(getClass().getResource("InformationWindow.fxml"));
			// Creation of a reference to the BorderPane element.
			BorderPane element;
			element = (BorderPane) loader.load();
			// Scene creation.
			Scene scene = new Scene(element);
			// Creation and visualization of the stage with the chosen title and with the scene previously created.
			Stage infoStage = new Stage();
			infoStage.setTitle("Informations");
			infoStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png"))); 
			infoStage.setScene(scene);
			infoStage.show();	
			
			// Loading the controller.
			InformationController controller = loader.getController();				
			controller.SetStage(infoStage);		
		} 
		catch (IOException e)
		{
			ShowErrorDialog("An error occurred while opening the information window.");
		}
	}

	/* Public method called by the "Choose Input File" button. */
	public void ChooseInputFile() 
	{
		// CK+.
		if (CKRadioButton.isSelected()) 
		{
			// Selection of the extended-cohn-kanade-images.zip file.
			showInformationDialog("For the CK+ database classification, you must choose the file containing the database images (extended-cohn-kanade-images.zip) and the file containing the emotion labels (Emotion_labels.zip).");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose CK+ images file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CK+ images file", "extended-cohn-kanade-images.zip"));
			File databaseFile = fileChooser.showOpenDialog(primaryStage);
			if (databaseFile != null) 
			{
				// Selection of the Emotion_labels.zip file.
				inputFile = databaseFile.getPath();
				FileChooser emotionFileChooser = new FileChooser();
				emotionFileChooser.setTitle("Choose CK+ emotion labels file");
				emotionFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				emotionFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CK+ emotion labels file", "Emotion_labels.zip"));
				File file = emotionFileChooser.showOpenDialog(primaryStage);
				if (file != null) 
				{
					emotionFile = file.getPath();
				}
			}
		}
		// FER2013.
		else if (FER2013RadioButton.isSelected()) 
		{
			// Selection of the fer2013.csv file.
			showInformationDialog("For the FER2013 database classification, you must choose the file containing the database images (fer2013.csv).");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose FER2013 database images file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FER2013 database images file", "fer2013.csv"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null)
			{
				inputFile = selectedFile.getPath();
			}
		}
		// JAFFE.
		else if (JAFFERadioButton.isSelected()) 
		{
			// Selection of the jaffedbase.zip file.
			showInformationDialog("For the JAFFE database classification, you must choose the file containing the database images (jaffedbase.zip).");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose JAFFE database images file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAFFE database images file", "jaffedbase.zip"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null)
			{
				inputFile = selectedFile.getPath();
			}
		}
		// MUG.
		else if (MUGRadioButton.isSelected()) 
		{
			// Selection of the manual.tar file.
			showInformationDialog("For the MUG database classification, you must choose the file containing the manual annotated files (manual.tar).");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose manual MUG Database annotation file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Manual MUG Database annotation file", "manual.tar"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null) 
			{
				inputFile = selectedFile.getPath();
			}
		}
		// RaFD.
		else if (RaFDRadioButton.isSelected()) 
		{
			// Selection of the RafDDownload-*.zip file.
			showInformationDialog("For the RaFD database classification, you must choose the file containing the database images (RafDDownload-*.zip).");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose RaFD file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("RaFD file", "RafDDownload-*.zip"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null)
			{
				inputFile = selectedFile.getPath();
			}
		}
	}

	/* Public method called by the "Choose Output Directory" button. */
	public void ChooseOutputDirectory() 
	{
		// Creation of a directory chooser to select the input folder.
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select Input Directory");
		dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File selectedDirectory = dirChooser.showDialog(primaryStage);
		if (selectedDirectory != null) 
		{
			outputDirectory = selectedDirectory.getPath();
			showInformationDialog("Selected output directory:\n" + outputDirectory);
		}
	}

	/* Public method called by the "Abort Classification" button. */
	public void AbortClassification() 
	{
		try 
		{
			if ((classifierThread != null) && (classifierThread.isAlive()))
			{
				PhaseLabel.setText("Cancelling...");
				classifierThread.interrupt();
				AbortClassificationButton.setDisable(true);
			}
		} 
		catch (SecurityException e) 
		{
			ShowErrorDialog("There was an error during the thread abort:\n" + e);
			StartStopClassification(false, true);
		}
	}

	/* Public method called by the "Classify" button. */
	public void Classify() 
	{
		// Verifies that the user has chosen an existing input file and output folder.
		if ((inputFile != "") && (outputDirectory != "")) 
		{
			File inputDir = new File(inputFile);
			File outputDir = new File(outputDirectory);
			if ((inputDir.exists()) && (outputDir.exists())) 
			{
				// Verifies that the output folder is empty.
				if (outputDir.list().length == 0) 
				{
					// Verifies that the user has chosen the width and height values ​​for the output photos.
					if ((!WidthTextField.getText().isEmpty()) && (!HeightTextField.getText().isEmpty())) 
					{
						int width = 0, height = 0;
						try 
						{
							width = Integer.parseInt(WidthTextField.getText());
							height = Integer.parseInt(HeightTextField.getText());
						} 
						catch (NumberFormatException e) 
						{
							ShowErrorDialog("The width and the height must be integers.");
							return;
						}
						// Verifies that the width and height values ​​entered are between 48 and 1024.
						if (width >= 48 && width <= 1024 && height >= 48 && height <= 1024) 
						{
							boolean classify = false;
							// Create a different classification thread based on the database.
							// CK + classifier.
							if (CKRadioButton.isSelected()) 
							{
								// Verifies that the user has chosen the correct files previously.
								if ((inputFile.contains("extended-cohn-kanade-images.zip")) && emotionFile.contains("Emotion_labels.zip")) 
								{
									File emotionFileTest = new File(emotionFile);
									// Verifies that the file containing the emotion labels exists.
									if (emotionFileTest.exists()) 
									{
										CKClassifier classifier = new CKClassifier(this, inputFile, emotionFile, outputDirectory, width, height, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), FaceDetectionCheckBox.isSelected());
										classifierThread = new Thread(classifier);
										classify = true;
									} 
									else 
									{
										ShowAttentionDialog("The input file and/or the output folder does not exist.");
									}
								} 
								else 
								{
									ShowAttentionDialog("For the CK+ database classification, you must choose the file containing the database images (extended-cohn-kanade-images.zip) and the file containing the emotion labels (Emotion_labels.zip).");
								}
							}
							// FER2013 classifier.
							else if (FER2013RadioButton.isSelected()) 
							{
								// Verifies that the user has chosen the correct files previously.
								if (inputFile.contains("fer2013.csv")) 
								{
									if (GrayscaleCheckBox.isSelected()) 
									{
										ShowAttentionDialog("The FER2013 Database is already a grayscale database: grayscale option will be ignored.");
									}
									if (FaceDetectionCheckBox.isSelected())
									{
										ShowAttentionDialog("The FER2013 Database has very low quality images (48x48): face detection option will be ignored.");
									}
									if (width != 48 || height != 48) 
									{
										ShowAttentionDialog("The FER2013 Database has very low quality images (48x48): width and height will be set to 48.");
									}
									Alert alert = new Alert(AlertType.CONFIRMATION);
									alert.setTitle("Facial Expression Database Classificator");
									alert.setHeaderText("Request");
									alert.setContentText("The FER2013 has a field for an automatic subdivision of images between training, validation and test datasets. Do you want to use it to split images in this way?");
									((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
									((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");

									Optional<ButtonType> option = alert.showAndWait();

									if (option.get() == ButtonType.OK) 
									{
										Fer2013Classifier classifier = new Fer2013Classifier(this, inputFile, outputDirectory, HistogramEqualizationCheckBox.isSelected(), true);
										classifierThread = new Thread(classifier);
										classify = true;
									} 
									else if (option.get() == ButtonType.CANCEL) 
									{
										Fer2013Classifier classifier = new Fer2013Classifier(this, inputFile, outputDirectory, HistogramEqualizationCheckBox.isSelected(), false);
										classifierThread = new Thread(classifier);
										classify = true;
									}
								} 
								else
								{
									ShowAttentionDialog("For the FER2013 database classification, you must choose the file containing the database images (fer2013.csv).");
								}
							}
							// JAFFE classifier.
							else if (JAFFERadioButton.isSelected()) 
							{
								// Verifies that the user has chosen the correct files previously.
								if (inputFile.contains("jaffedbase.zip")) 
								{
									if (GrayscaleCheckBox.isSelected()) 
									{
										ShowAttentionDialog("The JAFFE Database is already a grayscale database: grayscale option will be ignored.");
									}
									JAFFEClassifier classifier = new JAFFEClassifier(this, inputFile, outputDirectory, width, height, HistogramEqualizationCheckBox.isSelected(), FaceDetectionCheckBox.isSelected());
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the JAFFE database classification, you must choose the file containing the database images (jaffedbase.zip).");
								}
							}
							// MUG classifier.
							else if (MUGRadioButton.isSelected()) 
							{
								// Verifies that the user has chosen the correct files previously.
								if (inputFile.contains("manual.tar")) 
								{
									MUGClassifier classifier = new MUGClassifier(this, inputFile, outputDirectory, width, height, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), FaceDetectionCheckBox.isSelected());
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the MUG database classification, you must choose the file containing the manual annotated files (manual.tar).");
								}
							}
							// RaFD classifier.
							else if (RaFDRadioButton.isSelected()) 
							{
								// Verifies that the user has chosen the correct files previously.
								if (inputFile.contains("RafDDownload-")) 
								{
									if(FaceDetectionCheckBox.isSelected())
									{
										Alert alert = new Alert(AlertType.CONFIRMATION);
										alert.setTitle("Facial Expression Database Classificator");
										alert.setHeaderText("Request");
										alert.setContentText("The RaFD database contains also non-frontal images. Do you want to try to classify it with the haar classifier for profiles? The result will not be optimal as with the frontal images.");
										((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
										((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");

										Optional<ButtonType> option = alert.showAndWait();

										if (option.get() == ButtonType.OK) 
										{
											RaFDClassifier classifier = new RaFDClassifier(this, inputFile, outputDirectory, width, height, GrayscaleCheckBox.isSelected(),	HistogramEqualizationCheckBox.isSelected(),	FaceDetectionCheckBox.isSelected(), true);
											classifierThread = new Thread(classifier);
											classify = true;
										} 
										else if (option.get() == ButtonType.CANCEL)
										{
											RaFDClassifier classifier = new RaFDClassifier(this, inputFile, outputDirectory, width, height, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), FaceDetectionCheckBox.isSelected(), false);
											classifierThread = new Thread(classifier);
											classify = true;
										}
									}
									else
									{
										RaFDClassifier classifier = new RaFDClassifier(this, inputFile, outputDirectory, width, height, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), FaceDetectionCheckBox.isSelected(), false);
										classifierThread = new Thread(classifier);
										classify = true;
									}									
								} 
								else 
								{
									ShowAttentionDialog("For the RaFD database classification, you must choose the file containing the database images (RafDDownload-*.zip).");
								}
							}
							// If the classify variable is true, then the classification thread will be started.
							if (classify == true) 
							{
								StartStopClassification(true, false);
								classifierThread.start();
							}
						} 
						else 
						{
							ShowAttentionDialog("You must write a width and an height between 48 and 1024.");
						}
					}
					else 
					{
						ShowAttentionDialog("You must choose a width and an height for the output images.");
					}
				} 
				else 
				{
					ShowAttentionDialog("The output directory must be empty.");
				}
			} 
			else 
			{
				ShowErrorDialog("The input file and/or the output folder does not exist.");
			}
		}
		else 
		{
			ShowAttentionDialog("You must choose an input file and an output directory.");
		}
	}

	/* Public method for starting and canceling the classification. */
	public void StartStopClassification(boolean value, boolean error) 
	{
		// Clearing the classification progress bar.
		updateProgressBar(0);
		ProgressBarIndicator.setText("0%");
		// Cross enabling-disabling the classification button and the early termination button of the classification.
		ClassifyButton.setDisable(value);
		AbortClassificationButton.setDisable(!value);
		if(error)
		{
			setPhaseLabel("Error!");
		} 
		else
		{
			setPhaseLabel("Done.");
		}
	}

	/* Public method for updating the classification progress bar. */
	public void updateProgressBar(double value) 
	{
		if (value <= 1) 
		{
			ProgressBar.setProgress(value);
			ProgressBarIndicator.setText(String.format("%.0f", value * 100) + "%");
		}
	}

	/* Public method for updating the classification phase label. */
	public void setPhaseLabel(String text) 
	{
		PhaseLabel.setText(text);
		updateProgressBar(0);
	}

	/* Public method for creating an information dialog. */
	public void showInformationDialog(String message) 
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Facial Expression Database Classificator");
		alert.setHeaderText("Information");
		alert.setContentText(message);
		alert.showAndWait();
	}

	/* Public method for creating a warning dialog. */
	public void ShowAttentionDialog(String message) 
	{
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Facial Expression Database Classificator");
		alert.setHeaderText("Attention");
		alert.setContentText(message);
		alert.showAndWait();
	}

	/* Public method for creating an error dialog. */
	public void ShowErrorDialog(String message) 
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Facial Expression Database Classificator");
		alert.setHeaderText("Error");
		alert.setContentText(message);
		alert.showAndWait();
	}
}
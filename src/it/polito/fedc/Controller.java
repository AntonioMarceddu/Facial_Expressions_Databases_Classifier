package it.polito.fedc;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import it.polito.fedc.classifiers.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller 
{
	/* JAVAFX stage. */
	private Stage primaryStage = null;

	/* FXML buttons. */
	@FXML
	private Button CIDButton, CODButton, ClassifyButton, StopClassificationButton, InformationButton;

	/* FXML checkboxes. */
	@FXML
	private CheckBox GrayscaleCheckBox, HistogramEqualizationCheckBox, FaceDetectionCheckBox, EnableSubdivisionCheckBox, EnableValidationCheckBox;

	/* FXML checkboxes. */
	@FXML
	private ComboBox<String> DatabaseComboBox, OutputImageFormatComboBox;
	
	/* FXML labels. */
	@FXML
	private Label InputFilesLabel, OutputDirectoryLabel, WidthLabel, HeightLabel, TileSizeLabel, ContrastLimitLabel, TrainPercentageLabel, TrainLabel, TestLabel, ValidationPercentageLabel, TestPercentageLabel, TotalLabel, ProgressBarLabel, PhaseLabel;
	
	/* FXML sliders. */
	@FXML
	private Slider TrainSlider, ValidationSlider, TestSlider;

	/* FXML progressbar. */
	@FXML
	private ProgressBar ProgressBar;	

	/* FXML radio buttons and toggle groups. */
	@FXML
	private RadioButton NormalHERadioButton,CLAHERadioButton;

	/* FXML textfields. */
	@FXML
	private TextField WidthTextField, HeightTextField, TileSizeTextField, ContrastLimitTextField;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

	/* Variables. */
	private String inputFile = "", inputFile2 = "", inputFile3 = "", outputDirectory = "", logDirectoryName = ".\\log\\", logFileName = "";

	private int format = 0;
	
	private double tileSize = 8, contrastLimit = 40;

	/* Classification thread. */
	Thread classifierThread = null;

	/* Public method for initializing the controller. */
	public void initializeController(Stage stage) 
	{
		// Set the stage.
		primaryStage = stage;
		// Initialize the Database ComboBox.
		initializeDatabaseComboBox();
		// Initialize the OutputImageFormat ComboBox.
		initializeOutputImageFormatComboBox();
		// Define the Sliders functionality.
		defineSlidersFunctionality();
		// Get and format the current timestamp.
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		logFileName = "log_" + sdf.format(timestamp) + ".txt";
	}	

	/* Private method for defining the Sliders functionality. */
	private void defineSlidersFunctionality() 
	{
		TrainSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) 
			{
				TrainPercentageLabel.setText(newVal.intValue() + "%");
				int total =0;
				if(ValidationSlider.isDisabled())
				{
					total = newVal.intValue() + (int) TestSlider.getValue();
					int diff = 100 - total, testVal = (int) TestSlider.getValue();
					TestSlider.setValue(testVal + diff);
				}
				else
				{
					total = newVal.intValue() + (int) ValidationSlider.getValue() + (int) TestSlider.getValue();
					int diff = 100 - total, testVal = (int) TestSlider.getValue();
					if (testVal > 1) 
					{
						TestSlider.setValue(testVal + diff);
					} 
					else 
					{
						ValidationSlider.setValue(ValidationSlider.getValue() + diff);
					}
				}
			}
		});
		
		ValidationSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) 
			{
				ValidationPercentageLabel.setText(newVal.intValue() + "%");
				int total = newVal.intValue() + (int) TrainSlider.getValue() + (int) TestSlider.getValue();
				int diff = 100 - total, testVal = (int) TestSlider.getValue();
				if (testVal > 1) 
				{
					TestSlider.setValue(testVal + diff);
				} 
				else 
				{
					TrainSlider.setValue(TrainSlider.getValue() + diff);
				}
			}
		});
		
		TestSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) 
			{
				TestPercentageLabel.setText(newVal.intValue() + "%");
				int total = 0;
				if(ValidationSlider.isDisabled())
				{
					total = newVal.intValue() + (int) TrainSlider.getValue();
					int diff = 100 - total, trainVal = (int) TrainSlider.getValue();
					TrainSlider.setValue(trainVal + diff);
				}
				else
				{
					total = newVal.intValue() + (int) TrainSlider.getValue() + (int) ValidationSlider.getValue();			
					int diff = 100 - total, trainVal = (int) TrainSlider.getValue();
					if (trainVal > 1) 
					{
						TrainSlider.setValue(trainVal + diff);
					} 
					else 
					{
						ValidationSlider.setValue(ValidationSlider.getValue() + diff);
					}
				}
			}
		});
	}
	
	/* Private method for initializing the DatabaseComboBox. */
	private void initializeDatabaseComboBox()
	{
		DatabaseComboBox.getItems().removeAll(DatabaseComboBox.getItems());
		DatabaseComboBox.getItems().addAll("Extended Cohn-Kanade Database (CK+)", 
				"FACES Database", 
				"Facial Expression Recognition 2013 Database (FER2013)",
				"Indian Movie Face Database (IMFDB)",
				"Japanese Female Facial Expression Database (JAFFE)",
				"Multimedia Understanding Group Database (MUG)", 
				"NimStim Set Of Facial Expressions",
				"Radboud Faces Database (RaFD)",
				"Real-world Affective Faces Database (RAF-DB)",
				"Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0)");
		DatabaseComboBox.getSelectionModel().select("Extended Cohn-Kanade Database (CK+)");
	}
	
	/* Private method for initializing the OutputImageFormatComboBox. */
	private void initializeOutputImageFormatComboBox()
	{
		OutputImageFormatComboBox.getItems().removeAll(OutputImageFormatComboBox.getItems());
		OutputImageFormatComboBox.getItems().addAll("Same as input", 
				"Windows Bitmap (BMP)", 
				"Joint Photographic Experts Group (JPEG)",
				"Joint Photographic Experts Group 2000 (JPEG 2000)", 
				"Portable Image Format (PGM/PPM)",
				"Portable Network Graphics (PNG)",
				"Tagged Image File Format (TIFF)");
		OutputImageFormatComboBox.getSelectionModel().select("Same as input");
	}

	/* Private method for getting the image format. */
	private void getImageFormat() 
	{
		if (OutputImageFormatComboBox.getValue().equals("Same as input")) 
		{
			format = 0;
		} 
		else if (OutputImageFormatComboBox.getValue().equals("Windows Bitmap (BMP)")) 
		{
			format = 1;
		} 
		else if (OutputImageFormatComboBox.getValue().equals("Joint Photographic Experts Group (JPEG)")) 
		{
			format = 2;
		} 
		else if (OutputImageFormatComboBox.getValue().equals("Joint Photographic Experts Group 2000 (JPEG 2000)")) 
		{
			format = 3;
		} 
		else if (OutputImageFormatComboBox.getValue().equals("Portable Image Format (PGM/PPM)")) 
		{
			format = 4;
		} 
		else if (OutputImageFormatComboBox.getValue().equals("Portable Network Graphics (PNG)")) 
		{
			format = 5;
		} 
		else if (OutputImageFormatComboBox.getValue().equals("Tagged Image File Format (TIFF)")) 
		{
			format = 6;
		}
	}

	/* Public method for opening the window containing the program information. */
	public void openInformationWindow() 
	{
		try 
		{
			// Loading the FXML layout.
			FXMLLoader loader = new FXMLLoader(getClass().getResource("InformationWindow.fxml"));
			// Create the reference to the BorderPane element.
			BorderPane element;
			element = (BorderPane) loader.load();
			// Scene creation.
			Scene scene = new Scene(element);
			// Create and visualization of the stage with the chosen title and with the scene previously created.
			Stage infoStage = new Stage();
			infoStage.setTitle("Informations");
			infoStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
			infoStage.initModality(Modality.APPLICATION_MODAL);
			infoStage.setScene(scene);
			infoStage.show();

			// Loading the controller.
			InformationController controller = loader.getController();
			controller.SetStage(infoStage);
		} 
		catch (IOException e) 
		{
			showErrorDialog("An error occurred while opening the information window.");
		}
	}
	
	/* Public method for enabling or disabling HE options. */
	public void enableOrDisableHEOptions()
	{
		NormalHERadioButton.setDisable(!NormalHERadioButton.isDisable());
		CLAHERadioButton.setDisable(!CLAHERadioButton.isDisable());
		if(CLAHERadioButton.isSelected())
		{
			boolean value = false;
			if(CLAHERadioButton.isDisable())
			{
				value = true;
			}
			TileSizeTextField.setDisable(value); 
			TileSizeLabel.setDisable(value);
			ContrastLimitTextField.setDisable(value);
			ContrastLimitLabel.setDisable(value);
		}
	}
	
	/* Public method for enabling or disabling CLAHE options. */
	public void enableOrDisableCLAHEOptions()
	{
		TileSizeTextField.setDisable(!TileSizeTextField.isDisable()); 
		ContrastLimitTextField.setDisable(!ContrastLimitTextField.isDisable());
		TileSizeLabel.setDisable(!TileSizeLabel.isDisable());
		ContrastLimitLabel.setDisable(!ContrastLimitLabel.isDisable());
	}

	/* Public method for enabling or disabling the sliders. */
	public void enableOrDisableSubdivisionSliders() 
	{
		TrainSlider.setDisable(!TrainSlider.isDisable());
		TestSlider.setDisable(!TestSlider.isDisable());
		EnableValidationCheckBox.setDisable(!EnableValidationCheckBox.isDisable());

		if((EnableValidationCheckBox.isSelected()) && (!TrainSlider.isDisabled()))
		{
			ValidationSlider.setDisable(false);
		}
		else
		{
			ValidationSlider.setDisable(true);
		}
	}
	
	/* Public method for enabling or disabling the validation slider. */
	public void enableOrDisableValidationSlider() 
	{
		ValidationSlider.setDisable(!ValidationSlider.isDisabled());
		
		double trainValue = TrainSlider.getValue();
		double testValue = TestSlider.getValue();

		if(ValidationSlider.isDisabled())
		{
			// Update the maximum.
			TrainSlider.setMax(99);
			TestSlider.setMax(99);			
			// Set validation slider value to the minimum.
			ValidationSlider.setValue(1.0);
			ValidationPercentageLabel.setText("0%");
			
			// Add one to train or test slider.
			if((int)trainValue <= 98)
			{
				TrainSlider.setValue(trainValue + 1);
			}
			else
			{
				TestSlider.setValue(testValue + 1);
			}
		}
		else
		{
			// Update the maximum.
			TrainSlider.setMax(98);
			TestSlider.setMax(98);			
			// Set validation slider value to the minimum.
			ValidationPercentageLabel.setText("1%");
			
			// Subtract one to train or test slider.
			if((int)trainValue >= 2)
			{
				TrainSlider.setValue(trainValue - 1);
			}
			else
			{
				TestSlider.setValue(testValue - 1);
			}
		}
	}

	/* Public method called by the "Choose Input File" button. */
	public void chooseInputFile() 
	{
		File databaseFile = null;
		String chosenDatabase = DatabaseComboBox.getValue();
		// CK+.
		if (chosenDatabase.equals("Extended Cohn-Kanade Database (CK+)")) 
		{
			File emotionalLabelsFile = null;
			databaseFile = createInputFileChooser(true, "For the classification of the CK+ database, you must choose the files containing the database images (extended-cohn-kanade-images.zip) and the file containing the emotions labels (Emotion_labels.zip).", "Choose CK+ database images file", "extended-cohn-kanade-images.zip");
			if (databaseFile != null) 
			{
				emotionalLabelsFile = createInputFileChooser(false, "", "Choose CK+ emotional labels file", "Emotion_labels.zip");
				if (emotionalLabelsFile != null) 
				{
					inputFile2 = emotionalLabelsFile.getPath();
					InputFilesLabel.setText("CK+ database: all files have been selected.");
				}
			}
			if(!((databaseFile == null) && (inputFile != "")) && (emotionalLabelsFile == null))
			{
				InputFilesLabel.setText("CK+ database: not all files have been selected.");
			}
		}
		// FACES.
		else if (chosenDatabase.equals("FACES Database")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the FACES database, you must choose the file containing the database images, that has this format: DDD_MMM_DD_hh-mm-ss_CEST_YYYY.zip.", "Choose the file containing the images of the FACES database", "*.zip");
		}
		// FER2013.
		else if (chosenDatabase.equals("Facial Expression Recognition 2013 Database (FER2013)")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the FER2013 database, you must choose the file containing the database images (fer2013.csv).", "Choose the file containing the images of the FER2013 database", "fer2013.csv");
		}
		// IMFDB.
		else if (chosenDatabase.equals("Indian Movie Face Database (IMFDB)")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the IMFDB database, you must choose the file containing the database images (IMFDB_final.zip).", "Choose the file containing the images of the IMFDB database", "IMFDB_final.zip");
		}
		// JAFFE.
		else if (chosenDatabase.equals("Japanese Female Facial Expression Database (JAFFE)")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the JAFFE database, you must choose the file containing the database images (jaffedbase.zip).", "Choose the file containing the images of the JAFFE database", "jaffedbase.zip");
		}
		// MUG.
		else if (chosenDatabase.equals("Multimedia Understanding Group Database (MUG)")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the MUG database, you must choose the file containing the manual annotated files (manual.tar).", "Choose the file containing the images with manual annotations of the MUG database", "manual.tar");
		}
		// NimStim.
		else if (chosenDatabase.equals("NimStim Set Of Facial Expressions")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the NimStim database, you must choose the file containing the database images (NimStim.zip).", "Choose the file containing the images of the NimStim database", "NimStim.zip");
		}
		// RaFD.
		else if (chosenDatabase.equals("Radboud Faces Database (RaFD)")) 
		{
			databaseFile = createInputFileChooser(true, "For the classification of the RaFD database, you must choose the file containing the database images (RafDDownload-*.zip).", "Choose the file containing the images of the RafD database", "RafDDownload-*.zip");
		}
		// RAF-DB.
		else if (chosenDatabase.equals("Real-world Affective Faces Database (RAF-DB)")) 
		{
			File emotionalLabelsFile = null;
			databaseFile = createInputFileChooser(true, "For the classification of the RAF-DB database, you must choose the files containing the aligned faces database images (aligned.zip) and the file containing the emotions labels (list_patition_label.txt).", "Choose RAF-DB database images file", "aligned.zip");
			if (databaseFile != null) 
			{
				emotionalLabelsFile = createInputFileChooser(false, "", "Choose RAF-DB emotional labels file", "list_patition_label.txt");
				if (emotionalLabelsFile != null) 
				{
					inputFile2 = emotionalLabelsFile.getPath();
					InputFilesLabel.setText("RAF-DB database: all files have been selected.");
				}
			}
			if(!((databaseFile == null) && (inputFile != "")) && (emotionalLabelsFile == null))
			{
				InputFilesLabel.setText("RAF-DB database: not all files have been selected.");
			}
		}
		// SFEW 2.0.
		else if (chosenDatabase.equals("Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0)")) 
		{
			File validationFile = null,testFile = null;
			databaseFile = createInputFileChooser(true, "For the classification of the SFEW 2.0 database, you must choose the files containing the images for the train (Train_Aligned_Faces.zip), validation (Val_Aligned_Faces_new.zip) and test (Test_Aligned_Faces.zip).", "Choose the file containing the train images of the SFEW 2.0 database", "Train_Aligned_Faces.zip");
			if (databaseFile != null) 
			{
				validationFile = createInputFileChooser(false, "", "Choose the file containing the validation images of the SFEW 2.0 database", "Val_Aligned_Faces_new.zip");
				if (validationFile != null) 
				{
					inputFile2 = validationFile.getPath();
					testFile = createInputFileChooser(false, "", "Choose the file containing the test images of the SFEW 2.0 database", "Test_Aligned_Faces.zip");
					if (testFile != null) 
					{
						inputFile3 = testFile.getPath();
						InputFilesLabel.setText("SFEW 2.0 database: all files have been selected.");
					}
				}
			}
			if(!((databaseFile == null) && (inputFile != "")) && ((validationFile == null) || (testFile == null)))
			{
				InputFilesLabel.setText("SFEW 2.0 database: not all files have been selected.");
			}
		}	

		if (databaseFile != null) 
		{
			inputFile = databaseFile.getPath();
			if((!chosenDatabase.equals("Extended Cohn-Kanade Database (CK+)")) && (!chosenDatabase.equals("Real-world Affective Faces Database (RAF-DB)")) && (!chosenDatabase.equals("Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0)")))
			{
				InputFilesLabel.setText(inputFile);
				// For FACES, it is necessary to make a more precise control for the name.
				if(chosenDatabase.equals("FACES Database"))
				{
					if (!inputFile.substring(inputFile.lastIndexOf('\\') + 1).matches("([a-zA-Z]{3}_){2}[0-9]{2}_[0-9]{2}(-[0-9]{2}){2}_CEST_[0-9]{4}\\.zip")) 
					{
						InputFilesLabel.setText("FACES database: the input file does not have the correct format.");
					}
				}
			}
		}
	}

	/* Private method for creating an input file chooser. */
	private File createInputFileChooser(boolean showInformationDialog, String informationDialogText, String informationDialogTitle, String extension) 
	{
		boolean buttonOk = true;
		if (showInformationDialog)
		{
			buttonOk = showInformationDialog(informationDialogText);	
		}
		if (buttonOk)
		{
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(informationDialogTitle);
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extension, extension));
			return fileChooser.showOpenDialog(primaryStage);
		}
		return null;
	}

	/* Public method called by the "Choose Output Directory" button. */
	public void chooseOutputDirectory() 
	{
		// Create thea directory chooser to select the input directory.
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select Output Directory");
		dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File selectedDirectory = dirChooser.showDialog(primaryStage);
		if (selectedDirectory != null) 
		{
			outputDirectory = selectedDirectory.getPath();
			OutputDirectoryLabel.setText(outputDirectory);
		}
	}

	/* Public method called by the "Stop Classification" button. */
	public void stopClassification() 
	{
		try 
		{
			if ((classifierThread != null) && (classifierThread.isAlive())) 
			{
				PhaseLabel.setText("Cancelling...");
				classifierThread.interrupt();
				StopClassificationButton.setDisable(true);
			}
		} 
		catch (SecurityException e) 
		{
			showErrorDialog("There was an error during the thread stop:\n" + e);
			startStopClassification(false, 1);
		}
	}

	/* Public method called by the "Classify" button. */
	public void classify() 
	{
		// Verify that the user has chosen an existing input file and output directory.
		if ((inputFile != "") && (outputDirectory != "")) 
		{
			File databaseFile = new File(inputFile);
			File outputDir = new File(outputDirectory);
			if ((databaseFile.exists()) && (outputDir.exists())) 
			{
				// Verify that the output directory is empty.
				if (outputDir.list().length == 0) 
				{
					// Verify that the user has chosen the width and height values for the output photos.
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
							showAttentionDialog("The width and the height must be integers.");
							return;
						}
						// Verify that the width and height values entered are between 32 and 1024.
						if (width >= 32 && width <= 1024 && height >= 32 && height <= 1024) 
						{
							int histogramEqualizationType=0;
							// If the CLAHE option has been selected, check if the user has chosen the tile size and the contrast limit values.
							if ((HistogramEqualizationCheckBox.isSelected()) && (CLAHERadioButton.isSelected()))
							{
								//Set the histogram equalization type to CLAHE.
								histogramEqualizationType = 1;
								// Get the tile size.
								try
								{
									tileSize = Double.parseDouble(TileSizeTextField.getText());
								}
								catch (NumberFormatException|NullPointerException e)
								{
									showAttentionDialog("The value that has been given for the tile size is not a double or is empty. It will be set to the default value (2).");
									tileSize = 2;
								}
								// Get the contrast limit.
								try
								{
									contrastLimit = Double.parseDouble(ContrastLimitTextField.getText());
								}
								catch(NumberFormatException| NullPointerException e)
								{
									showAttentionDialog("The value that has been given for the contrast limit is not a double or is empty. It will be set to the default value (4).");
									contrastLimit = 4;
								}
							}							
							
							boolean classify = false, squareImages = false, defaultSubdivision = false;
							
							// Get the validation value.
							boolean validation = EnableSubdivisionCheckBox.isSelected() && EnableValidationCheckBox.isSelected();
							
							// Get the image format.
							getImageFormat();

							// Create a different classification thread based on the database.
							// CK+ classifier.
							if (DatabaseComboBox.getValue().equals("Extended Cohn-Kanade Database (CK+)")) 
							{
								// Verify that the user has chosen the correct files previously.
								if ((inputFile.contains("extended-cohn-kanade-images.zip"))	&& (inputFile2.contains("Emotion_labels.zip"))) 
								{
									File emotionalLabelsFileTest = new File(inputFile2);
									// Verify that the file containing the emotion labels exists.
									if (emotionalLabelsFileTest.exists()) 
									{
										if(!FaceDetectionCheckBox.isSelected())
										{
											// Alert for make square images.
											squareImages = showConfirmationDialog("The CK+ database has non-square images. Do you want FEDC to make them square?");
										}	
										
										CKClassifier classifier = new CKClassifier(this, logDirectoryName, logFileName, inputFile, inputFile2, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
										classifierThread = new Thread(classifier);
										classify = true;
									} 
									else 
									{
										showAttentionDialog("The CK+ database emotional labels file was not found.");
									}
								} 
								else 
								{
									showAttentionDialog("For the classification of the CK+ database, you must choose the files containing the database images (extended-cohn-kanade-images.zip) and the file containing the emotion labels (Emotion_labels.zip).");
								}
							}
							// FACES classifier.
							else if (DatabaseComboBox.getValue().equals("FACES Database")) 
							{
								// Verifies that the user has chosen the correct file previously.
								if (inputFile.substring(inputFile.lastIndexOf('\\') + 1).matches("([a-zA-Z]{3}_){2}[0-9]{2}_[0-9]{2}(-[0-9]{2}){2}_CEST_[0-9]{4}\\.zip")) 
								{
									if(!FaceDetectionCheckBox.isSelected())
									{
										// Alert for make square images.
										squareImages = showConfirmationDialog("The FACES database has non-square images. Do you want FEDC to make them square?");
									}	
									FACESClassifier classifier = new FACESClassifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									showAttentionDialog("For the classification of the FACES database, you must choose the file containing the database images, that has this format: DDD_MMM_DD_hh-mm-ss_CEST_YYYY.zip.");
								}
							}
							// FER2013 classifier.
							else if (DatabaseComboBox.getValue().equals("Facial Expression Recognition 2013 Database (FER2013)")) 
							{
								// Verify that the user has chosen the correct file previously.
								if (inputFile.contains("fer2013.csv")) 
								{
									FER2013Classifier classifier = null;
									String ferPlusPath = "";
									boolean ferPlus = false;
									
									if (format == 0) 
									{
										showAttentionDialog("The FER2013 Database has not a default format: will be set as default format JPEG.");
										format = 2;
									}
									if (GrayscaleCheckBox.isSelected()) 
									{
										showAttentionDialog("The FER2013 Database is already in grayscale: this option will be ignored.");
									}
									if (FaceDetectionCheckBox.isSelected()) 
									{
										showAttentionDialog("The FER2013 Database has very low resolution images (48x48): face detection option will be ignored.");
									}
									if (!EnableSubdivisionCheckBox.isSelected()) 
									{
										defaultSubdivision = showConfirmationDialog("The FER2013 has a field for an automatic subdivision of images between train, validation and test datasets. Do you want FEDC to use it to split images in this way?");
									}
									
									ferPlus = showConfirmationDialog("FEDC can use the FER+ annotations, freely downloadable from https://github.com/microsoft/FERPlus; these annotations improves the default ones. Do you want FEDC to use them to classify images?");
									if(ferPlus)
									{
										File ferPlusFile = createInputFileChooser(false, "", "Choose FER+ annotations file", "fer2013new.csv");										
										if (ferPlusFile != null) 
										{
											ferPlusPath = ferPlusFile.getAbsolutePath();
											ferPlus = true;
										}
										else
										{
											ferPlus = false;
										}
									}
									
									classifier = new FER2013Classifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, ferPlus, ferPlusPath, HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, defaultSubdivision, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									showAttentionDialog("For the classification of the FER2013 database, you must choose the file containing the database images (fer2013.csv).");
								}
							}
							// IMFDB classifier.
							else if (DatabaseComboBox.getValue().equals("Indian Movie Face Database (IMFDB)")) 
							{
								// Verifies that the user has chosen the correct file previously.
								if (inputFile.contains("IMFDB_final.zip"))
								{
									if(FaceDetectionCheckBox.isSelected())
									{
										showAttentionDialog("The IMFDB database has images already cropped to the face only: the \"Face Detection and Crop\" option will be ignored.");
									}	
									// Alert for make square images.
									squareImages = showConfirmationDialog("The IMFDB database has non-square images of different sizes. Do you want FEDC to make them square?");
									
									IMFDBClassifier classifier = new IMFDBClassifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{					
									showAttentionDialog("For the classification of the IMFDB database, you must choose the file containing the database images (IMFDB_final.zip).");
								}
							}
							// JAFFE classifier.
							else if (DatabaseComboBox.getValue().equals("Japanese Female Facial Expression Database (JAFFE)")) 
							{
								// Verify that the user has chosen the correct file previously.
								if (inputFile.contains("jaffedbase.zip")) 
								{
									if (GrayscaleCheckBox.isSelected()) 
									{
										showAttentionDialog("The JAFFE Database is already in grayscale: this option will be ignored.");
									}
									JAFFEClassifier classifier = new JAFFEClassifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									showAttentionDialog("For the classification of the JAFFE database, you must choose the file containing the database images (jaffedbase.zip).");
								}
							}
							// MUG classifier.
							else if (DatabaseComboBox.getValue().equals("Multimedia Understanding Group Database (MUG)")) 
							{
								// Verify that the user has chosen the correct file previously.
								if (inputFile.contains("manual.tar")) 
								{
									MUGClassifier classifier = new MUGClassifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									showAttentionDialog("For the classification of the MUG database, you must choose the file containing the manual annotated files (manual.tar).");
								}
							}
							// NimStim classifier.
							else if (DatabaseComboBox.getValue().equals("NimStim Set Of Facial Expressions")) 
							{
								// Verify that the user has chosen the correct file previously.
								if (inputFile.contains("NimStim.zip")) 
								{
									NimStimClassifier classifier = null;
									boolean separateMouthImages = false, separateCalmImages = false;									
									// Alert for separating calm images from neutrality ones.
									separateCalmImages = showConfirmationDialog("The NimStim database contains the \"calm\" pose, which is perceptually similar to neutrality one but in which the facial muscles are more relaxed. Do you want FEDC to separate it from neutrality one?");							
									// Alert for open and closed mouth images.
									separateMouthImages = showConfirmationDialog("The NimStim database contains open-mouth and closed-mouth version of the same emotion. Do you want FEDC to separate them?");								
									if (!FaceDetectionCheckBox.isSelected()) 
									{
										// Alert for make square images.
										squareImages = showConfirmationDialog("The NimStim database has non-square images. Do you want FEDC to make them square?");
									}

									classifier = new NimStimClassifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), separateMouthImages, separateCalmImages, squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									showAttentionDialog("For the classification of the NimStim database, you must choose the file containing the database images (NimStim.zip).");
								}
							}
							// RaFD classifier.
							else if (DatabaseComboBox.getValue().equals("Radboud Faces Database (RaFD)")) 
							{
								// Verify that the user has chosen the correct file previously.
								if (inputFile.contains("RafDDownload-")) 
								{
									RaFDClassifier classifier = null;
									boolean profileImages = false;
									if (FaceDetectionCheckBox.isSelected()) 
									{
										// Alert for detect profile images.
										profileImages = showConfirmationDialog("The RaFD database also contains non-frontal images. Do you want FEDC to detect them with the Haar cascade classifier for profiles? The result is not guaranteed to be optimal as it is for frontal images and many photos can be skipped.");
									} 
									else
									{
										// Alert for make square images.
										squareImages = showConfirmationDialog("The RAFD database has non-square images. Do you want FEDC to make them square?");
									}
									
									classifier = new RaFDClassifier(this, logDirectoryName, logFileName, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), profileImages, squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									showAttentionDialog("For the classification of the RaFD database, you must choose the file containing the database images (RafDDownload-*.zip).");
								}
							}
							// RAF-DB classifier.
							else if (DatabaseComboBox.getValue().equals("Real-world Affective Faces Database (RAF-DB)")) 
							{
								// Verify that the user has chosen the correct files previously.
								if ((inputFile.contains("aligned.zip")) && (inputFile2.contains("list_patition_label.txt")))
								{
									if (FaceDetectionCheckBox.isSelected()) 
									{
										showAttentionDialog("The RAF-DB database classification will be made on aligned photos: the \"Face Detection and Crop\" option will be ignored.");
									}
									
									RAFDBClassifier classifier = new RAFDBClassifier(this, logDirectoryName, logFileName, inputFile, inputFile2, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								}
								else 
								{
									showAttentionDialog("For the classification of the RAF-DB database, you must choose the files containing the database images (aligned.zip) and the file containing the emotion labels (list_patition_label.txt).");
								}
							}
							// SFEW 2.0 classifier.
							else if (DatabaseComboBox.getValue().equals("Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0)")) 
							{
								// Verify that the user has chosen the correct files previously.
								if ((inputFile.contains("Train_Aligned_Faces.zip")) && (inputFile2.contains("Val_Aligned_Faces_new.zip") && (inputFile3.contains("Test_Aligned_Faces.zip")))) 
								{
									boolean removeBadImages = false;
									if (FaceDetectionCheckBox.isSelected()) 
									{
										showAttentionDialog("The SFEW 2.0 database, in the \"aligned faces\" .zips, has images already cropped to the face only: the \"Face Detection and Crop\" option will be ignored.");
									}
									if (!EnableSubdivisionCheckBox.isSelected()) 
									{
										// Alert for predefined subdivision.
										defaultSubdivision = showConfirmationDialog("The SFEW 2.0 database has a predefined subdivision of images between train, validation and test datasets. Do you want FEDC to use the same subdivision?");
									}
									// Alert for removing bad images.
									removeBadImages = showConfirmationDialog("The SFEW 2.0 database has some images that do not represent human faces or that have a wrong cut. Do you want FEDC to remove them automatically?");
									// Alert for make square images.
									squareImages = showConfirmationDialog("The SFEW 2.0 database has non-square images. Do you want FEDC to make them square?");									
									
									SFEW20Classifier classifier = new SFEW20Classifier(this, logDirectoryName, logFileName, inputFile, inputFile2, inputFile3, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, removeBadImages, squareImages, defaultSubdivision, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								}
								else
								{
									showAttentionDialog("For the classification of the SFEW 2.0 database, you must choose the files containing the images for the train (Train_Aligned_Faces.zip), validation (Val_Aligned_Faces_new.zip) and test (Test_Aligned_Faces.zip).");
								}
							}
							// If the classify variable is true, then the classification thread will be started.
							if (classify == true) 
							{
								startStopClassification(true, -1);
								classifierThread.start();
							}
						} 
						else 
						{
							showAttentionDialog("You must write a width and a height between 32 and 1024.");
						}
					} 
					else 
					{
						showAttentionDialog("You must choose a width and a height for the output images.");
					}
				} 
				else 
				{
					showAttentionDialog("The output directory must be empty.");
				}
			} 
			else 
			{
				showAttentionDialog("The input file and/or the output directory does not exist/s.");
			}
		} 
		else 
		{
			showAttentionDialog("You must choose an input file and an output directory.");
		}
	}

	/* Public method for starting and canceling the classification. */
	public void startStopClassification(boolean value, int state) 
	{
		// Clearing the classification progress bar.
		updateProgressBar(0);
		ProgressBarLabel.setText("0%");
		
		// Cross enabling/disabling the classification button and the early termination button of the classification.
		ClassifyButton.setDisable(value);
		StopClassificationButton.setDisable(!value);
		
		// Enable/disable the other GUI elements.
		CIDButton.setDisable(value);
		CODButton.setDisable(value); 
		InformationButton.setDisable(value);
		DatabaseComboBox.setDisable(value);
		OutputImageFormatComboBox.setDisable(value);
		GrayscaleCheckBox.setDisable(value);
		HistogramEqualizationCheckBox.setDisable(value);
		FaceDetectionCheckBox.setDisable(value);
		EnableSubdivisionCheckBox.setDisable(value);
		WidthTextField.setDisable(value);
		HeightTextField.setDisable(value);	
		TrainPercentageLabel.setDisable(value);
		ValidationPercentageLabel.setDisable(value);
		TestPercentageLabel.setDisable(value);
		TrainLabel.setDisable(value);
		TestLabel.setDisable(value);
		WidthLabel.setDisable(value);
		HeightLabel.setDisable(value);
		
		if(HistogramEqualizationCheckBox.isSelected())
		{
			NormalHERadioButton.setDisable(value);
			CLAHERadioButton.setDisable(value);
			if(CLAHERadioButton.isSelected())
			{
				TileSizeTextField.setDisable(value);
				TileSizeLabel.setDisable(value);
				ContrastLimitTextField.setDisable(value);	
				ContrastLimitLabel.setDisable(value);
			}
		}
		
		if(EnableSubdivisionCheckBox.isSelected())
		{
			TrainSlider.setDisable(value);
			TestSlider.setDisable(value);	
			if(EnableValidationCheckBox.isSelected())
			{
				ValidationSlider.setDisable(value); 
			}
			EnableValidationCheckBox.setDisable(value);
		}

		
		// State-based phase label.
		if (state == 0) 
		{
			setPhaseLabel("Done.");
		} 
		else if (state == 1) 
		{
			setPhaseLabel("Done with errors.");
		}
		else if (state == 2) 
		{
			setPhaseLabel("Interrupted.");
		}
		else if (state == 3) 
		{
			setPhaseLabel("Error!");
		}
	}

	/* Public method for updating the classification progress bar. */
	public void updateProgressBar(double value) 
	{
		if (value <= 1) 
		{
			ProgressBar.setProgress(value);
			ProgressBarLabel.setText(String.format("%.0f", value * 100) + "%");
		}
	}

	/* Public method for updating the classification phase label. */
	public void setPhaseLabel(String text) 
	{
		PhaseLabel.setText(text);
		updateProgressBar(0);
	}
	
	/* Public method for creating a confirmation dialog. */
	public boolean showConfirmationDialog(String message)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Request");
		alert.setContentText(message);
		((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
		((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.isPresent())
		{
			if (result.get() == ButtonType.OK)
			{
			   return true;
			} 
		} 		
		return false;
	}

	/* Public method for creating an information dialog. */
	public boolean showInformationDialog(String message) 
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Information");
		alert.setContentText(message);
		Optional<ButtonType> result = alert.showAndWait();
		if(result.isPresent())
		{
			if (result.get() == ButtonType.OK)
			{
			   return true;
			} 
		} 		
		return false;
	}

	/* Public method for creating a warning dialog. */
	public void showAttentionDialog(String message) 
	{
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Attention");
		alert.setContentText(message);
		alert.showAndWait();
	}

	/* Public method for creating an error dialog. */
	public void showErrorDialog(String message) 
	{
		try 
		{
			// Create the log directory if do not exist.
			File logDirectory = new File(logDirectoryName);
			if(!logDirectory.exists()) 
			{
				logDirectory.mkdirs();
			}	
			// Instance the logger.
		 	Logger logger = Logger.getLogger(this.getClass().getName());
		 	logger.setUseParentHandlers(false);
			// Instance and set the filehandler and the formatter.
			FileHandler fileHandler;
			fileHandler = new FileHandler(logDirectoryName + logFileName, true);
		  	fileHandler.setFormatter(new SimpleFormatter());
		  	logger.addHandler(fileHandler);
		  	// Log the error message.
			logger.log(Level.SEVERE, message); 
			// Flush and close the log file handler.
			fileHandler.flush();
		  	fileHandler.close();
		} 
		catch (IOException | SecurityException e) 
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Facial Expressions Databases Classifier");
			alert.setHeaderText("Error");
			alert.setContentText("An error occurred while writing to the log.");
			alert.showAndWait();
		}
		finally
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Facial Expressions Databases Classifier");
			alert.setHeaderText("Error");
			alert.setContentText(message);
			alert.showAndWait();
		}
	}
}
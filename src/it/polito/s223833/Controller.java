package it.polito.s223833;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import it.polito.s223833.classifiers.*;

public class Controller 
{
	/* JAVAFX stage. */
	private Stage primaryStage;

	/* FXML buttons. */
	@FXML
	private Button CIDButton, CODButton, ClassifyButton, AbortClassificationButton, InformationButton;

	/* FXML checkboxes. */
	@FXML
	private CheckBox GrayscaleCheckBox, HistogramEqualizationCheckBox, FaceDetectionCheckBox, EnableSubdivisionCheckBox, EnableValidationCheckBox;

	/* FXML labels. */
	@FXML
	private Label OutputFolderLabel, TrainLabel, ValidationLabel, TestLabel, TotalLabel, ProgressBarLabel, PhaseLabel;

	/* FXML sliders. */
	@FXML
	private Slider TrainSlider, ValidationSlider, TestSlider;

	/* FXML progressbar. */
	@FXML
	private ProgressBar ProgressBar;	

	/* FXML radio buttons and toggle groups. */
	@FXML
	private RadioButton NormalHERadioButton,CLAHERadioButton;
	
	@FXML
	private ToggleGroup Database, ImageFormat;

	/* FXML textfields. */
	@FXML
	private TextField WidthTextField, HeightTextField, TileSizeTextField, ContrastLimitTextField;

	/* Variabili. */
	private String inputFile = "", inputFile2 = "", inputFile3 = "", outputDirectory = "";

	private int format = 0;
	
	private double tileSize=8, contrastLimit=40;

	/* Classification thread. */
	Thread classifierThread = null;

	public Controller() 
	{
		primaryStage = null;
	}

	/* Public method for setting the stage. */
	public void SetStage(Stage stage) 
	{
		primaryStage = stage;
		// Define the sliders functionality.
		DefineSlidersFunctionality();
	}	

	/* Private method for defining the sliders functionality. */
	private void DefineSlidersFunctionality() 
	{
		TrainSlider.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) 
			{
				TrainLabel.setText(newVal.intValue() + "%");
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
				ValidationLabel.setText(newVal.intValue() + "%");
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
				TestLabel.setText(newVal.intValue() + "%");
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

	/* Private method for getting the image format. */
	private void getImageFormat() 
	{
		RadioButton selectedRadioButton = (RadioButton) ImageFormat.getSelectedToggle();
		if (selectedRadioButton.getId().equals("SameRadioButton")) 
		{
			format = 0;
		} 
		else if (selectedRadioButton.getId().equals("BMPRadioButton")) 
		{
			format = 1;
		} 
		else if (selectedRadioButton.getId().equals("JPEGRadioButton")) 
		{
			format = 2;
		} 
		else if (selectedRadioButton.getId().equals("JPEG2000RadioButton")) 
		{
			format = 3;
		} 
		else if (selectedRadioButton.getId().equals("PIFRadioButton")) 
		{
			format = 4;
		} 
		else if (selectedRadioButton.getId().equals("PNGRadioButton")) 
		{
			format = 5;
		} 
		else if (selectedRadioButton.getId().equals("TIFFRadioButton")) 
		{
			format = 6;
		}
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
	
	/* Public method for enabling or disabling HE options. */
	public void EnableOrDisableHEOptions()
	{
		NormalHERadioButton.setDisable(!NormalHERadioButton.isDisable());
		CLAHERadioButton.setDisable(!CLAHERadioButton.isDisable());
		if(CLAHERadioButton.isSelected())
		{
			if(CLAHERadioButton.isDisable())
			{
				TileSizeTextField.setDisable(true); 
				ContrastLimitTextField.setDisable(true);
			}
			else
			{
				TileSizeTextField.setDisable(false); 
				ContrastLimitTextField.setDisable(false);
			}
		}
	}
	
	/* Public method for enabling or disabling CLAHE options. */
	public void EnableOrDisableCLAHEOptions()
	{
		TileSizeTextField.setDisable(!TileSizeTextField.isDisable()); 
		ContrastLimitTextField.setDisable(!ContrastLimitTextField.isDisable());
	}

	/* Public method for enabling or disabling the sliders. */
	public void EnableOrDisableSubdivisionSliders() 
	{
		TrainSlider.setDisable(!TrainSlider.isDisable());
		TestSlider.setDisable(!TestSlider.isDisable());
		EnableValidationCheckBox.setDisable(!EnableValidationCheckBox.isDisable());
		ValidationSlider.setDisable(true);
	}
	
	/* Public method for enabling or disabling the validation sliders. */
	public void EnableOrDisableValidationSlider() 
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
			ValidationLabel.setText("0%");
			
			// Add one to train or test slider.
			if((int)trainValue<=98)
			{
				TrainSlider.setValue(trainValue+1);
			}
			else
			{
				TestSlider.setValue(testValue+1);
			}
		}
		else
		{
			// Update the maximum.
			TrainSlider.setMax(98);
			TestSlider.setMax(98);			
			// Set validation slider value to the minimum.
			ValidationLabel.setText("1%");
			
			// Subtract one to train or test slider.
			if((int)trainValue>=2)
			{
				TrainSlider.setValue(trainValue-1);
			}
			else
			{
				TestSlider.setValue(testValue-1);
			}
		}
	}

	/* Public method called by the "Choose Input File" button. */
	public void ChooseInputFile() 
	{
		RadioButton selectedRadioButton = (RadioButton) Database.getSelectedToggle();
		File databaseFile = null;

		// CK+.
		if (selectedRadioButton.getId().equals("CKRadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the CK+ database, you must choose the files containing the database images (extended-cohn-kanade-images.zip) and the file containing the emotions labels (Emotion_labels.zip).","Choose CK+ database images file", "extended-cohn-kanade-images.zip");
			if (databaseFile != null) 
			{
				File emotionFile = CreateInputFileChooser(false, "", "Choose CK+ emotion labels file", "Emotion_labels.zip");
				if (emotionFile != null) 
				{
					inputFile2 = emotionFile.getPath();
				}
			}
		}
		// FACES.
		else if (selectedRadioButton.getId().equals("FACESRadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the FACES database, you must choose the file containing the database images, that has this format: DDD_MMM_DD_hh-mm-ss_CEST_YYYY.zip.", "Choose FACES database images file", "*.zip");
		}
		// FER2013.
		else if (selectedRadioButton.getId().equals("FER2013RadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the FER2013 database, you must choose the file containing the database images (fer2013.csv).","Choose FER2013 database images file", "fer2013.csv");
		}
		// JAFFE.
		else if (selectedRadioButton.getId().equals("JAFFERadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the JAFFE database, you must choose the file containing the database images (jaffedbase.zip).","Choose JAFFE database images file", "jaffedbase.zip");
		}
		// MUG.
		else if (selectedRadioButton.getId().equals("MUGRadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the MUG database, you must choose the file containing the manual annotated files (manual.tar).","Choose MUG database images file manually annotated", "manual.tar");
		}
		// RaFD.
		else if (selectedRadioButton.getId().equals("RaFDRadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the RaFD database, you must choose the file containing the database images (RafDDownload-*.zip).","Choose RaFD database images file", "RafDDownload-*.zip");
		}
		// SFEW 2.0.
		else if (selectedRadioButton.getId().equals("SFEW20RadioButton")) 
		{
			databaseFile = CreateInputFileChooser(true, "For the classification of the SFEW 2.0 database, you must choose the files containing the images for the train (Train_Aligned_Faces.zip), validation (Val_Aligned_Faces_new.zip) and test (Test_Aligned_Faces.zip).","Choose SFEW 2.0 train database image file", "Train_Aligned_Faces.zip");
			if (databaseFile != null) 
			{
				File validationFile = CreateInputFileChooser(false, "", "Choose SFEW 2.0 validation database images file", "Val_Aligned_Faces_new.zip");
				if (validationFile != null) 
				{
					inputFile2 = validationFile.getPath();
					File testFile = CreateInputFileChooser(false, "", "Choose SFEW 2.0 test database images file", "Test_Aligned_Faces.zip");
					if (testFile != null) 
					{
						inputFile3 = testFile.getPath();
					}
				}
			}
		}	

		if (databaseFile != null) 
		{
			inputFile = databaseFile.getPath();
		}
	}

	/* Private method for creating an input file chooser. */
	private File CreateInputFileChooser(boolean showInformationDialog, String informationDialogText, String informationDialogTitle, String extension) 
	{
		if(showInformationDialog)
		{
			showInformationDialog(informationDialogText);	
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(informationDialogTitle);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extension, extension));
		return fileChooser.showOpenDialog(primaryStage);
	}

	/* Public method called by the "Choose Output Directory" button. */
	public void ChooseOutputDirectory() 
	{
		// Creation of a directory chooser to select the input folder.
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select Output Directory");
		dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File selectedDirectory = dirChooser.showDialog(primaryStage);
		if (selectedDirectory != null) 
		{
			outputDirectory = selectedDirectory.getPath();
			OutputFolderLabel.setText("Output Directory: " + selectedDirectory.getPath());
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
		// Verify that the user has chosen an existing input file and output folder.
		if ((inputFile != "") && (outputDirectory != "")) 
		{
			File databaseFile = new File(inputFile);
			File outputDir = new File(outputDirectory);
			if ((databaseFile.exists()) && (outputDir.exists())) 
			{
				// Verify that the output folder is empty.
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
							ShowErrorDialog("The width and the height must be integers.");
							return;
						}
						// Verify that the width and height values entered are between 32 and 1024.
						if (width >= 32 && width <= 1024 && height >= 32 && height <= 1024) 
						{
							int histogramEqualizationType=0;
							// If the CLAHE option has been selected, check if the user has chosen the tile size and the contrast limit values.
							if((HistogramEqualizationCheckBox.isSelected()) && (CLAHERadioButton.isSelected()))
							{
								//Set the histogram equalization type to CLAHE.
								histogramEqualizationType=1;
								// Get the tile size.
								try
								{
									tileSize=Double.parseDouble(TileSizeTextField.getText());
								}
								catch(NumberFormatException|NullPointerException e)
								{
									ShowAttentionDialog("The value that has been given for the tile size is not a double or is empty. It will be set to the default value (2).");
									tileSize=2;
								}
								// Get the contrast limit.
								try
								{
									contrastLimit=Double.parseDouble(ContrastLimitTextField.getText());
								}
								catch(NumberFormatException| NullPointerException e)
								{
									ShowAttentionDialog("The value that has been given for the contrast limit is not a double or is empty. It will be set to the default value (4).");
									contrastLimit=4;
								}
							}							
							
							boolean classify = false;
							
							// Get the validation value.
							boolean validation=EnableSubdivisionCheckBox.isSelected()&&EnableValidationCheckBox.isSelected();
							
							// Get the image format.
							getImageFormat();

							// Get the selected radio button for the Database ToggleGroup.
							RadioButton selectedRadioButton = (RadioButton) Database.getSelectedToggle();

							// Create a different classification thread based on the database.
							// CK + classifier.
							if (selectedRadioButton.getId().equals("CKRadioButton")) 
							{
								// Verify that the user has chosen the correct files previously.
								if ((inputFile.contains("extended-cohn-kanade-images.zip"))	&& (inputFile2.contains("Emotion_labels.zip"))) 
								{
									File emotionFileTest = new File(inputFile2);
									// Verify that the file containing the emotion labels exists.
									if (emotionFileTest.exists()) 
									{
										boolean squareImages = false;
										if(!FaceDetectionCheckBox.isSelected())
										{
											// Alert for make square images.
											Alert alert = showConfirmationDialog("The CK+ database has non-square images. Do you want FEDC to make them square?");
											Optional<ButtonType> option2 = alert.showAndWait();
											if (option2.get() == ButtonType.OK) 
											{
												squareImages = true;
											}
										}	
										
										CKClassifier classifier = new CKClassifier(this, inputFile, inputFile2, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
										classifierThread = new Thread(classifier);
										classify = true;
									} 
									else 
									{
										ShowAttentionDialog("The emotions file for the CK+ database was not found.");
									}
								} 
								else 
								{
									ShowAttentionDialog("For the CK+ database classification, you must choose the files containing the database images (extended-cohn-kanade-images.zip) and the file containing the emotion labels (Emotion_labels.zip).");
								}
							}
							// FACES classifier.
							else if (selectedRadioButton.getId().equals("FACESRadioButton")) 
							{
								// Verifies that the user has chosen the correct files previously.
								if (inputFile.substring(inputFile.lastIndexOf('\\') + 1).matches("([a-zA-Z]{3}_){2}[0-9]{2}_[0-9]{2}(-[0-9]{2}){2}_CEST_[0-9]{4}\\.zip")) 
								{
									boolean squareImages = false;
									if(!FaceDetectionCheckBox.isSelected())
									{
										// Alert for make square images.
										Alert alert = showConfirmationDialog("The FACES database has non-square images. Do you want FEDC to make them square?");
										Optional<ButtonType> option2 = alert.showAndWait();
										if (option2.get() == ButtonType.OK) 
										{
											squareImages = true;
										}
									}	
									FACESClassifier classifier = new FACESClassifier(this, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the FACES database classification, you must choose the file containing the database images, that has this format: DDD_MMM_DD_hh-mm-ss_CEST_YYYY.zip.");
								}
							}
							// FER2013 classifier.
							else if (selectedRadioButton.getId().equals("FER2013RadioButton")) 
							{
								// Verify that the user has chosen the correct files previously.
								if (inputFile.contains("fer2013.csv")) 
								{
									FER2013Classifier classifier = null;
									String ferPlusPath = "";
									boolean defaultSubdivision = false, ferPlus = false;
									
									if (format == 0) 
									{
										ShowAttentionDialog("The FER2013 Database has not a default format: will be set as default format JPEG.");
										format = 2;
									}
									if (GrayscaleCheckBox.isSelected()) 
									{
										ShowAttentionDialog("The FER2013 Database is already in grayscale: this option will be ignored.");
									}
									if (FaceDetectionCheckBox.isSelected()) 
									{
										ShowAttentionDialog("The FER2013 Database has very low resolution images (48x48): face detection option will be ignored.");
									}
									if (!EnableSubdivisionCheckBox.isSelected()) 
									{
										Alert alert = showConfirmationDialog("The FER2013 has a field for an automatic subdivision of images between training, validation and test datasets. Do you want FEDC to use it to split images in this way?");
										Optional<ButtonType> option = alert.showAndWait();
										if (option.get() == ButtonType.OK) 
										{
											defaultSubdivision=true;
										} 
									}
									
									Alert alert = showConfirmationDialog("FEDC can use the FER+ annotations, freely downloadable from https://github.com/microsoft/FERPlus; these annotations improves the default ones. Do you want FEDC to use them to classify images?");
									Optional<ButtonType> option = alert.showAndWait();
									if (option.get() == ButtonType.OK) 
									{
										File ferPlusFile = CreateInputFileChooser(false, "", "Choose FER+ annotations file", "fer2013new.csv");										
										if (ferPlusFile != null) 
										{
											ferPlusPath=ferPlusFile.getAbsolutePath();
											ferPlus = true;
										}
									} 
									
									classifier = new FER2013Classifier(this, inputFile, outputDirectory, width, height, format, ferPlus, ferPlusPath, HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, defaultSubdivision, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the FER2013 database classification, you must choose the file containing the database images (fer2013.csv).");
								}
							}
							// JAFFE classifier.
							else if (selectedRadioButton.getId().equals("JAFFERadioButton")) 
							{
								// Verify that the user has chosen the correct files previously.
								if (inputFile.contains("jaffedbase.zip")) 
								{
									if (GrayscaleCheckBox.isSelected()) 
									{
										ShowAttentionDialog("The JAFFE Database is already in grayscale: this option will be ignored.");
									}
									JAFFEClassifier classifier = new JAFFEClassifier(this, inputFile, outputDirectory, width, height, format, HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the JAFFE database classification, you must choose the file containing the database images (jaffedbase.zip).");
								}
							}
							// MUG classifier.
							else if (selectedRadioButton.getId().equals("MUGRadioButton")) 
							{
								// Verify that the user has chosen the correct files previously.
								if (inputFile.contains("manual.tar")) 
								{
									MUGClassifier classifier = new MUGClassifier(this, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the MUG database classification, you must choose the file containing the manual annotated files (manual.tar).");
								}
							}
							// RaFD classifier.
							else if (selectedRadioButton.getId().equals("RaFDRadioButton")) 
							{
								// Verify that the user has chosen the correct files previously.
								if (inputFile.contains("RafDDownload-")) 
								{
									RaFDClassifier classifier = null;
									boolean profileImages = false, squareImages = false;
									if (FaceDetectionCheckBox.isSelected()) 
									{
										Alert alert = showConfirmationDialog("The RaFD database also contains non-frontal images. Do you want FEDC to try to classify them with the haar classifier for profiles? The result is not guaranteed to be optimal as it is for frontal images.");
										Optional<ButtonType> option = alert.showAndWait();
										if (option.get() == ButtonType.OK) 
										{
											profileImages = true;
										} 
									} 
									else
									{
										// Alert for make square images.
										Alert alert = showConfirmationDialog("The RAFD database has non-square images. Do you want FEDC to make them square?");
										Optional<ButtonType> option2 = alert.showAndWait();
										if (option2.get() == ButtonType.OK) 
										{
											squareImages = true;
										}
									}
									classifier = new RaFDClassifier(this, inputFile, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, FaceDetectionCheckBox.isSelected(), profileImages, squareImages, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								} 
								else 
								{
									ShowAttentionDialog("For the RaFD database classification, you must choose the file containing the database images (RafDDownload-*.zip).");
								}
							}
							// SFEW 2.0 classifier.
							else if (selectedRadioButton.getId().equals("SFEW20RadioButton")) 
							{
								// Verify that the user has chosen the correct files previously.
								if ((inputFile.contains("Train_Aligned_Faces.zip")) && (inputFile2.contains("Val_Aligned_Faces_new.zip") && (inputFile3.contains("Test_Aligned_Faces.zip")))) 
								{
									boolean defaultSubdivision = false, removeBadImages = false, squareImages = false;
									if (FaceDetectionCheckBox.isSelected()) 
									{
										ShowAttentionDialog("The SFEW 2.0 database, in the \"aligned faces\" .zips, has images already cutted to the face only: The \"Face Detection and Cut\" option will be ignored.");
									}
									if (!EnableSubdivisionCheckBox.isSelected()) 
									{
										Alert alert = showConfirmationDialog("The SFEW 2.0 database has a predefined subdivision of images between training, validation and test datasets. Do you want FEDC to use the same subdivision?");
										Optional<ButtonType> option = alert.showAndWait();
										if (option.get() == ButtonType.OK) 
										{
											defaultSubdivision=true;
										} 
									}
									
									// Alert for removing bad images.
									Alert alert = showConfirmationDialog("The SFEW 2.0 database has some images that do not represent human faces or that have a wrong cut. Do you want FEDC to remove them automatically?");
									Optional<ButtonType> option1 = alert.showAndWait();
									if (option1.get() == ButtonType.OK) 
									{
										removeBadImages = true;
									} 
									
									// Alert for make square images.
									alert.setContentText("The SFEW 2.0 database has non-square images. Do you want FEDC to make them square?");
									Optional<ButtonType> option2 = alert.showAndWait();
									if (option2.get() == ButtonType.OK) 
									{
										squareImages = true;
									}
									
									SFEW20Classifier classifier = new SFEW20Classifier(this, inputFile, inputFile2, inputFile3, outputDirectory, width, height, format, GrayscaleCheckBox.isSelected(), HistogramEqualizationCheckBox.isSelected(), histogramEqualizationType, tileSize, contrastLimit, removeBadImages, squareImages, defaultSubdivision, EnableSubdivisionCheckBox.isSelected(), validation, TrainSlider.getValue() / (double) 100, ValidationSlider.getValue() / (double) 100, TestSlider.getValue() / (double) 100);
									classifierThread = new Thread(classifier);
									classify = true;
								}
								else
								{
									ShowAttentionDialog("For the classification of the SFEW 2.0 database, you must choose the files containing the images for the train (Train_Aligned_Faces.zip), validation (Val_Aligned_Faces_new.zip) and test (Test_Aligned_Faces.zip).");
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
							ShowAttentionDialog("You must write a width and a height between 32 and 1024.");
						}
					} 
					else 
					{
						ShowAttentionDialog("You must choose a width and a height for the output images.");
					}
				} 
				else 
				{
					ShowAttentionDialog("The output directory must be empty.");
				}
			} 
			else 
			{
				ShowErrorDialog("The input file and/or the output folder does not exist/s.");
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
		ProgressBarLabel.setText("0%");
		// Cross enabling-disabling the classification button and the early termination button of the classification.
		ClassifyButton.setDisable(value);
		AbortClassificationButton.setDisable(!value);
		if (error) 
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
	public Alert showConfirmationDialog(String message)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Request");
		alert.setContentText(message);
		((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
		((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
		return alert;
	}

	/* Public method for creating an information dialog. */
	public void showInformationDialog(String message) 
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Information");
		alert.setContentText(message);
		alert.showAndWait();
	}

	/* Public method for creating a warning dialog. */
	public void ShowAttentionDialog(String message) 
	{
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Attention");
		alert.setContentText(message);
		alert.showAndWait();
	}

	/* Public method for creating an error dialog. */
	public void ShowErrorDialog(String message) 
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Facial Expressions Databases Classifier");
		alert.setHeaderText("Error");
		alert.setContentText(message);
		alert.showAndWait();
	}
}
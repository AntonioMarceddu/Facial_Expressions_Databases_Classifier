package it.polito.fedc.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;
import javafx.application.Platform;

public class NimStimClassifier extends Classifier implements Runnable 
{
	private boolean separateCalmImages = false, separateMouthImages = false, squareImages = false;
	private File openMouth = null, closedMouth = null;
	public NimStimClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean separateMouthImages, boolean separateCalmImages, boolean squareImages, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, false, true, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 325;		
		// Separate open and closed mouth images.
		this.separateMouthImages = separateMouthImages;
		// Separate calm images from neutrality ones.
		this.separateCalmImages = separateCalmImages;
		// Make image square.
		this.squareImages = squareImages;
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "NimStim Set Of Facial Expressions", "\nsquare images: " + squareImages + "\nseparate open and closed mouth images: " + separateMouthImages+ "\nseparate calm images: " + separateCalmImages + "\n")) 
		{
			return;
		}
		
		// Extraction of the images of the NimStim database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the NimStim images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the NimStim files.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Create the classification directories for the NimStim database.
			if (!separateMouthImages)
			{
				if (!createClassificationDirectories(outputDirectory)) 
				{
					return;
				}
				// Calm directory.
				if (separateCalmImages)
				{
					File calmDirectory = new File(outputDirectory, "Calm");
					calmDirectory.mkdirs();
				}
			}
			else
			{
				// Open and closed mouth directories.
				openMouth = new File(outputDirectory, "Open Mouth");
				openMouth.mkdirs();
				closedMouth = new File(outputDirectory, "Closed Mouth");
				closedMouth.mkdirs();
				// Calm directories.
				if(separateCalmImages)
				{	
					try
					{
						File calmDirectoryOpen = new File(openMouth, "Calm");
						calmDirectoryOpen.mkdirs();
						File calmDirectoryClosed = new File(closedMouth, "Calm");
						calmDirectoryClosed.mkdirs();
					} 
					catch (SecurityException e) 
					{
						exceptionManager("There was an error while creating classification directories.");
						return;
					}
				}
				// Create the other directories.
				createClassificationDirectories(openMouth.getAbsolutePath());
				createClassificationDirectories(closedMouth.getAbsolutePath());
			}			

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Read the newly extracted photos.
			File imagesFolder = new File(tempDirectory, "Crop-White Background");
			if (!imagesFolder.exists())
			{
				exceptionManager("The format of the input file is not as expected.");
				return;
			}

			// Cycle performed for every single file in the directory.
			File[] listOfInternalFiles = imagesFolder.listFiles();
			
			// Set the total number of photos; this is necessary to update the progress bar.
			numberOfPhotos = listOfInternalFiles.length;
			
			// Cycle performed for every single file in the directory.	
			for (int i = 0; (i < numberOfPhotos) && (!Thread.currentThread().isInterrupted()); i++) 
			{
				File file = listOfInternalFiles[i];
				// The .DS_Store, codebook_faces, Thumbs.db files and duplicated or problematic images (17) will be ignored.
				if (((!file.getName().equals(".DS_Store") && !file.getName().equals("codebook_faces")) && (!file.getName().equals("Thumbs.db")) && (!file.getName().equals("29m_an_c.jpg")) && (!file.getName().equals("45M_HA_X.BMP")) && (!file.getName().equals("45M_SA_O.BMP")) && (!file.getName().contains(".tiff"))))
				{
					// Verify that the filename has the typical form of the NimStim database files.
					if ((file.isFile()) && ((file.getName().matches("[0-9]{2}[a-zA-Z]_[a-zA-Z]{2}_[a-zA-Z]\\.BMP") || (file.getName().matches("[0-9]{2}[a-zA-Z]_[a-zA-Z]{2}_.[a-zA-Z]\\.BMP")))))
					{
						faceFound = true;
						Mat image = Imgcodecs.imread(file.getAbsolutePath());
						
						// Face detection and image cropping (optional).
						if (faceDetection) 
						{
							//Face crop for images in which the Haar classifier does not work.
							if ((file.getName().equals("39M_HA_O.BMP")) || (file.getName().equals("42M_AN_C.BMP")))
							{
								Mat temp = null;
								if (file.getName().equals("39M_HA_O.BMP"))
								{
									temp = image.submat(new Rect(42, 151, 420, 420));
								}
								else
								{
									temp = image.submat(new Rect(18, 70, 145, 145));									
								}		
								image.release();
								image = temp.clone();
								temp.release();
							}
							else
							{
								image = frontalFaceDetection(image);
							}
						} 
						
						// Photo classification phase.
						if (faceFound == true) 
						{
							// Make image square (optional).
							if (squareImages) 
							{
								image = makeImageSquare(image);
							}
							
							// Resize the image.
							Imgproc.resize(image, image, imageSize);
							
							// Conversion of the image in grayscale (optional).
							if (grayscale) 
							{
								Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
							}
	
							// Histogram equalization of the image (optional).
							if (histogramEqualization) 
							{
								image = histogramEqualization(image);
							}
							
							// Save the image.
							if (separateMouthImages) 
							{
								if(file.getName().substring(7,8).equalsIgnoreCase("O") || file.getName().substring(7,8).equalsIgnoreCase("X"))
								{
									SaveImage(openMouth.getAbsolutePath(), file.getName(), image);	
								}
								else
								{
									SaveImage(closedMouth.getAbsolutePath(), file.getName(), image);
								}
							}
							else
							{
								SaveImage(outputDirectory, file.getName(), image);
							}
							
							// Release the image.
							image.release();
						}
						else
						{
							logger.log(Level.WARNING, "No human face found in file " + file.getName() + ".\n"); 
						}
					}
					else 
					{
						exceptionManager("The images format in the input file is not as expected.\n");
						return;
					}
				} 	
				
				// Update the progress bar.
				updateProgressBar();
			}
		}
		// If a cancellation request has been made by the user, both temporary and classification directories will be deleted.
		if (Thread.currentThread().isInterrupted()) 
		{
			deleteAllDirectoriesAndReleaseResources("Classification interrupted by the user.\n\n\n\n");
		}
		// Otherwise only temporary ones will be deleted.
		else 
		{
			// If subdivision is active, the images will be divided between train, validation and test.
			if (subdivision) 
			{
				// If the user has chosen to enable the subdivision and to separate the open-mouth images from the closed ones, each of these will be subdivided.
				if (separateMouthImages)
				{
					Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test directory in the \"Open Mouth\" directory..."));
					if (!subdivideImages(openMouth.getAbsolutePath(), openMouth.getAbsolutePath(), trainPercentage, validationPercentage, testPercentage))
					{
						return;
					}
					Platform.runLater(() -> controller.setPhaseLabel("Phase 4: subdivision between train, validation and test directory in the \"Closed Mouth\" directory..."));
					if (!subdivideImages(closedMouth.getAbsolutePath(), closedMouth.getAbsolutePath(), trainPercentage, validationPercentage, testPercentage))
					{
						return;
					}
				}
				else
				{
					Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test directories..."));
					if (!subdivideImages(outputDirectory, outputDirectory, trainPercentage, validationPercentage, testPercentage)) 
					{
						return;
					}
				}				
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary directories..."));
			} 
			else 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: deleting temporary directories..."));
			}
			deleteTempDirectoryAndReleaseResources();
		}
	}
	
	/* Method for save the images in the corresponding directories. */
	private void SaveImage(String baseDirectory, String name, Mat image) 
	{
		String emotion = name.substring(4,6);
		if (emotion.equalsIgnoreCase("AN")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Anger", name, image);
		} 
		else if (emotion.equalsIgnoreCase("CA")) 
		{
			if(separateCalmImages)
			{
				saveImageInTheChosenFormat(baseDirectory + "\\Calm", name, image);
			}
			else
			{
				saveImageInTheChosenFormat(baseDirectory + "\\Neutrality", name, image);
			}
		} 
		else if (emotion.equalsIgnoreCase("DI")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Disgust", name, image);
		} 
		else if (emotion.equalsIgnoreCase("FE")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Fear", name, image);
		} 
		else if (emotion.equalsIgnoreCase("HA")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Happiness", name, image);
		} 
		else if (emotion.equalsIgnoreCase("NE")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Neutrality", name, image);
		}
		else if (emotion.equalsIgnoreCase("SA")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Sadness", name, image);
		}
		else if (emotion.equalsIgnoreCase("SP")) 
		{
			saveImageInTheChosenFormat(baseDirectory + "\\Surprise", name, image);
		}
		else
		{
			saveImageInTheChosenFormat(baseDirectory, name, image);
		}
	}
	
	/* Method to create the train, validation and test directory and to divide the images between them. */
	/* Overrided in order to support the NimStim Set Of Facial Expressions database features, that includes the calm state and the open - closed mouth variants. */
	@Override
	protected boolean subdivideImages(String trainValTestDirectory, String classificationDirectory, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		try 
		{
			// Creates the train directory.
			File trainDirectory, validationDirectory = null, testDirectory;
			trainDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Train", 0.1);
			
			// Creates the test directory and the inner directories.
			testDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Test", 0.2);
			if (createClassificationDirectoriesWithoutInitialization(testDirectory.getAbsolutePath())) 
			{
				if (separateCalmImages) 
				{
					File calmTestDirectory = new File(testDirectory, "Calm");
					calmTestDirectory.mkdirs();
				} 
			}
			else
			{
				return false;
			}
			
			if (validation)
			{
				// Creates the validation directory and the inner directories.
				validationDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Validation", 0.3);
				if (createClassificationDirectoriesWithoutInitialization(validationDirectory.getAbsolutePath())) 
				{
					if (separateCalmImages) 
					{
						File calmTestDirectory = new File(validationDirectory, "Calm");
						calmTestDirectory.mkdirs();
					} 
				}
				else
				{
					return false;
				}			
			}
			else
			{
				// Avoid NullPointerException.
				validationDirectory = testDirectory;
			}

			// Put a percentage of the images in the test and validation directories.
			getNRandomImages(classificationDirectory, testDirectory.getAbsolutePath(), testPercentage, validationDirectory.getAbsolutePath(), validationPercentage);

			// Move the remaining folders into the train directory.
			moveDirectoriesToDestination(new File(classificationDirectory), trainDirectory);
			
			return true;
		} 
		catch (IOException | SecurityException e) 
		{
			exceptionManager("An error occurred during the subdivision.");
			return false;
		}
	}
}
package it.polito.fedc.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;
import javafx.application.Platform;

public class FACESClassifier extends Classifier implements Runnable 
{
	private boolean squareImages = false;
	public FACESClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean squareImages, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, false, false, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 1500;		
		// Make image square.
		this.squareImages = squareImages;
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "FACES Database", "\nsquare images: " + squareImages + "\n")) 
		{
			return;
		}
		
		// Extraction of the images of the FACES database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the FACES images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the FACES images.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Create the classification directories for the FACES database.
			if (!createClassificationDirectories(outputDirectory)) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Read the newly extracted photos.
			File[] listOfFiles = new File(tempDirectory).listFiles();
			// If there is a directory, it will be open.
			if ((listOfFiles.length == 1) && (listOfFiles[0].isDirectory())) 
			{
				listOfFiles = new File(listOfFiles[0].getAbsolutePath()).listFiles();
			}
			
			// Set the total number of photos; this is necessary to update the progress bar.
			numberOfPhotos = listOfFiles.length;
			
			// Cycle performed for every single file in the directory.
			for (int i = 0; (i < numberOfPhotos) && (!Thread.currentThread().isInterrupted()); i++) 
			{
				File file = listOfFiles[i];
				
				// Verify that the filename has the typical form of the FACES database files.
				if ((file.isFile()) && (file.getName().matches("[0-9]{3}(_[a-z]){4}\\.jpg"))) 
				{
					faceFound = true;
					Mat image = Imgcodecs.imread(file.getAbsolutePath());
						
					// Face detection and image cropping (optional).
					if (faceDetection) 
					{
						image = frontalFaceDetection(image);
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
							
						String expression = file.getName().substring(8, 9);
						if (expression.equals("a")) 
						{
							saveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (expression.equals("d")) 
						{
							saveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (expression.equals("f")) 
						{
							saveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (expression.equals("h")) 
						{
							saveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (expression.equals("n")) 
						{
							saveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (expression.equals("s")) 
						{
							saveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), image);
						}
					}
					else
					{
						logger.log(Level.WARNING, "No human face found in file " + file.getName() + ".\n"); 
					}
					
					// Release the image and update the progress bar.
					image.release();
					updateProgressBar();
				} 
				else 
				{
					exceptionManager("The images format in the input file is not as expected.");
					return;
				}
			}
		}
		// If a cancellation request has been made by the user, both temporary and classification directories will be deleted.
		if (Thread.currentThread().isInterrupted()) 
		{
			deleteAllDirectoriesAndReleaseResources("Classification interrupted by the user.\n\n\n\n");
		}
		// Otherwise only temporary one will be deleted.
		else 
		{
			// If subdivision is active, the images will be divided between train, validation and test.
			if (subdivision) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test directories..."));
				if (!subdivideImages(outputDirectory, outputDirectory, trainPercentage, validationPercentage, testPercentage)) 
				{
					return;
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
}
package it.polito.fedc.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javafx.application.Platform;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;

public class JAFFEClassifier extends Classifier implements Runnable 
{
	public JAFFEClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, int width, int height, int format, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		// If a database is grayscale, it must be set up to avoid OpenCV errors in the case in which pgm/ppm has been chosen as format.
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, false, true, width, height, format, true, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "Japanese Female Facial Expression Database (JAFFE)", "\n")) 
		{
			return;
		}
				
		// Extraction of the images of the JAFFE database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the JAFFE images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the JAFFE images.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Create the classification directories for the JAFFE database.
			if (!createClassificationDirectories(outputDirectory)) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Read the newly extracted photos.
			File[] listOfFiles = new File(tempDirectory, "jaffe").listFiles();
			
			// Set the total number of photos; this is necessary to update the progress bar.
			numberOfPhotos = listOfFiles.length;
			
			// Cycle performed for every single file in the directory.
			for (int i = 0; (i < numberOfPhotos) && (!Thread.currentThread().isInterrupted()); i++) 
			{
				File file = listOfFiles[i];

				// README and .DS_Store files will be ignored.
				if ((!file.getName().equals("README")) && (!file.getName().equals(".DS_Store"))) 
				{
					// Verify that the filename has the typical form of the JAFFE database files.
					if ((file.isFile()) && (file.getName().matches("[A-Z]{2}\\.[A-Z]{2}[0-9]\\.[0-9]*\\.tiff"))) 
					{
						faceFound=true;
						Mat image = Imgcodecs.imread(file.getAbsolutePath());
						
						// Face detection and image cropping (optional).
						if (faceDetection) 
						{
							image = frontalFaceDetection(image);
						}

						// Photo classification phase.
						if (faceFound == true) 
						{
							// Resize the image.
							Imgproc.resize(image, image, imageSize);
							
							// Conversion of the image in 8 bit grayscale.
							Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);

							// Histogram equalization of the image (optional).
							if (histogramEqualization) 
							{
								image = histogramEqualization(image);
							}
							
							if (file.getName().contains("AN")) 
							{
								saveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("DI"))
							{
								saveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("FE")) 
							{
								saveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("HA")) 
							{
								saveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("NE")) 
							{
								saveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("SA")) 
							{
								saveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("SU")) 
							{
								saveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), file.getName(), image);
							}
						}
						else
						{
							logger.log(Level.WARNING, "No human face found in file " + file.getName() + ".\n"); 
						}
						
						// Release the image.
						image.release();
					} 
					else 
					{
						exceptionManager("The images format in the input file is not as expected.");
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
		// Otherwise only temporary one will be deleted.
		else 
		{
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
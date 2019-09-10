package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.s223833.Controller;
import it.polito.s223833.utils.UnzipClass;

public class JAFFEClassifier extends Classifier implements Runnable 
{
	public JAFFEClassifier(Controller controller, String inputFile, String outputDirectory, int width, int height, int format, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, inputFile, outputDirectory, false, true, width, height, format, false, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
	}

	@Override
	public void run() 
	{
		// Extraction of the images of the JAFFE database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the JAFFE images..."));
			unzipper.Unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e1) 
		{
			ExceptionManager("There was an error during the extraction of the JAFFE files.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Creation of classification directories for the JAFFE database.
			if (!CreateDirectories()) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Reading the newly extracted photos.
			File jaffeImages = new File(tempDirectory, "jaffe");
			File[] listOfFiles = jaffeImages.listFiles();
			// Cycle performed for every single file in the folder.
			int i = 0, numberOfFiles = listOfFiles.length;
			while ((i < numberOfFiles) && (!Thread.currentThread().isInterrupted())) 
			{
				File file = listOfFiles[i];

				// README and .DS_Store files will be ignored.
				if ((!file.getName().equalsIgnoreCase("README")) && (!file.getName().equalsIgnoreCase(".DS_Store"))) 
				{
					// Verify that the filename has the typical form of the JAFFE database files.
					if ((file.isFile()) && (file.getName().matches("[A-Z]{2}\\.[A-Z]{2}[0-9]\\.[0-9]*\\.tiff"))) 
					{
						faceFound=true;
						Mat image = Imgcodecs.imread(file.getAbsolutePath());
						
						// Face detection and image cropping (optional).
						if (faceDetection) 
						{
							image = FrontalFaceDetection(image);
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
								image = HistogramEqualization(image);
							}
							
							if (file.getName().contains("AN")) 
							{
								SaveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("DI"))
							{
								SaveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("FE")) 
							{
								SaveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("HA")) 
							{
								SaveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("NE")) 
							{
								SaveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("SA")) 
							{
								SaveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), image);
							} 
							else if (file.getName().contains("SU")) 
							{
								SaveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), file.getName(), image);
							}
						}
						// Release of the initialized variables.
						image.release();
					} 
					else 
					{
						ExceptionManager("The format of the images in the input file is not the one expected.");
						return;
					}
				}
				// Increase the count of the number of classified photos (or, if not classified, of the analyzed photos).
				classified++;
				// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
				percentage = (double) classified / (double) numberOfFiles;
				UpdateBar();
				// Next photo.
				i++;
			}
			// If subdivision is active, the images will be divided between train, validation and test.
			if ((subdivision) && (!Thread.currentThread().isInterrupted())) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test folder..."));
				if (!SubdivideImages(trainPercentage, validationPercentage, testPercentage)) 
				{
					ExceptionManager("There was an error while performing the subdivision.");
					return;
				}
			}
		}
		boolean error = false;
		// If a cancellation request has been made by the user, both temporary and classification folders will be deleted.
		if (Thread.currentThread().isInterrupted()) 
		{
			error = DeleteAllDirectories();
			Platform.runLater(() -> controller.ShowAttentionDialog("Classification interrupted.\n"));
		}
		// Otherwise only temporary ones will be deleted.
		else 
		{
			if (subdivision) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary folders..."));
			} 
			else 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: deleting temporary folders..."));
			}
			error = DeleteTempDirectory();
		}
		// Reset the buttons if no errors occured.
		if (!error)
		{
			Platform.runLater(() -> controller.StartStopClassification(false, false));
		}
	}
}
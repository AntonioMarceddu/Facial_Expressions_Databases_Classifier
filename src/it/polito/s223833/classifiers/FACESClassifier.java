package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.s223833.MainController;
import it.polito.s223833.utils.UnzipClass;
import javafx.application.Platform;

public class FACESClassifier extends Classifier implements Runnable 
{
	public FACESClassifier(MainController controller, String inputFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, boolean faceDetection, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, inputFile, outputDirectory, false, false, width, height, format, grayscale, histogramEqualization, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 1500;
	}

	@Override
	public void run() 
	{
		// Extraction of the images of the JAFFE database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the FACES images..."));
			unzipper.Unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e1) 
		{
			ExceptionManager("There was an error during the extraction of the FACES files.");
			return;
		}

		// Verifies if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Creation of classification directories for the FACES database.
			if (!CreateDirectories()) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: classification..."));

			// Reading the newly extracted photos.
			File facesImages = new File(tempDirectory);
			File[] listOfFiles = facesImages.listFiles();
			// If there is a folder, it will be open.
			if ((listOfFiles.length == 1) && (listOfFiles[0].isDirectory())) 
			{
				facesImages = new File(listOfFiles[0].getAbsolutePath());
				listOfFiles = facesImages.listFiles();
			}

			// Cycle performed for every single file in the folder.
			int i = 0, numberOfFiles = listOfFiles.length;
			while ((i < numberOfFiles) && (!Thread.currentThread().isInterrupted())) 
			{
				File file = listOfFiles[i];

				// Verifies that the filename has the typical form of the FACES database files.
				if ((file.isFile()) && (file.getName().matches("[0-9]{3}(_[a-z]){4}\\.jpg"))) 
				{
					faceFound = true;
					// Open the image to be analyzed.
					Mat image = Imgcodecs.imread(file.getAbsolutePath()),
							resizedFace = Mat.zeros(imageSize, CvType.CV_8UC1);

					// Conversion of the image in grayscale (optional).
					if (grayscale) 
					{
						Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
					}
					// Histogram equalization of the image (optional).
					if (histogramEqualization) 
					{
						image = HistogramEqualization(image);
					}
					// Face detection and image cropping (optional).
					if (faceDetection) 
					{
						resizedFace = FrontalFaceDetection(image, resizedFace);
					} 
					else 
					{
						// Scaling the photo to the desired size.
						Imgproc.resize(image, resizedFace, imageSize);
					}

					// Photo classification phase.
					if (faceFound == true) 
					{
						String expression = file.getName().substring(8, 9);
						if (expression.equals("a")) 
						{
							SaveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), resizedFace);
						} 
						else if (expression.equals("d")) 
						{
							SaveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), resizedFace);
						} 
						else if (expression.equals("f")) 
						{
							SaveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), resizedFace);
						} 
						else if (expression.equals("h")) 
						{
							SaveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), resizedFace);
						} 
						else if (expression.equals("n")) 
						{
							SaveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), resizedFace);
						} 
						else if (expression.equals("s")) 
						{
							SaveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), resizedFace);
						}
						// Release of the initialized variables.
						resizedFace.release();
					}
					// Release of the initialized variables.
					image.release();

					// Increase the count of the number of photos classified (or, if not classified, of the analyzed photos).
					classified++;
					// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
					percentage = (double) classified / (double) numberOfFiles;
					UpdateBar();
					// Next photo.
					i++;
				} 
				else 
				{
					ExceptionManager("The format of the images in the input file is not the one expected.");
					return;
				}
			}
			// If subdivision is active, the images will be divided between train, validation and test.
			if ((subdivision) && (!Thread.currentThread().isInterrupted()))
{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test folder..."));
				if (!SubdivideImages(trainPercentage, validationPercentage, testPercentage)) 
				{
					return;
				}
			}
		}
		// If a cancellation request has been made by the user, both temporary and classification folders will be deleted.
		if (Thread.currentThread().isInterrupted()) 
		{
			DeleteAllDirectories();
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
			DeleteTempDirectory();
		}

		Platform.runLater(() -> controller.StartStopClassification(false, false));
	}
}
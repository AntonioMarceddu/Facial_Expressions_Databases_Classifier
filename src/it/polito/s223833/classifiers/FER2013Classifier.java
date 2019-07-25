package it.polito.s223833.classifiers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

import javafx.application.Platform;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import it.polito.s223833.MainController;

public class FER2013Classifier extends Classifier implements Runnable 
{
	private int defaultWidth = 48, defaultHeight = 48, imageCounter = 0;
	private boolean defaultSubdivision;
	private File trainingDirectory = null, validationDirectory = null, testDirectory = null;

	public FER2013Classifier(MainController controller, String inputFile, String outputDirectory, int width, int height, int format, boolean histogramEqualization, boolean defaultSubdivision, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, inputFile, outputDirectory, false, true, width, height, format, false, histogramEqualization, false, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Default subdivision of the FER2013 database.
		this.defaultSubdivision = defaultSubdivision;
	}

	@Override
	public void run() 
	{
		String line = "";
		FileReader fr = null;
		BufferedReader br = null;
		File outputDir = null;

		// Case of subdivision of the photos between train, validation and test dataset.
		if (defaultSubdivision == true) 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: classification and subdivision..."));
			// Creation of training, validation and test folders.
			try 
			{
				trainingDirectory = new File(outputDirectory, "Train");
				trainingDirectory.mkdir();
				validationDirectory = new File(outputDirectory, "Validation");
				validationDirectory.mkdir();
				testDirectory = new File(outputDirectory, "Test");
				testDirectory.mkdir();
			} 
			catch (SecurityException e) 
			{
				Platform.runLater(() -> {controller.ShowErrorDialog("There was a problem while creating classification folders.\n");
					controller.StartStopClassification(false, true);});
				return;
			}
			// Creation of classification directories for train, validation and test dataset.
			if ((!CreateDirectoriesWithParameter(trainingDirectory))|| (!CreateDirectoriesWithParameter(validationDirectory)) || (!CreateDirectoriesWithParameter(testDirectory))) 
			{
				return;
			}
		}
		// Case of use of a single folder where placing the classified files.
		else 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: classification..."));
			outputDir = new File(outputDirectory);
			// Creation of classification directories for the FER2013 database.
			if (!CreateDirectoriesWithParameter(outputDir)) 
			{
				return;
			}
		}

		try 
		{
			// Creation of a read buffer for the fer2013.csv file.
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);
			// The first line, which contains information about the file, is skipped.
			line = br.readLine();

			// Cycle for reading and saving the photos.
			while (((line = br.readLine()) != null) && (!Thread.currentThread().isInterrupted())) 
			{
				String[] firstSplit = line.split(",");
				String[] secondSplit = firstSplit[1].split(" ");
				// Image size control: must be 48 * 48.
				int size = secondSplit.length;
				if (size == defaultWidth * defaultHeight) 
				{
					// Photo creation phase.
					Mat image = Mat.eye(defaultWidth, defaultHeight, CvType.CV_8UC1);
					int j, k;
					for (int i = 0; i < size; i++) 
					{
						j = i / defaultWidth;
						k = i - (defaultHeight * j);
						image.put(j, k, Integer.parseInt(secondSplit[i]));
					}
					// Resize the photo if width and height are different from 48x48.
					if((imageSize.width != defaultWidth) || (imageSize.height != defaultHeight))
					{
						Imgproc.resize(image, image, imageSize);
					}
					// Histogram equalization of the image (optional).
					if (histogramEqualization) 
					{
						Imgproc.equalizeHist(image, image);
					}
					// Saving the photo.
					if (defaultSubdivision == true) 
					{
						// Train case.
						if (firstSplit[2].equals("Training")) 
						{
							SaveImage(trainingDirectory.getAbsolutePath(), Integer.parseInt(firstSplit[0]), image);
						}
						// Validation case.
						else if (firstSplit[2].equals("PublicTest")) 
						{
							SaveImage(validationDirectory.getAbsolutePath(), Integer.parseInt(firstSplit[0]), image);
						}
						// Test case.
						else 
						{
							SaveImage(testDirectory.getAbsolutePath(), Integer.parseInt(firstSplit[0]), image);
						}
					}
					else 
					{
						SaveImage(outputDir.getAbsolutePath(), Integer.parseInt(firstSplit[0]), image);
					}

					// Release of the initialized variables.
					image.release();
				} 
				else 
				{
					ExceptionManager("Images have a different size than expected.");
					return;
				}
				// Increase the count of the number of photos classified (or, if not classified, of the analyzed photos).
				classified++;
				// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
				percentage = (double) classified / (double) 35887;
				UpdateBar();
			}
			// If subdivision is active, the images will be divided between train, validation and test.
			if ((subdivision) && (!Thread.currentThread().isInterrupted())) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 2: subdivision between train, validation and test folder..."));
				if (!SubdivideImages(trainPercentage, validationPercentage, testPercentage)) 
				{
					return;
				}
			}
			if (Thread.currentThread().isInterrupted()) 
			{
				// Elimination of all directories.
				DeleteAllDirectories();
				Platform.runLater(() -> controller.ShowAttentionDialog("Classification interrupted.\n"));
			}
			Platform.runLater(() -> controller.StartStopClassification(false, false));
		} 
		catch (IOException e) 
		{
			ExceptionManager("There was a problem while trying to read the fer2013.csv file.");
			return;
		} 
		finally 
		{
			try 
			{
				br.close();
				fr.close();
			} 
			catch (IOException e) 
			{
				ExceptionManager("There was a problem while closing the reading stream from the fer2013.csv file.");
				return;
			}
		}
	}

	/* Method for saving the images in the corresponding folders. */
	private void SaveImage(String directory, int emotion, Mat image) 
	{
		if (emotion == 0) 
		{
			SaveImageInTheChosenFormat(directory + "\\anger", imageCounter + "", image);
		} 
		else if (emotion == 1) 
		{
			SaveImageInTheChosenFormat(directory + "\\disgust", imageCounter + "", image);
		} 
		else if (emotion == 2) 
		{
			SaveImageInTheChosenFormat(directory + "\\fear", imageCounter + "", image);
		} 
		else if (emotion == 3) 
		{
			SaveImageInTheChosenFormat(directory + "\\happiness", imageCounter + "", image);
		} 
		else if (emotion == 4) 
		{
			SaveImageInTheChosenFormat(directory + "\\sadness", imageCounter + "", image);
		} 
		else if (emotion == 5) 
		{
			SaveImageInTheChosenFormat(directory + "\\surprise", imageCounter + "", image);
		} 
		else 
		{
			SaveImageInTheChosenFormat(directory + "\\neutrality", imageCounter + "", image);
		}
		// Increment of the total number of images counter.
		imageCounter++;
	}
}
package it.polito.s223833.classifiers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

import javafx.application.Platform;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.s223833.MainController;

public class Fer2013Classifier extends Classifier implements Runnable 
{
	private int width = 48, height = 48, imageCounter = 0;
	private boolean defaultSubdivision, subdivision;
	private double trainPercentage, validationPercentage, testPercentage;
	private File trainingDirectory = null, validationDirectory = null, testDirectory = null;
	
	public Fer2013Classifier(MainController controller, String inputFile, String outputDirectory, boolean histogramEqualization, boolean defaultSubdivision, boolean subdivision, double trainPercentage, double validationPercentage, double testPercentage)
	{
		super(controller, inputFile, outputDirectory, false, histogramEqualization);
		this.defaultSubdivision = defaultSubdivision;
		this.subdivision = subdivision;
		this.trainPercentage = trainPercentage;
		this.validationPercentage = validationPercentage;
		this.testPercentage = testPercentage;
	}

	@Override
	public void run() 
	{
		String line = "";
		FileReader fr = null;
		BufferedReader br = null;
		File outputDir = null;
		
		// Case of subdivision of the photos between training, validation and test dataset.
		if (defaultSubdivision == true) 
		{
			Platform.runLater(() ->	controller.setPhaseLabel("Phase 1: classification and subdivision..."));
			// Creation of training, validation and test folders.
			try 
			{
				trainingDirectory = new File(outputDirectory, "Training");
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
			boolean test1, test2, test3;
			// Creation of classification folders for training, validation and dataset testing.
			test1 = CreateClassificationSubfolders(trainingDirectory);
			test2 = CreateClassificationSubfolders(validationDirectory);
			test3 = CreateClassificationSubfolders(testDirectory);
			if ((!test1) || (!test2) || (!test3)) 
			{
		    	ExceptionManager("There was a problem while creating classification folders.\n");
		    	return;
			}
		}
		// Case of use of a single folder where placing the classified files.
		else 
		{
			Platform.runLater(() ->	controller.setPhaseLabel("Phase 1: classification..."));
			outputDir = new File(outputDirectory);
			// Creation of classification folders for the FER2013 database.
			if (!CreateClassificationSubfolders(outputDir))
			{
		    	ExceptionManager("There was a problem while creating classification folders.\n");
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
				if (size == width*height) 
				{
					// Photo creation phase.
					Mat image = Mat.eye(width, height, CvType.CV_8UC1);
					int j, k;
					for (int i = 0; i < size; i++) 
					{
						j = i / width;
						k = i - (width * j);
						image.put(j, k, Integer.parseInt(secondSplit[i]));
					}
					// Histogram equalization of the image (optional).
					if (histogramEqualization) 
					{
						Imgproc.equalizeHist(image, image);
					}
					// Saving the photo.
					if (defaultSubdivision == true) 
					{
						// Training case.
						if (firstSplit[2].equals("Training")) 
						{
							SaveImage(trainingDirectory, Integer.parseInt(firstSplit[0]), image);
						}
						// Validation case.
						else if (firstSplit[2].equals("PublicTest")) 
						{
							SaveImage(validationDirectory, Integer.parseInt(firstSplit[0]), image);
						}
						// Test case.
						else 
						{
							SaveImage(testDirectory, Integer.parseInt(firstSplit[0]), image);
						}
					} 
					else 
					{
						SaveImage(outputDir, Integer.parseInt(firstSplit[0]), image);
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
				// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
				UpdateBar();
			}		
			// If subdivision is active, the images will be divided between training, validation and test.
			if((subdivision)&&(!Thread.currentThread().isInterrupted()))
			{
				try 
				{
					Platform.runLater(() -> controller.setPhaseLabel("Phase 2: subdivision between training, validation and test folder..."));
					this.SubdivideImages(trainPercentage, validationPercentage, testPercentage);
				} 
				catch (SecurityException | IOException e) 
				{
					ExceptionManager("An error occurred during the subdivision.");
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

	/* Method of creating secondary classification directories. */
	private boolean CreateClassificationSubfolders(File directory) 
	{
		try 
		{
			CreateFoldersWithParameter(directory);
		} 
		catch (SecurityException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog("There was a problem while creating classification folders.");
			controller.StartStopClassification(false, true);});
			return false;
		}
		return true;
	}

	/* Method for saving the images in the corresponding folders. */
	private void SaveImage(File directory, int emotion, Mat image) 
	{
		if (emotion == 0) 
		{
			Imgcodecs.imwrite(directory + "\\anger\\" + imageCounter + ".jpg", image);
		}
		else if (emotion == 1) 
		{
			Imgcodecs.imwrite(directory + "\\disgust\\" + imageCounter + ".jpg", image);
		}
		else if (emotion == 2) 
		{
			Imgcodecs.imwrite(directory + "\\fear\\" + imageCounter + ".jpg", image);
		}
		else if (emotion == 3) 
		{
			Imgcodecs.imwrite(directory + "\\happiness\\" + imageCounter + ".jpg", image);
		}
		else if (emotion == 4) 
		{
			Imgcodecs.imwrite(directory + "\\sadness\\" + imageCounter + ".jpg", image);
		}
		else if (emotion == 5) 
		{
			Imgcodecs.imwrite(directory + "\\surprise\\" + imageCounter + ".jpg", image);
		}
		else
		{
			Imgcodecs.imwrite(directory + "\\neutrality\\" + imageCounter + ".jpg", image);
		}
		// Increment of the total number of images counter.
		imageCounter++;
	}
}
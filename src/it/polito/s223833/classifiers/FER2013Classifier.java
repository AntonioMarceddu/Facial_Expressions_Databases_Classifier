package it.polito.s223833.classifiers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

import javafx.application.Platform;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import it.polito.s223833.Controller;

public class FER2013Classifier extends Classifier implements Runnable 
{
	private static final int NUMIMAGES = 35887;
	
	private int defaultWidth = 48, defaultHeight = 48;
	private boolean ferPlus = false, defaultSubdivision = false;
	private String ferPlusFile;
	private File trainingDirectory = null, validationDirectory = null, testDirectory = null;

	public FER2013Classifier(Controller controller, String inputFile, String outputDirectory, int width, int height, int format, boolean ferPlus, String ferPlusFile, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean defaultSubdivision, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, inputFile, outputDirectory, ferPlus, true, width, height, format, false, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, false, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// FER+ options.
		this.ferPlus = ferPlus;
		this.ferPlusFile = ferPlusFile;
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
				Platform.runLater(() -> {controller.ShowErrorDialog("There was an error while creating classification folders.\n");
					controller.StartStopClassification(false, true);});
				return;
			}
			// Creation of classification directories for train, validation and test dataset.
			if ((!CreateDirectoriesWithParameter(trainingDirectory))|| (!CreateDirectoriesWithParameter(validationDirectory)) || (!CreateDirectoriesWithParameter(testDirectory))) 
			{
				Platform.runLater(() -> {controller.ShowErrorDialog("There was an error while creating classification folders.\n");
					controller.StartStopClassification(false, true);});
				return;
			}
		}
		// Case of use of a single folder where placing the classified files.
		else 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: execution of the operations chosen by the user..."));
			outputDir = new File(outputDirectory);
			// Creation of classification directories for the FER2013 database.
			if (!CreateDirectoriesWithParameter(outputDir)) 
			{
				Platform.runLater(() -> {controller.ShowErrorDialog("There was an error while creating classification folders.\n");
					controller.StartStopClassification(false, true);});
				return;
			}
		}

		try 
		{
			String[][] ferPlusSplits = new String[NUMIMAGES][];
			int i = 0, j = 0, k = 0, count = 0;
			if(ferPlus)
			{
				fr = new FileReader(ferPlusFile);
				br = new BufferedReader(fr);
				// The first line, which contains information about the file, is skipped.
				line = br.readLine();

				// Cycle for reading and saving the FER+ data.
				while (((line = br.readLine()) != null) && (!Thread.currentThread().isInterrupted())) 
				{
					ferPlusSplits[count] = line.split(",");
			
					// Increase the count of the number of read rows.
					count++;
				}
			}
			
			// Creation of a read buffer for the fer2013.csv file.
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);
			// The first line, which contains information about the file, is skipped.
			line = br.readLine();

			count = 0;
			String classification;
			boolean hasClass = true;
			// Cycle for reading and saving the photos.
			while (((line = br.readLine()) != null) && (!Thread.currentThread().isInterrupted())) 
			{
				String[] firstSplit = line.split(",");
				String[] secondSplit = firstSplit[1].split(" ");
				classification = firstSplit[0];
				// Image size control: must be 48 * 48.
				int size = secondSplit.length;
				if (size == defaultWidth * defaultHeight) 
				{
					// Photo creation phase.
					Mat image = Mat.eye(defaultWidth, defaultHeight, CvType.CV_8UC1);
					for (i = 0; i < size; i++) 
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
					
					// Enter if ferPlus annotations are enabled.
					if (ferPlus)
					{
						try
						{					
							hasClass = false;
							// Enter if there is a human face.
							if(!ferPlusSplits[count][1].equals(""))
							{
								j = 0;
								k = 0;
								// Pick the most voted class.
								for(i=2;i<11;i++)
								{
									if(Integer.parseInt(ferPlusSplits[count][i]) > j)
									{
										j = Integer.parseInt(ferPlusSplits[count][i]);
										k = i;
									}
								}
								// Enter if the most voted class is not "unknown".
								if(k < 10)
								{
									// Update the classification value.
									classification = k + "";							
									hasClass = true;
								}
							}
							
							// Increase the count of the number of read rows.
							count++;
						}
						catch (NumberFormatException e)
						{
							ExceptionManager("There was an error while classifying the images.");
							return;
						}
					}
					
					if (hasClass)
					{		
						try
						{
							i=Integer.parseInt(classification);
							// Saving the photo.
							if (defaultSubdivision == true) 
							{
								// Train case.
								if (firstSplit[2].equals("Training")) 
								{
									SaveImage(trainingDirectory.getAbsolutePath(), i, image);
								}
								// Validation case.
								else if (firstSplit[2].equals("PublicTest")) 
								{
									SaveImage(validationDirectory.getAbsolutePath(), i, image);
								}
								// Test case.
								else 
								{
									SaveImage(testDirectory.getAbsolutePath(), i, image);
								}
							}
							else 
							{
								SaveImage(outputDir.getAbsolutePath(), i, image);
							}
						}
						catch (NumberFormatException e)
						{
							ExceptionManager("There was an error while classifying the images.");
							return;
						}
					}
					
					// Release of the image.
					image.release();
				} 
				else 
				{
					ExceptionManager("Images have a different size than expected.");
					return;
				}
				// Increase the count of the number of classified photos (or, if not classified, of the analyzed photos).
				classified++;
				// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
				percentage = (double) classified / (double) NUMIMAGES;
				UpdateBar();
			}
			boolean error = false;
			// If subdivision is active, the images will be divided between train, validation and test.
			if ((subdivision) && (!Thread.currentThread().isInterrupted())) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 2: subdivision between train, validation and test folder..."));
				if (!SubdivideImages(trainPercentage, validationPercentage, testPercentage)) 
				{
					ExceptionManager("There was an error while performing the subdivision.");
					return;
				}
			}
			if (Thread.currentThread().isInterrupted()) 
			{
				// Elimination of all directories.
				error = DeleteAllDirectories();
				Platform.runLater(() -> controller.ShowAttentionDialog("Classification interrupted.\n"));
			}
			// Reset the buttons if no errors occured.
			if (!error)
			{
				Platform.runLater(() -> controller.StartStopClassification(false, false));
			}
		} 
		catch (IOException e) 
		{
			ExceptionManager("There was an error while trying to read the fer2013.csv file.");
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
				ExceptionManager("There was an error while closing the reading stream from the fer2013.csv file.");
				return;
			}
		}
	}

	/* Method for saving the images in the corresponding folders. */
	private void SaveImage(String directory, int emotion, Mat image) 
	{
		int number=classified+1;
		if(!ferPlus)
		{
			if (emotion == 0) 
			{
				SaveImageInTheChosenFormat(directory + "\\Anger", number + "", image);
			} 
			else if (emotion == 1) 
			{
				SaveImageInTheChosenFormat(directory + "\\Disgust", number + "", image);
			} 
			else if (emotion == 2) 
			{
				SaveImageInTheChosenFormat(directory + "\\Fear", number + "", image);
			} 
			else if (emotion == 3) 
			{
				SaveImageInTheChosenFormat(directory + "\\Happiness", number + "", image);
			} 
			else if (emotion == 4) 
			{
				SaveImageInTheChosenFormat(directory + "\\Sadness", number + "", image);
			} 
			else if (emotion == 5) 
			{
				SaveImageInTheChosenFormat(directory + "\\Surprise", number + "", image);
			} 
			else 
			{
				SaveImageInTheChosenFormat(directory + "\\Neutrality", number + "", image);
			}
		}
		else
		{
			if (emotion == 2) 
			{
				SaveImageInTheChosenFormat(directory + "\\Neutrality", number + "", image);
			} 
			else if (emotion == 3) 
			{
				SaveImageInTheChosenFormat(directory + "\\Happiness", number + "", image);
			} 
			else if (emotion == 4) 
			{
				SaveImageInTheChosenFormat(directory + "\\Surprise", number + "", image);
			} 
			else if (emotion == 5) 
			{
				SaveImageInTheChosenFormat(directory + "\\Sadness", number + "", image);
			} 
			else if (emotion == 6) 
			{
				SaveImageInTheChosenFormat(directory + "\\Anger", number + "", image);
			} 
			else if (emotion == 7) 
			{
				SaveImageInTheChosenFormat(directory + "\\Disgust", number + "", image);
			} 
			else if (emotion == 8) 
			{
				SaveImageInTheChosenFormat(directory + "\\Fear", number + "", image);
			}
			else if (emotion == 9) 
			{
				SaveImageInTheChosenFormat(directory + "\\Contempt", number + "", image);
			}
		}
	}
}
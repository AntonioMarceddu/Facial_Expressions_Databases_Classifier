package it.polito.fedc.classifiers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.io.BufferedReader;

import javafx.application.Platform;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;

public class FER2013Classifier extends Classifier implements Runnable 
{
	private int defaultWidth = 48, defaultHeight = 48;
	private boolean ferPlus = false, defaultSubdivision = false;
	private String ferPlusFile;
	private File trainDirectory = null, validationDirectory = null, testDirectory = null;

	public FER2013Classifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, int width, int height, int format, boolean ferPlus, String ferPlusFile, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean defaultSubdivision, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		// If a database is grayscale, it must be set up to avoid OpenCV errors in the case in which pgm/ppm has been chosen as format.
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, ferPlus, true, width, height, format, true, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, false, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
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
		
		// Set the total number of photos; this is necessary to update the progress bar.
		numberOfPhotos = 35887;

		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "Facial Expression Recognition 2013 Database (FER2013)", "\nFER+ annotations: " + ferPlus + "\ndefault subdivision: " + defaultSubdivision + "\n")) 
		{
			return;
		}
				
		// Case of subdivision of the photos between train, validation and test dataset.
		if (defaultSubdivision == true) 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: classification and subdivision..."));
			// Create the train, validation and test directories.
			try 
			{
				trainDirectory = new File(outputDirectory, "Train");
				trainDirectory.mkdir();
				validationDirectory = new File(outputDirectory, "Validation");
				validationDirectory.mkdir();
				testDirectory = new File(outputDirectory, "Test");
				testDirectory.mkdir();
			} 
			catch (SecurityException e) 
			{
				Platform.runLater(() -> {controller.showErrorDialog("There was an error while creating classification directories.\n");
					controller.startStopClassification(false, 3);});
				return;
			}
			// Create the classification directories for train, validation and test dataset.
			if ((!createClassificationDirectories(trainDirectory.getAbsolutePath())) || (!createClassificationDirectories(validationDirectory.getAbsolutePath())) || (!createClassificationDirectories(testDirectory.getAbsolutePath()))) 
			{
				return;
			}
		}
		// Case of use of a single directory where placing the classified files.
		else 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: execution of the operations chosen by the user..."));
			// Create the classification directories for the FER2013 database.
			if (!createClassificationDirectories(outputDirectory)) 
			{
				return;
			}
		}

		String[][] ferPlusSplits = new String[numberOfPhotos][];
		int i = 0, j = 0, k = 0, count = 0;
		if(ferPlus)
		{
			try 
			{
				fr = new FileReader(ferPlusFile);
				br = new BufferedReader(fr);
				// The first line, which contains information about the file, is skipped.
				line = br.readLine();

				// Cycle to read and save the FER+ data.
				while (((line = br.readLine()) != null) && (!Thread.currentThread().isInterrupted())) 
				{
					ferPlusSplits[count] = line.split(",");
			
					// Increase the read rows counter.
					count++;
				}
			}
			catch (IOException e) 
			{
				exceptionManager("There was an error while reading the FER+ annotations file.");
				return;
			} 
			finally 
			{
				try 
				{
					if (br != null) 
					{
						br.close();
					}	
					if (fr != null) 
					{
						fr.close();
					}	
				} 
				catch (IOException e) 
				{
					exceptionManager("There was an error while closing the reading stream from the FER+ annotations file.");
					return;
				}
			}
		}
		
		try 
		{
			// Create the read buffer for the fer2013.csv file.
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);
			// The first line, which contains information about the file, is skipped.
			line = br.readLine();

			count = 0;
			String classification = "";
			boolean hasClass = true;
			// Cycle for reading and save the photos.
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
					
					// Enter if FER+ annotations are enabled.
					if (ferPlus)
					{
						try
						{					
							hasClass = false;
							// Enter if there is a human face.
							if (!ferPlusSplits[count][1].equals(""))
							{
								j = 0;
								k = 0;
								// Pick the most voted class.
								for(i = 2; i < 11; i++)
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
							
							// Increase the read rows counter.
							count++;
						}
						catch (NumberFormatException e)
						{
							exceptionManager("An error occurred while performing the classification.");
							return;
						}
					}
					
					if (hasClass)
					{		
						try
						{
							i = Integer.parseInt(classification);
							// Save the photo.
							if (defaultSubdivision == true) 
							{
								// Train case.
								if (firstSplit[2].equals("Training")) 
								{
									SaveImage(trainDirectory.getAbsolutePath(), i, image);
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
								SaveImage(outputDirectory, i, image);
							}
						}
						catch (NumberFormatException e)
						{
							exceptionManager("An error occurred while performing the classification.");
							return;
						}
					}
					
					// Release the image.
					image.release();
				} 
				else 
				{
					exceptionManager("Images have a different size than expected.");
					return;
				}
				
				// Update the progress bar.
				updateProgressBar();
			}
			if (Thread.currentThread().isInterrupted()) 
			{
				// Elimination of all directories.
				deleteAllDirectoriesAndReleaseResources("Classification interrupted by the user.\n\n\n\n");
			}
			else
			{
				// If subdivision is active, the images will be divided between train, validation and test.
				if (subdivision) 
				{
					Platform.runLater(() -> controller.setPhaseLabel("Phase 2: subdivision between train, validation and test directories..."));
					if (!subdivideImages(outputDirectory, outputDirectory, trainPercentage, validationPercentage, testPercentage)) 
					{
						return;
					}
				}
				deleteTempDirectoryAndReleaseResources();
			}
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error while reading the fer2013.csv file.");
			return;
		} 
		catch (SecurityException e)
		{
			exceptionManager("An error occurred while closing the file handler to the log.");
			return;
		}
		finally 
		{
			try 
			{
				if (br != null) 
				{
					br.close();
			    }	
				if (fr != null) 
				{
					fr.close();
			    }	
			} 
			catch (IOException e) 
			{
				exceptionManager("There was an error while closing the reading stream from the fer2013.csv file.");
				return;
			}
		}
	}

	/* Method for save the images in the corresponding directories. */
	private void SaveImage(String directory, int emotion, Mat image) 
	{
		int number = classified + 1;
		if (!ferPlus)
		{
			if (emotion == 0) 
			{
				saveImageInTheChosenFormat(directory + "\\Anger", number + "", image);
			} 
			else if (emotion == 1) 
			{
				saveImageInTheChosenFormat(directory + "\\Disgust", number + "", image);
			} 
			else if (emotion == 2) 
			{
				saveImageInTheChosenFormat(directory + "\\Fear", number + "", image);
			} 
			else if (emotion == 3) 
			{
				saveImageInTheChosenFormat(directory + "\\Happiness", number + "", image);
			} 
			else if (emotion == 4) 
			{
				saveImageInTheChosenFormat(directory + "\\Sadness", number + "", image);
			} 
			else if (emotion == 5) 
			{
				saveImageInTheChosenFormat(directory + "\\Surprise", number + "", image);
			} 
			else 
			{
				saveImageInTheChosenFormat(directory + "\\Neutrality", number + "", image);
			}
		}
		else
		{
			if (emotion == 2) 
			{
				saveImageInTheChosenFormat(directory + "\\Neutrality", number + "", image);
			} 
			else if (emotion == 3) 
			{
				saveImageInTheChosenFormat(directory + "\\Happiness", number + "", image);
			} 
			else if (emotion == 4) 
			{
				saveImageInTheChosenFormat(directory + "\\Surprise", number + "", image);
			} 
			else if (emotion == 5) 
			{
				saveImageInTheChosenFormat(directory + "\\Sadness", number + "", image);
			} 
			else if (emotion == 6) 
			{
				saveImageInTheChosenFormat(directory + "\\Anger", number + "", image);
			} 
			else if (emotion == 7) 
			{
				saveImageInTheChosenFormat(directory + "\\Disgust", number + "", image);
			} 
			else if (emotion == 8) 
			{
				saveImageInTheChosenFormat(directory + "\\Fear", number + "", image);
			}
			else if (emotion == 9) 
			{
				saveImageInTheChosenFormat(directory + "\\Contempt", number + "", image);
			}
		}
	}
	
	/* Method to delete the temporary directory and to release the previously taken resources. */
	/* Overrided because for FER2013 there isn't a temp folder to delete. */
	@Override
	protected void deleteTempDirectoryAndReleaseResources() 
	{
		int state = 0;
		
		// Release memory allocated for CLAHE.
		clahe.collectGarbage();
		
		try 
		{
			// Log update.
			logger.log(Level.INFO, "The classification was successfully completed.\n\n\n\n"); 
			// Flush and close the log file handler.
			fileHandler.flush();
			fileHandler.close();
		}
		catch(SecurityException e)
		{
			state = 1;
			Platform.runLater(() -> controller.showErrorDialog("An error occurred while closing the file handler to the log."));
		}
		
		// Release the buttons with errors or not.
		final int finalstate = state;
		Platform.runLater(() -> controller.startStopClassification(false, finalstate));
	}
}
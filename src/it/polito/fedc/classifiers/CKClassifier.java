package it.polito.fedc.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import javafx.application.Platform;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;

public class CKClassifier extends Classifier implements Runnable 
{
	private String emotionalLabelsFile;
	private boolean error = false, squareImages = false;
	
	public CKClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String emotionalLabelsFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean squareImages, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, true, true, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 200;
		// Emotion file.
		this.emotionalLabelsFile = emotionalLabelsFile;		
		// Make image square.
		this.squareImages = squareImages;
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "Extended Cohn-Kanade Database (CK+)", "\nsquare images: " + squareImages + "\n")) 
		{
			return;
		}
		
		// Extraction of the labels and images of the CK+ database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the emotion labels..."));
			unzipper.unzip(controller, emotionalLabelsFile, tempDirectory);
			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: extraction of the CK+ images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the CK+ images.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			File[] listOfExternalDirectories;
			boolean neutralTaken;

			// Set the total number of photos; this is necessary to update the progress bar.
			numberOfPhotos = 450;
			
			// Create the classification directories for the CK+ database.
			if (!createClassificationDirectories(outputDirectory)) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 3: execution of the operations chosen by the user..."));

			// Read the files in the emotions directory.
			listOfExternalDirectories = new File(tempDirectory, "emotion").listFiles();

			// Cycle performed for each external directory.
			for (int i = 0; (i < listOfExternalDirectories.length) && (!Thread.currentThread().isInterrupted()); i++) 
			{
				// Verify that the directories start with an S followed by a sequence of three numbers.
				if (!listOfExternalDirectories[i].getName().matches("S[0-9]{3}")) 
				{
					exceptionManager("The format of the directories in the input file is not as expected.");
					return;
				}
				
				File[] listOfInternalDirectories = listOfExternalDirectories[i].listFiles();

				neutralTaken = false;
				// Cycle performed for each internal directory.
				for (int j = 0; (j < listOfInternalDirectories.length) && (!Thread.currentThread().isInterrupted()); j++)  
				{
					File internalDirectory = listOfInternalDirectories[j];
					File[] listOfFiles = internalDirectory.listFiles();

					// Verify that the directories are composed of a sequence of three numbers.
					if (!internalDirectory.getName().matches("[0-9]{3}")) 
					{
						exceptionManager("The format of the directories in the input file is not as expected.");
						return;
					}

					// Read the internal file.
					if (listOfFiles.length >= 1) 
					{
						// Verify that the filename has the typical form of the CK+ database files.
						if (!listOfFiles[0].getName().matches("S[0-9]{3}_[0-9]{3}_[0-9]*_[a-z]{7}\\.[a-z]{3}")) 
						{
							exceptionManager("The images format in the input file is not as expected.");
							return;
						}

						FileReader fr = null;
						BufferedReader br = null;
						try 
						{
							fr = new FileReader(listOfFiles[0]);
							br = new BufferedReader(fr);
							// Read the first line of the file.
							String line = br.readLine();

							// Reconstruction of the file name and checking of its existence.
							File file = new File(tempDirectory + "\\cohn-kanade-images\\" + listOfExternalDirectories[i].getName() + "\\" + internalDirectory.getName(),	listOfFiles[0].getName().substring(0, (int) listOfFiles[0].getName().length() - 12) + ".png");
							if (file.exists()) 
							{
								// Edit, classify and save the photo.
								performOperationsOnImage(file, line);
							} 
							else
							{
								exceptionManager("Images are not in the expected position.");
								error = true;
							}
						} 
						catch (IOException | SecurityException e) 
						{
							exceptionManager("There was an error while reading an emotional labels file.");
							error = true;
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
								if (error)
								{
									return;
								}
							} 
							catch (IOException e) 
							{
								exceptionManager("There was an error while closing an emotional labels file.");
								return;
							}
						}
					}

					// A neutral photo is also taken for each subject in the Cohn-Kanade database.
					if (!neutralTaken) 
					{
						String fileName = listOfExternalDirectories[i].getName() + "_" + internalDirectory.getName() + "_00000001.png";
						// Reconstruction of the file name and checking of its existence.
						File neutralFile = new File(tempDirectory + "\\cohn-kanade-images\\" + listOfExternalDirectories[i].getName()	+ "\\" + internalDirectory.getName(), fileName);
						if (neutralFile.exists()) 
						{
							// Edit, classify and save the photo.
							performOperationsOnImage(neutralFile, "0");
							neutralTaken = true;
						} 
						else 
						{
							exceptionManager("Images are not in the expected position.");
							return;
						}
					}
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
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: subdivision between train, validation and test directories..."));
				if (!subdivideImages(outputDirectory, outputDirectory, trainPercentage, validationPercentage, testPercentage)) 
				{
					return;
				}
				Platform.runLater(() -> controller.setPhaseLabel("Phase 5: deleting temporary directories..."));
			} 
			else 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary directories..."));
			}
			deleteTempDirectoryAndReleaseResources();
		}
	}

	/* Method for editing, classifying and save the photo. */
	private void performOperationsOnImage(File file, String emotion) 
	{
		faceFound = true;
		Mat image = Imgcodecs.imread(file.getAbsolutePath());
		
		// Make image square (optional).
		if (squareImages) 
		{
			image = makeImageSquare(image);
		}
		
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
						
			if (emotion.equals("0")) 
			{
				saveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), image);
			}
			if (emotion.contains("1")) 
			{
				saveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), image);
			} 
			else if (emotion.contains("2")) 
			{
				saveImageInTheChosenFormat(contemptDirectory.getAbsolutePath(), file.getName(), image);
			} 
			else if (emotion.contains("3")) 
			{
				saveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), image);
			} 
			else if (emotion.contains("4")) 
			{
				saveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), image);
			} 
			else if (emotion.contains("5")) 
			{
				saveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), image);
			} 
			else if (emotion.contains("6")) 
			{
				saveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), image);
			} 
			else if (emotion.contains("7")) 
			{
				saveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), file.getName(), image);
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
}
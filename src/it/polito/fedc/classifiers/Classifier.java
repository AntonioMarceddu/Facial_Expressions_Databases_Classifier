package it.polito.fedc.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Platform;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.CLAHE;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import it.polito.fedc.Controller;

public class Classifier 
{
	protected Controller controller;
	protected CascadeClassifier frontalFaceCascade;
	protected Size imageSize;
	protected String haarclassifierpath, inputFile, outputDirectory, tempDirectory, logDirectoryName, logFileName;
	protected int numberOfPhotos = 0, format = 0, absoluteFaceSize = 100, classified = 0, histogramEqualizationType = 0;
	protected boolean contemptState = false, surpriseState = false, grayscale = false, histogramEqualization = false, faceDetection = false, faceFound = true, subdivision = false, validation = false;
	protected double percentage = 0, difference = 0, trainPercentage = 0, validationPercentage = 0, testPercentage = 0;
	protected File angerDirectory = null, contemptDirectory = null, disgustDirectory = null, fearDirectory = null, happinessDirectory = null, neutralityDirectory = null, sadnessDirectory = null, surpriseDirectory = null;
	protected CLAHE clahe;
    
	protected FileHandler fileHandler;
	protected static Logger logger;
	
	/* Constructor for all database, except for FER2013. */
	Classifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, boolean contemptState, boolean surpriseState, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		this.controller = controller;
		this.inputFile = inputFile;
		this.outputDirectory = outputDirectory;
		this.contemptState = contemptState;
		this.surpriseState = surpriseState;
		this.format = format;
		this.grayscale = grayscale;
		this.histogramEqualization = histogramEqualization;
		this.histogramEqualizationType = histogramEqualizationType;
		this.faceDetection = faceDetection;
		this.subdivision = subdivision;
		this.validation = validation;
		this.trainPercentage = Math.floor(trainPercentage * 100) / 100.0d;
		this.validationPercentage = Math.floor(validationPercentage * 100) / 100.0d;
		this.testPercentage = Math.floor(testPercentage * 100) / 100.0d;
		this.logDirectoryName = logDirectoryName;
		this.logFileName = logFileName;
		
		tempDirectory = outputDirectory + "\\temp\\";
		
		// Instance the CLAHE functionality.
		clahe = Imgproc.createCLAHE(contrastLimit, new Size(tileSize, tileSize));

		// Instance the variable containing the dimensions of the target image.
		imageSize = new Size(width, height);
		
		// Instance the CascadeClassifier.
		haarclassifierpath = System.getProperty("user.dir") + "\\lib\\";
		frontalFaceCascade = new CascadeClassifier(haarclassifierpath + "haarcascade_frontalface_alt.xml");
	}
	
	/* Method to instantiate the logger. */
	protected boolean instantiateLoggerAndLogStartMessage(String className, String database, String additionalInformation)
	{
		try
		{
			// Create the log directory if do not exist.
			File logDirectory = new File(logDirectoryName);
			if (!logDirectory.exists()) 
			{
				logDirectory.mkdirs();
			}	
			
			// Instance the logger.
		 	logger = Logger.getLogger(className);
		 	logger.setUseParentHandlers(false);
			// Instance and set the filehandler and the formatter.
			fileHandler = new FileHandler(logDirectoryName + logFileName, true);
		  	fileHandler.setFormatter(new SimpleFormatter());
		  	logger.addHandler(fileHandler);
		  	
		  	// Create and log the begin classification message.
		  	String message = database + " Classifier executed with the following parameters:"
				+ "\ninput file: " + inputFile
				+ "\noutput directory: " + outputDirectory
				+ "\nwidth: " + (int) imageSize.width
				+ "\nheight: " + (int) imageSize.height
				+ "\ngrayscale: " + grayscale;
			if((histogramEqualization == true) && (histogramEqualizationType == 1))
			{
				message = message + ("\nCLAHE: true, tile size: " + clahe.getTilesGridSize() + ", contrast limit: " + (int) clahe.getClipLimit());			
			}
			else
			{
				message = message + "\nhistogram equalization: " + histogramEqualization;
			}
			message = message + "\nfaceDetection: " + faceDetection
			+ "\nsubdivision: " + subdivision;
			if (subdivision)
			{
				message = message + (" with the following percentages - train: " + (int) (trainPercentage * 100) + "%, validation: " + (int) (validationPercentage * 100) + "%, test: " + (int) (testPercentage * 100) + "%");
			}
			message = message + additionalInformation;
			logger.log(Level.INFO, message); 
		}
		catch (IOException | SecurityException e)
		{
			exceptionManager("There was an error during the instantiation of the logger.\n");
			return false;
		}
		return true;
	}

	/* Method to update the progress bar. */
	protected void updateProgressBar() 
	{
		// Increase the count of the number of classified photos (or, if not classified, of the analyzed photos).
		classified++;
		// Calculate the percentage of completion of the current operation and update of the classification progress bar.
		percentage = (double) classified / (double) numberOfPhotos;
		if (percentage - difference >= 0.01) 
		{
			difference = difference + (double) 0.01;
			difference = Math.round(difference * 100);
			difference = difference / 100;

			final double value = percentage;
			Platform.runLater(() -> controller.updateProgressBar(value));
		}
	}

	/* Method to update the progress bar by setting the value directly. */
	protected void updateProgressBar(double value) 
	{
		final double val = value;
		Platform.runLater(() -> controller.updateProgressBar(val));
	}

	/* Method to retrieve the file name excluding the extension. */
	protected String removeFileExtension(String fileName) 
	{
		if (fileName.contains(".")) 
		{
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}
		return fileName;
	}
	
	/* Method to retrieve the file name excluding the extension. */
	protected String getFileExtension(String fileName) 
	{
		if (fileName.contains(".")) 
		{
			return fileName.substring(fileName.lastIndexOf('.'));
		}
		return fileName;
	}

	/* Method to save images in the chosen format. */
	protected void saveImageInTheChosenFormat(String path, String fileName, Mat image) 
	{	
		String fullPath = path + "\\" + fileName;
		String extension = ".jpg";
		boolean removed = false;
		
		// Try to remove the file extension. If not present and format is set to 0, the image will be saved in .jpg.
		if(!fileName.equals(removeFileExtension(fileName)))
		{
			removed = true;
			fullPath = path + "\\" + removeFileExtension(fileName);	
			
			// Original format.
			if (format == 0) 
			{
				if(getFileExtension(fileName).startsWith("."))
				{
					extension = getFileExtension(fileName);
				}				
			}
		}		
		
		// BMP.
		if (format == 1) 
		{
			extension = ".bmp";
		}
		// JPEG 2000.
		else if (format == 3) 
		{
			extension = ".jp2";
		}
		// PIF.
		else if (format == 4) 
		{
			if (grayscale)
			{
				extension = ".pgm";
			}
			else
			{
				extension = ".ppm";
			}
		}
		// PNG.
		else if (format == 5) 
		{
			extension = ".png";
		}
		// TIFF.
		else if (format == 6) 
		{
			extension = ".tiff";
		}
		
		//Verify if image exists: if true, search for an alternative name to use.
		int i = 0;
		while (true)
		{
			if (i > 0)
			{
				if (removed)
				{
					fullPath = path + "\\" + removeFileExtension(fileName) + "_" + i;
				}
				else
				{
					fullPath = path + "\\" + fileName + "_" + i;
				}				
			}
			
			File file = new File(fullPath + extension);

			if (!file.exists())
			{
				break;
			}
			
			i++;
		}		
		
		// Write the image.
		Imgcodecs.imwrite(fullPath + extension, image);	
	}

	/* Method for creating classification directories in the specified directory. */
	protected boolean createClassificationDirectories(String baseDirectory) 
	{
		try 
		{
			angerDirectory = new File(baseDirectory, "Anger");
			angerDirectory.mkdirs();
			if (contemptState) 
			{
				contemptDirectory = new File(baseDirectory, "Contempt");
				contemptDirectory.mkdirs();
			}
			disgustDirectory = new File(baseDirectory, "Disgust");
			disgustDirectory.mkdirs();
			fearDirectory = new File(baseDirectory, "Fear");
			fearDirectory.mkdirs();
			happinessDirectory = new File(baseDirectory, "Happiness");
			happinessDirectory.mkdirs();
			neutralityDirectory = new File(baseDirectory, "Neutrality");
			neutralityDirectory.mkdirs();
			sadnessDirectory = new File(baseDirectory, "Sadness");
			sadnessDirectory.mkdirs();
			if (surpriseState) 
			{
				surpriseDirectory = new File(baseDirectory, "Surprise");
				surpriseDirectory.mkdirs();
			}
			
			// Log update.
			logger.log(Level.INFO, "The classification directories inside the \"" + baseDirectory + "\" directory have been successfully created.\n");
			
			return true;
		} 
		catch (SecurityException e) 
		{
			exceptionManager("There was an error while creating classification directories.");
			return false;
		}
	}

	/* Method for creating classification directories in the specified directory and without assigning a reference to any variable. */
	protected boolean createClassificationDirectoriesWithoutInitialization(String baseDirectory) 
	{
		try 
		{
			new File(baseDirectory, "Anger").mkdirs();
			if (contemptState) 
			{
				new File(baseDirectory, "Contempt").mkdirs();
			}
			new File(baseDirectory, "Disgust").mkdirs();
			new File(baseDirectory, "Fear").mkdirs();
			new File(baseDirectory, "Happiness").mkdirs();
			new File(baseDirectory, "Neutrality").mkdirs();
			new File(baseDirectory, "Sadness").mkdirs();
			if (surpriseState) 
			{
				new File(baseDirectory, "Surprise").mkdirs();
			}
			
			// Log update.
			logger.log(Level.INFO, "The classification directories inside the \"" + baseDirectory + "\" directory have been successfully created.\n"); 
			
			return true;
		} 
		catch (SecurityException e) 
		{
			exceptionManager("There was an error while creating classification directories.");
			return false;
		}
	}

	/* Method to delete the temporary directory and to release the previously taken resources. */
	protected void deleteTempDirectoryAndReleaseResources() 
	{
		int state = 0;
		
		// Release memory allocated for CLAHE.
		clahe.collectGarbage();
		
		try 
		{
			// Delete all files in the temporary directory.
			FileUtils.deleteDirectory(new File(tempDirectory));
		}
		catch (IOException e) 
		{
			state = 1;
			Platform.runLater(() -> controller.showErrorDialog("An error occurred while deleting temporary directories."));
		}
		finally
		{
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
		}
		
		// Release the buttons with errors or not.
		final int finalstate = state;
		Platform.runLater(() -> controller.startStopClassification(false, finalstate));
	}

	/* Method to delete all directories and to release the previously taken resources. */
	protected void deleteAllDirectoriesAndReleaseResources(String message) 
	{
		// Release memory allocated for CLAHE.
		clahe.collectGarbage();
		
		try 
		{
			// Delete all files in the output directory.
			FileUtils.cleanDirectory(new File(outputDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> controller.showErrorDialog("An error occurred while deleting temporary directories."));
		}
		finally
		{
			try 
			{
				// Log the message and close the log file handler.
				logger.log(Level.WARNING, message); 
				Platform.runLater(() -> controller.showAttentionDialog(message));
				fileHandler.flush();
				fileHandler.close();
			}
			catch (SecurityException e)
			{
				Platform.runLater(() -> controller.showErrorDialog("An error occurred while closing the file handler to the log."));
			}
		}

		Platform.runLater(() -> controller.startStopClassification(false, 2));
	}

	/* Exception handling method. */
	protected void exceptionManager(String message) 
	{
		// Release memory allocated for CLAHE.
		clahe.collectGarbage();
		
		try 
		{
			// Delete all files in the output directory.
			FileUtils.cleanDirectory(new File(outputDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> controller.showErrorDialog("An error occurred while deleting temporary directories."));
		}
		finally
		{
			try 
			{
				// Log the message and close the log file handler.
				logger.log(Level.SEVERE, message); 
				Platform.runLater(() -> controller.showErrorDialog(message));
				fileHandler.flush();
				fileHandler.close();
			}
			catch (SecurityException e)
			{
				Platform.runLater(() -> controller.showErrorDialog("An error occurred while closing the file handler to the log."));
			}
		}

		Platform.runLater(() -> controller.startStopClassification(false, 3));
	}
	
	/* Method to perform the normal or the CLA Histogram Equalization.*/
	protected Mat histogramEqualization(Mat image) 
	{
		if(histogramEqualizationType == 0)
		{
			return normalHE(image);
		}
		else
		{
			return CLAHE(image);
		}
	}

	/* Method to perform the normal histogram equalization on the image. */
	protected Mat normalHE(Mat image) 
	{
		if (grayscale) 
		{
			Imgproc.equalizeHist(image, image);
		} 
		else 
		{
			// Subdivision of the image in the three channels b, g and r.
			ArrayList<Mat> channels = new ArrayList<Mat>();
			Core.split(image, channels);

			Mat b = new Mat();
			Mat g = new Mat();
			Mat r = new Mat();

			// Histogram equalization for each individual channel.
			Imgproc.equalizeHist(channels.get(0), b);
			Imgproc.equalizeHist(channels.get(1), g);
			Imgproc.equalizeHist(channels.get(2), r);

			// Image reconstruction.
			ArrayList<Mat> normalizedImages = new ArrayList<Mat>();
			normalizedImages.add(b);
			normalizedImages.add(g);
			normalizedImages.add(r);
			Core.merge(normalizedImages, image);
		}
		return image;
	}
	
	/* Method to perform the Contrast Limited Adaptive Histogram Equalization on the image. */
	protected Mat CLAHE(Mat image) 
	{
		if (image.channels() >= 3) 
		{
	        // Read the RGB color image and converts it to Lab.
	        Mat channel = new Mat();
	        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2Lab);

	        // Extract the L channel.
	        Core.extractChannel(image, channel, 0);

	        // Apply the CLAHE algorithm to the L channel.
	        clahe.apply(channel, channel);

	        // Merge the color planes back into a Lab image.
	        Core.insertChannel(channel, image, 0);

	        // Convert back to RGB.
	        Imgproc.cvtColor(image, image, Imgproc.COLOR_Lab2BGR);

	        // Release the temporary mat.
	        channel.release();
	    }
	    else
	    {
	        // Apply the CLAHE algorithm to the L channel.
	        clahe.apply(image, image);
	    }		
		return image;
	}

	/* Method to perform face detection on frontal face images. */
	protected Mat frontalFaceDetection(Mat image) 
	{
		Mat face = null, tempImage = new Mat();
		
		// Do the operations with the grayscale version of the image in order to improve recognition performance.
		Imgproc.cvtColor(image, tempImage, Imgproc.COLOR_BGR2GRAY);
		
		MatOfRect faces = new MatOfRect();
		// Verify the presence of frontal faces through the haar front face classifier.
		frontalFaceCascade.detectMultiScale(tempImage, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
		Rect[] facesArray = faces.toArray();
		
		// If at least one face is detected, the photo will be cropped to this one: the others will be lost.
		if (facesArray.length > 0) 
		{
			face = new Mat(image, new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].width, facesArray[0].width));
		} 
		else 
		{
			faceFound = false;
		}
		// Release the acquired resources.
		image.release();
		tempImage.release();
		faces.release();
		
		return face;
	}
	
	/* Method to make an image square.*/
	protected Mat makeImageSquare(Mat image)
	{
		int diff = 0, div = 0, rst = 0;
		// Get the difference.
		if (image.width() >= image.height())
		{
			diff = image.width() - image.height();
		}
		else
		{
			diff = image.height() - image.width();
		}
		
		// Calculate division and rest.
		div = diff / 2;
		rst = diff % 2;

		// Add padding to image.
		if(image.width() >= image.height())
		{
			Core.copyMakeBorder(image, image, div + rst, div, 0, 0, Core.BORDER_CONSTANT);
		}
		else
		{
			Core.copyMakeBorder(image, image, 0, 0, div + rst, div, Core.BORDER_CONSTANT);
		}			
		return image;
	}

	/* Method to create the train, validation and test directory and to divide the images between them. */
	protected boolean subdivideImages(String trainValTestDirectory, String classificationDirectory, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		try 
		{
			// Create the train directory.
			File trainDirectory, validationDirectory = null, testDirectory;
			trainDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Train", 0.1);
			
			// Create the test directory and the inner directories.
			testDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Test", 0.2);
			if (!createClassificationDirectoriesWithoutInitialization(testDirectory.getAbsolutePath())) 
			{
				return false;
			}
			
			if (validation)
			{
				// Create the validation directory and the inner directories.
				validationDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Validation", 0.3);
				if (!createClassificationDirectoriesWithoutInitialization(validationDirectory.getAbsolutePath())) 
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
			exceptionManager("There was an error while performing the subdivision.");
			return false;
		}
	}

	/* Method for randomly selecting a certain percentage of images for validation or testing. */
	protected void getNRandomImages(String source, String testDirectory, double testPercentage, String validationDirectory, double validationPercentage) throws SecurityException, IOException 
	{
		File[] outputDirectories = new File(source).listFiles();
		int numberOfDirectories = outputDirectories.length, numberOfImages = 0;

		// Cycle through all the folders.
		for (int i = 0; (i < numberOfDirectories) && (!Thread.currentThread().isInterrupted()); i++)
		{
			// Exclude some non-classification directories.
			if ((!outputDirectories[i].getName().equals("Train")) && (!outputDirectories[i].getName().equals("Validation")) && (!outputDirectories[i].getName().equals("Test")) && (!outputDirectories[i].getName().equals("temp")))
			{	
				numberOfImages = outputDirectories[i].list().length;
				// Take the test images.
				takeNRandomImages(outputDirectories[i], testDirectory, testPercentage, (int) Math.round(numberOfImages * testPercentage));
				// Update bar.
				updateProgressBar(0.55);
				// Take the validation images.
				if (validation)
				{
					takeNRandomImages(outputDirectories[i], validationDirectory, validationPercentage, (int) Math.round(numberOfImages * validationPercentage));
				}			
			}
		}		
		// Update bar.
		updateProgressBar(0.9);
	}
	
	/* Method to randomly choose a percentage of images for test or validation. */
	private void takeNRandomImages(File source, String destination, double choicePercentage, int numberOfImages) throws IOException
	{
		int count = 0;
		boolean flag = false;
		Random random = new Random();
		// Randomly choose a percentage of images for a subdirectory.
		while ((flag == false) && (!Thread.currentThread().isInterrupted())) 
		{
			for (File file : source.listFiles()) 
			{
				if (file.isFile()) 
				{
					// Choose or not this image?
					if (random.nextDouble() < choicePercentage) 
					{
						count++;
						FileUtils.moveFileToDirectory(file, new File(destination, source.getName()), true);
						if (count >= numberOfImages) 
						{
							flag = true;
							break;
						}
					}
				}
			}
		}
	}
	
	/* Method to move the classification folders to a destination, that usually is the train directory. */
	protected void moveDirectoriesToDestination(File source, File destination) throws IOException
	{
		File[] outputDirectories = source.listFiles();
		int numberOfDirectories = outputDirectories.length;
		String folderName;
		
		// Cycle through all the temp folders and move the classification ones.
		for (int i = 0; (i < numberOfDirectories) && (!Thread.currentThread().isInterrupted()); i++)
		{
			folderName = outputDirectories[i].getName();
			if ((!folderName.equals("Train")) && (!folderName.equals("Validation")) && (!folderName.equals("Test")) && (!folderName.equals("temp")))
			{
				FileUtils.moveDirectoryToDirectory(outputDirectories[i], destination, true);
			}
		}
		
		updateProgressBar(1);
	}
	
	/* Method to create a folder and update the bar. */
	protected File createFolderAndUpdateProgressBar(String baseDirectory, String folder, double perc)
	{
		File createdDirectory = new File(baseDirectory, folder);
		createdDirectory.mkdirs();
		updateProgressBar(perc);
		return createdDirectory;
	}
}

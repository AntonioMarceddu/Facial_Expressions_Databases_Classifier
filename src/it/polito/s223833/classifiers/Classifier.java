package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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

import it.polito.s223833.Controller;

public class Classifier 
{
	protected Controller controller;
	protected CascadeClassifier frontalFaceCascade;
	protected Size imageSize;
	protected String haarclassifierpath, inputFile, outputDirectory, tempDirectory;
	protected int format = 0, absoluteFaceSize = 100, classified = 0, histogramEqualizationType=0;
	protected boolean contemptState = false, surpriseState = false, grayscale = false, histogramEqualization = false, faceDetection = false, faceFound = true, subdivision = false, validation=false;
	protected double percentage = 0, difference = 0, trainPercentage = 0, validationPercentage = 0, testPercentage = 0;
	protected File angerDirectory = null, contemptDirectory = null, disgustDirectory = null, fearDirectory = null, happinessDirectory = null, neutralityDirectory = null, sadnessDirectory = null, surpriseDirectory = null;
    CLAHE clahe;
	/* Constructor for all database, except for Fer2013. */
	Classifier(Controller controller, String inputFile, String outputDirectory, boolean contemptState, boolean surpriseState, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
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
		this.trainPercentage = trainPercentage;
		this.validationPercentage = validationPercentage;
		this.testPercentage = testPercentage;
		
		tempDirectory = outputDirectory + "\\temp\\";
		
		// Instancing of the CLAHE functionality.
		clahe = Imgproc.createCLAHE(contrastLimit, new Size(tileSize, tileSize));

		// Instancing of the variable containing the dimensions of the target image.
		imageSize = new Size(width, height);
		
		// Instancing of the CascadeClassifier.
		haarclassifierpath = System.getProperty("user.dir") + "\\lib\\";
		frontalFaceCascade = new CascadeClassifier(haarclassifierpath + "haarcascade_frontalface_alt.xml");
	}

	/* Method for updating the progress bar. */
	protected void UpdateBar() 
	{
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
	protected void UpdateBar(double value) 
	{
		final double val = value;
		Platform.runLater(() -> controller.updateProgressBar(val));
	}

	/* Method to retrieve the file name excluding the format. */
	private String GetFileName(String fileName) 
	{
		if (fileName.contains(".")) 
		{
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}
		return fileName;
	}

	/* Method to save images in the chosen format. */
	protected void SaveImageInTheChosenFormat(String path, String fileName, Mat image) 
	{
		// Original format.
		if (format == 0) 
		{
			Imgcodecs.imwrite(path + "\\" + fileName, image);
		}
		// BMP.
		else if (format == 1) 
		{
			Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".bmp", image);
		}
		// JPEG.
		else if (format == 2) 
		{
			Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".jpg", image);
		}
		// JPEG 2000.
		else if (format == 3) 
		{
			Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".jp2", image);
		}
		// PIF.
		else if (format == 4) 
		{
			if(grayscale)
			{
				Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".pgm", image);
			}
			else
			{
				Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".ppm", image);
			}
		}
		// PNG.
		else if (format == 5) 
		{
			Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".png", image);
		}
		// TIFF.
		else if (format == 6) 
		{
			Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".tiff", image);
		}
	}

	/* Method for creating classification directories in the output directory. */
	protected boolean CreateDirectories() 
	{
		try 
		{
			angerDirectory = new File(outputDirectory, "Anger");
			angerDirectory.mkdirs();
			if (contemptState) 
			{
				contemptDirectory = new File(outputDirectory, "Contempt");
				contemptDirectory.mkdirs();
			}
			disgustDirectory = new File(outputDirectory, "Disgust");
			disgustDirectory.mkdirs();
			fearDirectory = new File(outputDirectory, "Fear");
			fearDirectory.mkdirs();
			happinessDirectory = new File(outputDirectory, "Happiness");
			happinessDirectory.mkdirs();
			neutralityDirectory = new File(outputDirectory, "Neutrality");
			neutralityDirectory.mkdirs();
			sadnessDirectory = new File(outputDirectory, "Sadness");
			sadnessDirectory.mkdirs();
			if (surpriseState) 
			{
				surpriseDirectory = new File(outputDirectory, "Surprise");
				surpriseDirectory.mkdirs();
			}

			return true;
		} 
		catch (SecurityException e) 
		{
			ExceptionManager("There was an error while creating classification folders.");
			return false;
		}
	}

	/* Method for creating classification directories in the specified directory. */
	protected boolean CreateDirectoriesWithParameter(File directory) 
	{
		try 
		{
			angerDirectory = new File(directory, "Anger");
			angerDirectory.mkdirs();
			if (contemptState) 
			{
				contemptDirectory = new File(directory, "Contempt");
				contemptDirectory.mkdirs();
			}
			disgustDirectory = new File(directory, "Disgust");
			disgustDirectory.mkdirs();
			fearDirectory = new File(directory, "Fear");
			fearDirectory.mkdirs();
			happinessDirectory = new File(directory, "Happiness");
			happinessDirectory.mkdirs();
			neutralityDirectory = new File(directory, "Neutrality");
			neutralityDirectory.mkdirs();
			sadnessDirectory = new File(directory, "Sadness");
			sadnessDirectory.mkdirs();
			if (surpriseState) 
			{
				surpriseDirectory = new File(directory, "Surprise");
				surpriseDirectory.mkdirs();
			}
			return true;
		} 
		catch (SecurityException e) 
		{
			ExceptionManager("There was an error while creating classification folders.");
			return false;
		}
	}

	/* Method for creating classification directories in the specified directory and without assigning a reference to any variable. */
	protected boolean CreateDirectoriesWithParameterAndWithoutInitialization(File directory) 
	{
		try 
		{
			new File(directory, "Anger").mkdirs();
			if (contemptState) 
			{
				new File(directory, "Contempt").mkdirs();
			}
			new File(directory, "Disgust").mkdirs();
			new File(directory, "Fear").mkdirs();
			new File(directory, "Happiness").mkdirs();
			new File(directory, "Neutrality").mkdirs();
			new File(directory, "Sadness").mkdirs();
			if (surpriseState) 
			{
				new File(directory, "Surprise").mkdirs();
			}
			return true;
		} 
		catch (SecurityException e) 
		{
			ExceptionManager("There was an error while creating classification directories.");
			return false;
		}
	}

	/* Method to delete the temporary directory. */
	protected boolean DeleteTempDirectory() 
	{
		// Release memory allocated for CLAHE.
		clahe.collectGarbage();
		
		try 
		{
			FileUtils.deleteDirectory(new File(tempDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog("An error occurred while deleting temporary directories.");
				controller.StartStopClassification(false, true);});
			return true;
		}
		return false;
	}

	/* Method to delete all directories. */
	protected boolean DeleteAllDirectories() 
	{
		// Release memory allocated for CLAHE.
		clahe.collectGarbage();
		
		try 
		{
			FileUtils.cleanDirectory(new File(outputDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog("An error occurred while deleting temporary directories.");
				controller.StartStopClassification(false, true);});
			return true;
		}
		return false;
	}

	/* Exception handling method. */
	protected void ExceptionManager(String message) 
	{
		DeleteAllDirectories();
		Platform.runLater(() -> {controller.ShowErrorDialog(message);
			controller.StartStopClassification(false, true);});
	}
	
	/* Method to perform the normal or the CLA Histogram Equalization.*/
	protected Mat HistogramEqualization(Mat image) 
	{
		if(histogramEqualizationType==0)
		{
			return NormalHistogramEqualization(image);
		}
		else
		{
			return CLAHE(image);
		}
	}

	/* Method to perform the normal histogram equalization on the image. */
	protected Mat NormalHistogramEqualization(Mat image) 
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

	        // Extracts the L channel.
	        Core.extractChannel(image, channel, 0);

	        // Applies the CLAHE algorithm to the L channel.
	        clahe.apply(channel, channel);

	        // Merges the color planes back into a Lab image.
	        Core.insertChannel(channel, image, 0);

	        // Converts back to RGB.
	        Imgproc.cvtColor(image, image, Imgproc.COLOR_Lab2BGR);

	        // Releases the temporary mat.
	        channel.release();
	    }
	    else
	    {
	        // Applies the CLAHE algorithm to the L channel.
	        clahe.apply(image, image);
	    }		
		return image;
	}

	/* Method to perform face detection on frontal face images. */
	protected Mat FrontalFaceDetection(Mat image) 
	{
		Mat face = null, tempImage = new Mat();
		
		// Do the operations with the grayscale version of the image in order to improve performance.
		Imgproc.cvtColor(image, tempImage, Imgproc.COLOR_BGR2GRAY);
		
		MatOfRect faces = new MatOfRect();
		// Verifies the presence of frontal faces through the haar front face classifier.
		frontalFaceCascade.detectMultiScale(tempImage, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

		Rect[] facesArray = faces.toArray();
		// If at least one face is detected, the photo will be cropped to the face itself.
		if (facesArray.length > 0) 
		{
			Rect rectCrop = null;
			// Only the first face will be saved: if an image has more faces, the others are lost.
			if (facesArray[0].width > facesArray[0].height) 
			{
				rectCrop = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].width, facesArray[0].width);
			} 
			else 
			{
				rectCrop = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].height, facesArray[0].height);
			}	
			face = new Mat(image, rectCrop);			
			image.release();
		} 
		else 
		{
			faceFound = false;
		}
		tempImage.release();
		faces.release();
		
		return face;
	}
	
	/* Method to make an image square.*/
	protected Mat MakeImageSquare(Mat image)
	{
		int diff = 0, div = 0, rst = 0;
		// Get the difference.
		if(image.width() >= image.height())
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
			Core.copyMakeBorder(image, image, div+rst, div, 0, 0, Core.BORDER_CONSTANT);
		}
		else
		{
			Core.copyMakeBorder(image, image, 0, 0, div+rst, div, Core.BORDER_CONSTANT);
		}			
		return image;
	}

	/* Method to create the training, validation and test folder and to divide the images between them. */
	protected boolean SubdivideImages(double trainPerc, double validPerc, double testPerc) 
	{
		try 
		{
			int angerTotalNumber = 0, contemptTotalNumber = 0, disgustTotalNumber = 0, fearTotalNumber = 0, happinessTotalNumber = 0, neutralityTotalNumber = 0, sadnessTotalNumber = 0, surpriseTotalNumber = 0;

			// Count of the number of files per-directory.
			angerTotalNumber = countNumberOfFiles(angerDirectory);
			UpdateBar(0.02);
			if (contemptState) 
			{
				contemptTotalNumber = countNumberOfFiles(contemptDirectory);
			}
			disgustTotalNumber = countNumberOfFiles(disgustDirectory);
			UpdateBar(0.04);
			fearTotalNumber = countNumberOfFiles(fearDirectory);
			UpdateBar(0.06);
			happinessTotalNumber = countNumberOfFiles(happinessDirectory);
			UpdateBar(0.08);
			neutralityTotalNumber = countNumberOfFiles(neutralityDirectory);
			UpdateBar(0.10);
			sadnessTotalNumber = countNumberOfFiles(sadnessDirectory);
			UpdateBar(0.12);
			if (surpriseState) 
			{
				surpriseTotalNumber = countNumberOfFiles(surpriseDirectory);
				UpdateBar(0.14);
			}

			// Creates the train directory.
			File trainDirectory, validationDirectory, testDirectory;
			trainDirectory = new File(outputDirectory, "Train");
			trainDirectory.mkdirs();
			UpdateBar(0.16);
			
			// Creates the test directory and the inner directories.
			testDirectory = new File(outputDirectory, "Test");
			testDirectory.mkdirs();
			UpdateBar(0.18);
			if (!CreateDirectoriesWithParameterAndWithoutInitialization(testDirectory)) 
			{
				return false;
			}
			UpdateBar(0.20);
			
			if(validation)
			{
				// Creates the validation directory and the inner directories.
				validationDirectory = new File(outputDirectory, "Validation");
				validationDirectory.mkdirs();
				UpdateBar(0.22);
				if (!CreateDirectoriesWithParameterAndWithoutInitialization(validationDirectory)) 
				{
					return false;
				}
				UpdateBar(0.24);
				
				//Put a percentage of the image of the dataset in the validation directory.
				subdivideImages(validationDirectory.getAbsolutePath(), validPerc, angerTotalNumber, contemptTotalNumber, disgustTotalNumber, fearTotalNumber, happinessTotalNumber, neutralityTotalNumber, sadnessTotalNumber, surpriseTotalNumber);
				UpdateBar(0.57);
			}

			//Put a percentage of the image of the dataset in the test directory.
			subdivideImages(testDirectory.getAbsolutePath(), testPerc, angerTotalNumber, contemptTotalNumber, disgustTotalNumber, fearTotalNumber, happinessTotalNumber, neutralityTotalNumber, sadnessTotalNumber, surpriseTotalNumber);
			UpdateBar(0.9);

			// Move the remaining folders and images in the train directory.
			if (!Thread.currentThread().isInterrupted()) 
			{
				FileUtils.moveDirectoryToDirectory(angerDirectory, trainDirectory, true);
				if (contemptState) 
				{
					FileUtils.moveDirectoryToDirectory(contemptDirectory, trainDirectory, true);
				}
				FileUtils.moveDirectoryToDirectory(disgustDirectory, trainDirectory, true);
				FileUtils.moveDirectoryToDirectory(fearDirectory, trainDirectory, true);
				FileUtils.moveDirectoryToDirectory(happinessDirectory, trainDirectory, true);
				FileUtils.moveDirectoryToDirectory(neutralityDirectory, trainDirectory, true);
				FileUtils.moveDirectoryToDirectory(sadnessDirectory, trainDirectory, true);
				if (surpriseState) 
				{
					FileUtils.moveDirectoryToDirectory(surpriseDirectory, trainDirectory, true);
				}
				UpdateBar(1);
			}
			return true;
		} 
		catch (SecurityException | IOException | NullPointerException e) 
		{
			ExceptionManager("An error occurred during the subdivision.");
			return false;
		}
	}

	/* Method for counting the number of files in a directory. */
	private int countNumberOfFiles(File directory) throws SecurityException 
	{
		int count = 0;
		if (!Thread.currentThread().isInterrupted()) 
		{
			for (File file : directory.listFiles()) 
			{
				if (file.isFile()) 
				{
					count++;
				}
			}
		}
		return count;
	}

	/* Method for subdividing a percentage of images in the classification directory, for validation or testing. */
	private void subdivideImages(String destination, double percentage, int angerNumber, int contemptNumber, int disgustNumber, int fearNumber, int happinessNumber, int neutralityNumber, int sadnessNumber, int surpriseNumber) throws SecurityException, IOException 
	{
		takeNImagesRandom(angerDirectory, destination + "\\Anger\\", angerNumber, percentage);
		if (contemptState) 
		{
			takeNImagesRandom(contemptDirectory, destination + "\\Contempt\\", contemptNumber, percentage);
		}
		takeNImagesRandom(disgustDirectory, destination + "\\Disgust\\", disgustNumber, percentage);
		takeNImagesRandom(fearDirectory, destination + "\\Fear\\", fearNumber, percentage);
		takeNImagesRandom(happinessDirectory, destination + "\\Happiness\\", happinessNumber, percentage);
		takeNImagesRandom(neutralityDirectory, destination + "\\Neutrality\\", neutralityNumber, percentage);
		takeNImagesRandom(sadnessDirectory, destination + "\\Sadness\\", sadnessNumber, percentage);
		if (surpriseState) 
		{
			takeNImagesRandom(surpriseDirectory, destination + "\\Surprise\\", surpriseNumber, percentage);
		}
	}

	/* Method to randomly choose a percentage of images for a subdirectory. */
	private void takeNImagesRandom(File source, String destination, int totalNumber, double percentage) throws SecurityException, IOException 
	{
		if (!Thread.currentThread().isInterrupted()) 
		{
			Random random = new Random();
			int number = (int) (totalNumber * percentage), count = 0;
			boolean flag = false;
			while (flag == false) 
			{
				for (File file : source.listFiles()) 
				{
					if (file.isFile()) 
					{
						if (random.nextFloat() < percentage) 
						{
							count++;
							FileUtils.moveFileToDirectory(FileUtils.getFile(file.getAbsolutePath()), FileUtils.getFile(destination), true);
							if (count >= number) 
							{
								flag = true;
								break;
							}
						}
					}
				}
			}
		}

	}
}

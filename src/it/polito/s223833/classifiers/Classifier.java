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
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import it.polito.s223833.MainController;

public class Classifier 
{
	protected MainController controller;
	protected CascadeClassifier frontalFaceCascade;
	protected Size imageSize;
	protected String haarclassifierpath, inputFile, outputDirectory, tempDirectory;
	protected int format = 0, absoluteFaceSize = 100, classified = 0;
	protected boolean contemptState = false, surpriseState = false, grayscale = false, histogramEqualization = false, faceDetection = false, faceFound = true, subdivision = false, validation=false;
	protected double percentage = 0, difference = 0, trainPercentage = 0, validationPercentage = 0, testPercentage = 0;
	protected File angerDirectory = null, contemptDirectory = null, disgustDirectory = null, fearDirectory = null, happinessDirectory = null, neutralityDirectory = null, sadnessDirectory = null, surpriseDirectory = null;

	/* Constructor for all database, except for Fer2013. */
	Classifier(MainController controller, String inputFile, String outputDirectory, boolean contemptState, boolean surpriseState, int width, int height, int format, boolean grayscale, boolean histogramEqualization, boolean faceDetection, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		this.controller = controller;
		this.inputFile = inputFile;
		this.outputDirectory = outputDirectory;
		this.contemptState = contemptState;
		this.surpriseState = surpriseState;
		this.format = format;
		this.grayscale = grayscale;
		this.histogramEqualization = histogramEqualization;
		this.faceDetection = faceDetection;
		this.subdivision = subdivision;
		this.validation=validation;
		this.trainPercentage = trainPercentage;
		this.validationPercentage = validationPercentage;
		this.testPercentage = testPercentage;

		tempDirectory = outputDirectory + "\\temp\\";

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
	private void UpdateBar(double value) 
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
		// PNG.
		else if (format == 4) 
		{
			Imgcodecs.imwrite(path + "\\" + GetFileName(fileName) + ".png", image);
		}
		// TIFF.
		else if (format == 5) 
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
			ExceptionManager("There was a problem while creating classification folders.");
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
			ExceptionManager("There was a problem while creating classification folders.");
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
			ExceptionManager("There was a problem while creating classification directories.");
			return false;
		}
	}

	/* Method to delete the temporary directory. */
	protected void DeleteTempDirectory() 
	{
		try 
		{
			FileUtils.deleteDirectory(new File(tempDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog("An error occurred while deleting temporary directories.");
				controller.StartStopClassification(false, true);});
		}
	}

	/* Method to delete the classification directories. */
	protected void DeleteClassificationDirectories() 
	{
		try 
		{
			if (angerDirectory != null) 
			{
				FileUtils.deleteDirectory(angerDirectory);
			}
			if (contemptState == true && contemptDirectory != null) 
			{
				FileUtils.deleteDirectory(contemptDirectory);
			}
			if (disgustDirectory != null) 
			{
				FileUtils.deleteDirectory(disgustDirectory);
			}
			if (fearDirectory != null) 
			{
				FileUtils.deleteDirectory(fearDirectory);
			}
			if (happinessDirectory != null) 
			{
				FileUtils.deleteDirectory(happinessDirectory);
			}
			if (neutralityDirectory != null) 
			{
				FileUtils.deleteDirectory(neutralityDirectory);
			}
			if (sadnessDirectory != null) 
			{
				FileUtils.deleteDirectory(sadnessDirectory);
			}
			if (surpriseState == true && surpriseDirectory != null) 
			{
				FileUtils.deleteDirectory(surpriseDirectory);
			}
		} 
		catch (IOException e) 
		{
			ExceptionManager("An error occurred while deleting the classification directories.");
		}
	}

	/* Method to delete all directories. */
	protected void DeleteAllDirectories() 
	{
		try 
		{
			FileUtils.cleanDirectory(new File(outputDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog("An error occurred while deleting temporary directories.");
				controller.StartStopClassification(false, true);});
		}
	}

	/* Exception handling method. */
	protected void ExceptionManager(String message) 
	{
		DeleteAllDirectories();
		Platform.runLater(() -> {controller.ShowErrorDialog(message);
			controller.StartStopClassification(false, true);});
	}

	/* Method to perform the histogram equalization on the image. */
	protected Mat HistogramEqualization(Mat image) 
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

	/* Method to perform face detection on frontal face images. */
	protected Mat FrontalFaceDetection(Mat image, Mat resizedFace) 
	{
		MatOfRect faces = new MatOfRect();
		// Verifies the presence of frontal faces through the haar front face classifier.
		frontalFaceCascade.detectMultiScale(image, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

		Rect[] facesArray = faces.toArray();
		// If at least one face is detected, the photo will be cropped to the face itself.
		if (facesArray.length > 0) 
		{
			Rect rectCrop = null;
			// Only the first face will be saved: if an image has more faces, the others are
			// lost.
			if (facesArray[0].width > facesArray[0].height) 
			{
				rectCrop = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].width, facesArray[0].width);
			} 
			else 
			{
				rectCrop = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].height, facesArray[0].height);
			}
			// The face-only photo will be saved in a new matrix.
			Mat face = new Mat(image, rectCrop);
			// Scaling the photo to the desired size.
			Imgproc.resize(face, resizedFace, imageSize);
			// Release of the initialized variables.
			face.release();
		} 
		else 
		{
			faceFound = false;
		}
		// Release of the initialized variables.
		faces.release();
		return resizedFace;
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

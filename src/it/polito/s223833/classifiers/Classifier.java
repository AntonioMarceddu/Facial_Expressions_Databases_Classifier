package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javafx.application.Platform;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import it.polito.s223833.MainController;

public class Classifier 
{
	protected MainController controller;
	protected CascadeClassifier frontalFaceCascade;
	protected Size imageSize;
	protected String haarclassifierpath, inputFile, outputDirectory, tempDirectory; 
	protected int absoluteFaceSize = 100, classified = 0;
	protected boolean contemptState, grayscale, histogramEqualization, faceDetection;	
	protected double percentage = 0, difference = 0;
	protected File angerDirectory = null, contemptDirectory = null, disgustDirectory = null, fearDirectory = null, happinessDirectory = null, neutralityDirectory = null, sadnessDirectory = null, surpriseDirectory = null;

	Classifier(MainController controller, String inputFile, String outputDirectory, boolean contemptState, boolean histogramEqualization)
	{
		this.controller = controller;
		this.inputFile = inputFile;
		this.outputDirectory = outputDirectory;
		this.contemptState = contemptState;
		this.histogramEqualization = histogramEqualization;
	}
	
	Classifier(MainController controller, String inputFile, String outputDirectory, boolean contemptState, int width, int height, boolean grayscale, boolean histogramEqualization, boolean faceDetection)
	{
		this.controller = controller;
		this.inputFile = inputFile;
		this.outputDirectory = outputDirectory;
		this.contemptState = contemptState;
		this.grayscale = grayscale;
		this.histogramEqualization = histogramEqualization;
		this.faceDetection = faceDetection;

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
		if(percentage-difference >= 0.01) 
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
	
	/* Method for creating classification folders in the output directory. */
	protected void CreateFolders() throws SecurityException
	{
		angerDirectory = new File(outputDirectory, "Anger");
		angerDirectory.mkdirs();
		if(contemptState)
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
		surpriseDirectory = new File(outputDirectory, "Surprise");
		surpriseDirectory.mkdirs();	
	}
	
	/* Method for creating classification folders in the specified directory. */
	protected void CreateFoldersWithParameter(File directory) throws SecurityException
	{
		angerDirectory = new File(directory, "Anger");
		angerDirectory.mkdirs();
		if(contemptState)
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
		surpriseDirectory = new File(directory, "Surprise");
		surpriseDirectory.mkdirs();	
	}
	
	/* Method for creating classification folders in the specified directory and without assigning a reference to any variable. */
	protected void CreateFoldersWithParameterAndWithoutInitialization(File directory) throws SecurityException
	{
		new File(directory, "Anger").mkdirs();
		if(contemptState)
		{
			new File(directory, "Contempt").mkdirs();
		}
		new File(directory, "Disgust").mkdirs();
		new File(directory, "Fear").mkdirs();
		new File(directory, "Happiness").mkdirs();
		new File(directory, "Neutrality").mkdirs();
		new File(directory, "Sadness").mkdirs();
		new File(directory, "Surprise").mkdirs();
	}
	
	/* Method for eliminating the temporary directory. */
	protected void DeleteTempDirectory()
	{
		try 
		{
			FileUtils.deleteDirectory(new File(tempDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog(e.getMessage());
				controller.StartStopClassification(false, true);});
		}
	}
	
	/* Method for eliminating all directories. */
	protected void DeleteAllDirectories()
	{
		try 
		{
			FileUtils.cleanDirectory(new File(outputDirectory));
		} 
		catch (IOException e) 
		{
			Platform.runLater(() -> {controller.ShowErrorDialog(e.getMessage());
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
	
	/* Method for deleting the classification folders. */
	protected void DeleteClassificationFolders() throws IOException
	{
		if(angerDirectory != null)
		{
			FileUtils.deleteDirectory(angerDirectory);		
		}
		if(contemptState == true || contemptDirectory != null)
		{
			FileUtils.deleteDirectory(contemptDirectory);	
		}		
		if(disgustDirectory != null)
		{
			FileUtils.deleteDirectory(disgustDirectory);
		}
		if(fearDirectory != null)
		{
			FileUtils.deleteDirectory(fearDirectory);			
		}
		if(happinessDirectory != null)
		{
			FileUtils.deleteDirectory(happinessDirectory);			
		}
		if(neutralityDirectory != null)
		{
			FileUtils.deleteDirectory(neutralityDirectory);		
		}
		if(sadnessDirectory != null)
		{
			FileUtils.deleteDirectory(sadnessDirectory);
		}
		if(surpriseDirectory != null)
		{
			FileUtils.deleteDirectory(surpriseDirectory);	
		}
	}
	
	/* Method to create the training, validation and test folder and to divide the images between them. */
	protected void SubdivideImages(double trainPerc, double validPerc, double testPerc) throws SecurityException, IOException 
	{
		int angerTotalNumber = 0, contemptTotalNumber = 0, disgustTotalNumber = 0, fearTotalNumber = 0, happinessTotalNumber = 0, neutralityTotalNumber = 0, sadnessTotalNumber = 0, surpriseTotalNumber = 0;
		
		// Count of the number of files per-directory.
		angerTotalNumber = countNumberOfFiles(angerDirectory);
		UpdateBar(0.02);
		if(contemptState)
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
		surpriseTotalNumber = countNumberOfFiles(surpriseDirectory);		
		UpdateBar(0.14);
		
		// Create outer directories.
		File trainingDirectory, validationDirectory, testDirectory;
		trainingDirectory = new File(outputDirectory, "Training");
		trainingDirectory.mkdirs();	
		UpdateBar(0.16);
		validationDirectory = new File(outputDirectory, "Validation");
		validationDirectory.mkdirs();
		UpdateBar(0.18);
		testDirectory = new File(outputDirectory, "Test");
		testDirectory.mkdirs();		
		UpdateBar(0.2);
		
		// Create inner directories for validation and testing.
		CreateFoldersWithParameterAndWithoutInitialization(validationDirectory);
		UpdateBar(0.25);
		CreateFoldersWithParameterAndWithoutInitialization(testDirectory);		
		UpdateBar(0.3);
		
		// Subdivision of a percentage of the images in the validation and test directories.
		subdivideImages(validationDirectory.getAbsolutePath(), validPerc, angerTotalNumber, contemptTotalNumber, disgustTotalNumber, fearTotalNumber, happinessTotalNumber, neutralityTotalNumber, sadnessTotalNumber, surpriseTotalNumber);
		UpdateBar(0.6);
		subdivideImages(testDirectory.getAbsolutePath(), validPerc, angerTotalNumber, contemptTotalNumber, disgustTotalNumber, fearTotalNumber, happinessTotalNumber, neutralityTotalNumber, sadnessTotalNumber, surpriseTotalNumber);
		UpdateBar(0.9);
		
		// Move the remaining folders and images in the training directory.
		if (!Thread.currentThread().isInterrupted()) 
		{
			FileUtils.moveDirectoryToDirectory(angerDirectory, trainingDirectory, true);
			if(contemptState)
			{
				FileUtils.moveDirectoryToDirectory(contemptDirectory, trainingDirectory, true);	
			}
			FileUtils.moveDirectoryToDirectory(disgustDirectory, trainingDirectory, true);
			FileUtils.moveDirectoryToDirectory(fearDirectory, trainingDirectory, true);
			FileUtils.moveDirectoryToDirectory(happinessDirectory, trainingDirectory, true);
			FileUtils.moveDirectoryToDirectory(neutralityDirectory, trainingDirectory, true);
			FileUtils.moveDirectoryToDirectory(sadnessDirectory, trainingDirectory, true);
			FileUtils.moveDirectoryToDirectory(surpriseDirectory, trainingDirectory, true);	
			UpdateBar(1);
		}
	}
	
	/* Method for counting the number of files in a directory. */
	private int countNumberOfFiles(File directory)
	{
		int count = 0;
		if (!Thread.currentThread().isInterrupted()) 
		{
			for(File file : directory.listFiles())
			{
	            if(file.isFile()) 
	            {
	            	count++;   
	            }
			}
		}
		return count;
	}
	
	/* Method for subdividing a percentage of images in the classification directory, for validation or testing. */
	private void subdivideImages(String destination, double percentage, int angerNumber, int contemptNumber, int disgustNumber, int fearNumber, int happinessNumber, int neutralityNumber, int sadnessNumber, int surpriseNumber) throws IOException
	{

		takeNImagesRandom(angerDirectory, destination + "\\Anger\\", angerNumber, percentage);
		if(contemptState)
		{
			takeNImagesRandom(contemptDirectory, destination + "\\Contempt\\", contemptNumber, percentage);
		}
		takeNImagesRandom(disgustDirectory, destination + "\\Disgust\\", disgustNumber, percentage);
		takeNImagesRandom(fearDirectory, destination + "\\Fear\\", fearNumber, percentage);
		takeNImagesRandom(happinessDirectory, destination + "\\Happiness\\", happinessNumber, percentage);
		takeNImagesRandom(neutralityDirectory, destination + "\\Neutrality\\", neutralityNumber, percentage);	
		takeNImagesRandom(sadnessDirectory, destination + "\\Sadness\\", sadnessNumber, percentage);
		takeNImagesRandom(surpriseDirectory, destination + "\\Surprise\\", surpriseNumber, percentage);		
	}
	
	
	/* Method to randomly choose a percentage of images for a subdirectory. */
	private void takeNImagesRandom(File source, String destination, int totalNumber, double percentage) throws IOException
	{
		if (!Thread.currentThread().isInterrupted())		
		{
			Random random = new Random();
			int number = (int) (totalNumber * percentage), count = 0;
			boolean flag = false;
			while(flag == false)
			{
				for(File file : source.listFiles())
				{
		            if(file.isFile()) 
		            {
		            	if(random.nextFloat() < percentage)
		            	{
			            	count++;
			            	FileUtils.moveFileToDirectory(FileUtils.getFile(file.getAbsolutePath()), FileUtils.getFile(destination), true);
			            	if(count >= number)
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

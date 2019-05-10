package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;

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
	protected boolean grayscale, histogramEqualization, faceDetection;	
	protected double percentage = 0, difference = 0;
	protected File angerDirectory = null, disgustDirectory = null, fearDirectory = null, happinessDirectory = null, neutralityDirectory = null, sadnessDirectory = null, surpriseDirectory = null;

	Classifier(MainController controller, String inputFile, String outputDirectory, boolean histogramEqualization)
	{
		this.controller = controller;
		this.inputFile = inputFile;
		this.outputDirectory = outputDirectory;
		this.histogramEqualization = histogramEqualization;
	}
	
	Classifier(MainController controller, String inputFile, String outputDirectory, int width, int height, boolean grayscale, boolean histogramEqualization, boolean faceDetection)
	{
		this.controller = controller;
		this.inputFile = inputFile;
		this.outputDirectory = outputDirectory;
		this.grayscale=grayscale;
		this.histogramEqualization = histogramEqualization;
		this.faceDetection = faceDetection;

		tempDirectory=outputDirectory+"\\temp\\";
		
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
	
	/* Method for creating classification folders. */
	protected void CreateFolders() throws SecurityException
	{
		angerDirectory = new File(outputDirectory, "Anger");
		angerDirectory.mkdirs();
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
	
	/* Method for creating classification folders with parameters. */
	protected void CreateFoldersWithParameter(File directory) throws SecurityException
	{
		angerDirectory = new File(directory, "Anger");
		angerDirectory.mkdirs();
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
	
	/* Method for eliminating classification folders. */
	protected void DeleteClassificationFolders() throws IOException
	{
		if(angerDirectory!=null)
		{
			FileUtils.deleteDirectory(angerDirectory);
		}
		if(disgustDirectory!=null)
		{
			FileUtils.deleteDirectory(disgustDirectory);
		}
		if(fearDirectory!=null)
		{
			FileUtils.deleteDirectory(fearDirectory);
		}
		if(happinessDirectory!=null)
		{
			FileUtils.deleteDirectory(happinessDirectory);
		}
		if(neutralityDirectory!=null)
		{
			FileUtils.deleteDirectory(neutralityDirectory);
		}
		if(sadnessDirectory!=null)
		{
			FileUtils.deleteDirectory(sadnessDirectory);
		}
		if(surpriseDirectory!=null)
		{
			FileUtils.deleteDirectory(surpriseDirectory);
		}
	}
}

package it.polito.fedc.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javafx.application.Platform;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;

public class RaFDClassifier extends Classifier implements Runnable 
{
	private CascadeClassifier profileFaceCascade;
	private boolean detectProfileImages = false, squareImages = false;

	public RaFDClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean detectProfileImages, boolean squareImages, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, true, true, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Search for profile photos.
		this.detectProfileImages = detectProfileImages;
		profileFaceCascade = new CascadeClassifier(haarclassifierpath + "haarcascade_profileface.xml");
		// Make image square.
		this.squareImages = squareImages;
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "Radboud Faces Database (RaFD)", "\nprofile photos detection: " + detectProfileImages + "\nsquare images: " + squareImages + "\n")) 
		{
			return;
		}
				
		// Extraction of the images of the RaFD database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the RaFD images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the RaFD images.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Create the classification directories for the RaFD database.
			if (!createClassificationDirectories(outputDirectory)) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Read the newly extracted photos.
			File[] listOfFiles = new File(tempDirectory).listFiles();
			
			// Set the total number of photos; this is necessary to update the progress bar.
			numberOfPhotos = listOfFiles.length;
			
			// Cycle performed for every single file in the directory.
			for (int i = 0; (i < numberOfPhotos) && (!Thread.currentThread().isInterrupted()); i++) 
			{
				File file = listOfFiles[i];

				// Verify that the filename has the typical form of the RaFD database files.
				if ((file.isFile())	&& (file.getName().matches("Rafd[0-9]{3}_[0-9]{2}_[A-Z][a-z]*_[a-z]*_[a-z]*_[a-z]*\\.jpg"))) 
				{
					faceFound = true;
					Mat image = Imgcodecs.imread(file.getAbsolutePath());
										
					// Face detection and image cropping (optional).
					if (faceDetection) 
					{
						Mat grayscaleImage = new Mat(), face = new Mat();
						Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_BGR2GRAY);
						
						// Facial search based on the number of degrees.
						int degree = Integer.parseInt(file.getName().substring(4,7));
						if ((degree == 0) || (degree == 180))
						{
							absoluteFaceSize = 310;
						}
						else if ((degree == 45) || (degree == 135))
						{
							absoluteFaceSize = 300;				
						}
						else
						{
							absoluteFaceSize = 250;
						}
						
						// The cascade classifier for profiles tends to recognize better the right-oriented faces: let's use it.
						if((degree == 0) || (degree == 45))
						{			
							Mat clonedGrayscaleImage = grayscaleImage.clone();
							Core.flip(clonedGrayscaleImage, grayscaleImage, 1);
							// Release of the images.
							clonedGrayscaleImage.release();
						}
	
						MatOfRect faces = new MatOfRect();
						// Verify the presence of frontal faces through the Haar cascade classifier for frontal faces.
						frontalFaceCascade.detectMultiScale(grayscaleImage, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
						Rect[] facesArray = faces.toArray();
						
						// In the case that no frontal faces are detected and the user has chosen to also use the cascade classifier for profile faces, then any profile photos will be searched.
						if (facesArray.length == 0) 
						{
							if (detectProfileImages == true) 
							{
								// Verify the presence of profile faces through the Haar cascade classifier for profile faces.
								profileFaceCascade.detectMultiScale(grayscaleImage, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
								facesArray = faces.toArray();
							} 
							// If no front or profile face is detected, the faceFound variable will be set to false.
							if ((detectProfileImages == false) || (facesArray.length == 0))
							{
								faceFound = false;
							}
						}
						// If at least one face is detected, the photo will be cropped to this one: the others will be lost.
						if (faceFound == true) 
						{		
							// Flip the 0 and 45 degrees images.
							if((degree == 0) || (degree == 45))
							{
								Mat clonedImage = image.clone();
								Core.flip(clonedImage, image, 1);
								clonedImage.release();
							}
							face = new Mat(image, new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].width, facesArray[0].width));		
							image.release();
							// Re-flip the previously flipped image.
							if((degree == 0) || (degree == 45))
							{	
								Core.flip(face, image, 1);
							}				
							else
							{
								image = face.clone();	
							}
							// Release of the images.
							face.release();
						}
						// Release of the images.
						faces.release();
						grayscaleImage.release();
					} 

					// Photo classification phase.
					if (faceFound == true) 
					{
						// Make image square (optional).
						if (squareImages) 
						{
							image = makeImageSquare(image);
						}
						
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
						
						if (file.getName().contains("angry")) 
						{
							saveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("contempt")) 
						{
							saveImageInTheChosenFormat(contemptDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("disgusted")) 
						{
							saveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("fearful")) 
						{
							saveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("happy")) 
						{
							saveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("neutral")) 
						{
							saveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("sad")) 
						{
							saveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("surprised")) 
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
				else 
				{
					exceptionManager("The images format in the input file is not as expected.");
					return;
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
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test directories..."));
				if (!subdivideImages(outputDirectory, outputDirectory, trainPercentage, validationPercentage, testPercentage)) 
				{
					return;
				}
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary directories..."));
			} 
			else 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: deleting temporary directories..."));
			}
			deleteTempDirectoryAndReleaseResources();
		}
	}
}
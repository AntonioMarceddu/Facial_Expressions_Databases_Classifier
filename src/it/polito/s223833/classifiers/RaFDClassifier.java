package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import it.polito.s223833.Controller;
import it.polito.s223833.utils.UnzipClass;

public class RaFDClassifier extends Classifier implements Runnable 
{
	private CascadeClassifier profileFaceCascade;
	private boolean profile;

	public RaFDClassifier(Controller controller, String inputFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean faceDetection, boolean profile, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, inputFile, outputDirectory, true, true, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, faceDetection, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Search for profile photos.
		this.profile = profile;
		profileFaceCascade = new CascadeClassifier(haarclassifierpath + "haarcascade_profileface.xml");
	}

	@Override
	public void run() 
	{
		// Extraction of the images of the RaFD database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the RaFD images..."));
			unzipper.Unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e1) 
		{
			ExceptionManager("There was an error during the extraction of the RaFD files.");
			return;
		}

		// Verifies if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			// Creation of classification directories for the RaFD database.
			if (!CreateDirectories()) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Reading the newly extracted photos.
			File rafdImages = new File(tempDirectory);
			File[] listOfFiles = rafdImages.listFiles();
			// Cycle performed for every single file in the folder.
			int i = 0, numberOfFiles = listOfFiles.length;
			while ((i < numberOfFiles) && (!Thread.currentThread().isInterrupted())) 
			{
				File file = listOfFiles[i];

				// Verifies that the filename has the typical form of the RaFD database files.
				if ((file.isFile())	&& (file.getName().matches("Rafd[0-9]{3}_[0-9]{2}_[A-Z][a-z]*_[a-z]*_[a-z]*_[a-z]*\\.jpg"))) 
				{
					faceFound=true;
					Mat image = Imgcodecs.imread(file.getAbsolutePath()), tempImage = new Mat(), face = new Mat();
					
					Imgproc.cvtColor(image, tempImage, Imgproc.COLOR_BGR2GRAY);
					
					// Face detection and image cropping (optional).
					if (faceDetection) 
					{
						// Minimum face size to search.
						absoluteFaceSize = 325;
						MatOfRect faces = new MatOfRect();
						// Verifies the presence of frontal faces through the haar front face classifier.
						frontalFaceCascade.detectMultiScale(tempImage, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
						Rect[] facesArray = faces.toArray();
						
						// In the case that no frontal faces are detected and the user has chosen to also use the profile photo classifier, then any profile photos will be searched.
						if (facesArray.length == 0) 
						{
							if (profile == true) 
							{
								// Minimum face size to search.
								absoluteFaceSize = 500;
								// Verifies the presence of profile faces through the haar profile face classifier.
								profileFaceCascade.detectMultiScale(tempImage, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
								facesArray = faces.toArray();
							} 
							// If no front or profile face is detected, the faceFound variable will be set to false.
							if (profile == false || facesArray.length == 0) 
							{
								faceFound = false;
							}
						}
						// If at least one face is detected, the photo will be cropped to the face itself.
						if (faceFound == true) 
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
							image = face;						
						}
						// Release of the image.
						faces.release();
					} 
					// Release of the image.
					tempImage.release();

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
							image = HistogramEqualization(image);
						}
						
						if (file.getName().contains("angry")) 
						{
							SaveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("contempt")) 
						{
							SaveImageInTheChosenFormat(contemptDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("disgusted")) 
						{
							SaveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("fearful")) 
						{
							SaveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("happy")) 
						{
							SaveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("neutral")) 
						{
							SaveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("sad")) 
						{
							SaveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), image);
						} 
						else if (file.getName().contains("surprised")) 
						{
							SaveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), file.getName(), image);
						}
					}
					// Release of the image.
					image.release();

					// Increase the count of the number of classified photos (or, if not classified, of the analyzed photos).
					classified++;
					// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
					percentage = (double) classified / (double) numberOfFiles;
					UpdateBar();
					// Next photo.
					i++;
				} 
				else 
				{
					ExceptionManager("The format of the images in the input file is not the one expected.");
					return;
				}
			}
			// If subdivision is active, the images will be divided between train, validation and test.
			if ((subdivision) && (!Thread.currentThread().isInterrupted())) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between train, validation and test folder..."));
				if (!SubdivideImages(trainPercentage, validationPercentage, testPercentage)) 
				{
					ExceptionManager("There was an error while performing the subdivision.");
					return;
				}
			}
		}
		boolean error = false;
		// If a cancellation request has been made by the user, both temporary and classification folders will be deleted.
		if (Thread.currentThread().isInterrupted()) 
		{
			error = DeleteAllDirectories();
			Platform.runLater(() -> controller.ShowAttentionDialog("Classification interrupted.\n"));
		}
		// Otherwise only temporary ones will be deleted.
		else 
		{
			if (subdivision) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary folders..."));
			} 
			else 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: deleting temporary folders..."));
			}
			error = DeleteTempDirectory();
		}
		// Reset the buttons if no errors occured.
		if (!error)
		{
			Platform.runLater(() -> controller.StartStopClassification(false, false));
		}
	}
}
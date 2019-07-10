package it.polito.s223833.classifiers;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import it.polito.s223833.MainController;
import it.polito.s223833.utils.UnzipClass;

public class JAFFEClassifier extends Classifier implements Runnable 
{
	private boolean subdivision;
	private double trainPercentage, validationPercentage, testPercentage;
	
	public JAFFEClassifier(MainController controller, String inputFile, String outputDirectory, int width, int height, boolean histogramEqualization, boolean faceDetection, boolean subdivision, double trainPercentage, double validationPercentage, double testPercentage)
	{
		super(controller, inputFile, outputDirectory, false, width, height, false, histogramEqualization, faceDetection);
		this.subdivision = subdivision;
		this.trainPercentage = trainPercentage;
		this.validationPercentage = validationPercentage;
		this.testPercentage = testPercentage;
	}

	@Override
	public void run() 
	{
		// Extraction of the images of the JAFFE database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the JAFFE images..."));
			unzipper.Unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e1) 
		{
			ExceptionManager("There was an error during the extraction of the JAFFE files.");
	    	return;
		}
		
		// Verifies if the user has requested the cancellation of the current operation during the extraction phase.
		if(!Thread.currentThread().isInterrupted())
		{
			// Creation of classification folders for the JAFFE database.
			try 
			{
				CreateFolders();
			} 
			catch (SecurityException e)
			{
				ExceptionManager("There was a problem while creating classification folders.");
		    	return;
			}
	
			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: classification..."));
			
			// Reading the newly extracted photos.
			File jaffeImages = new File(tempDirectory, "jaffe");
			File[] listOfFiles = jaffeImages.listFiles();
			// Cycle performed for every single file in the folder.
			int i = 0, numberOfFiles = listOfFiles.length;
			boolean faceFound;
			while ((i < numberOfFiles) && (!Thread.currentThread().isInterrupted()))
			{
				File file = listOfFiles[i];
				
				// README and .DS_Store files will be ignored.
				if((!file.getName().equalsIgnoreCase("README")) && (!file.getName().equalsIgnoreCase(".DS_Store")))
				{
					// Verifies that the filename has the typical form of the JAFFE database files.
					if ((file.isFile()) && (file.getName().matches("[A-Z]{2}\\.[A-Z]{2}[0-9]\\.[0-9]*\\.tiff")))
					{
						faceFound = true;
						Mat resizedFace = Mat.zeros(imageSize, CvType.CV_8UC1);
	
						// Opening the image to be analyzed.
						Mat image = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC1);
						// Histogram equalization of the image (optional).
						if (histogramEqualization) 
						{
							Imgproc.equalizeHist(image, image);
						}
						// Face detection and image cropping (optional).
				    	if(faceDetection)
				    	{
							MatOfRect faces = new MatOfRect();
							// Verifies the presence of frontal faces through the haar front face classifier.
					    	frontalFaceCascade.detectMultiScale(image, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
	
							Rect[] facesArray = faces.toArray();
							// If at least one face is detected, the photo will be cropped to the face itself.
							if(facesArray.length > 0)
							{
								Rect rectCrop = null;
								// Only the first face will be saved: if an image has more faces, the others are lost.
								if(facesArray[0].width > facesArray[0].height)
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
				    	}		    	
				    	else
				    	{
				    		// Scaling the photo to the desired size.
							Imgproc.resize(image, resizedFace, imageSize);		
				    	}
	
				    	// Photo classification phase.
						if (faceFound == true) 
						{
							if (file.getName().contains("AN")) 
							{
								Imgcodecs.imwrite(angerDirectory + "\\" + file.getName(), resizedFace);
							} 
							else if (file.getName().contains("DI")) 
							{
								Imgcodecs.imwrite(disgustDirectory + "\\" + file.getName(), resizedFace);
							} 
							else if (file.getName().contains("FE")) 
							{
								Imgcodecs.imwrite(fearDirectory + "\\" + file.getName(), resizedFace);
							} 
							else if (file.getName().contains("HA")) 
							{
								Imgcodecs.imwrite(happinessDirectory + "\\" + file.getName(), resizedFace);
							} 
							else if (file.getName().contains("NE")) 
							{
								Imgcodecs.imwrite(neutralityDirectory + "\\" + file.getName(), resizedFace);
							} 
							else if (file.getName().contains("SA")) 
							{
								Imgcodecs.imwrite(sadnessDirectory + "\\" + file.getName(), resizedFace);
							} 
							else if (file.getName().contains("SU")) 
							{
								Imgcodecs.imwrite(surpriseDirectory + "\\" + file.getName(), resizedFace);
							}
							// Release of the initialized variables.
							resizedFace.release();
						}
						// Release of the initialized variables.
						image.release();					
					} 
					else 
					{
						ExceptionManager("The format of the images in the input file is not the one expected.");
				    	return;
					}
				}			
				// Increase the count of the number of photos classified (or, if not classified, of the analyzed photos).
				classified++;
				// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
				percentage = (double)classified / (double)numberOfFiles;
				UpdateBar();	
				// Next photo.
				i++;
			}	
			// If subdivision is active, the images will be divided between training, validation and test.
			if((subdivision)&&(!Thread.currentThread().isInterrupted()))
			{
				try 
				{
					Platform.runLater(() -> controller.setPhaseLabel("Phase 3: subdivision between training, validation and test folder..."));
					this.SubdivideImages(trainPercentage, validationPercentage, testPercentage);
				} 
				catch (SecurityException | IOException e) 
				{
					ExceptionManager("An error occurred during the subdivision.");
			    	return;
				}
			}
		}
		// If a cancellation request has been made by the user, both temporary and classification folders will be deleted.
		if (Thread.currentThread().isInterrupted()) 
		{
			DeleteAllDirectories();	
			Platform.runLater(() -> controller.ShowAttentionDialog("Classification interrupted.\n"));
		}	
		// Otherwise only temporary ones will be deleted.
		else
		{
			if(subdivision)
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary folders..."));
			}
			else
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 3: deleting temporary folders..."));
			}
			DeleteTempDirectory();		
		}
	
		Platform.runLater(() -> controller.StartStopClassification(false, false));
	}
}
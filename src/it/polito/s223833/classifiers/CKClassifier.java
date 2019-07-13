package it.polito.s223833.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javafx.application.Platform;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.s223833.MainController;
import it.polito.s223833.utils.UnzipClass;

public class CKClassifier extends Classifier implements Runnable 
{
	private String emotionFile;

	public CKClassifier(MainController controller, String inputFile, String emotionFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, boolean faceDetection, boolean subdivision, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, inputFile, outputDirectory, true, true, width, height, format, grayscale, histogramEqualization, faceDetection, subdivision, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 150;
		// Emotion file.
		this.emotionFile = emotionFile;
	}

	@Override
	public void run() 
	{
		// Extraction of the labels and images of the CK + database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the emotion labels..."));
			unzipper.Unzip(controller, emotionFile, tempDirectory);
			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: extraction of the CK+ images..."));
			unzipper.Unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e1) 
		{
			ExceptionManager("There was an error during the extraction of the CK+ files.");
			return;
		}

		// Verifies if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{
			File inputFolder;
			File[] listOfExternalFolders;
			int i = 0, j = 0, numberOfExternalFolders, numberOfInternalFolders;
			boolean neutralTaken;

			// Creation of classification directories for the CK+ database.
			if (!CreateDirectories()) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 3: classification..."));

			// Reading of the files in the emotions folder.
			inputFolder = new File(tempDirectory, "emotion");
			listOfExternalFolders = inputFolder.listFiles();
			numberOfExternalFolders = listOfExternalFolders.length;

			// Cycle performed for each individual external folder.
			for (i = 0; i < numberOfExternalFolders; i++) 
			{
				File externalFolder = listOfExternalFolders[i];
				File[] listOfInternalFolders = externalFolder.listFiles();
				numberOfInternalFolders = listOfInternalFolders.length;

				// Verifies that the folders start with an S followed by a sequence of three numbers.
				if (!externalFolder.getName().matches("S[0-9]{3}")) 
				{
					ExceptionManager("The format of the folders in the input file is not the one expected.");
					return;
				}

				j = 0;
				neutralTaken = false;
				// Cycle performed for each individual internal folder.
				while ((j < numberOfInternalFolders) && (!Thread.currentThread().isInterrupted())) 
				{
					File internalFolder = listOfInternalFolders[j];
					File[] listOfFiles = internalFolder.listFiles();

					// Verifies that the folders are composed of a sequence of three numbers.
					if (!internalFolder.getName().matches("[0-9]{3}")) 
					{
						ExceptionManager("The format of the folders in the input file is not the one expected.");
						return;
					}

					// Read the internal file.
					if (listOfFiles.length >= 1) 
					{
						// Verifies that the filename has the typical form of the CK+ database files.
						if (!listOfFiles[0].getName().matches("S[0-9]{3}_[0-9]{3}_[0-9]*_[a-z]{7}\\.[a-z]{3}")) 
						{
							ExceptionManager("The format of the images in the input file is not the one expected.");
							return;
						}

						FileReader fr = null;
						BufferedReader br = null;
						try 
						{
							fr = new FileReader(listOfFiles[0]);
							br = new BufferedReader(fr);
							// Reading the first line of the file.
							String line = br.readLine();
							br.close();
							fr.close();

							// Reconstruction of the file name and checking of its existence.
							File file = new File(tempDirectory + "\\cohn-kanade-images\\" + externalFolder.getName() + "\\" + internalFolder.getName(),	listOfFiles[0].getName().substring(0, (int) listOfFiles[0].getName().length() - 12) + ".png");
							if (file.exists()) 
							{
								// Edit, classify and save the photo.
								doOperationsWithFile(file, line);
							} 
							else
							{
								ExceptionManager("Images are not in the expected position.");
								return;
							}
						} 
						catch (IOException e) 
						{
							ExceptionManager("There was a problem while trying to read an emotion file.");
							return;
						}
					}

					// A neutral photo is also taken for each subject in the Cohn-Kanade database.
					if (!neutralTaken) 
					{
						String fileName = externalFolder.getName() + "_" + internalFolder.getName() + "_00000001.png";
						// Reconstruction of the file name and checking of its existence.
						File neutralFile = new File(tempDirectory + "\\cohn-kanade-images\\" + externalFolder.getName()	+ "\\" + internalFolder.getName(), fileName);
						if (neutralFile.exists()) 
						{
							// Edit, classify and save the photo.
							doOperationsWithFile(neutralFile, "0");
							neutralTaken = true;
						} 
						else 
						{
							ExceptionManager("Images are not in the expected position.");
							return;
						}
					}
					// Next folder.
					j++;
				}
			}
			// If subdivision is active, the images will be divided between train, validation and test.
			if ((subdivision) && (!Thread.currentThread().isInterrupted())) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: subdivision between train, validation and test folder..."));
				if (!SubdivideImages(trainPercentage, validationPercentage, testPercentage)) 
				{
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
			if (subdivision) 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 5: deleting temporary folders..."));
			} 
			else 
			{
				Platform.runLater(() -> controller.setPhaseLabel("Phase 4: deleting temporary folders..."));
			}
			DeleteTempDirectory();
		}

		Platform.runLater(() -> controller.StartStopClassification(false, false));
	}

	/* Method for editing, classifying and saving the photo. */
	private void doOperationsWithFile(File file, String emotion) 
	{
		Mat image, resizedFace = Mat.zeros(imageSize, CvType.CV_8UC1);

		// Open the image to be analyzed.
		if (grayscale) 
		{
			image = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC1);
		} 
		else 
		{
			image = Imgcodecs.imread(file.getAbsolutePath());
		}

		// Histogram equalization of the image (optional).
		if (histogramEqualization) 
		{
			image = HistogramEqualization(image);
		}
		// Face detection and image cropping (optional).
		if (faceDetection) 
		{
			resizedFace = FrontalFaceDetection(image, resizedFace);
		} 
		else 
		{
			// Scaling the photo to the desired size.
			Imgproc.resize(image, resizedFace, imageSize);
		}

		// Photo classification phase.
		if (faceFound == true) 
		{
			if (emotion.equals("0")) 
			{
				SaveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), file.getName(), resizedFace);
			}
			if (emotion.contains("1")) 
			{
				SaveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), file.getName(), resizedFace);
			} 
			else if (emotion.contains("2")) 
			{
				SaveImageInTheChosenFormat(contemptDirectory.getAbsolutePath(), file.getName(), resizedFace);
			} 
			else if (emotion.contains("3")) 
			{
				SaveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), file.getName(), resizedFace);
			} 
			else if (emotion.contains("4")) 
			{
				SaveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), file.getName(), resizedFace);
			} 
			else if (emotion.contains("5")) 
			{
				SaveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), file.getName(), resizedFace);
			} 
			else if (emotion.contains("6")) 
			{
				SaveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), file.getName(), resizedFace);
			} 
			else if (emotion.contains("7")) 
			{
				SaveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), file.getName(), resizedFace);
			}
			// Release of the initialized variables.
			resizedFace.release();
		}
		// Release of the initialized variables.
		image.release();

		// Increase the count of the number of photos classified (or, if not classified, of the analyzed photos).
		classified++;
		// Calculation of the percentage of completion of the current operation and update of the classification progress bar.
		percentage = (double) classified / (double) 450; // (327 labeled + 123 neutral)
		UpdateBar();
	}
}
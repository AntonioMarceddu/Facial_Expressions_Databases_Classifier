package it.polito.fedc.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;
import javafx.application.Platform;

public class IMFDBClassifier extends Classifier implements Runnable 
{
	boolean error = true, duplicateInformation, existsValidImages, imageExist, moreEmotionalLabels, squareImages = false;
	private FileReader fr = null;
	private BufferedReader br = null;
	String emotionRead = "", pictureRead = "", line, imageDirectory;
	ArrayList <String> pictureList = new ArrayList <String>();
	ArrayList <String> emotionList = new ArrayList <String>();
	int value;
	
	public IMFDBClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean squareImages, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, false, true, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, false, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 200;	
		// Make image square.
		this.squareImages = squareImages;
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "Indian Movie Face Database (IMFDB)", "")) 
		{
			return;
		}
		
		// Extraction of the labels and images of the IMFDB database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the IMFDB images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the IMFDB images.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{		
			// Set the total number of photos; this is necessary to update the progress bar.
			// This is the total number of database photos that can actually be classified.
			numberOfPhotos = 29623;
			
			// Create the classification directories for the IMFDB database.
			if (!createClassificationDirectories(outputDirectory)) 
			{
				return;
			}

			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			try
			{
				// Read the files in the emotions directory.
				File[] listOfExternalDirectories = new File(tempDirectory, "IMFDB_final").listFiles();

				// Cycle performed for each external directory.
				for (int i = 0; (i < listOfExternalDirectories.length) && (!Thread.currentThread().isInterrupted()); i++) 
				{		
					// Avoid the .DS_Store file.
					if (listOfExternalDirectories[i].isDirectory())
					{
						File[] listOfInternalDirectories = listOfExternalDirectories[i].listFiles();

						// Cycle performed for each internal directory.
						for (int j = 0; (j < listOfInternalDirectories.length) && (!Thread.currentThread().isInterrupted()); j++) 
						{
							// Avoid the .DS_Store files and other directories that contains a wrong correlation between the images and the corresponding emotions.
							if (listOfInternalDirectories[j].isDirectory())
							{
								// Reset the valid image counter.
								existsValidImages = false;
								
								// Search for the image directory and the emotional labels file.
								File[] listOfInternalDirectoryElements = listOfInternalDirectories[j].listFiles();
								value = -1;
								imageDirectory = "";
								for (int k = 0; (k < listOfInternalDirectoryElements.length) && (!Thread.currentThread().isInterrupted()); k++) 
								{
									if (listOfInternalDirectoryElements[k].getName().contains(".txt"))
									{
										// The KavyaMadhavan\chandranudikkunnadikhil2 directory contains two .txt files, so a dedicate fix is needed to pick the right one.
										if(listOfInternalDirectories[j].getAbsolutePath().contains("KavyaMadhavan\\chandranudikkunnadikhil2"))
										{
											if(listOfInternalDirectoryElements[k].getName().equals("chandranudikkunnadikhil2.txt"))
											{
												value = k;
											}
										}
										else
										{
											value = k;
										}
									}
									if(listOfInternalDirectoryElements[k].isDirectory())
									{
										imageDirectory = listOfInternalDirectoryElements[k].getName();
									}
								}
								
								// Enter if the image directory and the emotional labels file were found.
								if ((!imageDirectory.equals("")) && (value != -1 ))
								{
									//Open the emotional labels file and the images folder.
									fr = new FileReader(listOfInternalDirectoryElements[value].getAbsolutePath());
									br = new BufferedReader(fr);							    
									File[] listOfImages = new File(listOfInternalDirectories[j], imageDirectory).listFiles();
									
									// Reading the emotional labels file.
									while (((line = br.readLine()) != null) && (!Thread.currentThread().isInterrupted())) 
									{				
										String[] lineSplits = line.split("\t");
										// Gets the image and the emotional label.
										emotionRead = searchEmotionLabel(lineSplits);
										pictureRead = searchImage(lineSplits);
										// Discard the line if one of the informations was not found.
										if ((emotionRead != "") && (pictureRead != ""))
										{
											emotionList.add(emotionRead);
											pictureList.add(pictureRead);
										}
									}						

									// Search for duplicate information for the same image.
									for (int z = 0; z < pictureList.size(); z++)
									{
										moreEmotionalLabels = true;
										imageExist = false;
										duplicateInformation = false;
										for (int x = z + 1 ; x < pictureList.size(); x++)
										{
											if (pictureList.get(z).equals(pictureList.get(x)))
											{
												duplicateInformation = true;
												// Check if there is more than one emotional label for the same image; if true, the image cannot be classified.
												if (!emotionList.get(z).equals(emotionList.get(x)))
												{
													moreEmotionalLabels = false;												
												}
												// Remove the element and decrement x in order to update also the for count.
												pictureList.remove(x);
												emotionList.remove(x);
												x--;
											}
										}
										
										// Check if image exist.
										for (int v = 0; v < listOfImages.length; v++)
										{
											if (listOfImages[v].getName().equals(pictureList.get(z)))
											{
												imageExist = true;
												break;
											}
										}	

										// The image has no duplicate information: perform the operations desired by the user.
										if (duplicateInformation == false)
										{
											if (imageExist)
											{
												performOperationsOnImage(listOfInternalDirectories[j].getAbsolutePath() + "\\" + imageDirectory, pictureList.get(z), emotionList.get(z));
											}
											else
											{
												logger.log(Level.WARNING, "The " + listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + "\\" + imageDirectory + "\\" + pictureList.get(z)  + " image has duplicate information and the emotion is constant, but the photo wasn't found in the images folder. Therefore it cannot be classified.\n");
											}
										}
										// The image has duplicate information.
										else
										{
											// There is a unique emotional label for a single image.
											if (moreEmotionalLabels)
											{
												// There is only one emotional label for the image and it exists: perform the operations desired by the user.
												if (imageExist)
												{
													logger.log(Level.WARNING, "The " + listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + "\\" + imageDirectory + "\\" + pictureList.get(z)  + " image has duplicate information, but in them the emotion is constant. It will then be classified.\n");
													performOperationsOnImage(listOfInternalDirectories[j].getAbsolutePath() + "\\" + imageDirectory, pictureList.get(z), emotionList.get(z));
												}
												// There is only one emotional label for the image but it do not exists.
												else
												{
													logger.log(Level.WARNING, "The " + listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + "\\" + imageDirectory + "\\" + pictureList.get(z)  + " image has duplicate information and the emotion is constant, but the photo wasn't found in the images folder. Therefore it cannot be classified.\n");
												}
											}
											// There is more than one emotional label for a single image.
											else
											{
												logger.log(Level.WARNING, "The " + listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + "\\" + imageDirectory + "\\" + pictureList.get(z)  + " image has duplicate information, but in them the emotion is not constant. Therefore it cannot be classified.\n");
											}
										}									
									}
									
									// Clear the lists for the next cycle.
									pictureList.clear();
									emotionList.clear();
									
									// Close the handlers.
									if (br != null) 
									{
										br.close();
								    }	
									if (fr != null) 
									{
										fr.close();
								    }
								}
								else
								{
									if (imageDirectory.equals(""))
									{
										logger.log(Level.WARNING, "No images directory found in the " +  listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + " directory.\n");
									}
									else
									{
										logger.log(Level.WARNING, "No emotional labels file found in the " +  listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + " directory.\n");
									}								
								}
								
								if ((existsValidImages == false) && (value != -1 ) && (!imageDirectory.equals("")))
								{
									logger.log(Level.WARNING, "The emotional labels file in the " +  listOfExternalDirectories[i].getName() + "\\" +  listOfInternalDirectories[j].getName() + " directory contains incomplete information and therefore it was not possible to classify the photo in that directory.\n");
								}
							}											
						}
					}					
				}
				error = false;
			}
			catch (IOException | SecurityException e)
			{
				exceptionManager("There was an error while closing the reading stream from the fer2013.csv file.");
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
					exceptionManager("There was an error while closing the emotional labels file.");
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
	
	/* Method for editing, classifying and save the photo. */
	private void performOperationsOnImage(String path, String picture, String emotion) 
	{
		existsValidImages = true;
		
		Mat image = Imgcodecs.imread(path + "\\" +  picture);
			
		// Make image square (optional).
		if (squareImages) 
		{
			image = makeImageSquare(image);
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
							
			if (emotion.equals("ANGER")) 
			{
				saveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), picture, image);
			}
			else if (emotion.equals("DISGUST")) 
			{
				saveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), picture, image);
			} 
			else if (emotion.equals("FEAR")) 
			{
				saveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), picture, image);
			} 
			else if (emotion.equals("HAPPINESS")) 
			{
				saveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), picture, image);
			} 
			else if (emotion.equals("NEUTRAL")) 
			{
				saveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), picture, image);
			}
			else if (emotion.equals("SADNESS")) 
			{
				saveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), picture, image);
			} 
			else if (emotion.equals("SURPRISE")) 
			{
				saveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), picture, image);
			}
			
			// Release the image and update the progress bar.
			image.release();
			updateProgressBar();												
		}
	}
	
	/* Method to look for the emotion label. */
	private String searchImage (String [] stringArray)
	{
		int size = stringArray.length;
		
		// Discard the line if has no emotion information.
		if (size >= 12)
		{			
			// Less recurring case: the emotion label is in other positions.
			for (int i = 0; i < size; i++)
			{
				if(stringArray[i].matches("[a-zA-Z0-9.]+[-_][0-9]+.jpg"))
				{
					return stringArray[i];
				}
			}
		}
		return "";
	}
	
	/* Method to look for the emotion label. */
	private String searchEmotionLabel (String [] stringArray)
	{
		int size = stringArray.length;
		
		// Discard the line if has no information.
		if (size >= 12)
		{		
			// Majority case: the emotion label is in position 12.
			if ((stringArray[11].equals("ANGER")) 
			|| (stringArray[11].equals("DISGUST"))  
			|| (stringArray[11].equals("FEAR")) 
			|| (stringArray[11].equals("HAPPINESS")) 
			|| (stringArray[11].equals("NEUTRAL")) 
			|| (stringArray[11].equals("SADNESS")) 
			|| (stringArray[11].equals("SURPRISE"))) 
			{
				return stringArray[11];
			}
			// Less recurring case: the emotion label is in other positions.
			else
			{
				for (int i = 0; i < size; i++)
				{
					if ((stringArray[i].equals("ANGER")) 
					|| (stringArray[i].equals("DISGUST"))  
					|| (stringArray[i].equals("FEAR")) 
					|| (stringArray[i].equals("HAPPINESS")) 
					|| (stringArray[i].equals("NEUTRAL")) 
					|| (stringArray[i].equals("SADNESS")) 
					|| (stringArray[i].equals("SURPRISE"))) 
					{
						return stringArray[i];			
					}
				}
			}		
			// Even less recurring case: the label of emotions presents a space and another data.
			for (int i = 0; i < size; i++)
			{
				String[] stringArraySplit = stringArray[i].split(" ");
				int splitSize = stringArraySplit.length;
				
				for (int j = 0; j < splitSize; j++)
				{	
					if ((stringArraySplit[j].equals("ANGER")) 
					|| (stringArraySplit[j].equals("DISGUST"))  
					|| (stringArraySplit[j].equals("FEAR")) 
					|| (stringArraySplit[j].equals("HAPPINESS")) 
					|| (stringArraySplit[j].equals("NEUTRAL")) 
					|| (stringArraySplit[j].equals("SADNESS")) 
					|| (stringArraySplit[j].equals("SURPRISE"))) 
					{
						return stringArraySplit[j];			
					}
				}
			}			
		}
		
		return "";		
	}
}

package it.polito.fedc.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import it.polito.fedc.Controller;
import it.polito.fedc.utils.UnzipClass;
import javafx.application.Platform;

public class RAFDBClassifier extends Classifier implements Runnable 
{
	private String emotionalLabelsFile;
	private boolean compound = false, error = true;
	private FileReader fr = null;
	private BufferedReader br = null;	
	public RAFDBClassifier(Controller controller, String logDirectoryName, String logFileName, String inputFile, String emotionalLabelsFile, String outputDirectory, int width, int height, int format, boolean grayscale, boolean histogramEqualization, int histogramEqualizationType, double tileSize, double contrastLimit, boolean subdivision, boolean validation, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		super(controller, logDirectoryName, logFileName, inputFile, outputDirectory, false, true, width, height, format, grayscale, histogramEqualization, histogramEqualizationType, tileSize, contrastLimit, false, subdivision, validation, trainPercentage, validationPercentage, testPercentage);
		// Minimum face size to search. Improves performance if set to a reasonable size, depending on the size of the faces of the people depicted in the database.
		absoluteFaceSize = 200;	
		// Emotion file.
		this.emotionalLabelsFile = emotionalLabelsFile;		
	}

	@Override
	public void run() 
	{
		// Instantiation of the logger and print of the first message to screen.
		if (!instantiateLoggerAndLogStartMessage(this.getClass().getName(), "Real-world Affective Faces Database (RAF-DB)", "\nCompound emotions: " + compound + "\n")) 
		{
			return;
		}
		
		// Extraction of the labels and images of the RAF-DB database.
		UnzipClass unzipper = new UnzipClass();
		try 
		{
			Platform.runLater(() -> controller.setPhaseLabel("Phase 1: extraction of the RAF-DB images..."));
			unzipper.unzip(controller, inputFile, tempDirectory);
		} 
		catch (IOException e) 
		{
			exceptionManager("There was an error during the extraction of the RAF-DB images.");
			return;
		}

		// Verify if the user has requested the cancellation of the current operation during the extraction phase.
		if (!Thread.currentThread().isInterrupted()) 
		{		
			Platform.runLater(() -> controller.setPhaseLabel("Phase 2: execution of the operations chosen by the user..."));

			// Read the files in the EmoLabel directory.
			File emotionalLabels = new File(emotionalLabelsFile);
			if (emotionalLabels.exists())
			{		
				try 
				{
					int i = 0, emotion = 0;
					fr = new FileReader(emotionalLabels);
					br = new BufferedReader(fr);

					String line;
					String[] lineSplits = new String[2];
					File[] listOfImages = new File(tempDirectory, "aligned").listFiles();
					
					// Set the total number of photos; this is necessary to update the progress bar.
					numberOfPhotos = listOfImages.length;
					
					// Double check.
					while (((line = br.readLine()) != null) && (!Thread.currentThread().isInterrupted())) 
					{
						i++;
					}
					
					// If there are less than 4000 files the images are the compound ones.
					if ((numberOfPhotos == 3954) && (i == 3954))
					{
						compound = true;
					}	
					else if ((numberOfPhotos != 15339) || (i != 15339))
					{
						exceptionManager("The number of images and emotional labels is not as expected.");
						return;
					}
					
					// Reset the buffer.
					resetBuffer();
					
					// Create the classification directories for the RAF-DB database.
					if (!compound)
					{
						if (!createClassificationDirectories(outputDirectory)) 
						{
							return;
						}
					}
					else
					{
						if (!createCompoundEmotionsDirectories(outputDirectory)) 
						{
							return;
						}
					}
					
					// Cycle through the files.
					for (i = 0; (i < numberOfPhotos) && (!Thread.currentThread().isInterrupted()); i++)
					{
						emotion = -1;
						// Cycle through the label in order to search the corresponding one.
						while (!Thread.currentThread().isInterrupted()) 
						{
							line = br.readLine();
							if (line == null)
							{
								resetBuffer();
							}
							else
							{
								// Split the line for the space character and read the emotion value.
								lineSplits = line.split(" ");
								lineSplits[0] = removeFileExtension(lineSplits[0]);
								lineSplits[0] = lineSplits[0] + "_aligned.jpg";
								if (lineSplits[0].contains(listOfImages[i].getName()))
								{
									emotion = Integer.parseInt(lineSplits[1]);
									break;
								}	
							}						
						}

						// Emotional label found.
						if (emotion != -1)
						{
							Mat image = Imgcodecs.imread(listOfImages[i].getAbsolutePath());
							
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
							
							//Basic emotions case.
							if(!compound)
							{
								// Surprise.
								if (emotion == 1)
								{
									saveImageInTheChosenFormat(surpriseDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
								// Fear.
								else if (emotion == 2)
								{
									saveImageInTheChosenFormat(fearDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
								// Disgust.
								else if (emotion == 3)
								{
									saveImageInTheChosenFormat(disgustDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
								// Happiness.
								else if (emotion == 4)
								{
									saveImageInTheChosenFormat(happinessDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
								// Sadness.
								else if (emotion == 5)
								{
									saveImageInTheChosenFormat(sadnessDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
								// Anger.
								else if (emotion == 6)
								{
									saveImageInTheChosenFormat(angerDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
								// Neutrality.
								else if (emotion == 7) 
								{
									saveImageInTheChosenFormat(neutralityDirectory.getAbsolutePath(), listOfImages[i].getName(), image);
								} 
							}
							//Compound emotions case.
							else
							{
								// Happily Surprised.
								if (emotion == 1)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Happily Surprised//", listOfImages[i].getName(), image);
								} 
								// Happily Disgusted.
								else if (emotion == 2)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Happily Disgusted//", listOfImages[i].getName(), image);
								} 
								// Sadly Fearful.
								else if (emotion == 3)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Sadly Fearful//", listOfImages[i].getName(), image);
								} 
								// Sadly Angry.
								else if (emotion == 4)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Sadly Angry//", listOfImages[i].getName(), image);
								} 
								// Sadly Surprised.
								else if (emotion == 5)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Sadly Surprised//", listOfImages[i].getName(), image);
								} 
								// Sadly Disgusted.
								else if (emotion == 6)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Sadly Disgusted//", listOfImages[i].getName(), image);
								} 
								// Fearfully Angry.
								else if (emotion == 7) 
								{
									saveImageInTheChosenFormat(outputDirectory + "//Fearfully Angry//", listOfImages[i].getName(), image);
								} 
								// Fearfully Surprised.
								else if (emotion == 8)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Fearfully Surprised//", listOfImages[i].getName(), image);
								} 
								// Angrily Surprised.
								else if (emotion == 9)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Angrily Surprised//", listOfImages[i].getName(), image);
								} 
								// Angrily Disgusted.
								else if (emotion == 10)
								{
									saveImageInTheChosenFormat(outputDirectory + "//Angrily Disgusted//", listOfImages[i].getName(), image);
								} 
								// Disgustedly Surprised.
								else if (emotion == 11) 
								{
									saveImageInTheChosenFormat(outputDirectory + "//Disgustedly Surprised//", listOfImages[i].getName(), image);
								} 
							}
							
							// Release the image.
							image.release();
						}
						// Emotional label not found.
						else
						{
							logger.log(Level.WARNING, "No emotional label was found for the file " + listOfImages[i].getName() + ".\n"); 
						}

						// Update the progress bar.
						updateProgressBar();
					}
					error = false;
				} 
				catch (NumberFormatException e) 
				{
					exceptionManager("The emotional labels file has a different format than the one expected.");
				}
				catch (FileNotFoundException e) 
				{
					exceptionManager("The emotional labels file was not found.");
				}
				catch (NullPointerException | IOException e) 
				{
					exceptionManager("There was an error while performing the classification.");
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
			else
			{
				exceptionManager("The file containing the emotional labels of the images was not found.");
				return;
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
	
	/* Method to create the train, validation and test directory and to divide the images between them. */
	/* Overrided in order to support the NimStim Set Of Facial Expressions database features, that includes the calm state and the open - closed mouth variants. */
	@Override
	protected boolean subdivideImages(String trainValTestDirectory, String classificationDirectory, double trainPercentage, double validationPercentage, double testPercentage) 
	{
		try 
		{
			// Creates the train directory.
			File trainDirectory, validationDirectory = null, testDirectory;
			trainDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Train", 0.1);
			
			// Creates the test directory and the inner directories.
			testDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Test", 0.2);
			if (!compound)
			{
				if (!createClassificationDirectories(testDirectory.getAbsolutePath())) 
				{
					return false;
				}
			}
			else
			{
				if (!createCompoundEmotionsDirectories(testDirectory.getAbsolutePath())) 
				{
					return false;
				}
			}
			
			if (validation)
			{
				// Creates the validation directory and the inner directories.
				validationDirectory = createFolderAndUpdateProgressBar(trainValTestDirectory, "Validation", 0.3);
				if (!compound)
				{
					if (!createClassificationDirectories(validationDirectory.getAbsolutePath())) 
					{
						return false;
					}
				}
				else
				{
					if (!createCompoundEmotionsDirectories(validationDirectory.getAbsolutePath())) 
					{
						return false;
					}
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
			exceptionManager("An error occurred during the subdivision.");
			return false;
		}
	}
	
	/* Method for creating classification directories in the specified directory and without assigning a reference to any variable. */
	protected boolean createCompoundEmotionsDirectories(String baseDirectory) 
	{
		try 
		{
			new File(baseDirectory, "Happily Surprised").mkdirs();
			new File(baseDirectory, "Happily Disgusted").mkdirs();
			new File(baseDirectory, "Sadly Fearful").mkdirs();
			new File(baseDirectory, "Sadly Angry").mkdirs();
			new File(baseDirectory, "Sadly Surprised").mkdirs();
			new File(baseDirectory, "Sadly Disgusted").mkdirs();
			new File(baseDirectory, "Fearfully Angry").mkdirs();
			new File(baseDirectory, "Fearfully Surprised").mkdirs();
			new File(baseDirectory, "Angrily Surprised").mkdirs();
			new File(baseDirectory, "Angrily Disgusted").mkdirs();
			new File(baseDirectory, "Disgustedly Surprised").mkdirs();
			
			// Log update.
			logger.log(Level.INFO, "The classification directories for the compound emotions inside the " + baseDirectory + " folder have been successfully created.\n"); 
			
			return true;
		} 
		catch (SecurityException e) 
		{
			exceptionManager("There was an error while creating classification directories.");
			return false;
		}
	}	
	
	private void resetBuffer() throws FileNotFoundException, IOException
	{
		fr.close();
		br.close();
		fr = new FileReader(emotionalLabelsFile);
		br = new BufferedReader(fr);
	}
}

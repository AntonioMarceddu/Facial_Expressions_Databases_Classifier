package it.polito.s223833.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.application.Platform;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import it.polito.s223833.MainController;

public class UntarClass 
{
	/* Method for decompressing a tar file. */
	public void untar(MainController controller, String tarFile, String destinationDir) throws IOException
	{
		long total=0;
		double percentage = 0, difference = 0;
		
        FileInputStream fis = new FileInputStream(tarFile);
        TarArchiveInputStream tais = new TarArchiveInputStream(fis);
        TarArchiveEntry entry = null;
        
        // Save the total number of bytes in the file.
        total=fis.available();
        
        // Iteration on the tar file entries.
        while (((entry = tais.getNextTarEntry()) != null) && (!Thread.currentThread().isInterrupted()))
        {
            File outputFile = new File(destinationDir + File.separator + entry.getName());
            
            // Case where the entry is a directory.
            if(entry.isDirectory())
            {              
            	outputFile.mkdirs();
            }
            // Case where the entry is a file.
            else
            {
            	// Creation of parent directories.
                outputFile.getParentFile().mkdirs();
                
                // Reading from entry and writing to file.
                FileOutputStream fos = new FileOutputStream(outputFile); 
                IOUtils.copy(tais, fos);
                fos.close();
            }
            
            // Calculation of the percentage of completion of the current operation and update of the unarchiving progress bar.
    		percentage = (double) tais.getBytesRead() / (double) total;
    		if (percentage - difference >= 0.01) 
    		{
    			difference = difference + (double) 0.01;
    			difference = Math.round(difference * 100);
    			difference = difference / 100;
            
    			final double value = percentage;
    			Platform.runLater(() -> controller.updateProgressBar(value));
    		}	
        }
        tais.close();
        fis.close();
    }
}

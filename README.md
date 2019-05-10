# Facial Expression Database Classifier

<p align="center">
    <img src="https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier/blob/master/Resources/FEDC1.png"><br>
	Screenshot of the Facial Expression Database Classifier program.
</p>

Description available in english and italian below. The license is at the bottom of the page.

## English

### Facial Expression Database Classifier
Facial Expression Database Classifier (FEDC) is a program able to automatically classify images of some of the most used databases, depicting posed human faces:
* Extended Cohn-Kanade Database (CK+);
* Facial Expression Recognition 2013 Database (FER2013);
* Japanese Female Facial Expression (JAFFE);
* Multimedia Understanding Group Database (MUG);
* Radboud Faces Database (RaFD).

In practice, FEDC exploits the pre-classification implemented by the database creators. In addition to this, FEDC is also able to do several useful operations on the images, in order to simplify the neural network training operations:
* scaling of the horizontal and vertical resolutions;
* conversion in grayscale color space;
* histogram equalization;
* face detection to crop the images to face only.

This allows, for the people who make use of this databases, to minimize the time necessary for their classification, so that they can dedicate directly to other tasks, such as training of a neural network.

### The FEDC Interface
FEDC has a clean and essential user interface, consisting of four macro areas:
* in the left column, it is possible to choose the database to be classified;
* in the right column, it is possible to select the operations to be performed on the photos: those available have already been mentioned previously;
* in the lower part of the window, there are the buttons for selecting the input file, the output folder, and for starting and canceling the classification;
* finally, above the buttons, there is the progress bar, that indicates the progression of the current operation.

It should be noted that:
* the user must choose a size for the photos to be classified: it must be between 48x48 and 1024x1024 pixels. For the FER2013 database, since the starting images have a 48x48 pixels resolutions, this possibility, alongside to the face cropping feature, is not available;
* the JAFFE and the FER2013 databases only contain grayscale images;
* the RaFD database also contains photos taken in profile: the program excels in the recognition of frontal photos and allows recognition to be made even for this type of photo, although it is likely that it will not be able to classify all the photos of this type.

### Final Notes
The images automatically classified with this program can be used, for example, for the creation of a neural network with Keras: using Python with the Scikit-learn library, these images can be subdivided in the training, validation, and test dataset or cross-validated.

Access to the databases mentioned above is usually allowed only for research purposes: for more information, refer to the databases sites. FEDC was created by [Antonio Costantino Marceddu](https://www.linkedin.com/in/antonio-marceddu/) resorting to Eclipse, with Java and the addition of the OpenCV framework.

<p align="center">
    <img src="https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier/blob/master/Resources/FEDC2.png"><br>
	Screenshot of the Facial Expression Database Classifier program.
</p>

## Italiano

### Facial Expression Database Classifier
Facial Expression Database Classifier (FEDC) è un programma in grado di classificare automaticamente le immagini di alcuni tra i database utilizzati, raffiguranti volti umani in posa:
* Extended Cohn-Kanade Database (CK+);
* Facial Expression Recognition 2013 Database (FER2013);
* Japanese Female Facial Expression (JAFFE);
* Multimedia Understanding Group Database (MUG);
* Radboud Faces Database (RaFD).

Nella pratica, FEDC sfrutta la pre-classificazione implementata dai creatori del database. Oltre a ciò, FEDC è anche in grado di eseguire diverse operazioni utili sulle immagini, al fine di semplificare le operazioni di addestramento della rete neurale:
* ridimensionamento della risoluzione orizzontale e verticale;
* conversione dello spazio dei colori in scala di grigi;
* equalizzazione dell'istogramma;
* rilevamento del volto per il ritaglio delle immagini al solo volto.

Ciò consente, per le persone che fanno uso di questi database, di ridurre al minimo il tempo necessario per la loro classificazione, in modo che possano dedicarsi direttamente ad altre attività, come l'addestramento di una rete neurale.

### L'interfaccia di FEDC
FEDC ha un'interfaccia utente pulita ed essenziale, composta da quattro macro aree:
* nella colonna di sinistra, è possibile scegliere il database da classificare;
* nella colonna di destra, è possibile selezionare le operazioni da eseguire sulle foto: quelle disponibili sono già state citate in precedenza;
* nella parte inferiore della finestra, sono presenti i bottoni per selezionare il file di input, la cartella di output e per l'avvio e la cancellazione della classificazione;
* infine, sopra i bottoni, è presente la barra di avanzamento, che indica la progressione dell'operazione corrente.

Occorre notare che:
* l'utente deve scegliere una dimensione per le foto da classificare: essa deve essere compresa tra 48x48 e 1024x1024 pixel. Per il database FER2013, poiché le immagini iniziali hanno una risoluzione di 48x48 pixel, questa possibilità, insieme alla funzione di ritaglio del volto, non è disponibile;
* i database JAFFE e FER2013 contengono solo immagini in scala di grigi;
* il database RaFD contiene anche delle foto scattate di profilo: il programma eccelle nel riconoscimento delle foto frontali e consente il riconoscimento anche per questo tipo di foto, anche se è probabile che non sarà in grado di classificare tutte le foto di questo tipo.

### Note Finali
Le immagini automaticamente classificate con questo programma possono essere utilizzate, ad esempio, per la creazione di una rete neurale con Keras: utilizzando Python con la libreria Scikit-learn, queste immagini possono essere suddivise in training, validation o test dataset o cross-validate.

L'accesso ai database sopra menzionati è solitamente consentito al solo scopo di ricerca: per ulteriori informazioni, consultare i siti dei database. FEDC è stato creato da [Antonio Costantino Marceddu](https://www.linkedin.com/in/antonio-marceddu/) ricorrendo a Eclipse, con Java e con l'aggiunta del framework OpenCV.

<p align="center">
	<img src="https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier/blob/master/Resources/FEDC3.png"><br>
	Screenshot of the Facial Expression Database Classifier program.
</p>

## License
MIT License

Copyright (c) 2019 Antonio Costantino Marceddu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
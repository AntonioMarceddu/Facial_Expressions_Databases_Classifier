# Facial Expression Database Classifier

<p align="center">
    <img src="https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier/blob/master/Resources/FEDC1.png"><br>
	Screenshot of the Facial Expression Database Classifier program.
</p>

Description available in english and italian below. License is at the bottom of page.

## English

### Facial Expression Database Classifier
Facial Expression Database Classifier (FEDC) is a program able to automatically classify images of some of the most used databases, depicting posed human faces:
* Extended Cohn-Kanade Database (CK+);
* FACES Database;
* Facial Expression Recognition 2013 Database (FER2013);
* Japanese Female Facial Expression (JAFFE);
* Multimedia Understanding Group Database (MUG);
* Radboud Faces Database (RaFD);
* Static Facial Expressions in the Wild 2.0 (SFEW 2.0).

In practice, FEDC exploits the pre-classification implemented by databases creators. In addition to this, FEDC is also able to do several useful operations on images:
* change image format;
* conversion in grayscale color space;
* histogram equalization (normal or CLAHE);
* face detection to crop the images to face only;
* scaling of horizontal and vertical resolutions;
* subdivision in train, validation and test dataset;
* transformation of rectangular images into squared ones (SFEW 2.0 only).

This allows, for the people who use these databases, to reduce to the minimum the time necessary for their classification and to minimize the code for activities such as, for example, the training of a neural network.

### The FEDC interface
FEDC has a clean and essential user interface, consisting of four macroareas:
* on the left column it is possible to choose the database to be classified;
* in the other columns it is possible to select the operations to be performed on the photos: those available have already been mentioned previously;
* on the lower part of the window there are buttons for selecting input file, output folder, and for starting and canceling the classification;
* finally, above the buttons, there is the progress bar, that indicates the progression of the current operation.

### Informations about supported databases
Supported databases have features that make them different from one another:
* FER+ annotations will be applied by FEDC using the "Majority Voting" modality. For more information, please visit the [FER+ annotations Github page](https://github.com/microsoft/FERPlus);
* JAFFE and FER2013 databases only contain grayscale images;
* RaFD database also contains photos taken in profile: FEDC excels in the recognition of frontal photos and allows recognition to be made even for this type of photo, although it is likely that it will not be able to classify all this type of photos;
* SFEW 2.0 database has a natural subdivision between train, validation and test dataset, but only the first two have been classified: so, FEDC will optionally perform the subdivision only on these. However, the chosen transformations will be applied also to the test dataset, so as to give the user the freedom to use it;
* regarding the MUG database, FEDC only works with the manual annotated images;
* regarding the SFEW 2.0 database, FEDC only works with the aligned faces images.

### Updates
* 10/05/2019 - Version 1.0.0 released.
* 10/07/2019 - Version 2.0.0 released: subdivision function between train, validation, and test dataset has been added; many minor improvements to the code and its readability have also been made.
* 14/07/2019 - Version 3.0.0 released: support to FACES database has been added; the change image format function has been added; many corrections and improvements to the code and its readability have also been made.
* 26/07/2019 - Version 3.1.0 released: option for creating or not the validation folder during the subdivision added; small fixes to the code have also been made.
* 11/08/2019 - Version 4.0.0 released: support to SFEW 2.0 database has been added; support to FER+ annotations added; CLAHE option added; support to PGM and PPM formats added; option to transform database with rectangular images into squared ones added (currently only for SFEW 2.0); many corrections and improvements to the code and its readability have also been made.

### Final notes
The images automatically classified with FEDC can be used, for example, for the training of a neural network capable of recognizing facial expressions, through the use of Keras or similar frameworks. Since the code of the program is freely usable, it is possible to implement small changes to make the program work even in totally different contexts.

Access to the databases mentioned above is usually allowed only for research purposes: for more information, consult the sites related to the databases. FEDC was created by [Antonio Costantino Marceddu](https://www.linkedin.com/in/antonio-marceddu/) resorting to Eclipse, with Java and the addition of the OpenCV framework.

<p align="center">
    <img src="https://github.com/AntonioMarceddu/Facial_Expression_Database_Classifier/blob/master/Resources/FEDC2.png"><br>
	Screenshot of the Facial Expression Database Classifier program.
</p>

## Italiano

### Facial Expression Database Classifier
Facial Expression Database Classifier (FEDC) è un programma in grado di classificare automaticamente le immagini di alcuni tra i database utilizzati, raffiguranti volti umani in posa:
* Extended Cohn-Kanade Database (CK+);
* FACES Database;
* Facial Expression Recognition 2013 Database (FER2013);
* Japanese Female Facial Expression (JAFFE);
* Multimedia Understanding Group Database (MUG);
* Radboud Faces Database (RaFD);
* Static Facial Expressions in the Wild 2.0 (SFEW 2.0).

Nella pratica, FEDC sfrutta la pre-classificazione implementata dai creatori del database. Oltre a ciò, FEDC è anche in grado di eseguire diverse operazioni utili sulle immagini:
* cambio del formato dell'immagine;
* conversione dello spazio dei colori in scala di grigi;
* equalizzazione dell'istogramma (normale o CLAHE);
* ridimensionamento della risoluzione orizzontale e verticale;
* rilevamento del volto per il ritaglio delle immagini al solo volto;
* suddivisione in train, validation e test dataset;
* trasformazione delle immagini rettangolari in quadrate (solo per SFEW 2.0).

Ciò consente, per le persone che fanno uso di questi database, di ridurre al minimo il tempo necessario per la loro classificazione e di ridurre al minimo il codice per attività quali, ad esempio, l'addestramento di una rete neurale.

### L'interfaccia di FEDC
FEDC ha un'interfaccia utente pulita ed essenziale, composta da quattro macroaree:
* nella colonna di sinistra è possibile scegliere il database da classificare;
* nelle altre colonne è possibile selezionare le operazioni da eseguire sulle foto: quelle disponibili sono già state citate in precedenza;
* nella parte inferiore della finestra sono presenti i bottoni per selezionare il file di input, la cartella di output e per l'avvio e la cancellazione della classificazione;
* infine, sopra i bottoni, è presente la barra di avanzamento, che indica la progressione dell'operazione corrente.

### Informazioni riguardanti i database supportati
I database supportati hanno funzionalità che li rendono diversi l'uno dall'altro:
* le annotazioni FER+ saranno applicate da FEDC nella modalità "Majority Vote". Per ulteriori informazioni, visitare la [pagina Github delle annotazioni FER+](https://github.com/microsoft/FERPlus);
* i database JAFFE e FER2013 contengono solo immagini in scala di grigi;
* il database RaFD contiene anche delle foto scattate di profilo: FEDC eccelle nel riconoscimento delle foto frontali e consente il riconoscimento anche per questo tipo di foto, anche se è probabile che non sarà in grado di classificare tutte le foto di questo tipo;
* Il database SFEW 2.0 presenta una suddivisione naturale tra train, validation e test dataset, ma solamente i primi due sono stati classificati: FEDC quindi eseguirà opzionalmente la suddivisione solamente su questi. Tuttavia le trasformazioni scelte verranno applicate anche al test dataset, in modo tale da dare all'utente la libertà di usarlo;
* riguardo al database MUG, FEDC funziona solo con le immagini annotate manualmente;
* riguardo al database SFEW 2.0, FEDC funziona solo con le immagini facciali allineate.

### Aggiornamenti
* 10/05/2019 - Versione 1.0.0 rilasciata.
* 10/07/2019 - Versione 2.0.0 rilasciata: funzione di suddivisione tra train, validation e test dataset aggiunta; son stati fatti inoltre molti piccoli miglioramenti al codice e alla sua leggibilità.
* 14/07/2019 - Versione 3.0.0 rilasciata: è stato aggiunto il supporto al FACES database; è stata aggiunta la funzione di cambio del formato dell'immagine; sono state inoltre apportate molte correzioni e miglioramenti al codice e alla sua leggibilità.
* 26/07/2019 - Versione 3.1.0 rilasciata: opzionalità di creazione della cartella di validazione durante la suddivisione aggiunta; son state inoltre apportate piccole correzioni al codice.
* 11/08/2019 - Versione 4.0.0 rilasciata: è stato aggiunto il supporto al database SFEW 2.0;  è stato aggiunto il supporto alle annotazioni FER+; è stata aggiunta l'opzione CLAHE; supporto ai formati PGM e PPM aggiunto; aggiunta opzione per trasformare database con immagini rettangolari in quadrati (attualmente solo per SFEW 2.0); sono state inoltre apportate molte correzioni e miglioramenti al codice e alla sua leggibilità.

### Note finali
Le immagini automaticamente classificate con FEDC possono essere utilizzate, ad esempio, per l'addestramento di una rete neurale in grado di riconoscere le espressioni facciali, attraverso l'uso di Keras o di framework simili. Essendo il codice del programma liberamente fruibile, è possibile attuare delle piccole modifiche per far si che il programma possa funzionare anche in contesti totalmente differenti.

L'accesso ai database sopra menzionati è solitamente consentito al solo scopo di ricerca: per ulteriori informazioni, consultare i siti relativi ai database. FEDC è stato creato da [Antonio Costantino Marceddu](https://www.linkedin.com/in/antonio-marceddu/) ricorrendo a Eclipse, con Java e con l'aggiunta del framework OpenCV.

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
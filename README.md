# Facial Expressions Databases Classifier

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier)

[![Generic badge](https://img.shields.io/badge/Uses-OpenCV-blueviolet.svg)](https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier)
[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier/blob/master/LICENSE.txt)
[![Generic badge](https://img.shields.io/badge/Version-5.0.0-71bdef.svg)](https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier/graphs/commit-activity)
[![Ask Me Anything !](https://img.shields.io/badge/Ask%20me-anything-1abc9c.svg)](https://www.linkedin.com/in/antonio-marceddu/)

<p align="center">
    <img src="https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier/blob/master/resources/FEDC1.png"><br>
	Screenshot of the Facial Expressions Databases Classifier program.
</p>

Description available in english and italian below. License is at the bottom of page.

## English

### Facial Expressions Databases Classifier
Facial Expressions Databases Classifier (FEDC) is a program able to automatically classify images of some of the most used databases, depicting posed human faces:
* Extended Cohn-Kanade Database (CK+);
* FACES Database;
* Facial Expression Recognition 2013 Database (FER2013);
* Indian Movie Face Database (IMFDB);
* Japanese Female Facial Expression Database (JAFFE);
* Multimedia Understanding Group Database (MUG);
* NimStim Set Of Facial Expressions;
* Radboud Faces Database (RaFD);
* Real-world Affective Faces Database (RAF-DB);
* Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0).

In practice, FEDC exploits the pre-classification implemented by databases creators. In addition to this, FEDC is also able to do several useful operations on images:
* change image format;
* conversion in grayscale color space;
* histogram equalization (normal or CLAHE);
* face detection to crop the images to face only;
* scaling of horizontal and vertical resolutions;
* subdivision in train, validation and test dataset;
* transformation of rectangular images into square ones (can only be activated if the "Face Detection and Crop" option is not selected).

This allows, for the people who use these databases, to reduce to the minimum the time necessary for their classification and to minimize the code for activities such as, for example, the training of a neural network.

### Informations about the program
Starting from 2018, FEDC has been developed, and continues to be so, using Java 8 with the JavaFX GUI package as the programming language with the addition of the OpenCV library. In the lib folder you can find the needed libraries for Windows. If you are using a different OS, you need to download and link the latest version of OpenCV 3 to the code.

You can find an already compiled version for Windows [here](http://antoniomarceddu.altervista.org/en/fedc.htm).

### Informations about supported databases
Supported databases have features that make them different from each other. It is important to mention a few of them:
* FER+ annotations will be applied by FEDC using the "Majority Vote" mode. For more information, visit the [FER+ annotations Github page](https://github.com/microsoft/FERPlus);
* FER2013 and JAFFE databases only contain grayscale images;
* not all IMFDB database images can be classified, as for many of them the emotional label is absent. FEDC will catalog only those that have this label;
* the IMFDB database has images of different sizes. It is recommended to set the same height and width for the output image and to add a padding by selecting the appropriate option when required;
* in the case of MUG database, FEDC will only work with manually annotated images;
* NimStim Set Of Facial Expressions database also has the calm state and the classification of cases in which the mouth is open or closed: if desired, FEDC can also distinguish between these features;
* RaFD database also contains photos taken in profile: if you choose not to use the face cropping option, no problem will occur, but, if you select this option, it must be said that the Haar cascade classifier used for the recognition of profile faces is not as refined as that for the recognition of frontal faces and is not able to successfully classify all the photos;
* RAF-DB database contains both basic and compound emotions: FEDC can classify the aligned images version of both;
* in the case of SFEW 2.0 database, FEDC will only work with aligned face images. It also presents a natural subdivision between train, validation, and test dataset, but only the first two have been classified: FEDC will therefore optionally perform the subdivision only on these. However, the chosen transformations will also be applied to the test dataset, so as to give the user the freedom to use it.

### Other informations
Jasper project has many opened vulnerabilities which are not get fixed for a long time. Therefore, by default, OpenCV does not allow you to save images with the JPEG2000 format. This option can be enabled by setting the runtime option OPENCV_IO_ENABLE_JASPER to True. For more information, read [here](https://github.com/opencv/opencv/issues/14058).


### Updates
* 10/05/2019 - Version 1.0.0 released.
* 10/07/2019 - Version 2.0.0 released: subdivision function between train, validation, and test dataset has been added; many minor improvements to the code and its readability have also been made.
* 14/07/2019 - Version 3.0.0 released: support to FACES database has been added; the change image format function has been added; many corrections and improvements to the code and its readability have also been made.
* 26/07/2019 - Version 3.1.0 released: option for creating or not the validation folder during the subdivision added; small fixes to the code have also been made.
* 11/08/2019 - Version 4.0.0 released: support to SFEW 2.0 database has been added; support to FER+ annotations added; CLAHE option added; support to PGM and PPM formats added; option to transform database with rectangular images into squared ones added (currently only for SFEW 2.0); many corrections and improvements to the code and its readability have also been made.
* 10/09/2019 - Version 4.0.1 released: minor corrections to GUI and JAFFE classifier.
* 28/09/2019 - Version 4.0.2 released: option to transform images into square ones added for CK+, FACES and RaFD database.
* 05/10/2019 - Version 4.0.3 released: minor corrections to the code and the GUI, minor changes to the program name.
* 19/05/2020 - Version 5.0.0 released: support to IMFDB, NimStim Set Of Facial Expressions and RAF-DB have been added; log functions added; major corrections and improvements to the code and its readability have also been made.

### Final notes
The images automatically classified with FEDC can be used, for example, for the training of a neural network capable of recognizing facial expressions, through the use of Keras or similar frameworks. Since the code of the program is freely usable, it is possible to implement small changes to make the program work even in totally different contexts.

Access to the databases mentioned above is usually allowed only for research purposes: for more information, consult the sites related to the databases. FEDC was created by [Antonio Costantino Marceddu](https://www.linkedin.com/in/antonio-marceddu/): for any information, do not hesitate to [contact him](mailto:antonio.marceddu@polito.it).

<p align="center">
    <img src="https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier/blob/master/resources/FEDC2.png"><br>
	Screenshot of the Facial Expressions Databases Classifier program.
</p>

## Italiano

### Facial Expressions Databases Classifier
Facial Expressions Databases Classifier (FEDC) è un programma in grado di classificare automaticamente le immagini di alcuni tra i database utilizzati, raffiguranti volti umani in posa:
* Extended Cohn-Kanade Database (CK+);
* FACES Database;
* Facial Expression Recognition 2013 Database (FER2013);
* Indian Movie Face Database (IMFDB);
* Japanese Female Facial Expression Database (JAFFE);
* Multimedia Understanding Group Database (MUG);
* NimStim Set Of Facial Expressions;
* Radboud Faces Database (RaFD);
* Real-world Affective Faces Database (RAF-DB);
* Static Facial Expressions in the Wild 2.0 Database (SFEW 2.0).

Nella pratica, FEDC sfrutta la pre-classificazione implementata dai creatori del database. Oltre a ciò, FEDC è anche in grado di eseguire diverse operazioni utili sulle immagini:
* cambio del formato dell'immagine;
* conversione dello spazio dei colori in scala di grigi;
* equalizzazione dell'istogramma (normale o CLAHE);
* ridimensionamento della risoluzione orizzontale e verticale;
* rilevamento del volto per il ritaglio delle immagini al solo volto;
* suddivisione in train, validation e test dataset;
* trasformazione delle immagini rettangolari in quadrate (attivabile solamente se l'opzione "Face Detection and Crop" non è selezionata).

Ciò consente, per le persone che fanno uso di questi database, di ridurre al minimo il tempo necessario per la loro classificazione e di ridurre al minimo il codice per attività quali, ad esempio, l'addestramento di una rete neurale.

### Informazioni riguardanti il programma
A partire dal 2018, FEDC è stato sviluppato, e continua ad esserlo, utilizzando Java 8 con il pacchetto GUI JavaFX come linguaggio di programmazione con l'aggiunta della libreria OpenCV. Nella cartella lib si possono trovare le librerie necessarie per Windows. Se si utilizza un sistema operativo diverso, è necessario scaricare e collegare l'ultima versione di OpenCV 3 al codice.

Puoi trovare una versione già compilata per Windows [qui](http://antoniomarceddu.altervista.org/it/fedc.htm).

### Informazioni riguardanti i database supportati
I database supportati hanno caratteristiche che li rendono diversi l'uno dall'altro. E’ importante citarne alcune di esse:
* le annotazioni FER+ saranno applicate da FEDC nella modalità "Majority Vote". Per ulteriori informazioni, visitare la [pagina Github delle annotazioni FER+](https://github.com/microsoft/FERPlus);
* il FER2013 e il JAFFE database contengono solo immagini in scala di grigi;
* non tutte le immagini dell'IMFDB database possono essere classificate, in quanto per molte di esse l'etichetta emozionale è assente. FEDC catalogherà solo quelle che sono provviste di tale etichetta;
* il database IMFDB ha immagini di dimensioni diverse. Si consiglia di impostare la stessa altezza e larghezza per le immagini di output e di aggiungere un'imbottitura selezionando l'opzione appropriata quando richiesto;
* nel caso del MUG database, FEDC funzionerà solo con le immagini annotate manualmente;
* il NimStim Set Of Facial Expressions database ha anche lo stato di calma e la classificazione dei casi in cui la bocca è aperta o chiusa: se lo si desidera, FEDC può anche distinguere tra queste caratteristiche;
* il RaFD database contiene anche delle foto scattate di profilo: se si sceglie di non usare l’opzione di ritaglio del volto non avverrà alcun problema, ma, nel caso in cui si selezioni questa opzione, occorre dire che la cascata di classificatori di Haar utilizzata per il riconoscimento dei volti di profilo non è raffinata quanto quella per le foto frontali e non è in grado di classificare con successo tutte le foto;
* il RAF-DB database contiene sia emozioni di base che composte: FEDC puó classificare la versione con le immagini allineate di entrambe;
* nel caso dello SFEW 2.0 database, FEDC funzionerà solo con le immagini allineate dei volti. Esso presenta inoltre una suddivisione naturale tra train, validation e test dataset, ma solamente i primi due sono stati classificati: FEDC quindi eseguirà opzionalmente la suddivisione solamente su questi. Tuttavia le trasformazioni scelte verranno applicate anche al test dataset, in modo tale da dare all'utente la libertà di usarlo.

### Altre informazioni
Il progetto Jasper ha molte vulnerabilità aperte che non vengono risolte da molto tempo. Pertanto, per impostazione predefinita, OpenCV non consente di salvare immagini con il formato JPEG2000. Questa opzione può essere abilitata impostando l'opzione runtime OPENCV_IO_ENABLE_JASPER come True. Per ulteriori informazioni, leggi [qui](https://github.com/opencv/opencv/issues/14058).

### Aggiornamenti
* 10/05/2019 - Versione 1.0.0 rilasciata.
* 10/07/2019 - Versione 2.0.0 rilasciata: funzione di suddivisione tra train, validation e test dataset aggiunta; son stati fatti inoltre molti piccoli miglioramenti al codice e alla sua leggibilità.
* 14/07/2019 - Versione 3.0.0 rilasciata: è stato aggiunto il supporto al FACES database; è stata aggiunta la funzione di cambio del formato dell'immagine; sono state inoltre apportate molte correzioni e miglioramenti al codice e alla sua leggibilità.
* 26/07/2019 - Versione 3.1.0 rilasciata: opzionalità di creazione della cartella di validazione durante la suddivisione aggiunta; son state inoltre apportate piccole correzioni al codice.
* 11/08/2019 - Versione 4.0.0 rilasciata: è stato aggiunto il supporto al database SFEW 2.0;  è stato aggiunto il supporto alle annotazioni FER+; è stata aggiunta l'opzione CLAHE; supporto ai formati PGM e PPM aggiunto; aggiunta opzione per trasformare database con immagini rettangolari in quadrati (attualmente solo per SFEW 2.0); sono state inoltre apportate molte correzioni e miglioramenti al codice e alla sua leggibilità.
* 10/09/2019 - Versione 4.0.1 rilasciata: correzioni minori alla GUI e al classificatore JAFFE.
* 28/09/2019 - Versione 4.0.2 rilasciata: opzione per trasformare le immagini rettangolari in quadrate aggiunta per i database CK+, FACES e RaFD.
* 05/10/2019 - Versione 4.0.3 rilasciata: piccole correzioni al codice e alla GUI, piccole modifiche al nome del programma.
* 19/05/2020 - versione 5.0.0 rilasciata: è stato aggiunto il supporto per l'IMFDB, per il NimStim Set Of Facial Expressions e per il RAF-DB; funzioni di log aggiunte; sono state inoltre apportate importanti correzioni e miglioramenti al codice e alla sua leggibilità.

### Note finali
Le immagini automaticamente classificate con FEDC possono essere utilizzate, ad esempio, per l'addestramento di una rete neurale in grado di riconoscere le espressioni facciali, attraverso l'uso di Keras o di framework simili. Essendo il codice del programma liberamente fruibile, è possibile attuare delle piccole modifiche per far si che il programma possa funzionare anche in contesti totalmente differenti.

L'accesso ai database sopra menzionati è solitamente consentito al solo scopo di ricerca: per ulteriori informazioni, consultare i siti relativi ai database. FEDC è stato creato da [Antonio Costantino Marceddu](https://www.linkedin.com/in/antonio-marceddu/): per qualsiasi informazione, non esitate a [contattarlo](mailto:antonio.marceddu@polito.it).

<p align="center">
	<img src="https://github.com/AntonioMarceddu/Facial_Expressions_Databases_Classifier/blob/master/resources/FEDC3.png"><br>
	Screenshot of the Facial Expressions Databases Classifier program.
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
package projet.tutore.TourelleAndroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.lang.Math;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

public class MainActivity extends Activity implements SensorEventListener {
	public Button bouton_connexion = null; // variable permettant l'utilisation du bouton
	public Button bouton_calibrage = null; // variable permettant l'utilisation du bouton
	public Button bouton_demarrer_arreter = null; // variable permettant l'utilisation du bouton
	public Button bouton_deconnexion = null; // variable permettant l'utilisation du bouton
	public Button bouton_aide = null; // variable permettant l'utilisation du bouton
	
	public EditText adresse_mac_bras = null; // variable permettant l'utilisation du champ de saisi
	
	public TextView afficher_valeur_X = null; // variable permettant l'utilisation du texte
	public TextView afficher_valeur_Y = null; // variable permettant l'utilisation du texte
	public TextView afficher_valeur_Z = null; // variable permettant l'utilisation du texte
	public TextView message_axeZ_manuel = null; // variable permettant l'utilisation du texte
	public TextView etat_bluetooth = null; // variable permettant l'utilisation du texte
	public TextView etat_connexion = null; // variable permettant l'utilisation du texte
	
	public ProgressBar progressBar_X = null; // variable permettant l'utilisation de la barre de progression
	public ProgressBar progressBar_Y = null; // variable permettant l'utilisation de la barre de progression
	public ProgressBar progressBar_Z = null; // variable permettant l'utilisation de la barre de progression
	
	public SeekBar seekBar_Z = null; // variable permettant l'utilisation de la barre de déplacement latéral
	
	public ToggleButton gerer_bluetooth = null; // variable permettant l'utilisation de l'interrupteur
	
	// ------------------------------------------------------------------------
	
	public static final int VERT = 0xFF99CC00; // code couleur de la couleur verte
	public static final int ROUGE = 0xFFFF4444; // code couleur de la couleur rouge
	public static final int ORANGE = 0xFFFF8C00; // code couleur de la couleur orange
	public static final int NOIR = 0xFF000000; // code couleur de la couleur noire
	
	public static final int maxProgressBar = 179; // définition de la valeur maximale que peut atteindre la barre de progression

	public double calibrage_en_x = 0; // variable contenant la valeur en x qui va être prise en compte par l'algorithme pour calculer la position
	public double calibrage_en_y = 0; // variable contenant la valeur en y qui va être prise en compte par l'algorithme pour calculer la position
	public double calibrage_en_z = 0; // variable contenant la valeur en z qui va être prise en compte par l'algorithme pour calculer la position
	public double valeur_x_vraie = 0; // variable contenant la valeur de x mise à jour en permanence (n'est jamais modifiée)
	public double valeur_y_vraie = 0; // variable contenant la valeur de y mise à jour en permanence (n'est jamais modifiée)
	public double valeur_z_vraie = 0; // variable contenant la valeur de z mise à jour en permanence (n'est jamais modifiée)

	public double x = 0; // variable contenant la valeur de x
	public double y = 0; // variable contenant la valeur de y
	public double z = 0; // variable contenant la valeur de z
	
	public boolean presence_capteur_orientation = false; // variable qui vaut "vrai" si l'application détecte la présence du capteur d'orientation
	public boolean bluetooth_est_actif = false; // variable qui vaut "vrai" si le bluetooth est activé sur le périphérique
	public boolean calibrage_fait = false; // variable qui vaut "vrai" si le calibrage du périphérique est fait
	public boolean connexion_etablie = false; // variable qui vaut "vrai" si la connexion entre le périphérique est établie
	public boolean initialisation_connexion_faite = false; // variable qui vaut "vrai" si l'initialisation est faite
	public boolean connexion_en_cours = false; // variable qui vaut "vrai" lorsque la connexion entre les deux éléments est en cours
	public boolean demarrer_envoi_trame = false; // variable qui vaut "vrai" si l'utilisateur à demandé le démarrage des mouvements
	public boolean envoi_trame_reussi = false; // variable qui vaut "vrai" si l'envoi d'une trame de mouvements à réussi
	
	public String retour_initialisation_connexion; // variable contenant le message retourné par la fonction appellée correspondante (initialisation_connexion)
	public String retour_connexion_etablie; // variable contenant le message retourné par la fonction appellée correspondante (connexionBluetooth)
	public String retour_initialisation_communication; // variable contenant le message retourné par la fonction appellée correspondante (initialisation_communication)
	
	public BluetoothAdapter bluetoothAdapter; // Voir la documentation du Bluetooth sous Android
	public BluetoothDevice  bluetoothDevice;  
	public BluetoothSocket  bluetoothSocket;     
	public UUID identifiant_module_bluetooth = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");              
	public String DEVICE_ADRESSE = "00:0B:CE:08:4D:AB"; // Adresse MAC qui sera modifiée, mais utilisée par défaut (celle du bras que nous avons utilisé)
	
	public OutputStream sendStream;

	public int valeur_z_affichee = 0; // Si l'application est en mode manuel, elle prendra en compte l'affichage de la valeur en Z
	public int i = 0; // variable modifiable
	public int nombre_de_trames_envoyees = 0; // contient le nombre d'informations envoyées via le Bluetooth
	
	@SuppressWarnings("deprecation") // enlève les "Warning" sur les éléments obsolètes
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // création de la vue
		setContentView(R.layout.activity_main);
		
		SensorManager capteurs = (SensorManager) getSystemService(SENSOR_SERVICE); // Initialisation de la communication avec tous les capteurs
		List<Sensor> sensors = capteurs.getSensorList(Sensor.TYPE_ALL);
		
		for (Sensor sensor : sensors) { // fonction permettant de vérifier la présence du capteur d'orientation sur le périphérique
			verifierPresenceCapteurOrientation(sensor.getType()); // si le capteur d'orientation est trouvé, met à "vrai" la variable "presence_capteur_orientation"
		}
		
		Sensor accelerometre = capteurs.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // initialisation du capteur d'accéléromètre
		capteurs.registerListener(this, accelerometre, SensorManager.SENSOR_DELAY_FASTEST); // réglage du délais de mise à jour des informations du capteur d'accéléromètre
		
		seekBar_Z = (SeekBar) findViewById(R.id.seekBar_Z); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		message_axeZ_manuel = (TextView) findViewById(R.id.mode_manuel); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		
		if(presence_capteur_orientation == true) { // si le capteur d'orientation existe
			Sensor orientation = capteurs.getDefaultSensor(Sensor.TYPE_ORIENTATION); // initialisation du capteur d'orientation
			capteurs.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST); // réglage du délais de mise à jour des informations du capteur d'orientation
			seekBar_Z.setVisibility(0x04); // permet de cacher la barre de déplacement latérale
			message_axeZ_manuel.setVisibility(0x04); // permet de cacher le texte du mode manuel
		}
		else {
			seekBar_Z.setMax(100); // initialise le maximum de la barre de déplacement latéral
			seekBar_Z.setProgress(50); // initialise la valeur par défaut de la barre de déplacement
		}
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		bouton_connexion =        (Button) findViewById(R.id.bouton_connexion); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		bouton_calibrage =        (Button) findViewById(R.id.bouton_calibrage); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		bouton_demarrer_arreter = (Button) findViewById(R.id.bouton_demarrer_arreter); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		bouton_deconnexion =      (Button) findViewById(R.id.bouton_deconnexion); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		bouton_aide =             (Button) findViewById(R.id.bouton_aide); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		
		afficher_valeur_X = (TextView) findViewById(R.id.afficher_valeur_X); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		afficher_valeur_Y = (TextView) findViewById(R.id.afficher_valeur_Y); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		afficher_valeur_Z = (TextView) findViewById(R.id.afficher_valeur_Z); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		etat_bluetooth =    (TextView) findViewById(R.id.etat_bluetooth); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		etat_connexion =    (TextView) findViewById(R.id.etat_connexion); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		
		// -------------------
		
		adresse_mac_bras =    (EditText) findViewById(R.id.adresse_mac_bras); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		
		// -------------------
		
		gerer_bluetooth = (ToggleButton) findViewById(R.id.gerer_bluetooth); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		
		// -------------------
		
		progressBar_X = (ProgressBar) findViewById(R.id.progressBar_X); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		progressBar_Y = (ProgressBar) findViewById(R.id.progressBar_Y); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		progressBar_Z = (ProgressBar) findViewById(R.id.progressBar_Z); // variable contenant toutes les propriétés modifiables de l'élément du même sur la vue
		
		progressBar_X.setMax(100); // initialise la valeur maximale de la barre de progression pour afficher graphiquement la valeur de x
		progressBar_Y.setMax(100); // initialise la valeur maximale de la barre de progression pour afficher graphiquement la valeur de y
		progressBar_Z.setMax(100); // initialise la valeur maximale de la barre de progression pour afficher graphiquement la valeur de z
		
		// ---------------
		
		bouton_connexion.setOnClickListener(Connexion); // exécution d'une séquence pour l'évènement "Clic"
		bouton_calibrage.setOnClickListener(Calibrage); // exécution d'une séquence pour l'évènement "Clic"
		bouton_demarrer_arreter.setOnClickListener(Demarrer_arreter); // exécution d'une séquence pour l'évènement "Clic"
		bouton_deconnexion.setOnClickListener(Deconnexion); // exécution d'une séquence pour l'évènement "Clic"
		seekBar_Z.setOnSeekBarChangeListener(Changement_de_Z); // exécution d'une séquence pour l'évènement "Changement de valeur"
		bouton_aide.setOnClickListener(Aide); // exécution d'une séquence pour l'évènement "Clic"
		gerer_bluetooth.setOnCheckedChangeListener(Gerer_bluetooth); // exécution d'une séquence pour l'évènement "Changement de position"
		
		testBluetoothActive(); // test directement si le bluetooth est activé
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// Ligne rajoutée, sinon le logiciel nous indique une erreur de compilation
	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) { // Séquence exécutée si le périphérique à changer de position
		
		testBluetoothActive(); // fonction permettant de vérifier si le bluetooth est activé ou non
		miseAjourProgressBar(); // mise à jour de la barre de progression de chaque axe

		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { // si l'axe x ou y est concerné dans le changement de position
			if(calibrage_fait == true) { // si l'utilisateur à calibré
			
				x = Math.round(event.values[0] * 100);	// récupération de x
				y = Math.round(event.values[1] * 100);	// récupération de y
				
				valeur_x_vraie = x;	// Récupération de la valeur "vraie" pour un autre calibrage				
				x = x - calibrage_en_x; // calibrage de la future valeur à envoyer
				
				valeur_y_vraie = y;							
				y = y - calibrage_en_y;
				
				if(x > 800) { // blocage de la valeur
					x = 800;
				}
				else if(x < (-800)) {
					x = -800;
				}
				
				if(y > 800) {
					y = 800;
				}
				else if(y < (-800)) {
					y = -800;
				}
				
				x = (double) (map((int) x, -800, 800, 179, 77)); // convertie la valeur en x pour la recentrer dans l'intervalle
				y = (double) (map((int) y, -800, 800, 179, 77));
		        
				afficher_valeur_X.setText(Double.toString(x));	// affichage des valeurs
				afficher_valeur_Y.setText(Double.toString(y));
			}
			else { // si le calibrage n'a pas encore était fait
				valeur_x_vraie = Math.round(event.values[0] * 100); // mise à jour en temps réél de la valeur qui permettra le calibrage plus tard
				valeur_y_vraie = Math.round(event.values[1] * 100); // mise à jour en temps réél de la valeur qui permettra le calibrage plus tard
			}
		}
		
		if(presence_capteur_orientation == true) { // si le capteur d'orientation existe
			if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) { // si c'est le capteur d'orientation qui a détecté un mouvement
				if(calibrage_fait == true) { // si l'utilisateur à calibré
					
					z = Math.round(event.values[0]); // récupération de la valeur en z
					
					z = (204 - (z - 51));
					
					valeur_z_vraie = z;			
					z = z - calibrage_en_z;
					
					if(z < 0)
						z += 360; // décallage de la valeur : z = z + 360
				
					if(z > 90 && z < 180)
						z = 90;	// blocage à 90°
					
					else if(z < 270 && z > 180)
						z = 270; // blocage à -90°
					
					z += 90; // z = z + 90
					
					if(z >= 360)
						z -= 360; // repositionnement si > 360° : z = z - 360
					
					z = Math.round(z * 0.85) + 51;
					
					afficher_valeur_Z.setText(Double.toString(z));		
				}
				else {
					valeur_z_vraie = Math.round(event.values[0]);
					valeur_z_vraie = (204 - (valeur_z_vraie - 51));
				}
			}
		}
		else { // si 
			if(calibrage_fait == true) {
				afficher_valeur_Z.setText(Integer.toString(valeur_z_affichee) + ".0");		// Ecriture de la valeur
			}
		}
			
		// Ligne de code permettant d'envoyer la trame
		// si on est bien connecté, que le calibrage est
		// fait, et que l'on a démarrer l'envoi des trames
		if(bluetooth_est_actif && initialisation_connexion_faite &&
				connexion_etablie && connexion_en_cours &&
				calibrage_fait && demarrer_envoi_trame) {
			envoyerTrame();
			nombre_de_trames_envoyees++;
		}
	}

	public OnClickListener Connexion = new OnClickListener() { // séquence exécutée si l'application détecte un clic sur le bouton "Connexion"
		@Override
		public void onClick(View v) {
			if(bluetooth_est_actif == true) { // si le bluetooth est activé
				
				retour_initialisation_connexion = initialiserConnexionBluetooth(); // appel la fonction qui crée le socket
				
				if (initialisation_connexion_faite == true) { // Si l'initialisation de la connexion s'est correctement effectuée
					
					etat_connexion.setText(retour_initialisation_connexion); // affichage du message de confirmation
					etat_connexion.setTextColor(VERT);
					
					retour_connexion_etablie = connexionBluetooth(); // appel la fonction qui connecte l'appareil au bras
					
					if(connexion_etablie == true) { // si la communication est établie
						
						etat_connexion.setText(retour_connexion_etablie); // affichage du message de confirmation
						etat_connexion.setTextColor(VERT);
						
						retour_initialisation_communication = initialiserCommunication(); // initialise la communication
						
						if(connexion_en_cours == true) { // si l'initialisation de la communication s'est correctement effectuée
							
							etat_connexion.setText(retour_initialisation_communication); // affichage du message de confirmation
							etat_connexion.setTextColor(VERT);
							
							// déblocage de l'interface pour que l'utilisateur puisse passer à l'étape suivante
							
							bouton_connexion.setEnabled(false);
							bouton_calibrage.setEnabled(true);
							bouton_demarrer_arreter.setEnabled(false);
							bouton_deconnexion.setEnabled(true);
							if(presence_capteur_orientation == false) {
								seekBar_Z.setEnabled(true);
							}
							adresse_mac_bras.setEnabled(false);
						}
						else { // sinon on affiche le message d'erreur retourné par la fonction
							etat_connexion.setText(retour_initialisation_communication);
							etat_connexion.setTextColor(ROUGE);
						}
					}
					else { // sinon on affiche le message d'erreur retourné par la fonction
						etat_connexion.setText(retour_connexion_etablie);
						etat_connexion.setTextColor(ROUGE);
					}
				}
				else { // sinon on affiche le message d'erreur retourné par la fonction
					etat_connexion.setText(retour_initialisation_connexion);
					etat_connexion.setTextColor(ROUGE);
				}
			}
		}
	};
	
	public OnClickListener Calibrage = new OnClickListener() { // séquence exécutée si l'application détecte un clic sur le bouton "Calibrage"s
		@Override
		public void onClick(View v) {
			calibrage_en_x = valeur_x_vraie; // récupèration de la valeur mise à jour en temps réel avant le calibrage
			calibrage_en_y = valeur_y_vraie; // récupèration de la valeur mise à jour en temps réel avant le calibrage
			if(presence_capteur_orientation == true) { // si le capteur d'orientation existe
				calibrage_en_z = valeur_z_vraie;  // récupèration de la valeur mise à jour en temps réel avant le calibrage
			}
			else {
				valeur_z_affichee = map(seekBar_Z.getProgress(), 0, 100, 51, 204); // valeur de l'axe z affichée
				z                 = map(seekBar_Z.getProgress(), 0, 100, 204, 51); // valeur de l'axe z transmise
			}
			
			calibrage_fait = true; // le calibrage à était effectué
			
			bouton_demarrer_arreter.setEnabled(true); // déblocage de l'interface pour que l'utilisateur passe à l'étape suivante
		}
	};
	
	public OnClickListener Demarrer_arreter = new OnClickListener() { // séquence exécutée si l'application détecte un clic sur le bouton "Démarrer mouvements"
		@Override
		public void onClick(View v) {
			if(demarrer_envoi_trame == false) { // si les mouvements  n'ont pas encore commencé
				bouton_demarrer_arreter.setText("3) Arrêter mouvements"); // modification du texte du bouton
				demarrer_envoi_trame = true; // demarrage des mouvements du bras
				etat_connexion.setText("Bras en mouvement");
			} else {
				bouton_demarrer_arreter.setText("3) Démarrer mouvements");
				demarrer_envoi_trame = false;
				etat_connexion.setText("Connexion prête");
			}
		}
	};
	
	public OnClickListener Deconnexion = new OnClickListener() { // séquence exécutée si l'application détecte un clic sur le bouton "Déconnexion"
		public void onClick(View v) {
			try {
				if(envoyerTrame(0x80, 0x80, 0x80) == false) { // remise du bras dans sa position par défaut
					afficherAlerte("Impossible de remettre le bras robotisé dans position d'origine");
				}
				
				bluetoothSocket.close(); // fermeture de la communication bluetooth
				remiseAzero();
	
			} catch (IOException e) {
				etat_bluetooth.setText("Problème de déconnexion");
				etat_bluetooth.setTextColor(ROUGE);
			}
		}
	};
	
	public OnSeekBarChangeListener Changement_de_Z = new OnSeekBarChangeListener() { // séquence exécutée si l'application détecte un changement sur la barre de déplacement latéral
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) { }
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) { }
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			valeur_z_affichee = map(seekBar_Z.getProgress(), 0, 100, 51, 204); // valeur de l'axe z affichée
			z                 = map(seekBar_Z.getProgress(), 0, 100, 204, 51); // valeur de l'axe z transmise
			miseAjourProgressBar();
			
			if(bluetooth_est_actif && initialisation_connexion_faite &&
					connexion_etablie && connexion_en_cours &&
					calibrage_fait && demarrer_envoi_trame) {
				envoyerTrame((int) x, (int) y, (int) z);
				nombre_de_trames_envoyees++;
			}
		}
	};
	
	public OnClickListener Aide = new OnClickListener() { // séquence exécutée si l'application détecte un clic sur le bouton "Aide"
		public void onClick(View v) {
			if(bluetoothSocket != null) {
				try {
					bluetoothSocket.close();
				} catch(IOException e) { }
			}
			remiseAzero();
			Intent intent = new Intent(MainActivity.this, Page_aide.class); // création de la vue pour afficher l'aide
			startActivity(intent);				
		}
	};
	

	public OnCheckedChangeListener Gerer_bluetooth = new OnCheckedChangeListener() { // séquence exécutée si l'application détecte un clic sur le bouton de gestion du bluetooth
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // l'utilisateur à demandé à inverser l'état du bluetooth sur son périphérique
			if(isChecked) {
				bluetoothAdapter.enable(); // activation du bluetooth sur le périphérique
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) { }
			} else {
            	bluetoothAdapter.disable(); // désactivation du bluetooth sur le périphérique
            }
		}
	};
	
	// ###############################################################################
	// #                                FONCTIONS									 #
	// ###############################################################################
	
	@SuppressWarnings("deprecation")
	public void verifierPresenceCapteurOrientation(int type) {
		switch (type) {
			case Sensor.TYPE_ORIENTATION:
				presence_capteur_orientation = true;
		}
	}
	
	public void testBluetoothActive() {  // Vérifie si le bluetooth est activé ou non 
		if(bluetoothAdapter.isEnabled() == true) {
			if(connexion_en_cours == false) {
				bluetooth_est_actif = true;
				bouton_connexion.setEnabled(true);
			
				etat_bluetooth.setText("Bluetooth activé");
				etat_bluetooth.setTextColor(VERT);
				
				gerer_bluetooth.setChecked(true);
				gerer_bluetooth.setBackgroundDrawable(getResources().getDrawable(R.drawable.icone_bluetooth_on));
			}
		}
		else {
			bluetooth_est_actif = false;
			gerer_bluetooth.setChecked(false);
			gerer_bluetooth.setBackgroundDrawable(getResources().getDrawable(R.drawable.icone_bluetooth_off));
			
			remiseAzero();
			
			etat_bluetooth.setText("Bluetooth désactivé");
			etat_bluetooth.setTextColor(ROUGE);
		}
    }
	
	public void miseAjourProgressBar() {
		if(calibrage_fait == true) {		
			progressBar_X.setProgress(map((int) x, 77, 179, 0, 100));
			progressBar_Y.setProgress(map((int) y, 77, 179, 0, 100));
			if(presence_capteur_orientation == true) {
				progressBar_Z.setProgress(map((int) z, 51, 204, 0, 100));
			}
			else {
				progressBar_Z.setProgress(map(valeur_z_affichee, 51, 204, 0, 100));
			}
		}
	}
	
	public String initialiserConnexionBluetooth() { // Création du socket grâce à l'adresse   
		try {
			bluetoothDevice = bluetoothAdapter.getRemoteDevice(adresse_mac_bras.getText().toString());
			bluetoothAdapter.cancelDiscovery();  // annule toute recherche de périphérique
			
			if (bluetoothDevice != null) {         
				bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(identifiant_module_bluetooth);
				initialisation_connexion_faite = true;
				return("Initialisation faite");
			}
			else {
				initialisation_connexion_faite = false;
				return("Echec de l'initialisation");
			}
			
			
		} catch(IOException closeException) {
			initialisation_connexion_faite = false;
			return("Echec de l'initialisation");
		}                 
	}
	
	public String connexionBluetooth() { // Connexion au bras grâce au socket créé précédement
		try {
			bluetoothSocket.connect();
			connexion_etablie = true;
			return("Connexion réussie");
			
		 } catch (IOException connectException) {
			try {
				bluetoothSocket.close();
				connexion_etablie = false;
				return("Impossible de se connecter");
			}
			catch (IOException closeException) {
				connexion_etablie = false;
				return("Echec, fermeture connexion");
			}
		}
	}
	
	public String initialiserCommunication() { // Initie les paramètres de communication -> OutputStream=flux sortant      
		try {      
			sendStream = bluetoothSocket.getOutputStream();
			
			connexion_en_cours = true;
			return("Connexion prête");
			
		} catch(IOException e){
			connexion_en_cours = false;
			return("Echec de la communication");
		}           
	}
	
	public boolean envoyerTrame() { // Envoie des trames
		int valeur_X = (int) (x);
		int valeur_Y = (int) (y);
		int valeur_Z = (int) (z);
		
		envoi_trame_reussi = envoyerTrame(valeur_X, valeur_Y, valeur_Z);
		return(envoi_trame_reussi);
	}
	
	public int calculChecksum(int x, int y, int z) { // Calcul du checksum
		return ((x + y + z + 218) % 256); // 218 = 66 + 73 + 79 = "B" + "I" + "O" = Constante
	}
		
	public void remiseAzero() {
		adresse_mac_bras.setEnabled(true);
		bouton_connexion.setEnabled(false);
		
		bouton_calibrage.setEnabled(false);
		
		afficher_valeur_X.setText("-");
		afficher_valeur_Y.setText("-");
		afficher_valeur_Z.setText("-");
		
		progressBar_X.setProgress(0);
		progressBar_Y.setProgress(0);
		progressBar_Z.setProgress(0);
		
		if(presence_capteur_orientation == true) {
			seekBar_Z.setProgress(50);
		}
		
		bouton_demarrer_arreter.setEnabled(false);
		bouton_demarrer_arreter.setText("3) Démarrer mouvements");
		
		bouton_deconnexion.setEnabled(false);
		seekBar_Z.setEnabled(false);

		calibrage_fait = false;
		connexion_etablie = false;
		initialisation_connexion_faite = false;
		connexion_en_cours = false;
		demarrer_envoi_trame = false;
		envoi_trame_reussi = false;
		
		etat_connexion.setText("---");
		etat_connexion.setTextColor(NOIR);
		
		x = 0;
		y = 0;
		z = 0;
	}
	
	public boolean envoyerTrame(int valeur_X, int valeur_Y, int valeur_Z) {
		byte[] writeBuf = new byte[8]; //  Création du buffer avec les valeurs des capteurs
		writeBuf[0] = 0x42; // caractère "B"
		writeBuf[1] = 0x49; // caractère  "I"
		writeBuf[2] = 0x4F; // caractère  "O"
		writeBuf[3] = (byte) (valeur_X); // valeur en x
		writeBuf[4] = (byte) (valeur_Y); // valeur en y
		writeBuf[5] = (byte) (valeur_Z); // valeur en z
		writeBuf[6] = (byte) (calculChecksum(valeur_X, valeur_Y, valeur_Z)); // valeur du checksum
		writeBuf[7] = 0x0D; // fin de trame "\r"
		
		try {
    	   sendStream.write(writeBuf); // envoie de la trame
    	   nombre_de_trames_envoyees++;
    	   return(true);
    	} catch (IOException closeException) {
    		nombre_de_trames_envoyees++;
    		/*etat_connexion.setText("(" + nombre_de_trames_envoyees + ") Impossible de bouger le bras");
			etat_connexion.setTextColor(ROUGE);*/
    		afficherAlerte("Connexion avortée par le bras robotisé");
    		remiseAzero();
			return(false);
    	}
	}
	
	public int map(int valeur, int in_min, int in_max, int out_min, int out_max) {
		/*
		 * Ré-étalonne un nombre d'une fourchette de valeur vers une autre fourchette.
		 * Ainsi, une valeur basse source sera étalonnée en une valeur basse de destination,
		 * une valeur haute source sera étalonnée en une valeur haute de destination,
		 * une valeur entre les deux valeurs source sera étalonnée en une valeur entre les
		 * deux valeurs destinations, en respectant la proportionnalité. Cette fonction est
		 * très utile pour effectuer des changements d'échelle automatiques.
		 *
		 * Dans le cas où la valeur à étalonner est x
		 * nouvelle valeur = (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min */
		
		return((valeur - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
	}
	
	public void afficherAlerte(String message) { // Affichage temporaire de message sur le périphérique
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); // affiche une petite notification sur l'écran
    }
}
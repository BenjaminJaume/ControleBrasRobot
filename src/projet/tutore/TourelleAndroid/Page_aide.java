package projet.tutore.TourelleAndroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Page_aide extends Activity {

	public TextView afficher_aide = null;
	public TextView afficher_page = null;
	public Button bouton_video = null;
	public Button bouton_precedent = null;
	public Button bouton_suivant = null;
	
	int numero_page = 1;
	public static final int NOMBRE_DE_PAGE = 3;
	String page = null;
	int identifiant_page;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_aide);
		
		afficher_aide = (TextView) findViewById(R.id.afficher_aide);
		afficher_page = (TextView) findViewById(R.id.afficher_page);
		bouton_video = (Button) findViewById(R.id.bouton_video);
		bouton_precedent = (Button) findViewById(R.id.bouton_precedent);
		bouton_suivant = (Button) findViewById(R.id.bouton_suivant);
		
		bouton_video.setOnClickListener(Lancer_video);
		bouton_precedent.setOnClickListener(Precedent);
		bouton_suivant.setOnClickListener(Suivant);
		
		mettreAjourPage(1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.page_aide, menu);
		return true;
	}
	
	public OnClickListener Lancer_video = new OnClickListener() {
		public void onClick(View v) {
			//Page_aide.this.finish(); // Pour tuer la vue (et implicitement, revenir en arrière)
			
		    try {
		         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:WWNwB2Ol0IY"));
		         startActivity(intent);                 
	         } catch(ActivityNotFoundException e) {
	             Intent intent = new Intent(Intent.ACTION_VIEW, 
	             Uri.parse("https://www.youtube.com/watch?v=WWNwB2Ol0IY"));
	             startActivity(intent);
	         }
		}
	};
	
	public OnClickListener Precedent = new OnClickListener() {
		public void onClick(View v) {
			numero_page--;
			
			if(numero_page <= 1) {
				numero_page = 1;
				page = "texte_aide_" + numero_page;
				identifiant_page = getResources().getIdentifier(page, "string", getPackageName());
				afficher_aide.setText(identifiant_page);
				bouton_precedent.setEnabled(false);
				bouton_precedent.setBackground(getResources().getDrawable(R.drawable.fleche_gauche_inactive));
				bouton_suivant.setEnabled(true);
				bouton_suivant.setBackground(getResources().getDrawable(R.drawable.fleche_droite_active));
				
				mettreAjourPage(numero_page);
			}
			else {
				page = "texte_aide_" + numero_page;
				identifiant_page = getResources().getIdentifier(page, "string", getPackageName());
				afficher_aide.setText(identifiant_page);
				bouton_suivant.setEnabled(true);
				bouton_precedent.setBackground(getResources().getDrawable(R.drawable.fleche_gauche_active));
				bouton_suivant.setBackground(getResources().getDrawable(R.drawable.fleche_droite_active));
				
				mettreAjourPage(numero_page);
			}
		}
	};
	
	public OnClickListener Suivant = new OnClickListener() {
		public void onClick(View v) {
			numero_page++;
			
			if(numero_page >= NOMBRE_DE_PAGE) { // si c'est la dernière page
				numero_page = NOMBRE_DE_PAGE;
				page = "texte_aide_" + numero_page;
				identifiant_page = getResources().getIdentifier(page, "string", getPackageName());
				afficher_aide.setText(identifiant_page);
				bouton_suivant.setEnabled(false);
				bouton_suivant.setBackground(getResources().getDrawable(R.drawable.fleche_droite_inactive));
				
				bouton_precedent.setEnabled(true);
				bouton_precedent.setBackground(getResources().getDrawable(R.drawable.fleche_gauche_active));
				
				mettreAjourPage(numero_page);
			}
			else { // si ce n'est pas la dernière page
				page = "texte_aide_" + numero_page;
				identifiant_page = getResources().getIdentifier(page, "string", getPackageName());
				afficher_aide.setText(identifiant_page);
				bouton_precedent.setEnabled(true);
				bouton_precedent.setBackground(getResources().getDrawable(R.drawable.fleche_gauche_active));
				bouton_suivant.setBackground(getResources().getDrawable(R.drawable.fleche_droite_active));
				
				mettreAjourPage(numero_page);
			}
		}
	};
	
	public void mettreAjourPage(int numero_page) {
		afficher_page.setText("Page " + numero_page + " / " + NOMBRE_DE_PAGE);
	}
	
	public void afficherAlerte(String message) { // Affichage temporaire de message sur le périphérique
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();       
    }
}
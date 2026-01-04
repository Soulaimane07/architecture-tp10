package ma.projet.restclient;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import ma.projet.restclient.adapter.CompteAdapter;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.repository.CompteRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activité principale de l'application REST Client
 * Gère l'affichage, l'ajout, la modification et la suppression des comptes
 */
public class MainActivity extends AppCompatActivity
        implements CompteAdapter.OnDeleteClickListener, CompteAdapter.OnUpdateClickListener {

    private RecyclerView recyclerView;
    private CompteAdapter adapter;
    private RadioGroup formatGroup;
    private FloatingActionButton fabAdd;
    private String currentFormat = "JSON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupFormatSelection();
        setupAddButton();

        loadData(currentFormat);
    }

    /**
     * Initialise les vues
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        formatGroup = findViewById(R.id.formatGroup);
        fabAdd = findViewById(R.id.fabAdd);
    }

    /**
     * Configure le RecyclerView
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompteAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Configure le sélecteur de format (JSON/XML)
     */
    private void setupFormatSelection() {
        formatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            currentFormat = checkedId == R.id.radioJson ? "JSON" : "XML";
            loadData(currentFormat);
        });
    }

    /**
     * Configure le bouton d'ajout
     */
    private void setupAddButton() {
        fabAdd.setOnClickListener(v -> showAddCompteDialog());
    }

    /**
     * Affiche le dialogue pour ajouter un compte
     */
    private void showAddCompteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        TextInputEditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);

        builder.setView(dialogView)
                .setTitle("Ajouter un compte")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String soldeStr = etSolde.getText().toString();
                    if (soldeStr.isEmpty()) {
                        showToast("Veuillez entrer un solde");
                        return;
                    }

                    double solde = Double.parseDouble(soldeStr);
                    String type = typeGroup.getCheckedRadioButtonId() == R.id.radioCourant
                            ? "COURANT"
                            : "EPARGNE";

                    String formattedDate = getCurrentDateFormatted();
                    Compte compte = new Compte(null, solde, type, formattedDate);
                    addCompte(compte);
                })
                .setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Retourne la date actuelle au format yyyy-MM-dd
     */
    private String getCurrentDateFormatted() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(calendar.getTime());
    }

    /**
     * Ajoute un compte via l'API
     */
    private void addCompte(Compte compte) {
        CompteRepository repository = new CompteRepository("JSON");
        repository.addCompte(compte, new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (response.isSuccessful()) {
                    showToast("Compte ajouté avec succès");
                    loadData(currentFormat);
                } else {
                    showToast("Erreur lors de l'ajout");
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                showToast("Erreur: " + t.getMessage());
            }
        });
    }

    /**
     * Charge les données depuis l'API
     */
    private void loadData(String format) {
        CompteRepository repository = new CompteRepository(format);
        repository.getAllCompte(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Compte> comptes = response.body();
                    runOnUiThread(() -> {
                        adapter.updateData(comptes);
                        showToast("Chargé " + comptes.size() + " compte(s) en " + format);
                    });
                } else {
                    showToast("Erreur lors du chargement");
                }
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable t) {
                runOnUiThread(() -> showToast("Erreur: " + t.getMessage()));
            }
        });
    }

    /**
     * Gère le clic sur le bouton modifier
     */
    @Override
    public void onUpdateClick(Compte compte) {
        showUpdateCompteDialog(compte);
    }

    /**
     * Affiche le dialogue pour modifier un compte
     */
    private void showUpdateCompteDialog(Compte compte) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        TextInputEditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);

        // Pré-remplir avec les valeurs actuelles
        etSolde.setText(String.valueOf(compte.getSolde()));
        if (compte.getType().equalsIgnoreCase("COURANT")) {
            typeGroup.check(R.id.radioCourant);
        } else if (compte.getType().equalsIgnoreCase("EPARGNE")) {
            typeGroup.check(R.id.radioEpargne);
        }

        builder.setView(dialogView)
                .setTitle("Modifier le compte")
                .setPositiveButton("Modifier", (dialog, which) -> {
                    String soldeStr = etSolde.getText().toString();
                    if (soldeStr.isEmpty()) {
                        showToast("Veuillez entrer un solde");
                        return;
                    }

                    double solde = Double.parseDouble(soldeStr);
                    String type = typeGroup.getCheckedRadioButtonId() == R.id.radioCourant
                            ? "COURANT"
                            : "EPARGNE";

                    compte.setSolde(solde);
                    compte.setType(type);
                    updateCompte(compte);
                })
                .setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Met à jour un compte via l'API
     */
    private void updateCompte(Compte compte) {
        CompteRepository repository = new CompteRepository("JSON");
        repository.updateCompte(compte.getId(), compte, new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (response.isSuccessful()) {
                    showToast("Compte modifié avec succès");
                    loadData(currentFormat);
                } else {
                    showToast("Erreur lors de la modification");
                }
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable t) {
                showToast("Erreur: " + t.getMessage());
            }
        });
    }

    /**
     * Gère le clic sur le bouton supprimer
     */
    @Override
    public void onDeleteClick(Compte compte) {
        showDeleteConfirmationDialog(compte);
    }

    /**
     * Affiche un dialogue de confirmation avant la suppression
     */
    private void showDeleteConfirmationDialog(Compte compte) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer ce compte ?")
                .setPositiveButton("Oui", (dialog, which) -> deleteCompte(compte))
                .setNegativeButton("Non", null)
                .show();
    }

    /**
     * Supprime un compte via l'API
     */
    private void deleteCompte(Compte compte) {
        CompteRepository repository = new CompteRepository("JSON");
        repository.deleteCompte(compte.getId(), new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Compte supprimé avec succès");
                    loadData(currentFormat);
                } else {
                    showToast("Erreur lors de la suppression");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Erreur: " + t.getMessage());
            }
        });
    }

    /**
     * Affiche un message Toast
     */
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}

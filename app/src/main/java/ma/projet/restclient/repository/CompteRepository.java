package ma.projet.restclient.repository;

import ma.projet.restclient.api.CompteService;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.entities.CompteList;
import ma.projet.restclient.config.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository pour gérer les opérations CRUD sur les comptes
 * Sert de couche intermédiaire entre l'UI et l'API
 */
public class CompteRepository {
    private CompteService compteService;
    private String format;

    /**
     * Constructeur initialisant le service avec le type de convertisseur
     * 
     * @param converterType "JSON" ou "XML"
     */
    public CompteRepository(String converterType) {
        compteService = RetrofitClient.getClient(converterType).create(CompteService.class);
        this.format = converterType;
    }

    /**
     * Récupère tous les comptes depuis le serveur
     * 
     * @param callback Callback pour gérer la réponse
     */
    public void getAllCompte(Callback<List<Compte>> callback) {
        if ("JSON".equals(format)) {
            Call<List<Compte>> call = compteService.getAllCompteJson();
            call.enqueue(callback);
        } else {
            Call<CompteList> call = compteService.getAllCompteXml();
            call.enqueue(new Callback<CompteList>() {
                @Override
                public void onResponse(Call<CompteList> call, Response<CompteList> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Convertir CompteList en List<Compte>
                        List<Compte> comptes = response.body().getComptes();
                        callback.onResponse(null, Response.success(comptes));
                    } else {
                        callback.onFailure(null, new Exception("Response not successful"));
                    }
                }

                @Override
                public void onFailure(Call<CompteList> call, Throwable t) {
                    callback.onFailure(null, t);
                }
            });
        }
    }

    /**
     * Récupère un compte par son ID
     * 
     * @param id       Identifiant du compte
     * @param callback Callback pour gérer la réponse
     */
    public void getCompteById(Long id, Callback<Compte> callback) {
        Call<Compte> call = compteService.getCompteById(id);
        call.enqueue(callback);
    }

    /**
     * Ajoute un nouveau compte
     * 
     * @param compte   Compte à ajouter
     * @param callback Callback pour gérer la réponse
     */
    public void addCompte(Compte compte, Callback<Compte> callback) {
        Call<Compte> call = compteService.addCompte(compte);
        call.enqueue(callback);
    }

    /**
     * Met à jour un compte existant
     * 
     * @param id       Identifiant du compte
     * @param compte   Données mises à jour
     * @param callback Callback pour gérer la réponse
     */
    public void updateCompte(Long id, Compte compte, Callback<Compte> callback) {
        Call<Compte> call = compteService.updateCompte(id, compte);
        call.enqueue(callback);
    }

    /**
     * Supprime un compte
     * 
     * @param id       Identifiant du compte à supprimer
     * @param callback Callback pour gérer la réponse
     */
    public void deleteCompte(Long id, Callback<Void> callback) {
        Call<Void> call = compteService.deleteCompte(id);
        call.enqueue(callback);
    }
}

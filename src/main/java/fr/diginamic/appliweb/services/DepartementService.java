package fr.diginamic.appliweb.services;

import fr.diginamic.appliweb.entites.Departement;
import fr.diginamic.appliweb.exceptions.ExceptionFonctionnelle;
import fr.diginamic.appliweb.mappers.dtos.DepartementDto;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * Interface de services de type "application" pour l'exécution des CU de gestion des départements
 */
public interface DepartementService {

    /**
     * Extraction des départements
     * @return List de {@link DepartementDto}
     */
    List<DepartementDto> extraire();

    /**
     * Extraction d'un département à partir de son identifiant
     * @param id identifiant
     * @return {@link DepartementDto}
     * @throws ExceptionFonctionnelle si l'identifiant n'existe pas
     */
    DepartementDto extraireParId(int id) throws ExceptionFonctionnelle;

    /**
     * Extraction d'un département à partir de son code
     * @param code code du département
     * @return {@link DepartementDto}
     * @throws ExceptionFonctionnelle si le code du département n'existe pas
     */
    DepartementDto extraireParCode(String code) throws ExceptionFonctionnelle;

    /**
     * Insertion d'un nouveau département
     * @param departementFront données du nouveau département (code obligatoire)
     * @return List de {@link DepartementDto}
     * @throws ExceptionFonctionnelle si le code du département n'est pas renseigné.
     */
    List<DepartementDto> inserer(DepartementDto departementFront) throws ExceptionFonctionnelle;

    /**
     * Modification d'un département
     * @param departement données modifiées du département (identifiant obligatoire)
     * @return List de {@link DepartementDto}
     * @throws ExceptionFonctionnelle si le code du département n'est pas renseigné.
     */
    List<DepartementDto> modifier(DepartementDto departement) throws ExceptionFonctionnelle;

    /**
     * Suppression d'un département à partir de son identifiant
     * @param id identifiant
     * @return List de {@link DepartementDto}
     * @throws ExceptionFonctionnelle si l'identifiant n'existe pas
     */
    List<DepartementDto> supprimer(int id) throws ExceptionFonctionnelle;

    /**
     * Export PDF de la fiche du département avec toutes les villes connues
     * @param code code du département
     * @param response réponse HTTP
     */
    void exportVillesParDepartement(String code, HttpServletResponse response);

    /**
     * Recherche les données concernant un département sur une API externe et complète les données
     * @param departement département à compléter
     * @return Departement
     */
    void completeDepartement(Departement departement);

    /**
     * Vérifie les données d'un nouveau département avant d'insérer en base de données
     * @param departement departement
     * @return Departement issu de la base
     * @throws throws ExceptionFonctionnelle si une règle de gestion n'est pas respectée
     */
	Departement verifierDonnesEtInsererDepartement(DepartementDto departement) throws ExceptionFonctionnelle;
}

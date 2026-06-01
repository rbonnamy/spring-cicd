package fr.diginamic.appliweb.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.diginamic.appliweb.dao.DepartementRepository;
import fr.diginamic.appliweb.entites.Departement;
import fr.diginamic.appliweb.exceptions.ExceptionFonctionnelle;
import fr.diginamic.appliweb.exceptions.ExceptionTechnique;
import fr.diginamic.appliweb.mappers.DepartementMapper;
import fr.diginamic.appliweb.mappers.dtos.DepartementDto;
import fr.diginamic.appliweb.utils.pdf.PdfUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

/**
 * Service de type "Application" pour la gestion des départements.
 */
@Service
public class DepartementServiceDefaut implements DepartementService {

    /** Accès base de données à la table des départements */
    @Autowired
    private DepartementRepository dao;

    /** Mapper pour transformer des Departement en DepartementDto */
    @Autowired
    private DepartementMapper mapper;

    /** Pour la génération d'un PDF */
    @Autowired
    private PdfUtils pdfUtils;

    @Override
    public List<DepartementDto> extraire(){
        return mapper.toDtos(dao.findAll());
    }

    @Override
    public DepartementDto extraireParId(int id) throws ExceptionFonctionnelle {

        Departement dept = dao.findById(id);
        if (dept==null){
            throw new ExceptionFonctionnelle("Le département n'existe pas.");
        }

        return mapper.toDto(dept);
    }

    @Override
    public DepartementDto extraireParCode(String code) throws ExceptionFonctionnelle {

        Departement dept = dao.findByCode(code);
        if (dept==null){
            throw new ExceptionFonctionnelle("Ce code département n'existe pas.");
        }
        return mapper.toDto(dept);
    }

    @Transactional
    @Override
    public List<DepartementDto> inserer(DepartementDto departementFront) throws ExceptionFonctionnelle {

        if (departementFront.getCode()==null){
            throw new ExceptionFonctionnelle("Le code du département est obligatoire");
        }
        if (departementFront.getId()!=0){
            throw new ExceptionFonctionnelle("L'identifiant du département doit être nul en création.");
        }

        Departement dept = mapper.toBean(departementFront);
        Departement deptExistant = dao.findByNom(dept.getNom());
        if (deptExistant==null) {
            dao.save(dept);
        }
        else {
            throw new ExceptionFonctionnelle("Un département avec ce nom existe déjà.");
        }
        return mapper.toDtos(dao.findAll());
    }

    @Transactional
    @Override
    public List<DepartementDto> modifier(DepartementDto departementFront) throws ExceptionFonctionnelle {

        if (departementFront.getCode()==null){
            throw new ExceptionFonctionnelle("Le nom du département est obligatoire");
        }
        if (departementFront.getId()==0){
            throw new ExceptionFonctionnelle("L'identifiant du département doit être renseigné.");
        }

        Departement dept = mapper.toBean(departementFront);
        Departement deptExistant = dao.findById(dept.getId());
        if (deptExistant!=null) {
            deptExistant.setNom(dept.getNom());
            deptExistant.setCode(dept.getCode());
        }
        else {
            throw new ExceptionFonctionnelle("Ce département n'existe pas.");
        }
        return mapper.toDtos(dao.findAll());
    }

    @Transactional
    @Override
    public List<DepartementDto> supprimer(int id) throws ExceptionFonctionnelle {

        Departement deptExistant = dao.findById(id);
        if (deptExistant==null){
            throw new ExceptionFonctionnelle("Ce département n'existe pas.");
        }
        return mapper.toDtos(dao.findAll());
    }

    @Override
    public void exportVillesParDepartement(String code, HttpServletResponse response) {

        response.setHeader("Content-Disposition", "attachment; filename=\"departement"+code+".pdf\"");
        try {
            Departement dept = dao.findByCode(code);
            //TODO jeter une exception si le dept n'existe pas.

            completeDepartement(dept);

            pdfUtils.createPdf(dept, response);

            response.flushBuffer();
        } catch (IOException e) {
            throw new ExceptionTechnique("Erreur survenue lors de lé ganération du fichier CSv : "+e.getMessage());
        }
    }

    /**
     * Extrait le département correspondant de la base de données.<br>
     * Si le département n'est pas connu alors ce dernier est créé à condition que son code soit renseigné.
     * @param dto département à rechercher ou à créer
     * @return Departement
     * @throws ExceptionFonctionnelle si les données du département sont incomplètes
     */
    public Departement verifierDonnesEtInsererDepartement(DepartementDto dto) throws ExceptionFonctionnelle {

        Departement dept = mapper.toBean(dto);

        if (dept.getId()==0 && dept.getNom()==null && dept.getCode()==null){
            throw new ExceptionFonctionnelle("Un des 3 attributs du département doit être renseigné.");
        }

        Departement deptDB = null;
        if (dept.getId()!=0) {
            deptDB = dao.findById(dept.getId());
            if (deptDB==null){
                throw new ExceptionFonctionnelle("Le département d'identifiant "+dept.getId()+" n'existe pas.");
            }
        }
        else if (dept.getCode()!=null) {
            deptDB = dao.findByCode(dept.getCode());
        }
        else if (dept.getNom()!=null) {
            deptDB = dao.findByNom(dept.getNom());
        }

        if (deptDB==null) {
            if (dept.getCode()==null){
                throw new ExceptionFonctionnelle("Il s'agit d'un nouveau département. Vous devez donc renseigner le code a minima.");
            }
            deptDB = dao.save(dept);
        }
        return deptDB;
    }

    /**
     * Recherche le nom du département sur une API publique si le nom n'est pas connu.<br>
     * Met à jour la base de données dans ce dernier cas.
     * @param departement département à compléter
     */
    @Transactional
    public void completeDepartement(Departement departement) {

        Departement deptExistant = dao.findByCode(departement.getCode());
        if (deptExistant!=null && deptExistant.getNom()==null){
            RestTemplate restTemplate = new RestTemplate();
            DepartementDto response = restTemplate.getForObject("https://geo.api.gouv.fr/departements/"+departement.getCode()+"?fields=nom,code,codeRegion", DepartementDto.class);
            departement.setNom(response.getNom());

            // Mise à jour de la donnée en base ==> génère UPDATE
            deptExistant.setNom(response.getNom());
        }
    }


}

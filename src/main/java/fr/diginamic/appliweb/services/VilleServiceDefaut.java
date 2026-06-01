package fr.diginamic.appliweb.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.diginamic.appliweb.dao.DepartementRepository;
import fr.diginamic.appliweb.dao.VilleRepository;
import fr.diginamic.appliweb.entites.Departement;
import fr.diginamic.appliweb.entites.Ville;
import fr.diginamic.appliweb.exceptions.ExceptionFonctionnelle;
import fr.diginamic.appliweb.exceptions.ExceptionTechnique;
import fr.diginamic.appliweb.mappers.VilleMapper;
import fr.diginamic.appliweb.mappers.dtos.VilleDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

/**
 * Services de type "application" pour l'exécution des CU des villes.
 */
@Service
public class VilleServiceDefaut implements VilleService {

    /** Services de type application pour l'exécution des CU des départements */
    @Autowired
    private DepartementService departementService;

    /** Accès base de données à la table des villes */
    @Autowired
    private VilleRepository dao;

    /** Mapper pour transformer des Ville en VilleDto */
    @Autowired
    private VilleMapper mapper;

    /** Accès base de données à la table des départements */
    @Autowired
    private DepartementRepository deptDao;

    /** Permet de valider les données obligatoires reçues du front */
    @Autowired
    private Validator validator;

    @Override
    public List<VilleDto> extraire(PageRequest pageRequest){
        return mapper.toDtos(dao.findAll(pageRequest).toList());
    }

    @Override
    public List<VilleDto> extraire(){
        return mapper.toDtos(dao.findAll());
    }

    @Override
    public VilleDto extraireParId(int id) throws ExceptionFonctionnelle {

        Ville ville = dao.findById(id);
        if (ville==null){
            throw new ExceptionFonctionnelle("La ville d'identifiant "+id+" n'existe pas.");
        }

        return mapper.toDto(dao.findById(id));
    }

    @Override
    public List<VilleDto> extractVillesParPopulationMin(int mink) {

        int min = mink*1000;

        List<Ville> villes = dao.findByNbHabsGreaterThanOrderByNbHabsDesc(min);
        villes.stream().forEach(v->departementService.completeDepartement(v.getDepartement()));

        return mapper.toDtos(villes);
    }

    @Override
    public List<VilleDto> findVillesByMinAndDepartement(int mink, String departementCode) throws ExceptionFonctionnelle {

        Departement departement = deptDao.findByCode(departementCode);
        if (departement==null){
            throw new ExceptionFonctionnelle("Le département n'existe pas.");
        }
        int min = mink*1000;
        return mapper.toDtos(dao.findVillesByMinAndDepartement(min, departementCode));
    }

    @Override
    public List<VilleDto> extractVillesPopEntreMinEtMax(int mink, int maxk) throws ExceptionFonctionnelle {

        if (maxk<mink){
            throw new ExceptionFonctionnelle("Le max doit être supérieur au min");
        }
        int min = mink*1000;
        int max = maxk*1000;

        return mapper.toDtos(dao.findByNbHabsBetweenOrderByNbHabsDesc(min, max));
    }

    @Override
    public List<VilleDto> extractVilleNomLike(String debut) {
        return mapper.toDtos(dao.findByNomStartingWith(debut));
    }

    @Override
    public List<VilleDto> extraireVillesPourDepartementMinMax(int mink, int maxk, String departementCode) throws ExceptionFonctionnelle {

        Departement deptExistant = deptDao.findByCode(departementCode);
        if (deptExistant==null){
            throw new ExceptionFonctionnelle("Ce département n'existe pas.");
        }
        if (maxk<mink){
            throw new ExceptionFonctionnelle("Le max doit être supérieur au min.");
        }
        int min = mink*1000;
        int max = maxk*1000;

        return mapper.toDtos(dao.findVillesBetweenMinAndMaxAndDepartement(min, max, departementCode));
    }

    @Transactional
    @Override
    public List<VilleDto> inserer(VilleDto dto) throws ExceptionFonctionnelle {

        Errors result = validator.validateObject(dto);
        if (result.hasErrors()) {
            throw new ExceptionFonctionnelle(result.getAllErrors().get(0).getDefaultMessage());
        }
        if (dto.getDepartement()==null){
            throw new ExceptionFonctionnelle("Le département est obligatoire.");
        }

        Departement departementDB = departementService.verifierDonnesEtInsererDepartement(dto.getDepartement());
        Ville ville = mapper.toBean(dto);

        // Enfin je vérifie si une ville avec le même existe ou pas
        List<Ville> villesDB = dao.findByNom(dto.getNom());
        for (Ville villeDB: villesDB){

            // Il ne ne doit pas exister une ville avec le même nom dans le même département
            if (ville.getDepartement().getId()==dto.getDepartement().getId()){
                throw new ExceptionFonctionnelle("Il existe déjà une ville avec le même nom dans ce département.");
            }
        }

        // Insertion
        ville.setDepartement(departementDB);
        dao.save(ville);

        return mapper.toDtos(dao.findAll());
    }

    @Transactional
    @Override
    public List<VilleDto> modifier(VilleDto dto) throws ExceptionFonctionnelle {

        Errors result = validator.validateObject(dto);
        if (result.hasErrors()) {
            throw new ExceptionFonctionnelle(result.getAllErrors().get(0).getDefaultMessage());
        }

        Departement departement = departementService.verifierDonnesEtInsererDepartement(dto.getDepartement());
        List<Ville> villes = dao.findByNom(dto.getNom());
        for (Ville ville: villes){

            // Il ne ne doit pas exister une ville avec le même nom dans le même département
            if (ville.getId()!=dto.getId() && ville.getDepartement().getId()==dto.getDepartement().getId()){
                throw new ExceptionFonctionnelle("Il existe déjà une ville avec le même nom dans ce département.");
            }
        }

        Ville villeDB = dao.findById(dto.getId());
        if (villeDB!=null) {
            Ville villeModif = dao.findById(dto.getId());
            villeModif.setNom(dto.getNom());
            villeModif.setNbHabs(dto.getNbHabs());
            villeModif.setDepartement(departement);
        }
        return mapper.toDtos(dao.findAll());
    }

    @Transactional
    @Override
    public List<VilleDto> supprimer(int id) throws ExceptionFonctionnelle {

        Ville villeDB = dao.findById(id);
        if (villeDB==null){
            throw new ExceptionFonctionnelle("Le département d'identifiant "+id+" n'existe pas.");
        }
        dao.deleteById(id);
        return mapper.toDtos(dao.findAll());
    }

    @Override
    public void exportCsv(int min, HttpServletResponse response) {
        // On commence par positionner une propriété dans le header de la réponse pour indiquer la présence
        // d'un fichier attaché
        response.setHeader("Content-Disposition", "attachment; filename=\"export_population_"+min+"k.csv\"");
        try {
            // On va écrire dans le flux de sortie de la réponse HTTP avec response.getWriter()
            response.getWriter().append("Code département;Nom du département;Ville;Population\n");
            List<VilleDto> dtos = extractVillesParPopulationMin(min);
            for (VilleDto dto: dtos){
                String serialVille = dto.getDepartement().getCode()+";"+dto.getDepartement().getNom()+";"+dto.getNom()+";"+dto.getNbHabs()+"\n";
                response.getWriter().append(serialVille);
            }

            // Finalisation de la réponse HTTP
            response.flushBuffer();
        } catch (IOException e) {
            throw new ExceptionTechnique("Erreur survenue lors de lé ganération du fichier CSv : "+e.getMessage());
        }
    }

}

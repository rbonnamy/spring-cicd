package fr.diginamic.appliweb.dao;

import fr.diginamic.appliweb.entites.Departement;
import fr.diginamic.appliweb.entites.Ville;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DepartementDao {

    @PersistenceContext
    private EntityManager em;

    public List<Departement> findAll(){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x", Departement.class);

        // Exécution de la requête :
        return query.getResultList();
    }

    public Departement findById(int id){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x WHERE x.id=:id", Departement.class);
        query.setParameter("id", id);

        // Exécution de la requête :
        return query.getResultStream().findFirst().orElse(null);
    }

    public Departement findByNom(String nom){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x WHERE x.nom='"+nom+"'", Departement.class);

        // Exécution de la requête :
        return query.getResultStream().findFirst().orElse(null);
    }

    public Departement findByCode(String code){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x WHERE x.code='"+code+"'", Departement.class);

        // Exécution de la requête :
        return query.getResultStream().findFirst().orElse(null);
    }

    public Departement save(Departement dept) {

        em.persist(dept);
        return dept;
    }

    public Departement update(Departement departement) {
        Departement deptDB = findById(departement.getId());
        if (deptDB!=null) {
            deptDB.setNom(departement.getNom());
            deptDB.setCode(departement.getCode());
            return deptDB;
        }
        return deptDB;
    }

    public boolean delete(int id) {
        Departement deptDB = findById(id);
        if (deptDB!=null) {
            em.remove(deptDB);
            return true;
        }
        return false;
    }

    public List<Ville> extraireVillesPourDepartementMinMax(int id, int min, int max) {
        TypedQuery<Ville> query = em.createQuery("SELECT v FROM Ville v JOIN v.departement d WHERE d.id=:id AND v.nbHabs>=:min AND v.nbHabs<=:max", Ville.class);
        query.setParameter("id", id);
        query.setParameter("min", min);
        query.setParameter("max", max);
        return  query.getResultList();
    }
}

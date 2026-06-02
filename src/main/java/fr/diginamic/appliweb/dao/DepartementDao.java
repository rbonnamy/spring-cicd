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

    public List<Departement> extraire(){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x", Departement.class);

        // Exécution de la requête :
        return query.getResultList();
    }

    public Departement extraireParId(int id){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x WHERE x.id=:id", Departement.class);
        query.setParameter("id", id);

        // Exécution de la requête :
        return query.getResultStream().findFirst().orElse(null);
    }

    public Departement extraireParNom(String nom){

        // Création de la requête :
        TypedQuery<Departement> query = em.createQuery("SELECT x FROM Departement x WHERE x.nom='"+nom+"'", Departement.class);

        // Exécution de la requête :
        return query.getResultStream().findFirst().orElse(null);
    }

    public void inserer(Departement dept) {
        em.persist(dept);
    }

    public boolean modifier(Departement departement) {
        Departement deptDB = extraireParId(departement.getId());
        if (deptDB!=null) {
            deptDB.setNom(departement.getNom());
            return true;
        }
        return false;
    }

    public boolean supprimer(int id) {
        Departement deptDB = extraireParId(id);
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

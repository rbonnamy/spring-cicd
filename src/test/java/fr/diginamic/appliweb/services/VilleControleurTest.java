package fr.diginamic.appliweb.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.diginamic.appliweb.dao.DepartementRepository;
import fr.diginamic.appliweb.dao.VilleRepository;
import fr.diginamic.appliweb.entites.Departement;
import fr.diginamic.appliweb.entites.Ville;
import fr.diginamic.appliweb.exceptions.ExceptionFonctionnelle;
import fr.diginamic.appliweb.mappers.dtos.DepartementDto;
import fr.diginamic.appliweb.mappers.dtos.VilleDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

import static org.hamcrest.Matchers.containsString;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class VilleControleurTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VilleRepository repository;

    @Autowired
    private DepartementRepository deptRepository;

    @Test
    public void testExtraire(){
        try {
            this.mvc.perform(MockMvcRequestBuilders.get("/villes/toutes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Montpellier")))
                    .andExpect(content().string(containsString("Béziers")))
                    .andExpect(jsonPath("$[0].nom").value("Montpellier"))
                    .andExpect(jsonPath("$[0].nbHabs").value(281613));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testExtrairePagination(){
        try {
            this.mvc.perform(MockMvcRequestBuilders.get("/villes/pagination?numPage=1&nbLignes=1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nom").value("Béziers"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testExtraireParId(){
        try {
            this.mvc.perform(MockMvcRequestBuilders.get("/villes/id/13326"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom").value("Montpellier"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testExtraireLikeNom(){
        try {
            this.mvc.perform(MockMvcRequestBuilders.get("/villes/like/Béz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nom").value("Béziers"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testExtraireGreaterMin(){
        try {
            this.mvc.perform(MockMvcRequestBuilders.get("/villes/greater/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nom").value("Montpellier"))
                    .andExpect(jsonPath("$[1].nom").value("Béziers"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}

package fr.diginamic.appliweb.services;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class VilleControleurTest {

    @Autowired
    private MockMvc mvc;
    
    @Mock
    private DepartementService departementService;

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

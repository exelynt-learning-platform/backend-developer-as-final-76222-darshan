package com.example.booking;

import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-test-secret"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingSecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void loginReturnsJwtForSeedUser() throws Exception {
        login("user", "User@123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void userCannotCreateResource() throws Exception {
        String token = token("user", "User@123");

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Board Room",
                                  "type": "ROOM",
                                  "description": "Quiet room",
                                  "available": true,
                                  "hourlyRate": 42.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateResource() throws Exception {
        String token = token("admin", "Admin@123");

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Training Room",
                                  "type": "ROOM",
                                  "description": "Seats 20",
                                  "available": true,
                                  "hourlyRate": 75.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void userCanCreateReservationOnlyForThemself() throws Exception {
        Long resourceId = resourceRepository.findAll().get(1).getId();
        String token = token("user2", "User2@123");

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "startTime": "2030-01-01T10:00:00Z",
                                  "endTime": "2030-01-01T12:00:00Z",
                                  "status": "PENDING",
                                  "price": 70.00
                                }
                                """.formatted(resourceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user2"));
    }

    @Test
    void userCannotDeleteReservations() throws Exception {
        Long reservationId = reservationRepository.findAll().get(0).getId();
        String token = token("user", "User@123");

        mockMvc.perform(delete("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userReservationListIsScopedToOwnerButAdminSeesAll() throws Exception {
        String userToken = token("user", "User@123");
        String adminToken = token("admin", "Admin@123");

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .param("status", "CONFIRMED")
                        .param("minPrice", "50")
                        .param("maxPrice", "150")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("user"));

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(reservationRepository.count()));
    }

    private org.springframework.test.web.servlet.ResultActions login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password)));
    }

    private String token(String username, String password) throws Exception {
        return login(username, password)
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
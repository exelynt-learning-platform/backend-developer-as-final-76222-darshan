package com.example.booking;

import com.example.booking.entity.BookableResource;
import com.example.booking.entity.Reservation;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void userCanViewResources() throws Exception {
        String token = token("user", "User@123");

        mockMvc.perform(get("/resources")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(3)));
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
    void adminCanUpdateResource() throws Exception {
        Long resourceId = resourceRepository.findAll().get(0).getId();
        String token = token("admin", "Admin@123");

        mockMvc.perform(put("/resources/" + resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Conference Room",
                                  "type": "ROOM",
                                  "description": "Updated meeting room",
                                  "available": true,
                                  "hourlyRate": 60.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Conference Room"));
    }

    @Test
    void adminCanDeleteResource() throws Exception {
        String token = token("admin", "Admin@123");

        BookableResource resource = resourceRepository.save(new BookableResource(
                "Temporary Projector",
                "EQUIPMENT",
                "Temporary resource for delete test",
                true,
                new BigDecimal("15.00")
        ));

        mockMvc.perform(delete("/resources/" + resource.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
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

    @Test
    void invalidResourceRequestReturnsValidationError() throws Exception {
        String token = token("admin", "Admin@123");

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "type": "",
                                  "description": "",
                                  "available": true,
                                  "hourlyRate": -10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void getNonExistingResourceReturnsNotFound() throws Exception {
        String token = token("user", "User@123");

        mockMvc.perform(get("/resources/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void invalidReservationTimeReturnsBadRequest() throws Exception {
        String token = token("user", "User@123");
        Long resourceId = resourceRepository.findAll().get(1).getId();

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "startTime": "2030-01-01T12:00:00Z",
                                  "endTime": "2030-01-01T10:00:00Z",
                                  "status": "PENDING",
                                  "price": 50.00
                                }
                                """.formatted(resourceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("endTime must be after startTime"));
    }

    @Test
    void invalidReservationStatusParameterReturnsBadRequest() throws Exception {
        String token = token("user", "User@123");

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void userCannotViewAnotherUsersReservation() throws Exception {
        Long reservationId = reservationRepository.findAll().get(0).getId();
        String token = token("user2", "User2@123");

        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You can access only your own reservations"));
    }

    @Test
    void overlappingReservationIsRejected() throws Exception {
        Reservation existing = reservationRepository.findAll().get(0);

        Long resourceId = existing.getResource().getId();

        String token = token("user2", "User2@123");

        String startTime = existing.getStartTime().plusMinutes(30).toString();
        String endTime = existing.getEndTime().minusMinutes(30).toString();

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "status": "PENDING",
                                  "price": 50.00
                                }
                                """.formatted(resourceId, startTime, endTime)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Resource is already reserved for the requested time"));
    }

    @Test
    void invalidPriceRangeIsRejected() throws Exception {
        String token = token("user", "User@123");

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .param("minPrice", "200")
                        .param("maxPrice", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("minPrice must be less than or equal to maxPrice"));
    }

    @Test
    void userCannotUpdateReservation() throws Exception {
        Long reservationId = reservationRepository.findAll().get(0).getId();
        String token = token("user", "User@123");

        mockMvc.perform(put("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 1,
                                  "startTime": "2040-01-01T10:00:00Z",
                                  "endTime": "2040-01-01T12:00:00Z",
                                  "status": "CONFIRMED",
                                  "price": 100.00
                                }
                                """))
                .andExpect(status().isForbidden());
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
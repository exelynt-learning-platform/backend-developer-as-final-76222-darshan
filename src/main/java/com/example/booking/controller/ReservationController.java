package com.example.booking.controller;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.dto.ReservationResponse;
import com.example.booking.dto.ReservationUpdateRequest;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.security.UserPrincipal;
import com.example.booking.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public Page<ReservationResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                          @RequestParam(required = false) ReservationStatus status,
                                          @RequestParam(required = false) BigDecimal minPrice,
                                          @RequestParam(required = false) BigDecimal maxPrice,
                                          @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return reservationService.list(principal, status, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.get(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.create(request, principal);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponse update(@PathVariable Long id,
                                      @Valid @RequestBody ReservationUpdateRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.update(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal UserPrincipal principal) {
        reservationService.delete(id, principal);
    }
}
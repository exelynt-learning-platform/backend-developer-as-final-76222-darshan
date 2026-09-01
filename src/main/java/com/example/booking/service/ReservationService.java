package com.example.booking.service;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.dto.ReservationResponse;
import com.example.booking.dto.ReservationUpdateRequest;
import com.example.booking.entity.AppUser;
import com.example.booking.entity.BookableResource;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ForbiddenException;
import com.example.booking.exception.NotFoundException;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceService resourceService,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceService = resourceService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> list(UserPrincipal principal,
                                          ReservationStatus status,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice,
                                          Pageable pageable) {
        validatePriceRange(minPrice, maxPrice);

        Specification<Reservation> spec = filters(status, minPrice, maxPrice);

        if (!principal.isAdmin()) {
            spec = spec.and(ReservationRepository.belongsToUser(principal.getId()));
        }

        return reservationRepository.findAll(spec, pageable)
                .map(ReservationResponse::from);
    }

    @Transactional(readOnly = true)
    public ReservationResponse get(Long id, UserPrincipal principal) {
        Reservation reservation = findEntity(id);
        enforceOwnerOrAdmin(reservation, principal);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, UserPrincipal principal) {
        validateTimes(request.startTime(), request.endTime());

        BookableResource resource = resourceService.findEntity(request.resourceId());

        if (!resource.isAvailable()) {
            throw new BadRequestException("Resource is not available");
        }

        assertNoOverlap(resource.getId(), null, request.startTime(), request.endTime());

        AppUser user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Reservation reservation = new Reservation(
                resource,
                user,
                request.startTime(),
                request.endTime(),
                request.status() == null ? ReservationStatus.PENDING : request.status(),
                request.price()
        );

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationUpdateRequest request, UserPrincipal principal) {
        validateTimes(request.startTime(), request.endTime());

        Reservation reservation = findEntity(id);
        enforceOwnerOrAdmin(reservation, principal);

        BookableResource resource = resourceService.findEntity(request.resourceId());

        if (!resource.isAvailable()) {
            throw new BadRequestException("Resource is not available");
        }

        assertNoOverlap(resource.getId(), id, request.startTime(), request.endTime());

        reservation.setResource(resource);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setStatus(request.status());
        reservation.setPrice(request.price());

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void delete(Long id, UserPrincipal principal) {
        Reservation reservation = findEntity(id);
        enforceOwnerOrAdmin(reservation, principal);
        reservationRepository.delete(reservation);
    }

    private Reservation findEntity(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    private Specification<Reservation> filters(ReservationStatus status,
                                               BigDecimal minPrice,
                                               BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice must be less than or equal to maxPrice");
        }
    }

    private void validateTimes(OffsetDateTime startTime, OffsetDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("endTime must be after startTime");
        }
    }

    private void assertNoOverlap(Long resourceId,
                                 Long reservationId,
                                 OffsetDateTime startTime,
                                 OffsetDateTime endTime) {
        boolean exists = reservationRepository.existsOverlappingReservation(
                resourceId,
                reservationId,
                startTime,
                endTime,
                ReservationStatus.CANCELLED
        );

        if (exists) {
            throw new BadRequestException("Resource is already reserved for the requested time");
        }
    }

    private void enforceOwnerOrAdmin(Reservation reservation, UserPrincipal principal) {
        if (!principal.isAdmin() && !reservation.getUser().getId().equals(principal.getId())) {
            throw new ForbiddenException("You can access only your own reservations");
        }
    }
}
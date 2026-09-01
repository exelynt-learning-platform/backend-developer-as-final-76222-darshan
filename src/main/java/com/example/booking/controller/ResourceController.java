package com.example.booking.controller;

import com.example.booking.dto.ResourceRequest;
import com.example.booking.dto.ResourceResponse;
import com.example.booking.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
public class ResourceController {
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public Page<ResourceResponse> list(@RequestParam(required = false) Boolean available,
                                       @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return resourceService.list(available, pageable);
    }

    @GetMapping("/{id}")
    public ResourceResponse get(@PathVariable Long id) {
        return resourceService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceResponse create(@Valid @RequestBody ResourceRequest request) {
        return resourceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceResponse update(@PathVariable Long id,
                                   @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        resourceService.delete(id);
    }
}
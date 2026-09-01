package com.example.booking.service;

import com.example.booking.dto.ResourceRequest;
import com.example.booking.dto.ResourceResponse;
import com.example.booking.entity.BookableResource;
import com.example.booking.exception.NotFoundException;
import com.example.booking.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> list(Boolean available, Pageable pageable) {
        Page<BookableResource> page = available == null
                ? resourceRepository.findAll(pageable)
                : resourceRepository.findByAvailable(available, pageable);

        return page.map(ResourceResponse::from);
    }

    @Transactional(readOnly = true)
    public ResourceResponse get(Long id) {
        return ResourceResponse.from(findEntity(id));
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        BookableResource resource = new BookableResource(
                request.name(),
                request.type(),
                request.description(),
                request.available(),
                request.hourlyRate()
        );

        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse update(Long id, ResourceRequest request) {
        BookableResource resource = findEntity(id);

        resource.setName(request.name());
        resource.setType(request.type());
        resource.setDescription(request.description());
        resource.setAvailable(request.available());
        resource.setHourlyRate(request.hourlyRate());

        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public void delete(Long id) {
        BookableResource resource = findEntity(id);
        resourceRepository.delete(resource);
    }

    @Transactional(readOnly = true)
    public BookableResource findEntity(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
    }
}
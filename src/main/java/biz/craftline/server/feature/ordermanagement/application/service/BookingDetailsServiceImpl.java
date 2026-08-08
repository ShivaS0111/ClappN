package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.BookingDetailsService;
import biz.craftline.server.feature.ordermanagement.infra.entity.BookingDetailsEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.BookingDetailsEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.BookingDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingDetailsServiceImpl implements BookingDetailsService {
    private final BookingDetailsRepository repository;
    private final SecurityContextService securityContextService;

    @Autowired
    public BookingDetailsServiceImpl(BookingDetailsRepository repository,
                                     SecurityContextService securityContextService) {
        this.repository = repository;
        this.securityContextService = securityContextService;
    }

    @Override
    public List<BookingDetails> getAllBookingDetails() {
        List<Long> storeIds = securityContextService.getAccessibleStoreIds();
        if (storeIds == null) {
            return repository.findAll().stream()
                    .map(BookingDetailsEntityMapper::toModel)
                    .collect(Collectors.toList());
        }
        if (storeIds.isEmpty()) {
            return List.of();
        }
        return repository.findByStoreIdIn(storeIds).stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDetails getBookingDetails(Long id) {
        BookingDetails details = repository.findById(id)
                .map(BookingDetailsEntityMapper::toModel)
                .orElse(null);
        if (details != null) {
            securityContextService.validateStoreAccess(details.getStoreId());
        }
        return details;
    }

    @Override
    public BookingDetails addBookingDetails(BookingDetails bookingDetails) {
        securityContextService.validateStoreAccess(bookingDetails.getStoreId());
        if (bookingDetails.getBookingStatus() == null) {
            bookingDetails.setBookingStatus("PENDING");
        }
        BookingDetailsEntity entity = BookingDetailsEntityMapper.toEntity(bookingDetails);
        BookingDetailsEntity saved = repository.save(entity);
        return BookingDetailsEntityMapper.toModel(saved);
    }

    @Override
    public BookingDetails updateBookingDetails(Long id, BookingDetails bookingDetails) {
        BookingDetailsEntity existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        securityContextService.validateStoreAccess(existing.getStoreId());
        if (bookingDetails.getStoreId() != null) {
            securityContextService.validateStoreAccess(bookingDetails.getStoreId());
        }
        BookingDetailsEntity entity = BookingDetailsEntityMapper.toEntity(bookingDetails);
        entity.setId(id);
        BookingDetailsEntity saved = repository.save(entity);
        return BookingDetailsEntityMapper.toModel(saved);
    }

    @Override
    public void deleteBookingDetails(Long id) {
        repository.findById(id).ifPresent(entity -> {
            securityContextService.validateStoreAccess(entity.getStoreId());
            repository.deleteById(id);
        });
    }

    @Override
    public List<BookingDetails> getBookingsByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return repository.findByStoreId(storeId).stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetails> getBookingsByCustomerId(Long customerId) {
        List<BookingDetails> bookings = repository.findByCustomerId(customerId).stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
        List<Long> accessible = securityContextService.getAccessibleStoreIds();
        if (accessible == null) {
            return bookings;
        }
        return bookings.stream()
                .filter(b -> b.getStoreId() != null && accessible.contains(b.getStoreId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetails> getBookingsByStoreAndStatus(Long storeId, String status) {
        securityContextService.validateStoreAccess(storeId);
        return repository.findByStoreIdAndBookingStatus(storeId, status).stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetails> getBookingsByStoreAndDateRange(Long storeId, Date startDate, Date endDate) {
        securityContextService.validateStoreAccess(storeId);
        return repository.findByStoreIdAndAppointmentDateBetween(storeId, startDate, endDate).stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetails> getBookingsByStaffId(Long staffId) {
        List<BookingDetails> bookings = repository.findByStaffId(staffId).stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
        List<Long> accessible = securityContextService.getAccessibleStoreIds();
        if (accessible == null) {
            return bookings;
        }
        return bookings.stream()
                .filter(b -> b.getStoreId() != null && accessible.contains(b.getStoreId()))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDetails updateBookingStatus(Long id, String status) {
        return repository.findById(id).map(entity -> {
            securityContextService.validateStoreAccess(entity.getStoreId());
            entity.setBookingStatus(status);
            BookingDetailsEntity saved = repository.save(entity);
            return BookingDetailsEntityMapper.toModel(saved);
        }).orElse(null);
    }
}

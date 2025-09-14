package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.BookingDetailsService;
import biz.craftline.server.feature.ordermanagement.infra.entity.BookingDetailsEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.BookingDetailsEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.BookingDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingDetailsServiceImpl implements BookingDetailsService {
    private final BookingDetailsRepository repository;

    @Autowired
    public BookingDetailsServiceImpl(BookingDetailsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<BookingDetails> getAllBookingDetails() {
        return repository.findAll().stream()
                .map(BookingDetailsEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDetails getBookingDetails(Long id) {
        return repository.findById(id)
                .map(BookingDetailsEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public BookingDetails addBookingDetails(BookingDetails bookingDetails) {
        BookingDetailsEntity entity = BookingDetailsEntityMapper.toEntity(bookingDetails);
        BookingDetailsEntity saved = repository.save(entity);
        return BookingDetailsEntityMapper.toModel(saved);
    }

    @Override
    public BookingDetails updateBookingDetails(Long id, BookingDetails bookingDetails) {
        if (!repository.existsById(id)) return null;
        BookingDetailsEntity entity = BookingDetailsEntityMapper.toEntity(bookingDetails);
        entity.setId(id);
        BookingDetailsEntity saved = repository.save(entity);
        return BookingDetailsEntityMapper.toModel(saved);
    }

    @Override
    public void deleteBookingDetails(Long id) {
        repository.deleteById(id);
    }
}


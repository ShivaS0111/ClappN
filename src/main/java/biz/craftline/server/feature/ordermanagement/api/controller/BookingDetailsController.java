package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.BookingDetailsDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.BookingDetailsDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.BookingDetailsService;
import biz.craftline.server.util.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/booking-details")
public class BookingDetailsController {
    private final BookingDetailsService bookingDetailsService;

    public BookingDetailsController(BookingDetailsService bookingDetailsService) {
        this.bookingDetailsService = bookingDetailsService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getAllBookingDetails() {
        List<BookingDetails> details = bookingDetailsService.getAllBookingDetails();
        List<BookingDetailsDTO> dtos = new ArrayList<>();
        for (BookingDetails detail : details) {
            dtos.add(BookingDetailsDTOMapper.toDTO(detail));
        }
        return APIResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<BookingDetailsDTO>> getBookingDetails(@PathVariable Long id) {
        BookingDetails detail = bookingDetailsService.getBookingDetails(id);
        return APIResponse.ok(detail != null ? BookingDetailsDTOMapper.toDTO(detail) : null);
    }

    @PostMapping
    public ResponseEntity<APIResponse<BookingDetailsDTO>> addBookingDetails(@RequestBody BookingDetailsDTO dto) {
        BookingDetails detail = BookingDetailsDTOMapper.fromDTO(dto);
        BookingDetails saved = bookingDetailsService.addBookingDetails(detail);
        return APIResponse.ok( BookingDetailsDTOMapper.toDTO(saved) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<BookingDetailsDTO>> updateBookingDetails(@PathVariable Long id, @RequestBody BookingDetailsDTO dto) {
        BookingDetails detail = BookingDetailsDTOMapper.fromDTO(dto);
        BookingDetails updated = bookingDetailsService.updateBookingDetails(id, detail);
        return APIResponse.ok(updated != null ? BookingDetailsDTOMapper.toDTO(updated) : null);
    }

    @DeleteMapping("/{id}")
    public void deleteBookingDetails(@PathVariable Long id) {
        bookingDetailsService.deleteBookingDetails(id);
    }
}


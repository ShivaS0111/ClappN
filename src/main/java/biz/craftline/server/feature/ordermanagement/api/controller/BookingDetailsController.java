package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.ordermanagement.api.dto.BookingDetailsDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.BookingDetailsDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.BookingDetailsService;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/booking-details")
public class BookingDetailsController {
    private final BookingDetailsService bookingDetailsService;

    public BookingDetailsController(BookingDetailsService bookingDetailsService) {
        this.bookingDetailsService = bookingDetailsService;
    }

    @GetMapping
    @Operation(summary = "Get all bookings")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getAllBookingDetails() {
        List<BookingDetails> details = bookingDetailsService.getAllBookingDetails();
        List<BookingDetailsDTO> dtos = details.stream()
                .map(BookingDetailsDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Bookings retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<BookingDetailsDTO>> getBookingDetails(@PathVariable Long id) {
        BookingDetails detail = bookingDetailsService.getBookingDetails(id);
        if (detail == null) {
            return APIResponse.error("Booking not found", HttpStatus.NOT_FOUND);
        }
        return APIResponse.success(BookingDetailsDTOMapper.toDTO(detail), "Booking retrieved successfully");
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all bookings for a store")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getBookingsByStore(@PathVariable Long storeId) {
        List<BookingDetails> details = bookingDetailsService.getBookingsByStoreId(storeId);
        List<BookingDetailsDTO> dtos = details.stream()
                .map(BookingDetailsDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Store bookings retrieved successfully");
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all bookings for a customer")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getBookingsByCustomer(@PathVariable Long customerId) {
        List<BookingDetails> details = bookingDetailsService.getBookingsByCustomerId(customerId);
        List<BookingDetailsDTO> dtos = details.stream()
                .map(BookingDetailsDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Customer bookings retrieved successfully");
    }

    @GetMapping("/store/{storeId}/status/{status}")
    @Operation(summary = "Get bookings by store and status")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getBookingsByStoreAndStatus(
            @PathVariable Long storeId, @PathVariable String status) {
        List<BookingDetails> details = bookingDetailsService.getBookingsByStoreAndStatus(storeId, status);
        List<BookingDetailsDTO> dtos = details.stream()
                .map(BookingDetailsDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Bookings retrieved successfully");
    }

    @GetMapping("/store/{storeId}/date-range")
    @Operation(summary = "Get bookings by store and date range")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getBookingsByStoreAndDateRange(
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<BookingDetails> details = bookingDetailsService.getBookingsByStoreAndDateRange(storeId, startDate, endDate);
        List<BookingDetailsDTO> dtos = details.stream()
                .map(BookingDetailsDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Bookings retrieved successfully");
    }

    @GetMapping("/staff/{staffId}")
    @Operation(summary = "Get bookings by staff member")
    @RequirePermission("booking.read")
    public ResponseEntity<APIResponse<List<BookingDetailsDTO>>> getBookingsByStaff(@PathVariable Long staffId) {
        List<BookingDetails> details = bookingDetailsService.getBookingsByStaffId(staffId);
        List<BookingDetailsDTO> dtos = details.stream()
                .map(BookingDetailsDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Staff bookings retrieved successfully");
    }

    @PostMapping
    @Operation(summary = "Create a new booking")
    @RequirePermission("booking.create")
    public ResponseEntity<APIResponse<BookingDetailsDTO>> addBookingDetails(@RequestBody BookingDetailsDTO dto) {
        BookingDetails detail = BookingDetailsDTOMapper.fromDTO(dto);
        BookingDetails saved = bookingDetailsService.addBookingDetails(detail);
        return APIResponse.success(BookingDetailsDTOMapper.toDTO(saved), "Booking created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a booking")
    @RequirePermission("booking.update")
    public ResponseEntity<APIResponse<BookingDetailsDTO>> updateBookingDetails(
            @PathVariable Long id, @RequestBody BookingDetailsDTO dto) {
        BookingDetails detail = BookingDetailsDTOMapper.fromDTO(dto);
        BookingDetails updated = bookingDetailsService.updateBookingDetails(id, detail);
        if (updated == null) {
            return APIResponse.error("Booking not found", HttpStatus.NOT_FOUND);
        }
        return APIResponse.success(BookingDetailsDTOMapper.toDTO(updated), "Booking updated successfully");
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update booking status")
    @RequirePermission("booking.update")
    public ResponseEntity<APIResponse<BookingDetailsDTO>> updateBookingStatus(
            @PathVariable Long id, @RequestParam String status) {
        BookingDetails updated = bookingDetailsService.updateBookingStatus(id, status);
        if (updated == null) {
            return APIResponse.error("Booking not found", HttpStatus.NOT_FOUND);
        }
        return APIResponse.success(BookingDetailsDTOMapper.toDTO(updated), "Booking status updated to " + status);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a booking")
    @RequirePermission("booking.delete")
    public ResponseEntity<APIResponse<String>> deleteBookingDetails(@PathVariable Long id) {
        bookingDetailsService.deleteBookingDetails(id);
        return APIResponse.success("Booking deleted successfully");
    }
}


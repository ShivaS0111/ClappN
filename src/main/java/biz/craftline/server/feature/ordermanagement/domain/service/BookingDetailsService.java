package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;

import java.util.Date;
import java.util.List;

public interface BookingDetailsService {
    List<BookingDetails> getAllBookingDetails();
    BookingDetails getBookingDetails(Long id);
    BookingDetails addBookingDetails(BookingDetails bookingDetails);
    BookingDetails updateBookingDetails(Long id, BookingDetails bookingDetails);
    void deleteBookingDetails(Long id);
    
    List<BookingDetails> getBookingsByStoreId(Long storeId);
    List<BookingDetails> getBookingsByCustomerId(Long customerId);
    List<BookingDetails> getBookingsByStoreAndStatus(Long storeId, String status);
    List<BookingDetails> getBookingsByStoreAndDateRange(Long storeId, Date startDate, Date endDate);
    List<BookingDetails> getBookingsByStaffId(Long staffId);
    BookingDetails updateBookingStatus(Long id, String status);
}


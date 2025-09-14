package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.BookingDetails;
import java.util.List;

public interface BookingDetailsService {
    List<BookingDetails> getAllBookingDetails();
    BookingDetails getBookingDetails(Long id);
    BookingDetails addBookingDetails(BookingDetails bookingDetails);
    BookingDetails updateBookingDetails(Long id, BookingDetails bookingDetails);
    void deleteBookingDetails(Long id);
}


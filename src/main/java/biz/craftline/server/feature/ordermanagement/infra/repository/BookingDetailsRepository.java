package biz.craftline.server.feature.ordermanagement.infra.repository;

import biz.craftline.server.feature.ordermanagement.infra.entity.BookingDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingDetailsRepository extends JpaRepository<BookingDetailsEntity, Long> {
    
    List<BookingDetailsEntity> findByStoreId(Long storeId);
    
    List<BookingDetailsEntity> findByCustomerId(Long customerId);
    
    List<BookingDetailsEntity> findByStoreIdAndBookingStatus(Long storeId, String bookingStatus);
    
    List<BookingDetailsEntity> findByStoreIdAndAppointmentDateBetween(Long storeId, Date startDate, Date endDate);
    
    List<BookingDetailsEntity> findByStaffId(Long staffId);
    
    List<BookingDetailsEntity> findByStoreIdAndStaffIdAndAppointmentDate(Long storeId, Long staffId, Date appointmentDate);
    
    List<BookingDetailsEntity> findByServiceId(Long serviceId);
}


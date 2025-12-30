package com.example.hoccode.repository;

import com.example.hoccode.dto.payment.projection.MonthlyRevenueProjection;
import com.example.hoccode.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {


    Optional<Payment> findFirstByOrderCode(Long orderCode);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = com.example.hoccode.util.constant.PaymentStatus.PAID
    """)
    Long getTotalRevenue();

    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE p.status = com.example.hoccode.util.constant.PaymentStatus.PAID
      AND YEAR(p.createdAt) = :year
      AND MONTH(p.createdAt) = :month
""")
    Long getRevenueByMonth(
            @Param("year") int year,
            @Param("month") int month
    );

    @Query(value = """
    SELECT 
        DATE_FORMAT(p.created_at, '%Y-%m') AS month,
        COALESCE(SUM(p.amount), 0) AS revenue
    FROM payments p
    WHERE p.status = '1' OR p.status = 'PAID'
      AND p.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 11 MONTH)
    GROUP BY DATE_FORMAT(p.created_at, '%Y-%m')
    ORDER BY month
""", nativeQuery = true)
    List<MonthlyRevenueProjection> getRevenueLast12Months();


    @Query("""
    FROM Payment p
    WHERE p.status = com.example.hoccode.util.constant.PaymentStatus.PAID
      AND p.createdAt BETWEEN :start AND :end
    ORDER BY p.createdAt
""")
    List<Payment> findPaidBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

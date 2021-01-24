package net.metisapp.metisapi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProtocolNumberRepository extends JpaRepository<ProtocolNumber, Integer>{
    @Modifying
    @Query("DELETE FROM ProtocolNumber WHERE patient = :patient")
    int deleteAllProtocolNumbers(@Param("patient") User patient);

    @Query("SELECT n FROM ProtocolNumber n WHERE n.patient = :patient")
    List<ProtocolNumber> getAllProtocolNumbers(@Param("patient") User patient);

    @Query("SELECT n.patient FROM ProtocolNumber n WHERE n.number = :number")
    User getOwner(@Param("number") long number);
}

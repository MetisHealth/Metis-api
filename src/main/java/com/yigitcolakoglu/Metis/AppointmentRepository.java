package com.yigitcolakoglu.Metis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Date;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer>{
    @Query("SELECT a FROM Appointment a WHERE a.start >= :start AND a.end <= :end AND a.doctor = :doctor")
    List<Appointment> findAllBetweenDoctor(@Param("start") Date start, @Param("end") Date end, @Param("doctor") User doctor);    

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a WHERE ((a.end >= :start and :start >= a.start) or (:end >= a.start and a.end >= :end) or (:start <= a.start and :end >= a.end)) and a.doctor = :doctor")
    // Check whether the following is met for any appointment
    boolean findOverlaps(@Param("start") Date start, @Param("end") Date end,  @Param("doctor") User doctor);    

    @Modifying
    @Query("DELETE FROM Appointment WHERE id = :id AND doctor = :doctor")
    int deleteAppointment(@Param("id") long id, @Param("doctor") User doctor);

}

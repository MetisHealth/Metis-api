package com.yigitcolakoglu.Metis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Calendar;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer>{
    @Query("SELECT a FROM Appointment a WHERE a.start >= :start AND a.end <= :end AND a.doctor = :doctor")
    List<Appointment> findAllBetweenDoctor(@Param("start") Calendar start, @Param("end") Calendar end, @Param("doctor") User doctor);    

    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient")
    List<Appointment> findAllPatients(@Param("patient") User patient);    

    @Modifying
    @Query("DELETE FROM Appointment WHERE id = :id AND doctor = :doctor")
    int deleteAppointment(@Param("id") long id, @Param("doctor") User doctor);

    @Modifying
    @Query("DELETE FROM Appointment WHERE patient = :patient")
    void deletePatientsAppointments(@Param("patient") User patient);
}

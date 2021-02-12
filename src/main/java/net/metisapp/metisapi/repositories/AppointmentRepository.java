package net.metisapp.metisapi.repositories;

import net.metisapp.metisapi.entities.Appointment;
import net.metisapp.metisapi.entities.MetisUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Calendar;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer>{
    @Query("SELECT a FROM Appointment a JOIN a.patient WHERE a.start >= :start AND a.end <= :end AND a.doctor = :doctor")
    List<Appointment> findAllBetweenDoctor(@Param("start") Calendar start, @Param("end") Calendar end, @Param("doctor") MetisUser doctor);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p WHERE a.start >= :start AND a.end <= :end AND a.doctor.email = :doctor")
    List<Appointment> findAllBetweenDoctor(@Param("start") Calendar start, @Param("end") Calendar end, @Param("doctor") String doctor);

    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient")
    List<Appointment> findAllPatients(@Param("patient") MetisUser patient);

    @Modifying
    @Query("DELETE FROM Appointment WHERE id = :id AND doctor = :doctor")
    int deleteAppointment(@Param("id") long id, @Param("doctor") MetisUser doctor);

    @Modifying
    @Query("DELETE FROM Appointment WHERE patient = :patient")
    void deletePatientsAppointments(@Param("patient") MetisUser patient);
}

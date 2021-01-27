package net.metisapp.metisapi.repositories;

import net.metisapp.metisapi.entities.DisabledRule;
import net.metisapp.metisapi.entities.MetisUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Calendar;

public interface DisabledRuleRepository extends JpaRepository<DisabledRule, Integer>{
    @Modifying
    @Query("DELETE FROM DisabledRule WHERE id = :id AND doctor = :doctor")
    int deleteRule(@Param("id") long id, @Param("doctor") MetisUser doctor);

    @Modifying
    @Query("UPDATE DisabledRule d SET d.name = :name, d.start = :start, d.repetition = :repetition, d.duration = :duration WHERE d.id = :id")
    int updateRule(@Param("name") String name, @Param("start") Calendar start, @Param("repetition") long repetition, @Param("duration") long duration, @Param("id") long id);

    @Query("SELECT p FROM DisabledRule p WHERE p.id = :id AND p.doctor = :doctor")
    List<DisabledRule> checkDoctor(@Param("id") long id,  @Param("doctor") MetisUser doctor);

}

package com.yigitcolakoglu.Clinic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Date;

public interface UserRepository extends JpaRepository<User, Integer>{
        @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM User p WHERE p.id = :patient_id AND p.doctor = :doctor")
        boolean checkDoctor(@Param("patient_id") long id,  @Param("doctor") User doctor);

        @Query("SELECT p FROM User p WHERE p.email LIKE :email% AND p.phone LIKE :phone% AND p.name LIKE %:name% AND p.doctor = :doctor AND p.role = :role")
        List<User> searchUser(@Param("email") String email, @Param("phone") String phone, @Param("name") String name, @Param("doctor") User doctor, @Param("role") String role, Pageable pageable);

        @Query("FROM User WHERE email = :email")
        User findByEmail(@Param("email") String email);

}

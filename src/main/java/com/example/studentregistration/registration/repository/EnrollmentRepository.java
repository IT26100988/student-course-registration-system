
package com.example.studentregistration.registration.repository;

import com.example.studentregistration.registration.model.Enrollment;

import java.util.ArrayList;
import java.util.Optional;

public interface EnrollmentRepository {

    ArrayList<Enrollment> findAll();

    Optional<Enrollment> findById(String enrollmentId);

    void save(Enrollment enrollment);

    void update(Enrollment enrollment);

}

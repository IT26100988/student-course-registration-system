package com.example.studentregistration.registration.repository;

import com.example.studentregistration.registration.model.RegistrationRequest;

import java.util.ArrayList;
import java.util.Optional;

public interface RegistrationRequestRepository {

    ArrayList<RegistrationRequest> findAll();

    Optional<RegistrationRequest> findById(String requestId);

    void save(RegistrationRequest request);

    void update(RegistrationRequest request);

}

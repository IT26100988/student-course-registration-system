package com.example.studentregistration.repository.Txt;

import com.example.studentregistration.model.Admin;
import com.example.studentregistration.repository.AdminRepository;

import java.util.List;
import java.util.Optional;

public class TxtAdminRepository implements AdminRepository {

    @Override
    public void save(Admin admin) {

    }

    @Override
    public Optional<Admin> findById(String id) {

        return Optional.empty();
    }

    @Override
    public List<Admin> findAll() {

        return List.of();
    }

    @Override
    public boolean update(String id, Admin updatedAdmin) {

        return false;
    }

    @Override
    public boolean deleteById(String id) {

        return false;
    }
}
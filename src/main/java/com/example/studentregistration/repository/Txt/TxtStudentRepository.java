package com.example.studentregistration.repository.Txt;

import com.example.studentregistration.model.Student;
import com.example.studentregistration.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

public class TxtStudentRepository implements StudentRepository {

    @Override
    public void save(Student student) {

    }

    @Override
    public Optional<Student> findById(String id) {

        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {

        return List.of();
    }

    @Override
    public boolean update(String id, Student updatedStudent) {

        return false;
    }

    @Override
    public boolean deleteById(String id) {

        return false;
    }
}
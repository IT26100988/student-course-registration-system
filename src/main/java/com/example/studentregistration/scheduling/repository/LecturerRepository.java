package com.example.studentregistration.scheduling.repository;

import com.example.studentregistration.scheduling.model.Lecturer;

import java.io.IOException;
import java.util.ArrayList;

public interface LecturerRepository {
    ArrayList<Lecturer> findAll() throws IOException;

    Lecturer findById(String lecturerId) throws IOException;

    void save(Lecturer lecturer) throws IOException;

    void update(Lecturer lecturer) throws IOException;
}
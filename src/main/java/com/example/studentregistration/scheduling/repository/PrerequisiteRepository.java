package com.example.studentregistration.scheduling.repository;

import com.example.studentregistration.scheduling.model.Prerequisite;

import java.io.IOException;
import java.util.ArrayList;

public interface PrerequisiteRepository {
    ArrayList<Prerequisite> findAll() throws IOException;

    Prerequisite findById(String prerequisiteId) throws IOException;

    void save(Prerequisite prerequisite) throws IOException;

    void update(Prerequisite prerequisite) throws IOException;
}
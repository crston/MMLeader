package com.gmail.bobason01.database;

import com.gmail.bobason01.model.KillGroup;

public interface DatabaseBackend {

    void init();

    void close();

    void saveGroup(String mobId, KillGroup group);
}

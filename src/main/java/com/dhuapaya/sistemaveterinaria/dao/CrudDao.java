package com.dhuapaya.sistemaveterinaria.dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudDao<T> {

    void create(T entity) throws Exception;

    List<T> listAll() throws Exception;

    void update(T entity) throws Exception;

    void delete(int id) throws Exception;
}

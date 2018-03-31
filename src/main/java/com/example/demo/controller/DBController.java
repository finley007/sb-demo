package com.example.demo.controller;

import com.example.demo.domain.DBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/db")
public class DBController {

    final String RESULT_OK = "ok";

    @Autowired
    private DBRepository repository;

    @RequestMapping(value="/{table}", method = RequestMethod.POST)
    public String update(@PathVariable String table, @RequestBody Map record) {
        Set<String> pkSet = repository.getPKSet(table);
        if(repository.checkByPk(table, pkSet, record)) {
            repository.update(table, record, pkSet);
        } else {
            repository.create(table, record);
        }
        return RESULT_OK;
    }

    @RequestMapping(value="/{table}/delete", method = RequestMethod.POST)
    public String delete(@PathVariable String table, String key, String value) {
        repository.delete(table, key, value);
        return RESULT_OK;
    }

    @RequestMapping(value="/{table}/list", method = RequestMethod.GET)
    public List list(@PathVariable String table, int index, int num, String orderby) {
        return repository.list(table, index, num, orderby);
    }

    @RequestMapping(value="/{table}/get", method = RequestMethod.POST)
    public Map get(@PathVariable String table, String key, String value) {
        return repository.findByKey(table, key, value);
    }

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public List listTable() {
        return repository.listTables();
    }

}

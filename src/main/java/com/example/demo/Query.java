package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:${db.type}.properties")
public class Query {

    @Value("${query.pk.cols}")
    private String pkCols;

    @Value("${query.all.tables}")
    private String allTables;

    @Value("${query.paged.list}")
    private String pagedList;

    @Value("${parse.table}")
    private String parseTable;

    public String getPkCols() {
        return pkCols;
    }

    public void setPkCols(String pkCols) {
        this.pkCols = pkCols;
    }

    public String getAllTables() {
        return allTables;
    }

    public void setAllTables(String allTables) {
        this.allTables = allTables;
    }

    public String getPagedList() {
        return pagedList;
    }

    public void setPagedList(String pagedList) {
        this.pagedList = pagedList;
    }

    public String getParseTable() {
        return parseTable;
    }

    public void setParseTable(String parseTable) {
        this.parseTable = parseTable;
    }
}

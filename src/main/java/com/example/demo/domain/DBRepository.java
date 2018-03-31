package com.example.demo.domain;

import com.example.demo.Query;
import com.example.demo.field.ConverterManager;
import com.example.demo.field.IConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

@Repository
public class DBRepository {

    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String ROWNUMBER = "rownumber";

    private static final String DB_MYSQL = "mysql";
    private static final String DB_SQLSERVER = "sqlserver";

    @Value("${db.schema}")
    private String schema;

    @Value("${db.type}")
    private String dbType;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Query query;

    @Autowired
    private ConverterManager converterManager;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String table, int index, int num, String orderby) {
        String orderCol = orderby;
        if (StringUtils.isEmpty(orderCol)) {
            Set pkSet = getPKSet(table);
            if (pkSet != null && pkSet.size() > 0) {
                orderCol = pkSet.iterator().next().toString();
            }
        }
        String sql = MessageFormat.format(query.getPagedList(), table);
        if (DB_MYSQL.equals(dbType)) {
            return jdbcTemplate.queryForList(sql, new Object[]{orderCol, index * num, num});
        } else if (DB_SQLSERVER.equals(dbType)){
            return jdbcTemplate.query(sql, new Object[]{orderCol, index, index + num}, new ResultRowMapper(this.dbType, this.converterManager, parseTable(table)));
        } else {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public Map findByKey(String table, String key, String value) {
        String sql = "select * from " + table + " where " + key + " = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[]{value});
        if (list.size() > 0) {
            return (Map)list.get(0);
        } else {
            return new HashMap();
        }
    }

    @Transactional(readOnly = true)
    public boolean checkByPk(String table, Set<String> pkSet, Map record) {
        if (pkSet == null || pkSet.size() == 0) {
            return false;
        }
        StringBuffer sb = new StringBuffer("select count(*) from " + table + " where ");
        for (String col : pkSet) {
            String value = record.get(col) == null ? "" : record.get(col).toString();
            sb.append(col + " = '" + value + "' and ");
        }
        String sql = sb.toString();
        sql = sql.substring(0, sql.length() - 4);
        int count = jdbcTemplate.queryForObject(sql, Integer.class);
        return  count > 0;
    }

    @Transactional(readOnly = true)
    public Set<String> getPKSet(String table) {
        String sql = query.getPkCols();
        List<Map<String, Object>> list = new ArrayList<>();
        if (DB_MYSQL.equals(dbType)) {
            list = jdbcTemplate.queryForList(sql, new Object[]{table, schema});
        } else {
            String newSql = MessageFormat.format(sql, table);
            list = jdbcTemplate.queryForList(newSql);
        }
        Set<String> set = new HashSet<String>();
        if (list != null && list.size() > 0) {
            for (Map map : list) {
                set.add(map.get(COLUMN_NAME).toString());
            }
        }
        return set;
    }

    @Transactional(readOnly = true)
    public List listTables() {
        if (DB_MYSQL.equals(dbType)) {
            return jdbcTemplate.queryForList(query.getAllTables(), new Object[]{schema}, String.class);
        } else {
            return jdbcTemplate.queryForList(query.getAllTables(), String.class);
        }
    }

    @Transactional(readOnly = true)
    public Map parseTable(String table) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(query.getParseTable(), table);
        Map result = new HashMap();
        if (list != null && list.size() > 0) {
            for (Map map : list) {
                result.put(map.get(COLUMN_NAME), map.get(DATA_TYPE));
            }
        }
        return  result;
    }

    public void create(String table, Map record) {
        StringBuffer colSb = new StringBuffer("insert into " + table + " (");
        StringBuffer valSb = new StringBuffer(" values (");
        for (Object key : record.keySet()) {
            colSb.append(key.toString() + ",");
            valSb.append("'" + handleField(table, key, record.get(key)) + "',");
        }
        String sql = colSb.toString();
        sql = sql.substring(0, sql.length() - 1) + ") ";
        String valPart = valSb.toString();
        valPart = valPart.substring(0, valPart.length() - 1) + ") ";
        sql = sql + valPart;
        jdbcTemplate.update(sql);
    }

    public void update(String table, Map record, Set pkSet) {
        StringBuffer upSb = new StringBuffer("update " + table + " set ");
        boolean needsUpdate = false;
        for (Object key : record.keySet()) {
            if (!pkSet.contains(key)) {
                upSb.append(key.toString() + " = '" + handleField(table, key,record.get(key)) + "',");
                needsUpdate = true;
            }
        }
        if (!needsUpdate) {
            return;
        }
        StringBuffer whereSb = new StringBuffer("");
        for (Object key : record.keySet()) {
            if (pkSet.contains(key)) {
                whereSb.append(key.toString() + " = '" + record.get(key.toString()) + "' and ");
            }
        }
        String sql = upSb.toString();
        sql = sql.substring(0, sql.length() - 1);
        String whereClause = whereSb.toString();
        if (!StringUtils.isEmpty(whereClause)) {
            whereClause = whereClause.substring(0, whereClause.length() - 4);
            sql = sql + " where " + whereClause;
        }
        jdbcTemplate.update(sql);
    }

    public void delete(String table, String key, String value) {
        final String sql = "delete from " + table + " where " + key + " = '" + value + "'";
        jdbcTemplate.update(sql);
    }

    private String handleField(String table, Object column, Object fieldValue) {
        if (fieldValue == null) {
            return "";
        }
        Map tableInfo = this.parseTable(table);
        IConverter converter = this.converterManager.getOut().get(tableInfo.get(column));
        if (converter != null) {
            return converter.convert(fieldValue.toString());
        } else {
            return fieldValue.toString();
        }
    }

    class ResultRowMapper<T> implements RowMapper<List> {

        private String dbType;

        private ConverterManager converterManager;

        private Map tableInfo;

        public ResultRowMapper(String dbType, ConverterManager converterManager, Map tableInfo) {
            this.dbType = dbType;
            this.converterManager = converterManager;
            this.tableInfo = tableInfo;
        }

        @Override
        public List<T> mapRow(ResultSet rs, int rowNum) throws SQLException {
            if (rs == null) {
                return Collections.EMPTY_LIST;
            }
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            List list = new ArrayList();
            Map rowData = new HashMap();
            do {
                rowData = new HashMap(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    handleField(rowData, md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
                System.out.println("list:" + list.toString());
            } while (rs.next());
            return list;
        }

        private void handleField(Map map, Object key, Object value) {
            if (DB_SQLSERVER.equals(this.dbType)) {
                if (ROWNUMBER.equals(key)) {
                    return;
                } else {
                    String colType = this.tableInfo.get(key).toString();
                    IConverter converter = this.converterManager.getOut().get(colType);
                    if (converter != null) {
                        map.put(key, converter.convert(value.toString()));
                    } else {
                        map.put(key, value);
                    }
                }
            }
        }

    }

}

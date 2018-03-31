package com.example.demo.domain;

import com.example.demo.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.*;

@Repository
public class DBRepository {

    private static final String COLUMN_NAME = "COLUMN_NAME";

    @Value("${db.schema}")
    private String schema;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Query query;

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
        return jdbcTemplate.queryForList(sql, new Object[]{orderCol, index * num, num});
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
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[]{table, schema});
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
        return jdbcTemplate.queryForList(query.getAllTables(), new Object[]{schema}, String.class);
    }

    public void create(String table, Map record) {
        StringBuffer colSb = new StringBuffer("insert into " + table + " (");
        StringBuffer valSb = new StringBuffer(" values (");
        for (Object key : record.keySet()) {
            colSb.append(key.toString() + ",");
            valSb.append("'" + record.get(key.toString()) + "',");
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
                upSb.append(key.toString() + " = '" + record.get(key.toString()) + "',");
                needsUpdate = true;
            }
        }
        if (!needsUpdate) {
            return;
        }
        StringBuffer whereSb = new StringBuffer("");
        for (Object key : record.keySet()) {
            if (!pkSet.contains(key)) {
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

//    class ResultRowMapper<T> implements RowMapper<List> {
//
//        @Override
//        public List<T> mapRow(ResultSet rs, int rowNum) throws SQLException {
//            if (rs == null)
//                return Collections.EMPTY_LIST;
//            ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
//            int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
//            List list = new ArrayList();
//            Map rowData = new HashMap();
//            do {
//                rowData = new HashMap(columnCount);
//                for (int i = 1; i <= columnCount; i++) {
//                    rowData.put(md.getColumnName(i), rs.getObject(i));
//                }
//                list.add(rowData);
//                System.out.println("list:" + list.toString());
//            } while (rs.next());
//            return list;
//        }
//
//    }

}

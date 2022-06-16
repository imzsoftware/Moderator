package com.imz.praj.data.impl;

import com.imz.praj.data.AccountTargetDao;
import com.imz.praj.data.obj.AccountTarget;
import com.traq.common.base.BaseInitializer;
import com.traq.common.data.entity.Account;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.mapping.AccountRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


public class AccountTargetDaoImpl extends BaseInitializer implements AccountTargetDao {
   private DataSource dataSource;
   private static final String entityName = "account_target";

public DataSource getDataSource() {
/*  22 */     return this.dataSource;
/*     */   }

   public void setDataSource(DataSource dataSource) {
/*  26 */     this.dataSource = dataSource;
/*     */   }

    public List<AccountTarget> findAll() {
     JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
     StringBuilder query = new StringBuilder();
        //select sum(tg_total_devices), tg_acc_id from account_target where tg_acc_id in (1725,1726,1727,1730)
     query.append("SELECT * FROM ").append(entityName);
     List<AccountTarget> accountList = new ArrayList<>();
        try {
            List<Map> rows = jdbcTemplate.queryForList(query.toString());
            for (Map row : rows) {
                AccountTarget target = new AccountTarget();
                target.setAccId(Long.valueOf(((BigInteger)row.get("tg_acc_id")).longValue()));
                target.setTotalDevices((Integer) row.get("tg_total_devices"));
                accountList.add(target);
            }
        } catch (Exception exception) {}

    return accountList;
   }

    public List<AccountTarget> find(List<Long> accIds) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(entityName).append(" WHERE tg_acc_id in (");
        int count = 0;
        List<String> paramList = new ArrayList<>();
        while (count < accIds.size()) {
            paramList.add(accIds.get(count)+ "");
            count++;
            if (count == accIds.size()) {
                query.append("?)"); continue;
            }
            query.append("?,");
        }
        List<AccountTarget> accountList = new ArrayList<>();
        Object[] params = paramList.toArray();
        try {
            List<Map> rows = jdbcTemplate.queryForList(query.toString(), params);
            for (Map row : rows) {
                AccountTarget target = new AccountTarget();
                target.setAccId(Long.valueOf(((BigInteger)row.get("tg_acc_id")).longValue()));
                target.setDistrictId(Long.valueOf(((BigInteger)row.get("tg_level_3")).longValue()));
                target.setBlockId(Long.valueOf(((BigInteger)row.get("tg_level_4")).longValue()));
                target.setTotalDevices((Integer) row.get("tg_total_devices"));
                accountList.add(target);
            }
        } catch (Exception exception) {}

        return accountList;
    }

    public List<AccountTarget> findByPanchayat(List<Long> accIds) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        StringBuilder query = new StringBuilder();
        query.append("SELECT sum(tg_total_devices) as ct, tg_acc_id,tg_level_3,tg_level_4 FROM ").append(entityName).append(" WHERE tg_acc_id in (");
        int count = 0;
        List<String> paramList = new ArrayList<>();
        while (count < accIds.size()) {
            paramList.add(accIds.get(count)+ "");
            count++;
            if (count == accIds.size()) {
                query.append("?)"); continue;
            }
            query.append("?,");
        }
        List<AccountTarget> accountList = new ArrayList<>();
        Object[] params = paramList.toArray();
        //info("FindByPanchayat ... "+query +"   , paramList "+paramList);
        try {
            List<Map> rows = jdbcTemplate.queryForList(query.toString(), params);
            for (Map row : rows) {
                AccountTarget target = new AccountTarget();
                target.setDistrictId((Long)row.get("tg_level_3"));
                target.setBlockId((Long)row.get("tg_level_4"));
                target.setAccId((Long)row.get("tg_acc_id"));
                target.setTotalDevices(((BigDecimal) row.get("ct")).intValue());
                accountList.add(target);
            }
        } catch (Exception exception) {}

        return accountList;
    }

    public List<AccountTarget> findByBlock(List<String> accIds) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        StringBuilder query = new StringBuilder();
        query.append("SELECT sum(tg_total_devices), tg_acc_id,tg_level_3,tg_level_4 FROM ").append(entityName).append(" WHERE tg_level_4 in (");
        int count = 0;
        List<String> paramList = new ArrayList<>();
        while (count < accIds.size()) {
            paramList.add(accIds.get(count)+ "");
            count++;
            if (count == accIds.size()) {
                query.append("?)"); continue;
            }
            query.append("?,");
        }
        List<AccountTarget> accountList = new ArrayList<>();
        Object[] params = paramList.toArray();
        try {
            List<Map> rows = jdbcTemplate.queryForList(query.toString(), params);
            for (Map row : rows) {
                AccountTarget target = new AccountTarget();
                target.setBlockId((Long)row.get("tg_level_4"));
                target.setTotalDevices((Integer) row.get("tg_total_devices"));
                accountList.add(target);
            }
        } catch (Exception exception) {}

        return accountList;
    }

    public List<AccountTarget> findByDistrict(List<Long> accIds) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        StringBuilder query = new StringBuilder();
        query.append("SELECT sum(tg_total_devices) as dev, tg_acc_id,tg_level_3,tg_level_4 FROM ").append(entityName).append(" WHERE tg_level_3 in (");
        int count = 0;
        List<String> paramList = new ArrayList<>();
        while (count < accIds.size()) {
            paramList.add(accIds.get(count)+"");
            count++;
            if (count == accIds.size()) {
                query.append("?)");
            }else {
                query.append("?,");
            }
        }
        List<AccountTarget> accountList = new ArrayList<>();
        Object[] params = paramList.toArray();
        info("AccountTargetDaoImpl ......  accIds ="+accIds);
        info("AccountTargetDaoImpl ...... query = "+query +" params ="+params);
        try {
            List<Map> rows = jdbcTemplate.queryForList(query.toString(), params);
            for (Map row : rows) {
                info("AccountTargetDaoImpl ...... row = "+row);
                AccountTarget target = new AccountTarget();
                target.setDistrictId((Long)row.get("tg_level_3"));
                target.setTotalDevices(((BigDecimal) row.get("dev")).intValue());
                accountList.add(target);
            }
        } catch (Exception exception) {
            error("AccountTargetDaoImpl ...... exception = "+exception.getMessage());
            exception.printStackTrace();
        }

        return accountList;
    }
 }

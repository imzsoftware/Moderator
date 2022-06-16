package com.imz.praj.data;

import com.imz.praj.data.obj.AccountTarget;
import com.imz.praj.data.obj.PrajReportData;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.data.entity.Account;

import java.util.List;

public interface AccountTargetDao {
    List<AccountTarget> findAll();
    List<AccountTarget> find(List<Long> accIds);
    List<AccountTarget> findByPanchayat(List<Long> accIds);
    List<AccountTarget> findByBlock(List<String> accIds);
    List<AccountTarget> findByDistrict(List<Long> accIds);
}


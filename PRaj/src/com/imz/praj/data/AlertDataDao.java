package com.imz.praj.data;

import com.imz.praj.data.obj.PrajReportData;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.data.entity.Account;

import java.util.List;

public interface AlertDataDao {
    List<PrajReportData> findAlerts(RequestMessage paramRequestMessage, List<Account> paramList, boolean paramBoolean);
}


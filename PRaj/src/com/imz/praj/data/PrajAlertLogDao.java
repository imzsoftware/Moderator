package com.imz.praj.data;

import com.imz.praj.entity.PrajAlertLog;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.AlertMongoLog;
import com.traq.common.data.entity.Device;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public interface PrajAlertLogDao {
  void saveAlertLog(PrajAlertLog paramAlertMongoLog);

  public void saveNonFuncAlert(Device device, String devRedisData, Calendar cal);
  
  void saveAlert(String paramString);

  int updateOne(String paramString1, String paramString2, String paramString3);
  
  int updateMultipleFields(String paramString, Map<String, String> paramMap);
  
  List<PrajAlertLog> findAlerts(RequestMessage paramRequestMessage, List<Account> paramList, boolean paramBoolean);
}


/* Location:              D:\imz\vvdn\common.jar!\com\traq\common\data\model\mongodao\AlertLogMongoDao.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */
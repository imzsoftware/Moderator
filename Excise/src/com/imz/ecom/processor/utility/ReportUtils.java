package com.imz.praj.processor.utility;

import com.traq.common.apihandler.TagValues;
import com.traq.common.base.Constants;

import java.util.Map;

public class ReportUtils {
    private final static long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    private final static long oneDay = (24*60*60*1000);
    public static Long nonFunctionalSch(Map<String, String> reportMap, Map<String, String> redisMap) throws Exception {
        Long nonFuncCount = 0L;
        Long curTime = System.currentTimeMillis();
        for (Map.Entry<String, String> row : redisMap.entrySet()) {
            if(!reportMap.containsKey(row.getKey())){
                Long orgTs = Long.parseLong(TagValues.getNodeValue(row.getValue(), Constants.NODEORIGINTSMILLI)) - timeDiff ;
                if((orgTs +oneDay) > curTime) {
                    nonFuncCount++;
                }
            }
        }
        return nonFuncCount;
    }
}

package com.rep.handler;

import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.AccountType;
import com.traq.common.data.entity.Role;
import com.traq.common.data.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amit on 5/12/18.
 */
public class RoleManager {

    public static List<Role> userWiseRoleList(User user){
        //BaseInitializer.getRoleMap();
        List<Role> accRoles = new ArrayList<Role>();

        if(Constants.USER_ADMIN.equalsIgnoreCase(user.getType()) || "HO".equalsIgnoreCase(user.getType())){
            accRoles.add(BaseInitializer.getRoleMap().get(Constants.USER_ADMIN));
            accRoles.add(BaseInitializer.getRoleMap().get(Constants.USER_ADMIN_ASSISTANT));
            accRoles.add(BaseInitializer.getRoleMap().get(Constants.USER_CUSTOMER));
        }else if(Constants.USER_ADMIN_ASSISTANT.equalsIgnoreCase(user.getType())){
            accRoles.add(BaseInitializer.getRoleMap().get(Constants.USER_ADMIN_ASSISTANT));
            accRoles.add(BaseInitializer.getRoleMap().get(Constants.USER_CUSTOMER));
        }else {
            return null;
        }
        return  accRoles;
    }

    public static List<AccountType> accountTypes(Account account){
        List<AccountType> accRoles = new ArrayList<AccountType>();

        if(Constants.ACC_LEVEL1.equalsIgnoreCase(account.getType()) || "HO".equalsIgnoreCase(account.getType())){
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL2));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL3));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL4));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL5));
        }else if(Constants.ACC_LEVEL2.equalsIgnoreCase(account.getType())){
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL2));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL3));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL4));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL5));
        }else if(Constants.ACC_LEVEL3.equalsIgnoreCase(account.getType())){
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL3));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL4));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL5));
        }else if(Constants.ACC_LEVEL4.equalsIgnoreCase(account.getType())){
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL4));
            accRoles.add(BaseInitializer.getAccountTypeMap().get(Constants.ACC_LEVEL5));
        }else{
            return null;
        }

        return accRoles;
    }
}

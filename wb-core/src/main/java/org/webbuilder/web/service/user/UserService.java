package org.webbuilder.web.service.user;

import org.springframework.stereotype.Service;
import org.webbuilder.utils.base.MD5;
import org.webbuilder.utils.base.StringUtil;
import org.webbuilder.web.core.service.GenericService;
import org.webbuilder.web.core.utils.RandomUtil;
import org.webbuilder.web.dao.role.UserRoleMapper;
import org.webbuilder.web.dao.user.UserMapper;
import org.webbuilder.web.po.module.Module;
import org.webbuilder.web.po.role.UserRole;
import org.webbuilder.web.po.user.User;
import org.webbuilder.web.service.module.ModuleService;
import org.webbuilder.web.service.storage.StorageService;

import javax.annotation.Resource;
import java.util.*;

/**
 * 后台管理用户服务类
 * Created by generator
 *
 * @Copyright 2015 www.cqtaihong.com Inc. All rights reserved.
 * 注意：本内容仅限于重庆泰虹医药网络发展有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Service
public class UserService extends GenericService<User, String> {

    //默认数据映射接口
    @Resource
    protected UserMapper userMapper;

    @Resource
    protected UserRoleMapper userRoleMapper;


    @Resource
    protected ModuleService moduleService;

    @Override
    protected UserMapper getMapper() {
        return this.userMapper;
    }

    @Resource
    protected StorageService storageService;

    public User selectByUserName(String username) throws Exception {
        return this.getMapper().selectByUserName(username);
    }

    @Override
    public int insert(User data) throws Exception {
        tryValidPo(data);
        data.setU_id(RandomUtil.randomChar(6));
        data.setCreate_date(new Date());
        data.setUpdate_date(new Date());
        data.setPassword(MD5.encode(data.getPassword()));
        int i = userMapper.insert(data);
        if (data.getUserRoles().size() != 0) {
            for (UserRole userRole : data.getUserRoles()) {
                userRole.setU_id(RandomUtil.randomChar());
                userRole.setUser_id(data.getU_id());
                userRoleMapper.insert(userRole);
            }
        }
        return i;
    }

    @Override
    public int update(User data) throws Exception {
        tryValidPo(data);
        data.setUpdate_date(new Date());
        if (!"$default".equals(data.getPassword())) {
            data.setPassword(MD5.encode(data.getPassword()));
            userMapper.updatePassword(data);
        }
        int i = userMapper.update(data);
        if (data.getUserRoles().size() != 0) {
            //删除所有
            userRoleMapper.deleteByUserId(data.getU_id());
            for (UserRole userRole : data.getUserRoles()) {
                userRole.setU_id(RandomUtil.randomChar());
                userRole.setUser_id(data.getU_id());
                userRoleMapper.insert(userRole);
            }
        }
        return i;
    }

    public void initAdminUser(User user) throws Exception {
        HashMap map = new HashMap<>();
        map.put("sortField", "sort_index");
        map.put("sortOrder", "asc");
        List<Module> modules = moduleService.select(map);
        Map<Module, Set<String>> roleInfo = new LinkedHashMap<>();
        for (Module module : modules) {
            roleInfo.put(module, new LinkedHashSet<>(module.getM_optionMap().keySet()));
        }
        user.setRoleInfo(roleInfo);
    }

}

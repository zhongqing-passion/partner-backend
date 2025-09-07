package com.jonqing.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jonqing.usercenterbackend.common.ErrorCode;
import com.jonqing.usercenterbackend.exception.BusinessException;
import com.jonqing.usercenterbackend.model.User;
import com.jonqing.usercenterbackend.service.UserService;
import com.jonqing.usercenterbackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jonqing.usercenterbackend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author JonQing
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "jonqing";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        // 1. 非空校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }

        // 2. 账号密码设置校验
        // 账户不小于4位，密码不小于8位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，编号过长");
        }

        // 账号不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，账户不能包含特殊字符");
        }

        // 密码和确认密码一致性
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，两次输入的密码不一致");
        }

        // 账户唯一性检验
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_REPEAT,"，已存在该账号的用户");
        }

        // 星球编号唯一性检验
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planet_code", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_REPEAT,"，已存在该编号的用户");
        }

        // 密码加密
        String saltPwd = DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());

        // 向数据库中插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(saltPwd);
        user.setPlanetCode(planetCode);
        int saveResult = userMapper.insert(user);

        if (saveResult == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，插入用户数据失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 非空校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        // 2. 账号密码格式校验
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，用户密码过短");
        }

        //  账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"，账户不能包含特殊字符");
        }

        //  密码加密
        String saltPwd = DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());

        // 密码是否正确的校验
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", saltPwd);
        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }

        // 3. 用户信息脱敏
        User safeUser = getSafeUser(user);

        // 4. 服务器记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);

        // 5. 返回脱敏后的用户信息
        return safeUser;
    }

    @Override
    public User getSafeUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在，无法脱敏");
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUserName(originUser.getUserName());
        safeUser.setUserAccount(originUser.getUserAccount());
        // 不能返回给前端密码
        safeUser.setAvatar(originUser.getAvatar());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setPlanetCode(originUser.getPlanetCode());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setStatus(originUser.getStatus());
        safeUser.setCreatedTime(originUser.getCreatedTime());

        return safeUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 更新用户信息
     * @param user 要更新的用户信息
     * @param loginUser 当前登录用户
     * @return
     */
    @Override
    public boolean updateUserInfo(User user, User loginUser) {
        // 参数校验
        if (user == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验用户ID
        if (user.getId() == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        // 只允许更新自己的信息
        if (!loginUser.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能更新自己的信息");
        }

        // 创建更新对象
        User updateUser = new User();
        updateUser.setId(loginUser.getId());

        // 逐个检查并更新非空字段
        if (StringUtils.isNotBlank(user.getUserName())) {
            updateUser.setUserName(user.getUserName());
        }
        if (StringUtils.isNotBlank(user.getEmail())) {
            updateUser.setEmail(user.getEmail());
        }
        if (StringUtils.isNotBlank(user.getPhone())) {
            updateUser.setPhone(user.getPhone());
        }
        if (StringUtils.isNotBlank(user.getAvatar())) {
            updateUser.setAvatar(user.getAvatar());
        }

        return this.updateById(updateUser);
    }

    /**
     * 根据标签名搜索用户
     * @param tagNameList 所有标签名
     * @return
     */
    @Override
    public List<User> searchUsersByTagNames(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long startTime = System.currentTimeMillis();
        /**
         * 方式1：Sql查询
         */
        QueryWrapper queryWrapper = new QueryWrapper();
        for (String tagName : tagNameList) {
            queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
//            userList.forEach(user -> getSafeUser(user));
        log.info("sql query time = ：" + (System.currentTimeMillis() - startTime));
        return userList.stream().map(user -> getSafeUser(user)).collect(Collectors.toList());

        /**
         * 方式2：内存查询
         */
//        // 先查询所有用户
//        QueryWrapper queryWrapper = new QueryWrapper();
//        List<User> userList = userMapper.selectList(queryWrapper);
//        Gson gson = new Gson();
//        
//        log.info("memory query time = ：" + (System.currentTimeMillis() - startTime));
//        return userList.stream().filter(user -> {
//            String tagsStr = user.getTags();
//            // 从数据库中得到的数据要处理NPE
//            if (StringUtils.isBlank(tagsStr)) {
//                return false;
//            }
//            Set<String> tagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
//            for (String tagName : tagNameList) {
//                if (!tagNameSet.contains(tagName)) {
//                    return false;
//                }
//                // 如果标签匹配，继续检查下一个标签
//            }
//            return true;    // 说明已经检查完所有需要的标签
//        }).map(user -> getSafeUser(user)).collect(Collectors.toList());
        
    }
}





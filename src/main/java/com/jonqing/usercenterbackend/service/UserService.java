package com.jonqing.usercenterbackend.service;

import com.jonqing.usercenterbackend.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author JonQing
 * @description 针对表【user】的数据库操作Service
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    
    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafeUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 更新用户信息
     *
     * @param user 要更新的用户信息
     * @param loginUser 当前登录用户
     * @return 是否更新成功
     */
    boolean updateUserInfo(User user, User loginUser);

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTagNames(List<String> tagNameList);
}

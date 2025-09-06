package com.jonqing.usercenterbackend.control;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jonqing.usercenterbackend.common.BaseResponse;
import com.jonqing.usercenterbackend.common.ErrorCode;
import com.jonqing.usercenterbackend.common.ResultUtils;
import com.jonqing.usercenterbackend.exception.BusinessException;
import com.jonqing.usercenterbackend.model.User;
import com.jonqing.usercenterbackend.model.request.UserLoginRequest;
import com.jonqing.usercenterbackend.model.request.UserRegisterRequest;
import com.jonqing.usercenterbackend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jonqing.usercenterbackend.contant.UserConstant.ADMIN_ROLE;
import static com.jonqing.usercenterbackend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户的API接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:8081","http://localhost:3000"},allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    // 用户注册接口
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        if (request == null) {
            // 对应错误枚举类中的请求参数(请求体)为空
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // control层的验空，是对请求参数校验，不涉及业务逻辑
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        String planetCode = request.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

       long userId = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        return ResultUtils.success(userId);
    }

    // 用户登录接口
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    // 用户注销接口
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    // 获取当前用户信息
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        if (currentUser == null) {
            return ResultUtils.error(ErrorCode.NOT_LOGIN);
        }

        // 从先前用户登录信息中获取id，查询数据库中的实时登录信息
        // 为了避免用户信息更新后，调用该方法无法获取到最新更新的信息
        Long userId = currentUser.getId();
        User user = userService.getById(userId);

        // 返回脱敏后的用户信息
        User safeUser = userService.getSafeUser(user);
        return ResultUtils.success(safeUser);

    }


    /**
     * 权限接口
     */

    // 用户查询接口，仅管理员可查询
    @GetMapping("search")
    public BaseResponse<List<User>> findByUserName(String userName,HttpServletRequest request) {
        // 查询用户权限
        if (!checkUserRole(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"缺少管理员权限");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper();
        // 如果用户名不为空，则根据用户名进行模糊查询
        if (StringUtils.isNoneBlank(userName)) {
            queryWrapper.like("user_name", userName);
        }

        // 解决接口测试的时候http请求返回的响应，响应内容出现了密码字段的问题
        List<User> userList = userService.list(queryWrapper);
        userList.stream().map(user -> userService.getSafeUser(user))
                .collect(Collectors.toList());

        return ResultUtils.success(userList);

    }

    // 用户删除接口，仅管理员可删除
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody long id,HttpServletRequest request) {

        // 判断用户身份确定是否限制权限接口的调用
        if (!checkUserRole(request)) {
            return ResultUtils.error(ErrorCode.NO_AUTH);
        }

        if (id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        // MyBatis-plus自动把删除方法转换为逻辑删除
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息或ID不能为空");
        }
        // 获取当前登录用户
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            return ResultUtils.error(ErrorCode.NOT_LOGIN);
        }
        // 获取最新的用户数据
        Long userId = currentUser.getId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            return ResultUtils.error(ErrorCode.NOT_LOGIN, "用户不存在");
        }
        boolean result = userService.updateUserInfo(user, loginUser);
        return ResultUtils.success(result);
    }

    // 避免重复代码，写成一个方法
    private boolean checkUserRole(HttpServletRequest request) {
        // 判断用户身份确定是否限制权限接口的调用
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 拒绝非管理员的访问权限
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}

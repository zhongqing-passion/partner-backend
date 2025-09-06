package com.jonqing.usercenterbackend.service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.jonqing.usercenterbackend.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 */

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void testAddUser() {
        User user = new User();
        user.setUserName("zq123456");
        user.setUserAccount("zhongqing12138");
        user.setUserPassword("zhongqing");
        user.setAvatar(null);
        user.setGender(0);
        user.setPhone("15895824886");
        user.setEmail("1279887157");
        user.setStatus(0);
        userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(user.getId() > 0);
    }

    @Test
    void userRegister() {
        // 非空
        String userName = "Vc";
        String userAccount = "1279887157";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        String planetCode = "4";
        userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
    }
    
    @Test
    void testSearchUsersByTagNames() {
        // 直接创建一个固定大小的List
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> users = userService.searchUsersByTagNames(tagNameList);
//        Assertions.assertTrue(users.size() > 0);  断言方法一
        Assertions.assertNotNull(users);    // 断言方法二
    }
    
}
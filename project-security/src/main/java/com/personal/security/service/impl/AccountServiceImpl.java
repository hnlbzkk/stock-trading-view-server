package com.personal.security.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.personal.base.exception.ServiceException;
import com.personal.base.message.ResultCode;
import com.personal.base.utils.LogUtils;
import com.personal.db.redis.RedisUtil;
import com.personal.security.mapper.DbAdminMapper;
import com.personal.security.mapper.DbUserMapper;
import com.personal.security.model.converter.AccountConverter;
import com.personal.security.model.entity.AdminEntity;
import com.personal.security.model.entity.LoginUser;
import com.personal.security.model.entity.UserEntity;
import com.personal.security.model.param.LoginParam;
import com.personal.security.model.param.RegisterParam;
import com.personal.security.service.AccountService;
import com.personal.security.utils.JwtUtils;
import com.personal.security.vo.LoginVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.personal.base.constant.Constant.LOGIN_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ZKKzs
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DbUserMapper userMapper;

    @Autowired
    private DbAdminMapper adminMapper;

    @Autowired
    private RedisUtil redisCache;

    @Override
    public LoginVO login(LoginParam param) {
        try {
            // 1.Authentication 进行用户认证
            Authentication token = new UsernamePasswordAuthenticationToken(param.email(), param.password());
            Authentication authenticate = authenticationManager.authenticate(token);
            // 为空说明验证不过
            if (Objects.isNull(authenticate)) {
                logger.error(LogUtils.error(ResultCode.USER_ACCOUNT_VERIFY));
                throw new ServiceException(ResultCode.USER_ACCOUNT_VERIFY);
            }

            // 2.验证通过，进行 jwt 设置
            LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
            loginUser.getUser().setPassword("");
            String json = JSON.toJSONString(loginUser);
            String email = loginUser.getUser().getEmail();

            // 3.把用户信息保存在redis中
            redisCache.setCacheObject(LOGIN_PREFIX + email, loginUser);

            // 4.返回 JWT 数据
            return new LoginVO(JwtUtils.createJWT(json));
        } catch (Exception e) {
            logger.error(LogUtils.error(e.getMessage()));
            logger.error(LogUtils.error(ResultCode.USER_LOGIN_ERROR));
            throw new ServiceException(ResultCode.USER_LOGIN_ERROR);
        }
    }

    public boolean register(RegisterParam param) {
        UserEntity user = AccountConverter.convertToUserEntity(param);

        try {
            userMapper.insert(user);
            AdminEntity admin = AccountConverter.convertToAdminEntity(user);
            adminMapper.insert(admin);
        } catch (Exception e) {
            logger.error(LogUtils.error(e.getMessage()));
            logger.error(LogUtils.error(ResultCode.USER_REGISTER));
            throw new ServiceException(ResultCode.USER_REGISTER);
        }

        return true;
    }

    @Override
    public LoginVO test() {
        LoginVO result = null;
        try {
            // test mysql
            List<UserEntity> userEntities = userMapper.selectList(new QueryWrapper<>());

            Object cacheObject = redisCache.getCacheObject("LOGIN:admin@163.com");
            return new LoginVO("成功!   mysql有数据:" + userEntities.get(0).toString() + "      redis有数据:" + cacheObject);
        } catch (Exception e) {
            result = new LoginVO("mysql 或者 redis 有问题");
        }
        return result;
    }
}

package com.personal.security.service;

import com.personal.security.model.param.LoginParam;
import com.personal.security.model.param.RegisterParam;
import com.personal.security.vo.LoginVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ZKKzs
 */
public interface AccountService {

    LoginVO login(LoginParam param);

    boolean register(RegisterParam param);

    LoginVO test();
}

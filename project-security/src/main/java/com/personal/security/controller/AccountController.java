package com.personal.security.controller;

import com.personal.base.annotation.LogApi;
import com.personal.base.message.ResponseResult;
import com.personal.base.message.ResultCode;
import com.personal.security.model.param.LoginParam;
import com.personal.security.model.param.RegisterParam;
import com.personal.security.service.AccountService;
import com.personal.security.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author ZKKzs
 * @since 2023/8/13
 **/
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/register")
    @LogApi
    public ResponseResult register(@RequestBody @Valid RegisterParam param) {
        boolean isSuccess = accountService.register(param);
        return isSuccess ?
                ResponseResult.success() :
                ResponseResult.fail(ResultCode.USER_REGISTER);
    }

    @GetMapping("/login")
    @LogApi
    public ResponseResult<LoginVO> login(@RequestBody @Valid LoginParam param) {
        LoginVO result = accountService.login(param);
        return result != null ?
                ResponseResult.success(result) :
                ResponseResult.fail(ResultCode.USER_LOGIN_ERROR);
    }

    @GetMapping("/test")
    @LogApi
    public ResponseResult<LoginVO> test() {
        LoginVO result = accountService.test();
        return ResponseResult.success(result);
    }
}

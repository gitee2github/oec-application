/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * southbound-service is licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.openeuler.southbound.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.openeuler.southbound.model.SouthBoundUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * token工具类
 *
 * @since 2022-06-27
 */
@Slf4j
public class TokenUtil {
    // 密钥盐
    private static final String TOKEN_SECRET = "southbound-dashboard";

    /**
     * 签名生成
     *
     * @param user user
     * @return token
     */
    public static String sign(SouthBoundUser user) {
        return JWT.create()
                .withIssuer("auth0")
                .withClaim("userid", user.getId())
                .withClaim("username", user.getUsername())
                .sign(Algorithm.HMAC256(TOKEN_SECRET));
    }

    /**
     * 签名验证
     *
     * @param token token
     * @return 验证结果
     */
    public static boolean verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 刷新token
     *
     * @param token token
     * @return boolean
     */
    public static boolean refreshToken(String token) {
        if (!StringUtils.isEmpty(token)) {
            // 校验token有效性，注意需要校验的是缓存中的token
            if (verify(token)) {
                long tokenTime = TokenCacheUtil.getServerStartLongTime(token);
                long minute = (System.currentTimeMillis() - tokenTime) / 1000 / 60;
                // 更新token缓存
                if (minute < 30) {
                    TokenCacheUtil.setTokenCache(token, new Date().getTime());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取request
     *
     * @return request
     */
    public static HttpServletRequest getRequest() {
        Object requestObj = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes requestAttributes = null;
        if (requestObj instanceof ServletRequestAttributes) {
            requestAttributes = (ServletRequestAttributes) requestObj;
        }
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            request = requestAttributes.getRequest();
        }
        return request;
    }

    /**
     * 获得token中的用户名信息
     *
     * @param token token
     * @return token中包含的用户名
     */
    public static String getUserNameByToken(String token) {
        String username = null;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET)).withIssuer("auth0").build();
            DecodedJWT jwt = verifier.verify(token);
            username = jwt.getClaim("username").asString();
        } catch (TokenExpiredException t) {
            log.error("Get user name failed", t.getMessage());
        }
        return username;
    }
}

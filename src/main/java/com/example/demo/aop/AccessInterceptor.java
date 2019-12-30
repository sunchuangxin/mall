package com.example.demo.aop;

import cn.hutool.core.net.NetUtil;
import com.example.demo.base.Result;
import com.example.demo.base.Status;
import com.example.demo.component.RedisService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.RenderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API访问限制
 */
@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            boolean ip = accessLimit.ip();
            int time = accessLimit.time();
            int count = accessLimit.count();
            String key = Constants.ACCESS_KEY + request.getRequestURI();
            if (ip) {
                key += ":" + getClientIP(request);
            }
            Integer now = (Integer) redisService.get(key);
            if (now == null) {
                redisService.set(key, Integer.valueOf(1), time);
            } else if (now < count) {
                redisService.increment(key, 1);
            } else {
                RenderUtil.render(response, Result.failure(Status.ACCESS_LIMIT));
                return false;
            }
        }
        return true;
    }

    public static String getClientIP(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        String ip;
        for (String header : headers) {
            ip = request.getHeader(header);
            if (!NetUtil.isUnknow(ip)) {
                return NetUtil.getMultistageReverseProxyIp(ip);
            }
        }
        ip = request.getRemoteAddr();
        return NetUtil.getMultistageReverseProxyIp(ip);
    }
}

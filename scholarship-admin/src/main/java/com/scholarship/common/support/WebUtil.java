package com.scholarship.common.support;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Web 通用工具类，提供请求相关的公共方法。
 *
 * @author Scholarship Development Team
 */
public final class WebUtil {

    private WebUtil() {
        // 工具类禁止实例化
    }

    /**
     * 从 HttpServletRequest 提取客户端真实 IP。
     *
     * <p>优先级：X-Forwarded-For &gt; X-Real-IP &gt; request.getRemoteAddr()</p>
     *
     * @param request HTTP 请求
     * @return 客户端 IP，无法获取时返回 "unknown"
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null || ip.isBlank() ? "unknown" : ip;
    }
}

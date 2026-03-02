#!/bin/bash
# ========================================
# 研究生学业奖学金评定系统 - 快速测试脚本
# ========================================
# 使用方法:
#   bash test-api.sh
# ========================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# API 基础地址
BASE_URL="http://localhost:8080/api"

# 测试计数器
TESTS_PASSED=0
TESTS_FAILED=0

# 打印测试结果
print_result() {
    if [ "$1" -eq 0 ]; then
        echo -e "${GREEN}✓ 通过${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ 失败${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

# 打印标题
echo "========================================"
echo "  研究生学业奖学金评定系统 - API 测试"
echo "========================================"
echo ""

# ========================================
# 测试 1: 健康检查
# ========================================
echo -e "${YELLOW}[测试 1] 服务健康检查${NC}"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/auth/current-user")
if [ "$RESPONSE" == "401" ] || [ "$RESPONSE" == "200" ]; then
    print_result 0 "服务已启动 (HTTP $RESPONSE)"
else
    print_result 1 "服务未响应 (HTTP $RESPONSE)"
fi
echo ""

# ========================================
# 测试 2: 登录测试
# ========================================
echo -e "${YELLOW}[测试 2] 登录功能测试${NC}"

# 正确密码
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"123456"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    print_result 0 "登录成功获取 Token"
    echo "  Token: ${TOKEN:0:50}..."
else
    print_result 1 "登录失败"
    echo "  响应：$LOGIN_RESPONSE"
fi
echo ""

# ========================================
# 测试 3: 用户名验证（防 SQL 注入）
# ========================================
echo -e "${YELLOW}[测试 3] 用户名验证测试${NC}"

# 测试 SQL 注入用户名
INJECT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin; DROP TABLE users;--","password":"123"}')

if echo "$INJECT_RESPONSE" | grep -q "用户名包含非法字符\|用户名只能包含"; then
    print_result 0 "SQL 注入尝试被阻止"
else
    print_result 1 "SQL 注入防护未生效"
fi

# 测试数字开头的用户名
INVALID_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"123admin","password":"123"}')

if echo "$INVALID_RESPONSE" | grep -q "用户名只能包含\|必须以字母开头"; then
    print_result 0 "非法用户名格式被阻止"
else
    print_result 1 "用户名格式验证未生效"
fi
echo ""

# ========================================
# 测试 4: Token 有效性测试
# ========================================
echo -e "${YELLOW}[测试 4] Token 有效性测试${NC}"

if [ -n "$TOKEN" ]; then
    # 使用 Token 访问受保护接口
    USER_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/current-user" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$USER_RESPONSE" | grep -q '"code":200\|"username"'; then
        print_result 0 "Token 认证成功"
    else
        print_result 1 "Token 认证失败"
        echo "  响应：$USER_RESPONSE"
    fi
fi
echo ""

# ========================================
# 测试 5: Token 注销（黑名单）测试
# ========================================
echo -e "${YELLOW}[测试 5] Token 注销（黑名单）测试${NC}"

if [ -n "$TOKEN" ]; then
    # 登出
    LOGOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/logout" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$LOGOUT_RESPONSE" | grep -q "登出成功"; then
        print_result 0 "登出成功"
    else
        print_result 1 "登出失败"
    fi

    # 再次使用同一 Token 访问（应失败）
    REUSE_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/current-user" \
        -H "Authorization: Bearer $TOKEN")

    if echo "$REUSE_RESPONSE" | grep -q "401\|未授权\|失效"; then
        print_result 0 "已注销 Token 无法再次使用"
    else
        print_result 1 "Token 注销未生效"
        echo "  响应：$REUSE_RESPONSE"
    fi
fi
echo ""

# ========================================
# 测试 6: 登录限流测试（可选，耗时）
# ========================================
echo -e "${YELLOW}[测试 6] 登录限流测试（跳过，需手动测试）${NC}"
echo "  提示：连续 5 次错误登录后应触发限流"
# 取消下面注释可启用自动测试（约需 30 秒）
# for i in {1..6}; do
#     LIMIT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
#         -H "Content-Type: application/json" \
#         -d '{"username":"admin","password":"wrong"}')
#     echo "  第 $i 次尝试：$LIMIT_RESPONSE"
# done
echo ""

# ========================================
# 测试 7: 获取当前用户信息
# ========================================
echo -e "${YELLOW}[测试 7] 重新登录并获取用户信息${NC}"

# 重新登录
NEW_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"123456"}')

NEW_TOKEN=$(echo "$NEW_LOGIN" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$NEW_TOKEN" ]; then
    # 获取用户信息
    CURRENT_USER=$(curl -s -X GET "$BASE_URL/auth/current-user" \
        -H "Authorization: Bearer $NEW_TOKEN")

    if echo "$CURRENT_USER" | grep -q '"username":"admin"'; then
        print_result 0 "获取当前用户信息成功"
        echo "  用户信息：$CURRENT_USER"
    else
        print_result 1 "获取用户信息失败"
    fi
fi
echo ""

# ========================================
# 打印总结
# ========================================
echo "========================================"
echo "  测试总结"
echo "========================================"
echo -e "通过：${GREEN}$TESTS_PASSED${NC}"
echo -e "失败：${RED}$TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}部分测试失败，请检查日志${NC}"
    exit 1
fi

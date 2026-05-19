package com.noteaiBackend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.noteaiBackend.dto.CreateUserRequest;
import com.noteaiBackend.dto.LoginRequest;
import com.noteaiBackend.dto.LoginResponse;
import com.noteaiBackend.dto.UpdateUserRequest;
import com.noteaiBackend.dto.UserDTO;
import com.noteaiBackend.dto.UserSimpleDTO;
import com.noteaiBackend.dto.UserStatsDTO;
import com.noteaiBackend.entity.User;
import com.noteaiBackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 基础CRUD
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findEntityById(Integer id) {
        return userRepository.findById(id);
    }

    public UserDTO findById(Integer id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    public Optional<User> findByUserAccount(String userAccount) {
        return userRepository.findByUserAccount(userAccount);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public boolean existsByUserAccount(String userAccount) {
        return userRepository.existsByUserAccount(userAccount);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // 按角色查询
    public List<UserDTO> findByRole(Integer role) {
        return userRepository.findByRole(role).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<UserSimpleDTO> findByRole(Integer role, Pageable pageable) {
        Page<User> userPage = userRepository.findByRole(role, pageable);
        List<UserSimpleDTO> dtos = userPage.getContent().stream()
                .map(UserSimpleDTO::fromEntity)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, userPage.getTotalElements());
    }

    // 按状态查询
    public List<User> findByStatus(Integer status) {
        return userRepository.findByStatus(status);
    }

    public Page<User> findByStatus(Integer status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }

    // 按性别查询
    public List<User> findByGender(Integer gender) {
        return userRepository.findByGender(gender);
    }

    public Page<User> findByGender(Integer gender, Pageable pageable) {
        return userRepository.findByGender(gender, pageable);
    }

    // 组合条件查询
    public List<User> findByConditions(Integer role, Integer status, Integer gender) {
        return userRepository.findByConditions(role, status, gender);
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.searchByKeyword(query, pageable);
    }

    @Transactional
    public UserDTO updateStatus(Integer id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }
    public List<User> searchByKeyword(String keyword) {
        return userRepository.searchByKeyword(keyword);
    }

    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    // 统计
    public Long countAllUsers() {
        return userRepository.count();
    }

    public Long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    public Long countBlockedUsers() {
        return userRepository.countBlockedUsers();
    }

    public Long countByRole(Integer role) {
        return userRepository.countByRole(role);
    }

    public Long countActiveByRole(Integer role) {
        return userRepository.countActiveByRole(role);
    }

    public UserStatsDTO getUserStatistics() {
        UserStatsDTO stats = new UserStatsDTO();

        stats.setTotalUsers(countAllUsers());
        stats.setActiveUsers(countActiveUsers());
        stats.setBlockedUsers(countBlockedUsers());

        // 按角色统计
        List<Object[]> roleCounts = userRepository.countUsersByRole();
        Map<String, Long> roleMap = new HashMap<>();
        for (Object[] row : roleCounts) {
            Integer role = row[0] != null ? ((Number) row[0]).intValue() : null;
            Long count = (Long) row[1];
            roleMap.put(getRoleName(role), count);
        }
        stats.setUsersByRole(roleMap);

        // 按状态统计
        List<Object[]> statusCounts = userRepository.countUsersByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            Integer status = row[0] != null ? ((Number) row[0]).intValue() : null;
            Long count = (Long) row[1];
            statusMap.put(getStatusName(status), count);
        }
        stats.setUsersByStatus(statusMap);

        // 按性别统计
        List<Object[]> genderCounts = userRepository.countUsersByGender();
        Map<String, Long> genderMap = new HashMap<>();
        for (Object[] row : genderCounts) {
            Integer gender = row[0] != null ? ((Number) row[0]).intValue() : null;
            Long count = (Long) row[1];
            genderMap.put(getGenderName(gender), count);
        }
        stats.setUsersByGender(genderMap);

        return stats;
    }

    // 业务方法
    @Transactional
    public UserDTO save(User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        if (user.getStatus() == null) {
            user.setStatus(User.STATUS_NORMAL);
        }
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Transactional
    public UserDTO register(CreateUserRequest request) {
        if (userRepository.existsByUserAccount(request.getUserAccount())) {
            throw new RuntimeException("账号已存在: " + request.getUserAccount());
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已存在: " + request.getPhone());
        }

        request.validate();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setUserAccount(request.getUserAccount());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        System.out.println("注册加密: raw=" + request.getPassword() + ", encoded=" + encodedPassword);
        user.setPassword(encodedPassword); // 加密密码
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        user.setSignature(request.getSignature());
        user.setAvatar(request.getAvatar());
        user.setRole(request.getRole());
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(User.STATUS_NORMAL);

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Transactional
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDTO update(Integer id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .map(existing -> {
                    if (request.getUsername() != null) {
                        existing.setUsername(request.getUsername());
                    }
                    if (request.getRealName() != null) {
                        existing.setRealName(request.getRealName());
                    }
                    if (request.getPhone() != null) {
                        Optional<User> userByPhone = userRepository.findByPhone(request.getPhone());
                        if (userByPhone.isPresent() && !userByPhone.get().getId().equals(id)) {
                            throw new RuntimeException("手机号已被其他用户使用: " + request.getPhone());
                        }
                        existing.setPhone(request.getPhone());
                    }
                    if (request.getAvatar() != null) {
                        existing.setAvatar(request.getAvatar());
                    }
                    if (request.getAge() != null) {
                        existing.setAge(request.getAge());
                    }
                    if (request.getSignature() != null) {
                        existing.setSignature(request.getSignature());
                    }
                    if (request.getStatus() != null) {
                        existing.setStatus(request.getStatus());
                    }
                    if (request.getGender() != null) {
                        existing.setGender(request.getGender());
                    }
                    if (request.getRole() != null) {
                        if (request.getRole() < 1 || request.getRole() > 3) {
                            throw new RuntimeException("角色值无效，必须是1-3");
                        }
                        existing.setRole(request.getRole());
                    }

                    User updatedUser = userRepository.save(existing);
                    return UserDTO.fromEntity(updatedUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

//    @Transactional
//    public UserDTO updateStatus(Integer id, Integer status) {
//        return userRepository.findById(id)
//                .map(user -> {
//                    user.setStatus(status);
//                    User updatedUser = userRepository.save(user);
//                    return UserDTO.fromEntity(updatedUser);
//                })
//                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
//    }

    @Transactional
    public UserDTO updateRole(Integer id, Integer role) {
        if (role < 1 || role > 3) {
            throw new RuntimeException("角色值无效，必须是1-3");
        }

        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(role);
                    User updatedUser = userRepository.save(user);
                    return UserDTO.fromEntity(updatedUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    @Transactional
    public UserDTO updateAvatar(Integer id, String avatarUrl) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setAvatar(avatarUrl);
                    User updatedUser = userRepository.save(user);
                    return UserDTO.fromEntity(updatedUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    @Transactional
    public UserDTO updatePassword(Integer id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }

    public boolean authenticate(String userAccount, String password) {
        return userRepository.findByUserAccount(userAccount)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("登录尝试: account=" + request.getUserAccount());
        User user = userRepository.findByUserAccount(request.getUserAccount())
                .orElseThrow(() -> new RuntimeException("账号或密码错误"));

        boolean matches = false;
        try {
            matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } catch (Exception e) {
            System.err.println("密码校验异常: " + e.getMessage());
        }

        // 如果 BCrypt 校验失败，尝试明文匹配（仅用于从明文向加密过度的阶段）
        if (!matches && request.getPassword().equals(user.getPassword())) {
            System.out.println("检测到明文密码登录，正在自动升级为加密密码...");
            matches = true;
            // 自动升级密码为加密形式
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        if (!matches) {
            System.out.println("密码不匹配");
            throw new RuntimeException("账号或密码错误");
        }

        // 检查用户状态：如果status=0（屏蔽），拒绝登录
        if (user.getStatus() == User.STATUS_BLOCKED) {
            System.out.println("账号已被屏蔽");
            throw new RuntimeException("账号已被屏蔽");
        }

        // 角色一致性校验
        if (request.getRole() != null) {
            int requestedRoleInt = 0;
            switch (request.getRole()) {
                case "student": requestedRoleInt = User.ROLE_STUDENT; break;
                case "teacher": requestedRoleInt = User.ROLE_TEACHER; break;
                case "admin": requestedRoleInt = User.ROLE_ADMIN; break;
            }
            if (user.getRole() != requestedRoleInt) {
                throw new RuntimeException("所选角色与账号实际职业不符");
            }
        }

        LoginResponse response = new LoginResponse();
        response.setUser(UserDTO.fromEntity(user));
        response.setToken(generateToken(user));
        response.setLoginTime(LocalDateTime.now());
        System.out.println("登录成功: " + user.getUsername());
        return response;
    }

    // 批量操作
    @Transactional
    public void updateUserStatusBatch(List<Integer> userIds, Integer status) {
        userIds.forEach(userId -> {
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(status);
                userRepository.save(user);
            });
        });
    }

    @Transactional
    public void updateUserRoleBatch(List<Integer> userIds, Integer role) {
        if (role < 1 || role > 3) {
            throw new RuntimeException("角色值无效，必须是1-3");
        }

        userIds.forEach(userId -> {
            userRepository.findById(userId).ifPresent(user -> {
                user.setRole(role);
                userRepository.save(user);
            });
        });
    }

    // 辅助方法
    private String generateToken(User user) {
        // 简单的token生成，实际应该使用JWT
        return "token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    private String getRoleName(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case User.ROLE_STUDENT: return "学生";
            case User.ROLE_TEACHER: return "教师";
            case User.ROLE_ADMIN: return "管理员";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case User.STATUS_NORMAL: return "正常";
            case User.STATUS_BLOCKED: return "屏蔽";
            default: return "未知";
        }
    }

    private String getGenderName(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case User.GENDER_FEMALE: return "女";
            case User.GENDER_MALE: return "男";
            default: return "未知";
        }
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}

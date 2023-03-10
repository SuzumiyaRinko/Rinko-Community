package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@TableName("sys_user")
@Data
public class User implements UserDetails, Serializable {
    // 自增id
    @TableId(type = IdType.AUTO)
    private Long id;
    // 账号（邮箱）
    private String username;
    // 密码
    private String password;
    // 用户昵称
    private String nickname;
    // 密码盐
    private String salt;
    // 性别 1:男 2:女
    private Integer gender;
    // 当前激活状态 0:未激活 1:激活 2:匿名
    private Integer activation;
    // 激活时的UUID
    @TableField("activation_UUID")
    private String activationUUID;
    // 头像路径
    private String avatar;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @TableLogic
    private Boolean isDelete;

    // 当前用户角色
    @TableField(exist = false)
    private List<Integer> roles;
    // 当前用户的所有权限
    @TableField(exist = false)
    private List<String> authoritiesStr;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authoritiesStr == null) {
            return null;
        }
        return authoritiesStr.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    // true: 账号未过期
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    // true: 账号未锁定
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    // true: 账号凭证未过期
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // true: 账号可用
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}

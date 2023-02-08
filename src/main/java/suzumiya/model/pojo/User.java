package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@TableName("sys_user")
@Data
public class User implements UserDetails {

    @TableId(type = IdType.AUTO)
    private Long id; // 自增id
    private String username;
    private String password;
    private String nickname; // 用户昵称
    private String salt;
    private Integer gender; // 0:男 1:女
    private Integer activation; // 0:未激活 1:激活
    @TableField("activation_UUID")
    private String activationUUID;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    // 当前用户的所有权限
    @TableField(exist = false)
    private List<String> authoritiesStr;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(authoritiesStr == null) {
            return null;
        }
        return authoritiesStr.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    // true: 账号未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // true: 账号未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // true: 账号凭证未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // true: 账号可用
    @Override
    public boolean isEnabled() {
        return true;
    }
}

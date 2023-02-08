package suzumiya.util;//package suzumiya.util;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import suzumiya.model.pojo.User;
//
//import java.util.List;
//
//@Component("authorityUtils")
//public class AuthorityUtils {
//
//    /* 权限鉴定 */
//    public boolean hasAuthority(String necessaryAuthority) {
//        /* 获取当前用户的权限 */
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User) authentication.getPrincipal();
//        List<String> authorities = user.getAuthoritiesStr();
//        /* 判断用户权限集合中是否存在necessaryAuthority */
//        return authorities.contains(necessaryAuthority);
//    }
//}

package suzumiya.model.dto;//package suzumiya.model.dto;
//
//import org.springframework.stereotype.Component;
//import suzumiya.model.pojo.User;
//
//@Component
//public class UserHolder {
//
//    // 当前登录的用户
//    public ThreadLocal<User> loginUser = new ThreadLocal<>();
//
//    public void setLoginUser(User user) {
//        loginUser.set(user);
//    }
//
//    public void clear() {
//        loginUser.remove();
//    }
//
//    public User getLoginUser() {
//        return loginUser.get();
//    }
//}

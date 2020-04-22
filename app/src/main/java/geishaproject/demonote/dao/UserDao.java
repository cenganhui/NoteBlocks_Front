package geishaproject.demonote.dao;

public class UserDao {
    private static String u_user="test";

    public static String getU_user() {
        return u_user;
    }

    public static void setU_user(String u_user) {
        UserDao.u_user = u_user;
    }
}

package IServices;

import Entities.User;
import java.util.List;

public interface IUserService {

    User addUser(User user);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}

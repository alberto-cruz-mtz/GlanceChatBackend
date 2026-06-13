package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.ProfileResponse;
import alberto.cruz.mtz.glance.chat.backend.exception.UserNotFoundException;
import alberto.cruz.mtz.glance.chat.backend.model.User;
import alberto.cruz.mtz.glance.chat.backend.repository.UserRepository;
import alberto.cruz.mtz.glance.chat.backend.util.AvatarStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AvatarStorage avatarStorage;

    @Override
    public ProfileResponse setUpProfile(String id, String username, MultipartFile file) {
        User user = this.findUserById(id);

        if (file != null && !file.isEmpty()) {
            String avatarUrl = this.avatarStorage.upload(id, file);
            user.setAvatarUrl(avatarUrl);
        }

        user.setDisplayName(username);
        user.setHasSetUpProfile(true);
        User updatedUser = userRepository.save(user);

        return new ProfileResponse(updatedUser.getAvatarUrl(), updatedUser.getDisplayName());
    }

    @Override
    public String updateAvatarUrl(String id, MultipartFile file) {
        User user = this.findUserById(id);

        String avatarUrl = this.avatarStorage.upload(id, file);
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        return updatedUser.getAvatarUrl();
    }

    @Override
    public void updateUsername(String id, String username) {
        User user = this.findUserById(id);
        user.setDisplayName(username);

        userRepository.save(user);
    }

    private User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }
}

package alberto.cruz.mtz.glance.chat.backend.service;

import alberto.cruz.mtz.glance.chat.backend.dto.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

   ProfileResponse setUpProfile(String id, String username, MultipartFile file);
}

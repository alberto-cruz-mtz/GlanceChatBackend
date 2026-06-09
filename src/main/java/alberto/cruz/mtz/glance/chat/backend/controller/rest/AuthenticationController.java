package alberto.cruz.mtz.glance.chat.backend.controller.rest;

import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationResponse;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationRequest;
import alberto.cruz.mtz.glance.chat.backend.dto.AuthenticationWithOtpRequest;
import alberto.cruz.mtz.glance.chat.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup/request-code")
    public ResponseEntity<Void> requestCodeForSignup(@RequestBody @Valid AuthenticationRequest request) {
        authenticationService.prepareBeforeRegister(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/request-code")
    public ResponseEntity<Void> requestCodeForLogin(@RequestBody @Valid AuthenticationRequest request){
        authenticationService.prepareBeforeAuthenticate(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationWithOtpRequest request){
        var response = authenticationService.authenticate(request.email(), request.OTP());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/signup/verify")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid AuthenticationWithOtpRequest request){
        var response = authenticationService.register(request.email(), request.OTP());
        return ResponseEntity.ok(response);
    }
}

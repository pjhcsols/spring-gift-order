package gift.service.user;


import gift.domain.user.User;
import gift.exception.user.InvalidCredentialsException;
import gift.exception.user.UserAlreadyExistsException;
import gift.exception.user.UserNotFoundException;
import gift.repository.user.UserRepository;
import gift.util.JwtTokenUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String email, String password) throws UserNotFoundException, InvalidCredentialsException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return jwtTokenUtil.generateAccessToken(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String generateRefreshToken(String email) {
        return jwtTokenUtil.generateRefreshToken(email);
    }

    public void blacklistToken(String token) {
        jwtTokenUtil.blacklistToken(token);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public void registerUser(String email, String password) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists");
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(email, hashedPassword);
        userRepository.save(newUser);
    }

    //카카오 로그인
    public User findOrCreateUser(Long id, String email) {
        // 이메일이 주어진 경우, 해당 이메일로 사용자 검색
        // 현재 카카오 서버에서 이메일을 못 받는 상황을 고려하여 임시 코드
        String emailToUse = (email == null || email.isEmpty()) ? id.toString() : email;

        return userRepository.findByEmail(emailToUse)
                .orElseGet(() -> {
                    // 이메일이 없거나 사용자 존재하지 않는 경우 새로운 사용자 생성
                    String randomPassword = UUID.randomUUID().toString();
                    String encodedPassword = passwordEncoder.encode(randomPassword);

                    User newUser = new User(id, emailToUse, encodedPassword);
                    return userRepository.save(newUser);
                });
    }
    /*
    public User findOrCreateUser(Long id, String email) {
        // 이메일이 주어진 경우, 해당 이메일로 사용자 검색
        // 현재 카카오 서버에서 이메일을 못 받는 상황을 고려하여 임시 코드
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // 이메일로 사용자 찾음
            return userOptional.get();
        } else {
            // 이메일이 없거나 사용자 존재하지 않는 경우
            // id를 이메일로 사용하여 새로운 사용자 생성
            String randomPassword = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(randomPassword);

            // 이메일이 null 또는 빈 문자열인 경우, id를 문자열로 변환하여 이메일로 사용
            String emailToUse = (email == null || email.isEmpty()) ? id.toString() : email;

            // 중복 이메일 확인
            Optional<User> existingUserOptional = userRepository.findByEmail(emailToUse);
            if (existingUserOptional.isEmpty()) {
                // 이메일이 비어있거나 유저가 없으면 새 사용자 생성
                User newUser = new User(id, emailToUse, encodedPassword);
                return userRepository.save(newUser);
            } else {
                // 이메일이 이미 존재하면 해당 사용자 반환
                return existingUserOptional.get();
            }
        }
    }

     */

    //카카오 인증 후 서버 Jwt 발급
    public Map<String, String> generateJwtToken(User user) {
        String jwtToken = jwtTokenUtil.generateAccessToken(user.getEmail());
        String jwtRefresh = jwtTokenUtil.generateRefreshToken(user.getEmail());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("jwt_token", jwtToken);
        tokens.put("jwt_refresh", jwtRefresh);

        return tokens;
    }


}
package ru.tokarev.cloudstorage.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.tokarev.cloudstorage.database.entity.Role;
import ru.tokarev.cloudstorage.service.UserService;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserService userService;

    private final String JWT_SECRET = "secret";  // Ваш секретный ключ
    private final long JWT_EXPIRATION_MS = 86400000; // 1 день

    // Метод для генерации токена
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();  // Получаем имя пользователя
        Role userRole = userService.findByUsername(username).get().getRole();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);// Устанавливаем срок действия токена

        return Jwts.builder()
                .setSubject(username)  // Устанавливаем имя пользователя в качестве субъекта
//                .claim("role", userRole.toString())
                .setIssuedAt(now)  // Устанавливаем дату выпуска токена
                .setExpiration(expiryDate)  // Устанавливаем дату истечения срока действия токена
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)// Используем алгоритм HMAC с секретом
                .compact();  // Возвращаем сгенерированный токен
    }

    // Метод для извлечения имени пользователя из токена
    public String getUsernameFromJWT(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SECRET)  // Указываем секрет для парсинга
                .parseClaimsJws(token)  // Парсим токен
                .getBody()  // Извлекаем тело токена
                .getSubject();  // Возвращаем имя пользователя
    }

    // Метод для валидации токена
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);  // Пытаемся распарсить токен с секретом
            return true;  // Токен валидный
        } catch (Exception ex) {
            return false;  // Ошибка при парсинге — токен недействителен
        }
    }
}

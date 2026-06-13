package com.team13.context;

import com.team13.context.entity.Favorite;
import com.team13.context.entity.User;
import com.team13.context.mapper.FavoriteMapper;
import com.team13.context.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FavoriteUniqueConstraintIntegrationTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Test
    void duplicateFavoriteViolatesUniqueIndex() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername("u_" + UUID.randomUUID());
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        Favorite first = new Favorite();
        first.setUserId(user.getId());
        first.setTargetType("QUESTION");
        first.setTargetId(999L);
        first.setCreatedAt(now);
        first.setUpdatedAt(now);
        favoriteMapper.insert(first);

        Favorite duplicate = new Favorite();
        duplicate.setUserId(user.getId());
        duplicate.setTargetType("QUESTION");
        duplicate.setTargetId(999L);
        duplicate.setCreatedAt(now);
        duplicate.setUpdatedAt(now);

        assertThrows(DuplicateKeyException.class, () -> favoriteMapper.insert(duplicate));
    }
}

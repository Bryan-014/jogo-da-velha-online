package com.example.api.repository;

import com.example.api.model.Game;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class GameCacheRepository {

    private final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();

    public void save(Game game) {
        games.put(game.getCode(), game);
    }

    public Optional<Game> findByCode(String code) {
        return Optional.ofNullable(games.get(code));
    }

    public boolean existsByCode(String code) {
        return games.containsKey(code);
    }

    public void delete(String code) {
        games.remove(code);
    }
}
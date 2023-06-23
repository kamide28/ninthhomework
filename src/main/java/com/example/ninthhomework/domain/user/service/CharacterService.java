package com.example.ninthhomework.domain.user.service;

import com.example.ninthhomework.domain.user.model.Character;

import java.util.List;

public interface CharacterService {

    public List<Character> getCharacters();

    public List<Character> findByAge(Integer age);

    public Character createCharacter(String name, Integer age);

    public Character updateCharacter(int id, String name, Integer age);

    public Character findById(int id);

    public void deleteCharacter(int id);

}

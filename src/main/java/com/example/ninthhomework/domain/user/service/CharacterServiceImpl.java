package com.example.ninthhomework.domain.user.service;

import com.example.ninthhomework.domain.user.model.Character;
import com.example.ninthhomework.exception.NotFoundException;
import com.example.ninthhomework.mapper.CharacterMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CharacterServiceImpl implements CharacterService {
    private final CharacterMapper characterMapper;

    public CharacterServiceImpl(CharacterMapper characterMapper) {
        this.characterMapper = characterMapper;
    }

    @Override
    public List<Character> getCharacters() {
        return characterMapper.findAll();
    }

    public List<Character> findByAge(Integer age) {
        if (Objects.isNull(age)) {
            return characterMapper.findAll();
        } else {
            return characterMapper.searchByAge(age);
        }
    }

    public Character findById(int id) {
        return this.characterMapper.searchById(id).orElseThrow(() -> new NotFoundException(id));
    }

    public Character createCharacter(String name, Integer age) {
        Character character = new Character(name, age);
        characterMapper.createCharacter(character);
        return character;
    }

    public Character updateCharacter(int id, String name, Integer age) {
        Character character = characterMapper.searchById(id).orElseThrow(() -> new NotFoundException(id));
        character.update(name, age);
        characterMapper.updateCharacter(character);
        return character;
    }

    public void deleteCharacter(int id) {
        characterMapper.searchById(id).orElseThrow(() -> new NotFoundException(id));
        characterMapper.deleteCharacter(id);
    }
}

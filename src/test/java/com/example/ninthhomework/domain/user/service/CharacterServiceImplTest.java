package com.example.ninthhomework.domain.user.service;

import com.example.ninthhomework.controller.CreateForm;
import com.example.ninthhomework.domain.user.model.Character;
import com.example.ninthhomework.exception.NotFoundException;
import com.example.ninthhomework.mapper.CharacterMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceImplTest {
    @InjectMocks
    CharacterServiceImpl charactersServiceImpl;

    @Mock
    CharacterMapper characterMapper;

    @Test
    public void 全てのキャラクターの情報を返すこと() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(1, "mei", 5));
        characters.add(new Character(2, "satuki", 10));
        characters.add(new Character(3, "tatuo", 32));
        doReturn(characters).when(characterMapper).findAll();
        List<Character> actual = charactersServiceImpl.getCharacters();
        assertThat(actual).isEqualTo(characters);
        verify(characterMapper, times(1)).findAll();
    }

    @Test
    public void リストが空の時は空で返すこと() {
        List<Character> characters = new ArrayList<>();
        doReturn(characters).when(characterMapper).findAll();
        List<Character> actual = charactersServiceImpl.getCharacters();
        assertThat(actual).isEqualTo(characters);
        verify(characterMapper, times(1)).findAll();
    }

    @Test
    public void 指定年齢より年上のデータを返すこと() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(3, "tatuo", 32));
        doReturn(characters).when(characterMapper).searchByAge(30);
        List<Character> actual = charactersServiceImpl.findByAge(30);
        assertThat(actual).isEqualTo(characters);
        verify(characterMapper, times(1)).searchByAge(30);
        verify(characterMapper, never()).findAll();
    }

    @Test
    public void 年齢未指定の場合全てのデータを返すこと() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(1, "mei", 5));
        characters.add(new Character(2, "satuki", 10));
        characters.add(new Character(3, "tatuo", 32));
        doReturn(characters).when(characterMapper).findAll();
        List<Character> actual = charactersServiceImpl.findByAge(null);
        assertThat(actual).isEqualTo(characters);
        verify(characterMapper, never()).searchByAge(null);
        verify(characterMapper, times(1)).findAll();
    }

    @Test
    public void 指定年齢以上のデータが存在しない時空データを返すこと() {
        List<Character> characters = new ArrayList<>();
        doReturn(characters).when(characterMapper).searchByAge(99);
        List<Character> actual = charactersServiceImpl.findByAge(99);
        assertThat(actual).isEqualTo(characters);
        verify(characterMapper, times(1)).searchByAge(99);
        verify(characterMapper, never()).findAll();
    }

    @Test
    public void 指定IDのデータを返すこと() throws NotFoundException {
        Optional<Character> character = Optional.of(new Character(1, "mei", 5));
        doReturn(character).when(characterMapper).searchById(1);
        Character actual = charactersServiceImpl.findById(1);
        assertThat(actual).isEqualTo(new Character(1, "mei", 5));
        verify(characterMapper, times(1)).searchById(1);
    }

    @Test
    public void 指定IDが存在しない時例外をスローすること() {
        doReturn(Optional.empty()).when(characterMapper).searchById(99);
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> {
                    Character actual = charactersServiceImpl.findById(99);
                }).withMessageMatching("ID:99は見つかりませんでした");
        verify(characterMapper, times(1)).searchById(99);
    }

    @Test
    public void 自動採番されたIDに入力データが登録できること() {
        CreateForm createdCharacter = new CreateForm("mei", 5);
        Character character = new Character(createdCharacter.getName(), createdCharacter.getAge());
        doNothing().when(characterMapper).createCharacter(character);
        charactersServiceImpl.createCharacter(createdCharacter.getName(), createdCharacter.getAge());

        verify(characterMapper, times(1)).createCharacter(character);
    }

    @Test
    public void 指定されたIDのデータ更新ができること() {
        doReturn(Optional.of(new Character("mei", 5))).when(characterMapper).searchById(1);

        Character character = new Character(1, "satuki", 10);
        Character updateCharacter = charactersServiceImpl.updateCharacter(
                character.getId(), character.getName(), character.getAge());

        verify(characterMapper, times(1)).searchById(1);
        verify(characterMapper, times(1)).updateCharacter(updateCharacter);
    }

    @Test
    public void 年齢の入力がなくとも更新作業を実施できること() {
        doReturn(Optional.of(new Character("mei", 5))).when(characterMapper).searchById(1);

        Character character = new Character(1, "satuki", null);
        Character updateCharacter = charactersServiceImpl.updateCharacter(
                character.getId(), character.getName(), character.getAge());

        assertThat(updateCharacter.getName()).isEqualTo("satuki");
        assertThat(updateCharacter.getAge()).isEqualTo(5);
    }

    @Test
    public void 名前の入力がなくとも更新作業ができること() {
        doReturn(Optional.of(new Character("mei", 5))).when(characterMapper).searchById(1);

        Character character = new Character(1, null, 10);
        Character updateCharacter = charactersServiceImpl.updateCharacter(
                character.getId(), character.getName(), character.getAge());

        assertThat(updateCharacter.getAge()).isEqualTo(10);
        assertThat(updateCharacter.getName()).isEqualTo("mei");
    }

    @Test
    public void 更新用指定IDが存在しない時例外をスローすること() {
        doReturn(Optional.empty()).when(characterMapper).searchById(99);
        Character character = new Character(99, "satuki", 10);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> {
                    charactersServiceImpl.updateCharacter(
                            character.getId(), character.getName(), character.getAge());
                }).withMessageMatching("ID:99は見つかりませんでした");

        verify(characterMapper, times(1)).searchById(99);
        verify(characterMapper, never()).updateCharacter(character);
    }

    @Test
    public void 指定されたIDのデータを削除すること() {
        doReturn(Optional.of(new Character("mei", 5))).when(characterMapper).searchById(1);
        charactersServiceImpl.deleteCharacter(1);

        verify(characterMapper, times(1)).searchById(1);
        verify(characterMapper, times(1)).deleteCharacter(1);
    }

    @Test
    public void 削除対象のIDが存在しない時は例外をスローすること() {
        doReturn(Optional.empty()).when(characterMapper).searchById(99);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> {
                    charactersServiceImpl.deleteCharacter(99);
                }).withMessageMatching("ID:99は見つかりませんでした");
        verify(characterMapper, times(1)).searchById(99);
        verify(characterMapper, never()).deleteCharacter(1);
    }
}

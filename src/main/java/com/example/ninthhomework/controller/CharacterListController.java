package com.example.ninthhomework.controller;

import com.example.ninthhomework.domain.user.model.Character;
import com.example.ninthhomework.domain.user.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
public class CharacterListController {
    @Autowired
    private final CharacterService characterService;

    public CharacterListController(CharacterService characterService) {
        this.characterService = characterService;
    }

    //指定したIDの内容のみ返す
    @GetMapping("/characters/{id}")
    public Character findCharacterById(@PathVariable("id") int id) {
        return characterService.findById(id);
    }

    // このAPIはController層でResponseクラスへ変換する方法を試すために作りました
    @GetMapping("/characters-without-id")
    public List<CharacterResponse> selectCharacters() {
        return characterService.getCharacters().stream().map(y -> new CharacterResponse(y.getName(), y.getAge())).toList();
    }

    //クエリに指定がない時は全件、指定ありの時は年齢でフィルターをかけて表示
    @GetMapping("/characters")
    public List<Character> findCharacterByAge(@RequestParam(name = "age", required = false) Integer age) {
        return characterService.findByAge(age);
    }

    @PostMapping("/characters")
    public ResponseEntity<Map<String, String>> create
            (@RequestBody @Validated CreateForm createForm, UriComponentsBuilder uriBuilder) {
        Character character = characterService.createCharacter(createForm.getName(), createForm.getAge());

        URI url = uriBuilder
                .path("/characters/" + character.getId())
                .build()
                .toUri();
        return ResponseEntity.created(url).body(Map.of("message", "character successfully created"));
    }

    @PatchMapping("/characters/{id}")
    public ResponseEntity<Map<String, String>> update
            (@PathVariable("id") int id, @RequestBody UpdateForm updateForm) {
        characterService.updateCharacter(id, updateForm.getName(), updateForm.getAge());
        return ResponseEntity.ok(Map.of("message", "character successfully updated"));
    }

    @DeleteMapping("characters/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable("id") int id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.ok(Map.of("message", "character successfully deleted"));
    }
}


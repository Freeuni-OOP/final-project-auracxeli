package com.auracxeli.admin;

import com.auracxeli.wordle.WordleWord;
import com.auracxeli.wordle.WordleWordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminWordService {

    private final WordleWordRepository wordleWordRepository;

    public AdminWordService(WordleWordRepository wordleWordRepository) {
        this.wordleWordRepository = wordleWordRepository;
    }

    public List<WordleWord> listWords() {
        return wordleWordRepository.findAll();
    }

    @Transactional
    public WordleWord addWord(String word, LocalDate scheduledDate, Long addedBy) {
        if (wordleWordRepository.existsByWord(word)) {
            throw new DuplicateWordException("word", "ეს სიტყვა უკვე დამატებულია");
        }
        if (scheduledDate != null && wordleWordRepository.findByScheduledDate(scheduledDate).isPresent()) {
            throw new DuplicateWordException("scheduledDate", "ამ თარიღზე სიტყვა უკვე დანიშნულია");
        }
        return wordleWordRepository.save(new WordleWord(word, scheduledDate, addedBy));
    }
}
